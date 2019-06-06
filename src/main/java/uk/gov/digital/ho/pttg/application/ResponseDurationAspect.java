package uk.gov.digital.ho.pttg.application;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.stereotype.Component;
import uk.gov.digital.ho.pttg.api.RequestHeaderData;

@Aspect
@AllArgsConstructor
@Component
@Slf4j
public class ResponseDurationAspect {

    private final RequestHeaderData requestHeaderData;

    @Before("@annotation(uk.gov.digital.ho.pttg.application.AbortIfBeyondMaxResponseDuration)")
    public void before(JoinPoint joinPoint) {
        requestHeaderData.abortIfTakingTooLong();
    }

}
