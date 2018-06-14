package uk.gov.digital.ho.pttg.application;

public interface ApplicationExceptions {

    class AuditDataException extends RuntimeException {
        public AuditDataException(Throwable cause) {
            super(cause);
        }

        public AuditDataException(String message) {
            super(message);
        }

        public AuditDataException(String message, Throwable cause) {
            super(message, cause);
        }
    }

    class HmrcException extends RuntimeException {

        public HmrcException(String message) {
            super(message);
        }

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
        public InvalidNationalInsuranceNumberException(final String s) {
            super(s);
        }
    }

    class HmrcUnauthorisedException extends RuntimeException {
        public HmrcUnauthorisedException(final String s) {
            super(s);
        }
        public HmrcUnauthorisedException(final String s, final Exception e) {
            super(s, e);
        }

    }

    class TooManyNamesException extends RuntimeException {
        public TooManyNamesException(final String s) {
            super(s);
        }
        public TooManyNamesException(final String s, final Exception e) {
            super(s, e);
        }

    }

    class ProxyUnauthorizedException extends RuntimeException {
        public ProxyUnauthorizedException(String s) {
            super(s);
        }
    }
}
