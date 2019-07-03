package uk.gov.digital.ho.pttg.application;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.Resource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Service;
import uk.gov.digital.ho.pttg.api.RequestHeaderData;
import uk.gov.digital.ho.pttg.application.domain.Individual;
import uk.gov.digital.ho.pttg.application.namematching.CandidateName;
import uk.gov.digital.ho.pttg.application.namematching.NameMatchingCandidatesService;
import uk.gov.digital.ho.pttg.application.namematching.NameMatchingPerformance;
import uk.gov.digital.ho.pttg.application.util.namenormalizer.NameNormalizer;
import uk.gov.digital.ho.pttg.dto.*;
import uk.gov.digital.ho.pttg.dto.saselfemployment.SelfEmployment;
import uk.gov.digital.ho.pttg.dto.saselfemployment.SelfEmploymentSelfAssessment;
import uk.gov.digital.ho.pttg.dto.saselfemployment.SelfEmploymentTaxReturn;

import java.math.BigDecimal;
import java.net.URI;
import java.time.Instant;
import java.time.LocalDate;
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
import static uk.gov.digital.ho.pttg.application.ApplicationExceptions.HmrcNotFoundException;
import static uk.gov.digital.ho.pttg.application.ApplicationExceptions.InvalidIdentityException;
import static uk.gov.digital.ho.pttg.application.HmrcHateoasClientFunctions.buildLinkWithDateRangeQueryParams;
import static uk.gov.digital.ho.pttg.application.HmrcHateoasClientFunctions.buildLinkWithTaxYearRangeQueryParams;
import static uk.gov.digital.ho.pttg.application.LogEvent.*;

@Service
@Slf4j
public class HmrcHateoasClient {

    private final NameMatchingPerformance nameMatchingPerformance;
    private final String hmrcUrl;
    private final RequestHeaderData requestHeaderData;
    private final NameNormalizer nameNormalizer;
    private final String matchUrl;
    private final HmrcCallWrapper hmrcCallWrapper;
    private final NameMatchingCandidatesService nameMatchingCandidatesService;

    private static final ParameterizedTypeReference<Resource<String>> linksResourceTypeRef = new ParameterizedTypeReference<Resource<String>>() {};
    private static final ParameterizedTypeReference<Resource<EmbeddedIndividual>> individualResourceTypeRef = new ParameterizedTypeReference<Resource<EmbeddedIndividual>>() {};
    private static final ParameterizedTypeReference<Resource<PayeIncome>> payeIncomesResourceTypeRef = new ParameterizedTypeReference<Resource<PayeIncome>>() {};
    private static final ParameterizedTypeReference<Resource<Employments>> employmentsResourceTypeRef = new ParameterizedTypeReference<Resource<Employments>>() {};
    private static final ParameterizedTypeReference<Resource<SelfEmploymentSelfAssessment>> saSelfEmploymentsResourceTypeRef = new ParameterizedTypeReference<Resource<SelfEmploymentSelfAssessment>>() {};

    private static final String INDIVIDUALS_MATCHING_PATH = "/individuals/matching/";
    private static final String REQUEST_DURATION_TIMESTAMP = "request_duration_ms";

    public HmrcHateoasClient(
            RequestHeaderData requestHeaderData,
            NameNormalizer nameNormalizer,
            HmrcCallWrapper hmrcCallWrapper,
            NameMatchingCandidatesService nameMatchingCandidatesService,
            NameMatchingPerformance nameMatchingPerformance,
            @Value("${hmrc.endpoint}") String hmrcUrl) {

        this.requestHeaderData = requestHeaderData;
        this.nameNormalizer = nameNormalizer;
        this.hmrcCallWrapper = hmrcCallWrapper;
        this.nameMatchingCandidatesService = nameMatchingCandidatesService;
        this.nameMatchingPerformance = nameMatchingPerformance;
        this.hmrcUrl = hmrcUrl;
        this.matchUrl = hmrcUrl + INDIVIDUALS_MATCHING_PATH;
    }

    List<Income> getPayeIncome(LocalDate fromDate, LocalDate toDate, String accessToken, Link link) {

        String linkHref = buildLinkWithDateRangeQueryParams(fromDate, toDate, asAbsolute(link.getHref()));
        log.info("Sending PAYE request to HMRC", value(EVENT, HMRC_PAYE_REQUEST_SENT));
        long requestStartTimeStamp = timestamp();
        Resource<PayeIncome> incomeResource = hmrcCallWrapper.followTraverson(linkHref, accessToken, payeIncomesResourceTypeRef);
        log.info("PAYE response received from HMRC",
                    value(EVENT, HMRC_PAYE_RESPONSE_RECEIVED),
                    value(REQUEST_DURATION_TIMESTAMP, calculateRequestDuration(requestStartTimeStamp)));

        return DataCleanser.clean(incomeResource.getContent().getPaye().getIncome());
    }

    List<Employment> getEmployments(LocalDate fromDate, LocalDate toDate, String accessToken, Link link) {

        final String linkHref = buildLinkWithDateRangeQueryParams(fromDate, toDate, asAbsolute(link.getHref()));

        log.info("Sending Employments request to HMRC", value(EVENT, HMRC_EMPLOYMENTS_REQUEST_SENT));
        long requestStartTimeStamp = timestamp();
        Resource<Employments> employmentsResource = hmrcCallWrapper.followTraverson(linkHref, accessToken, employmentsResourceTypeRef);
        log.info("Employments response received from HMRC",
                    value(EVENT, HMRC_EMPLOYMENTS_RESPONSE_RECEIVED),
                    value(REQUEST_DURATION_TIMESTAMP, calculateRequestDuration(requestStartTimeStamp)));
        return employmentsResource.getContent().getEmployments();
    }

    List<AnnualSelfAssessmentTaxReturn> getSelfAssessmentSelfEmploymentIncome(String accessToken, Link link) {

        if (link == null) {
            return emptyList();
        }

        log.info("Sending Self Assessment self employment request to HMRC", value(EVENT, HMRC_SA_REQUEST_SENT));
        long requestStartTimeStamp = timestamp();
        Resource<SelfEmploymentSelfAssessment> selfEmploymentsResource =
                hmrcCallWrapper.followTraverson(asAbsolute(link.getHref()), accessToken, saSelfEmploymentsResourceTypeRef);
        log.info("Self Assessment self employment response received from HMRC",
                    value(EVENT, HMRC_SA_RESPONSE_RECEIVED),
                    value(REQUEST_DURATION_TIMESTAMP, calculateRequestDuration(requestStartTimeStamp)));

        List<SelfEmploymentTaxReturn> taxReturns = selfEmploymentsResource.getContent().getSelfAssessment().getTaxReturns();

        return groupSelfEmploymentIncomes(taxReturns);
    }

    List<AnnualSelfAssessmentTaxReturn> groupSelfEmploymentIncomes(List<SelfEmploymentTaxReturn> taxReturns) {

        return taxReturns
                .stream()
                .map(tr -> AnnualSelfAssessmentTaxReturn.builder()
                        .taxYear(tr.getTaxYear())
                        .selfEmploymentProfit(tr.getSelfEmployments()
                                .stream()
                                .map(SelfEmployment::getSelfEmploymentProfit)
                                .reduce(BigDecimal.ZERO, BigDecimal::add))
                        .build())
                .collect(Collectors.toList());
    }

    Resource<String> getMatchResource(Individual individual, String accessToken) {

        log.info("Match Individual {} via a POST to {}", individual.getNino(), matchUrl, value(EVENT, HMRC_MATCHING_REQUEST_SENT));

        List<CandidateName> candidateNames = nameMatchingCandidatesService.generateCandidateNames(individual.getFirstName(), individual.getLastName(), individual.getAliasSurnames());

        int retries = 0;

        while (retries < candidateNames.size()) {
            log.info("Match attempt {} of {}", retries + 1, candidateNames.size(), value(EVENT, HMRC_MATCHING_ATTEMPTS));

            try {
                final Resource<String> matchedIndividual = performMatchedIndividualRequest(matchUrl, accessToken, candidateNames.get(retries), individual.getNino(), individual.getDateOfBirth());
                log.info("Successfully matched individual {}",
                         individual.getNino(),
                         value("attempt", retries + 1),
                         value("max_attempts", candidateNames.size()),
                         value("name-matching-analysis", candidateNames.get(retries).derivation()),
                         value("has_aliases", nameMatchingPerformance.hasAliases(candidateNames.get(retries).derivation().inputNames())),
                         value("special_characters", nameMatchingPerformance.hasSpecialCharacters(candidateNames.get(retries).derivation().inputNames())),
                         value(EVENT, HMRC_MATCHING_SUCCESS_RECEIVED));

                return matchedIndividual;

            } catch (HmrcNotFoundException ex) {
                log.info("Failed to match individual {}", individual.getNino(), value(EVENT, HMRC_MATCHING_FAILURE_RECEIVED));
                retries++;
            } catch (InvalidIdentityException e) {
                log.info("Skipped HMRC call due to Invalid Identity: {}", e.getMessage(), value(EVENT, HMRC_MATCHING_ATTEMPT_SKIPPED));
                retries++;
            }
        }

        log.info("Unsuccessfully matched individual {}",
                 individual.getNino(),
                 value("max_attempts", candidateNames.size()),
                 value("name-matching-analysis", candidateNames.get(0).derivation().inputNames()),
                 value(EVENT, HMRC_MATCHING_UNSUCCESSFUL));

        throw new HmrcNotFoundException(String.format("Unable to match: %s", individual));
    }

    Resource<String> performMatchedIndividualRequest(String matchUrl, String accessToken, CandidateName candidateNames, String nino, LocalDate dateOfBirth) {

        HmrcIndividual individualToMatch = new HmrcIndividual(candidateNames.firstName(), candidateNames.lastName(), nino, dateOfBirth);
        HmrcIndividual normalizedIndividual = nameNormalizer.normalizeNames(individualToMatch);
        checkForEmptyNormalizedName(normalizedIndividual);

        return hmrcCallWrapper.exchange(
                URI.create(matchUrl),
                HttpMethod.POST,
                createEntity(normalizedIndividual, accessToken),
                linksResourceTypeRef).getBody();
    }

    private void checkForEmptyNormalizedName(HmrcIndividual normalizedIndividual) {
        if (StringUtils.isBlank(normalizedIndividual.getFirstName()) || StringUtils.isBlank(normalizedIndividual.getLastName())) {
            throw new InvalidIdentityException("Normalized name contains a blank name");
        }
    }

    Resource<EmbeddedIndividual> getIndividualResource(String accessToken, Link link) {

        String url = asAbsolute(link.getHref());
        log.info("About to GET individual resource from HMRC at {}", url, value(EVENT, HMRC_INDIVIDUAL_REQUEST_SENT));
        long requestStartTimeStamp = timestamp();

        Resource<EmbeddedIndividual> resource = hmrcCallWrapper.exchange(URI.create(url), GET, createEntityWithHeadersWithoutBody(accessToken), individualResourceTypeRef).getBody();

        log.info("Individual resource response received",
                value(EVENT, HMRC_INDIVIDUAL_RESPONSE_RECEIVED),
                value(REQUEST_DURATION_TIMESTAMP, calculateRequestDuration(requestStartTimeStamp)));

        return resource;
    }

    Resource<String> getIncomeResource(String accessToken, Link link) {

        String url = asAbsolute(link.getHref());
        log.info("About to GET income resource from HMRC at {}", url, value(EVENT, HMRC_INCOME_REQUEST_SENT));
        long requestStartTimeStamp = timestamp();

        Resource<String> resource = hmrcCallWrapper.exchange(URI.create(url), GET, createEntityWithHeadersWithoutBody(accessToken), linksResourceTypeRef).getBody();

        log.info("Income resource response received",
                value(EVENT, HMRC_INCOME_RESPONSE_RECEIVED),
                value(REQUEST_DURATION_TIMESTAMP, calculateRequestDuration(requestStartTimeStamp)));

        return resource;
    }

    Resource<String> getEmploymentResource(String accessToken, Link link) {

        String url = asAbsolute(link.getHref());
        log.info("About to GET employment resource from HMRC at {}", url, value(EVENT, HMRC_EMPLOYMENTS_REQUEST_SENT));
        long requestStartTimeStamp = timestamp();

        Resource<String> resource = hmrcCallWrapper.exchange(URI.create(url), GET, createEntityWithHeadersWithoutBody(accessToken), linksResourceTypeRef).getBody();

        log.info("Employment resource response received",
                value(EVENT, HMRC_EMPLOYMENTS_RESPONSE_RECEIVED),
                value(REQUEST_DURATION_TIMESTAMP, calculateRequestDuration(requestStartTimeStamp)));

        return resource;
    }

    Resource<String> getSelfAssessmentResource(String accessToken, String fromTaxYear, String toTaxYear, Link link) {

        if (link == null) {
            log.debug("No SA Resource");
            return new Resource<>("", emptyList());
        }

        String baseUrl = asAbsolute(link.getHref());
        String url = buildLinkWithTaxYearRangeQueryParams(fromTaxYear, toTaxYear, baseUrl);

        log.info("About to get self assessment resource from HMRC at {}", url, value(EVENT, HMRC_SA_REQUEST_SENT));
        long requestStartTimeStamp = timestamp();

        Resource<String> resource = hmrcCallWrapper.exchange(URI.create(url), GET, createEntityWithHeadersWithoutBody(accessToken), linksResourceTypeRef).getBody();
        log.info("Self assessment resource response received",
                value(EVENT, HMRC_SA_RESPONSE_RECEIVED),
                value(REQUEST_DURATION_TIMESTAMP, calculateRequestDuration(requestStartTimeStamp)));

        return resource;
    }

    private String asAbsolute(String uri) {

        if (uri.startsWith("http")) {
            return uri;
        }

        return hmrcUrl + uri;
    }

    private HttpEntity<HmrcIndividual> createEntity(HmrcIndividual individual, String accessToken) {

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

    private long calculateRequestDuration(long requestStartTimeStamp) {
        return timestamp() - requestStartTimeStamp;
    }

    private long timestamp() {
        return Instant.now().toEpochMilli();
    }

}
