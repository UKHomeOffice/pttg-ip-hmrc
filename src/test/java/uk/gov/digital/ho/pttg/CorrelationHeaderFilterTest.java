package uk.gov.digital.ho.pttg;

import org.junit.Test;
import uk.gov.digital.ho.pttg.api.CorrelationHeaderFilter;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

public class CorrelationHeaderFilterTest {
    @Test
    public void shouldGenerateCorrelationIdWhenNotPresent() throws Exception {
        String uuid = CorrelationHeaderFilter.generateCorrelationIdIfNeeded(null);
        assertThat(UUID.fromString(uuid)).isNotNull();
    }

    @Test
    public void shouldPassThroughGenerateCorrelationIdWhenPresent() throws Exception {
        assertThat(CorrelationHeaderFilter.generateCorrelationIdIfNeeded("beans")).isEqualTo("beans");
    }

}