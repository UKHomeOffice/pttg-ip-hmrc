# pttg-ip-hmrc
Interfaces to the HMRC API for Income Proving

Income Proving API
=

[![Build Status](https://drone.acp.homeoffice.gov.uk/api/badges/UKHomeOffice/pttg-ip-hmrc/status.svg)](https://drone.acp.homeoffice.gov.uk/UKHomeOffice/pttg-ip-hmrc)

[![Docker Repository on Quay](https://quay.io/repository/ukhomeofficedigital/pttg-ip-hmrc/status "Docker Repository on Quay")](https://quay.io/repository/ukhomeofficedigital/pttg-ip-hmrc)

Overview
-

This is the Income Proving HMRC API. Interfaces with the HMRC using [pttg-ip-hmrc-access-code] for authentication to retrieve previous incomes and employments. 

Currently the client of this service are [pttg-ip-api] [eue-api-rps-service].

###### NINO logging

N.B. Any log output that contains a NINO must sufficiently redact the data to hide the actual identity!
This is achieved through the use of a specific LogBack appender in logback-spring.xml.
 
## Find Us

* [GitHub]
* [Quay.io]

### Technical Notes

The API is implemented using Spring Boot and exposes a RESTFul interface.

* /income

### Infrastructure

This service is packaged as a Docker image and stored on [Quay.io]

This service currently runs in AWS and has an associated [kubernetes configuration]

## Building

This service is built using Gradle on Drone using [Drone yaml]

## Versioning

For the versions available, see the [tags on this repository].

## Authors

See the list of [contributors] who participated in this project.

## License

This project is licensed under the MIT License - see the [LICENCE.md]
file for details.



[contributors]:                     https://github.com/UKHomeOffice/pttg-ip-hmrc/graphs/contributors
[pttg-ip-hmrc-access-code]:         https://github.com/UKHomeOffice/pttg-ip-hmrc-access-code
[pttg-ip-api]:                      https://github.com/UKHomeOffice/pttg-ip-api
[Quay.io]:                          https://quay.io/repository/ukhomeofficedigital/pttg-ip-hmrc
[kubernetes configuration]:         https://github.com/UKHomeOffice/kube-pttg-ip-hmrc
[Drone yaml]:                       .drone.yml
[tags on this repository]:          https://github.com/UKHomeOffice/pttg-ip-hmrc/tags
[LICENCE.md]:                       LICENCE.md
[GitHub]:                           https://github.com/UKHomeOffice/pttg-ip-hmrc
