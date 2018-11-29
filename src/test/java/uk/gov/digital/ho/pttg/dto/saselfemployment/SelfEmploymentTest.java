package uk.gov.digital.ho.pttg.dto.saselfemployment;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.io.FileUtils;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.util.ResourceUtils;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.charset.Charset;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.digital.ho.pttg.application.SpringConfiguration.initialiseObjectMapper;

@RunWith(SpringRunner.class)
public class SelfEmploymentTest {

    private ObjectMapper objectMapper = new ObjectMapper();

    @Before
    public void setUp() {
        initialiseObjectMapper(objectMapper);
    }

    @Test
    public void shouldDeserialise() throws IOException {
        String selfEmploymentJson = FileUtils.readFileToString(ResourceUtils.getFile(String.format("classpath:%s", "dto/saselfemployment/SelfEmploymentTest.json")), Charset.defaultCharset());

        SelfEmployment selfEmployment = objectMapper.readValue(selfEmploymentJson, SelfEmployment.class);

        assertThat(selfEmployment.getSelfEmploymentProfit()).isEqualTo(new BigDecimal(10));
    }

    @Test
    public void shouldDeserialiseNullProfit() throws IOException {
        String selfEmploymentJson = FileUtils.readFileToString(ResourceUtils.getFile(String.format("classpath:%s", "dto/saselfemployment/SelfEmploymentTestNullProfit.json")), Charset.defaultCharset());

        SelfEmployment selfEmployment = objectMapper.readValue(selfEmploymentJson, SelfEmployment.class);

        assertThat(selfEmployment.getSelfEmploymentProfit()).isEqualTo(new BigDecimal(0));
    }
}
