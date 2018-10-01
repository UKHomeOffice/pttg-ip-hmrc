package uk.gov.digital.ho.pttg.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
@ToString
public class AccessCode {
    @JsonProperty(value = "code", required = true)
    private final String code;

    @JsonProperty(value = "expiry")
    private final LocalDateTime expiry;

    @JsonProperty(value = "refreshTime")
    @JsonSerialize(using = LocalDateTimeSerializer.class)
    private final LocalDateTime refreshTime;

    public boolean hasExpired() {
        return !LocalDateTime.now().isBefore(expiry);
    }

    public boolean needsRefreshing() {
        return !LocalDateTime.now().isBefore(refreshTime);
    }

}