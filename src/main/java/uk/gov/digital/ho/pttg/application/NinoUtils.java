package uk.gov.digital.ho.pttg.application;

import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;
import uk.gov.digital.ho.pttg.application.ApplicationExceptions.InvalidNationalInsuranceNumberException;

import java.util.regex.Pattern;

import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;

@Component
public class NinoUtils {
    private static final Pattern NINO_PATTERN = Pattern.compile("(^((?!(BG|GB|KN|NK|NT|TN|ZZ)|([DFIQUV])[A-Z]|[A-Z]([DFIOQUV]))[A-Z]{2})[0-9]{6}[A-D]?$)");

    public String sanitise(final String nino) {
        if (isNull(nino)) {
            return null;
        }
        return StringUtils.deleteWhitespace(nino).toUpperCase();
    }

    public void validate(final String nino) {
        if (isInvalid(nino)) {
            throw new InvalidNationalInsuranceNumberException("Error: Invalid NINO");
        }
    }

    private boolean isInvalid(final String nino) {
        return !isValid(nino);
    }

    private boolean isValid(final String nino) {
        return nonNull(nino) && NINO_PATTERN.matcher(nino).matches();
    }
}
