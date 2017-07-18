package uk.gov.digital.ho.pttg;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.hateoas.MediaTypes;
import org.springframework.hateoas.Resource;
import org.springframework.hateoas.client.Traverson;
import org.springframework.hateoas.hal.Jackson2HalModule;
import org.springframework.http.*;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;
import uk.gov.digital.ho.pttg.dto.*;

import java.net.URI;
import java.net.URISyntaxException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static java.lang.String.format;
import static org.springframework.http.MediaType.APPLICATION_FORM_URLENCODED;
import static org.springframework.http.MediaType.APPLICATION_JSON;

@Service
@Slf4j
public class HmrcClient {

    private static final ParameterizedTypeReference<Resource<String>> linksResourceTypeRef = new ParameterizedTypeReference<Resource<String>>() {
    };
    private static final ParameterizedTypeReference<Resource<EmbeddedIncome>> incomesResourceTypeRef = new ParameterizedTypeReference<Resource<EmbeddedIncome>>() {
    };
    private static final ParameterizedTypeReference<Resource<EmbeddedEmployments>> employmentsResourceTypeRef = new ParameterizedTypeReference<Resource<EmbeddedEmployments>>() {
    };


    private RestTemplate restTemplate;
    private String url;
    private String clientId;
    private final String totpKey;

    static final String CONSUMER_ID_HEADER = "clientId";

    @Autowired
    public HmrcClient(RestTemplate restTemplate, @Value("${hmrc.endpoint}") String url, @Value("${client.id}") String clientId, @Value("${totp.key}") String totpKey) {
        this.restTemplate = restTemplate;
        this.url = url;
        this.clientId = clientId;
        this.totpKey = totpKey;
    }

    public IncomeSummary getIncome(Individual individual, LocalDate fromDate, LocalDate toDate) {

        String accessToken = getAccessCode();
        //entrypoint to retrieve match url
        final String matchUrl = getMatchUrl(accessToken);
        //post individual details and follow redirect
        final Resource<String> individualResource = getIndividual(individual, accessToken, matchUrl);
        final List<Employment> employments = getEmployments(fromDate, toDate, accessToken, individualResource);
        final List<Income> incomeList = getIncome(fromDate, toDate, accessToken, individualResource);


        return new IncomeSummary(incomeList, employments);
    }

    private List<Income> getIncome(LocalDate fromDate, LocalDate toDate, String accessToken, Resource<String> linksResource) {

        final Resource<EmbeddedIncome> incomeResource = followTraverson(buildLinkWithDateRangeQueryParams(fromDate, toDate, linksResource.getLink("income").getHref()), accessToken)
                .toObject(incomesResourceTypeRef);
        return incomeResource.getContent().get_embedded().getIncome();
    }

    private String buildLinkWithDateRangeQueryParams(LocalDate fromDate, LocalDate toDate, String href) {
        String uri = null;
        final UriComponentsBuilder uriComponentsBuilder = UriComponentsBuilder.fromHttpUrl(href);
        final UriComponentsBuilder withFromDate = uriComponentsBuilder.queryParam("fromDate", fromDate.format(DateTimeFormatter.ISO_DATE));
        if(toDate!=null) {
            uri =  withFromDate.queryParam("toDate", toDate.format(DateTimeFormatter.ISO_DATE)).build().toUriString();
        }else{
            withFromDate.build().toUriString();
        }
        log.info("hmrc link with date range - {}", uri);
        return uri;
    }

    private List<Employment> getEmployments(LocalDate fromDate, LocalDate toDate, String accessToken, Resource<String> linksResource) {
        final Resource<EmbeddedEmployments> employmentsResource = followTraverson(buildLinkWithDateRangeQueryParams(fromDate, toDate, linksResource.getLink("employments").getHref()), accessToken)
                .toObject(employmentsResourceTypeRef);
        return employmentsResource.getContent().get_embedded().getEmployments();
    }

    private Resource<String> getIndividual(Individual individual, String accessToken, String matchUrl) {
        ResponseEntity<String> entity = restTemplate.postForEntity(matchUrl, createEntity(individual, accessToken), String.class);
        String location = entity.getHeaders().get("Location").get(0);
        return followTraverson(location, accessToken).toObject(linksResourceTypeRef);
    }

    private String getMatchUrl(String accessToken) {
        final Resource<String> linksResource = followTraverson(url + "/individuals/", accessToken).toObject(linksResourceTypeRef);
        return linksResource.getLink("match").getHref();
    }

    private void handleError(String balanceUrl, HttpStatusCodeException e) {
        if (isNotFound(e)) {
            throw new HmrcNotFoundException(String.format("Not Found returned for url %s", balanceUrl));
        }
        Optional.of(e.getResponseBodyAsString()).ifPresent(log::error);
        throw new HmrcException(String.format("Error returned from %s", balanceUrl), e);
    }

    private static boolean isNotFound(HttpStatusCodeException e) {
        return e.getStatusCode() != null && e.getStatusCode() == HttpStatus.NOT_FOUND;
    }

    private HttpEntity<Individual> createEntity(Individual individual, String accessToken) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.add("Authorization", format("Bearer %s", accessToken));
        return new HttpEntity<Individual>(individual, headers);
    }

    private static HttpHeaders generateHeaders(String accessToken) {
        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE);
        headers.add(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE);
        headers.add("Authorization", format("Bearer %s", accessToken));
        return headers;
    }

    private static Traverson traversonFor(String link) {
        try {
            return new Traverson(new URI(link), APPLICATION_JSON).setRestOperations(new RestTemplate(Collections.singletonList(getHalConverter())));
        } catch (URISyntaxException e) {
            throw new HmrcException("Problem building hmrc API url", e);
        }
    }

    private static Traverson.TraversalBuilder followTraverson(String link, String accessToken) {
        return traversonFor(link).follow().withHeaders(generateHeaders(accessToken));
    }

    private static HttpMessageConverter<?> getHalConverter() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new Jackson2HalModule());
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

        MappingJackson2HttpMessageConverter converter = new MappingJackson2HttpMessageConverter();

        converter.setObjectMapper(mapper);
        converter.setSupportedMediaTypes(Arrays.asList(MediaTypes.HAL_JSON, APPLICATION_JSON));

        return converter;
    }

    private String getAccessCode() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(APPLICATION_FORM_URLENCODED);

        MultiValueMap<String, String> map = new LinkedMultiValueMap<String, String>();
        map.add("grant_type", "client_credentials");
        map.add("client_id", clientId);
        final String totpCode = getTotpCode();
        map.add("client_secret", totpCode);

        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(map, headers);

        log.info("Calling oauth/token with totp: {}", totpCode);
        final AuthToken oauth = restTemplate.postForEntity(url + "/oauth/token", request, AuthToken.class).getBody();
        log.info("Received AuthToken response {}", oauth);
        return oauth.getAccess_token();
    }

    private String getTotpCode() {
        try {
            return TotpGenerator.getTotpCode(totpKey);
        } catch (NoSuchAlgorithmException | InvalidKeyException e) {
            log.error("Problem generating TOTP code", e);
            throw new HmrcException("Problem generating TOTP code", e);
        }
    }

}
