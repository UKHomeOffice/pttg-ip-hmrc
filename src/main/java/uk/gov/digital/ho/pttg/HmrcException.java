package uk.gov.digital.ho.pttg;

public class HmrcException extends RuntimeException {

    public HmrcException(String message) {
        super(message);
    }

    public HmrcException(String message, Throwable cause) {
        super(message, cause);
    }
}
