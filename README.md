HMRC Service
=

[![Docker Repository on Quay](https://quay.io/repository/ukhomeofficedigital/pttg-ip-hmrc/status "Docker Repository on Quay")](https://quay.io/repository/ukhomeofficedigital/pttg-ip-hmrc)

## Overview

This is the HMRC Service. The service talks with HMRC to retrieve previous incomes and employments.

Authentication to HMRC is provided by [pttg-ip-hmrc-access-code].  

Currently the clients of this service are [pttg-ip-api] and [eue-api-rps-service].

 
## Find Us

* [GitHub]

## Technical Notes

The API is implemented using Spring Boot and exposes a RESTFul interface.

The endpoint is defined in `HmrcResource.java#getHmrcData`.

### NINO logging

Any log output that contains a NINO must sufficiently redact the data to hide the actual identity.
This is achieved through the use of a specific LogBack appender in `logback-spring.xml`.

## Building

### ACP

This service is built by Gradle on [Drone] using [Drone yaml].

### EBSA

This service is built by Gradle on [Jenkins] using the [build Jenkinsfile].

## Infrastructure

### ACP

This service is packaged as a Docker image and stored on [Quay.io]

This service is deployed by [Drone] onto a Kubernetes cluster using its [Kubernetes configuration]

### EBSA

This service is packaged as a Docker image and stored on AWS ECR.

This service is deployed by [Jenkins] onto a Kubernetes cluster using the [deploy Jenkinsfile].

## Running Locally

Check out the project and run the command `./gradlew bootRun` which will install gradle locally, download all dependencies, build the project and run it.

The API should then be available on http://localhost:8100/income, where:
- port 8100 is defined in `application.properties` with key `server.port`
- path `/income` is defined in `HmrcResource.java#getHmrcData`
- the expected request body contains a JSON representation of `IncomeDataRequest.java`

Note that this API needs collaborating services [pttg-ip-audit] and [pttg-ip-hmrc-access-code]. Connection details for these services can be found in `application.properties` with keys `base.hmrc.access.*` and `pttg.audit.*`, which should include the default ports of the services. 

### Stub / Sandbox

HMRC provide a Sandbox into which we can seed data. Tools for seeding the HMRC sandbox can be found in [HMRC Sandbox].

There is also a stub providing canned data from HMRC. The stub project can be found at [HMRC stub]. 

## Dependencies

This service depends upon:

* [pttg-ip-hmrc-access-code]
* [pttg-ip-audit]

## Versioning

For the versions available, see the [tags on this repository].

## Authors

See the list of [contributors] who participated in this project.

## License

This project is licensed under the MIT License - see the [LICENCE.md]
file for details.



[contributors]:                     https://github.com/UKHomeOffice/pttg-ip-hmrc/graphs/contributors
[pttg-ip-hmrc-access-code]:         https://github.com/UKHomeOffice/pttg-ip-hmrc-access-code
[pttg-ip-audit]:                    https://github.com/UKHomeOffice/pttg-ip-audit
[pttg-ip-api]:                      https://github.com/UKHomeOffice/pttg-ip-api
[eue-api-rps-service]:              https://bitbucket.ipttools.info/projects/EUE-API/repos/eue-api-rps-service/
[Quay.io]:                          https://quay.io/repository/ukhomeofficedigital/pttg-ip-hmrc
[kubernetes configuration]:         https://github.com/UKHomeOffice/kube-pttg-ip-hmrc
[Drone yaml]:                       .drone.yml
[tags on this repository]:          https://github.com/UKHomeOffice/pttg-ip-hmrc/tags
[LICENCE.md]:                       LICENCE.md
[GitHub]:                           https://github.com/orgs/UKHomeOffice/teams/pttg
[Drone]:                            https://drone.acp.homeoffice.gov.uk/UKHomeOffice/pttg-ip-api
[Jenkins]:                          https://eue-pttg-jenkins-dtzo-kops1.service.ops.iptho.co.uk/job/build_eue_api_hmrc_service/             
[build Jenkinsfile]:                https://bitbucket.ipttools.info/projects/EUE-API/repos/eue-api-shared-services-toolset/browse/Jenkinsfile.pttg_ip_hmrc
[deploy Jenkinsfile]:               https://eue-pttg-jenkins-dtzo-kops1.service.ops.iptho.co.uk/job/deploy_np_dev_push_eue_api_project_tiller/
[HMRC Sandbox]:                     https://bitbucket.ipttools.info/projects/EUE-API/repos/eue-api-hmrc-sandbox/
[HMRC Stub]:                        https://bitbucket.ipttools.info/projects/EUE-API/repos/eue-api-hmrc-stub/
