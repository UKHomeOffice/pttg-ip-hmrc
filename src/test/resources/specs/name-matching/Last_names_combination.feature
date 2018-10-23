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
      | Brian       | Coates        |
      | Coates      | Arthur        |
      | Coates      | Brian         |
      | Brian       | Arthur        |


  @name_matching
  Scenario: Multi-part first name is tried without splitting
    Given HMRC has the following individual records
      | First name | Last name | Date of Birth | nino |
      | Any        | Any       | 0001-01-01    | Any  |
    When an income request is made with the following identity
      | First name    | Arthur-Brian  |
      | Last name     | Coates        |
      | Date of Birth | 9999-12-31    |
      | nino          | NR123456C     |
    Then the following names will be tried
      | First name   | Last name     |
      | Arthur-Brian | Coates        |


  @name_matching
  Scenario: Multi-part first name is tried with splitting
    Given HMRC has the following individual records
      | First name | Last name | Date of Birth | nino |
      | Any        | Any       | 0001-01-01    | Any  |
    When an income request is made with the following identity
      | First name    | Arthur-Brian  |
      | Last name     | Coates        |
      | Date of Birth | 9999-12-31    |
      | nino          | NR123456C     |
    Then the following names will be tried
      | First name  | Last name     |
      | Coates      | Arthur-Brian  |
      | Brian       | Coates        |
      | Coates      | Brian         |
      | Arthur      | Brian         |
      | Brian       | Arthur        |


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
      | O'Bobbins   | Arthur        |


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
      | O           | Bobbins     |
      | Bobbins     | Arthur      |
      | Bobbins     | O           |
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
      | O'Coates      | Arthur-Brian  |


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
      | ArthurBrian   | OCoates   |
      | Arthur        | Coates    |
      | Brian         | Coates    |
      | O             | Coates    |
      | Arthur        | Brian     |
      | Arthur        | O         |
      | Brian         | Arthur    |
      | Coates        | Arthur    |
      | Brian         | O         |
      | O             | Brian     |
      | Coates        | Brian     |
      | Coates        | O         |


  @name_matching
  Scenario: Ignore first names when too many names in total
    Given HMRC has the following individual records
      | First name | Last name | Date of Birth | nino |
      | Any        | Any       | 0001-01-01    | Any  |
    When an income request is made with the following identity
      | First name    | A B C D E F G  |
      | Last name     | Van Halen      |
      | Date of Birth | 9999-12-31     |
      | nino          | NR123456C      |
    Then the following names will not be tried
      | First name  | Last name |
      | F           | Van       |
      | G           | Van       |
      | F           | Halen     |
      | G           | Halen     |


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
