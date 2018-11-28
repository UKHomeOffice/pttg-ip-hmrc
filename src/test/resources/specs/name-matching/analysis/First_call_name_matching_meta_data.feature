@jira=EE-8289

Feature: Analysis logging

  Scenario: Meta-Data for Applicant with 7 names and no Aliases
    Given HMRC has the following individual records
      | First name      | Last name       | Date of Birth | nino        |
      | Aaa Bbb Ccc     | DDD Eee Fff Ggg | 1987-12-10    | SE123456B   |
    When an income request is made with the following identity
      | First name    | Aaa Bbb Ccc     |
      | Last name     | DDD Eee Fff Ggg |
      | Date of Birth | 1987-12-10      |
      | nino          | SE123456B       |
    Then meta-data was logged following a successful match
    And the meta-data contains the following input name information
      | nameType  | index | diacritics  | umlauts | abbreviation  | nameSplitter  | uniCodeBlocks | length  |
      | FIRST     | 0     | false       | false   | false         | false         | BASIC_LATIN   | 3       |
      | FIRST     | 1     | false       | false   | false         | false         | BASIC_LATIN   | 3       |
      | FIRST     | 2     | false       | false   | false         | false         | BASIC_LATIN   | 3       |
      | LAST      | 0     | false       | false   | false         | false         | BASIC_LATIN   | 3       |
      | LAST      | 1     | false       | false   | false         | false         | BASIC_LATIN   | 3       |
      | LAST      | 2     | false       | false   | false         | false         | BASIC_LATIN   | 3       |
      | LAST      | 3     | false       | false   | false         | false         | BASIC_LATIN   | 3       |
    And the meta-data indicates that the following generators were used
      | ENTIRE_NON_ALIAS_NAME    |
    And the meta-data contains the following name derivation information
      | section   | index   | length  | derivationActions |
      | FIRST     | 0 1 2   | 11      | ENTIRE            |
      | LAST      | 0 1 2 3 | 15      | ENTIRE            |


  Scenario: Meta-Data for Applicant with single surname names and no Aliases
    Given HMRC has the following individual records
      | First name      | Last name       | Date of Birth | nino        |
      | Ddd             | Fff             | 1987-12-10    | SE123456B   |
    When an income request is made with the following identity
      | First name    | AAA Ddd         |
      | Last name     | Fff             |
      | Date of Birth | 1987-12-10      |
      | nino          | SE123456B       |
    Then meta-data was logged following a successful match
    And the meta-data contains the following input name information
      | nameType  | index | diacritics  | umlauts | abbreviation  | nameSplitter  | uniCodeBlocks | length  |
      | FIRST     | 0     | false       | false   | false         | false         | BASIC_LATIN   | 3       |
      | FIRST     | 1     | false       | false   | false         | false         | BASIC_LATIN   | 3       |
      | LAST      | 0     | false       | false   | false         | false         | BASIC_LATIN   | 3       |
    And the meta-data indicates that the following generators were used
      | ENTIRE_LAST_NAME_AND_EACH_FIRST_NAME    |
    And the meta-data contains the following name derivation information
      | section   | index   | length  | derivationActions |
      | FIRST     | 1       | 3       | ORIGINAL          |
      | LAST      | 0       | 3       | ENTIRE            |


  Scenario: Meta-Data for Applicant with single first name, no last name and no Aliases
    Given HMRC has the following individual records
      | First name      | Last name       | Date of Birth | nino        |
      | Aaa             | Aaa             | 1987-12-10    | SE123456B   |
    When an income request is made with the following identity
      | First name    | Aaa         |
      | Last name     |             |
      | Date of Birth | 1987-12-10  |
      | nino          | SE123456B   |
    Then meta-data was logged following a successful match
    And the meta-data contains the following input name information
      | nameType  | index | diacritics  | umlauts | abbreviation  | nameSplitter  | uniCodeBlocks | length  |
      | FIRST     | 0     | false       | false   | false         | false         | BASIC_LATIN   | 3       |
    And the meta-data indicates that the following generators were used
      | ENTIRE_NON_ALIAS_NAME    |
    And the meta-data contains the following name derivation information
      | section   | index   | length  | derivationActions |
      | FIRST     | 0       | 3       | ENTIRE            |
      | FIRST     | 0       | 3       | ENTIRE            |


  Scenario: Meta-Data for Applicant with no first name, single last name and no Aliases
    Given HMRC has the following individual records
      | First name      | Last name       | Date of Birth | nino        |
      | Aaa             | Aaa             | 1987-12-10    | SE123456B   |
    When an income request is made with the following identity
      | First name    |             |
      | Last name     | Aaa         |
      | Date of Birth | 1987-12-10  |
      | nino          | SE123456B   |
    Then meta-data was logged following a successful match
    And the meta-data contains the following input name information
      | nameType  | index | diacritics  | umlauts | abbreviation  | nameSplitter  | uniCodeBlocks | length  |
      | LAST      | 0     | false       | false   | false         | false         | BASIC_LATIN   | 3       |
    And the meta-data indicates that the following generators were used
      | ENTIRE_NON_ALIAS_NAME    |
    And the meta-data contains the following name derivation information
      | section   | index   | length  | derivationActions |
      | LAST      | 0       | 3       | ENTIRE            |
      | LAST      | 0       | 3       | ENTIRE            |


  Scenario: Meta-Data for Applicant with single first name, no last name and single Aliases
    Given HMRC has the following individual records
      | First name      | Last name       | Date of Birth | nino        |
      | Aaa             | Aaa             | 1987-12-10    | SE123456B   |
    When an income request is made with the following identity
      | First name    | Aaa         |
      | Last name     |             |
      | Alias Surname | Bbb         |
      | Date of Birth | 1987-12-10  |
      | nino          | SE123456B   |
    Then meta-data was logged following a successful match
    And the meta-data contains the following input name information
      | nameType  | index | diacritics  | umlauts | abbreviation  | nameSplitter  | uniCodeBlocks | length  |
      | FIRST     | 0     | false       | false   | false         | false         | BASIC_LATIN   | 3       |
      | ALIAS     | 0     | false       | false   | false         | false         | BASIC_LATIN   | 3       |
    And the meta-data indicates that the following generators were used
      | ENTIRE_NON_ALIAS_NAME    |
    And the meta-data contains the following name derivation information
      | section   | index   | length  | derivationActions |
      | FIRST     | 0       | 3       | ENTIRE            |
      | FIRST     | 0       | 3       | ENTIRE            |


  Scenario: Meta-Data for Applicant with 5 original names and 3 aliases and only using original names
    Given HMRC has the following individual records
      | First name      | Last name       | Date of Birth | nino        |
      | Aaa             | Hhh             | 1987-12-10    | SE123456B   |
    When an income request is made with the following identity
      | First name    | Aaa Bbb Ccc |
      | Last name     | Ddd Eee     |
      | Alias Surname | Fff Ggg Hhh |
      | Date of Birth | 1987-12-10  |
      | nino          | SE123456B   |
    Then meta-data was logged following a successful match
    And the meta-data contains the following input name information
      | nameType  | index | diacritics  | umlauts | abbreviation  | nameSplitter  | uniCodeBlocks | length  |
      | FIRST     | 0     | false       | false   | false         | false         | BASIC_LATIN   | 3       |
      | FIRST     | 1     | false       | false   | false         | false         | BASIC_LATIN   | 3       |
      | FIRST     | 2     | false       | false   | false         | false         | BASIC_LATIN   | 3       |
      | LAST      | 0     | false       | false   | false         | false         | BASIC_LATIN   | 3       |
      | LAST      | 1     | false       | false   | false         | false         | BASIC_LATIN   | 3       |
      | ALIAS     | 0     | false       | false   | false         | false         | BASIC_LATIN   | 3       |
      | ALIAS     | 1     | false       | false   | false         | false         | BASIC_LATIN   | 3       |
      | ALIAS     | 2     | false       | false   | false         | false         | BASIC_LATIN   | 3       |
    And the meta-data indicates that the following generators were used
      | ALIAS_COMBINATIONS    |
    And the meta-data contains the following name derivation information
      | section   | index   | length  | derivationActions |
      | FIRST     | 0       | 3       | ORIGINAL          |
      | ALIAS     | 2       | 3       | ORIGINAL          |
