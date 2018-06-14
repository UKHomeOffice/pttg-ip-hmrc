package uk.gov.digital.ho.pttg.application;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.hateoas.Resource;
import org.springframework.hateoas.client.Traverson;
import org.springframework.http.*;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Recover;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;
import uk.gov.digital.ho.pttg.application.util.NameNormalizer;
import uk.gov.digital.ho.pttg.dto.*;

import java.math.BigDecimal;
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
import static org.springframework.http.HttpMethod.GET;
import static org.springframework.http.HttpStatus.FORBIDDEN;
import static org.springframework.http.HttpStatus.UNAUTHORIZED;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static uk.gov.digital.ho.pttg.api.RequestData.CORRELATION_ID_HEADER;
import static uk.gov.digital.ho.pttg.api.RequestData.USER_ID_HEADER;
import static uk.gov.digital.ho.pttg.application.ApplicationExceptions.*;
import static uk.gov.digital.ho.pttg.application.retry.NameMatchingCandidatesGenerator.generateCandidateNames;

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

    private final String hmrcApiVersion;
    private final String url;
    private final RestTemplate restTemplate;
    private final NinoUtils ninoUtils;
    private final NameNormalizer nameNormalizer;

    @Autowired
    public HmrcClient(RestTemplate restTemplate,
                      NinoUtils ninoUtils,
                      NameNormalizer nameNormalizer,
                      @Value("${hmrc.api.version}") String hmrcApiVersion,
                      @Value("${hmrc.endpoint}") String url) {
        this.restTemplate = restTemplate;
        this.nameNormalizer = nameNormalizer;
        this.hmrcApiVersion = hmrcApiVersion;
        this.url = url;
        this.ninoUtils = ninoUtils;
    }

    @Retryable(
            include = {HttpServerErrorException.class},
            exclude = {HttpClientErrorException.class, HmrcUnauthorisedException.class, HmrcNotFoundException.class},
            maxAttemptsExpression = "#{${hmrc.retry.attempts}}",
            backoff = @Backoff(delayExpression = "#{${hmrc.retry.delay}}"))
    public IncomeSummary getIncome(String accessToken, Individual suppliedIndividual, LocalDate fromDate, LocalDate toDate) {

        log.info("Commence the attempt to retrieve HMRC data for {}", ninoUtils.redact(suppliedIndividual.getNino()));

        Resource<String> matchedIndividualResource = getMatchedIndividualResource(suppliedIndividual, accessToken, url + "/individuals/matching/");

        String individualLink = asAbsolute(matchedIndividualResource.getLink("individual").getHref());
        Resource<EmbeddedIndividual> individualResource = getIndividualResource(suppliedIndividual, accessToken, individualLink);

        Individual matchedIndividual = individualResource.getContent().getIndividual();

        // income response with paye and SA hrefs
        String incomeLink = asAbsolute(individualResource.getLink("income").getHref());
        Resource<String> incomeResource = getIncomeResource(matchedIndividual, accessToken, incomeLink);

        // income paye response
        List<Income> payeIncomes = DataCleanser.clean(matchedIndividual, getIncome(fromDate, toDate, accessToken, incomeResource));

        // employments response with paye href
        String employmentLink = asAbsolute(individualResource.getLink("employments").getHref());
        Resource<String> employmentResource = getEmploymentResource(matchedIndividual, accessToken, employmentLink);

        // employment paye response
        List<Employment> employments = getEmployments(fromDate, toDate, accessToken, employmentResource);

        enrichIncomeData(payeIncomes, employments);

        // income SA response
        List<AnnualSelfAssessmentTaxReturn> selfAssessmentSelfEmployment = getSelfAssessmentSelfEmployment(fromDate, toDate, accessToken, incomeResource);

        log.info("Completed successfully the attempt to retrieve HMRC data for {}", ninoUtils.redact(suppliedIndividual.getNino()));

        return new IncomeSummary(payeIncomes, selfAssessmentSelfEmployment, employments, individualResource.getContent().getIndividual());
    }

    private void enrichIncomeData(List<Income> incomes, List<Employment> employments) {
        Map<String, String> employerPaymentRefMap = createEmployerPaymentRefMap(employments);
        addPaymentFrequency(incomes, employerPaymentRefMap);
    }

    void addPaymentFrequency(List<Income> incomes, Map<String, String> employerPaymentRefMap) {
        if (incomes == null) {
            return;
        }

        incomes
                .forEach(income -> income.setPaymentFrequency(employerPaymentRefMap.get(income.getEmployerPayeReference())));
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

    @Recover
    IncomeSummary getIncomeRetryFailureRecovery(HmrcNotFoundException e, String accessToken, Individual individual, LocalDate fromDate, LocalDate toDate) {
        log.error("Failed to retrieve HMRC data for {} - not matched", ninoUtils.redact(individual.getNino()));
        throw (e);
    }

    @Recover
    IncomeSummary getIncomeRetryFailureRecovery(HmrcUnauthorisedException e, String accessToken, Individual individual, LocalDate fromDate, LocalDate toDate) {
        log.error("Failed to retrieve HMRC data for {} - not authorised", ninoUtils.redact(individual.getNino()));
        throw (e);
    }

    @Recover
    IncomeSummary getIncomeRetryFailureRecovery(RuntimeException e, String accessToken, Individual individual, LocalDate fromDate, LocalDate toDate) {
        log.error("Failed to retrieve HMRC data for {} - {}", ninoUtils.redact(individual.getNino()), e.getMessage());
        throw (e);
    }

    private List<Income> getIncome(LocalDate fromDate, LocalDate toDate, String accessToken, Resource<String> linksResource) {

        final Resource<PayeIncome> incomeResource =
                followTraverson(buildLinkWithDateRangeQueryParams(fromDate, toDate, asAbsolute(linksResource.getLink("paye").getHref())), accessToken)
                        .toObject(payeIncomesResourceTypeRef);
        return incomeResource.getContent().getPaye().getIncome();
    }

    private List<AnnualSelfAssessmentTaxReturn> getSelfAssessmentSelfEmployment(LocalDate fromDate, LocalDate toDate, String accessToken, Resource<String> linksResource) {

        String selfEmploymentsLink = buildLinkWithTaxYearRangeQueryParams(fromDate, toDate, asAbsolute(linksResource.getLink("selfAssessment").getHref()));

        Resource<String> selfAssessmentResource = getSelfAssessmentResource(accessToken, selfEmploymentsLink);

        Resource<SelfEmployments> selfEmploymentsResource =
                followTraverson(asAbsolute(selfAssessmentResource.getLink("selfEmployments").getHref()), accessToken)
                        .toObject(selfEmploymentsResourceTypeRef);

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

    private Resource<String> getMatchedIndividualResource(Individual suppliedIndividual, String accessToken, String matchUrl) {

        log.info("POST to {}", matchUrl);

        List<String> candidateNames = generateCandidateNames(suppliedIndividual.getFirstName(), suppliedIndividual.getLastName());

        int retries = 0;

        while (retries < candidateNames.size()) {

            try {

                return performMatchedIndividualRequest(matchUrl, accessToken, candidateNames.get(retries), suppliedIndividual.getNino(), suppliedIndividual.getDateOfBirth());

            } catch (HttpClientErrorException ex) {
                HttpStatus statusCode = ex.getStatusCode();
                if (isHmrcMatchFailedError(ex)) {
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

        throw new HmrcNotFoundException(String.format("Unable to match: %s", suppliedIndividual));
    }

    private boolean isHmrcMatchFailedError(HttpClientErrorException exception) {
        HttpStatus statusCode = exception.getStatusCode();
        if (!statusCode.equals(FORBIDDEN)) {
            return  false;
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

    private Resource<EmbeddedIndividual> getIndividualResource(Individual individual, String accessToken, String matchUrl) {
        log.info("GET from {}", matchUrl);
        Resource<EmbeddedIndividual> resource = restTemplate.exchange(URI.create(matchUrl), GET, createHeadersEntity(accessToken), individualResourceTypeRef).getBody();
        log.info("Individual Response has been received for {}", ninoUtils.redact(individual.getNino()));
        return resource;
    }

    private Resource<String> getIncomeResource(Individual individual, String accessToken, String incomeLink) {
        log.info("GET from {}", incomeLink);

        Resource<String> resource = restTemplate.exchange(URI.create(incomeLink), GET, createHeadersEntity(accessToken), linksResourceTypeRef).getBody();
        log.info("Income Response has been received for {}, {}", ninoUtils.redact(individual.getNino()), resource);
        return resource;
    }

    private Resource<String> getSelfAssessmentResource(String accessToken, String selfEmploymentsLink) {
        log.info("GET from {}", selfEmploymentsLink);

        Resource<String> resource = restTemplate.exchange(URI.create(selfEmploymentsLink), GET, createHeadersEntity(accessToken), linksResourceTypeRef).getBody();
        log.info("Self Employment Response has been received for {}", resource);
        return resource;
    }

    private Resource<String> getEmploymentResource(Individual individual, String accessToken, String employmentLink) {
        log.info("GET from {}", employmentLink);

        Resource<String> resource = restTemplate.exchange(URI.create(employmentLink), GET, createHeadersEntity(accessToken), linksResourceTypeRef).getBody();
        log.info("Employment Response has been received for {}, {}", ninoUtils.redact(individual.getNino()), resource);
        return resource;
    }

    private String asAbsolute(String uri) {
        if (uri.startsWith("http")) {
            return uri;
        }
        return url + uri;
    }

    private HttpEntity<Individual> createEntity(Individual individual, String accessToken) {
        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.ACCEPT, hmrcApiVersion);
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.add("Authorization", format("Bearer %s", accessToken));
        return new HttpEntity<>(individual, headers);
    }

    private HttpEntity createHeadersEntity(String accessToken) {
        HttpHeaders headers = generateHeaders();
        headers.add(HttpHeaders.ACCEPT, hmrcApiVersion);
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.add("Authorization", format("Bearer %s", accessToken));
        return new HttpEntity<>(null, headers);
    }

    private HttpHeaders generateHeaders(String accessToken) {
        final HttpHeaders headers = generateHeaders();
        headers.add("Authorization", format("Bearer %s", accessToken));
        return headers;
    }

    private HttpHeaders generateHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.ACCEPT, hmrcApiVersion);
        headers.add(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE);
        headers.add(CORRELATION_ID_HEADER, MDC.get(CORRELATION_ID_HEADER));
        headers.add(USER_ID_HEADER, MDC.get(USER_ID_HEADER));
        return headers;
    }

    private Traverson traversonFor(String link) {
        try {
            return new Traverson(new URI(link), APPLICATION_JSON).setRestOperations(this.restTemplate);
        } catch (URISyntaxException e) {
            throw new HmrcException("Problem building hmrc API url", e);
        }
    }

    private Traverson.TraversalBuilder followTraverson(String link, String accessToken) {
        log.info("following traverson for {}", link);
        return traversonFor(link).follow().withHeaders(generateHeaders(accessToken));
    }

}
