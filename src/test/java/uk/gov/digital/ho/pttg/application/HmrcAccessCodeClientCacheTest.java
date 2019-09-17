package uk.gov.digital.ho.pttg.application;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.github.tomakehurst.wiremock.WireMockServer;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.hateoas.MediaTypes;
import org.springframework.hateoas.hal.Jackson2HalModule;
import org.springframework.http.HttpStatus;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestTemplate;
import uk.gov.digital.ho.pttg.api.RequestHeaderData;
import uk.gov.digital.ho.pttg.dto.AccessCode;

import java.net.URI;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.*;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.http.MediaType.APPLICATION_JSON;

@RunWith(MockitoJUnitRunner.class)
public class HmrcAccessCodeClientCacheTest {

    /**
     * There is a potential race condition with the cached access code, where one thread retrieves an expired access code
     * at the same time another thread receives a 401 from HMRC.  Both would generate a new access code with the last to
     * do so being valid and the other being stale.  There would now be a race condition to determine which access code
     * is stored in the cache.
     *
     * This test simulates the race condition by making the 1st thread take longer to retrieve a new access code than the
     * 2nd thread, which could mean that the 1st thread's stale access code overwrites the 2nd thread's valid access code.
     */
    @Test
    @SuppressFBWarnings("NP_NULL_ON_SOME_PATH_FROM_RETURN_VALUE")
    public void shouldNotGetAccessCodeWhileLoadingNewAccessCode() throws JsonProcessingException, InterruptedException, ExecutionException {
        WireMockServer accessCodeServer1 = new WireMockServer(8181);
        WireMockServer accessCodeServer2 = new WireMockServer(8282);

        AccessCode expiredAccessCode = new AccessCode("A", LocalDateTime.now().minusHours(1l), LocalDateTime.MAX);
        AccessCode staleAccessCode = new AccessCode("B", LocalDateTime.MAX, LocalDateTime.MAX);
        AccessCode validAccessCode = new AccessCode("C", LocalDateTime.MAX, LocalDateTime.MAX);

        ObjectMapper objectMapper = initialiseObjectMapper();
        HmrcAccessCodeClient client = createClient(expiredAccessCode, objectMapper);

        accessCodeServer1.start();
        accessCodeServer2.start();

        accessCodeServer1.stubFor(get(urlEqualTo("/access"))
                .willReturn(aResponse()
                        .withFixedDelay(2000)
                        .withStatus(HttpStatus.OK.value())
                        .withBody(buildAccesCodeResponse(staleAccessCode, objectMapper))
                        .withHeader("Content-type", "application/json")));

        accessCodeServer2.stubFor(get(urlEqualTo("/access"))
                .willReturn(aResponse()
                        .withStatus(HttpStatus.OK.value())
                        .withBody(buildAccesCodeResponse(validAccessCode, objectMapper))
                        .withHeader("Content-type" ,"application/json")));

        List<Future<String>> futures =
                runConflictingThreads(
                        callGetAccessCode(client, "http://localhost:8181/access" ),
                        callRefresAccessCode(client, "http://localhost:8282/access")
                );

        assertThat(futures.get(0).get()).isEqualTo("finished get access code");
        assertThat(futures.get(1).get()).isEqualTo("finished refresh access code");

        Optional<AccessCode> cachedAccessCode = (Optional<AccessCode>) ReflectionTestUtils.getField(client, "accessCode");
        assertThat(cachedAccessCode.get().getCode()).isEqualTo(validAccessCode.getCode());

    }

    private HmrcAccessCodeClient createClient(AccessCode accessCode, ObjectMapper objectMapper) {
        MappingJackson2HttpMessageConverter converter = new MappingJackson2HttpMessageConverter();
        converter.setObjectMapper(objectMapper);
        converter.setSupportedMediaTypes(Arrays.asList(MediaTypes.HAL_JSON, APPLICATION_JSON));

        RequestHeaderData requestData = new RequestHeaderData();
        RestTemplate restTemplate = new RestTemplateBuilder().additionalMessageConverters(converter).build();
        ReflectionTestUtils.setField(requestData, "hmrcAccessBasicAuth", "some-auth");
        ReflectionTestUtils.setField(requestData, "auditBasicAuth", "some-auth");

        HmrcAccessCodeClient client = new HmrcAccessCodeClient(restTemplate, requestData, "", 3, 1L);
        ReflectionTestUtils.setField(client, "accessCode", Optional.of(accessCode));
        return client;
    }

    private static ObjectMapper initialiseObjectMapper() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.setDateFormat(new SimpleDateFormat("yyyy-MM-dd"));
        mapper.registerModule(new JavaTimeModule());
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        mapper.enable(SerializationFeature.INDENT_OUTPUT);
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        mapper.registerModule(new Jackson2HalModule());
        return mapper;
    }

    private List<Future<String>> runConflictingThreads(Callable<String>... callables) throws InterruptedException {
        ExecutorService executor = Executors.newFixedThreadPool(callables.length);

        List<Future<String>> futures = executor.invokeAll(Arrays.asList(callables));

        awaitTerminationAfterShutdown(executor);

        return futures;
    }

    private void awaitTerminationAfterShutdown(ExecutorService threadPool) {
        threadPool.shutdown();
        try {
            if (!threadPool.awaitTermination(60, TimeUnit.SECONDS)) {
                threadPool.shutdownNow();
            }
        } catch (InterruptedException ex) {
            threadPool.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }

    private Callable<String> callGetAccessCode(HmrcAccessCodeClient client, String accessCodeServiceUrl) {
        return  () -> {
            ReflectionTestUtils.setField(client, "accessUri", new URI(accessCodeServiceUrl));
            client.getAccessCode();
            return "finished get access code";
        };
    }

    private Callable<String> callRefresAccessCode(HmrcAccessCodeClient client, String accessCodeServiceUrl) {
        return () -> {
            try {
                Thread.sleep(500);
                ReflectionTestUtils.setField(client, "accessUri", new URI(accessCodeServiceUrl));
            } catch (Exception e) {
                e.printStackTrace();
                return "error";
            }
            client.loadLatestAccessCode();
            return "finished refresh access code";
        };
    }

    String buildAccesCodeResponse(AccessCode accessCode, ObjectMapper objectMapper) throws JsonProcessingException {
        return objectMapper.writeValueAsString(accessCode);
    }

}
