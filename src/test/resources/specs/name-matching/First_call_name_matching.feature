@jira=EE-9764

Feature: The first call in the name matching process sends the full name entered by applicant

  @name_matching
  Scenario: Applicant with 7 names and no Aliases
    Given HMRC has the following individual records
      | First name | Last name | Date of Birth | nino        |
      | Ggg        | Fff       | 1987-12-10    | SE 123456 B |
    When an income request is made with the following identity
      | First name    | Aaa Bbb Ccc Ddd |
      | Last name     | Eee Fff Ggg     |
      | Date of Birth | 1987-12-10      |
      | nino          | SE 123456 B     |
    Then the following identity will be tried first
      | First name      | Last name   | Date of Birth | nino        |
      | Aaa Bbb Ccc Ddd | Eee Fff Ggg | 1987-12-10    | SE 123456 B |

  @name_matching
  @aliases
  Scenario: Applicant with single surname names and no Aliases
    Given HMRC has the following individual records
      | First name | Last name | Date of Birth | nino        |
      | Ddd        | Fff       | 1987-12-10    | SE 123456 B |
    When an income request is made with the following identity
      | First name    | Aaa Bbb Ccc Ddd |
      | Last name     | Eee             |
      | Date of Birth | 1987-12-10      |
      | nino          | SE 123456 B     |
    Then the following identity will be tried first
      | First name      | Last name | Date of Birth | nino        |
      | Aaa Bbb Ccc Ddd | Eee       | 1987-12-10    | SE 123456 B |

  @name_matching
  Scenario: Applicant with single mono name and no Aliases
    Given HMRC has the following individual records
      | First name | Last name | Date of Birth | nino        |
      | Aaa        | Aaa       | 1987-12-10    | SE 123456 B |
    When an income request is made with the following identity
      | First name    | Aaa         |
      | Last name     |             |
      | Date of Birth | 1987-12-10  |
      | nino          | SE 123456 B |
    Then the following identity will be tried first
      | First name | Last name | Date of Birth | nino        |
      | Aaa        | Aaa       | 1987-12-10    | SE 123456 B |

  @name_matching
  @aliases
  Scenario: Applicant with single mono name and an Alias
    Given HMRC has the following individual records
      | First name | Last name | Date of Birth | nino        |
      | Aaa        | Aaa       | 1987-12-10    | SE 123456 B |
    When an income request is made with the following identity
      | First name    | Aaa         |
      | Last name     |             |
      | Alias Surname | Bbb         |
      | Date of Birth | 1987-12-10  |
      | nino          | SE 123456 B |
    Then the following identity will be tried first
      | First name | Last name | Date of Birth | nino        |
      | Aaa        | Aaa       | 1987-12-10    | SE 123456 B |

  @name_matching
  @aliases
  Scenario: Applicant with 5 original names and 3 aliases and only using original names in the first name matching call
    Given HMRC has the following individual records
      | First name | Last name | Date of Birth | nino        |
      | Aaa        | Hhh       | 1987-12-10    | SE 123456 B |
    When an income request is made with the following identity
      | First name    | Aaa Bbb Ccc |
      | Last name     | Ddd Eee     |
      | Alias Surname | Fff Ggg Hhh |
      | Date of Birth | 1987-12-10  |
      | nino          | SE 123456 B |
    Then the following identity will be tried first
      | First name  | Last name | Date of Birth | nino        |
      | Aaa Bbb Ccc | Ddd Eee   | 1987-12-10    | SE 123456 B |