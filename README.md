# CPCDS Reference Server

This project is a reference FHIR server for the [Consumer-Directed Payer Data Exchange Implementation Guide](https://build.fhir.org/ig/HL7/carin-bb/toc.html) (also know as Carin Blue Button Implementation Guide). It is based on the [HAPI FHIR JPA Server](https://github.com/hapifhir/hapi-fhir-jpaserver-starter) (HAPI 8.0.0).

The server is hosted live at https://cpcds-server.lantanagroup.com/fhir

## Prerequisites
Building and running the server locally requires either Docker or
- Java 17+
- Maven

## Quickstart

The quickest way to get the server up and running is by pulling the built image from docker hub.

```bash
docker pull lantanagroup/cpcds-server-ri
docker run -p 8080:8080 -e lantanagroup/cpcds-server-ri
```

This will deploy the server to http://localhost:8080/fhir

Note: A docker-compose file is also included which can be configured for your use case. To run using compose use `docker-compose up`.

## Building Locally with Docker

The docker image can also be built locally before running. The container will automatically build and deploy using a tomcat server. You may need to configure some settings in `docker-compose.yml` before running.

```bash
git clone https://github.com/carin-alliance/cpcds-server-ri.git
cd cpcds-server-ri
docker-compose up
```

Alternatively you can build and run using normal docker commands:

```bash
docker build -t cpcds-server-ri .
docker run -p 8080:8080 cpcds-server-ri
```

This will build a read only version of the server with the test data pre-loaded. The server will then be browesable at http://localhost:8080/ and the FHIR endpoint will be available at http://localhost:8080/fhir

## Manual Build and Run

Clone the repo and build the server:

```bash
git clone https://github.com/carin-alliance/cpcds-server-ri.git
cd cpcds-server-ri
mvn spring-boot:run
```

The server will then be browseable at http://localhost:8080/ and the FHIR endpoint will be available at http://localhost:8080/fhir/


## Security Configuration

By default, this server is protected and requires users to authenticate before obtaining access to protected resources. The authorization server is hosted at the `/oauth` endpoint. The Capability Statement for this server is at the `/fhir/metadata` endpoint. The smart configuration file is at `/fhir/.well-known/smart-configuration`. Even with an access token, only the resources marked as must support in the IG will be available.

Security can be bypassed by using the admin token. The admin token is set in the `security.admin_token` configuration variable. If this variable is not set, the admin token will not be available. This token defaults to the value `admin` and would be supplied in an authorization header such as: `Authorization: Bearer admin`

Security can be disabled by setting the `security.enabled` property to `false`.

## Uploading Test Data

The server is configured by default to be read only.  To write data to the server, you will need to either supply the admin token (if configured) in the request or set the `security.read_only` property to `false`.


## Configuration

Beyond on the normal HAPI configuration found in `src/main/resources/application.yaml`, this server has a few other configurations.

| ENV            | Default | Description                                                                                           |
| -------------- | -------- | ----------------------------------------------------------------------------------------------------- |
| SECURITY_ENABLED | `true` | If set to false, security will be disabled. |
| SECURITY_ADMIN_TOKEN | `admin` | The value of the admin token to bypass authorization. If unset no admin token can be used. |
| SECURITY_READ_ONLY | `true` | If set to true, the server will be read only. |


## Security Considerations

Since this code base serves as the reference implementation for the Carin BB IG there are multiple places where potential security vulnerabilities were intentionally made to allow testing developers to debug their code. With these vulnerabilities in place, testing and debugging connections is substantially easier. If this code is to be used for a production enviornment care must be taken to ensure all vulnerabilities are fixed. Below is a list of _some_ of the identified issues. It is your responsibility to make sure all vulnerabilities, including those not listed below, are fixed in a production enviornment.

1. Logger statements print secrets - in places such as `User.java` and `Client.java` the logger displays the hashed password and client secret. Caution should be used any time a secret value is logged. Care should be taken to protect the log files from malicious users.
2. Debug endpoint - the debug endpoint provides public access to the Users and Client table which provides hashed passwords and client secrets. This endpoint also provides public access to the log file. The debug endpoint should be removed for a production enviornment.
3. Managing keys - the RSA keys used to sign and validate the JWT tokens are hard coded in `App.java`. Your implementation must change these keys and ensure they are stored in a secure location. Consider having rotating keys.

This may not be an exhaustive list. The developers of this reference implementations are not responsible for any vulnerabilities in the code base. All use of this repository comes with the understanding this reference implementation is used for testing connections only.


## Questions and Contributions
Questions about the project can be asked in the [CARIN BlueButton stream on the FHIR Zulip Chat](https://chat.fhir.org/#narrow/stream/204607-CARIN-IG-for-Blue-Button.C2.AE).

This project welcomes Pull Requests. Any issues identified with the RI should be submitted via the [GitHub issue tracker](https://github.com/carin-alliance/cpcds-server-ri/issues).

As of October 1, 2022, The Lantana Consulting Group is responsible for the management and maintenance of this Reference Implementation.
In addition to posting on FHIR Zulip Chat channel mentioned above you can contact [Corey Spears](mailto:corey.spears@lantanagroup.com) for questions or requests.
