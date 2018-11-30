@jira=EE-8289

Feature: Analysis logging

  Scenario: Meta-Data for plain name
    Given HMRC has the following individual records
      | First name | Last name | Date of Birth | nino      |
      | Aaa        | Bbb       | 1987-12-10    | SE123456B |
    When an income request is made with the following identity
      | First name    | Aaa         |
      | Last name     | Bbb         |
      | Alias Surname | Ccc         |
      | Date of Birth | 1987-12-10  |
      | nino          | SE123456B   |
    Then meta-data was logged following a successful match
    And the meta-data contains the following input name information
      | nameType  | index | diacritics  | umlauts | abbreviation  | nameSplitter  | uniCodeBlocks | length  |
      | FIRST     | 0     | false       | false   | false         | false         | BASIC_LATIN   | 3       |
      | LAST      | 0     | false       | false   | false         | false         | BASIC_LATIN   | 3       |
      | ALIAS     | 0     | false       | false   | false         | false         | BASIC_LATIN   | 3       |

  Scenario: Meta-Data for name with Diacritics
    Given HMRC has the following individual records
      | First name | Last name | Date of Birth | nino      |
      | Aaa        | Beb       | 1987-12-10    | SE123456B |
    When an income request is made with the following identity
      | First name    | Aâa         |
      | Last name     | Bèb         |
      | Alias Surname | Cóc         |
      | Date of Birth | 1987-12-10  |
      | nino          | SE123456B   |
    Then meta-data was logged following a successful match
    And the meta-data contains the following input name information
      | nameType  | index | diacritics  | umlauts | abbreviation  | nameSplitter  | uniCodeBlocks                     | length  |
      | FIRST     | 0     | true        | false   | false         | false         | BASIC_LATIN LATIN_1_SUPPLEMENT    | 3       |
      | LAST      | 0     | true        | false   | false         | false         | BASIC_LATIN LATIN_1_SUPPLEMENT    | 3       |
      | ALIAS     | 0     | true        | false   | false         | false         | BASIC_LATIN LATIN_1_SUPPLEMENT    | 3       |

  Scenario: Meta-Data for name with Umlauts
    Given HMRC has the following individual records
      | First name | Last name | Date of Birth | nino      |
      | Aaa        | Beb       | 1987-12-10    | SE123456B |
    When an income request is made with the following identity
      | First name    | Aäa         |
      | Last name     | Bëb         |
      | Alias Surname | Cöc         |
      | Date of Birth | 1987-12-10  |
      | nino          | SE123456B   |
    Then meta-data was logged following a successful match
    And the meta-data contains the following input name information
      | nameType  | index | diacritics  | umlauts | abbreviation  | nameSplitter  | uniCodeBlocks                     | length  |
      | FIRST     | 0     | true        | true    | false         | false         | BASIC_LATIN LATIN_1_SUPPLEMENT    | 3       |
      | LAST      | 0     | true        | true    | false         | false         | BASIC_LATIN LATIN_1_SUPPLEMENT    | 3       |
      | ALIAS     | 0     | true        | true    | false         | false         | BASIC_LATIN LATIN_1_SUPPLEMENT    | 3       |

  Scenario: Meta-Data for names with abbreviations
    Given HMRC has the following individual records
      | First name | Last name | Date of Birth | nino      |
      | Aaa        | Bbb       | 1987-12-10    | SE123456B |
    When an income request is made with the following identity
      | First name    | Aaa A. obrian        |
      | Last name     | Bbb B. brown         |
      | Alias Surname | Ccc C. clown         |
      | Date of Birth | 1987-12-10          |
      | nino          | SE123456B           |
    Then meta-data was logged following a successful match
    And the meta-data contains the following input name information
      | nameType  | index | diacritics  | umlauts | abbreviation  | nameSplitter  | uniCodeBlocks | length  |
      | FIRST     | 0     | false       | false   | false         | false         | BASIC_LATIN   | 3       |
      | FIRST     | 1     | false       | false   | true          | true          | BASIC_LATIN   | 2       |
      | FIRST     | 2     | false       | false   | false         | false         | BASIC_LATIN   | 6       |
      | LAST      | 0     | false       | false   | false         | false         | BASIC_LATIN   | 3       |
      | LAST      | 1     | false       | false   | true          | true          | BASIC_LATIN   | 2       |
      | LAST      | 2     | false       | false   | false         | false         | BASIC_LATIN   | 5       |
      | ALIAS     | 0     | false       | false   | false         | false         | BASIC_LATIN   | 3       |
      | ALIAS     | 1     | false       | false   | true          | true          | BASIC_LATIN   | 2       |
      | ALIAS     | 2     | false       | false   | false         | false         | BASIC_LATIN   | 5       |

  Scenario: Meta-Data for names with splitters
    Given HMRC has the following individual records
      | First name | Last name | Date of Birth | nino      |
      | Aaa        | Bbb       | 1987-12-10    | SE123456B |
    When an income request is made with the following identity
      | First name    | Aaa A'obrian        |
      | Last name     | Bbb B'br-own        |
      | Alias Surname | Ccc C-clown         |
      | Date of Birth | 1987-12-10          |
      | nino          | SE123456B           |
    Then meta-data was logged following a successful match
    And the meta-data contains the following input name information
      | nameType  | index | diacritics  | umlauts | abbreviation  | nameSplitter  | uniCodeBlocks | length  |
      | FIRST     | 0     | false       | false   | false         | false         | BASIC_LATIN   | 3       |
      | FIRST     | 1     | false       | false   | false         | true          | BASIC_LATIN   | 8       |
      | LAST      | 0     | false       | false   | false         | false         | BASIC_LATIN   | 3       |
      | LAST      | 1     | false       | false   | false         | true          | BASIC_LATIN   | 8       |
      | ALIAS     | 0     | false       | false   | false         | false         | BASIC_LATIN   | 3       |
      | ALIAS     | 1     | false       | false   | false         | true          | BASIC_LATIN   | 7       |
