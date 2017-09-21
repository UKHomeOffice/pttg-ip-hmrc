package uk.gov.digital.ho.pttg.application;

import org.junit.Test;

import java.sql.Date;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

public class LocalDateAttributeConverterTest {

    private LocalDateAttributeConverter converter = new LocalDateAttributeConverter();

    @Test
    public void shouldConvertNullLocalDate() {
        assertThat(converter.convertToDatabaseColumn(null)).isEqualTo(null);
    }

    @Test
    public void shouldConvertLocalDateToDate() {
        LocalDate localDate = LocalDate.of(1920, 12, 27);
        Date date = converter.convertToDatabaseColumn(localDate);

        assertThat(date.toLocalDate()).isEqualTo(localDate);
    }

    @Test
    public void shouldConvertDateToLocalDate() {
        LocalDate now = LocalDate.now();
        Date date = Date.valueOf(now);
        LocalDate localDate = converter.convertToEntityAttribute(date);

        assertThat(localDate).isEqualTo(now);
    }
}
