package uk.gov.digital.ho.pttg.application;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.Resource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;
import uk.gov.digital.ho.pttg.application.util.NameNormalizer;
import uk.gov.digital.ho.pttg.application.util.TraversonUtils;
import uk.gov.digital.ho.pttg.dto.*;

import java.math.BigDecimal;
import java.net.URI;
import java.time.LocalDate;
import java.time.MonthDay;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static java.lang.String.format;
import static java.util.Collections.emptyList;
import static net.logstash.logback.argument.StructuredArguments.value;
import static org.springframework.http.HttpHeaders.ACCEPT;
import static org.springframework.http.HttpMethod.GET;
import static org.springframework.http.HttpStatus.FORBIDDEN;
import static org.springframework.http.HttpStatus.UNAUTHORIZED;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static uk.gov.digital.ho.pttg.api.RequestData.CORRELATION_ID_HEADER;
import static uk.gov.digital.ho.pttg.api.RequestData.USER_ID_HEADER;
import static uk.gov.digital.ho.pttg.application.ApplicationExceptions.*;
import static uk.gov.digital.ho.pttg.application.LogEvent.*;
import static uk.gov.digital.ho.pttg.application.namematching.NameMatchingCandidatesGenerator.generateCandidateNames;

@Service
@Slf4j
public class HmrcClient {

    private static final ParameterizedTypeReference<Resource<String>> linksResourceTypeRef = new ParameterizedTypeReference<Resource<String>>() {};
    private static final ParameterizedTypeReference<Resource<EmbeddedIndividual>> individualResourceTypeRef = new ParameterizedTypeReference<Resource<EmbeddedIndividual>>() {};
    private static final ParameterizedTypeReference<Resource<PayeIncome>> payeIncomesResourceTypeRef = new ParameterizedTypeReference<Resource<PayeIncome>>() {};
    private static final ParameterizedTypeReference<Resource<Employments>> employmentsResourceTypeRef = new ParameterizedTypeReference<Resource<Employments>>() {};
    private static final ParameterizedTypeReference<Resource<SelfEmployments>> selfEmploymentsResourceTypeRef = new ParameterizedTypeReference<Resource<SelfEmployments>>() {};

    private static final MonthDay END_OF_TAX_YEAR = MonthDay.of(4, 5);
    private static final String QUERY_PARAM_TO_DATE = "toDate";
    private static final String QUERY_PARAM_FROM_DATE = "fromDate";
    private static final String QUERY_PARAM_TO_TAX_YEAR = "toTaxYear";
    private static final String QUERY_PARAM_FROM_TAX_YEAR = "fromTaxYear";

    private static final String DEFAULT_PAYMENT_FREQUENCY = "ONE_OFF";

    /*
        Hypermedia paths and links
    */
    private static final String INDIVIDUALS_MATCHING_PATH = "/individuals/matching/";
    static final String INDIVIDUAL = "individual";
    static final String INCOME = "income";
    static final String EMPLOYMENTS = "employments";
    static final String SELF_ASSESSMENT = "selfAssessment";
    static final String PAYE_INCOME = "paye";
    static final String PAYE_EMPLOYMENT = "paye";
    static final String SELF_EMPLOYMENTS = "selfEmployments";

    private final String hmrcApiVersion;
    private final String hmrcUrl;
    private final RestTemplate restTemplate;
    private final NinoUtils ninoUtils;
    private final TraversonUtils traversonUtils;
    private final NameNormalizer nameNormalizer;
    private final String matchUrl;

    public HmrcClient(RestTemplate restTemplate,
                      NinoUtils ninoUtils,
                      TraversonUtils traversonUtils,
                      NameNormalizer nameNormalizer,
                      @Value("${hmrc.api.version}") String hmrcApiVersion,
                      @Value("${hmrc.endpoint}") String hmrcUrl) {
        this.restTemplate = restTemplate;
        this.nameNormalizer = nameNormalizer;
        this.hmrcApiVersion = hmrcApiVersion;
        this.hmrcUrl = hmrcUrl;
        this.ninoUtils = ninoUtils;
        this.traversonUtils = traversonUtils;
        this.matchUrl = hmrcUrl + INDIVIDUALS_MATCHING_PATH;
    }

    public IncomeSummary getIncomeSummary(String accessToken, Individual suppliedIndividual, LocalDate fromDate, LocalDate toDate, IncomeSummaryContext context) {

        String redactedNino = ninoUtils.redact(suppliedIndividual.getNino());

        log.info("Attempt to retrieve HMRC data for {}", redactedNino);

        storeMatchResource(suppliedIndividual, accessToken, matchUrl, context);

        storeIndividualResource(redactedNino, accessToken, context);
        storeIncomeResource(redactedNino, accessToken, context);
        storeEmploymentResource(redactedNino, accessToken, context);
        storeSelfAssessmentResource(redactedNino, accessToken, fromDate, toDate, context);

        storePayeIncome(fromDate, toDate, accessToken, context);
        storeEmployments(fromDate, toDate, accessToken, context);
        storeSelfAssessmentIncome(accessToken, context);

        enrichIncomeData(context.payeIncome(), context.employments());

        log.info("Successfully retrieved HMRC data for {}", redactedNino);

        return new IncomeSummary(
                context.payeIncome(),
                context.selfAssessmentIncome(),
                context.employments(),
                context.individualResource().getContent().getIndividual());
    }

    private void enrichIncomeData(List<Income> incomes, List<Employment> employments) {
        Map<String, String> employerPaymentRefMap = createEmployerPaymentRefMap(employments);
        addPaymentFrequency(incomes, employerPaymentRefMap);
    }

    void addPaymentFrequency(List<Income> incomes, Map<String, String> employerPaymentRefMap) {
        if (incomes == null) {
            return;
        }

        incomes.forEach(income -> income.setPaymentFrequency(employerPaymentRefMap.get(income.getEmployerPayeReference())));
    }

    Map<String, String> createEmployerPaymentRefMap(List<Employment> employments) {
        Map<String, String> paymentFrequency = new HashMap<>();

        for (Employment employment : employments) {

            String payeReference = employment.getEmployer().getPayeReference();

            if (StringUtils.isEmpty(employment.getPayFrequency())) {
                paymentFrequency.put(payeReference, DEFAULT_PAYMENT_FREQUENCY);
            } else {
                paymentFrequency.put(payeReference, employment.getPayFrequency());
            }

        }

        return paymentFrequency;
    }

    private void storePayeIncome(LocalDate fromDate, LocalDate toDate, String accessToken, IncomeSummaryContext context) {
        if (context.needsPayeIncome()) {
            context.setPayeIncome(getPayeIncome(fromDate, toDate, accessToken, context.incomeResource().getLink(PAYE_INCOME)));
        }
    }

    List<Income> getPayeIncome(LocalDate fromDate, LocalDate toDate, String accessToken, Link link) {

        String linkHref = buildLinkWithDateRangeQueryParams(fromDate, toDate, asAbsolute(link.getHref()));
        log.info("Sending PAYE request to HMRC", value(EVENT, HMRC_PAYE_REQUEST_SENT));
        Resource<PayeIncome> incomeResource = traversonUtils.followTraverson(linkHref, accessToken, hmrcApiVersion, restTemplate, payeIncomesResourceTypeRef);
        log.info("PAYE response received from HMRC", value(EVENT, HMRC_PAYE_RESPONSE_RECEIVED));

        return DataCleanser.clean(incomeResource.getContent().getPaye().getIncome());
    }


    private void storeEmployments(LocalDate fromDate, LocalDate toDate, String accessToken, IncomeSummaryContext context) {
        if (context.needsEmployments()) {
            context.setEmployments(getEmployments(fromDate, toDate, accessToken, context.employmentResource().getLink(PAYE_EMPLOYMENT)));
        }
    }

    private List<Employment> getEmployments(LocalDate fromDate, LocalDate toDate, String accessToken, Link link) {

        Resource<Employments> employmentsResource =
                traversonUtils.followTraverson(buildLinkWithDateRangeQueryParams(fromDate, toDate, asAbsolute(link.getHref())), accessToken, hmrcApiVersion, restTemplate, employmentsResourceTypeRef);

        return employmentsResource.getContent().getEmployments();
    }

    private void storeSelfAssessmentIncome(String accessToken, IncomeSummaryContext context) {
        if (context.needsSelfAssessmentIncome()) {
            context.setSelfAssessmentIncome(getSelfAssessmentIncome(accessToken, context.selfAssessmentResource().getLink(SELF_EMPLOYMENTS)));
        }
    }

    private List<AnnualSelfAssessmentTaxReturn> getSelfAssessmentIncome(String accessToken, Link link) {

        if (link == null) {
            return emptyList();
        }

        Resource<SelfEmployments> selfEmploymentsResource =
                traversonUtils.followTraverson(asAbsolute(link.getHref()), accessToken, hmrcApiVersion, restTemplate, selfEmploymentsResourceTypeRef);

        List<TaxReturn> taxReturns = selfEmploymentsResource.getContent().getSelfAssessment().getTaxReturns();

        return groupSelfEmployments(taxReturns);
    }

    private List<AnnualSelfAssessmentTaxReturn> groupSelfEmployments(List<TaxReturn> taxReturns) {

        return taxReturns
                .stream()
                .map(tr -> new AnnualSelfAssessmentTaxReturn(tr.getTaxYear(),
                        tr.getSelfEmployments()
                                .stream()
                                .map(SelfEmployment::getSelfEmploymentProfit)
                                .reduce(BigDecimal.ZERO, BigDecimal::add)))
                .collect(Collectors.toList());
    }

    private String buildLinkWithDateRangeQueryParams(LocalDate fromDate, LocalDate toDate, String href) {
        String uri;
        UriComponentsBuilder uriComponentsBuilder = UriComponentsBuilder.fromHttpUrl(stripPlaceholderQueryParams(href));
        UriComponentsBuilder withFromDate = uriComponentsBuilder.queryParam(QUERY_PARAM_FROM_DATE, fromDate.format(DateTimeFormatter.ISO_DATE));
        if (toDate != null) {
            uri = withFromDate.queryParam(QUERY_PARAM_TO_DATE, toDate.format(DateTimeFormatter.ISO_DATE)).build().toUriString();
        } else {
            uri = withFromDate.build().toUriString();
        }
        return uri;
    }

    private String buildLinkWithTaxYearRangeQueryParams(LocalDate fromDate, LocalDate toDate, String href) {
        String uri;
        UriComponentsBuilder uriComponentsBuilder = UriComponentsBuilder.fromHttpUrl(stripPlaceholderQueryParams(href));
        UriComponentsBuilder withFromDate = uriComponentsBuilder.queryParam(QUERY_PARAM_FROM_TAX_YEAR, getTaxYear(fromDate));
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

    private void storeMatchResource(Individual individual, String accessToken, String matchUrl, IncomeSummaryContext context) {
        if (context.needsMatchResource()) {
            context.setMatchResource(getMatchResource(individual, accessToken, matchUrl));
        }
    }

    Resource<String> getMatchResource(Individual individual, String accessToken, String matchUrl) {

        log.info("Match Individual {} via a POST to {}", ninoUtils.redact(individual.getNino()), matchUrl, value(EVENT, HMRC_MATCHING_REQUEST_SENT));

        List<String> candidateNames = generateCandidateNames(individual.getFirstName(), individual.getLastName());

        int retries = 0;

        while (retries < candidateNames.size()) {
            log.info("Match attempt {} of {}", retries + 1, candidateNames.size(), value(EVENT, HMRC_MATCHING_ATTEMPTS));

            try {
                final Resource<String> matchedIndividual = performMatchedIndividualRequest(matchUrl, accessToken, candidateNames.get(retries), individual.getNino(), individual.getDateOfBirth());
                log.info("Successfully matched individual {}", ninoUtils.redact(individual.getNino()), value(EVENT, HMRC_MATCHING_SUCCESS_RECEIVED));
                return matchedIndividual;

            } catch (HttpClientErrorException ex) {
                HttpStatus statusCode = ex.getStatusCode();
                if (isHmrcMatchFailedError(ex)) {
                    log.info("Failed to match individual {}", ninoUtils.redact(individual.getNino()), value(EVENT, HMRC_MATCHING_FAILURE_RECEIVED));
                    retries++;
                } else if (statusCode.equals(FORBIDDEN)) {
                    throw new ProxyForbiddenException("Received a 403 Forbidden response from proxy");
                } else if (statusCode.equals(UNAUTHORIZED)) {
                    throw new HmrcUnauthorisedException(ex.getMessage(), ex);
                } else {
                    throw ex;
                }
            }
        }

        throw new HmrcNotFoundException(String.format("Unable to match: %s", individual));
    }

    private boolean isHmrcMatchFailedError(HttpClientErrorException exception) {

        HttpStatus statusCode = exception.getStatusCode();

        if (!statusCode.equals(FORBIDDEN)) {
            return false;
        }

        return exception.getResponseBodyAsString().contains("MATCHING_FAILED");
    }

    private Resource<String> performMatchedIndividualRequest(String matchUrl, String accessToken, String candidateNames, String nino, LocalDate dateOfBirth) {
        String[] names = candidateNames.split("\\s+");

        Individual individualToMatch = new Individual(names[0], names[1], nino, dateOfBirth);
        Individual normalizedIndividual = nameNormalizer.normalizeNames(individualToMatch);

        return restTemplate.exchange(
                URI.create(matchUrl),
                HttpMethod.POST,
                createEntity(normalizedIndividual, accessToken),
                linksResourceTypeRef).getBody();
    }

    private void storeIndividualResource(String nino, String accessToken, IncomeSummaryContext context) {
        if (context.needsIndividualResource()) {
            context.setIndividualResource(getIndividualResource(nino, accessToken, context.matchResource().getLink(INDIVIDUAL)));
        }
    }

    private Resource<EmbeddedIndividual> getIndividualResource(String nino, String accessToken, Link link) {

        String url = asAbsolute(link.getHref());
        log.info("GET Individual Resource for {} from {}", nino, url);
        Resource<EmbeddedIndividual> resource = restTemplate.exchange(URI.create(url), GET, createEntityWithHeadersWithoutBody(accessToken), individualResourceTypeRef).getBody();
        log.info("Individual Response has been received for {}", nino);

        return resource;
    }

    private void storeIncomeResource(String nino, String accessToken, IncomeSummaryContext context) {
        if (context.needsIncomeResource()) {
            context.setIncomeResource(getIncomeResource(nino, accessToken, context.individualResource().getLink(INCOME)));
        }
    }

    private Resource<String> getIncomeResource(String nino, String accessToken, Link link) {

        String url = asAbsolute(link.getHref());
        log.info("GET Income Resource for {} from {}", nino, url);
        Resource<String> resource = restTemplate.exchange(URI.create(url), GET, createEntityWithHeadersWithoutBody(accessToken), linksResourceTypeRef).getBody();
        log.info("Income Response has been received for {}", nino);

        return resource;
    }

    private void storeEmploymentResource(String nino, String accessToken, IncomeSummaryContext context) {
        if (context.needsEmploymentResource()) {
            context.setEmploymentResource(getEmploymentResource(nino, accessToken, context.individualResource().getLink(EMPLOYMENTS)));
        }
    }

    private Resource<String> getEmploymentResource(String nino, String accessToken, Link link) {

        String url = asAbsolute(link.getHref());
        log.info("GET Employment Resource for {} from {}", nino, url);
        Resource<String> resource = restTemplate.exchange(URI.create(url), GET, createEntityWithHeadersWithoutBody(accessToken), linksResourceTypeRef).getBody();
        log.info("Employment Response has been received for {}", nino);

        return resource;
    }

    private void storeSelfAssessmentResource(String nino, String accessToken, LocalDate fromDate, LocalDate toDate, IncomeSummaryContext context) {
        if (context.needsSelfAssessmentResource()) {
            context.setSelfAssessmentResource(getSelfAssessmentResource(nino, accessToken, fromDate, toDate, context.incomeResource().getLink(SELF_ASSESSMENT)));
        }
    }

    private Resource<String> getSelfAssessmentResource(String nino, String accessToken, LocalDate fromDate, LocalDate toDate, Link link) {

        if (link == null) {
            log.info("No SA Resource for {}", nino);
            return new Resource<>("", emptyList());
        }

        String baseUrl = asAbsolute(link.getHref());
        String url = buildLinkWithTaxYearRangeQueryParams(fromDate, toDate, baseUrl);

        log.info("GET SA Resource for {} from {}", nino, url);

        Resource<String> resource = restTemplate.exchange(URI.create(url), GET, createEntityWithHeadersWithoutBody(accessToken), linksResourceTypeRef).getBody();
        log.info("SA Response has been received for {}", nino);

        return resource;
    }

    private String asAbsolute(String uri) {

        if (uri.startsWith("http")) {
            return uri;
        }

        return hmrcUrl + uri;
    }

    // TODO: Can this method produce an entity with CORRELATION_ID header? If so, can combine createEntityWithHeadersWithoutBody
    private HttpEntity<Individual> createEntity(Individual individual, String accessToken) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(APPLICATION_JSON);
        headers.add(ACCEPT, hmrcApiVersion);
        headers.add("Authorization", format("Bearer %s", accessToken));
        return new HttpEntity<>(individual, headers);
    }

    private HttpEntity createEntityWithHeadersWithoutBody(String accessToken) {
        HttpHeaders headers = generateHeaders();
        headers.add("Authorization", format("Bearer %s", accessToken));
        return new HttpEntity<>(null, headers);
    }

    private HttpHeaders generateHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(APPLICATION_JSON);
        headers.add(ACCEPT, hmrcApiVersion);
        headers.add(CORRELATION_ID_HEADER, MDC.get(CORRELATION_ID_HEADER));
        headers.add(USER_ID_HEADER, MDC.get(USER_ID_HEADER));
        return headers;
    }
}
