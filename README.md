# CPCDS Reference Server

This project is a reference FHIR server for the [Consumer-Directed Payer Data Exchange Implementation Guide](https://build.fhir.org/ig/HL7/carin-bb/toc.html) (also know as Carin Blue Button Implementation Guide). It is based on the [HAPI FHIR JPA Server](https://github.com/hapifhir/hapi-fhir-jpaserver-starter) (HAPI 4.1.0).

The server is hosted live at http://cpcds-ri.org:8080/cpcds-server/fhir.
For more information on hte AWS server visit the [wiki](https://github.com/carin-alliance/cpcds-server-ri/wiki/AWS-Reference-Implementation).

## Quickstart

The quickest way to get the server up and running is by pulling the built image from docker hub.

```bash
docker pull blangley/cpcds-server-ri
docker run -p 8080:8080 blangley/cpcds-server-ri
```

This will deploy the server to http://localhost:8080/cpcds-server/fhir.

## Building Locally with Docker

To start the server simply build and run using docker. The container will automatically build and deploy using a tomcat server.

```bash
git clone https://github.com/carin-alliance/cpcds-server-ri.git
cd cpcds-server-ri
docker build -t cpcds-server-ri .
docker run -p 8080:8080 cpcds-server-ri
```

This will build a read only version of the server with the test data pre-loaded. The server will then be browesable at http://localhost:8080/cpcds-server and the FHIR endpoint will be available at http://localhost:8080/cpcds-server/fhir

## Manual Build and Run

Clone the repo and build the server:

```bash
git clone https://github.com/carin-alliance/cpcds-server-ri.git
cd cpcds-server-ri
mvn dependency:resolve
mvn jetty:run
```

The server will then be browseable at http://localhost:8080/cpcds-server and the FHIR endpoint will be available at http://localhost:8080/cpcds-server/fhir

Note: This has only been tested using Java 8. Later version may not be supported.

## GET Requests

This server is protected and requires users to authenticate before obtaining access to protected resources. The authorization server is hosted at the `/oauth` endpoint. The Capability Statement for this server is at the `/fhir/metadata` endpoint. The smart configuration file is at `/.well-known/smart-configuration`. Even with an access token, only the resources marked as must support in the IG will be available.

Once an access token is received it must be sent in the `Authorization` header using the correct `token_type` returned by the auth server. Learn more about the server authorization from the [wiki](https://github.com/carin-alliance/cpcds-server-ri/wiki)

## Configuration

If the address of CPCDS server changes it must be updated in `src/main/resources/hapi.properties` by modifying the `server_address` property respectivelty.

## Uploading Test Data

The master branch of this repository sets up the server as read only. Uploading to the server will fail. To disable the read only interceptor switch to the `cpcds-write` branch. The database is read from `target/database/h2.mv.db`. A copy of the database with the test data preloaded can be found in the `/data` directory. Copying this into `target/database` will allow the server to have a read only copy of the loaded database.

## Security

Since this code base serves as the reference implementation for the Carin BB IG there are multiple places where potential security vulnerabilities were intentionally made to allow testing developers to debug their code. With these vulnerabilities in place, testing and debugging connections is substantially easier. If this code is to be used for a production enviornment care must be taken to ensure all vulnerabilities are fixed. Below is a list of _some_ of the identified issues. It is your responsibility to make sure all vulnerabilities, including those not listed below, are fixed in a production enviornment.

1. Logger statements print secrets - in places such as `User.java` and `Client.java` the logger displays the hashed password and client secret. Caution should be used any time a secret value is logged. Care should be taken to protect the log files from malicious users.
2. Debug endpoint - the debug endpoint provides public access to the Users and Client table which provides hashed passwords and client secrets. This endpoint also provides public access to the log file. The debug endpoint should be removed for a production enviornment.
3. Managing keys - the RSA keys used to sign and validate the JWT tokens are hard coded in `App.java`. Your implementation must change these keys and ensure they are stored in a secure location. Consider having rotating keys.

This may not be an exhaustive list. The developers of this reference implementations are not responsible for any vulnerabilities in the code base. All use of this repository comes with the understanding this reference implementation is used for testing connections only.
