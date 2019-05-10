package uk.gov.digital.ho.pttg.api;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.io.IOUtils;
import org.jetbrains.annotations.NotNull;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.*;
import org.springframework.test.web.client.ExpectedCount;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestTemplate;
import uk.gov.digital.ho.pttg.dto.AccessCode;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.http.HttpMethod.POST;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.client.MockRestServiceServer.MockRestServiceServerBuilder;
import static org.springframework.test.web.client.MockRestServiceServer.bindTo;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;
import static uk.gov.digital.ho.pttg.api.RequestHeaderData.MAX_DURATION_MS_HEADER;

class HmrcResourceTimeoutIntegrationTestUtils {

    static MockRestServiceServer buildMockService(RestTemplate restTemplate) {
        MockRestServiceServerBuilder serverBuilder = bindTo(restTemplate);
        serverBuilder.ignoreExpectOrder(true);
        return serverBuilder.build();
    }

    static void resourceRequest(MockRestServiceServer hmrcApiMockService, String resource, String matchId, HttpMethod httpMethod, int delayInMillis, String jsonFile, HttpStatus httpStatus) {
        resourceRequest(
                hmrcApiMockService,
                ExpectedCount.max(1),
                resource,
                matchId,
                httpMethod,
                delayInMillis,
                jsonFile,
                httpStatus
                );
    }

    static void resourceRequest(MockRestServiceServer hmrcApiMockService, ExpectedCount expectedCount, String resource, String matchId, HttpMethod httpMethod, int delayInMillis, String jsonFile, HttpStatus httpStatus) {

        hmrcApiMockService
                .expect(expectedCount, requestTo(containsString(resource)))
                .andExpect(method(httpMethod))
                .andRespond(request -> {
                    try {
                        Thread.sleep(delayInMillis);
                    } catch (InterruptedException ignored) {
                        // expecting the exception
                    }

                    String response;

                    if (httpStatus.is2xxSuccessful()) {
                        response = loadJsonFile(jsonFile)
                                           .replace("${matchId}", matchId);
                    } else {
                        response = httpStatus.getReasonPhrase();
                    }

                    return withStatus(httpStatus)
                            .body(response)
                            .contentType(APPLICATION_JSON)
                            .createResponse(request);
                });
    }

    private static String loadJsonFile(String filename) throws IOException {
        return IOUtils.toString(HmrcResourceTimeoutIntegrationTestUtils.class.getResourceAsStream(String.format("/template/%s.json", filename)), StandardCharsets.UTF_8);
    }

    static String buildOauthResponse(ObjectMapper mapper, String accessId) throws JsonProcessingException {
        return mapper.writeValueAsString(new AccessCode(accessId, LocalDateTime.MAX, LocalDateTime.MAX));
    }

    static ResponseEntity<String> performHmrcRequest(TestRestTemplate testRestTemplate, int maxDuration) {
        IncomeDataRequest request = new IncomeDataRequest("Laurie", "Halford", "GH576240A",
                LocalDate.of(1992, 3, 1),
                LocalDate.of(2017, 1, 1),
                LocalDate.of(2017, 6, 1),
                "");

        HttpHeaders headers = new HttpHeaders();
        headers.set(MAX_DURATION_MS_HEADER, Integer.toString(maxDuration));

        HttpEntity<IncomeDataRequest> requestEntity = new HttpEntity<>(request, headers);

        return testRestTemplate.exchange("/income", POST, requestEntity, String.class);
    }

    @NotNull
    static String responseMessage(HttpStatus httpStatus) {
        return httpStatus + " " + httpStatus.getReasonPhrase();
    }
}

