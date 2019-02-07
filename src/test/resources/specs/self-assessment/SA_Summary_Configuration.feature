@jira=EE-7304

Feature: Self Employment only configuration
  As a consumer of the HMRC service, I want to be able to retrieve SA self-employment data

# Currently RPS and IPS require SA self employment only

  Scenario: Self Assessment summary is returned by default
    Given HMRC has the following individual records
      | First name | Last name | Date of Birth | nino      |
      | Adam       | Ball      | 1987-12-10    | SE123456B |

    When an income request is made with the following identity
      | First name    | Adam        |
      | Last name     | Ball        |
      | Date of Birth | 1987-12-10  |
      | nino          | SE 123456 B |

    Then the self employment profit will be returned from the service
