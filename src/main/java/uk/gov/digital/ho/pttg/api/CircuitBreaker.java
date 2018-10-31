package uk.gov.digital.ho.pttg.api;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import uk.gov.digital.ho.pttg.application.ApplicationExceptions.CircuitBreakerException;

import java.util.concurrent.TimeUnit;

@Component
public class CircuitBreaker {

    private static final long TIME_WINDOW = TimeUnit.MINUTES.toMillis(1);

    private RingBuffer ringBuffer;

    public CircuitBreaker(@Value("${requestsPerMinute:125}")int requestsPerMinute) {
        long[] initialBuffer = createInitialBuffer(requestsPerMinute);
        this.ringBuffer = new RingBuffer(initialBuffer, initialBuffer.length - 1, 0);
    }

    private long[] createInitialBuffer(int size) {

        long[] buffer = new long[size];
        long timestamp = System.currentTimeMillis() - TIME_WINDOW;

        for (int i=0; i < size; ++i) {
            buffer[i] = timestamp - (i * TIME_WINDOW);
        }

        return buffer;
    }

    void check() {
        if (!updated()) {
            throw new CircuitBreakerException("Rate Limit breached");
        }
    }

    private synchronized boolean updated() {

        if (ringBuffer.newest() - ringBuffer.oldest() > TIME_WINDOW) {
            ringBuffer.store(System.currentTimeMillis());
            return true;
        }

        return false;
    }
}
