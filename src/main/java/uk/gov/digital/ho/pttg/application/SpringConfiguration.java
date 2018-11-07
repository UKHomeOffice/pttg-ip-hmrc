package uk.gov.digital.ho.pttg.application;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.LaxRedirectStrategy;
import org.apache.http.ssl.SSLContexts;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.hateoas.MediaTypes;
import org.springframework.hateoas.hal.Jackson2HalModule;
import org.springframework.http.MediaType;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.retry.annotation.EnableRetry;
import org.springframework.retry.support.RetryTemplate;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import uk.gov.digital.ho.pttg.api.RequestHeaderData;
import uk.gov.digital.ho.pttg.application.retry.RetryProperties;
import uk.gov.digital.ho.pttg.application.retry.RetryTemplateBuilder;
import uk.gov.digital.ho.pttg.application.util.namenormalizer.*;

import java.text.SimpleDateFormat;
import java.time.Clock;
import java.time.ZoneId;
import java.util.Arrays;
import java.util.List;

import static org.springframework.http.MediaType.APPLICATION_JSON;

@Configuration
@EnableRetry
public class SpringConfiguration implements WebMvcConfigurer {

    private final boolean useProxy;
    private final String hmrcBaseUrl;
    private final String proxyHost;
    private final Integer proxyPort;

    private final TimeoutProperties timeoutProperties;
    private final RetryProperties retryProperties;

    private final int hmrcNameMaxLength;
    private final String[] supportedSslProtocols;

    public SpringConfiguration(ObjectMapper objectMapper,
                               @Value("${proxy.enabled:false}") boolean useProxy,
                               @Value("${hmrc.endpoint:}") String hmrcBaseUrl,
                               @Value("${proxy.host:}") String proxyHost,
                               @Value("${proxy.port}") Integer proxyPort,
                               @Value("${hmrc.name.rules.length.max:35}") int hmrcNameMaxLength,
                               @Value("#{'${hmrc.ssl.supportedProtocols}'.split(',')}") List<String> supportedSslProtocols,
                               RetryProperties retryProperties,
                               TimeoutProperties timeoutProperties) {

        this.useProxy = useProxy;
        this.hmrcBaseUrl = hmrcBaseUrl;
        this.proxyHost = proxyHost;
        this.proxyPort = proxyPort;
        this.hmrcNameMaxLength = hmrcNameMaxLength;
        this.timeoutProperties = timeoutProperties;
        this.retryProperties = retryProperties;

        initialiseObjectMapper(objectMapper);
        this.supportedSslProtocols = supportedSslProtocols.toArray(new String[]{});
    }

    public static void initialiseObjectMapper(final ObjectMapper mapper) {
        mapper.setDateFormat(new SimpleDateFormat("yyyy-MM-dd"));
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        mapper.enable(SerializationFeature.INDENT_OUTPUT);
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        mapper.registerModule(new Jackson2HalModule());
        mapper.registerModule(new JavaTimeModule());
    }

    @Bean
    public RestTemplate auditRestTemplate(RestTemplateBuilder restTemplateBuilder, ObjectMapper mapper) {
        RestTemplateBuilder builder = initaliseRestTemplateBuilder(restTemplateBuilder, mapper);

        MappingJackson2HttpMessageConverter converter = initialiseConverter(mapper, APPLICATION_JSON);

        return builder
                .setReadTimeout(timeoutProperties.getAudit().getReadMs())
                .setConnectTimeout(timeoutProperties.getAudit().getConnectMs())
                .additionalMessageConverters(converter)
                .build();
    }

    @Bean
    public RestTemplate hmrcAccessCodeRestTemplate(RestTemplateBuilder restTemplateBuilder, ObjectMapper mapper) {
        RestTemplateBuilder builder = initaliseRestTemplateBuilder(restTemplateBuilder, mapper);

        MappingJackson2HttpMessageConverter converter = initialiseConverter(mapper, APPLICATION_JSON);

        return builder
                .setReadTimeout(timeoutProperties.getHmrcAccessCode().getReadMs())
                .setConnectTimeout(timeoutProperties.getHmrcAccessCode().getConnectMs())
                .additionalMessageConverters(converter)
                .build();
    }

    @Bean
    public RestTemplate hmrcApiRestTemplate(RestTemplateBuilder restTemplateBuilder, ObjectMapper mapper, ClientHttpRequestFactory clientHttpRequestFactory) {
        RestTemplateBuilder builder = initaliseRestTemplateBuilder(restTemplateBuilder, mapper);

        MappingJackson2HttpMessageConverter converter = initialiseConverter(mapper, MediaTypes.HAL_JSON, APPLICATION_JSON);

        return builder
                .setReadTimeout(timeoutProperties.getHmrcApi().getReadMs())
                .setConnectTimeout(timeoutProperties.getHmrcApi().getConnectMs())
                .additionalMessageConverters(converter)
                .requestFactory(() -> clientHttpRequestFactory)
                .build();
    }

    private RestTemplateBuilder initaliseRestTemplateBuilder(RestTemplateBuilder restTemplateBuilder, ObjectMapper mapper) {
        RestTemplateBuilder builder = restTemplateBuilder;

        if (useProxy) {
            builder = builder.additionalCustomizers(createProxyCustomizer());
        }

        return builder;
    }

    private MappingJackson2HttpMessageConverter initialiseConverter(ObjectMapper mapper, MediaType... mediaTypes) {
        MappingJackson2HttpMessageConverter converter = new MappingJackson2HttpMessageConverter();
        converter.setObjectMapper(mapper);
        converter.setSupportedMediaTypes(Arrays.asList(mediaTypes));
        return converter;

    }


    private ProxyCustomizer createProxyCustomizer() {
        return new ProxyCustomizer(hmrcBaseUrl, proxyHost, proxyPort);
    }

    @Bean
    public ClientHttpRequestFactory createClientHttpRequestFactory(HttpClientBuilder builder) {

        HttpComponentsClientHttpRequestFactory factory = new HttpComponentsClientHttpRequestFactory();
        factory.setHttpClient(builder.build());

        return factory;
    }

    @Bean
    public HttpClientBuilder createHttpClientBuilder() {
        /*
         * HttpClient - By default, only GET requests resulting in a redirect are automatically followed
         * need to alter the default redirect strategy for redirect on post
         */

        final SSLConnectionSocketFactory sslSocketFactory = new SSLConnectionSocketFactory(SSLContexts.createDefault(), supportedSslProtocols, null, SSLConnectionSocketFactory.getDefaultHostnameVerifier());
        return HttpClientBuilder.create()
                .setSSLSocketFactory(sslSocketFactory)
                .setRedirectStrategy(new LaxRedirectStrategy())
                .evictExpiredConnections();
    }

    @Bean
    Clock createClock() {
        return Clock.system(ZoneId.of("UTC"));
    }

    @Bean
    public RequestHeaderData createRequestData() {
        return new RequestHeaderData();
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(createRequestData());
    }

    @Bean
    public NameNormalizer nameNormalizer() {
        NameNormalizer[] nameNormalizers = {
                new MaxLengthNameNormalizer(hmrcNameMaxLength),
                new DiacriticNameNormalizer(),
                new InvalidCharacterNameNormalizer(),
                new TrimmingNameNormalizer(),
        };
        return new CompositeNameNormalizer(nameNormalizers);
    }

    @Bean
    public RetryTemplate reauthorisingRetryTemplate() {
        return new RetryTemplateBuilder(retryProperties.getUnauthorizedAttempts())
                .retryHmrcUnauthorisedException()
                .build();
    }

    @Bean
    public RetryTemplate apiFailureRetryTemplate() {
        return new RetryTemplateBuilder(retryProperties.getAttempts())
                .withBackOffPeriod(retryProperties.getDelay())
                .retryHttpServerErrors()
                .build();
    }

}

