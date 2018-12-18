@jira=EE-8838
Feature: Handling of name matching edge cases

  @name_matching
  Scenario: Applicant submits exclamation mark for both names
    Given HMRC has the following individual records
      | First name | Last name | Date of Birth | nino      |
      | Estoban    | Higuain   | 1987-12-10    | SE123456B |
    When an income request is made with the following identity
      | First name    | !           |
      | Last name     | !           |
      | Date of Birth | 1987-12-10  |
      | nino          | SE 123456 B |
    Then a not matched response is returned
    And HMRC was called 0 times

  @name_matching
  Scenario: Applicant submits exclamation mark for first name
    Given HMRC has the following individual records
      | First name | Last name | Date of Birth | nino      |
      | Estoban    | Higuain   | 1987-12-10    | SE123456B |
    When an income request is made with the following identity
      | First name    | Ali !       |
      | Last name     | Figuero     |
      | Date of Birth | 1987-12-10  |
      | nino          | SE 123456 B |
    Then a not matched response is returned
    And the following identities will be tried in this order
      | First name | Last name | Date of Birth | nino        |
      | Ali        | Figuero   | 1987-12-10    | SE 123456 B |
      | Figuero    | Ali       | 1987-12-10    | SE 123456 B |

  @name_matching
  Scenario: Applicant submits exclamation mark only for first name plus one last name
    Given HMRC has the following individual records
      | First name | Last name | Date of Birth | nino      |
      | Estoban    | Higuain   | 1987-12-10    | SE123456B |
    When an income request is made with the following identity
      | First name    | !           |
      | Last name     | Figuero     |
      | Date of Birth | 1987-12-10  |
      | nino          | SE 123456 B |
    Then a not matched response is returned
    And HMRC was called 0 times

  @name_matching
  Scenario: Applicant submits exclamation mark only for last name plus one first name
    Given HMRC has the following individual records
      | First name | Last name | Date of Birth | nino      |
      | Estoban    | Higuain   | 1987-12-10    | SE123456B |
    When an income request is made with the following identity
      | First name    | Estoban     |
      | Last name     | !           |
      | Date of Birth | 1987-12-10  |
      | nino          | SE 123456 B |
    And HMRC was called 0 times

  @name_matching
  Scenario: Applicant submits exclamation mark only for first name plus two last names
    Given HMRC has the following individual records
      | First name | Last name | Date of Birth | nino      |
      | Estoban    | Higuain   | 1987-12-10    | SE123456B |
    When an income request is made with the following identity
      | First name    | !               |
      | Last name     | Figuero Higuain |
      | Date of Birth | 1987-12-10      |
      | nino          | SE 123456 B     |
    Then a not matched response is returned
    And the following identities will be tried in this order
      | First name | Last name | Date of Birth | nino        |
      | Figuero    | Higuain   | 1987-12-10    | SE 123456 B |
      | Higuain    | Figuero   | 1987-12-10    | SE 123456 B |


  @name_matching
  Scenario: Applicant submits exclamation mark only for last name plus two first names
    Given HMRC has the following individual records
      | First name | Last name | Date of Birth | nino      |
      | Estoban    | Higuain   | 1987-12-10    | SE123456B |
    When an income request is made with the following identity
      | First name    | Estoban Figuero |
      | Last name     | !               |
      | Date of Birth | 1987-12-10      |
      | nino          | SE 123456 B     |
    Then a not matched response is returned
    And  the following identities will be tried in this order
      | First name | Last name | Date of Birth | nino        |
      | Estoban    | Figuero   | 1987-12-10    | SE 123456 B |
      | Figuero    | Estoban   | 1987-12-10    | SE 123456 B |

  @name_matching
  Scenario: Applicant submits exclamation mark as some of their last names - last name in matching attempt should not have leading or trailing whitespace
    Given HMRC has the following individual records
      | First name | Last name | Date of Birth | nino      |
      | Estoban    | Higuain   | 1987-12-10    | SE123456B |
    When an income request is made with the following identity
      | First name    | Ali         |
      | Last name     | ! Figuero ! |
      | Date of Birth | 1987-12-10  |
      | nino          | SE 123456 B |
    Then none of the name matching calls contain leading or trailing whitespace

  @name_matching
  Scenario: Applicant submits full stop for both names - full stop now not removed from names for matching
    Given HMRC has the following individual records
      | First name | Last name | Date of Birth | nino      |
      | Estoban    | Higuain   | 1987-12-10    | SE123456B |
    When an income request is made with the following identity
      | First name    | .           |
      | Last name     | .           |
      | Date of Birth | 1987-12-10  |
      | nino          | SE 123456 B |
    Then a not matched response is returned
    And HMRC was called 1 times
