# TrustVault — AI Development Log

This document records AI-assisted development and the verification performed by the developer. Entries should reflect actual interactions and changes.

## Entry 1 — Project setup and authentication

**Date:** 2026-10-20

**Goal:** Set up the Spring Boot project and establish a basic authentication foundation.

**Prompt summary:** I asked for step-by-step help setting up TrustVault locally on macOS, using Java and Spring Boot, and verifying the application through Terminal.

**Work performed:** Configured the project, added database-backed user lookup and password hashing, and set up a protected health endpoint.

**Verification:** The health endpoint returned HTTP 401 without valid credentials and HTTP 200 with valid credentials. The test suite subsequently passed.

**Corrections and lessons:** An HTTP 401 response during an unauthenticated request was expected behavior, not an application failure. The database and local credentials must not be committed to source control.

**Commit link:** Pending — add the real Git commit URL after committing this work.

## Entry 2 — Document permissions and access control

**Date:** 2026-10-20

**Goal:** Introduce document ownership and READ/EDIT permission concepts.

**Prompt summary:** I asked for guidance implementing document access checks, granting and revoking permissions, and writing tests for owners, readers, editors, and ungranted users.

**Work performed:** Added document and permission models, repository support, and service-level access checks.

**Verification:** The automated test suite reported 12 tests, zero failures, and zero errors.

**Corrections and lessons:** A successful build does not prove that every API endpoint or file operation is secure. API-level authorization and end-to-end file access tests are still required.

**Commit link:** Pending — add the real Git commit URL after committing this work.

## Entry 3 — Security documentation

**Date:** 2026-10-20

**Goal:** Document security risks and current implementation limitations.

**Prompt summary:** I asked for help documenting threats involving unauthorized document access, permission escalation, revocation, unsafe uploads, credential exposure, audit integrity, and information leakage.

**Work performed:** Created a threat model distinguishing existing controls from features that remain unimplemented.

**Verification:** Confirmed that THREATS.md contains the threat model.

**Corrections and lessons:** Documentation must not describe planned controls as implemented. Upload validation, private file storage, and tamper-evident audit logging still require implementation and testing.

**Commit link:** Pending — add the real Git commit URL after committing this work.

## Ongoing record

For each additional AI-assisted change, record the actual prompt, relevant output, problems discovered, the correction made, the test results, and the resulting commit URL. Do not fabricate prompt history or commit links.
