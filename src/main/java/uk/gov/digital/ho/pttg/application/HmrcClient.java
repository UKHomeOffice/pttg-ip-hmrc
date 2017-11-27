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
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Recover;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
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
import static uk.gov.digital.ho.pttg.api.RequestData.CORRELATION_ID_HEADER;
import static uk.gov.digital.ho.pttg.api.RequestData.USER_ID_HEADER;

@Service
@Slf4j
public class HmrcClient {

    private static final ParameterizedTypeReference<Resource<String>> linksResourceTypeRef = new ParameterizedTypeReference<Resource<String>>() {
    };
    private static final ParameterizedTypeReference<Resource<EmbeddedIndividual>> individualResourceTypeRef = new ParameterizedTypeReference<Resource<EmbeddedIndividual>>() {
    };
    private static final ParameterizedTypeReference<Resource<PayeIncome>> payeIncomesResourceTypeRef = new ParameterizedTypeReference<Resource<PayeIncome>>() {
    };
    private static final ParameterizedTypeReference<Resource<Employments>> employmentsResourceTypeRef = new ParameterizedTypeReference<Resource<Employments>>() {
    };

    private String url;
    private RestTemplate restTemplate;

    @Autowired
    public HmrcClient(RestTemplate restTemplate,
                      @Value("${hmrc.endpoint}") String url) {
        this.restTemplate = restTemplate;
        this.url = url;
    }

    @Retryable(
            include = { HttpServerErrorException.class },
            exclude = { HttpClientErrorException.class },
            maxAttemptsExpression = "#{${hmrc.retry.attempts}}",
            backoff = @Backoff(delayExpression = "#{${hmrc.retry.delay}}"))
    public IncomeSummary getIncome(String accessToken, Individual individual, LocalDate fromDate, LocalDate toDate) {

        //individual match response with income and employment hrefs
        final Resource<EmbeddedIndividual> individualResource = getIndividualResource(individual, accessToken, getIndividualLink(individual, accessToken, getIndividualMatchUrl()));

        //income response with paye and SA hrefs
        final String incomeLink = asAbsolute(individualResource.getLink("income").getHref());
        final Resource<String> incomeResource = getIncomeResource(individual, accessToken, incomeLink);

        //income paye response
        final List<Income> incomeList = DataCleanser.clean(individual, getIncome(fromDate, toDate, accessToken, incomeResource));

        //income SA response
        //TODO

        //employments response with paye href
        final String employmentLink = asAbsolute(individualResource.getLink("employments").getHref());
        final Resource<String> employmentResource = getEmploymentResource(individual, accessToken, employmentLink);

        //employment paye response
        final List<Employment> employments = getEmployments(fromDate, toDate, accessToken, employmentResource);

        log.info("Received Income data for nino {}", individual.getNino());

        return new IncomeSummary(incomeList, employments, individualResource.getContent().getIndividual());
    }

    @Recover
    IncomeSummary getIncomeRetryFailureRecovery(HttpServerErrorException e) {
        log.error("Failed to retrieve HMRC data after retries", e.getMessage());
        throw(e);
    }

    @Recover
    IncomeSummary getIncomeRetryFailureRecovery(HttpClientErrorException e) {
        log.error("Failed to retrieve HMRC data (no retries attempted)", e.getMessage());
        throw(e);
    }

    private List<Income> getIncome(LocalDate fromDate, LocalDate toDate, String accessToken, Resource<String> linksResource) {

        final Resource<PayeIncome> incomeResource =
                followTraverson(buildLinkWithDateRangeQueryParams(fromDate, toDate, asAbsolute(linksResource.getLink("paye").getHref())), accessToken)
                        .toObject(payeIncomesResourceTypeRef);
        return incomeResource.getContent().getPaye().getIncome();
    }

    private String buildLinkWithDateRangeQueryParams(LocalDate fromDate, LocalDate toDate, String href) {
        String uri;
        final UriComponentsBuilder uriComponentsBuilder = UriComponentsBuilder.fromHttpUrl(stripPlaceholderQueryParams(href));
        final UriComponentsBuilder withFromDate = uriComponentsBuilder.queryParam("fromDate", fromDate.format(DateTimeFormatter.ISO_DATE));
        if (toDate != null) {
            uri = withFromDate.queryParam("toDate", toDate.format(DateTimeFormatter.ISO_DATE)).build().toUriString();
        } else {
            uri = withFromDate.build().toUriString();
        }
        return uri;
    }


    private String stripPlaceholderQueryParams(String href) {
        return href.replace("{&fromDate,toDate}", "").replace("{&toDate,fromDate}", "");
    }

    private List<Employment> getEmployments(LocalDate fromDate, LocalDate toDate, String accessToken, Resource<String> linksResource) {
        final Resource<Employments> employmentsResource =
                followTraverson(buildLinkWithDateRangeQueryParams(fromDate, toDate, asAbsolute(linksResource.getLink("paye").getHref())), accessToken)
                        .toObject(employmentsResourceTypeRef);
        return employmentsResource.getContent().getEmployments();
    }

    private String getIndividualLink(Individual individual, String accessToken, String matchUrl) {
        log.info("POST to {}", matchUrl);
        //post includes following 303 redirect NOT ANYMORE

        Resource<String> resource = restTemplate.exchange(URI.create(matchUrl), HttpMethod.POST, createEntity(individual, accessToken), linksResourceTypeRef).getBody();
        log.info("Individual Response has been received for {}, {}", individual.getNino(), resource);
        return asAbsolute(resource.getLink("individual").getHref());
    }

    private Resource<EmbeddedIndividual> getIndividualResource(Individual individual, String accessToken, String matchUrl) {
        log.info("GET from {}", matchUrl);
        //post includes following 303 redirect NOT ANYMORE

        Resource<EmbeddedIndividual> resource = restTemplate.exchange(URI.create(matchUrl), HttpMethod.GET, createHeadersEntity(accessToken), individualResourceTypeRef).getBody();
        log.info("Individual Response has been received for {}", individual.getNino());
        return resource;
    }

    private Resource<String> getIncomeResource(Individual individual, String accessToken, String incomeLink) {
        log.info("GET from {}", incomeLink);

        Resource<String> resource = restTemplate.exchange(URI.create(incomeLink), HttpMethod.GET, createHeadersEntity(accessToken), linksResourceTypeRef).getBody();
        log.info("Income Response has been received for {}, {}", individual.getNino(), resource);
        return resource;
    }

    private Resource<String> getEmploymentResource(Individual individual, String accessToken, String employmentLink) {
        log.info("GET from {}", employmentLink);

        Resource<String> resource = restTemplate.exchange(URI.create(employmentLink), HttpMethod.GET, createHeadersEntity(accessToken), linksResourceTypeRef).getBody();
        log.info("Employment Response has been received for {}, {}", individual.getNino(), resource);
        return resource;
    }

    private String getIndividualMatchUrl() {

        return url + "/individuals/matching/";
    }

    private String asAbsolute(String uri) {
        if (uri.startsWith("http")) {
            return uri;
        }
        return url + uri;
    }

    private HttpEntity<Individual> createEntity(Individual individual, String accessToken) {
        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.ACCEPT, "application/vnd.hmrc.P1.0+json");
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.add("Authorization", format("Bearer %s", accessToken));
        return new HttpEntity<>(individual, headers);
    }

    private HttpEntity createHeadersEntity(String accessToken) {
        HttpHeaders headers = generateHeaders();
        headers.add(HttpHeaders.ACCEPT, "application/vnd.hmrc.P1.0+json");
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.add("Authorization", format("Bearer %s", accessToken));
        return new HttpEntity(null, headers);
    }

    private static HttpHeaders generateHeaders(String accessToken) {
        final HttpHeaders headers = generateHeaders();
        headers.add("Authorization", format("Bearer %s", accessToken));
        return headers;
    }

    private static HttpHeaders generateHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.ACCEPT, "application/vnd.hmrc.P1.0+json");
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
}
