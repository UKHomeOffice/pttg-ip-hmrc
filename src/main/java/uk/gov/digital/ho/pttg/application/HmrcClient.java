package uk.gov.digital.ho.pttg.application;

import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.hateoas.Resource;
import org.springframework.hateoas.client.Traverson;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
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
import java.time.MonthDay;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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
    private static final ParameterizedTypeReference<Resource<SelfAssessment>> selfAssessmentResourceTypeRef = new ParameterizedTypeReference<Resource<SelfAssessment>>() {
    };
    private static final MonthDay END_OF_TAX_YEAR = MonthDay.of(4, 5);
    private static final String QUERY_PARAM_TO_DATE = "toDate";
    private static final String QUERY_PARAM_FROM_DATE = "fromDate";
    private static final String QUERY_PARAM_TO_TAX_YEAR = "toTaxYear";
    private static final String QUERY_PARAM_FROM_TAX_YEAR = "fromTaxYear";
    private static final String HMRC_VERSION_ACCEPT_HEADER = "application/vnd.hmrc.P1.0+json";
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
        final List<String> selfAssessment = getSelfAssessment(fromDate, toDate, accessToken, incomeResource);

        //employments response with paye href
        final String employmentLink = asAbsolute(individualResource.getLink("employments").getHref());
        final Resource<String> employmentResource = getEmploymentResource(individual, accessToken, employmentLink);

        //employment paye response
        final List<Employment> employments = getEmployments(fromDate, toDate, accessToken, employmentResource);

        log.info("Received Income data for nino {}", individual.getNino());

        enrichIncomeData(incomeList, employments);

        return new IncomeSummary(incomeList, selfAssessment, employments, individualResource.getContent().getIndividual());


    }

    private void enrichIncomeData(List<Income> incomeList, List<Employment> employments) {

        Map<String, String> m = produceMap(employments);
        addPaymentFrequency(incomeList, m);

    }

    void addPaymentFrequency(List<Income> incomeList, Map<String, String> m) {

        for (Income income : incomeList) {
            income.setPaymentFrequency(m.get(income.getEmployerPayeReference()));
        }

    }

    Map<String, String> produceMap(List<Employment> employments) {
        Map<String, String> paymentFrequency = new HashMap<>();


        for(int i = 0; i < employments.size(); i++) {

            Employment employmentEntry = employments.get(i);

            String payFrequency = employmentEntry.getPayFrequency();
            String payeReference = employmentEntry.getEmployer().getPayeReference();

            paymentFrequency.put(payeReference, payFrequency);

        }

        return paymentFrequency;
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

    private List<String> getSelfAssessment(LocalDate fromDate, LocalDate toDate, String accessToken, Resource<String> linksResource) {

        final Resource<SelfAssessment> incomeResource =
                followTraverson(buildLinkWithTaxYearRangeQueryParams(fromDate, toDate, asAbsolute(linksResource.getLink("selfAssessment").getHref())), accessToken)
                        .toObject(selfAssessmentResourceTypeRef);
        final List<TaxReturn> taxReturns = incomeResource.getContent().getSelfAssessment().getTaxReturns();

        return taxReturns.stream().flatMap(taxReturn ->
            taxReturn.getSubmissions().stream().map(Submission::getReceivedDate)
        ).collect(Collectors.toList());
    }

    private String buildLinkWithDateRangeQueryParams(LocalDate fromDate, LocalDate toDate, String href) {
        String uri;
        final UriComponentsBuilder uriComponentsBuilder = UriComponentsBuilder.fromHttpUrl(stripPlaceholderQueryParams(href));
        final UriComponentsBuilder withFromDate = uriComponentsBuilder.queryParam(QUERY_PARAM_FROM_DATE, fromDate.format(DateTimeFormatter.ISO_DATE));
        if (toDate != null) {
            uri = withFromDate.queryParam(QUERY_PARAM_TO_DATE, toDate.format(DateTimeFormatter.ISO_DATE)).build().toUriString();
        } else {
            uri = withFromDate.build().toUriString();
        }
        return uri;
    }

    private String buildLinkWithTaxYearRangeQueryParams(LocalDate fromDate, LocalDate toDate, String href) {
        String uri;
        final UriComponentsBuilder uriComponentsBuilder = UriComponentsBuilder.fromHttpUrl(stripPlaceholderQueryParams(href));
        final UriComponentsBuilder withFromDate = uriComponentsBuilder.queryParam(QUERY_PARAM_FROM_TAX_YEAR, getTaxYear(fromDate));
        if (toDate != null) {
            uri = withFromDate.queryParam(QUERY_PARAM_TO_TAX_YEAR, getTaxYear(toDate)).build().toUriString();
        } else {
            uri = withFromDate.build().toUriString();
        }
        return uri;
    }

    private String getTaxYear(LocalDate date) {
        String taxYear;
        if (MonthDay.from(date).isAfter(END_OF_TAX_YEAR)) {
            taxYear = date.getYear() + "-" + (removeFirstTwoDigits(date.getYear() + 1));
        } else {
            taxYear = (date.getYear() - 1) + "-" + removeFirstTwoDigits(date.getYear());
        }
        return taxYear;
    }

    private int removeFirstTwoDigits(int fourDigitYear) {
        return fourDigitYear % 100;
    }

    private String stripPlaceholderQueryParams(String href) {
        return href.replaceFirst("\\{&.*\\}", "");
    }

    private List<Employment> getEmployments(LocalDate fromDate, LocalDate toDate, String accessToken, Resource<String> linksResource) {
        final Resource<Employments> employmentsResource =
                followTraverson(buildLinkWithDateRangeQueryParams(fromDate, toDate, asAbsolute(linksResource.getLink("paye").getHref())), accessToken)
                        .toObject(employmentsResourceTypeRef);
        return employmentsResource.getContent().getEmployments();
    }

    private String getIndividualLink(Individual individual, String accessToken, String matchUrl) {
        log.info("POST to {}", matchUrl);

        Resource<String> resource = restTemplate.exchange(URI.create(matchUrl), HttpMethod.POST, createEntity(individual, accessToken), linksResourceTypeRef).getBody();
        log.info("Individual Response has been received for {}, {}", individual.getNino(), resource);
        return asAbsolute(resource.getLink("individual").getHref());
    }

    private Resource<EmbeddedIndividual> getIndividualResource(Individual individual, String accessToken, String matchUrl) {
        log.info("GET from {}", matchUrl);

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
        headers.add(HttpHeaders.ACCEPT, HMRC_VERSION_ACCEPT_HEADER);
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.add("Authorization", format("Bearer %s", accessToken));
        return new HttpEntity<>(individual, headers);
    }

    private HttpEntity createHeadersEntity(String accessToken) {
        HttpHeaders headers = generateHeaders();
        headers.add(HttpHeaders.ACCEPT, HMRC_VERSION_ACCEPT_HEADER);
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.add("Authorization", format("Bearer %s", accessToken));
        return new HttpEntity(null, headers);
    }

    private HttpHeaders generateHeaders(String accessToken) {
        final HttpHeaders headers = generateHeaders();
        headers.add("Authorization", format("Bearer %s", accessToken));
        return headers;
    }

    private HttpHeaders generateHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.ACCEPT, HMRC_VERSION_ACCEPT_HEADER);
        headers.add(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE);
        headers.add(CORRELATION_ID_HEADER, MDC.get(CORRELATION_ID_HEADER));
        headers.add(USER_ID_HEADER, MDC.get(USER_ID_HEADER));
        return headers;
    }

    private Traverson traversonFor(String link) {
        try {
            return new Traverson(new URI(link), APPLICATION_JSON).setRestOperations(this.restTemplate);
        } catch (URISyntaxException e) {
            throw new ApplicationExceptions.HmrcException("Problem building hmrc API url", e);
        }
    }

    private Traverson.TraversalBuilder followTraverson(String link, String accessToken) {
        log.info("following traverson for {}", link);
        return traversonFor(link).follow().withHeaders(generateHeaders(accessToken));
    }

}
