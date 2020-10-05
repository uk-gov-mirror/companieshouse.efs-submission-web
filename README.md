# efs-submission-web
The Emergency Filing Service web application allows users to file forms by uploading electronic documents.

- Provides fields for users to enter data
- Calls endpoints on the `efs-submission-api` as well as other internal services

The service integrates with a number of internal systems. This includes [company-lookup.web.ch.gov.uk](https://github.com/companieshouse/company-lookup.web.ch.gov.uk) and [file-transfer-api](https://github.com/companieshouse/file-transfer-api).

Requirements
------------
* [Git](https://git-scm.com/downloads)
* [Java](http://www.oracle.com/technetwork/java/javase/downloads/jdk8-downloads-2133151.html)
* [Maven](https://maven.apache.org/download.cgi)
* [efs-submission-api](https://github.com/companieshouse/efs-submission-api)
* Internal Companies House core services

Building and Running Locally
-----------------------------
**Note**: As this project has dependencies on internal Companies House libraries, you will need access to private GitHub repositories to build successfully. To run the service locally, you will need the CHS developer environment.  

1. From the command line, in the same folder as the Makefile run `make clean build`
1. Configure project environment variables where necessary
1. Start the service in the CHS developer environment
1. Access the web application, running in the CHS developer environment, on the host and port configured in application.properties