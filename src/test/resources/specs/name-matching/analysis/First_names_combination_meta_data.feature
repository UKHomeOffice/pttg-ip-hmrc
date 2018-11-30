@jira=EE-8289

Feature: Analysis logging

  Scenario: Meta-Data for Multi-part first name
    Given HMRC has the following individual records
      | First name      | Last name       | Date of Birth | nino        |
      | Bob             | Cheese          | 0001-01-01    | NR123456C   |
    When an income request is made with the following identity
      | First name    | Arthur Bob   |
      | Last name     | Cheese       |
      | Date of Birth | 0001-01-01   |
      | nino          | NR123456C    |
    Then meta-data was logged following a successful match
    And the meta-data contains the following input name information
      | nameType  | index | diacritics  | umlauts | abbreviation  | nameSplitter  | uniCodeBlocks | length  |
      | FIRST     | 0     | false       | false   | false         | false         | BASIC_LATIN   | 6       |
      | FIRST     | 1     | false       | false   | false         | false         | BASIC_LATIN   | 3       |
      | LAST      | 0     | false       | false   | false         | false         | BASIC_LATIN   | 6       |
    And the meta-data indicates that the following generators were used
      | ENTIRE_LAST_NAME_AND_EACH_FIRST_NAME    |
    And the meta-data contains the following name derivation information
      | section   | index   | length  | derivationActions |
      | FIRST     | 1       | 3       | ORIGINAL          |
      | LAST      | 0       | 6       | ENTIRE            |

  Scenario: Meta-Data for Hyphenated first name
    Given HMRC has the following individual records
      | First name      | Last name       | Date of Birth | nino        |
      | Arthur-Brian    | Coates          | 0001-01-01    | NR123456C   |
    When an income request is made with the following identity
      | First name    | Arthur-Brian  |
      | Last name     | Coates        |
      | Date of Birth | 0001-01-01    |
      | nino          | NR123456C     |
    Then meta-data was logged following a successful match
    And the meta-data contains the following input name information
      | nameType  | index | diacritics  | umlauts | abbreviation  | nameSplitter  | uniCodeBlocks | length  |
      | FIRST     | 0     | false       | false   | false         | true          | BASIC_LATIN   | 12      |
      | LAST      | 0     | false       | false   | false         | false         | BASIC_LATIN   | 6       |
    And the meta-data indicates that the following generators were used
      | ENTIRE_NON_ALIAS_NAME    |
    And the meta-data contains the following name derivation information
      | section   | index   | length  | derivationActions |
      | FIRST     | 0       | 12      | ENTIRE            |
      | LAST      | 0       | 6       | ENTIRE            |


  Scenario: Meta-Data for Hyphenated first name (with splitting)
    Given HMRC has the following individual records
      | First name      | Last name       | Date of Birth | nino        |
      | Brian           | Coates          | 0001-01-01    | NR123456C   |
    When an income request is made with the following identity
      | First name    | Arthur-Brian  |
      | Last name     | Coates        |
      | Date of Birth | 0001-01-01    |
      | nino          | NR123456C     |
    Then meta-data was logged following a successful match
    And the meta-data contains the following input name information
      | nameType  | index | diacritics  | umlauts | abbreviation  | nameSplitter  | uniCodeBlocks | length  |
      | FIRST     | 0     | false       | false   | false         | true          | BASIC_LATIN   | 12      |
      | LAST      | 0     | false       | false   | false         | false         | BASIC_LATIN   | 6       |
    And the meta-data indicates that the following generators were used
      | SPLITTERS_REPLACED                      |
      | ENTIRE_LAST_NAME_AND_EACH_FIRST_NAME    |
    And the meta-data contains the following name derivation information
      | section   | index   | length  | derivationActions |
      | FIRST     | 0       | 5       | RIGHT_OF_SPLIT    |
      | LAST      | 0       | 6       | ENTIRE            |

  Scenario: Meta-Data for Ignore first names when too many names in total
    Given HMRC has the following individual records
      | First name      | Last name       | Date of Birth | nino        |
      | C               | Halen           | 9999-12-31    | NR123456C   |
    When an income request is made with the following identity
      | First name    | A B C D E F G  |
      | Last name     | Van Halen      |
      | Date of Birth | 9999-12-31     |
      | nino          | NR123456C      |
    Then meta-data was logged following a successful match
    And the meta-data contains the following input name information
      | nameType  | index | diacritics  | umlauts | abbreviation  | nameSplitter  | uniCodeBlocks | length  |
      | FIRST     | 0     | false       | false   | false         | false         | BASIC_LATIN   | 1       |
      | FIRST     | 1     | false       | false   | false         | false         | BASIC_LATIN   | 1       |
      | FIRST     | 2     | false       | false   | false         | false         | BASIC_LATIN   | 1       |
      | FIRST     | 3     | false       | false   | false         | false         | BASIC_LATIN   | 1       |
      | FIRST     | 4     | false       | false   | false         | false         | BASIC_LATIN   | 1       |
      | FIRST     | 5     | false       | false   | false         | false         | BASIC_LATIN   | 1       |
      | FIRST     | 6     | false       | false   | false         | false         | BASIC_LATIN   | 1       |
      | LAST      | 0     | false       | false   | false         | false         | BASIC_LATIN   | 3       |
      | LAST      | 1     | false       | false   | false         | false         | BASIC_LATIN   | 5       |
    And the meta-data indicates that the following generators were used
      | NAME_COMBINATIONS    |
    And the meta-data contains the following name derivation information
      | section   | index   | length  | derivationActions |
      | FIRST     | 2       | 1       | ORIGINAL          |
      | LAST      | 1       | 5       | ORIGINAL          |

