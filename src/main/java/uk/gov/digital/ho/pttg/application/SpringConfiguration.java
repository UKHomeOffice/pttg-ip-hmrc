package uk.gov.digital.ho.pttg.application;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
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
import org.springframework.retry.annotation.EnableRetry;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;
import uk.gov.digital.ho.pttg.api.RequestData;
import uk.gov.digital.ho.pttg.application.util.MaxLengthNameNormalizer;
import uk.gov.digital.ho.pttg.application.util.NameNormalizer;

import java.text.SimpleDateFormat;
import java.time.Clock;
import java.time.ZoneId;
import java.util.Arrays;

import static org.springframework.http.MediaType.APPLICATION_JSON;

@Configuration
@EnableRetry
public class SpringConfiguration extends WebMvcConfigurerAdapter {
    private final boolean useProxy;
    private final String hmrcBaseUrl;
    private final String proxyHost;
    private final Integer proxyPort;

    private final int restTemplateReadTimeoutInMillis;
    private final int restTemplateConnectTimeoutInMillis;

    private final int hmrcNameMaxLength;

    public SpringConfiguration(ObjectMapper objectMapper,
                               @Value("${proxy.enabled:false}") boolean useProxy,
                               @Value("${hmrc.endpoint:}") String hmrcBaseUrl,
                               @Value("${proxy.host:}") String proxyHost,
                               @Value("${proxy.port}") Integer proxyPort,
                               @Value("${resttemplate.timeout.read:30000}") int restTemplateReadTimeoutInMillis,
                               @Value("${resttemplate.timeout.connect:30000}") int restTemplateConnectTimeoutInMillis,
                               @Value("${hmrc.name.rules.length.max:35}") int hmrcNameMaxLength) {

        this.useProxy = useProxy;
        this.hmrcBaseUrl = hmrcBaseUrl;
        this.proxyHost = proxyHost;
        this.proxyPort = proxyPort;
        this.restTemplateReadTimeoutInMillis = restTemplateReadTimeoutInMillis;
        this.restTemplateConnectTimeoutInMillis = restTemplateConnectTimeoutInMillis;
        this.hmrcNameMaxLength = hmrcNameMaxLength;
        initialiseObjectMapper(objectMapper);
    }

    private static void initialiseObjectMapper(final ObjectMapper mapper) {
        mapper.setDateFormat(new SimpleDateFormat("yyyy-MM-dd"));
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        mapper.enable(SerializationFeature.INDENT_OUTPUT);
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        mapper.registerModule(new Jackson2HalModule());
    }

    @Bean
    public RestTemplate createRestTemplate(RestTemplateBuilder restTemplateBuilder, ObjectMapper mapper) {

        if (useProxy) {
            restTemplateBuilder = restTemplateBuilder.additionalCustomizers(createProxyCustomizer());
        }

        MappingJackson2HttpMessageConverter converter = new MappingJackson2HttpMessageConverter();
        converter.setObjectMapper(mapper);
        converter.setSupportedMediaTypes(Arrays.asList(MediaTypes.HAL_JSON, APPLICATION_JSON));

        return restTemplateBuilder
                .requestFactory(createClientHttpRequestFactory())
                .additionalMessageConverters(converter)
                .setReadTimeout(restTemplateReadTimeoutInMillis)
                .setConnectTimeout(restTemplateConnectTimeoutInMillis)
                .build();
    }

    private ProxyCustomizer createProxyCustomizer() {
        return new ProxyCustomizer(hmrcBaseUrl, proxyHost, proxyPort);
    }

    @Bean
    public ClientHttpRequestFactory createClientHttpRequestFactory() {
        HttpComponentsClientHttpRequestFactory factory =
                new HttpComponentsClientHttpRequestFactory();

        /* HttpClient - By default, only GET requests resulting in a redirect are automatically followed
           need to alter the default redirect strategy for redirect on post
         */

        HttpClientBuilder builder = HttpClientBuilder.create().setRedirectStrategy(new LaxRedirectStrategy());

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

    @Bean
    public NameNormalizer nameNormalizer() {
        return new MaxLengthNameNormalizer(hmrcNameMaxLength);
    }
}

