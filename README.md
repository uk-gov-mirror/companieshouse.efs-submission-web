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


## Building and Running Locally

**Note**: As this project has dependencies on internal Companies House libraries, you will need access to private GitHub repositories to build successfully. To run the service locally, you will need the CHS developer environment.  

1. From the command line, in the same folder as the Makefile run `make clean build`
1. Configure project environment variables where necessary
1. Start the service in the CHS developer environment
1. Access the web application, running in the CHS developer environment, on the host and port configured in application.properties

## Building the docker image 

    mvn -s settings.xml compile jib:dockerBuild -Dimage=169942020521.dkr.ecr.eu-west-1.amazonaws.com/local/efs-submission-api

## Running Locally using Docker

1. Clone [Docker CHS Development](https://github.com/companieshouse/docker-chs-development) and follow the steps in the README.

1. Enable the `efs` module

1. Run `tilt up` and wait for all services to start

**note**: The database is populated with potentially old data. If you need the most up to date categories, forms, or payment templates; follow step 6 of the local setup in the [API](https://github.com/companieshouse/efs-submission-api/blob/master/README.md#building-and-running-locally), or enable [devlopment mode for the API](https://github.com/companieshouse/efs-submission-api/blob/master/README.md#to-make-local-changes). 

### To make local changes

Development mode is available for this service in [Docker CHS Development](https://github.com/companieshouse/docker-chs-development).

    ./bin/chs-dev development enable efs-submission-web

This will clone the efs-submission-web into the repositories folder inside of docker-chs-dev folder. Any changes to the code or resources will automatically trigger a rebuild and reluanch.