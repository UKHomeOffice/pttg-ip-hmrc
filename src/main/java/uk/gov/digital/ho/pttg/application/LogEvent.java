package uk.gov.digital.ho.pttg.application;

public enum LogEvent {
    HMRC_SERVICE_CIRCUIT_BREAKER_TRIPPED,
    HMRC_SERVICE_STARTED,
    HMRC_PROXY_ENABLED,
    HMRC_SERVICE_REQUEST_RECEIVED,
    HMRC_SERVICE_RESPONSE_SUCCESS,
    HMRC_SERVICE_RESPONSE_ERROR,
    HMRC_SERVICE_GENERATED_CORRELATION_ID,
    HMRC_PROXY_ERROR,
    HMRC_AUTHENTICATION_ERROR,
    HMRC_SERVICE_RESPONSE_NOT_FOUND,
    HMRC_MATCHING_REQUEST_SENT,
    HMRC_MATCHING_SUCCESS_RECEIVED,
    HMRC_MATCHING_FAILURE_RECEIVED,
    HMRC_MATCHING_ATTEMPT_SKIPPED,
    HMRC_MATCHING_ATTEMPTS,
    HMRC_API_CALL_ATTEMPT,
    HMRC_PAYE_REQUEST_SENT,
    HMRC_PAYE_RESPONSE_RECEIVED,
    HMRC_SA_REQUEST_SENT,
    HMRC_SA_RESPONSE_RECEIVED,
    HMRC_EMPLOYMENTS_REQUEST_SENT,
    HMRC_EMPLOYMENTS_RESPONSE_RECEIVED,
    HMRC_AUDIT_FAILURE;

    public static final String EVENT = "event_id";
}
