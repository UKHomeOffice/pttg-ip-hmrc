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

    public SpringConfiguration(ObjectMapper objectMapper,
                                @Value("${proxy.enabled:false}") boolean useProxy,
                                @Value("${hmrc.endpoint:}") String hmrcBaseUrl,
                                @Value("${proxy.host:}") String proxyHost,
                                @Value("${proxy.port}") Integer proxyPort) {

        this.useProxy =useProxy;
        this.hmrcBaseUrl = hmrcBaseUrl;
        this.proxyHost = proxyHost;
        this.proxyPort = proxyPort;
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
    public RestTemplate createRestTemplate(RestTemplateBuilder builder, ObjectMapper mapper) {

        if (useProxy) {
            builder = builder.additionalCustomizers(createProxyCustomiser());
        }

        MappingJackson2HttpMessageConverter converter = new MappingJackson2HttpMessageConverter();
        converter.setObjectMapper(mapper);
        converter.setSupportedMediaTypes(Arrays.asList(MediaTypes.HAL_JSON, APPLICATION_JSON));

        return builder.requestFactory(createClientHttpRequestFactory()).additionalMessageConverters(converter).build();
    }

    private ProxyCustomizer createProxyCustomiser() {
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

}

