@jira=EE-8289

Feature: Analysis logging

  Scenario: Meta-Data for match on First Name 1 and Alias 1
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

  Scenario: Meta-Data for match on First Name 2 and Entire Last Name
    Given HMRC has the following individual records
      | First name | Last name | Date of Birth | nino      |
      | Aaa        | Ddd XXX       | 1987-12-10    | SE123456B |
    When an income request is made with the following identity
      | First name    | Bbb Aaa     |
      | Last name     | Ddd XXX     |
      | Date of Birth | 1987-12-10  |
      | nino          | SE123456B   |
    Then meta-data was logged following a successful match
    And the meta-data contains the following input name information
      | nameType  | index | diacritics  | umlauts | abbreviation  | nameSplitter  | uniCodeBlocks | length  |
      | FIRST     | 0     | false       | false   | false         | false         | BASIC_LATIN   | 3       |
      | FIRST     | 1     | false       | false   | false         | false         | BASIC_LATIN   | 3       |
      | LAST      | 0     | false       | false   | false         | false         | BASIC_LATIN   | 3       |
      | LAST      | 1     | false       | false   | false         | false         | BASIC_LATIN   | 3       |
    And the meta-data indicates that the following generators were used
      | ENTIRE_LAST_NAME_AND_EACH_FIRST_NAME    |
    And the meta-data contains the following name derivation information
      | section   | index | length  | derivationActions |
      | FIRST     | 1     | 3       | ORIGINAL          |
      | LAST      | 0 1   | 7       | ENTIRE            |

