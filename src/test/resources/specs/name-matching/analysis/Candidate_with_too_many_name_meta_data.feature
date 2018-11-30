@jira=EE-8289

Feature: Analysis logging

  Scenario: Meta-Data for Only use the first 4 first names when maximum number of last names
    Given HMRC has the following individual records
      | First name | Last name | Date of Birth | nino      |
      | Ggg        | Fff       | 1987-12-10    | SE123456B |
    When an income request is made with the following identity
      | First name    | Aaa Bbb Ccc Ddd Xxx |
      | Last name     | Eee Fff Ggg         |
      | Date of Birth | 1987-12-10          |
      | nino          | SE 123456 B         |
    Then meta-data was logged following a successful match
    And the meta-data contains the following input name information
      | nameType  | index | diacritics  | umlauts | abbreviation  | nameSplitter  | uniCodeBlocks | length  |
      | FIRST     | 0     | false       | false   | false         | false         | BASIC_LATIN   | 3       |
      | FIRST     | 1     | false       | false   | false         | false         | BASIC_LATIN   | 3       |
      | FIRST     | 2     | false       | false   | false         | false         | BASIC_LATIN   | 3       |
      | FIRST     | 3     | false       | false   | false         | false         | BASIC_LATIN   | 3       |
      | FIRST     | 4     | false       | false   | false         | false         | BASIC_LATIN   | 3       |
      | LAST      | 0     | false       | false   | false         | false         | BASIC_LATIN   | 3       |
      | LAST      | 1     | false       | false   | false         | false         | BASIC_LATIN   | 3       |
      | LAST      | 2     | false       | false   | false         | false         | BASIC_LATIN   | 3       |
    And the meta-data indicates that the following generators were used
      | NAME_COMBINATIONS    |
    And the meta-data contains the following name derivation information
      | section   | index | length  | derivationActions |
      | LAST      | 2     | 3       | ORIGINAL          |
      | LAST      | 1     | 3       | ORIGINAL          |

