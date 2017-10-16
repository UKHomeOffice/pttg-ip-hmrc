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

    private final static int EXPIRY_MARGIN = 60;

    @JsonProperty(value = "code", required = true)
    private final String code;
    @JsonProperty(value = "expiry", required = true)
    private final LocalDateTime expiry;


}
