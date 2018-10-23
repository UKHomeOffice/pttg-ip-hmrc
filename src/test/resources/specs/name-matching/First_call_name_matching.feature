JIRA EE-9764

  Feature: The first call in the name matching process

    Scenario: Applicant with 7 names an no Aliases
      Given HMRC has the following individual records
        | First name | Last name | Date of Birth | nino        |
        | Ggg        | Fff       | 1987-12-10    | SE 123456 B |
      When the applicant submits the following data to the RPS service
        | First name    | Aaa Bbb Ccc Ddd |
        | Last name     | Eee Fff Ggg     |
        | Date of Birth | 1987-12-10      |
        | nino          | SE 123456 B     |
      Then the footprint will try the following combination first
        | First name      | Last name   | Date of Birth | nino        |
        | Aaa Bbb Ccc Ddd | Eee Fff Ggg | 1987-12-10    | SE 123456 B |




    Scenario: Applicant with 5 original names and 3 aliases
      Given HMRC has the following individual records
        | First name | Last name | Date of Birth | nino        |
        | Aaa        | Hhh       | 1987-12-10    | SE 123456 B |
      When the applicant submits the following data to the RPS service
        | First name    | Aaa Bbb Ccc |
        | Last name     | Ddd Eee     |
        | Alias Surname | Fff Ggg Hhh |
        | Date of Birth | 1987-12-10  |
        | nino          | SE 123456 B |
      Then the footprint will try the following combination first
        | First name  | Last name | Date of Birth | nino        |
        | Aaa Bbb Ccc | Ddd Eee   | 1987-12-10    | SE 123456 B |