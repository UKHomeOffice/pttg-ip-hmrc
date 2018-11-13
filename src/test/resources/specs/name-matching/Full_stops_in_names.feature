@Sprint=17.1
@Sprint=17.2
@epic=EE-3855
@story=Names_With_Full_Stops
@jira=EE-9349

Feature: Names with full stops

  @name_matching
  Scenario: Name with a full stop and a space which matched with HMRC details
    Given HMRC has the following individual records
      | First name | Last name | Date of Birth | nino      |
      | Aaa        | Bb. Ccc   | 1987-12-10    | SE123456B |
    When an income request is made with the following identity
      | First name    | Aaa        |
      | Last name     | Bb. Ccc    |
      | Date of Birth | 1987-12-10 |
      | nino          | SE123456B  |
    Then the following identities will be tried in this order
      | First name | Last name | Date of Birth | nino      |
      | Aaa        | Bb. Ccc   | 1987-12-10    | SE123456B |
    And a Matched response will be returned from the service

  @name_matching
  Scenario: Enters full stop when name does not contain a full stop and is match
    Given HMRC has the following individual records
      | First name | Last name | Date of Birth | nino      |
      | Aaa        | Bb Ccc    | 1987-12-10    | SE123456B |
    When an income request is made with the following identity
      | First name    | Aaa        |
      | Last name     | Bb. Ccc    |
      | Date of Birth | 1987-12-10 |
      | nino          | SE123456B  |
    Then the following identities will be tried in this order
      | First name | Last name | Date of Birth | nino      |
      | Aaa        | Bb. Ccc   | 1987-12-10    | SE123456B |
      | Bb. Ccc    | Aaa       | 1987-12-10    | SE123456B |
      | Aaa        | Ccc       | 1987-12-10    | SE123456B |
      | Bb.        | Ccc       | 1987-12-10    | SE123456B |
      | Ccc        | Aaa       | 1987-12-10    | SE123456B |
      | Ccc        | Bb.       | 1987-12-10    | SE123456B |
      | Aaa        | Bb Ccc    | 1987-12-10    | SE123456B |
    And a Matched response will be returned from the service

  @name_matching
  Scenario: Name with a full stop and no space which matched with HMRC details
    Given HMRC has the following individual records
      | First name | Last name | Date of Birth | nino      |
      | Aaa        | Bb.Ccc    | 1987-12-10    | SE123456B |
    When an income request is made with the following identity
      | First name    | Aaa        |
      | Last name     | Bb.Ccc     |
      | Date of Birth | 1987-12-10 |
      | nino          | SE123456B  |
    Then the following identities will be tried in this order
      | First name | Last name | Date of Birth | nino      |
      | Aaa        | Bb.Ccc    | 1987-12-10    | SE123456B |
    And a Matched response will be returned from the service

  @name_matching
  Scenario: Name when middle name has a full stop and a space and is matched with HMRC
    Given HMRC has the following individual records
      | First name | Last name | Date of Birth | nino      |
      | Ddd        | B. Ccc    | 1987-12-10    | SE123456B |
    When an income request is made with the following identity
      | First name    | Aaa B. Ccc  |
      | Last name     | Ddd         |
      | Date of Birth | 1987-12-10  |
      | nino          | SE123456B   |
    Then the following words will be used for the first part of the last name
      | B. Ccc        |
    And a Matched response will be returned from the service

  @name_matching
  @aliases
  Scenario: Alias name with a full stop and no space is matched with HMRC details with space and no full stop
    Given HMRC has the following individual records
      | First name | Last name | Date of Birth | nino      |
      | Aaa        | Ddd       | 1987-12-10    | SE123456B |
    When an income request is made with the following identity
      | First name    | Aaa        |
      | Last name     | Bbb        |
      | Date of Birth | 1987-12-10 |
      | nino          | SE123456B  |
      | Alias Surname | Cc.Ddd     |
    Then the following words will be used for the first part of the last name
      | Bbb    |
      | Aaa    |
      | Ddd    |
      | Cc.Ddd |
      | CcDdd  |
    And a Matched response will be returned from the service

  @name_matching
  @aliases
  Scenario: Alias name with a full stop and a space is matched with HMRC details with space and a full stop
    Given HMRC has the following individual records
      | First name | Last name | Date of Birth | nino      |
      | Cc. Ddd    | Aaa       | 1987-12-10    | SE123456B |
    When an income request is made with the following identity
      | First name    | Aaa        |
      | Last name     | Bbb        |
      | Date of Birth | 1987-12-10 |
      | nino          | SE123456B  |
      | Alias Surname | Cc. Ddd    |
    Then the following words will be used for the first name
      | Aaa     |
      | Bbb     |
      | Cc. Ddd |
    And a Matched response will be returned from the service
