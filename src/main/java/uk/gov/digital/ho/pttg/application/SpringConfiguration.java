package uk.gov.digital.ho.pttg.application;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpHost;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.LaxRedirectStrategy;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.hateoas.MediaTypes;
import org.springframework.hateoas.hal.Jackson2HalModule;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;
import uk.gov.digital.ho.pttg.api.RequestData;

import java.text.SimpleDateFormat;
import java.time.Clock;
import java.time.ZoneId;
import java.util.Arrays;

import static org.springframework.http.MediaType.APPLICATION_JSON;

@Slf4j
@Configuration
public class SpringConfiguration extends WebMvcConfigurerAdapter {

    @Value("${https.useProxy}") private boolean useProxy;
    @Value("${https.proxyHost}") private String proxyHost;
    @Value("${https.proxyPort}") private String proxyPort;
    @Value("${https.nonProxyHosts}") private String nonProxyHosts;

    public SpringConfiguration(ObjectMapper objectMapper) {
        initialiseObjectMapper(objectMapper);
    }

    private static ObjectMapper initialiseObjectMapper(final ObjectMapper m) {
        m.setDateFormat(new SimpleDateFormat("yyyy-MM-dd"));
        m.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        m.enable(SerializationFeature.INDENT_OUTPUT);
        m.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        m.registerModule(new Jackson2HalModule());
        return m;
    }

    @Bean
    public RestTemplate createRestTemplate(RestTemplateBuilder builder, ObjectMapper mapper) {

        MappingJackson2HttpMessageConverter converter = new MappingJackson2HttpMessageConverter();
        converter.setObjectMapper(mapper);
        converter.setSupportedMediaTypes(Arrays.asList(MediaTypes.HAL_JSON, APPLICATION_JSON));

        return builder.requestFactory(createClientHttpRequestFactory()).additionalMessageConverters(converter).build();
    }

    @Bean
    public ClientHttpRequestFactory createClientHttpRequestFactory() {
        HttpComponentsClientHttpRequestFactory factory =
                new HttpComponentsClientHttpRequestFactory();

        /* HttpClient - By default, only GET requests resulting in a redirect are automatically followed
           need to alter the default redirect strategy for redirect on post
         */

        HttpClientBuilder builder = HttpClientBuilder.create();
        builder.setRedirectStrategy(new LaxRedirectStrategy());

        if (useProxy) {
            log.info("Using proxy {}:{}", proxyHost, proxyPort);
            builder.setProxy(new HttpHost(proxyHost, Integer.parseInt(proxyPort), "https"));
        }

        factory.setHttpClient(builder.build());

        return factory;
    }

    @Bean
    Clock createClock() {
        return Clock.system(ZoneId.of("UTC"));
    }

    @Bean
    public RequestData createRequestData() {
        return new RequestData();
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(createRequestData());
    }

}

