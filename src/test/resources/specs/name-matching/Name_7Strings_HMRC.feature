Feature: Name matching with 7 name strings

  Scenario: applicant with 7 names is matched on the last combination
    Given HMRC has the following individual records
      | First name | Last name | Date of Birth | nino        |
      | Ggg        | Fff       | 1987-12-10    | SE 123456 B |

    When the applicant submits the following data to the RPS service
      | First name    | Aaa Bbb Ccc Ddd |
      | Last name     | Eee Fff Ggg     |
      | Date of Birth | 1987-12-10      |
      | nino          | SE 123456 B     |

    Then the footprint will try the following combination of names in order
      | First name | Last name | Date of Birth | nino        |
      | Aaa        | Ggg       | 1987-12-10    | SE 123456 B |
      | Bbb        | Ggg       | 1987-12-10    | SE 123456 B |
      | Ccc        | Ggg       | 1987-12-10    | SE 123456 B |
      | Ddd        | Ggg       | 1987-12-10    | SE 123456 B |
      | Eee        | Ggg       | 1987-12-10    | SE 123456 B |
      | Fff        | Ggg       | 1987-12-10    | SE 123456 B |
      | Aaa        | Bbb       | 1987-12-10    | SE 123456 B |
      | Aaa        | Ccc       | 1987-12-10    | SE 123456 B |
      | Aaa        | Ddd       | 1987-12-10    | SE 123456 B |
      | Aaa        | Eee       | 1987-12-10    | SE 123456 B |
      | Aaa        | Fff       | 1987-12-10    | SE 123456 B |
      | Bbb        | Aaa       | 1987-12-10    | SE 123456 B |
      | Bbb        | Ccc       | 1987-12-10    | SE 123456 B |
      | Bbb        | Ddd       | 1987-12-10    | SE 123456 B |
      | Bbb        | Eee       | 1987-12-10    | SE 123456 B |
      | Bbb        | Fff       | 1987-12-10    | SE 123456 B |
      | Ccc        | Aaa       | 1987-12-10    | SE 123456 B |
      | Ccc        | Bbb       | 1987-12-10    | SE 123456 B |
      | Ccc        | Ddd       | 1987-12-10    | SE 123456 B |
      | Ccc        | Eee       | 1987-12-10    | SE 123456 B |
      | Ccc        | Fff       | 1987-12-10    | SE 123456 B |
      | Ddd        | Aaa       | 1987-12-10    | SE 123456 B |
      | Ddd        | Bbb       | 1987-12-10    | SE 123456 B |
      | Ddd        | Ccc       | 1987-12-10    | SE 123456 B |
      | Ddd        | Eee       | 1987-12-10    | SE 123456 B |
      | Ddd        | Fff       | 1987-12-10    | SE 123456 B |
      | Eee        | Aaa       | 1987-12-10    | SE 123456 B |
      | Eee        | Bbb       | 1987-12-10    | SE 123456 B |
      | Eee        | Ccc       | 1987-12-10    | SE 123456 B |
      | Eee        | Ddd       | 1987-12-10    | SE 123456 B |
      | Eee        | Fff       | 1987-12-10    | SE 123456 B |
      | Fff        | Aaa       | 1987-12-10    | SE 123456 B |
      | Fff        | Bbb       | 1987-12-10    | SE 123456 B |
      | Fff        | Ccc       | 1987-12-10    | SE 123456 B |
      | Fff        | Ddd       | 1987-12-10    | SE 123456 B |
      | Fff        | Eee       | 1987-12-10    | SE 123456 B |
      | Ggg        | Aaa       | 1987-12-10    | SE 123456 B |
      | Ggg        | Bbb       | 1987-12-10    | SE 123456 B |
      | Ggg        | Ccc       | 1987-12-10    | SE 123456 B |
      | Ggg        | Ddd       | 1987-12-10    | SE 123456 B |
      | Ggg        | Eee       | 1987-12-10    | SE 123456 B |
      | Ggg        | Fff       | 1987-12-10    | SE 123456 B |
    And a Matched response will be returned from the service


  Scenario: applicant with 7 names is matched after trying 34 name combinations
    Given HMRC has the following individual records
      | First name | Last name | Date of Birth | nino        |
      | Fff        | Ccc       | 1987-12-10    | SE 123456 B |

    When the applicant submits the following data to the RPS service
      | First name    | Aaa Bbb Ccc Ddd |
      | Last name     | Eee Fff Ggg     |
      | Date of Birth | 1987-12-10      |
      | nino          | SE 123456 B     |

    Then a Matched response will be returned from the service
    And HMRC was called 34 times


  Scenario: applicant with 7 names and is not found after trying all possible combinations
    Given HMRC has the following individual records
      | First name | Last name | Date of Birth | nino        |
      | Iii        | Fff       | 1987-12-10    | SE 123456 B |

    When the applicant submits the following data to the RPS service
      | First name    | Aaa Bbb Ccc Ddd |
      | Last name     | Eee Fff Ggg     |
      | Date of Birth | 1987-12-10      |
      | nino          | SE 123456 B     |

    Then a not matched response is returned
    And HMRC was called 42 times


  Scenario: applicant with 7 names is matched after trying 6 name combinations
    Given HMRC has the following individual records
      | First name | Last name | Date of Birth | nino        |
      | Gonzalo    | Higuain   | 1987-12-10    | SE 123456 B |

    When the applicant submits the following data to the RPS service
      | First name    | Ali Bob-Chicharito Danilo Estoban |
      | Last name     | Figuero Gonzalo Higuain           |
      | Date of Birth | 1987-12-10                        |
      | nino          | SE 123456 B                       |

    Then a Matched response will be returned from the service
    And HMRC was called 6 times


  Scenario: applicant with a hyphenated name is matched on the concatenated version of the name
    Given HMRC has the following individual records
      | First name    | Last name | Date of Birth | nino        |
      | BobChicharito | Higuain   | 1987-12-10    | SE 123456 B |

    When the applicant submits the following data to the RPS service
      | First name    | Ali Bob-Chicharito Danilo Estoban |
      | Last name     | Figuero Gonzalo Higuain           |
      | Date of Birth | 1987-12-10                        |
      | nino          | SE 123456 B                       |

    Then a Matched response will be returned from the service
    And HMRC was called 2 times


  Scenario: applicant with more than 7 names only uses the first 4 and the last 3 names for matching
    Given HMRC has the following individual records
      | First name | Last name | Date of Birth | nino        |
      | Estoban    | Higuain   | 1987-12-10    | SE 123456 B |

    When the applicant submits the following data to the RPS service
      | First name    | Ali Bob Chicharito Danilo Estoban |
      | Last name     | Figuero Gonzalo Higuain           |
      | Date of Birth | 1987-12-10                        |
      | nino          | SE 123456 B                       |

    Then a not matched response is returned
    And HMRC was called 42 times
