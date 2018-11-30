@jira=EE-8289

Feature: Analysis logging

  Scenario: Meta-Data for identity with 7 names 1
    Given HMRC has the following individual records
      | First name      | Last name       | Date of Birth | nino      |
      | Ggg             | Fff             | 1987-12-10    | SE123456B |
    When an income request is made with the following identity
      | First name    | Aaa Bbb Ccc Ddd |
      | Last name     | Eee Fff Ggg     |
      | Date of Birth | 1987-12-10      |
      | nino          | SE 123456 B     |
    Then meta-data was logged following a successful match
    And the meta-data contains the following input name information
      | nameType  | index | diacritics  | umlauts | abbreviation  | nameSplitter  | uniCodeBlocks | length  |
      | FIRST     | 0     | false       | false   | false         | false         | BASIC_LATIN   | 3       |
      | FIRST     | 1     | false       | false   | false         | false         | BASIC_LATIN   | 3       |
      | FIRST     | 2     | false       | false   | false         | false         | BASIC_LATIN   | 3       |
      | FIRST     | 3     | false       | false   | false         | false         | BASIC_LATIN   | 3       |
      | LAST      | 0     | false       | false   | false         | false         | BASIC_LATIN   | 3       |
      | LAST      | 1     | false       | false   | false         | false         | BASIC_LATIN   | 3       |
      | LAST      | 2     | false       | false   | false         | false         | BASIC_LATIN   | 3       |
    And the meta-data indicates that the following generators were used
      | NAME_COMBINATIONS    |
    And the meta-data contains the following name derivation information
      | section   | index   | length  | derivationActions |
      | LAST      | 2       | 3       | ORIGINAL          |
      | LAST      | 1       | 3       | ORIGINAL          |


  Scenario: Meta-Data for identity with 7 names 2
    Given HMRC has the following individual records
      | First name      | Last name       | Date of Birth | nino      |
      | Fff             | Ccc             | 1987-12-10    | SE123456B |
    When an income request is made with the following identity
      | First name    | Aaa Bbb Ccc Ddd |
      | Last name     | Eee Fff Ggg     |
      | Date of Birth | 1987-12-10      |
      | nino          | SE 123456 B     |
    Then meta-data was logged following a successful match
    And the meta-data contains the following input name information
      | nameType  | index | diacritics  | umlauts | abbreviation  | nameSplitter  | uniCodeBlocks | length  |
      | FIRST     | 0     | false       | false   | false         | false         | BASIC_LATIN   | 3       |
      | FIRST     | 1     | false       | false   | false         | false         | BASIC_LATIN   | 3       |
      | FIRST     | 2     | false       | false   | false         | false         | BASIC_LATIN   | 3       |
      | FIRST     | 3     | false       | false   | false         | false         | BASIC_LATIN   | 3       |
      | LAST      | 0     | false       | false   | false         | false         | BASIC_LATIN   | 3       |
      | LAST      | 1     | false       | false   | false         | false         | BASIC_LATIN   | 3       |
      | LAST      | 2     | false       | false   | false         | false         | BASIC_LATIN   | 3       |
    And the meta-data indicates that the following generators were used
      | NAME_COMBINATIONS    |
    And the meta-data contains the following name derivation information
      | section   | index   | length  | derivationActions |
      | LAST      | 1       | 3       | ORIGINAL          |
      | FIRST     | 2       | 3       | ORIGINAL          |


  Scenario: Meta-Data for identity with 7 names 3
    Given HMRC has the following individual records
      | First name      | Last name       | Date of Birth | nino      |
      | Gonzalo         | Higuain         | 1987-12-10    | SE123456B |
    When an income request is made with the following identity
      | First name    | Ali Bob-Chicharito Danilo Estoban |
      | Last name     | Figuero Gonzalo Higuain           |
      | Date of Birth | 1987-12-10                        |
      | nino          | SE 123456 B                       |
    Then meta-data was logged following a successful match
    And the meta-data contains the following input name information
      | nameType  | index | diacritics  | umlauts | abbreviation  | nameSplitter  | uniCodeBlocks | length  |
      | FIRST     | 0     | false       | false   | false         | false         | BASIC_LATIN   | 3       |
      | FIRST     | 1     | false       | false   | false         | true          | BASIC_LATIN   | 14      |
      | FIRST     | 2     | false       | false   | false         | false         | BASIC_LATIN   | 6       |
      | FIRST     | 3     | false       | false   | false         | false         | BASIC_LATIN   | 7       |
      | LAST      | 0     | false       | false   | false         | false         | BASIC_LATIN   | 7       |
      | LAST      | 1     | false       | false   | false         | false         | BASIC_LATIN   | 7       |
      | LAST      | 2     | false       | false   | false         | false         | BASIC_LATIN   | 7       |
    And the meta-data indicates that the following generators were used
      | NAME_COMBINATIONS    |
    And the meta-data contains the following name derivation information
      | section   | index   | length  | derivationActions |
      | LAST      | 1       | 7       | ORIGINAL          |
      | LAST      | 2       | 7       | ORIGINAL          |


  Scenario: Meta-Data for hyphenated name
    Given HMRC has the following individual records
      | First name      | Last name       | Date of Birth | nino      |
      | BobChicharito   | Higuain         | 1987-12-10    | SE123456B |
    When an income request is made with the following identity
      | First name    | Ali Bob-Chicharito Danilo Estoban |
      | Last name     | Figuero Gonzalo Higuain           |
      | Date of Birth | 1987-12-10                        |
      | nino          | SE 123456 B                       |
    Then meta-data was logged following a successful match
    And the meta-data contains the following input name information
      | nameType  | index | diacritics  | umlauts | abbreviation  | nameSplitter  | uniCodeBlocks | length  |
      | FIRST     | 0     | false       | false   | false         | false         | BASIC_LATIN   | 3       |
      | FIRST     | 1     | false       | false   | false         | true          | BASIC_LATIN   | 14      |
      | FIRST     | 2     | false       | false   | false         | false         | BASIC_LATIN   | 6       |
      | FIRST     | 3     | false       | false   | false         | false         | BASIC_LATIN   | 7       |
      | LAST      | 0     | false       | false   | false         | false         | BASIC_LATIN   | 7       |
      | LAST      | 1     | false       | false   | false         | false         | BASIC_LATIN   | 7       |
      | LAST      | 2     | false       | false   | false         | false         | BASIC_LATIN   | 7       |
    And the meta-data indicates that the following generators were used
      | NAME_COMBINATIONS    |
    And the meta-data contains the following name derivation information
      | section   | index   | length  | derivationActions |
      | FIRST     | 1       | 14      | ORIGINAL          |
      | LAST      | 2       | 7       | ORIGINAL          |



  Scenario: Meta-Data for hyphenated first name
    Given HMRC has the following individual records
      | First name      | Last name       | Date of Birth | nino      |
      | Bob-Chicharito  | Higuain         | 1987-12-10    | SE123456B |
    When an income request is made with the following identity
      | First name    | Ali Bob-Chicharito Danilo Estoban |
      | Last name     | Figuero Gonzalo Higuain           |
      | Date of Birth | 1987-12-10                        |
      | nino          | SE 123456 B                       |
    Then meta-data was logged following a successful match
    And the meta-data contains the following input name information
      | nameType  | index | diacritics  | umlauts | abbreviation  | nameSplitter  | uniCodeBlocks | length  |
      | FIRST     | 0     | false       | false   | false         | false         | BASIC_LATIN   | 3       |
      | FIRST     | 1     | false       | false   | false         | true          | BASIC_LATIN   | 14      |
      | FIRST     | 2     | false       | false   | false         | false         | BASIC_LATIN   | 6       |
      | FIRST     | 3     | false       | false   | false         | false         | BASIC_LATIN   | 7       |
      | LAST      | 0     | false       | false   | false         | false         | BASIC_LATIN   | 7       |
      | LAST      | 1     | false       | false   | false         | false         | BASIC_LATIN   | 7       |
      | LAST      | 2     | false       | false   | false         | false         | BASIC_LATIN   | 7       |
    And the meta-data indicates that the following generators were used
      | NAME_COMBINATIONS    |
    And the meta-data contains the following name derivation information
      | section   | index   | length  | derivationActions |
      | FIRST     | 1       | 14      | ORIGINAL          |
      | LAST      | 2       | 7       | ORIGINAL          |



  Scenario: Meta-Data for hyphenated last name
    Given HMRC has the following individual records
      | First name      | Last name       | Date of Birth | nino      |
      | Bob             | El-Mohtar       | 1987-12-10    | SE123456B |
    When an income request is made with the following identity
      | First name    | Bob         |
      | Last name     | El-Mohtar   |
      | Date of Birth | 1987-12-10  |
      | nino          | SE 123456 B |
    Then meta-data was logged following a successful match
    And the meta-data contains the following input name information
      | nameType  | index | diacritics  | umlauts | abbreviation  | nameSplitter  | uniCodeBlocks | length  |
      | FIRST     | 0     | false       | false   | false         | false         | BASIC_LATIN   | 3       |
      | LAST      | 0     | false       | false   | false         | true          | BASIC_LATIN   | 9       |
    And the meta-data indicates that the following generators were used
      | ENTIRE_NON_ALIAS_NAME    |
    And the meta-data contains the following name derivation information
      | section   | index   | length  | derivationActions |
      | FIRST     | 0       | 3       | ENTIRE            |
      | LAST      | 0       | 9       | ENTIRE            |
