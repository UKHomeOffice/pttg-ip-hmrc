package uk.gov.digital.ho.pttg.api;

import org.junit.Before;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class RingBufferTest {

    private RingBuffer ringBuffer;

    @Before
    public void setup() {
        long[] initialArray = {9, 8, 7, 6, 5, 4, 3, 2, 1, 0};
        ringBuffer = new RingBuffer(initialArray, 9, 0);
    }

    @Test
    public void shouldGiveNewest() {
        assertThat(ringBuffer.newest()).isEqualTo(9);
    }

    @Test
    public void shouldGiveOldest() {
        assertThat(ringBuffer.oldest()).isEqualTo(0);
    }

    @Test
    public void shouldStoreRoundTheBuffer() {
        ringBuffer.store(10);
        assertThat(ringBuffer.newest()).isEqualTo(10);
        assertThat(ringBuffer.oldest()).isEqualTo(1);

        ringBuffer.store(11);
        assertThat(ringBuffer.newest()).isEqualTo(11);
        assertThat(ringBuffer.oldest()).isEqualTo(2);

        ringBuffer.store(12);
        assertThat(ringBuffer.newest()).isEqualTo(12);
        assertThat(ringBuffer.oldest()).isEqualTo(3);

        ringBuffer.store(13);
        assertThat(ringBuffer.newest()).isEqualTo(13);
        assertThat(ringBuffer.oldest()).isEqualTo(4);

        ringBuffer.store(14);
        assertThat(ringBuffer.newest()).isEqualTo(14);
        assertThat(ringBuffer.oldest()).isEqualTo(5);

        ringBuffer.store(15);
        assertThat(ringBuffer.newest()).isEqualTo(15);
        assertThat(ringBuffer.oldest()).isEqualTo(6);

        ringBuffer.store(16);
        assertThat(ringBuffer.newest()).isEqualTo(16);
        assertThat(ringBuffer.oldest()).isEqualTo(7);

        ringBuffer.store(17);
        assertThat(ringBuffer.newest()).isEqualTo(17);
        assertThat(ringBuffer.oldest()).isEqualTo(8);

        ringBuffer.store(18);
        assertThat(ringBuffer.newest()).isEqualTo(18);
        assertThat(ringBuffer.oldest()).isEqualTo(9);

        ringBuffer.store(19);
        assertThat(ringBuffer.newest()).isEqualTo(19);
        assertThat(ringBuffer.oldest()).isEqualTo(10);
    }
}