package uk.gov.digital.ho.pttg.application;

import org.junit.Test;

import java.sql.Timestamp;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

public class LocalDateTimeAttributeConverterTest {

    private LocalDateTimeAttributeConverter converter = new LocalDateTimeAttributeConverter();

    @Test
    public void shouldConvertNullLocalDateTime() {
        assertThat(converter.convertToDatabaseColumn(null)).isEqualTo(null);
    }

    @Test
    public void shouldConvertLocalDateTimeToTimestamp() {
        LocalDateTime now = LocalDateTime.now();
        Timestamp timestamp = converter.convertToDatabaseColumn(now);

        assertThat(timestamp.toLocalDateTime()).isEqualTo(now);
    }

    @Test
    public void shouldConvertTimestampToLocalDateTime() {
        long now = System.currentTimeMillis();
        LocalDateTime localDateTime = converter.convertToEntityAttribute(new Timestamp(now));

        assertThat(Timestamp.valueOf(localDateTime).getTime()).isEqualTo(now);
    }
}
