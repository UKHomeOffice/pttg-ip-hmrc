@jira=EE-8289

Feature: Analysis logging

  Scenario: Meta-Data for hyphenated name
    Given HMRC has the following individual records
      | First name      | Last name       | Date of Birth | nino      |
      | Aaa             | Bb-Ccc          | 1987-12-10    | SE123456B |
    When an income request is made with the following identity
      | First name    | Aaa         |
      | Last name     | Bb-Ccc      |
      | Date of Birth | 1987-12-10  |
      | nino          | SE 123456 B |
    Then meta-data was logged following a successful match
    And the meta-data contains the following input name information
      | nameType  | index | diacritics  | umlauts | abbreviation  | nameSplitter  | uniCodeBlocks | length  |
      | FIRST     | 0     | false       | false   | false         | false         | BASIC_LATIN   | 3       |
      | LAST      | 0     | false       | false   | false         | true          | BASIC_LATIN   | 6       |
    And the meta-data indicates that the following generators were used
      | ENTIRE_NON_ALIAS_NAME    |
    And the meta-data contains the following name derivation information
      | section   | index   | length  | derivationActions |
      | FIRST     | 0       | 3       | ENTIRE            |
      | LAST      | 0       | 6       | ENTIRE            |


  Scenario: Meta-Data for hyphenated name and surname
    Given HMRC has the following individual records
      | First name      | Last name       | Date of Birth | nino      |
      | Aa-Bbb          | Cc-Ddd          | 1987-12-10    | SE123456B |
    When an income request is made with the following identity
      | First name    | Cc-Ddd      |
      | Last name     | Aa-Bbb      |
      | Date of Birth | 1987-12-10  |
      | nino          | SE 123456 B |
    Then meta-data was logged following a successful match
    And the meta-data contains the following input name information
      | nameType  | index | diacritics  | umlauts | abbreviation  | nameSplitter  | uniCodeBlocks | length  |
      | FIRST     | 0     | false       | false   | false         | true          | BASIC_LATIN   | 6       |
      | LAST      | 0     | false       | false   | false         | true          | BASIC_LATIN   | 6       |
    And the meta-data indicates that the following generators were used
      | NAME_COMBINATIONS    |
    And the meta-data contains the following name derivation information
      | section   | index   | length  | derivationActions |
      | LAST      | 0       | 6       | ORIGINAL          |
      | FIRST     | 0       | 6       | ORIGINAL          |


  Scenario: Meta-Data for hyphenated surname
    Given HMRC has the following individual records
      | First name      | Last name       | Date of Birth | nino      |
      | Aaa             | Bb-             | 1987-12-10    | SE123456B |
    When an income request is made with the following identity
      | First name    | Aaa         |
      | Last name     | Bb- Ccc     |
      | Date of Birth | 1987-12-10  |
      | nino          | SE 123456 B |
    Then meta-data was logged following a successful match
    And the meta-data contains the following input name information
      | nameType  | index | diacritics  | umlauts | abbreviation  | nameSplitter  | uniCodeBlocks | length  |
      | FIRST     | 0     | false       | false   | false         | false         | BASIC_LATIN   | 3       |
      | LAST      | 0     | false       | false   | false         | true          | BASIC_LATIN   | 3       |
      | LAST      | 1     | false       | false   | false         | false         | BASIC_LATIN   | 3       |
    And the meta-data indicates that the following generators were used
      | ENTIRE_NON_ALIAS_NAME    |
    And the meta-data contains the following name derivation information
      | section   | index   | length  | derivationActions |
      | FIRST     | 0       | 3       | ENTIRE            |
      | LAST      | 0 1     | 7       | ENTIRE            |


  Scenario: Meta-Data for Applicant enters hyphen when name does not contain a hyphen
    Given HMRC has the following individual records
      | First name      | Last name       | Date of Birth | nino      |
      | Aaa             | Bb Ccc          | 1987-12-10    | SE123456B |
    When an income request is made with the following identity
      | First name    | Aaa         |
      | Last name     | Bb-Ccc      |
      | Date of Birth | 1987-12-10  |
      | nino          | SE 123456 B |
    Then meta-data was logged following a successful match
    And the meta-data contains the following input name information
      | nameType  | index | diacritics  | umlauts | abbreviation  | nameSplitter  | uniCodeBlocks | length  |
      | FIRST     | 0     | false       | false   | false         | false         | BASIC_LATIN   | 3       |
      | LAST      | 0     | false       | false   | false         | true          | BASIC_LATIN   | 6       |
    And the meta-data indicates that the following generators were used
      | SPLITTERS_REPLACED        |
      | ENTIRE_NON_ALIAS_NAME     |
    And the meta-data contains the following name derivation information
      | section   | index   | length  | derivationActions |
      | FIRST     | 0       | 3       | ENTIRE            |
      | LAST      | 0       | 6       | ENTIRE            |


  Scenario: Meta-Data for Applicant enters hyphen when name does not contain a hyphen 2
    Given HMRC has the following individual records
      | First name      | Last name       | Date of Birth | nino      |
      | Bb              | Aaa             | 1987-12-10    | SE123456B |
    When an income request is made with the following identity
      | First name    | Aaa         |
      | Last name     | Bb-Ccc      |
      | Date of Birth | 1987-12-10  |
      | nino          | SE 123456 B |
    Then meta-data was logged following a successful match
    And the meta-data contains the following input name information
      | nameType  | index | diacritics  | umlauts | abbreviation  | nameSplitter  | uniCodeBlocks | length  |
      | FIRST     | 0     | false       | false   | false         | false         | BASIC_LATIN   | 3       |
      | LAST      | 0     | false       | false   | false         | true          | BASIC_LATIN   | 6       |
    And the meta-data indicates that the following generators were used
      | NAME_COMBINATIONS     |
    And the meta-data contains the following name derivation information
      | section   | index   | length  | derivationActions |
      | LAST      | 0       | 6       | ORIGINAL          |
      | FIRST     | 0       | 3       | ORIGINAL          |


  Scenario: Meta-Data for Applicant with a name containing an apostrophe
    Given HMRC has the following individual records
      | First name      | Last name       | Date of Birth | nino      |
      | Aaa             | Bb'Ccc          | 1987-12-10    | SE123456B |
    When an income request is made with the following identity
      | First name    | Aaa         |
      | Last name     | Bb'Ccc      |
      | Date of Birth | 1987-12-10  |
      | nino          | SE 123456 B |
    Then meta-data was logged following a successful match
    And the meta-data contains the following input name information
      | nameType  | index | diacritics  | umlauts | abbreviation  | nameSplitter  | uniCodeBlocks | length  |
      | FIRST     | 0     | false       | false   | false         | false         | BASIC_LATIN   | 3       |
      | LAST      | 0     | false       | false   | false         | true          | BASIC_LATIN   | 6       |
    And the meta-data indicates that the following generators were used
      | ENTIRE_NON_ALIAS_NAME     |
    And the meta-data contains the following name derivation information
      | section   | index   | length  | derivationActions |
      | FIRST     | 0       | 3       | ENTIRE            |
      | LAST      | 0       | 6       | ENTIRE            |


  Scenario: Meta-Data for Applicant enters apostrophe when name does not contain an apostrophe
    Given HMRC has the following individual records
      | First name      | Last name       | Date of Birth | nino      |
      | Aaa             | Bb Ccc          | 1987-12-10    | SE123456B |
    When an income request is made with the following identity
      | First name    | Aaa         |
      | Last name     | Bb'Ccc      |
      | Date of Birth | 1987-12-10  |
      | nino          | SE 123456 B |
    Then meta-data was logged following a successful match
    And the meta-data contains the following input name information
      | nameType  | index | diacritics  | umlauts | abbreviation  | nameSplitter  | uniCodeBlocks | length  |
      | FIRST     | 0     | false       | false   | false         | false         | BASIC_LATIN   | 3       |
      | LAST      | 0     | false       | false   | false         | true          | BASIC_LATIN   | 6       |
    And the meta-data indicates that the following generators were used
      | SPLITTERS_REPLACED        |
      | ENTIRE_NON_ALIAS_NAME     |
    And the meta-data contains the following name derivation information
      | section   | index   | length  | derivationActions |
      | FIRST     | 0       | 3       | ENTIRE            |
      | LAST      | 0       | 6       | ENTIRE            |


  Scenario: Meta-Data for Applicant with multiple surnames containing one letter in the surname
    Given HMRC has the following individual records
      | First name      | Last name       | Date of Birth | nino      |
      | Joseph James    | R De Bloggs     | 1987-12-10    | SE123456B |
    When an income request is made with the following identity
      | First name    | Joseph James |
      | Last name     | R De Bloggs  |
      | Date of Birth | 1987-12-10   |
      | nino          | SE 123456 B  |
    Then meta-data was logged following a successful match
    And the meta-data contains the following input name information
      | nameType  | index | diacritics  | umlauts | abbreviation  | nameSplitter  | uniCodeBlocks | length  |
      | FIRST     | 0     | false       | false   | false         | false         | BASIC_LATIN   | 6       |
      | FIRST     | 1     | false       | false   | false         | false         | BASIC_LATIN   | 5       |
      | LAST      | 0     | false       | false   | false         | false         | BASIC_LATIN   | 1       |
      | LAST      | 1     | false       | false   | false         | false         | BASIC_LATIN   | 2       |
      | LAST      | 2     | false       | false   | false         | false         | BASIC_LATIN   | 6       |
    And the meta-data indicates that the following generators were used
      | ENTIRE_NON_ALIAS_NAME     |
    And the meta-data contains the following name derivation information
      | section   | index   | length  | derivationActions |
      | FIRST     | 0 1     | 12      | ENTIRE            |
      | LAST      | 0 1 2   | 11      | ENTIRE            |


  Scenario: Meta-Data for Applicant with multiple surnames containing two letters in the surname
    Given HMRC has the following individual records
      | First name      | Last name       | Date of Birth | nino      |
      | Pas Alb         | De Fey          | 1987-12-10    | SE123456B |
    When an income request is made with the following identity
      | First name    | Pas Alb     |
      | Last name     | R De Fey    |
      | Date of Birth | 1987-12-10  |
      | nino          | SE 123456 B |
    Then meta-data was logged following a successful match
    And the meta-data contains the following input name information
      | nameType  | index | diacritics  | umlauts | abbreviation  | nameSplitter  | uniCodeBlocks | length  |
      | FIRST     | 0     | false       | false   | false         | false         | BASIC_LATIN   | 3       |
      | FIRST     | 1     | false       | false   | false         | false         | BASIC_LATIN   | 3       |
      | LAST      | 0     | false       | false   | false         | false         | BASIC_LATIN   | 1       |
      | LAST      | 1     | false       | false   | false         | false         | BASIC_LATIN   | 2       |
      | LAST      | 2     | false       | false   | false         | false         | BASIC_LATIN   | 3       |
    And the meta-data indicates that the following generators were used
      | MULTIPLE_NAMES     |
    And the meta-data contains the following name derivation information
      | section   | index   | length  | derivationActions |
      | FIRST     | 0       | 3       | ORIGINAL          |
      | LAST      | 1 2     | 5       | COMBINATION       |

