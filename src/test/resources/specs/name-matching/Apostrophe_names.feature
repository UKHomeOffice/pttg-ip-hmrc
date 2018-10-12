@Sprint=XX.X
@epic=EE-3855
@story=Updated_Rules_For_Apostrophe
@jira=EE-9575

Feature: Names with apostrophes and spaces


  Scenario: Name with a apostrophe and a space which matched with HMRC details
    Given HMRC has the following individual records
      | First name | Last name | Date of Birth | nino        |
      | Aaa        | Bb' Ccc   | 1987-12-10    | SE 123456 B |
    When the applicant submits the following data to the RPS service
      | First name    | Aaa         |
      | Last name     | Bb' Ccc     |
      | Date of Birth | 1987-12-10  |
      | nino          | SE 123456 B |
    Then the footprint will try the following combination of names in order
      | First name | Last name | Date of Birth | nino        |
      | Aaa        | Bb' Ccc   | 1987-12-10    | SE 123456 B |
    And a Matched response will be returned from the service


  Scenario: Enters apostrophe when name does not contain an apostrophe and is matched
    Given HMRC has the following individual records
      | First name | Last name | Date of Birth | nino        |
      | Aaa        | Bb Ccc    | 1987-12-10    | SE 123456 B |
    When the applicant submits the following data to the RPS service
      | First name    | Aaa         |
      | Last name     | Bb' Ccc     |
      | Date of Birth | 1987-12-10  |
      | nino          | SE 123456 B |
    Then the footprint will try the following combination of names in order
      | First name | Last name | Date of Birth | nino        |
      | Aaa        | Bb' Ccc   | 1987-12-10    | SE 123456 B |
      | Aaa        | Ccc       | 1987-12-10    | SE 123456 B |
      | Bb'        | Ccc       | 1987-12-10    | SE 123456 B |
      | Ccc        | Aaa       | 1987-12-10    | SE 123456 B |
      | Ccc        | Bb'       | 1987-12-10    | SE 123456 B |
      | Aaa        | Bb'       | 1987-12-10    | SE 123456 B |
      | Bb'        | Aaa       | 1987-12-10    | SE 123456 B |
      | Aaa        | Bb Ccc    | 1987-12-10    | SE 123456 B |
    And a Matched response will be returned from the service


  Scenario: Name with a apostrophe and no space which matched with HMRC details
    Given HMRC has the following individual records
      | First name | Last name | Date of Birth | nino        |
      | Aaa        | Bb'Ccc    | 1987-12-10    | SE 123456 B |
    When the applicant submits the following data to the RPS service
      | First name    | Aaa         |
      | Last name     | Bb'Ccc      |
      | Date of Birth | 1987-12-10  |
      | nino          | SE 123456 B |
    Then the footprint will try the following combination of names in order
      | First name | Last name | Date of Birth | nino        |
      | Aaa        | Bb'Ccc    | 1987-12-10    | SE 123456 B |
    And a Matched response will be returned from the service


  Scenario: Name when middle name has an apostrophe and a space which is matched with HMRC
    Given HMRC has the following individual records
      | First name | Last name | Date of Birth | nino        |
      | Bb' ccc    | Ddd       | 1987-12-10    | SE 123456 B |
    When the applicant submits the following data to the RPS service
      | First name    | Aaa Bb' Ccc |
      | Last name     | Ddd         |
      | Date of Birth | 1987-12-10  |
      | nino          | SE 123456 B |
    Then the footprint will try the following combination of names in order
      | First name | Last name | Date of Birth | nino        |
      | Aaa        | Ddd       | 1987-12-10    | SE 123456 B |
      | Bb'        | Ddd       | 1987-12-10    | SE 123456 B |
    And a Matched response will be returned from the service
