@jira=EE-8289

Feature: Analysis logging

  Scenario: Meta-Data for Multi-part last name is tried without splitting
    Given HMRC has the following individual records
      | First name      | Last name       | Date of Birth | nino        |
      | Arthur          | Brian Coates    | 9999-12-31    | NR123456C   |
    When an income request is made with the following identity
      | First name    | Arthur       |
      | Last name     | Brian Coates |
      | Date of Birth | 9999-12-31   |
      | nino          | NR123456C    |
    Then meta-data was logged following a successful match
    And the meta-data contains the following input name information
      | nameType  | index | diacritics  | umlauts | abbreviation  | nameSplitter  | uniCodeBlocks | length  |
      | FIRST     | 0     | false       | false   | false         | false         | BASIC_LATIN   | 6       |
      | LAST      | 0     | false       | false   | false         | false         | BASIC_LATIN   | 5       |
      | LAST      | 1     | false       | false   | false         | false         | BASIC_LATIN   | 6       |
    And the meta-data indicates that the following generators were used
      | ENTIRE_NON_ALIAS_NAME    |
    And the meta-data contains the following name derivation information
      | section   | index   | length  | derivationActions |
      | FIRST     | 0       | 6       | ENTIRE            |
      | LAST      | 0 1     | 12      | ENTIRE            |


  Scenario: Meta-Data for Multi-part last name is tried with splitting
    Given HMRC has the following individual records
      | First name      | Last name       | Date of Birth | nino        |
      | Arthur          | Coates          | 9999-12-31    | NR123456C   |
    When an income request is made with the following identity
      | First name    | Arthur       |
      | Last name     | Brian Coates |
      | Date of Birth | 9999-12-31   |
      | nino          | NR123456C    |
    Then meta-data was logged following a successful match
    And the meta-data contains the following input name information
      | nameType  | index | diacritics  | umlauts | abbreviation  | nameSplitter  | uniCodeBlocks | length  |
      | FIRST     | 0     | false       | false   | false         | false         | BASIC_LATIN   | 6       |
      | LAST      | 0     | false       | false   | false         | false         | BASIC_LATIN   | 5       |
      | LAST      | 1     | false       | false   | false         | false         | BASIC_LATIN   | 6       |
    And the meta-data indicates that the following generators were used
      | NAME_COMBINATIONS    |
    And the meta-data contains the following name derivation information
      | section   | index   | length  | derivationActions |
      | FIRST     | 0       | 6       | ORIGINAL            |
      | LAST      | 1       | 6       | ORIGINAL          |


  Scenario: Meta-Data for Hyphenated last name is tried without splitting
    Given HMRC has the following individual records
      | First name      | Last name       | Date of Birth | nino        |
      | Arthur          | Brown-Coates    | 9999-12-31    | NR123456C   |
    When an income request is made with the following identity
      | First name    | Arthur       |
      | Last name     | Brown-Coates |
      | Date of Birth | 9999-12-31   |
      | nino          | NR123456C    |
    Then meta-data was logged following a successful match
    And the meta-data contains the following input name information
      | nameType  | index | diacritics  | umlauts | abbreviation  | nameSplitter  | uniCodeBlocks | length  |
      | FIRST     | 0     | false       | false   | false         | false         | BASIC_LATIN   | 6       |
      | LAST      | 0     | false       | false   | false         | true          | BASIC_LATIN   | 12      |
    And the meta-data indicates that the following generators were used
      | ENTIRE_NON_ALIAS_NAME    |
    And the meta-data contains the following name derivation information
      | section   | index   | length  | derivationActions |
      | FIRST     | 0       | 6       | ENTIRE            |
      | LAST      | 0       | 12      | ENTIRE            |


  Scenario: Meta-Data for Hyphenated last name is tried with splitting
    Given HMRC has the following individual records
      | First name      | Last name       | Date of Birth | nino        |
      | Arthur          | Coates          | 9999-12-31    | NR123456C   |
    When an income request is made with the following identity
      | First name    | Arthur       |
      | Last name     | Brown-Coates |
      | Date of Birth | 9999-12-31   |
      | nino          | NR123456C    |
    Then meta-data was logged following a successful match
    And the meta-data contains the following input name information
      | nameType  | index | diacritics  | umlauts | abbreviation  | nameSplitter  | uniCodeBlocks | length  |
      | FIRST     | 0     | false       | false   | false         | false         | BASIC_LATIN   | 6       |
      | LAST      | 0     | false       | false   | false         | true          | BASIC_LATIN   | 12      |
    And the meta-data indicates that the following generators were used
      | SPLITTERS_REPLACED |
      | NAME_COMBINATIONS      |
    And the meta-data contains the following name derivation information
      | section   | index   | length  | derivationActions |
      | FIRST     | 0       | 6       | ORIGINAL          |
      | LAST      | 0       | 6       | RIGHT_OF_SPLIT    |



  Scenario: Meta-Data for Apostrophed last name is tried with apostrophe
    Given HMRC has the following individual records
      | First name      | Last name       | Date of Birth | nino        |
      | Arthur          | O'Bobbins       | 9999-12-31    | NR123456C   |
    When an income request is made with the following identity
      | First name    | Arthur     |
      | Last name     | O'Bobbins  |
      | Date of Birth | 9999-12-31 |
      | nino          | NR123456C  |
    Then meta-data was logged following a successful match
    And the meta-data contains the following input name information
      | nameType  | index | diacritics  | umlauts | abbreviation  | nameSplitter  | uniCodeBlocks | length  |
      | FIRST     | 0     | false       | false   | false         | false         | BASIC_LATIN   | 6       |
      | LAST      | 0     | false       | false   | false         | true          | BASIC_LATIN   | 9       |
    And the meta-data indicates that the following generators were used
      | ENTIRE_NON_ALIAS_NAME      |
    And the meta-data contains the following name derivation information
      | section   | index   | length  | derivationActions |
      | FIRST     | 0       | 6       | ENTIRE            |
      | LAST      | 0       | 9       | ENTIRE            |


  Scenario: Meta-Data for Apostrophed last name is tried without apostrophes 1
    Given HMRC has the following individual records
      | First name      | Last name       | Date of Birth | nino        |
      | Arthur          | O Bobbins       | 9999-12-31    | NR123456C   |
    When an income request is made with the following identity
      | First name    | Arthur     |
      | Last name     | O'Bobbins  |
      | Date of Birth | 9999-12-31 |
      | nino          | NR123456C  |
    Then meta-data was logged following a successful match
    And the meta-data contains the following input name information
      | nameType  | index | diacritics  | umlauts | abbreviation  | nameSplitter  | uniCodeBlocks | length  |
      | FIRST     | 0     | false       | false   | false         | false         | BASIC_LATIN   | 6       |
      | LAST      | 0     | false       | false   | false         | true          | BASIC_LATIN   | 9       |
    And the meta-data indicates that the following generators were used
      | SPLITTERS_REPLACED          |
      | ENTIRE_NON_ALIAS_NAME       |
    And the meta-data contains the following name derivation information
      | section   | index   | length  | derivationActions |
      | FIRST     | 0       | 6       | ENTIRE            |
      | LAST      | 0       | 9       | ENTIRE            |


  Scenario: Meta-Data for Apostrophed last name is tried without apostrophes 2
    Given HMRC has the following individual records
      | First name      | Last name       | Date of Birth | nino        |
      | Arthur          | OBobbins        | 9999-12-31    | NR123456C   |
    When an income request is made with the following identity
      | First name    | Arthur     |
      | Last name     | O'Bobbins  |
      | Date of Birth | 9999-12-31 |
      | nino          | NR123456C  |
    Then meta-data was logged following a successful match
    And the meta-data contains the following input name information
      | nameType  | index | diacritics  | umlauts | abbreviation  | nameSplitter  | uniCodeBlocks | length  |
      | FIRST     | 0     | false       | false   | false         | false         | BASIC_LATIN   | 6       |
      | LAST      | 0     | false       | false   | false         | true          | BASIC_LATIN   | 9       |
    And the meta-data indicates that the following generators were used
      | SPLITTERS_REMOVED           |
      | ENTIRE_NON_ALIAS_NAME       |
    And the meta-data contains the following name derivation information
      | section   | index   | length  | derivationActions |
      | FIRST     | 0       | 6       | ENTIRE            |
      | LAST      | 0       | 8       | ENTIRE            |


  Scenario: Meta-Data for Apostrophed last name is tried without apostrophes 3
    Given HMRC has the following individual records
      | First name      | Last name       | Date of Birth | nino        |
      | Arthur          | Bobbins         | 9999-12-31    | NR123456C   |
    When an income request is made with the following identity
      | First name    | Arthur     |
      | Last name     | O'Bobbins  |
      | Date of Birth | 9999-12-31 |
      | nino          | NR123456C  |
    Then meta-data was logged following a successful match
    And the meta-data contains the following input name information
      | nameType  | index | diacritics  | umlauts | abbreviation  | nameSplitter  | uniCodeBlocks | length  |
      | FIRST     | 0     | false       | false   | false         | false         | BASIC_LATIN   | 6       |
      | LAST      | 0     | false       | false   | false         | true          | BASIC_LATIN   | 9       |
    And the meta-data indicates that the following generators were used
      | SPLITTERS_REPLACED           |
      | NAME_COMBINATIONS                |
    And the meta-data contains the following name derivation information
      | section   | index   | length  | derivationActions |
      | FIRST     | 0       | 6       | ORIGINAL          |
      | LAST      | 0       | 7       | RIGHT_OF_SPLIT    |


  Scenario: Meta-Data for Apostrophed last name is tried without apostrophes 4
    Given HMRC has the following individual records
      | First name      | Last name       | Date of Birth | nino        |
      | Arthur          | O               | 9999-12-31    | NR123456C   |
    When an income request is made with the following identity
      | First name    | Arthur     |
      | Last name     | O'Bobbins  |
      | Date of Birth | 9999-12-31 |
      | nino          | NR123456C  |
    Then meta-data was logged following a successful match
    And the meta-data contains the following input name information
      | nameType  | index | diacritics  | umlauts | abbreviation  | nameSplitter  | uniCodeBlocks | length  |
      | FIRST     | 0     | false       | false   | false         | false         | BASIC_LATIN   | 6       |
      | LAST      | 0     | false       | false   | false         | true          | BASIC_LATIN   | 9       |
    And the meta-data indicates that the following generators were used
      | ENTIRE_NON_ALIAS_NAME      |
    And the meta-data contains the following name derivation information
      | section   | index   | length  | derivationActions |
      | FIRST     | 0       | 6       | ENTIRE            |
      | LAST      | 0       | 9       | ENTIRE            |


  Scenario: Meta-Data for Apostrophed and Hyphenated names are tried with apostrophes and hyphens
    Given HMRC has the following individual records
      | First name      | Last name       | Date of Birth | nino        |
      | Arthur-Brian    | O'Coates        | 9999-12-31    | NR123456C   |
    When an income request is made with the following identity
      | First name    | Arthur-Brian  |
      | Last name     | O'Coates      |
      | Date of Birth | 9999-12-31    |
      | nino          | NR123456C     |
    Then meta-data was logged following a successful match
    And the meta-data contains the following input name information
      | nameType  | index | diacritics  | umlauts | abbreviation  | nameSplitter  | uniCodeBlocks | length  |
      | FIRST     | 0     | false       | false   | false         | true          | BASIC_LATIN   | 12      |
      | LAST      | 0     | false       | false   | false         | true          | BASIC_LATIN   | 8       |
    And the meta-data indicates that the following generators were used
      | ENTIRE_NON_ALIAS_NAME      |
    And the meta-data contains the following name derivation information
      | section   | index   | length  | derivationActions |
      | FIRST     | 0       | 12      | ENTIRE            |
      | LAST      | 0       | 8       | ENTIRE            |


  Scenario: Meta-Data for Apostrophed and Hyphenated names are tried without apostrophes and hyphens 1
    Given HMRC has the following individual records
      | First name      | Last name       | Date of Birth | nino        |
      | Arthur-Brian    | O Coates        | 9999-12-31    | NR123456C   |
    When an income request is made with the following identity
      | First name    | Arthur-Brian  |
      | Last name     | O'Coates      |
      | Date of Birth | 9999-12-31    |
      | nino          | NR123456C     |
    Then meta-data was logged following a successful match
    And the meta-data contains the following input name information
      | nameType  | index | diacritics  | umlauts | abbreviation  | nameSplitter  | uniCodeBlocks | length  |
      | FIRST     | 0     | false       | false   | false         | true          | BASIC_LATIN   | 12      |
      | LAST      | 0     | false       | false   | false         | true          | BASIC_LATIN   | 8       |
    And the meta-data indicates that the following generators were used
      | SPLITTERS_REPLACED         |
      | ENTIRE_NON_ALIAS_NAME      |
    And the meta-data contains the following name derivation information
      | section   | index   | length  | derivationActions |
      | FIRST     | 0       | 12      | ENTIRE            |
      | LAST      | 0       | 8       | ENTIRE            |


  Scenario: Meta-Data for Apostrophed and Hyphenated names are tried without apostrophes and hyphens 2
    Given HMRC has the following individual records
      | First name      | Last name       | Date of Birth | nino        |
      | Brian           | O Coates        | 9999-12-31    | NR123456C   |
    When an income request is made with the following identity
      | First name    | Arthur-Brian  |
      | Last name     | O'Coates      |
      | Date of Birth | 9999-12-31    |
      | nino          | NR123456C     |
    Then meta-data was logged following a successful match
    And the meta-data contains the following input name information
      | nameType  | index | diacritics  | umlauts | abbreviation  | nameSplitter  | uniCodeBlocks | length  |
      | FIRST     | 0     | false       | false   | false         | true          | BASIC_LATIN   | 12      |
      | LAST      | 0     | false       | false   | false         | true          | BASIC_LATIN   | 8       |
    And the meta-data indicates that the following generators were used
      | SPLITTERS_REPLACED                   |
      | ENTIRE_LAST_NAME_AND_EACH_FIRST_NAME |
    And the meta-data contains the following name derivation information
      | section   | index   | length  | derivationActions |
      | FIRST     | 0       | 5       | RIGHT_OF_SPLIT    |
      | LAST      | 0       | 8       | ENTIRE            |


  Scenario: Meta-Data for Apostrophed and Hyphenated names are tried without apostrophes and hyphens 3
    Given HMRC has the following individual records
      | First name      | Last name       | Date of Birth | nino        |
      | Arthur          | Coates          | 9999-12-31    | NR123456C   |
    When an income request is made with the following identity
      | First name    | Arthur-Brian  |
      | Last name     | O'Coates      |
      | Date of Birth | 9999-12-31    |
      | nino          | NR123456C     |
    Then meta-data was logged following a successful match
    And the meta-data contains the following input name information
      | nameType  | index | diacritics  | umlauts | abbreviation  | nameSplitter  | uniCodeBlocks | length  |
      | FIRST     | 0     | false       | false   | false         | true          | BASIC_LATIN   | 12      |
      | LAST      | 0     | false       | false   | false         | true          | BASIC_LATIN   | 8       |
    And the meta-data indicates that the following generators were used
      | SPLITTERS_REPLACED  |
      | NAME_COMBINATIONS       |
    And the meta-data contains the following name derivation information
      | section   | index   | length  | derivationActions |
      | FIRST     | 0       | 6       | LEFT_OF_SPLIT     |
      | LAST      | 0       | 6       | RIGHT_OF_SPLIT    |



  Scenario: Meta-Data for Apostrophed and Hyphenated names are tried without apostrophes and hyphens 4
    Given HMRC has the following individual records
      | First name      | Last name       | Date of Birth | nino        |
      | Brian           | Coates          | 9999-12-31    | NR123456C   |
    When an income request is made with the following identity
      | First name    | Arthur-Brian  |
      | Last name     | O'Coates      |
      | Date of Birth | 9999-12-31    |
      | nino          | NR123456C     |
    Then meta-data was logged following a successful match
    And the meta-data contains the following input name information
      | nameType  | index | diacritics  | umlauts | abbreviation  | nameSplitter  | uniCodeBlocks | length  |
      | FIRST     | 0     | false       | false   | false         | true          | BASIC_LATIN   | 12      |
      | LAST      | 0     | false       | false   | false         | true          | BASIC_LATIN   | 8       |
    And the meta-data indicates that the following generators were used
      | SPLITTERS_REPLACED  |
      | NAME_COMBINATIONS       |
    And the meta-data contains the following name derivation information
      | section   | index   | length  | derivationActions |
      | FIRST     | 0       | 5       | RIGHT_OF_SPLIT    |
      | LAST      | 0       | 6       | RIGHT_OF_SPLIT    |


  Scenario: Meta-Data for Apostrophed and Hyphenated names are tried without apostrophes and hyphens 6
    Given HMRC has the following individual records
      | First name      | Last name       | Date of Birth | nino        |
      | Arthur          | O               | 9999-12-31    | NR123456C   |
    When an income request is made with the following identity
      | First name    | Arthur-Brian  |
      | Last name     | O'Coates      |
      | Date of Birth | 9999-12-31    |
      | nino          | NR123456C     |
    Then meta-data was logged following a successful match
    And the meta-data contains the following input name information
      | nameType  | index | diacritics  | umlauts | abbreviation  | nameSplitter  | uniCodeBlocks | length  |
      | FIRST     | 0     | false       | false   | false         | true          | BASIC_LATIN   | 12      |
      | LAST      | 0     | false       | false   | false         | true          | BASIC_LATIN   | 8       |
    And the meta-data indicates that the following generators were used
      | ENTIRE_NON_ALIAS_NAME  |
    And the meta-data contains the following name derivation information
      | section   | index   | length  | derivationActions |
      | FIRST     | 0       | 12      | ENTIRE            |
      | LAST      | 0       | 8       | ENTIRE            |


  Scenario: Meta-Data for Apostrophed and Hyphenated names are tried without apostrophes and hyphens 5
    Given HMRC has the following individual records
      | First name      | Last name       | Date of Birth | nino        |
      | Brian           | O               | 9999-12-31    | NR123456C   |
    When an income request is made with the following identity
      | First name    | Arthur-Brian  |
      | Last name     | O'Coates      |
      | Date of Birth | 9999-12-31    |
      | nino          | NR123456C     |
    Then meta-data was logged following a successful match
    And the meta-data contains the following input name information
      | nameType  | index | diacritics  | umlauts | abbreviation  | nameSplitter  | uniCodeBlocks | length  |
      | FIRST     | 0     | false       | false   | false         | true          | BASIC_LATIN   | 12      |
      | LAST      | 0     | false       | false   | false         | true          | BASIC_LATIN   | 8       |
    And the meta-data indicates that the following generators were used
      | SPLITTERS_REPLACED                    |
      | ENTIRE_LAST_NAME_AND_EACH_FIRST_NAME  |
    And the meta-data contains the following name derivation information
      | section   | index   | length  | derivationActions |
      | FIRST     | 0       | 5       | RIGHT_OF_SPLIT    |
      | LAST      | 0       | 8       | ENTIRE            |


  Scenario: Meta-Data for joining in first and last name
    Given HMRC has the following individual records
      | First name      | Last name           | Date of Birth | nino        |
      | Bob-Brian       | Hill O'Coates-Smith | 9999-12-31    | NR123456C   |
    When an income request is made with the following identity
      | First name    | Bob-Brian           |
      | Last name     | Hill O'Coates-Smith |
      | Date of Birth | 9999-12-31    |
      | nino          | NR123456C     |
    Then meta-data was logged following a successful match
    And the meta-data contains the following input name information
      | nameType  | index | diacritics  | umlauts | abbreviation  | nameSplitter  | uniCodeBlocks | length  |
      | FIRST     | 0     | false       | false   | false         | true          | BASIC_LATIN   | 9       |
      | LAST      | 0     | false       | false   | false         | false         | BASIC_LATIN   | 4       |
      | LAST      | 1     | false       | false   | false         | true          | BASIC_LATIN   | 14      |
    And the meta-data indicates that the following generators were used
      | ENTIRE_NON_ALIAS_NAME                    |
    And the meta-data contains the following name derivation information
      | section   | index   | length  | derivationActions |
      | FIRST     | 0       | 9       | ENTIRE            |
      | LAST      | 0 1     | 19      | ENTIRE            |
