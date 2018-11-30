@jira=EE-8289

Feature: Analysis logging

  Scenario: Meta-Data for Name Matching attempts
    Given HMRC has the following individual records
      | First name      | Last name       | Date of Birth | nino      |
      | Higuain         | Gonzalo         | 1987-12-10    | SE123456B |
    When an income request is made with the following identity
      | First name    | Ali Bob Chicharito Danilo Estoban |
      | Last name     | Figuero Gonzalo Higuain           |
      | Date of Birth | 1987-12-10                        |
      | nino          | SE123456B                       |
    Then the meta-data contains the following input name information
      | nameType  | index | diacritics  | umlauts | abbreviation  | nameSplitter  | uniCodeBlocks | length  |
      | FIRST     | 0     | false       | false   | false         | false         | BASIC_LATIN   | 3       |
      | FIRST     | 1     | false       | false   | false         | false         | BASIC_LATIN   | 3       |
      | FIRST     | 2     | false       | false   | false         | false         | BASIC_LATIN   | 10      |
      | FIRST     | 3     | false       | false   | false         | false         | BASIC_LATIN   | 6       |
      | FIRST     | 4     | false       | false   | false         | false         | BASIC_LATIN   | 7       |
      | LAST      | 0     | false       | false   | false         | false         | BASIC_LATIN   | 7       |
      | LAST      | 1     | false       | false   | false         | false         | BASIC_LATIN   | 7       |
      | LAST      | 2     | false       | false   | false         | false         | BASIC_LATIN   | 7       |
    And the meta-data indicates that the following generators were used
      | NAME_COMBINATIONS    |
    And the meta-data contains the following name derivation information
      | section   | index   | length  | derivationActions |
      | LAST      | 2       | 7       | ORIGINAL          |
      | LAST      | 1       | 7       | ORIGINAL          |
    And the meta-data indicates the following number of attempts to match
      | 43 |
