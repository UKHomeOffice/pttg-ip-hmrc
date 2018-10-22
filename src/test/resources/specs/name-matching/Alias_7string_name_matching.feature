JIRA-EE 9228

  Alias names will have a separate name pot therefore highlight what the accepted Alias name is


  Feature: Alias name matching when names consists 7 strings or more

    Scenario: Applicant with 7 names is matched in HMRC on the last combination
      Given HMRC has the following individual records
        | First name | Last name | Date of Birth | nino        |
        | Ggg        | Fff       | 1987-12-10    | SE 123456 B |
      When the applicant submits the following data to the RPS service
        | First name    | Aaa Bbb Ccc Ddd |
        | Last name     | Eee Fff Ggg     |
        | Date of Birth | 1987-12-10      |
        | nino          | SE 123456 B     |
      Then the footprint will try the following combination of names in order
        | First name      | Last name   | Date of Birth | nino        |
        | Aaa Bbb Ccc Ddd | Eee Fff Ggg | 1987-12-10    | SE 123456 B |
        | Aaa             | Eee Fff Ggg | 1987-12-10    | SE 123456 B |
        | Bbb             | Eee Fff Ggg | 1987-12-10    | SE 123456 B |
        | Ccc             | Eee Fff Ggg | 1987-12-10    | SE 123456 B |
        | Ddd             | Eee Fff Ggg | 1987-12-10    | SE 123456 B |
        | Aaa             | Ggg         | 1987-12-10    | SE 123456 B |
        | Bbb             | Ggg         | 1987-12-10    | SE 123456 B |
        | Ccc             | Ggg         | 1987-12-10    | SE 123456 B |
        | Ddd             | Ggg         | 1987-12-10    | SE 123456 B |
        | Eee             | Ggg         | 1987-12-10    | SE 123456 B |
        | Fff             | Ggg         | 1987-12-10    | SE 123456 B |
        | Aaa             | Bbb         | 1987-12-10    | SE 123456 B |
        | Aaa             | Ccc         | 1987-12-10    | SE 123456 B |
        | Aaa             | Ddd         | 1987-12-10    | SE 123456 B |
        | Aaa             | Eee         | 1987-12-10    | SE 123456 B |
        | Aaa             | Fff         | 1987-12-10    | SE 123456 B |
        | Bbb             | Aaa         | 1987-12-10    | SE 123456 B |
        | Bbb             | Ccc         | 1987-12-10    | SE 123456 B |
        | Bbb             | Ddd         | 1987-12-10    | SE 123456 B |
        | Bbb             | Eee         | 1987-12-10    | SE 123456 B |
        | Bbb             | Fff         | 1987-12-10    | SE 123456 B |
        | Ccc             | Aaa         | 1987-12-10    | SE 123456 B |
        | Ccc             | Bbb         | 1987-12-10    | SE 123456 B |
        | Ccc             | Ddd         | 1987-12-10    | SE 123456 B |
        | Ccc             | Eee         | 1987-12-10    | SE 123456 B |
        | Ccc             | Fff         | 1987-12-10    | SE 123456 B |
        | Ddd             | Aaa         | 1987-12-10    | SE 123456 B |
        | Ddd             | Bbb         | 1987-12-10    | SE 123456 B |
        | Ddd             | Ccc         | 1987-12-10    | SE 123456 B |
        | Ddd             | Eee         | 1987-12-10    | SE 123456 B |
        | Ddd             | Fff         | 1987-12-10    | SE 123456 B |
        | Eee             | Aaa         | 1987-12-10    | SE 123456 B |
        | Eee             | Bbb         | 1987-12-10    | SE 123456 B |
        | Eee             | Ccc         | 1987-12-10    | SE 123456 B |
        | Eee             | Ddd         | 1987-12-10    | SE 123456 B |
        | Eee             | Fff         | 1987-12-10    | SE 123456 B |
        | Fff             | Aaa         | 1987-12-10    | SE 123456 B |
        | Fff             | Bbb         | 1987-12-10    | SE 123456 B |
        | Fff             | Ccc         | 1987-12-10    | SE 123456 B |
        | Fff             | Ddd         | 1987-12-10    | SE 123456 B |
        | Fff             | Eee         | 1987-12-10    | SE 123456 B |
        | Ggg             | Aaa         | 1987-12-10    | SE 123456 B |
        | Ggg             | Bbb         | 1987-12-10    | SE 123456 B |
        | Ggg             | Ccc         | 1987-12-10    | SE 123456 B |
        | Ggg             | Ddd         | 1987-12-10    | SE 123456 B |
        | Ggg             | Eee         | 1987-12-10    | SE 123456 B |
        | Ggg             | Fff         | 1987-12-10    | SE 123456 B |
      And a Matched response will be returned from the service