# TrustVault

TrustVault is a secure document-sharing application built with Java 21
and Spring Boot. It provides authenticated APIs for document management,
permission-based sharing, file uploads and downloads, and audit events.

## Features

- Database-backed user authentication with BCrypt password hashing.
- Role model for ADMIN, OWNER, and VIEWER.
- Document ownership and READ/EDIT permissions.
- Document metadata creation and retrieval.
- Multipart document uploads and authenticated downloads.
- Grant and revoke document access.
- Audit events for document and access-control operations.
- Automated tests for authentication, repositories, security, and document access.

## Technology Stack

- Java 21
- Spring Boot
- Spring Security
- Spring Data JPA
- H2 database
- Maven
- Local filesystem storage

## Requirements

- Java 21
- Git (optional, for cloning the repository)

## Run Locally

Clone the repository and enter its directory:

    git clone https://github.com/durreshahvar/TrustVault.git
    cd TrustVault

Set the development profile and owner credentials:

    export SPRING_PROFILES_ACTIVE=dev
    export TRUSTVAULT_DEV_OWNER_EMAIL=owner@example.test
    read -s "TRUSTVAULT_DEV_OWNER_PASSWORD?Enter a temporary password: "
    echo
    export TRUSTVAULT_DEV_OWNER_PASSWORD

Start the application:

    ./mvnw spring-boot:run

The application runs on port 8080 by default.

Use a strong, unique development password. Do not commit credentials,
database files, or uploaded documents to source control.

## API Overview

All endpoints require authentication under the current Spring Security
configuration. API requests use HTTP Basic authentication.

| Method | Endpoint | Purpose |
|---|---|---|
| GET | /api/documents | List accessible document metadata |
| POST | /api/documents | Create document metadata |
| POST | /api/documents | Upload a document using multipart form data |
| GET | /api/documents/{documentId}/download | Download a document |
| POST | /api/documents/{documentId}/grants | Grant document access |
| DELETE | /api/documents/{documentId}/grants/{targetEmail} | Revoke document access |

The two POST /api/documents operations use different request content types.
Consult the controller implementation for the exact request fields.

## Run Tests

    ./mvnw test

## Storage and Security Notes

- H2 is configured as a local file database.
- Uploaded files are stored on the local filesystem.
- Passwords are hashed with BCrypt.
- Document authorization is enforced by application services.
- Audit events are stored in the application database.
- The current configuration is intended for development and evaluation,
  not as a claim of production security.
- Review THREATS.md for security risks and known limitations before deployment.

## Current Limitations

- Local H2 and filesystem storage are not a managed production deployment.
- File content validation and malware scanning require further review.
- Audit records are not currently described as tamper-evident.
- Production secret management, HTTPS, and deployment hardening remain necessary.
- Automated tests do not prove that every security scenario is covered.
