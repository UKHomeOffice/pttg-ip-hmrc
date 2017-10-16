package uk.gov.digital.ho.pttg.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
@ToString
public class AuthToken {

    @JsonProperty(value = "code", required = true)
    private final String code;

    @JsonProperty(value = "expiry")
    private final LocalDateTime expiry;
}
