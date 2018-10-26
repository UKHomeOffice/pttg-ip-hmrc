@jira=EE-7304

Feature: Self Assessment Summary or Self Employment only configuration
  As a consumer of the HMRC service, I want to be able to select either SA summary or SA self-employment
  based on a configuration property

# By default the service will be configured to return the full SA summary
# The configuration hmrc.sa.self-employment-only=true indicates that the service will return SA self employment only
# Currently RPS requires SA self employment only, but IPS requires the SA full summary


  Scenario: Self Assessment summary is returned by default
    Given HMRC has the following individual records
      | First name | Last name | Date of Birth | nino      |
      | Adam       | Ball      | 1987-12-10    | SE123456B |

    When an income request is made with the following identity
      | First name    | Adam        |
      | Last name     | Ball        |
      | Date of Birth | 1987-12-10  |
      | nino          | SE 123456 B |

    Then the summary income will be returned from the service

  Scenario: Self Assessment self employment is returned when the configuration property is true
    Given HMRC has the following individual records
      | First name | Last name | Date of Birth | nino      |
      | Adam       | Ball      | 1987-12-10    | SE123456B |
    And the service configuration is changed to self assessment summary

    When an income request is made with the following identity
      | First name    | Adam        |
      | Last name     | Ball        |
      | Date of Birth | 1987-12-10  |
      | nino          | SE 123456 B |

    Then the self employment profit will be returned from the service
    And the service configuration is changed to self assessment self employment only
