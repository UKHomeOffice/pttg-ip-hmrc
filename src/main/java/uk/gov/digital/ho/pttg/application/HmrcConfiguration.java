package uk.gov.digital.ho.pttg.application;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.google.common.collect.ImmutableMap;
import org.apache.http.client.HttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.LaxRedirectStrategy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.hateoas.MediaTypes;
import org.springframework.hateoas.hal.Jackson2HalModule;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.retry.backoff.FixedBackOffPolicy;
import org.springframework.retry.policy.SimpleRetryPolicy;
import org.springframework.retry.support.RetryTemplate;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;
import uk.gov.digital.ho.pttg.api.RequestData;

import java.text.SimpleDateFormat;
import java.util.Arrays;

import static org.springframework.http.MediaType.APPLICATION_JSON;

@Configuration
public class HmrcConfiguration extends WebMvcConfigurerAdapter {


    @Autowired
    public HmrcConfiguration(ObjectMapper objectMapper) {
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

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(requestData());
    }

    @Bean
    public RequestData requestData() {
        return new RequestData();
    }


    @Bean
    public RestTemplate restTemplate(RestTemplateBuilder builder, ObjectMapper mapper) {

        MappingJackson2HttpMessageConverter converter = new MappingJackson2HttpMessageConverter();
        converter.setObjectMapper(mapper);
        converter.setSupportedMediaTypes(Arrays.asList(MediaTypes.HAL_JSON, APPLICATION_JSON));

        return builder.requestFactory(clientHttpRequestFactory()).additionalMessageConverters(converter).build();
    }

    @Bean
    public ClientHttpRequestFactory clientHttpRequestFactory() {
        HttpComponentsClientHttpRequestFactory factory =
                new HttpComponentsClientHttpRequestFactory();

        /* HttpClient - By default, only GET requests resulting in a redirect are automatically followed
           need to alter the default redirect strategy for redirect on post
         */
        HttpClient httpClient =
                HttpClientBuilder.create().setRedirectStrategy(new LaxRedirectStrategy()).build();
        factory.setHttpClient(httpClient);

        return factory;
    }

    @Bean
    public RetryTemplate retryTemplate(@Value("${retry.attempts}") int maxAttempts,
                                       @Value("${retry.delay}") Long backOffPeriod) {
        final RetryTemplate retryTemplate = new RetryTemplate();
        final FixedBackOffPolicy fixedBackOffPolicy = new FixedBackOffPolicy();
        final SimpleRetryPolicy simpleRetryPolicy = new SimpleRetryPolicy(maxAttempts, ImmutableMap.of(ResourceAccessException.class, true, HttpServerErrorException.class, true));
        fixedBackOffPolicy.setBackOffPeriod(backOffPeriod);
        retryTemplate.setBackOffPolicy(fixedBackOffPolicy);
        retryTemplate.setRetryPolicy(simpleRetryPolicy);
        return retryTemplate;
    }




}

