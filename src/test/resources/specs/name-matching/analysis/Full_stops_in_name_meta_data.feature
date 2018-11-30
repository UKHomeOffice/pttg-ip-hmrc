@jira=EE-8289

Feature: Analysis logging

  Scenario: Meta-Data for Name with a full stop and a space
    Given HMRC has the following individual records
      | First name      | Last name       | Date of Birth | nino        |
      | Aaa             | Bb. Ccc         | 1987-12-10    | SE123456B   |
    When an income request is made with the following identity
      | First name    | Aaa        |
      | Last name     | Bb. Ccc    |
      | Date of Birth | 1987-12-10 |
      | nino          | SE123456B  |
    Then meta-data was logged following a successful match
    And the meta-data contains the following input name information
      | nameType  | index | diacritics  | umlauts | abbreviation  | nameSplitter  | uniCodeBlocks | length  |
      | FIRST     | 0     | false       | false   | false         | false         | BASIC_LATIN   | 3       |
      | LAST      | 0     | false       | false   | true          | true          | BASIC_LATIN   | 3       |
      | LAST      | 1     | false       | false   | false         | false         | BASIC_LATIN   | 3       |
    And the meta-data indicates that the following generators were used
      | ENTIRE_NON_ALIAS_NAME    |
    And the meta-data contains the following name derivation information
      | section   | index   | length  | derivationActions |
      | FIRST     | 0       | 3       | ENTIRE            |
      | LAST      | 0 1     | 7       | ENTIRE            |


  Scenario: Meta-Data for Enters full stop when name does not contain a full stop
    Given HMRC has the following individual records
      | First name      | Last name       | Date of Birth | nino        |
      | Aaa             | Bb Ccc          | 1987-12-10    | SE123456B   |
    When an income request is made with the following identity
      | First name    | Aaa        |
      | Last name     | Bb. Ccc    |
      | Date of Birth | 1987-12-10 |
      | nino          | SE123456B  |
    Then meta-data was logged following a successful match
    And the meta-data contains the following input name information
      | nameType  | index | diacritics  | umlauts | abbreviation  | nameSplitter  | uniCodeBlocks | length  |
      | FIRST     | 0     | false       | false   | false         | false         | BASIC_LATIN   | 3       |
      | LAST      | 0     | false       | false   | true          | true          | BASIC_LATIN   | 3       |
      | LAST      | 1     | false       | false   | false         | false         | BASIC_LATIN   | 3       |
    And the meta-data indicates that the following generators were used
      | SPLITTERS_REMOVED         |
      | ENTIRE_NON_ALIAS_NAME     |
    And the meta-data contains the following name derivation information
      | section   | index   | length  | derivationActions |
      | FIRST     | 0       | 3       | ENTIRE            |
      | LAST      | 0 1     | 6       | ENTIRE            |


  Scenario: Meta-Data for Name with a full stop and no space
    Given HMRC has the following individual records
      | First name      | Last name       | Date of Birth | nino        |
      | Aaa             | Bb.Ccc          | 1987-12-10    | SE123456B   |
    When an income request is made with the following identity
      | First name    | Aaa        |
      | Last name     | Bb.Ccc     |
      | Date of Birth | 1987-12-10 |
      | nino          | SE123456B  |
    Then meta-data was logged following a successful match
    And the meta-data contains the following input name information
      | nameType  | index | diacritics  | umlauts | abbreviation  | nameSplitter  | uniCodeBlocks | length  |
      | FIRST     | 0     | false       | false   | false         | false         | BASIC_LATIN   | 3       |
      | LAST      | 0     | false       | false   | true          | true          | BASIC_LATIN   | 6       |
    And the meta-data indicates that the following generators were used
      | ENTIRE_NON_ALIAS_NAME     |
    And the meta-data contains the following name derivation information
      | section   | index   | length  | derivationActions |
      | FIRST     | 0       | 3       | ENTIRE            |
      | LAST      | 0       | 6       | ENTIRE            |


  Scenario: Meta-Data for Name when middle name has a full stop and a space
    Given HMRC has the following individual records
      | First name      | Last name       | Date of Birth | nino      |
      | Ddd             | B. Ccc          | 1987-12-10    | SE123456B |
    When an income request is made with the following identity
      | First name    | Aaa B. Ccc  |
      | Last name     | Ddd         |
      | Date of Birth | 1987-12-10  |
      | nino          | SE123456B   |
    Then meta-data was logged following a successful match
    And the meta-data contains the following input name information
      | nameType  | index | diacritics  | umlauts | abbreviation  | nameSplitter  | uniCodeBlocks | length  |
      | FIRST     | 0     | false       | false   | false         | false         | BASIC_LATIN   | 3       |
      | FIRST     | 1     | false       | false   | true          | true          | BASIC_LATIN   | 2       |
      | FIRST     | 2     | false       | false   | false         | false         | BASIC_LATIN   | 3       |
      | LAST      | 0     | false       | false   | false         | false         | BASIC_LATIN   | 3       |
    And the meta-data indicates that the following generators were used
      | ABBREVIATED_NAMES |
      | NAME_COMBINATIONS     |
    And the meta-data contains the following name derivation information
      | section   | index   | length  | derivationActions |
      | LAST      | 0       | 3       | ORIGINAL          |
      | FIRST     | 1       | 6       | ABBREVIATED_PAIR  |


  Scenario: Meta-Data for Alias name with a full stop and no space
    Given HMRC has the following individual records
      | First name      | Last name       | Date of Birth | nino      |
      | Aaa             | Ddd             | 1987-12-10    | SE123456B |
    When an income request is made with the following identity
      | First name    | Aaa        |
      | Last name     | Bbb        |
      | Alias Surname | Cc.Ddd     |
      | Date of Birth | 1987-12-10 |
      | nino          | SE123456B  |
    Then meta-data was logged following a successful match
    And the meta-data contains the following input name information
      | nameType  | index | diacritics  | umlauts | abbreviation  | nameSplitter  | uniCodeBlocks | length  |
      | FIRST     | 0     | false       | false   | false         | false         | BASIC_LATIN   | 3       |
      | LAST      | 0     | false       | false   | false         | false         | BASIC_LATIN   | 3       |
      | ALIAS     | 0     | false       | false   | true          | true          | BASIC_LATIN   | 6       |
    And the meta-data indicates that the following generators were used
      | SPLITTERS_REPLACED |
      | ALIAS_COMBINATIONS |
    And the meta-data contains the following name derivation information
      | section   | index   | length  | derivationActions |
      | FIRST     | 0       | 3       | ORIGINAL          |
      | ALIAS     | 0       | 3       | RIGHT_OF_SPLIT    |


  Scenario: Meta-Data for Alias name with a full stop and a space is matched
    Given HMRC has the following individual records
      | First name      | Last name       | Date of Birth | nino      |
      | Cc. Ddd         | Aaa             | 1987-12-10    | SE123456B |
    When an income request is made with the following identity
      | First name    | Aaa        |
      | Last name     | Bbb        |
      | Alias Surname | Cc. Ddd    |
      | Date of Birth | 1987-12-10 |
      | nino          | SE123456B  |
    Then meta-data was logged following a successful match
    And the meta-data contains the following input name information
      | nameType  | index | diacritics  | umlauts | abbreviation  | nameSplitter  | uniCodeBlocks | length  |
      | FIRST     | 0     | false       | false   | false         | false         | BASIC_LATIN   | 3       |
      | LAST      | 0     | false       | false   | false         | false         | BASIC_LATIN   | 3       |
      | ALIAS     | 0     | false       | false   | true          | true          | BASIC_LATIN   | 3       |
      | ALIAS     | 1     | false       | false   | false         | false         | BASIC_LATIN   | 3       |
    And the meta-data indicates that the following generators were used
      | ABBREVIATED_NAMES  |
      | ALIAS_COMBINATIONS |
    And the meta-data contains the following name derivation information
      | section   | index   | length  | derivationActions |
      | ALIAS     | 0       | 7       | ABBREVIATED_PAIR  |
      | FIRST     | 0       | 3       | ORIGINAL          |

