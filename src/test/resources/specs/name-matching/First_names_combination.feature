Feature: First name combinations

  @name_matching
  Scenario: Multi-part first name is tried with splitting
    Given HMRC has the following individual records
      | First name | Last name | Date of Birth | nino |
      | Any        | Any       | 0001-01-01    | Any  |
    When an income request is made with the following identity
      | First name    | Arthur Bob   |
      | Last name     | Cheese       |
      | Date of Birth | 9999-12-31   |
      | nino          | NR123456C    |
    Then the following names will be tried
      | First name | Last name |
      | Arthur Bob | Cheese    |
      | Bob        | Cheese    |


  @name_matching
  Scenario: Hyphenated first name is tried without splitting
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
  Scenario: Hyphenated first name is tried with splitting
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
      | Brian | Coates        |


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
