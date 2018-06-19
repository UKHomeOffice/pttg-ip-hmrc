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

    @Before
    public void setUp() {
        when(mockRestTemplateBuilder.requestFactory(any(Supplier.class))).thenReturn(mockRestTemplateBuilder);
        when(mockRestTemplateBuilder.additionalCustomizers(any(ProxyCustomizer.class))).thenReturn(mockRestTemplateBuilder);
        when(mockRestTemplateBuilder.additionalMessageConverters(any(HttpMessageConverter.class))).thenReturn(mockRestTemplateBuilder);
        when(mockRestTemplateBuilder.setReadTimeout(anyInt())).thenReturn(mockRestTemplateBuilder);
        when(mockRestTemplateBuilder.setConnectTimeout(anyInt())).thenReturn(mockRestTemplateBuilder);

        when(mockRestTemplateBuilder.build()).thenReturn(mockRestTemplate);
    }

    @Test
    public void shouldUseCustomizerWhenProxyEnabled() {
        SpringConfiguration config = new SpringConfiguration(new ObjectMapper(),
                true, "", "some-proxy-host", 1234, 0, 0, 35);
        config.createRestTemplate(mockRestTemplateBuilder, new ObjectMapper());
        verify(mockRestTemplateBuilder).additionalCustomizers(any(ProxyCustomizer.class));
    }

    @Test
    public void shouldNotUseCustomizerByWhenProxyDisabled() {
        SpringConfiguration config = new SpringConfiguration(new ObjectMapper(),
                false, null, null, null, 0, 0,35);
        config.createRestTemplate(mockRestTemplateBuilder, new ObjectMapper());
        verify(mockRestTemplateBuilder, never()).additionalCustomizers(any(ProxyCustomizer.class));
    }

    @Test
    public void shouldSetTimeoutsOnRestTemplate() {
        // given
        int readTimeout = 1234;
        int connectTimeout = 4321;
        SpringConfiguration springConfig = new SpringConfiguration(new ObjectMapper(), false, null, null, null, readTimeout, connectTimeout, 35);

        // when
        RestTemplate restTemplate = springConfig.createRestTemplate(mockRestTemplateBuilder, new ObjectMapper());

        // then
        verify(mockRestTemplateBuilder).setReadTimeout(readTimeout);
        verify(mockRestTemplateBuilder).setConnectTimeout(connectTimeout);

        assertThat(restTemplate).isEqualTo(mockRestTemplate);
    }
}