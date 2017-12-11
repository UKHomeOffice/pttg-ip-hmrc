package uk.gov.digital.ho.pttg.application;

import com.fasterxml.jackson.databind.ObjectMapper;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@SpringBootTest
@SpringBootConfiguration
@RunWith(SpringJUnit4ClassRunner.class)
@SuppressFBWarnings
public class SpringConfigurationTest {

     RestTemplateBuilder builder = Mockito.spy(new RestTemplateBuilder());

    @Test
    public void shouldUseCustomizerWhenProxyEnabled() {

        SpringConfiguration config = new SpringConfiguration(new ObjectMapper(),
                true,"","", 1234);
        config.createRestTemplate(builder, new ObjectMapper());
        verify(builder).additionalCustomizers(any(ProxyCustomizer.class));
    }

    @Test
    public void shouldNotUseCustomizerByWhenProxyDisabled() {

        SpringConfiguration config = new SpringConfiguration(new ObjectMapper(),
                false,null,null, null);
        config.createRestTemplate(builder, new ObjectMapper());
        verify(builder, never()).additionalCustomizers(any(ProxyCustomizer.class));
    }
}