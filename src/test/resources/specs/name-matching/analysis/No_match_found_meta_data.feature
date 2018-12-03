@jira=EE-8289

Feature: Analysis logging

  Scenario: Meta-Data for no matches found
    Given HMRC has the following individual records
      | First name      | Last name       | Date of Birth | nino      |
      | Aaa             | Bb-Ccc          | 1987-12-10    | NR123456C |
    When an income request is made with the following identity
      | First name    | Aaa         |
      | Last name     | Bb-Ccc      |
      | Date of Birth | 1987-12-10  |
      | nino          | SE123456B |
    Then the unsuccessful match meta-data contains the following input name information
      | nameType  | index | diacritics  | umlauts | abbreviation  | nameSplitter  | uniCodeBlocks | length  |
      | FIRST     | 0     | false       | false   | false         | false         | BASIC_LATIN   | 3       |
      | LAST      | 0     | false       | false   | false         | true          | BASIC_LATIN   | 6       |
