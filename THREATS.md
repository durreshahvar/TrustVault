# TrustVault — Threat Model

## 1. Assets to Protect

- User credentials and password hashes.
- Uploaded documents and their contents.
- Document ownership and sharing permissions.
- Audit records of security-sensitive actions.

## 2. Trust Boundaries

- Clients are untrusted; requests may be modified or replayed.
- Authentication does not grant access to every document.
- Authorization must be checked for each document operation.
- Database files and uploaded documents must not be directly exposed.

## 3. Threats and Mitigations

### T1 — Unauthorized Document Access

**Threat:** A user changes a document ID to access another user's document.

**Mitigation:** Enforce ownership or explicit permission checks for document operations.

**Status:** Service-level access checks and automated tests exist. Complete API-level verification remains necessary.

### T2 — Permission Escalation

**Threat:** A viewer grants access to others or upgrades their own permission.

**Mitigation:** Restrict grant and revoke operations to authorized users.

**Status:** Service-level permission checks are implemented. Verify all API authorization paths and role boundaries.

### T3 — Access After Revocation

**Threat:** A user accesses a document after their permission is revoked.

**Mitigation:** Check current permissions when handling each access request.

**Status:** Revocation logic has automated test coverage. Additional end-to-end tests are recommended.

### T4 — Malicious or Unsafe File Uploads

**Threat:** An attacker uploads oversized files, misleading file types, or malicious content.

**Mitigation:** Apply upload size limits, use generated storage keys, and keep uploaded files in private storage.

**Status:** File upload and download handling are implemented. MIME-type validation, file signature verification, malware scanning, and broader upload security testing require further review.

### T5 — Credential Exposure

**Threat:** Passwords or development credentials are exposed in source control.

**Mitigation:** Hash passwords, load development credentials from environment variables, and exclude local data from Git.

**Status:** BCrypt and environment-based development credentials are implemented. Production secret management and credential rotation remain necessary.

### T6 — Audit Log Tampering or Gaps

**Threat:** Security events are omitted, modified, or deleted.

**Mitigation:** Record relevant actors, actions, document references, outcomes, and timestamps. Restrict access and consider tamper-evident storage.

**Status:** Audit event persistence and recording calls have been implemented. Tamper-evident integrity, restricted audit administration, and comprehensive event-coverage verification are not established.

### T7 — Information Leakage

**Threat:** Error responses reveal private document details, storage paths, or internal exceptions.

**Mitigation:** Return appropriate errors and avoid exposing internal implementation details.

**Status:** Initial access-denied handling exists. Review API responses and error paths comprehensively.

### T8 — Unsafe Deployment Configuration

**Threat:** Development credentials, insecure transport, or local storage are exposed in a public deployment.

**Mitigation:** Use HTTPS, production-grade secret management, private storage, least-privilege permissions, and a deployment-specific security review.

**Status:** The current configuration uses local H2 storage and local filesystem storage. Production deployment hardening remains necessary.

## 4. Security Test Plan

- Verify owner, READ, EDIT, and ungranted-user access.
- Verify access is denied after permission revocation.
- Test unauthorized upload and download attempts.
- Test unauthorized grant and revoke attempts.
- Test oversized files and invalid or misleading file types.
- Verify denied attempts produce audit events where required.
- Verify files cannot be retrieved without authorization.
- Review every API endpoint for authorization and information leakage.

## 5. Current Limitations

TrustVault is a development project. The current automated test suite does not prove complete security.

Local database and filesystem storage are not a production deployment architecture. File validation, API-level authorization coverage, audit integrity, HTTPS, production secrets, and deployment hardening require further verification before production use.
