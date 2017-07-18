package uk.gov.digital.ho.pttg.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;

@Getter
@AllArgsConstructor
@ToString
public class AuthToken {
    private final String access_token;


    /*public String getAccess_token() {
        return UUID.randomUUID().toString();
    }

    public String getToken_type() {
        return "bearer";
    }

    public String getScope() {
        return "scope1, scope2";
    }

    public int getExpires_in() {
        return 14400;
    }*/

}
