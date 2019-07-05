package uk.gov.digital.ho.pttg.application;

public enum LogEvent {
    HMRC_SERVICE_MAX_RESPONSE_TIME,
    HMRC_UPDATE_ACCESS_CODE,
    HMRC_ACCESS_CODE_RECEIVED,
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
    HMRC_MATCHING_UNSUCCESSFUL,
    HMRC_MATCHING_PERFORMANCE_ANALYSIS,
    HMRC_API_CALL_ATTEMPT,
    HMRC_OVER_RATE_LIMIT,
    HMRC_PAYE_REQUEST_SENT,
    HMRC_PAYE_RESPONSE_RECEIVED,
    HMRC_SA_REQUEST_SENT,
    HMRC_SA_RESPONSE_RECEIVED,
    HMRC_EMPLOYMENTS_REQUEST_SENT,
    HMRC_EMPLOYMENTS_RESPONSE_RECEIVED,
    HMRC_AUDIT_FAILURE,
    HMRC_INDIVIDUAL_REQUEST_SENT,
    HMRC_INDIVIDUAL_RESPONSE_RECEIVED,
    HMRC_INCOME_REQUEST_SENT,
    HMRC_INCOME_RESPONSE_RECEIVED,
    HMRC_INSUFFICIENT_TIME_TO_COMPLETE;


    public static final String EVENT = "event_id";
}
