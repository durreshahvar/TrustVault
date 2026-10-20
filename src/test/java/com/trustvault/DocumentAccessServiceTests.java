package com.trustvault;
import com.trustvault.permission.DocumentPermission;
import com.trustvault.permission.DocumentGrantRepository;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.trustvault.document.Document;
import com.trustvault.document.DocumentAccessService;
import com.trustvault.document.DocumentRepository;
import com.trustvault.user.AppUser;
import com.trustvault.user.AppUserRepository;
import com.trustvault.user.UserRole;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@Transactional
class DocumentAccessServiceTests {

    @Autowired
    private DocumentAccessService documentAccessService;

    @Autowired
    private DocumentRepository documentRepository;

    @Autowired
    private AppUserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;
@Autowired
private DocumentGrantRepository grantRepository;

    @Test
    void ownerCanAccessTheirOwnDocument() {
        AppUser owner = createUser("owner1@example.test");
        Document document = createDocument(owner);

        Document result = documentAccessService.getOwnedDocument(
                document.getId(), owner.getEmail());

        assertEquals(document.getId(), result.getId());
    }

    @Test
    void differentOwnerCannotAccessDocument() {
        AppUser owner = createUser("owner2@example.test");
        AppUser otherOwner = createUser("owner3@example.test");
        Document document = createDocument(owner);

        assertThrows(
                AccessDeniedException.class,
                () -> documentAccessService.getOwnedDocument(
                        document.getId(), otherOwner.getEmail())
        );
    }
@Test
void readPermissionAllowsReadingButDeniesEditing() {
    AppUser owner = createUser("read-owner@example.test");
    AppUser viewer = createUser("read-viewer@example.test");
    Document document = createDocument(owner);

    documentAccessService.grantAccess(
            document.getId(), owner.getEmail(),
            viewer.getEmail(), DocumentPermission.READ);

    assertEquals(document.getId(),
            documentAccessService.getReadableDocument(
                    document.getId(), viewer.getEmail()).getId());

    assertThrows(AccessDeniedException.class,
            () -> documentAccessService.getEditableDocument(
                    document.getId(), viewer.getEmail()));
}

@Test
void editPermissionAllowsReadingAndEditing() {
    AppUser owner = createUser("edit-owner@example.test");
    AppUser editor = createUser("edit-user@example.test");
    Document document = createDocument(owner);

    documentAccessService.grantAccess(
            document.getId(), owner.getEmail(),
            editor.getEmail(), DocumentPermission.EDIT);

    assertEquals(document.getId(),
            documentAccessService.getReadableDocument(
                    document.getId(), editor.getEmail()).getId());

    assertEquals(document.getId(),
            documentAccessService.getEditableDocument(
                    document.getId(), editor.getEmail()).getId());
}

@Test
void revokingGrantRemovesAccess() {
    AppUser owner = createUser("revoke-owner@example.test");
    AppUser viewer = createUser("revoke-viewer@example.test");
    Document document = createDocument(owner);

    documentAccessService.grantAccess(
            document.getId(), owner.getEmail(),
            viewer.getEmail(), DocumentPermission.READ);

    documentAccessService.revokeAccess(
            document.getId(), owner.getEmail(), viewer.getEmail());

    assertThrows(AccessDeniedException.class,
            () -> documentAccessService.getReadableDocument(
                    document.getId(), viewer.getEmail()));
}

@Test
void ungrantedUserCannotReadDocument() {
    AppUser owner = createUser("private-owner@example.test");
    AppUser stranger = createUser("stranger@example.test");
    Document document = createDocument(owner);

    assertThrows(AccessDeniedException.class,
            () -> documentAccessService.getReadableDocument(
                    document.getId(), stranger.getEmail()));
}

    private AppUser createUser(String email) {
        AppUser user = new AppUser(
                email,
                passwordEncoder.encode("Test-Only-Password-123!"),
                UserRole.OWNER
        );
        return userRepository.save(user);
    }

    private Document createDocument(AppUser owner) {
        Document document = new Document(
                "test-document.txt",
                "test-storage-key-" + owner.getEmail(),
                "test",
                12,
                "text/plain",
                owner
        );
        return documentRepository.save(document);
    }
}
