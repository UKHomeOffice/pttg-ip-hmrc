package uk.gov.digital.ho.pttg.dto;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;

@Getter
@AllArgsConstructor
@EqualsAndHashCode
public class Employment {
    private final String payFrequency;
    private final String startDate;
    private final String endDate;
    private final Employer employer;
}
