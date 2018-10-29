Feature: Name matching does not attempt matches that are duplicates from HMRC's point of view.

  Scenario: First names starting with same letter does not produce duplicate matching calls to HMRC.
    Given HMRC has the following individual records
      | First name | Last name | Date of Birth | nino      |
      | Bert       | Evans     | 1987-12-10    | SE123456B |
    When an income request is made with the following identity
      | First name    | Alex Andrew |
      | Last name     | Evans       |
      | Date of Birth | 1987-12-10  |
      | nino          | SE 123456 B |
    Then the following identities will be tried in this order
      | First name  | Last name | Date of Birth | nino        |
      | Alex Andrew | Evans     | 1987-12-10    | SE 123456 B |
      | Evans       | Alex      | 1987-12-10    | SE 123456 B |
      | Evans       | Andrew    | 1987-12-10    | SE 123456 B |
      | Alex        | Andrew    | 1987-12-10    | SE 123456 B |
      | Andrew      | Alex      | 1987-12-10    | SE 123456 B |
    And a not matched response is returned

  Scenario: Surnames with same first three letters does not produce duplicate matching calls to HMRC.
    Given HMRC has the following individual records
      | First name | Last name   | Date of Birth | nino      |
      | Bert       | Smith Terry | 1987-12-10    | SE123456B |
    When an income request is made with the following identity
      | First name    | Bert             |
      | Last name     | Roberts Robinson |
      | Date of Birth | 1987-12-10       |
      | nino          | SE 123456 B      |
    Then the following identities will be tried in this order
      | First name | Last name        | Date of Birth | nino        |
      | Bert       | Roberts Robinson | 1987-12-10    | SE 123456 B |
      | Roberts    | Robinson         | 1987-12-10    | SE 123456 B |
      | Robinson   | Bert             | 1987-12-10    | SE 123456 B |