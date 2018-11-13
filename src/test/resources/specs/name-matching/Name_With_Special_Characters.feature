@Sprint=16.2
@epic=EE-3855
@story=Names_With_Special_Characters
@jira=EE-8204
@jira=EE-8337

Feature: Accept Hyphens and Apostrophes as part of the name matching


  @name_matching
  Scenario: Applicant with a hyphenated name
    Given HMRC has the following individual records
      | First name | Last name | Date of Birth | nino      |
      | Aaa        | Bb-Ccc    | 1987-12-10    | SE123456B |
    When an income request is made with the following identity
      | First name    | Aaa         |
      | Last name     | Bb-Ccc      |
      | Date of Birth | 1987-12-10  |
      | nino          | SE 123456 B |
    Then the following identities will be tried in this order
      | First name | Last name | Date of Birth | nino        |
      | Aaa        | Bb-Ccc    | 1987-12-10    | SE 123456 B |
    And a Matched response will be returned from the service
    And HMRC was called 1 times


  @name_matching
  Scenario: Applicant with a hyphenated name and surname
    Given HMRC has the following individual records
      | First name | Last name | Date of Birth | nino      |
      | Aa-Bbb     | Cc-Ddd    | 1987-12-10    | SE123456B |
    When an income request is made with the following identity
      | First name    | Cc-Ddd      |
      | Last name     | Aa-Bbb      |
      | Date of Birth | 1987-12-10  |
      | nino          | SE 123456 B |
    Then the following identities will be tried in this order
      | First name | Last name | Date of Birth | nino        |
      | Cc-Ddd     | Aa-Bbb    | 1987-12-10    | SE 123456 B |
      | Aa-Bbb     | Cc-Ddd    | 1987-12-10    | SE 123456 B |
    And a Matched response will be returned from the service
    And HMRC was called 2 times


  @name_matching
  Scenario: Applicant with a hyphenated surname is matched when more than the original names are sent via the API
    Given HMRC has the following individual records
      | First name | Last name | Date of Birth | nino      |
      | Aaa        | Bb-       | 1987-12-10    | SE123456B |
    When an income request is made with the following identity
      | First name    | Aaa         |
      | Last name     | Bb- Ccc     |
      | Date of Birth | 1987-12-10  |
      | nino          | SE 123456 B |
    Then the following identities will be tried in this order
      | First name | Last name | Date of Birth | nino        |
      | Aaa        | Bb- Ccc   | 1987-12-10    | SE 123456 B |
    And a Matched response will be returned from the service
    And HMRC was called 1 times


  @name_matching
  Scenario: Applicant enters hyphen when name does not contain a hyphen
    Given HMRC has the following individual records
      | First name | Last name | Date of Birth | nino      |
      | Aaa        | Bb Ccc    | 1987-12-10    | SE123456B |
    When an income request is made with the following identity
      | First name    | Aaa         |
      | Last name     | Bb-Ccc      |
      | Date of Birth | 1987-12-10  |
      | nino          | SE 123456 B |
    Then the following identities will be tried in this order
      | First name | Last name | Date of Birth | nino        |
      | Aaa        | Bb-Ccc    | 1987-12-10    | SE 123456 B |
      | Bb-Ccc     | Aaa       | 1987-12-10    | SE 123456 B |
      | Aaa        | BbCcc     | 1987-12-10    | SE 123456 B |
      | Aaa        | Bb Ccc    | 1987-12-10    | SE 123456 B |
    And a Matched response will be returned from the service


  @name_matching
  Scenario: Applicant enters hyphen when name does not contain a hyphen 2
    Given HMRC has the following individual records
      | First name | Last name | Date of Birth | nino      |
      | Bb         | Aaa       | 1987-12-10    | SE123456B |
    When an income request is made with the following identity
      | First name    | Aaa         |
      | Last name     | Bb-Ccc      |
      | Date of Birth | 1987-12-10  |
      | nino          | SE 123456 B |
    Then the following identities will be tried in this order
      | First name | Last name | Date of Birth | nino        |
      | Aaa        | Bb-Ccc    | 1987-12-10    | SE 123456 B |
      | Bb-Ccc     | Aaa       | 1987-12-10    | SE 123456 B |
    And a Matched response will be returned from the service
    And HMRC was called 2 times


  @name_matching
  Scenario: Applicant with a name containing an apostrophe
    Given HMRC has the following individual records
      | First name | Last name | Date of Birth | nino      |
      | Aaa        | Bb'Ccc    | 1987-12-10    | SE123456B |
    When an income request is made with the following identity
      | First name    | Aaa         |
      | Last name     | Bb'Ccc      |
      | Date of Birth | 1987-12-10  |
      | nino          | SE 123456 B |
    Then the following identities will be tried in this order
      | First name | Last name | Date of Birth | nino        |
      | Aaa        | Bb'Ccc    | 1987-12-10    | SE 123456 B |
    And a Matched response will be returned from the service
    And HMRC was called 1 times


  @name_matching
  Scenario: Applicant enters apostrophe when name does not contain an apostrophe
    Given HMRC has the following individual records
      | First name | Last name | Date of Birth | nino      |
      | Aaa        | Bb Ccc    | 1987-12-10    | SE123456B |
    When an income request is made with the following identity
      | First name    | Aaa         |
      | Last name     | Bb'Ccc      |
      | Date of Birth | 1987-12-10  |
      | nino          | SE 123456 B |
    Then the following identities will be tried in this order
      | First name | Last name | Date of Birth | nino        |
      | Aaa        | Bb'Ccc    | 1987-12-10    | SE 123456 B |
      | Bb'Ccc     | Aaa       | 1987-12-10    | SE 123456 B |
      | Aaa        | BbCcc     | 1987-12-10    | SE 123456 B |
      | Aaa        | Bb Ccc    | 1987-12-10    | SE 123456 B |
    And a Matched response will be returned from the service


  @name_matching
  Scenario: Applicant with multiple surnames containing one letter in the surname
    Given HMRC has the following individual records
      | First name   | Last name   | Date of Birth | nino      |
      | Joseph James | R De Bloggs | 1987-12-10    | SE123456B |
    When an income request is made with the following identity
      | First name    | Joseph James |
      | Last name     | R De Bloggs  |
      | Date of Birth | 1987-12-10   |
      | nino          | SE 123456 B  |
    Then the following identities will be tried in this order
      | First name   | Last name  | Date of Birth | nino        |
      | Joseph James | R De Bloggs | 1987-12-10    | SE 123456 B |
    And a Matched response will be returned from the service
    And HMRC was called 1 times


  @name_matching
  Scenario: Applicant with multiple surnames containing two letters in the surname
    Given HMRC has the following individual records
      | First name | Last name | Date of Birth | nino      |
      | Pas Alb    | De Fey    | 1987-12-10    | SE123456B |
    When an income request is made with the following identity
      | First name    | Pas Alb     |
      | Last name     | R De Fey    |
      | Date of Birth | 1987-12-10  |
      | nino          | SE 123456 B |
    Then the following identities will be tried in this order
      | First name | Last name | Date of Birth | nino        |
      | Pas Alb    | R De Fey  | 1987-12-10    | SE 123456 B |
      | Alb        | R De Fey  | 1987-12-10    | SE 123456 B |
      | Pas        | R Fey     | 1987-12-10    | SE 123456 B |
      | Pas        | De R      | 1987-12-10    | SE 123456 B |
      | Pas        | De Fey    | 1987-12-10    | SE 123456 B |
    And a Matched response will be returned from the service
    And HMRC was called 5 times


  @name_matching
  Scenario: Applicant is not matched with multiple surnames containing one letter name
    Given HMRC has the following individual records
      | First name | Last name | Date of Birth | nino      |
      | Pas Alb    | De Fey    | 1987-12-10    | SE123456B |
    When an income request is made with the following identity
      | First name    | R              |
      | Last name     | Pas Alb De Fey |
      | Date of Birth | 1987-12-10     |
      | nino          | SE 123456 B    |
    Then the following identities will be tried in this order
      | First name | Last name      | Date of Birth | nino        |
      | R          | Pas Alb De Fey | 1987-12-10    | SE 123456 B |
      | R          | De Pas         | 1987-12-10    | SE 123456 B |
      | R          | De Alb         | 1987-12-10    | SE 123456 B |
      | R          | De Fey         | 1987-12-10    | SE 123456 B |
      | R          | Fey            | 1987-12-10    | SE 123456 B |
      | Pas        | Fey            | 1987-12-10    | SE 123456 B |
      | Alb        | Fey            | 1987-12-10    | SE 123456 B |
      | De         | Fey            | 1987-12-10    | SE 123456 B |
      | R          | Alb            | 1987-12-10    | SE 123456 B |
      | R          | De             | 1987-12-10    | SE 123456 B |
      | Pas        | R              | 1987-12-10    | SE 123456 B |
      | Pas        | Alb            | 1987-12-10    | SE 123456 B |
      | Pas        | De             | 1987-12-10    | SE 123456 B |
      | Alb        | R              | 1987-12-10    | SE 123456 B |
      | Alb        | Pas            | 1987-12-10    | SE 123456 B |
      | Alb        | De             | 1987-12-10    | SE 123456 B |
      | De         | R              | 1987-12-10    | SE 123456 B |
      | De         | Pas            | 1987-12-10    | SE 123456 B |
      | De         | Alb            | 1987-12-10    | SE 123456 B |
      | Fey        | R              | 1987-12-10    | SE 123456 B |
      | Fey        | Pas            | 1987-12-10    | SE 123456 B |
      | Fey        | Alb            | 1987-12-10    | SE 123456 B |
      | Fey        | De             | 1987-12-10    | SE 123456 B |
    Then a not matched response is returned
    And HMRC was called 23 times