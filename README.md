# ADC-PEI 25/26 — First Web Application (Part 3)

Building on Part 2, this version introduces **user authentication** via a login endpoint (rudimentar).

---

## What changed from Part 2

- A new **`/rest/login`** endpoint was added to handle user authentication
- A **`LoginData`** utility class was added to deserialize login request bodies
- An **`AuthToken`** utility class was added to generate and represent authentication tokens

---

## What this app does

The app serves a simple web page with the following available services:

- **`POST /rest/login/`** — authenticates a user and returns a JSON auth token
- **`GET /rest/login/{username}`** — checks whether a username is already taken
- **`GET /rest/utils/hello`** — intentionally throws an exception and redirects to `/error/500.html`
- **`GET /rest/utils/time`** — returns the current server time in JSON

---

## Authentication

Login is handled by `LoginResource.java`. It accepts a JSON body with `username` and `password` fields.

**Example request:**
```json
{
  "username": "jleitao",
  "password": "password"
}
```

**On success**, the server returns a JSON `AuthToken` object, e.g.:
```json
{
  "username": "jleitao",
  "tokenID": "<uuid>",
  "creationData": 1234567890000,
  "expirationData": 1234575090000
}
```

**On failure**, the server returns `403 Forbidden` with the message `Incorrect username or password.`

The token is valid for **2 hours** from the time of creation.

---

## Prerequisites

Before you begin, make sure you have the following installed:

- **Google Cloud Account**
  > ⚠️ See the instructions to redeem your cupon in shared materials: **`ADC_Project_Google_Cloud_Account_Creation.pdf`**
- [Java 21](https://www.oracle.com/java/technologies/javase/jdk21-archive-downloads.html)
- [Python 3.10](https://www.python.org/downloads/release/python-3100/) (for the Google Cloud SDK download)
- [Apache Maven](https://maven.apache.org/install.html)
- [Git](https://git-scm.com/)
- [Google Cloud SDK](https://cloud.google.com/sdk/docs/install) (for cloud deployment)
- [Eclipse IDE](https://www.eclipse.org/downloads/) with the Maven plugin

---

## Getting Started

### 1. Fork and clone the repository

Fork the project on GitHub, then clone your fork locally:

```bash
git clone git@github.com:ADC-Projeto/adc-pei-2526-part3.git
cd adc-pei-2526-part3
```

### 2. Import into Eclipse

1. Open Eclipse and go to **File → Import → Maven → Existing Maven Projects**
2. Navigate to the folder where you cloned the project
3. Select it and click **Finish**
4. Eclipse will resolve dependencies automatically — check for any errors in the **Problems** tab

---

## Building the project

From the project root, run:

```bash
mvn clean package
```

If the build succeeds, you'll find the compiled `.war` file at:

```
target/Firstwebapp-0.0.1.war
```

---

## Running locally

Start the local App Engine dev server with:

```bash
mvn appengine:run
```

Then open your browser and go to:

```
http://localhost:8080/
```

You can test the login endpoint using **Postman** (recommended, matches the course slides) or `curl`.

### Testing with Postman

1. Open [Postman](https://www.postman.com/downloads/) and create a new request
2. Set the method to **POST** and the URL to `http://localhost:8080/rest/login/`
3. Go to the **Body** tab, select **raw** and set the format to **JSON**
4. Enter the following body and click **Send**:

```json
{
  "username": "jleitao",
  "password": "password"
}
```

A successful login returns `200 OK` with a JSON auth token. An incorrect login returns `403 Forbidden`.

To check username availability, create a new **GET** request to:
```
http://localhost:8080/rest/login/jleitao
```

Returns `200 OK` with `false` if the username is already taken, or `true` if it is available.

### Testing with curl (alternative on terminal)

```bash
# Successful login
curl -i -X POST http://localhost:8080/rest/login/ \
  -H "Content-Type: application/json" \
  -d '{"username":"jleitao","password":"password"}'

# Failed login
curl -i -X POST http://localhost:8080/rest/login/ \
  -H "Content-Type: application/json" \
  -d '{"username":"someother","password":"pass"}'

# Check username availability
curl http://localhost:8080/rest/login/jleitao
curl http://localhost:8080/rest/login/someother
```

---

## Deploying to Google App Engine

### 1. Create a project on Google Cloud Console

Go to https://console.cloud.google.com/ and create a new project. Take note of the Project ID (e.g. `my-adc-app`).

### 2. Authenticate with Google Cloud

```bash
gcloud auth login
gcloud config set project <your-proj-id>
```

### 3. Deploy

```bash
mvn appengine:deploy -Dapp.deploy.projectId=<your-proj-id> -Dapp.deploy.version=<version-number>
```

After a successful deploy, your app will be live at:

```
https://<your-project-id>.appspot.com/
```

---

## Project Structure

```
src/
└── main/
    ├── java/
    │   └── pt/unl/fct/di/adc/firstwebapp/
    │       ├── resources/
    │       │   ├── ComputationResource.java              ← Utility endpoints
    │       │   └── LoginResource.java                    ← Login endpoint
    │       └── util/
    │           ├── AuthToken.java                        ← Token model
    │           └── LoginData.java                        ← Login request model
    └── webapp/
        ├── index.html                                    ← Front page
        ├── error/
        │   ├── 404.html                                  ← Custom 404 page
        │   └── 500.html                                  ← Custom 500 page
        ├── img/
        │   ├── cat.png
        │   └── jedi.gif
        └── WEB-INF/
            ├── web.xml                                   ← Servlet config
            └── appengine-web.xml                         ← App Engine config
```

---

## License

See [LICENSE](LICENSE) for details.

---

*FCT NOVA — ADC-PEI 2025/2026*
