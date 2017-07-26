package uk.gov.digital.ho.pttg.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;

@Getter
@AllArgsConstructor
@ToString
public class AuthToken {
    private final String code;
}
