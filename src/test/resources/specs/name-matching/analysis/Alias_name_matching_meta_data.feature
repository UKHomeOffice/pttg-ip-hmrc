@jira=EE-8289

Feature: Analysis logging

  Scenario: Meta-Data for Applicant with 3 original names and one alias is matched with alias name
    Given HMRC has the following individual records
      | First name | Last name | Date of Birth | nino      |
      | Aaa        | Ddd       | 1987-12-10    | SE123456B |
    When an income request is made with the following identity
      | First name    | Aaa Bbb     |
      | Last name     | Ccc         |
      | Alias Surname | Ddd         |
      | Date of Birth | 1987-12-10  |
      | nino          | SE123456B   |
    Then meta-data was logged following a successful match
    And the meta-data contains the following input name information
      | nameType  | index | diacritics  | umlauts | abbreviation  | nameSplitter  | uniCodeBlocks | length  |
      | FIRST     | 0     | false       | false   | false         | false         | BASIC_LATIN   | 3       |
      | FIRST     | 1     | false       | false   | false         | false         | BASIC_LATIN   | 3       |
      | LAST      | 0     | false       | false   | false         | false         | BASIC_LATIN   | 3       |
      | ALIAS     | 0     | false       | false   | false         | false         | BASIC_LATIN   | 3       |
    And the meta-data indicates that the following generators were used
      | ALIAS_COMBINATIONS    |
    And the meta-data contains the following name derivation information
      | section   | index | length  | derivationActions |
      | FIRST     | 0     | 3       | ORIGINAL          |
      | ALIAS     | 0     | 3       | ORIGINAL          |


  Scenario: Meta-Data for Applicant with 4 original names and 2 aliases is matched with the alias name
    Given HMRC has the following individual records
      | First name | Last name | Date of Birth | nino      |
      | Ccc        | Eee       | 1987-12-10    | SE123456B |
    When an income request is made with the following identity
      | First name    | Aaa Bbb Ccc |
      | Last name     | Ddd         |
      | Alias Surname | Eee Fff     |
      | Date of Birth | 1987-12-10  |
      | nino          | SE123456B   |
    Then meta-data was logged following a successful match
    And the meta-data contains the following input name information
      | nameType  | index | diacritics  | umlauts | abbreviation  | nameSplitter  | uniCodeBlocks | length  |
      | FIRST     | 0     | false       | false   | false         | false         | BASIC_LATIN   | 3       |
      | FIRST     | 1     | false       | false   | false         | false         | BASIC_LATIN   | 3       |
      | FIRST     | 2     | false       | false   | false         | false         | BASIC_LATIN   | 3       |
      | LAST      | 0     | false       | false   | false         | false         | BASIC_LATIN   | 3       |
      | ALIAS     | 0     | false       | false   | false         | false         | BASIC_LATIN   | 3       |
      | ALIAS     | 1     | false       | false   | false         | false         | BASIC_LATIN   | 3       |
    And the meta-data indicates that the following generators were used
      | ALIAS_COMBINATIONS    |
    And the meta-data contains the following name derivation information
      | section   | index | length  | derivationActions |
      | FIRST     | 2     | 3       | ORIGINAL          |
      | ALIAS     | 0     | 3       | ORIGINAL          |


  Scenario: Meta-Data for Applicant with 4 original names and 4 aliases is matched with the alias name
    Given HMRC has the following individual records
      | First name | Last name | Date of Birth | nino      |
      | Hhh        | Ddd       | 1987-12-10    | SE123456B |
    When an income request is made with the following identity
      | First name    | Aaa Bbb Ccc     |
      | Last name     | Ddd             |
      | Alias Surname | Eee Fff Ggg Hhh |
      | Date of Birth | 1987-12-10      |
      | nino          | SE123456B       |
    Then meta-data was logged following a successful match
    And the meta-data contains the following input name information
      | nameType  | index | diacritics  | umlauts | abbreviation  | nameSplitter  | uniCodeBlocks | length  |
      | FIRST     | 0     | false       | false   | false         | false         | BASIC_LATIN   | 3       |
      | FIRST     | 1     | false       | false   | false         | false         | BASIC_LATIN   | 3       |
      | FIRST     | 2     | false       | false   | false         | false         | BASIC_LATIN   | 3       |
      | LAST      | 0     | false       | false   | false         | false         | BASIC_LATIN   | 3       |
      | ALIAS     | 0     | false       | false   | false         | false         | BASIC_LATIN   | 3       |
      | ALIAS     | 1     | false       | false   | false         | false         | BASIC_LATIN   | 3       |
      | ALIAS     | 2     | false       | false   | false         | false         | BASIC_LATIN   | 3       |
      | ALIAS     | 3     | false       | false   | false         | false         | BASIC_LATIN   | 3       |
    And the meta-data indicates that the following generators were used
      | ALIAS_COMBINATIONS    |
    And the meta-data contains the following name derivation information
      | section   | index | length  | derivationActions |
      | ALIAS     | 3     | 3       | ORIGINAL          |
      | LAST      | 0     | 3       | ORIGINAL          |


  Scenario: Meta-Data for Applicant with 4 original names includes 2 surnames and 3 aliases is matched with the alias
    Given HMRC has the following individual records
      | First name | Last name | Date of Birth | nino      |
      | Eee        | Fff       | 1987-12-10    | SE123456B |
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
      | section   | index | length  | derivationActions |
      | LAST      | 1     | 3       | ORIGINAL          |
      | ALIAS     | 0     | 3       | ORIGINAL          |


  Scenario: Meta-Data for Applicant with 6 original names includes 3 surnames and 4 aliases is matched with the alias
    Given HMRC has the following individual records
      | First name | Last name | Date of Birth | nino      |
      | Eee        | Ggg       | 1987-12-10    | SE123456B |
    When an income request is made with the following identity
      | First name    | Aaa Bbb Ccc     |
      | Last name     | Ddd Eee Fff     |
      | Alias Surname | Ggg Hhh Iii Jjj |
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
      | ALIAS     | 0     | false       | false   | false         | false         | BASIC_LATIN   | 3       |
      | ALIAS     | 1     | false       | false   | false         | false         | BASIC_LATIN   | 3       |
      | ALIAS     | 2     | false       | false   | false         | false         | BASIC_LATIN   | 3       |
      | ALIAS     | 3     | false       | false   | false         | false         | BASIC_LATIN   | 3       |
    And the meta-data indicates that the following generators were used
      | ALIAS_COMBINATIONS    |
    And the meta-data contains the following name derivation information
      | section   | index | length  | derivationActions |
      | LAST      | 1     | 3       | ORIGINAL          |
      | ALIAS     | 0     | 3       | ORIGINAL          |


  Scenario: Meta-Data for Applicant with 5 original names and 3 aliases is matched
    Given HMRC has the following individual records
      | First name | Last name | Date of Birth | nino      |
      | Hhh        | Ggg       | 1987-12-10    | SE123456B |
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
      | section   | index | length  | derivationActions |
      | ALIAS     | 2     | 3       | ORIGINAL          |
      | ALIAS     | 1     | 3       | ORIGINAL          |
