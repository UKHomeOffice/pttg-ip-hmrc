package uk.gov.digital.ho.pttg.application;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.hateoas.MediaTypes;
import org.springframework.hateoas.Resource;
import org.springframework.hateoas.client.Traverson;
import org.springframework.hateoas.hal.Jackson2HalModule;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;
import uk.gov.digital.ho.pttg.dto.*;

import java.net.URI;
import java.net.URISyntaxException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static java.lang.String.format;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static uk.gov.digital.ho.pttg.api.CorrelationHeaderFilter.CORRELATION_ID_HEADER;
import static uk.gov.digital.ho.pttg.api.UserHeaderFilter.USER_ID_HEADER;

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
    private final String accessCodeurl;

    @Autowired
    public HmrcClient(RestTemplate restTemplate, @Value("${hmrc.endpoint}") String url, @Value("${base.hmrc.access.code.url}") String accessCodeurl) {
        this.restTemplate = restTemplate;
        this.url = url;
        this.accessCodeurl = accessCodeurl;
    }

    public IncomeSummary getIncome(Individual individual, LocalDate fromDate, LocalDate toDate) {

        String accessToken = getAccessCode();
        //entrypoint to retrieve match url
        final String matchUrl = getMatchUrl(accessToken);
        final Resource<String> individualResource = getIndividual(individual, accessToken, matchUrl);
        final List<Employment> employments = getEmployments(fromDate, toDate, accessToken, individualResource);
        final List<Income> incomeList = DataCleanser.clean(individual, getIncome(fromDate, toDate, accessToken, individualResource));

        return new IncomeSummary(incomeList, employments);
    }

    private List<Income> getIncome(LocalDate fromDate, LocalDate toDate, String accessToken, Resource<String> linksResource) {

        final Resource<EmbeddedIncome> incomeResource =
                followTraverson(buildLinkWithDateRangeQueryParams(fromDate, toDate, asAbsolute(linksResource.getLink("income").getHref())), accessToken)
                .toObject(incomesResourceTypeRef);
        return incomeResource.getContent().get_embedded().getIncome();
    }

    String buildLinkWithDateRangeQueryParams(LocalDate fromDate, LocalDate toDate, String href) {
        String uri;
        final UriComponentsBuilder uriComponentsBuilder = UriComponentsBuilder.fromHttpUrl(stripBraces(href)).replaceQuery(null);
        final UriComponentsBuilder withFromDate = uriComponentsBuilder.queryParam("fromDate", fromDate.format(DateTimeFormatter.ISO_DATE));
        if(toDate!=null) {
            uri =  withFromDate.queryParam("toDate", toDate.format(DateTimeFormatter.ISO_DATE)).build().toUriString();
        }else{
            uri = withFromDate.build().toUriString();
        }
        return uri;
    }

    private String stripBraces(String href) {
        return href.replaceAll("[{}]", "");
    }

    private List<Employment> getEmployments(LocalDate fromDate, LocalDate toDate, String accessToken, Resource<String> linksResource) {
        final Resource<EmbeddedEmployments> employmentsResource =
                followTraverson(buildLinkWithDateRangeQueryParams(fromDate, toDate, asAbsolute(linksResource.getLink("employments").getHref())), accessToken)
                .toObject(employmentsResourceTypeRef);
        return employmentsResource.getContent().get_embedded().getEmployments();
    }

    private Resource<String> getIndividual(Individual individual, String accessToken, String matchUrl) {
        log.info("POST to {}", matchUrl);
        //post includes following 303 redirect
        Resource<String> resource = restTemplate.exchange(URI.create(matchUrl), HttpMethod.POST, createEntity(individual, accessToken), linksResourceTypeRef).getBody();
        log.info("Response is {}", resource);
        return resource;
    }

    private String getMatchUrl(String accessToken) {
        final Resource<String> linksResource = followTraverson(url + "/individuals/", accessToken).toObject(linksResourceTypeRef);
        return asAbsolute(linksResource.getLink("match").getHref());
    }

    private String asAbsolute(String uri){
        if(uri.startsWith("http")) {
            return uri;
        }
        return url + uri;

    }

    private HttpEntity<Individual> createEntity(Individual individual, String accessToken) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.add("Authorization", format("Bearer %s", accessToken));
        return new HttpEntity<>(individual, headers);
    }

    private static HttpHeaders generateHeaders(String accessToken) {
        final HttpHeaders headers = generateHeaders();
        headers.add("Authorization", format("Bearer %s", accessToken));
        return headers;
    }

    private static HttpHeaders generateHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE);
        headers.add(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE);
        headers.add(CORRELATION_ID_HEADER, MDC.get(CORRELATION_ID_HEADER));
        headers.add(USER_ID_HEADER, MDC.get(USER_ID_HEADER));
        return headers;
    }

    private static Traverson traversonFor(String link) {
        try {
            return new Traverson(new URI(link), APPLICATION_JSON).setRestOperations(new RestTemplate(Collections.singletonList(getHalConverter())));
        } catch (URISyntaxException e) {
            throw new ApplicationExceptions.HmrcException("Problem building hmrc API url", e);
        }
    }

    private static Traverson.TraversalBuilder followTraverson(String link, String accessToken) {
        log.info("following traverson for {}", link);
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

        final AuthToken oauth = restTemplate.exchange(accessCodeurl + "/access", HttpMethod.GET, createHeadersEntityWithMDC(), AuthToken.class).getBody();
        log.info("Received AuthToken response");
        return oauth.getCode();
    }

    private HttpEntity createHeadersEntityWithMDC() {
        HttpHeaders headers = new HttpHeaders();
        headers.add(CORRELATION_ID_HEADER, MDC.get(CORRELATION_ID_HEADER));
        headers.add(USER_ID_HEADER, MDC.get(USER_ID_HEADER));
        headers.setContentType(MediaType.APPLICATION_JSON);
        return new HttpEntity<>("headers", headers);
    }

}
