@Sprint=17.2
@epic=EE-3855
@story=Updated_Rules_For_Apostrophe
@jira=EE-9575

Feature: Names with apostrophes and spaces


  Scenario: Name with a apostrophe and a space which matched with HMRC details
    Given HMRC has the following individual records
      | First name | Last name | Date of Birth | nino      |
      | Aaa        | Bb' Ccc   | 1987-12-10    | SE123456B |
    When an income request is made with the following identity
      | First name    | Aaa        |
      | Last name     | Bb' Ccc    |
      | Date of Birth | 1987-12-10 |
      | nino          | SE123456B  |
    Then the following identities will be tried in this order
      | First name | Last name | Date of Birth | nino      |
      | Aaa        | Bb' Ccc   | 1987-12-10    | SE123456B |
    And a Matched response will be returned from the service
    And HMRC was called 1 times


  Scenario: Enters apostrophe when name does not contain an apostrophe and is matched
    Given HMRC has the following individual records
      | First name | Last name | Date of Birth | nino      |
      | Aaa        | Bb Ccc    | 1987-12-10    | SE123456B |
    When an income request is made with the following identity
      | First name    | Aaa        |
      | Last name     | Bb' Ccc    |
      | Date of Birth | 1987-12-10 |
      | nino          | SE123456B  |
    Then the following identities will be tried in this order
      | First name | Last name | Date of Birth | nino      |
      | Aaa        | Bb' Ccc   | 1987-12-10    | SE123456B |
      | Aaa        | Ccc       | 1987-12-10    | SE123456B |
      | Bb'        | Ccc       | 1987-12-10    | SE123456B |
      | Ccc        | Aaa       | 1987-12-10    | SE123456B |
      | Ccc        | Bb'       | 1987-12-10    | SE123456B |
      | Bb'        | Aaa       | 1987-12-10    | SE123456B |
      | Aaa        | Bb Ccc    | 1987-12-10    | SE123456B |
    And a Matched response will be returned from the service
    And HMRC was called 7 times


  Scenario: Name with a apostrophe and no space which matched with HMRC details
    Given HMRC has the following individual records
      | First name | Last name | Date of Birth | nino      |
      | Aaa        | Bb'Ccc    | 1987-12-10    | SE123456B |
    When an income request is made with the following identity
      | First name    | Aaa        |
      | Last name     | Bb'Ccc     |
      | Date of Birth | 1987-12-10 |
      | nino          | SE123456B  |
    Then the following identities will be tried in this order
      | First name | Last name | Date of Birth | nino      |
      | Aaa        | Bb'Ccc    | 1987-12-10    | SE123456B |
    And a Matched response will be returned from the service
    And HMRC was called 1 times


  Scenario: Name when middle name has an apostrophe and a space which is matched with HMRC
    Given HMRC has the following individual records
      | First name | Last name | Date of Birth | nino      |
      | Bb' ccc    | Ddd       | 1987-12-10    | SE123456B |
    When an income request is made with the following identity
      | First name    | Aaa Bb' Ccc |
      | Last name     | Ddd         |
      | Date of Birth | 1987-12-10  |
      | nino          | SE123456B   |
    Then the following identities will be tried in this order
      | First name  | Last name | Date of Birth | nino      |
      | Aaa Bb' Ccc | Ddd       | 1987-12-10    | SE123456B |
      | Bb'         | Ddd       | 1987-12-10    | SE123456B |
    And a Matched response will be returned from the service
    And HMRC was called 2 times
