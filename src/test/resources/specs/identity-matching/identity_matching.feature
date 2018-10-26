Feature: Identity Matching

  Scenario: applicant should be matched using NINO, DOB, firstname and lastname
    Given HMRC has the following individual records
      | First name    | Last name        | Date of Birth | nino      |
      | Freddy        | Flintstone       | 1952-02-28    | NR123456C |
    When an income request is made with the following identity
      | First name    | Freddy      |
      | Last name     | Flintstone  |
      | Date of Birth | 1952-02-28  |
      | nino          | NR123456C |
    Then a Matched response will be returned from the service

  Scenario: applicant should not be matched when using invalid NINO
    Given HMRC has the following individual records
      | First name    | Last name        | Date of Birth | nino      |
      | Freddy        | Flintstone       | 1952-02-28    | NR123456C |
    When an income request is made with the following identity
      | First name    | Freddy      |
      | Last name     | Flintstone  |
      | Date of Birth | 1952-02-28  |
      | nino          | SE123456B |
    Then a Matched response will not be returned from the service

  Scenario: applicant should not be matched when using invalid DOB
    Given HMRC has the following individual records
      | First name    | Last name        | Date of Birth | nino      |
      | Freddy        | Flintstone       | 1952-02-28    | NR123456C |
    When an income request is made with the following identity
      | First name    | Freddy      |
      | Last name     | Flintstone  |
      | Date of Birth | 1952-02-27  |
      | nino          | NR 123456 C |
    Then a Matched response will not be returned from the service

  Scenario: applicant should not be matched when using invalid firstname
    Given HMRC has the following individual records
      | First name    | Last name        | Date of Birth | nino      |
      | Freddy        | Flintstone       | 1952-02-28    | NR123456C |
    When an income request is made with the following identity
      | First name    | Wilma      |
      | Last name     | Flintstone  |
      | Date of Birth | 1952-02-28  |
      | nino          | NR 123456 C |
    Then a Matched response will not be returned from the service

  Scenario: applicant should not be matched when using invalid lastname
    Given HMRC has the following individual records
      | First name    | Last name        | Date of Birth | nino      |
      | Freddy        | Flintstone       | 1952-02-28    | NR123456C |
    When an income request is made with the following identity
      | First name    | Freddy      |
      | Last name     | Rubble  |
      | Date of Birth | 1952-02-28  |
      | nino          | NR 123456 C |
    Then a Matched response will not be returned from the service
