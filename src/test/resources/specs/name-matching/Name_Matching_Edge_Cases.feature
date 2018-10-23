@jira=EE-8838
Feature: Handling of name matching edge cases

  @name_matching
  Scenario: Applicant submits full stop for both names
    Given HMRC has the following individual records
      | First name | Last name | Date of Birth | nino      |
      | Estoban    | Higuain   | 1987-12-10    | SE123456B |
    When the applicant submits the following data to the RPS service
      | First name    | .           |
      | Last name     | .           |
      | Date of Birth | 1987-12-10  |
      | nino          | SE 123456 B |
    Then a not matched response is returned
    And HMRC was called 0 times

  @name_matching
  Scenario: Applicant submits full stop for first name
    Given HMRC has the following individual records
      | First name | Last name | Date of Birth | nino      |
      | Estoban    | Higuain   | 1987-12-10    | SE123456B |
    When the applicant submits the following data to the RPS service
      | First name    | Ali .       |
      | Last name     | Figuero     |
      | Date of Birth | 1987-12-10  |
      | nino          | SE 123456 B |
    Then a not matched response is returned
    And the footprint will try the following combination of names in order
      | First name | Last name | Date of Birth | nino        |
      | Ali        | Figuero   | 1987-12-10    | SE 123456 B |
      | Figuero    | Ali       | 1987-12-10    | SE 123456 B |

  @name_matching
  Scenario: Applicant submits full stop only for first name plus one last name
    Given HMRC has the following individual records
      | First name | Last name | Date of Birth | nino      |
      | Estoban    | Higuain   | 1987-12-10    | SE123456B |
    When the applicant submits the following data to the RPS service
      | First name    | .           |
      | Last name     | Figuero     |
      | Date of Birth | 1987-12-10  |
      | nino          | SE 123456 B |
    Then a not matched response is returned
    And HMRC was called 0 times

  @name_matching
  Scenario: Applicant submits full stop only for last name plus one first name
    Given HMRC has the following individual records
      | First name | Last name | Date of Birth | nino      |
      | Estoban    | Higuain   | 1987-12-10    | SE123456B |
    When the applicant submits the following data to the RPS service
      | First name    | Estoban     |
      | Last name     | .           |
      | Date of Birth | 1987-12-10  |
      | nino          | SE 123456 B |
    And HMRC was called 0 times

  @name_matching
  Scenario: Applicant submits full stop only for first name plus two last names
    Given HMRC has the following individual records
      | First name | Last name | Date of Birth | nino      |
      | Estoban    | Higuain   | 1987-12-10    | SE123456B |
    When the applicant submits the following data to the RPS service
      | First name    | .               |
      | Last name     | Figuero Higuain |
      | Date of Birth | 1987-12-10      |
      | nino          | SE 123456 B     |
    Then a not matched response is returned
    And the footprint will try the following combination of names in order
      | First name | Last name | Date of Birth | nino        |
      | Figuero    | Higuain   | 1987-12-10    | SE 123456 B |
      | Higuain    | Figuero   | 1987-12-10    | SE 123456 B |


  @name_matching
  Scenario: Applicant submits full stop only for last name plus two first names
    Given HMRC has the following individual records
      | First name | Last name | Date of Birth | nino      |
      | Estoban    | Higuain   | 1987-12-10    | SE123456B |
    When the applicant submits the following data to the RPS service
      | First name    | Estoban Figuero |
      | Last name     | .               |
      | Date of Birth | 1987-12-10      |
      | nino          | SE 123456 B     |
    Then a not matched response is returned
    And  the footprint will try the following combination of names in order
      | First name | Last name | Date of Birth | nino        |
      | Estoban    | Figuero   | 1987-12-10    | SE 123456 B |
      | Figuero    | Estoban   | 1987-12-10    | SE 123456 B |


