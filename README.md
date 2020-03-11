
## Overview
A basic Gradle based multi-modules Spring Boot project to showcase how use Pivotal Cloud Cache (PCC) with Spring GemFire Starter.

### PCC Workshop
The root project that includes the Gradle configuration for Spring Dependency management. The `setting.gradle` file can be used to upgrade/override the versions of the dependencies.

### Repository
A base module that includes Spring Data GemFire configurations for entities and repositories. 

### Client
An example GemFire client application which exposes RESTful endpoints and utilizes `repository` module for backend CRUD operations.

This application demonstrates two use cases:
* unit test and basic local functional test without the need to provision any GemFire/PCC instance (localhost or container).
* deploy to Pivotal Cloud Foundry and bind to PCC service instance without any code or configuration change. 

### Sizer
A highly extensible GemFire client application which can be used to load sample of data into GemFire instance locally. This application is useful to estimate PCC capacity without the need to provision PCC service instance first.

### CI
`ci` directory contains Dockerfile for GemFire that is version agnostic. Furthermore, a basic example of how to use Concourse pipeline to create GemFire Docker image from Pivotal Network is also provided. 
