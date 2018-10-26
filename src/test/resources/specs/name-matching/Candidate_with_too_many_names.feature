Feature: Candidate names removed when too many supplied

  @name_matching
  Scenario: Only use the first 4 first names when maximum number of last names
    Given HMRC has the following individual records
      | First name | Last name | Date of Birth | nino      |
      | Ggg        | Fff       | 1987-12-10    | SE123456B |
    When an income request is made with the following identity
      | First name    | Aaa Bbb Ccc Ddd Xxx |
      | Last name     | Eee Fff Ggg         |
      | Date of Birth | 1987-12-10          |
      | nino          | SE 123456 B         |
    Then the following words will be used for the first part of the last name
      | Eee         |
      | Fff         |
      | Ggg         |
      | Aaa         |
      | Bbb         |
      | Ccc         |
      | Ddd         |

