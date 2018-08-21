package uk.gov.digital.ho.pttg.application;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.Resource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Service;
import org.springframework.web.util.UriComponentsBuilder;
import uk.gov.digital.ho.pttg.api.RequestHeaderData;
import uk.gov.digital.ho.pttg.application.namematching.PersonName;
import uk.gov.digital.ho.pttg.application.util.NameNormalizer;
import uk.gov.digital.ho.pttg.dto.*;

import java.math.BigDecimal;
import java.net.URI;
import java.time.LocalDate;
import java.time.MonthDay;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

import static java.lang.String.format;
import static java.util.Collections.emptyList;
import static net.logstash.logback.argument.StructuredArguments.value;
import static org.springframework.http.HttpHeaders.ACCEPT;
import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static org.springframework.http.HttpMethod.GET;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static uk.gov.digital.ho.pttg.api.RequestHeaderData.*;
import static uk.gov.digital.ho.pttg.application.LogEvent.*;
import static uk.gov.digital.ho.pttg.application.namematching.NameMatchingCandidatesGenerator.generateCandidateNames;

@Service
@Slf4j
public class HmrcHateoasClient {

    private final String hmrcUrl;
    private final RequestHeaderData requestHeaderData;
    private final NameNormalizer nameNormalizer;
    private final String matchUrl;
    private final HmrcCallWrapper hmrcCallWrapper;

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

    private static final String INDIVIDUALS_MATCHING_PATH = "/individuals/matching/";

    public HmrcHateoasClient(
            RequestHeaderData requestHeaderData,
            NameNormalizer nameNormalizer,
            HmrcCallWrapper hmrcCallWrapper,
            @Value("${hmrc.endpoint}") String hmrcUrl
            ) {
        this.requestHeaderData = requestHeaderData;
        this.nameNormalizer = nameNormalizer;
        this.hmrcCallWrapper = hmrcCallWrapper;
        this.hmrcUrl = hmrcUrl;
        this.matchUrl = hmrcUrl + INDIVIDUALS_MATCHING_PATH;
    }

    List<Income> getPayeIncome(LocalDate fromDate, LocalDate toDate, String accessToken, Link link) {

        String linkHref = buildLinkWithDateRangeQueryParams(fromDate, toDate, asAbsolute(link.getHref()));
        log.info("Sending PAYE request to HMRC", value(EVENT, HMRC_PAYE_REQUEST_SENT));
        Resource<PayeIncome> incomeResource = hmrcCallWrapper.followTraverson(linkHref, accessToken, payeIncomesResourceTypeRef);
        log.info("PAYE response received from HMRC", value(EVENT, HMRC_PAYE_RESPONSE_RECEIVED));

        return DataCleanser.clean(incomeResource.getContent().getPaye().getIncome());
    }

    List<Employment> getEmployments(LocalDate fromDate, LocalDate toDate, String accessToken, Link link) {

        final String linkHref = buildLinkWithDateRangeQueryParams(fromDate, toDate, asAbsolute(link.getHref()));

        log.info("Sending Employments request to HMRC", value(EVENT, HMRC_EMPLOYMENTS_REQUEST_SENT));
        Resource<Employments> employmentsResource = hmrcCallWrapper.followTraverson(linkHref, accessToken, employmentsResourceTypeRef);
        log.info("Employments response received from HMRC", value(EVENT, HMRC_EMPLOYMENTS_RESPONSE_RECEIVED));
        return employmentsResource.getContent().getEmployments();
    }

    List<AnnualSelfAssessmentTaxReturn> getSelfAssessmentIncome(String accessToken, Link link) {

        if (link == null) {
            return emptyList();
        }

        log.info("Sending Self Assessment request to HMRC", value(EVENT, HMRC_SA_REQUEST_SENT));
        Resource<SelfEmployments> selfEmploymentsResource =
                hmrcCallWrapper.followTraverson(asAbsolute(link.getHref()), accessToken, selfEmploymentsResourceTypeRef);
        log.info("Self Assessment response received from HMRC", value(EVENT, HMRC_SA_RESPONSE_RECEIVED));

        List<TaxReturn> taxReturns = selfEmploymentsResource.getContent().getSelfAssessment().getTaxReturns();

        return groupSelfEmployments(taxReturns);
    }

    List<AnnualSelfAssessmentTaxReturn> groupSelfEmployments(List<TaxReturn> taxReturns) {

        return taxReturns
                .stream()
                .map(tr -> new AnnualSelfAssessmentTaxReturn(tr.getTaxYear(),
                        tr.getSelfEmployments()
                                .stream()
                                .map(SelfEmployment::getSelfEmploymentProfit)
                                .reduce(BigDecimal.ZERO, BigDecimal::add)))
                .collect(Collectors.toList());
    }

    String buildLinkWithDateRangeQueryParams(LocalDate fromDate, LocalDate toDate, String href) {
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

    String buildLinkWithTaxYearRangeQueryParams(LocalDate fromDate, LocalDate toDate, String href) {
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

    Resource<String> getMatchResource(Individual individual, String accessToken) {

        log.info("Match Individual {} via a POST to {}", individual.getNino(), matchUrl, value(EVENT, HMRC_MATCHING_REQUEST_SENT));

        List<PersonName> candidateNames = generateCandidateNames(individual.getFirstName(), individual.getLastName());

        int retries = 0;

        while (retries < candidateNames.size()) {
            log.info("Match attempt {} of {}", retries + 1, candidateNames.size(), value(EVENT, HMRC_MATCHING_ATTEMPTS));

            try {
                final Resource<String> matchedIndividual = performMatchedIndividualRequest(matchUrl, accessToken, candidateNames.get(retries), individual.getNino(), individual.getDateOfBirth());
                log.info("Successfully matched individual {}", individual.getNino(), value(EVENT, HMRC_MATCHING_SUCCESS_RECEIVED));
                return matchedIndividual;

            } catch (ApplicationExceptions.HmrcNotFoundException ex) {
                    log.info("Failed to match individual {}", individual.getNino(), value(EVENT, HMRC_MATCHING_FAILURE_RECEIVED));
                    retries++;
            }
        }

        throw new ApplicationExceptions.HmrcNotFoundException(String.format("Unable to match: %s", individual));
    }


    private Resource<String> performMatchedIndividualRequest(String matchUrl, String accessToken, PersonName candidateNames, String nino, LocalDate dateOfBirth) {

        Individual individualToMatch = new Individual(candidateNames.getFirstName(), candidateNames.getSurname(), nino, dateOfBirth);
        Individual normalizedIndividual = nameNormalizer.normalizeNames(individualToMatch);

        return hmrcCallWrapper.exchange(
                URI.create(matchUrl),
                HttpMethod.POST,
                createEntity(normalizedIndividual, accessToken),
                linksResourceTypeRef).getBody();
    }

    Resource<EmbeddedIndividual> getIndividualResource(String accessToken, Link link) {

        String url = asAbsolute(link.getHref());
        log.debug("GET Individual Resource from {}", url);
        Resource<EmbeddedIndividual> resource = hmrcCallWrapper.exchange(URI.create(url), GET, createEntityWithHeadersWithoutBody(accessToken), individualResourceTypeRef).getBody();
        log.debug("Individual Response has been received");

        return resource;
    }

    Resource<String> getIncomeResource(String accessToken, Link link) {

        String url = asAbsolute(link.getHref());
        log.debug("GET Income Resource from {}", url);
        Resource<String> resource = hmrcCallWrapper.exchange(URI.create(url), GET, createEntityWithHeadersWithoutBody(accessToken), linksResourceTypeRef).getBody();
        log.debug("Income Response has been received");

        return resource;
    }

    Resource<String> getEmploymentResource(String accessToken, Link link) {

        String url = asAbsolute(link.getHref());
        log.debug("GET Employment Resource from {}", url);
        Resource<String> resource = hmrcCallWrapper.exchange(URI.create(url), GET, createEntityWithHeadersWithoutBody(accessToken), linksResourceTypeRef).getBody();
        log.debug("Employment Response has been received");

        return resource;
    }

    Resource<String> getSelfAssessmentResource(String accessToken, LocalDate fromDate, LocalDate toDate, Link link) {

        if (link == null) {
            log.debug("No SA Resource");
            return new Resource<>("", emptyList());
        }

        String baseUrl = asAbsolute(link.getHref());
        String url = buildLinkWithTaxYearRangeQueryParams(fromDate, toDate, baseUrl);

        log.debug("GET SA Resource from {}", url);

        Resource<String> resource = hmrcCallWrapper.exchange(URI.create(url), GET, createEntityWithHeadersWithoutBody(accessToken), linksResourceTypeRef).getBody();
        log.debug("SA Response has been received");

        return resource;
    }

    private String asAbsolute(String uri) {

        if (uri.startsWith("http")) {
            return uri;
        }

        return hmrcUrl + uri;
    }

    private HttpEntity<Individual> createEntity(Individual individual, String accessToken) {

        HttpHeaders headers = generateHeaders();

        headers.add(AUTHORIZATION, format("Bearer %s", accessToken));

        return new HttpEntity<>(individual, headers);
    }

    private HttpEntity createEntityWithHeadersWithoutBody(String accessToken) {

        HttpHeaders headers = generateHeaders();

        headers.add(AUTHORIZATION, format("Bearer %s", accessToken));

        return new HttpEntity<>(null, headers);
    }

    private HttpHeaders generateHeaders() {

        HttpHeaders headers = new HttpHeaders();

        headers.add(ACCEPT, requestHeaderData.hmrcApiVersion());
        headers.add(SESSION_ID_HEADER, requestHeaderData.sessionId());
        headers.add(CORRELATION_ID_HEADER, requestHeaderData.correlationId());
        headers.add(USER_ID_HEADER, requestHeaderData.userId());
        headers.setContentType(APPLICATION_JSON);

        return headers;
    }

}
