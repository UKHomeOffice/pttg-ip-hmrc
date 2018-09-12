@Sprint=16.2
@epic=EE-3855
@story=Names_With_Special_Characters
@jira=EE-8204

Feature: Accept Hyphens and Apostrophes as part of the name matching


    @name_matching
    Scenario: Applicant with a hyphenated name
        Given HMRC has the following individual records
            | First name | Last name | Date of Birth | nino        |
            | Aaa        | Bb-Ccc    | 1987-12-10    | SE 123456 B |
        When the applicant submits the following data to the RPS service
            | First name    | Aaa         |
            | Last name     | Bb-Ccc      |
            | Date of Birth | 1987-12-10  |
            | nino          | SE 123456 B |
        Then the footprint will try the following combination of names in order
            | First name | Last name | Date of Birth | nino        |
            | Aaa        | Bb-Ccc    | 1987-12-10    | SE 123456 B |
        And a Matched response will be returned from the service
        And HMRC was called 1 times


    @name_matching
    Scenario: Applicant with a hyphenated name and surname
        Given HMRC has the following individual records
            | First name | Last name | Date of Birth | nino        |
            | Aa-Bbb     | Cc-Ddd    | 1987-12-10    | SE 123456 B |
        When the applicant submits the following data to the RPS service
            | First name    | Cc-Ddd      |
            | Last name     | Aa-Bbb      |
            | Date of Birth | 1987-12-10  |
            | nino          | SE 123456 B |
        Then the footprint will try the following combination of names in order
            | First name | Last name | Date of Birth | nino        |
            | Cc-Ddd     | Aa-Bbb    | 1987-12-10    | SE 123456 B |
            | Aa-Bbb     | Cc-Ddd    | 1987-12-10    | SE 123456 B |
        And a Matched response will be returned from the service
        And HMRC was called 2 times


    @name_matching
    Scenario: Applicant with a hyphenated surname is matched when more than the original names are sent via the API
        Given HMRC has the following individual records
            | First name | Last name | Date of Birth | nino        |
            | Aaa        | Bb-       | 1987-12-10    | SE 123456 B |
        When the applicant submits the following data to the RPS service
            | First name    | Aaa         |
            | Last name     | Bb- Ccc     |
            | Date of Birth | 1987-12-10  |
            | nino          | SE 123456 B |
        Then the footprint will try the following combination of names in order
            | First name | Last name | Date of Birth | nino        |
            | Aaa        | Bb- Ccc   | 1987-12-10    | SE 123456 B |
        And a Matched response will be returned from the service
        And HMRC was called 1 times


    @name_matching
    Scenario: Applicant enters hyphen when name does not contain a hyphen
        Given HMRC has the following individual records
            | First name | Last name | Date of Birth | nino        |
            | Aaa        | Bb Ccc    | 1987-12-10    | SE 123456 B |
        When the applicant submits the following data to the RPS service
            | First name    | Aaa         |
            | Last name     | Bb-Ccc      |
            | Date of Birth | 1987-12-10  |
            | nino          | SE 123456 B |
        Then the footprint will try the following combination of names in order
            | First name | Last name | Date of Birth | nino        |
            | Aaa        | Bb-Ccc    | 1987-12-10    | SE 123456 B |
            | Bb-Ccc     | Aaa       | 1987-12-10    | SE 123456 B |
            | Aaa        | Bb Ccc    | 1987-12-10    | SE 123456 B |
        And a Matched response will be returned from the service
        And HMRC was called 3 times


    @name_matching
    Scenario: Applicant enters hyphen when name does not contain a hyphen
        Given HMRC has the following individual records
            | First name | Last name | Date of Birth | nino        |
            | Bb         | Aaa       | 1987-12-10    | SE 123456 B |
        When the applicant submits the following data to the RPS service
            | First name    | Aaa         |
            | Last name     | Bb-Ccc      |
            | Date of Birth | 1987-12-10  |
            | nino          | SE 123456 B |
        Then the footprint will try the following combination of names in order
            | First name | Last name | Date of Birth | nino        |
            | Aaa        | Bb-Ccc    | 1987-12-10    | SE 123456 B |
            | Bb-Ccc     | Aaa       | 1987-12-10    | SE 123456 B |
        And a Matched response will be returned from the service
        And HMRC was called 2 times


    @name_matching
    Scenario: Applicant with a name containing an apostrophe
        Given HMRC has the following individual records
            | First name | Last name | Date of Birth | nino        |
            | Aaa        | Bb'Ccc    | 1987-12-10    | SE 123456 B |
        When the applicant submits the following data to the RPS service
            | First name    | Aaa         |
            | Last name     | Bb'Ccc      |
            | Date of Birth | 1987-12-10  |
            | nino          | SE 123456 B |
        Then the footprint will try the following combination of names in order
            | First name | Last name | Date of Birth | nino        |
            | Aaa        | Bb'Ccc    | 1987-12-10    | SE 123456 B |
        And a Matched response will be returned from the service
        And HMRC was called 1 times


    @name_matching
    Scenario: Applicant enters apostrophe when name does not contain an apostrophe
        Given HMRC has the following individual records
            | First name | Last name | Date of Birth | nino        |
            | Aaa        | Bb Ccc    | 1987-12-10    | SE 123456 B |
        When the applicant submits the following data to the RPS service
            | First name    | Aaa         |
            | Last name     | Bb'Ccc      |
            | Date of Birth | 1987-12-10  |
            | nino          | SE 123456 B |
        Then the footprint will try the following combination of names in order
            | First name | Last name | Date of Birth | nino        |
            | Aaa        | Bb'Ccc    | 1987-12-10    | SE 123456 B |
            | Bb'Ccc     | Aaa       | 1987-12-10    | SE 123456 B |
            | Aaa        | Bb Ccc    | 1987-12-10    | SE 123456 B |
        And a Matched response will be returned from the service
        And HMRC was called 3 times
