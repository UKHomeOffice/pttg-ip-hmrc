package uk.gov.digital.ho.pttg.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class Employment {
    private final String payFrequency;
    private final String startDate;
    private final String endDate;
    private final Employer employer;
}
