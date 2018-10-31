package uk.gov.digital.ho.pttg.api;

import static java.util.Arrays.copyOf;

class RingBuffer {

    private long[] buffer;
    private int newest;
    private int oldest;

    RingBuffer(long[] initialBuffer, int oldest, int newest) {
        this.oldest = oldest;
        this.newest = newest;
        this.buffer = copyOf(initialBuffer, initialBuffer.length);
    }

    long oldest() {
        return buffer[oldest];
    }

    long newest() {
        return buffer[newest];
    }

    void store(long value) {
        newest = oldest;
        buffer[newest] = value;
        oldest = oldest - 1;
        if (oldest < 0) {
            oldest = buffer.length - 1;
        }
    }
}
