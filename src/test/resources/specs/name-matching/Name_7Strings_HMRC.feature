@Sprint=14.4
@Sprint=15.2
@story=HMRC_Name_Matching
@jira=EE-5266
@jira=EE-8337
@epic=EE-3855

Feature: Name matching with 7 name strings
  As a product owner, I want the system to accept 7 strings of names So that an applicant has a better possibility to be matched with the HMRC API

# Acceptance Criteria
#  • Must have a valid National Insurance number
#  • Must have a matching Date of Birth
#  • Must start name matching when first call to HMRC results in a not matched
#  • Must accept all names
#  • Letters with special characters must be replaced to the English equivalent
#    e.g.- Higuaín
#    The Latin letter "í" will be translated to "i" from the English alphabet
#  • Must accept all characters from the English alphabet and names can have any letters from the English alphabet
#  • Must try all possible combinations of the names unless the name is matched with HMRC during the process


  @name_matching
  Scenario: Applicant with 7 names is matched in HMRC on the last combination
    Given HMRC has the following individual records
      | First name | Last name | Date of Birth | nino      |
      | Ggg        | Fff       | 1987-12-10    | SE123456B |
    When an income request is made with the following identity
      | First name    | Aaa Bbb Ccc Ddd |
      | Last name     | Eee Fff Ggg     |
      | Date of Birth | 1987-12-10      |
      | nino          | SE 123456 B     |
    Then the following identities will be tried in this order
      | First name      | Last name   | Date of Birth | nino        |
      | Aaa Bbb Ccc Ddd | Eee Fff Ggg | 1987-12-10    | SE 123456 B |
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
      | Aaa             | Fff         | 1987-12-10    | SE 123456 B |
      | Bbb             | Aaa         | 1987-12-10    | SE 123456 B |
      | Bbb             | Ccc         | 1987-12-10    | SE 123456 B |
      | Bbb             | Ddd         | 1987-12-10    | SE 123456 B |
      | Bbb             | Fff         | 1987-12-10    | SE 123456 B |
      | Ccc             | Aaa         | 1987-12-10    | SE 123456 B |
      | Ccc             | Bbb         | 1987-12-10    | SE 123456 B |
      | Ccc             | Ddd         | 1987-12-10    | SE 123456 B |
      | Ccc             | Fff         | 1987-12-10    | SE 123456 B |
      | Ddd             | Aaa         | 1987-12-10    | SE 123456 B |
      | Ddd             | Bbb         | 1987-12-10    | SE 123456 B |
      | Ddd             | Ccc         | 1987-12-10    | SE 123456 B |
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


  @name_matching
  Scenario: Applicant with 7 names is matched in HMRC after trying 34 name combinations
    Given HMRC has the following individual records
      | First name | Last name | Date of Birth | nino      |
      | Fff        | Ccc       | 1987-12-10    | SE123456B |
    When an income request is made with the following identity
      | First name    | Aaa Bbb Ccc Ddd |
      | Last name     | Eee Fff Ggg     |
      | Date of Birth | 1987-12-10      |
      | nino          | SE 123456 B     |
    Then a Matched response will be returned from the service
    And HMRC was called 34 times


  @name_matching
  Scenario: Applicant with 7 names and is not found in HMRC after trying all possible combinations
    Given HMRC has the following individual records
      | First name | Last name | Date of Birth | nino      |
      | Iii        | Fff       | 1987-12-10    | SE123456B |
    When an income request is made with the following identity
      | First name    | Aaa Bbb Ccc Ddd |
      | Last name     | Eee Fff Ggg     |
      | Date of Birth | 1987-12-10      |
      | nino          | SE 123456 B     |
    Then a not matched response is returned
    And HMRC was called 42 times


  @name_matching
  Scenario: Applicant with 7 names is matched in HMRC after trying 10 name combinations
    Given HMRC has the following individual records
      | First name | Last name | Date of Birth | nino      |
      | Gonzalo    | Higuain   | 1987-12-10    | SE123456B |
    When an income request is made with the following identity
      | First name    | Ali Bob-Chicharito Danilo Estoban |
      | Last name     | Figuero Gonzalo Higuain           |
      | Date of Birth | 1987-12-10                        |
      | nino          | SE 123456 B                       |
    Then a Matched response will be returned from the service
    And HMRC was called 10 times


  @name_matching
  Scenario: Applicant with a hyphenated name is matched in HMRC on the concatenated version of the name
    Given HMRC has the following individual records
      | First name    | Last name | Date of Birth | nino      |
      | BobChicharito | Higuain   | 1987-12-10    | SE123456B |
    When an income request is made with the following identity
      | First name    | Ali Bob-Chicharito Danilo Estoban |
      | Last name     | Figuero Gonzalo Higuain           |
      | Date of Birth | 1987-12-10                        |
      | nino          | SE 123456 B                       |
    Then a Matched response will be returned from the service
    And HMRC was called 6 times

  @name_matching
  Scenario: Applicant with a hyphenated first name is matched in HMRC on the hyphenated version of the name
    Given HMRC has the following individual records
      | First name     | Last name | Date of Birth | nino      |
      | Bob-Chicharito | Higuain   | 1987-12-10    | SE123456B |
    When an income request is made with the following identity
      | First name    | Ali Bob-Chicharito Danilo Estoban |
      | Last name     | Figuero Gonzalo Higuain           |
      | Date of Birth | 1987-12-10                        |
      | nino          | SE 123456 B                       |
    Then a Matched response will be returned from the service
    And HMRC was called 6 times

  @name_matching
  Scenario: Applicant with a hyphenated last name is matched in HMRC on the hyphenated version of the name
    Given HMRC has the following individual records
      | First name | Last name | Date of Birth | nino      |
      | Bob        | El-Mohtar | 1987-12-10    | SE123456B |
    When an income request is made with the following identity
      | First name    | Bob         |
      | Last name     | El-Mohtar   |
      | Date of Birth | 1987-12-10  |
      | nino          | SE 123456 B |
    Then a Matched response will be returned from the service
    And HMRC was called 1 times


  @name_matching
  Scenario: Applicant with more than 7 names only uses the first 4 and the last 3 names for matching in HMRC
    Given HMRC has the following individual records
      | First name | Last name | Date of Birth | nino      |
      | Estoban    | Higuain   | 1987-12-10    | SE123456B |
    When an income request is made with the following identity
      | First name    | Ali Bob Chicharito Danilo Estoban |
      | Last name     | Figuero Gonzalo Higuain           |
      | Date of Birth | 1987-12-10                        |
      | nino          | SE 123456 B                       |
    Then a not matched response is returned
    And HMRC was called 43 times
