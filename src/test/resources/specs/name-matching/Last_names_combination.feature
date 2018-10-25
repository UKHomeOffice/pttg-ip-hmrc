Feature: Last name combinations

  @name_matching
  Scenario: Multi-part last name is tried without splitting
    Given HMRC has the following individual records
      | First name | Last name | Date of Birth | nino |
      | Any        | Any       | 0001-01-01    | Any  |
    When an income request is made with the following identity
      | First name    | Arthur       |
      | Last name     | Brian Coates |
      | Date of Birth | 9999-12-31   |
      | nino          | NR123456C    |
    Then the following names will be tried
      | First name  | Last name     |
      | Arthur      | Brian Coates  |


  @name_matching
  Scenario: Multi-part last name is tried with splitting
    Given HMRC has the following individual records
      | First name | Last name | Date of Birth | nino |
      | Any        | Any       | 0001-01-01    | Any  |
    When an income request is made with the following identity
      | First name    | Arthur       |
      | Last name     | Brian Coates |
      | Date of Birth | 9999-12-31   |
      | nino          | NR123456C    |
    Then the following names will be tried
      | First name  | Last name     |
      | Arthur      | Coates        |


  @name_matching
  Scenario: Hyphenated last name is tried without splitting
    Given HMRC has the following individual records
      | First name | Last name | Date of Birth | nino |
      | Any        | Any       | 0001-01-01    | Any  |
    When an income request is made with the following identity
      | First name    | Arthur        |
      | Last name     | Brown-Coates  |
      | Date of Birth | 9999-12-31    |
      | nino          | NR123456C     |
    Then the following names will be tried
      | First name   | Last name     |
      | Arthur       | Brown-Coates  |


  @name_matching
  Scenario: Hyphenated last name is tried with splitting
    Given HMRC has the following individual records
      | First name | Last name | Date of Birth | nino |
      | Any        | Any       | 0001-01-01    | Any  |
    When an income request is made with the following identity
      | First name    | Arthur        |
      | Last name     | Brown-Coates  |
      | Date of Birth | 9999-12-31    |
      | nino          | NR123456C     |
    Then the following names will be tried
      | First name  | Last name     |
      | Arthur      | Coates        |


  @name_matching
  Scenario: Apostrophed last name is tried with apostrophes
    Given HMRC has the following individual records
      | First name | Last name | Date of Birth | nino |
      | Any        | Any       | 0001-01-01    | Any  |
    When an income request is made with the following identity
      | First name    | Arthur     |
      | Last name     | O'Bobbins  |
      | Date of Birth | 9999-12-31 |
      | nino          | NR123456C  |
    Then the following names will be tried
      | First name  | Last name     |
      | Arthur      | O'Bobbins     |


  @name_matching
  Scenario: Apostrophed last name is tried without apostrophes
    Given HMRC has the following individual records
      | First name | Last name | Date of Birth | nino |
      | Any        | Any       | 0001-01-01    | Any  |
    When an income request is made with the following identity
      | First name    | Arthur     |
      | Last name     | O'Bobbins  |
      | Date of Birth | 9999-12-31 |
      | nino          | NR123456C  |
    Then the following names will be tried
      | First name  | Last name   |
      | Arthur      | O Bobbins   |
      | Arthur      | OBobbins    |
      | Arthur      | Bobbins     |
      | Arthur      | O           |


  @name_matching
  Scenario: Apostrophed and Hyphenated names are tried with apostrophes and hyphens
    Given HMRC has the following individual records
      | First name | Last name | Date of Birth | nino |
      | Any        | Any       | 0001-01-01    | Any  |
    When an income request is made with the following identity
      | First name    | Arthur-Brian  |
      | Last name     | O'Coates      |
      | Date of Birth | 9999-12-31    |
      | nino          | NR123456C     |
    Then the following names will be tried
      | First name    | Last name     |
      | Arthur-Brian  | O'Coates      |


  @name_matching
  Scenario: Apostrophed and Hyphenated names are tried without apostrophes and hyphens
    Given HMRC has the following individual records
      | First name | Last name | Date of Birth | nino |
      | Any        | Any       | 0001-01-01    | Any  |
    When an income request is made with the following identity
      | First name    | Arthur-Brian  |
      | Last name     | O'Coates      |
      | Date of Birth | 9999-12-31    |
      | nino          | NR123456C     |
    Then the following names will be tried
      | First name    | Last name |
      | Arthur Brian  | O Coates  |
      | Brian         | O Coates  |
      | Arthur        | Coates    |
      | Brian         | Coates    |
      | Arthur        | O         |
      | Brian         | O         |

  @name_matching
  Scenario: Handle joining in first and last name
    Given HMRC has the following individual records
      | First name | Last name | Date of Birth | nino |
      | Any        | Any       | 0001-01-01    | Any  |
    When an income request is made with the following identity
      | First name    | Bob-Brian           |
      | Last name     | Hill O'Coates-Smith |
      | Date of Birth | 9999-12-31    |
      | nino          | NR123456C     |
    Then the following names will be tried
      | First name | Last name            |
      | Bob-Brian  | Hill O'Coates-Smith  |
