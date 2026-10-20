# TrustVault — Threat Model

## 1. Assets to Protect
- User credentials and password hashes.
- Uploaded documents and their contents.
- Document ownership and sharing permissions.
- Audit records of security-sensitive actions.

## 2. Trust Boundaries
- The client is untrusted; requests can be modified or replayed.
- Authentication does not automatically grant access to every document.
- The server must verify authorization for every document operation.
- Database and file storage must not be directly exposed to users.

## 3. Threats and Mitigations

### T1 — Unauthorized Document Access
**Threat:** A user changes a document ID to access another user's document.

**Mitigation:** Check ownership or an explicit permission grant on every request.

**Status:** Initial service-level ownership and access tests exist. Complete API-level verification is pending.

### T2 — Permission Escalation
**Threat:** A viewer grants access to others or upgrades their own permission.

**Mitigation:** Only the document owner or an explicitly authorized administrator may manage permissions. Verify the caller on every grant and revoke request.

**Status:** Service-level checks exist; API-level tests remain necessary.

### T3 — Access After Revocation
**Threat:** A user continues to access a document after permission is revoked.

**Mitigation:** Check current permissions on every request. Avoid public URLs or long-lived download links that bypass authorization.

**Status:** Revocation logic has an automated test.

### T4 — Malicious File Uploads
**Threat:** An attacker uploads oversized files, misleading file types, or malicious content.

**Mitigation:** Enforce size limits, allowlisted file types, server-side validation, generated storage keys, and private storage.

**Status:** Upload handling is not yet implemented.

### T5 — Credential Exposure
**Threat:** Passwords or development credentials are exposed in source control.

**Mitigation:** Hash passwords, load secrets from environment variables or a secret manager, and exclude local database files and secrets from Git.

**Status:** BCrypt password hashing and environment-based development credentials are implemented. Production secret management remains pending.

### T6 — Audit Log Tampering
**Threat:** Security events are omitted, altered, or deleted.

**Mitigation:** Record the actor, action, document, timestamp, and outcome. Restrict modifications and use a tamper-evident integrity mechanism.

**Status:** Audit logging and tamper-evidence are not yet implemented.

### T7 — Information Leakage
**Threat:** Error messages reveal private document or account information.

**Mitigation:** Use consistent authorization errors and avoid exposing storage paths or internal exception details.

**Status:** Initial service checks use a generic access-denied message. API responses require further review.

## 4. Security Test Plan
- Test owner, READ, EDIT, and ungranted-user access.
- Verify access is denied after revocation.
- Test unauthorized upload, download, edit, delete, grant, and revoke requests.
- Test invalid file types and oversized uploads.
- Verify denied attempts produce audit events.
- Verify files cannot be retrieved without server-side authorization.
- Review every API endpoint for authorization and information leakage.

## 5. Current Limitations
TrustVault is under development. Passing the current automated tests does not prove the complete application is secure. File upload and private storage, complete API authorization, audit logging, and deployment security must be implemented and verified before production use.
