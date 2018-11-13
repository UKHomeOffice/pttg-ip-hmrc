package uk.gov.digital.ho.pttg.application;

public interface ApplicationExceptions {

    class HmrcException extends RuntimeException {
        public HmrcException(String message, Throwable cause) {
            super(message, cause);
        }
    }

    class HmrcNotFoundException extends RuntimeException {

        public HmrcNotFoundException(String message) {
            super(message);
        }
    }

    class InvalidNationalInsuranceNumberException extends IllegalArgumentException {
        InvalidNationalInsuranceNumberException(String s) {
            super(s);
        }
    }

    class InvalidIdentityException extends IllegalArgumentException {
        public InvalidIdentityException(String s) {
            super(s);
        }
    }

    class HmrcUnauthorisedException extends RuntimeException {
        public HmrcUnauthorisedException(String s) {
            super(s);
        }

        HmrcUnauthorisedException(String s, Exception e) {
            super(s, e);
        }
    }

    class HmrcOverRateLimitException extends RuntimeException {
        public HmrcOverRateLimitException(String s) {
            super(s);
        }
    }

    class ProxyForbiddenException extends RuntimeException {
        public ProxyForbiddenException(String s) {
            super(s);
        }
    }
}
