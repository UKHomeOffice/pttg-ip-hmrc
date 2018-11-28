@jira=EE-9244
# Note - the first call in the name matching process will use all the original names (EE-9764). The below sequence will follow from the first call

Feature: name sequence when making calls

  @aliases
  @name_matching
  Scenario: Applicant with 3 original names and one alias is matched with alias name in HMRC after trying 3 name combinations
    Given HMRC has the following individual records
      | First name | Last name | Date of Birth | nino      |
      | Aaa        | Ddd       | 1987-12-10    | SE123456B |
    When an income request is made with the following identity
      | First name    | Aaa Bbb     |
      | Last name     | Ccc         |
      | Alias Surname | Ddd         |
      | Date of Birth | 1987-12-10  |
      | nino          | SE123456B   |
    Then a Matched response will be returned from the service
    And HMRC was called 3 times

  @aliases
  @name_matching
  Scenario: Applicant with 4 original names and 2 aliases is matched with the alias name in HMRC after trying 10 name combinations
    Given HMRC has the following individual records
      | First name | Last name | Date of Birth | nino      |
      | Ccc        | Eee       | 1987-12-10    | SE123456B |
    When an income request is made with the following identity
      | First name    | Aaa Bbb Ccc |
      | Last name     | Ddd         |
      | Alias Surname | Eee Fff     |
      | Date of Birth | 1987-12-10  |
      | nino          | SE123456B   |
    Then a Matched response will be returned from the service
    And HMRC was called 10 times

  @aliases
  @name_matching
  Scenario: Applicant with 4 original names and 4 aliases is matched with the alias name in HMRC after trying 53 name combinations
    Given HMRC has the following individual records
      | First name | Last name | Date of Birth | nino      |
      | Hhh        | Ddd       | 1987-12-10    | SE123456B |
    When an income request is made with the following identity
      | First name    | Aaa Bbb Ccc     |
      | Last name     | Ddd             |
      | Alias Surname | Eee Fff Ggg Hhh |
      | Date of Birth | 1987-12-10      |
      | nino          | SE123456B       |
    Then a Matched response will be returned from the service
    And HMRC was called 53 times

  @aliases
  @name_matching
  Scenario: Applicant with 4 original names includes 2 surnames and 3 aliases is matched with the alias name in HMRC after trying 23 name combinations
    Given HMRC has the following individual records
      | First name | Last name | Date of Birth | nino      |
      | Eee        | Fff       | 1987-12-10    | SE123456B |
    When an income request is made with the following identity
      | First name    | Aaa Bbb Ccc |
      | Last name     | Ddd Eee     |
      | Alias Surname | Fff Ggg Hhh |
      | Date of Birth | 1987-12-10  |
      | nino          | SE123456B   |
    Then a Matched response will be returned from the service
    And HMRC was called 23 times

  @aliases
  @name_matching
  Scenario: Applicant with 6 original names includes 3 surnames and 4 aliases is matched with the alias name in HMRC after trying 38 name combinations
    Given HMRC has the following individual records
      | First name | Last name | Date of Birth | nino      |
      | Eee        | Ggg       | 1987-12-10    | SE123456B |
    When an income request is made with the following identity
      | First name    | Aaa Bbb Ccc     |
      | Last name     | Ddd Eee Fff     |
      | Alias Surname | Ggg Hhh Iii Jjj |
      | Date of Birth | 1987-12-10      |
      | nino          | SE123456B       |
    Then a Matched response will be returned from the service
    And HMRC was called 38 times

  @aliases
  @name_matching
  Scenario: Applicant with 5 original names and 3 aliases is matched in HMRC on the last combination
    Given HMRC has the following individual records
      | First name | Last name | Date of Birth | nino      |
      | Hhh        | Ggg       | 1987-12-10    | SE123456B |
    When an income request is made with the following identity
      | First name    | Aaa Bbb Ccc |
      | Last name     | Ddd Eee     |
      | Alias Surname | Fff Ggg Hhh |
      | Date of Birth | 1987-12-10  |
      | nino          | SE123456B   |
    Then the following identities will be tried in this order
      | First name  | Last name | Date of Birth | nino      |
      | Aaa Bbb Ccc | Ddd Eee   | 1987-12-10    | SE123456B |
      | Bbb         | Ddd Eee   | 1987-12-10    | SE123456B |
      | Ccc         | Ddd Eee   | 1987-12-10    | SE123456B |
      | Aaa         | Eee       | 1987-12-10    | SE123456B |
      | Bbb         | Eee       | 1987-12-10    | SE123456B |
      | Ccc         | Eee       | 1987-12-10    | SE123456B |
      | Ddd         | Eee       | 1987-12-10    | SE123456B |
      | Eee         | Ddd       | 1987-12-10    | SE123456B |
      | Aaa         | Hhh       | 1987-12-10    | SE123456B |
      | Bbb         | Hhh       | 1987-12-10    | SE123456B |
      | Ccc         | Hhh       | 1987-12-10    | SE123456B |
      | Ddd         | Hhh       | 1987-12-10    | SE123456B |
      | Eee         | Hhh       | 1987-12-10    | SE123456B |
      | Aaa         | Ggg       | 1987-12-10    | SE123456B |
      | Bbb         | Ggg       | 1987-12-10    | SE123456B |
      | Ccc         | Ggg       | 1987-12-10    | SE123456B |
      | Ddd         | Ggg       | 1987-12-10    | SE123456B |
      | Eee         | Ggg       | 1987-12-10    | SE123456B |
      | Aaa         | Fff       | 1987-12-10    | SE123456B |
      | Bbb         | Fff       | 1987-12-10    | SE123456B |
      | Ccc         | Fff       | 1987-12-10    | SE123456B |
      | Ddd         | Fff       | 1987-12-10    | SE123456B |
      | Eee         | Fff       | 1987-12-10    | SE123456B |
      | Aaa         | Bbb       | 1987-12-10    | SE123456B |
      | Aaa         | Ccc       | 1987-12-10    | SE123456B |
      | Bbb         | Aaa       | 1987-12-10    | SE123456B |
      | Bbb         | Ccc       | 1987-12-10    | SE123456B |
      | Ccc         | Aaa       | 1987-12-10    | SE123456B |
      | Ccc         | Bbb       | 1987-12-10    | SE123456B |
      | Ddd         | Aaa       | 1987-12-10    | SE123456B |
      | Ddd         | Bbb       | 1987-12-10    | SE123456B |
      | Ddd         | Ccc       | 1987-12-10    | SE123456B |
      | Eee         | Aaa       | 1987-12-10    | SE123456B |
      | Eee         | Bbb       | 1987-12-10    | SE123456B |
      | Eee         | Ccc       | 1987-12-10    | SE123456B |
      | Fff         | Aaa       | 1987-12-10    | SE123456B |
      | Fff         | Bbb       | 1987-12-10    | SE123456B |
      | Fff         | Ccc       | 1987-12-10    | SE123456B |
      | Fff         | Ddd       | 1987-12-10    | SE123456B |
      | Fff         | Eee       | 1987-12-10    | SE123456B |
      | Fff         | Ggg       | 1987-12-10    | SE123456B |
      | Fff         | Hhh       | 1987-12-10    | SE123456B |
      | Ggg         | Aaa       | 1987-12-10    | SE123456B |
      | Ggg         | Bbb       | 1987-12-10    | SE123456B |
      | Ggg         | Ccc       | 1987-12-10    | SE123456B |
      | Ggg         | Ddd       | 1987-12-10    | SE123456B |
      | Ggg         | Eee       | 1987-12-10    | SE123456B |
      | Ggg         | Fff       | 1987-12-10    | SE123456B |
      | Ggg         | Hhh       | 1987-12-10    | SE123456B |
      | Hhh         | Aaa       | 1987-12-10    | SE123456B |
      | Hhh         | Bbb       | 1987-12-10    | SE123456B |
      | Hhh         | Ccc       | 1987-12-10    | SE123456B |
      | Hhh         | Ddd       | 1987-12-10    | SE123456B |
      | Hhh         | Eee       | 1987-12-10    | SE123456B |
      | Hhh         | Fff       | 1987-12-10    | SE123456B |
      | Hhh         | Ggg       | 1987-12-10    | SE123456B |
    And a Matched response will be returned from the service