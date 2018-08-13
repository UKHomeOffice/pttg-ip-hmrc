package uk.gov.digital.ho.pttg.application;

import com.fasterxml.jackson.databind.ObjectMapper;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.web.client.RestTemplate;

import java.util.function.Supplier;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
@SuppressFBWarnings(value = "RV_RETURN_VALUE_IGNORED_INFERRED")
public class SpringConfigurationTest {

    @Mock
    private RestTemplateBuilder mockRestTemplateBuilder;

    @Mock
    private RestTemplate mockRestTemplate;

    private TimeoutProperties timeoutProperties;

    @Before
    public void setUp() {
        when(mockRestTemplateBuilder.additionalCustomizers(any(ProxyCustomizer.class))).thenReturn(mockRestTemplateBuilder);
        when(mockRestTemplateBuilder.additionalMessageConverters(any(HttpMessageConverter.class))).thenReturn(mockRestTemplateBuilder);
        when(mockRestTemplateBuilder.setReadTimeout(anyInt())).thenReturn(mockRestTemplateBuilder);
        when(mockRestTemplateBuilder.setConnectTimeout(anyInt())).thenReturn(mockRestTemplateBuilder);

        when(mockRestTemplateBuilder.build()).thenReturn(mockRestTemplate);

        timeoutProperties = new TimeoutProperties();
        timeoutProperties.setAudit(new TimeoutProperties.Audit());
        timeoutProperties.setHmrcAccessCode(new TimeoutProperties.HmrcAccessCode());
        timeoutProperties.setHmrcApi(new TimeoutProperties.HmrcApi());
    }

    @Test
    public void shouldUseCustomizerWhenProxyEnabled() {
        SpringConfiguration config = new SpringConfiguration(new ObjectMapper(),
                true, "", "some-proxy-host", 1234, 35, 1, 1, 1, timeoutProperties);
        config.auditRestTemplate(mockRestTemplateBuilder, new ObjectMapper());
        verify(mockRestTemplateBuilder).additionalCustomizers(any(ProxyCustomizer.class));
    }

    @Test
    public void shouldNotUseCustomizerByWhenProxyDisabled() {
        SpringConfiguration config = new SpringConfiguration(new ObjectMapper(),
                false, null, null, null,35, 1, 1, 1, timeoutProperties);
        config.auditRestTemplate(mockRestTemplateBuilder, new ObjectMapper());
        verify(mockRestTemplateBuilder, never()).additionalCustomizers(any(ProxyCustomizer.class));
    }

    @Test
    public void shouldSetTimeoutsOnAuditRestTemplate() {
        // given
        final int readTimeout = 1234;
        final int connectTimeout = 4321;

        timeoutProperties.getAudit().setReadMs(readTimeout);
        timeoutProperties.getAudit().setConnectMs(connectTimeout);
        SpringConfiguration springConfig = new SpringConfiguration(new ObjectMapper(), false, null, null, null, 35, 1, 1, 1, timeoutProperties);

        // when
        RestTemplate restTemplate = springConfig.auditRestTemplate(mockRestTemplateBuilder, new ObjectMapper());

        // then
        verify(mockRestTemplateBuilder).setReadTimeout(readTimeout);
        verify(mockRestTemplateBuilder).setConnectTimeout(connectTimeout);

        assertThat(restTemplate).isEqualTo(mockRestTemplate);
    }

    @Test
    public void shouldSetTimeoutsOnHmrcAccessCodeRestTemplate() {
        // given
        final int readTimeout = 1234;
        final int connectTimeout = 4321;

        timeoutProperties.getHmrcAccessCode().setReadMs(readTimeout);
        timeoutProperties.getHmrcAccessCode().setConnectMs(connectTimeout);
        SpringConfiguration springConfig = new SpringConfiguration(new ObjectMapper(), false, null, null, null, 35, 1, 1, 1, timeoutProperties);

        // when
        RestTemplate restTemplate = springConfig.hmrcAccessCodeRestTemplate(mockRestTemplateBuilder, new ObjectMapper());

        // then
        verify(mockRestTemplateBuilder).setReadTimeout(readTimeout);
        verify(mockRestTemplateBuilder).setConnectTimeout(connectTimeout);

        assertThat(restTemplate).isEqualTo(mockRestTemplate);
    }

    @Test
    public void shouldSetTimeoutsOnHmrcApiRestTemplate() {
        // given
        final int readTimeout = 1234;
        final int connectTimeout = 4321;
        when(mockRestTemplateBuilder.requestFactory(any(Supplier.class))).thenReturn(mockRestTemplateBuilder);

        timeoutProperties.getHmrcApi().setReadMs(readTimeout);
        timeoutProperties.getHmrcApi().setConnectMs(connectTimeout);
        SpringConfiguration springConfig = new SpringConfiguration(new ObjectMapper(), false, null, null, null, 35, 1, 1, 1, timeoutProperties);

        // when
        RestTemplate restTemplate = springConfig.hmrcApiRestTemplate(mockRestTemplateBuilder, new ObjectMapper());

        // then
        verify(mockRestTemplateBuilder).setReadTimeout(readTimeout);
        verify(mockRestTemplateBuilder).setConnectTimeout(connectTimeout);

        assertThat(restTemplate).isEqualTo(mockRestTemplate);
    }
}