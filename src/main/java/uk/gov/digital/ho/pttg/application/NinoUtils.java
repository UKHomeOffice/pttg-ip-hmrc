package uk.gov.digital.ho.pttg.application;

import org.springframework.stereotype.Component;

@Component
public class NinoUtils {

    public String redactedNino(String nino) {
        if (nino.length() == 9) {
            return nino.substring(0, 4) + "***" + nino.substring(7, 9);
        }

        return nino;
    }

}
