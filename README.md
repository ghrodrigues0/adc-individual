# User Management REST API

A user management backend in **Java 21** with **JAX-RS (Jersey 3)**, deployed on **Google App Engine** and backed by **Google Cloud Datastore**. It covers registration, login with expiring session tokens, role-based access control and account administration, plus a small HTML/JavaScript client to call every endpoint.

Individual project for the *ADC* course at NOVA School of Science and Technology (NOVA FCT), built on the course's starter template ([ADC-Projeto](https://github.com/ADC-Projeto/adc-pei-2526-part3)), which provided the project skeleton and App Engine setup.

## Stack

- Java 21, Maven
- JAX-RS with Jersey 3.1, Jackson for JSON
- Google App Engine (Java 21 standard runtime)
- Google Cloud Datastore
- HTML, CSS and vanilla JavaScript client (`src/main/webapp`)

## Features

- **Session tokens:** a successful login creates a session with a random UUID token that expires after **15 minutes**. Sessions are stored in Datastore, so they can be listed and revoked.
- **Three roles:** `USER`, `BOFFICER` (back office) and `ADMIN`. Each endpoint checks the caller's role from the session before doing anything.
- **Transactions:** registration, login (session plus audit log), password, role and account changes, and account deletion all run inside Datastore transactions, with rollback on failure.
- **Login audit log:** each successful login records IP, host, time and the approximate location App Engine provides (city, country, lat/long), as a child entity of the user.
- **Input validation:** usernames must contain `@`, phone numbers must have 9 digits, and the password must match its confirmation.

## Security changes after the course

The version submitted for the course stored unsalted SHA-512 hashes, let the client pick its own role at registration (including `ADMIN`), and told apart "user not found" from "wrong password" at login. I fixed all three before publishing:

- **bcrypt for passwords** (`PasswordHasher`). Accounts that still hold an old SHA-512 hash can log in once and are rehashed with bcrypt in the same transaction, so no one has to reset a password.
- **Registration always creates a `USER`.** Any `role` sent by the client is ignored; only an `ADMIN` can change roles, through `/changeuserrole`.
- **One login error for both cases.** An unknown username and a wrong password return the same error, and the unknown-username path still runs a bcrypt check so response times don't reveal which accounts exist.

## Endpoints

All endpoints live under `/rest`, accept and return JSON, and use `POST`. Authenticated calls send the session token in the request body.

| Endpoint | Who can call it | What it does |
|---|---|---|
| `/createaccount` | anyone | Register a user (always with role `USER`) |
| `/login` | anyone | Check credentials, open a session, log the login |
| `/logout` | the user, or `ADMIN` for anyone | Close a session |
| `/changeuserpwd` | the user | Change own password (requires the old one) |
| `/modaccount` | the user; `BOFFICER` also for `USER` accounts; `ADMIN` for anyone | Update phone and address |
| `/changeuserrole` | `ADMIN` | Change another user's role |
| `/deleteaccount` | `ADMIN` | Delete a user and all their sessions |
| `/showusers` | `BOFFICER`, `ADMIN` | List users |
| `/showuserrole` | `BOFFICER`, `ADMIN` | Show a user's role |
| `/showauthsessions` | `ADMIN` | List active sessions |

### Example: login

```http
POST /rest/login
Content-Type: application/json

{ "input": { "username": "ana@example.com", "password": "..." } }
```

A successful response looks like this, and the client sends the `token` object back in the body of every authenticated call:

```json
{
  "status": "success",
  "data": {
    "token": {
      "tokenId": "3f6c1e0a-...",
      "username": "ana@example.com",
      "role": "USER",
      "issuedAt": 1767225600,
      "expiresAt": 1767226500
    }
  }
}
```

### Error codes

The API contract was set by the course assignment: errors are returned with HTTP 200 and an application-level code in the body.

| Code | Meaning |
|---|---|
| 9900 | Invalid username/password pair |
| 9901 | Username already exists |
| 9902 | User not found |
| 9903 | Invalid token |
| 9904 | Expired token |
| 9905 | Operation not allowed for this role |
| 9906 | Invalid input |
| 9907 | Forbidden for another reason |

## Data model (Datastore)

- `User`: keyed by username. Password hash, phone, address, role, creation time.
- `UserSession`: keyed by token id. Username, role, issue and expiry times.
- `UserLog`: child of `User`, one entity per login.

## Running it

You need a Google Cloud project with App Engine and Datastore enabled, and the `gcloud` CLI authenticated.

1. Build and deploy with the App Engine Maven plugin, passing your project ID:

   ```bash
   mvn clean package appengine:deploy -Dapp.deploy.projectId=YOUR_PROJECT_ID -Dapp.deploy.version=GCLOUD_CONFIG
   ```

   In PowerShell, put each `-D` argument in quotes.

2. Open the deployed URL. `index.html` links to a page for each operation.

**Creating the first admin:** registration only creates `USER` accounts. Register an account, then set its `role` property to `ADMIN` on the `User` entity in the Datastore console. From there, that admin can promote others with `/changeuserrole`.

## Known limitations and next steps

The API contract still follows the course specification. These are the things I would change for production use, and the improvements I'm working on next:

- **HTTP semantics:** use proper status codes (400, 401, 403, 404, 409) instead of 200 with an error code, `GET`/`PUT`/`DELETE` where they fit, and the token in the `Authorization` header instead of the body.
- **Duplicated auth checks:** each resource validates the token itself. This belongs in a single `ContainerRequestFilter`.
- **Tests and CI:** no automated tests yet. Next are JUnit 5 unit tests, integration tests against the Datastore emulator, and a GitHub Actions workflow.
