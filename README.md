# TrustVault

TrustVault is a secure document-sharing application built with Java 21
and Spring Boot.

## Current Features

- User records stored using Spring Data JPA.
- Password hashing with BCrypt.
- Role model for ADMIN, OWNER, and VIEWER.
- Document ownership model.
- Document permission model with READ and EDIT permissions.
- Service-level checks for document access and permission revocation.
- Automated tests for user authentication components and document access logic.

## Technology Stack

- Java 21
- Spring Boot
- Spring Security
- Spring Data JPA
- H2 database for local development and testing
- Maven

## Run Locally

Set the development profile and credentials:

```bash
export SPRING_PROFILES_ACTIVE=dev
export TRUSTVAULT_DEV_OWNER_EMAIL=owner@example.test
read -s "TRUSTVAULT_DEV_OWNER_PASSWORD?Enter a temporary password (12+ characters): "
echo
export TRUSTVAULT_DEV_OWNER_PASSWORD
./mvnw spring-boot:run

