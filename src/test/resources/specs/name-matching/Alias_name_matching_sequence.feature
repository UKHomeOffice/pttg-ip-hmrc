JIRA - EE-9244
Note - the first call in the name matching process will use all the original names (EE-9764). The below sequence follows from the first call

Feature: name sequence when making calls

  @name_matching
  Scenario: Applicant with 3 original names and one alias is matched with alias name in HMRC after trying 4 name combinations
    Given HMRC has the following individual records
      | First name | Last name | Date of Birth | nino        |
      | Aaa        | Ddd       | 1987-12-10    | SE 123456 B |
    When the applicant submits the following data to the RPS service
      | First name    | Aaa Bbb     |
      | Last name     | Ccc         |
      | Alias Surname | Ddd         |
      | Date of Birth | 1987-12-10  |
      | nino          | SE 123456 B |
    Then a Matched response will be returned from the service
    And HMRC was called 4 times

######################################################

  Scenario: Applicant with 4 original names and 2 aliases is matched with the alias name in HMRC after trying 11 name combinations
    Given HMRC has the following individual records
      | First name | Last name | Date of Birth | nino        |
      | Ccc        | Eee       | 1987-12-10    | SE 123456 B |
    When the applicant submits the following data to the RPS service
      | First name    | Aaa Bbb Ccc |
      | Last name     | Ddd         |
      | Alias Surname | Eee Fff     |
      | Date of Birth | 1987-12-10  |
      | nino          | SE 123456 B |
    Then a Matched response will be returned from the service
    And HMRC was called 11 times


######################################
  Scenario: Applicant with 4 original names and 4 aliases is matched with the alias name in HMRC after trying 54 name combinations
    Given HMRC has the following individual records
      | First name | Last name | Date of Birth | nino        |
      | Hhh        | Ddd       | 1987-12-10    | SE 123456 B |
    When the applicant submits the following data to the RPS service
      | First name    | Aaa Bbb Ccc     |
      | Last name     | Ddd             |
      | Alias Surname | Eee Fff Ggg Hhh |
      | Date of Birth | 1987-12-10      |
      | nino          | SE 123456 B     |
    Then a Matched response will be returned from the service
    And HMRC was called 54 times


####################################################


  Scenario: Applicant with 4 original names includes 2 surnames and 3 aliases is matched with the alias name in HMRC after trying 24 name combinations
    Given HMRC has the following individual records
      | First name | Last name | Date of Birth | nino        |
      | Eee        | Fff       | 1987-12-10    | SE 123456 B |
    When the applicant submits the following data to the RPS service
      | First name    | Aaa Bbb Ccc |
      | Last name     | Ddd Eee     |
      | Alias Surname | Fff Ggg Hhh |
      | Date of Birth | 1987-12-10  |
      | nino          | SE 123456 B |
    Then a Matched response will be returned from the service
    And HMRC was called 24 times

    #########################

  Scenario: Applicant with 6 original names includes 3 surnames and 4 aliases is matched with the alias name in HMRC after trying 38 name combinations
    Given HMRC has the following individual records
      | First name | Last name | Date of Birth | nino        |
      | Eee        | Ggg       | 1987-12-10    | SE 123456 B |
    When the applicant submits the following data to the RPS service
      | First name    | Aaa Bbb Ccc     |
      | Last name     | Ddd Eee Fff     |
      | Alias Surname | Ggg Hhh Iii Jjj |
      | Date of Birth | 1987-12-10      |
      | nino          | SE 123456 B     |
    Then a Matched response will be returned from the service
    And HMRC was called 38 times



########################
  @name_matching
  Scenario: Applicant with 5 original names and 3 aliases is matched in HMRC on the last combination
    Given HMRC has the following individual records
      | First name | Last name | Date of Birth | nino        |
      | Hhh        | Ggg       | 1987-12-10    | SE 123456 B |
    When the applicant submits the following data to the RPS service
      | First name    | Aaa Bbb Ccc |
      | Last name     | Ddd Eee     |
      | Alias Surname | Fff Ggg Hhh |
      | Date of Birth | 1987-12-10  |
      | nino          | SE 123456 B |
    Then the footprint will try the following combination of names in order
      | First name  | Last name | Date of Birth | nino        |
      | Aaa Bbb Ccc | Ddd Eee   | 1987-12-10    | SE 123456 B |
      | Aaa         | Eee       | 1987-12-10    | SE 123456 B |
      | Bbb         | Eee       | 1987-12-10    | SE 123456 B |
      | Ccc         | Eee       | 1987-12-10    | SE 123456 B |
      | Ddd         | Eee       | 1987-12-10    | SE 123456 B |
      | Aaa         | Ddd       | 1987-12-10    | SE 123456 B |
      | Bbb         | Ddd       | 1987-12-10    | SE 123456 B |
      | Ccc         | Ddd       | 1987-12-10    | SE 123456 B |
      | Eee         | Ddd       | 1987-12-10    | SE 123456 B |
      | Aaa         | Hhh       | 1987-12-10    | SE 123456 B |
      | Bbb         | Hhh       | 1987-12-10    | SE 123456 B |
      | Ccc         | Hhh       | 1987-12-10    | SE 123456 B |
      | Ddd         | Hhh       | 1987-12-10    | SE 123456 B |
      | Eee         | Hhh       | 1987-12-10    | SE 123456 B |
      | Aaa         | Ggg       | 1987-12-10    | SE 123456 B |
      | Bbb         | Ggg       | 1987-12-10    | SE 123456 B |
      | Ccc         | Ggg       | 1987-12-10    | SE 123456 B |
      | Ddd         | Ggg       | 1987-12-10    | SE 123456 B |
      | Eee         | Ggg       | 1987-12-10    | SE 123456 B |
      | Aaa         | Fff       | 1987-12-10    | SE 123456 B |
      | Bbb         | Fff       | 1987-12-10    | SE 123456 B |
      | Ccc         | Fff       | 1987-12-10    | SE 123456 B |
      | Ddd         | Fff       | 1987-12-10    | SE 123456 B |
      | Eee         | Fff       | 1987-12-10    | SE 123456 B |
      | Aaa         | Bbb       | 1987-12-10    | SE 123456 B |
      | Aaa         | Ccc       | 1987-12-10    | SE 123456 B |
      | Bbb         | Aaa       | 1987-12-10    | SE 123456 B |
      | Bbb         | Ccc       | 1987-12-10    | SE 123456 B |
      | Ccc         | Aaa       | 1987-12-10    | SE 123456 B |
      | Ccc         | Bbb       | 1987-12-10    | SE 123456 B |
      | Ddd         | Aaa       | 1987-12-10    | SE 123456 B |
      | Ddd         | Bbb       | 1987-12-10    | SE 123456 B |
      | Ddd         | Ccc       | 1987-12-10    | SE 123456 B |
      | Eee         | Aaa       | 1987-12-10    | SE 123456 B |
      | Eee         | Bbb       | 1987-12-10    | SE 123456 B |
      | Eee         | Ccc       | 1987-12-10    | SE 123456 B |
      | Fff         | Aaa       | 1987-12-10    | SE 123456 B |
      | Fff         | Bbb       | 1987-12-10    | SE 123456 B |
      | Fff         | Ccc       | 1987-12-10    | SE 123456 B |
      | Fff         | Ddd       | 1987-12-10    | SE 123456 B |
      | Fff         | Eee       | 1987-12-10    | SE 123456 B |
      | Fff         | Ggg       | 1987-12-10    | SE 123456 B |
      | Fff         | Hhh       | 1987-12-10    | SE 123456 B |
      | Ggg         | Aaa       | 1987-12-10    | SE 123456 B |
      | Ggg         | Bbb       | 1987-12-10    | SE 123456 B |
      | Ggg         | Ccc       | 1987-12-10    | SE 123456 B |
      | Ggg         | Ddd       | 1987-12-10    | SE 123456 B |
      | Ggg         | Eee       | 1987-12-10    | SE 123456 B |
      | Ggg         | Fff       | 1987-12-10    | SE 123456 B |
      | Ggg         | Hhh       | 1987-12-10    | SE 123456 B |
      | Hhh         | Aaa       | 1987-12-10    | SE 123456 B |
      | Hhh         | Bbb       | 1987-12-10    | SE 123456 B |
      | Hhh         | Ccc       | 1987-12-10    | SE 123456 B |
      | Hhh         | Ddd       | 1987-12-10    | SE 123456 B |
      | Hhh         | Eee       | 1987-12-10    | SE 123456 B |
      | Hhh         | Fff       | 1987-12-10    | SE 123456 B |
      | Hhh         | Ggg       | 1987-12-10    | SE 123456 B |
    And a Matched response will be returned from the service


