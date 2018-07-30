package uk.gov.digital.ho.pttg.application;

public enum LogEvent {
    HMRC_SERVICE_STARTED,
    HMRC_PROXY_ENABLED,
    HMRC_SERVICE_REQUEST_RECEIVED,
    HMRC_SERVICE_RESPONSE_SUCCESS,
    HMRC_SERVICE_RESPONSE_ERROR,
    HMRC_PROXY_ERROR,
    HMRC_AUTHENTICATION_ERROR,
    HMRC_SERVICE_RESPONSE_NOT_FOUND;

    public static final String EVENT = "event_id";
}
