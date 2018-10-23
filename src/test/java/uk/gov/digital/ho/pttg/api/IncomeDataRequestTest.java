package uk.gov.digital.ho.pttg.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.exc.InvalidDefinitionException;
import com.fasterxml.jackson.databind.exc.InvalidFormatException;
import com.fasterxml.jackson.databind.exc.MismatchedInputException;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.io.IOException;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.digital.ho.pttg.api.JsonRequestUtilities.*;
import static uk.gov.digital.ho.pttg.application.SpringConfiguration.initialiseObjectMapper;

public class IncomeDataRequestTest {

    private ObjectMapper objectMapper;

    @Rule
    public final ExpectedException expectedException = ExpectedException.none();

    @Before
    public void setUp() {
        objectMapper = new ObjectMapper();
        initialiseObjectMapper(objectMapper);
    }

    @Test
    public void shouldDeserializeRequest() throws Exception {
        String request = getDefaultRequest();

        IncomeDataRequest incomeDataRequest = objectMapper.readValue(request, IncomeDataRequest.class);

        assertThat(incomeDataRequest).isNotNull();
        assertThat(incomeDataRequest.firstName()).isEqualTo("some first name");
        assertThat(incomeDataRequest.lastName()).isEqualTo("some last name");
        assertThat(incomeDataRequest.nino()).isEqualTo("AA123456A");
        assertThat(incomeDataRequest.dateOfBirth()).isEqualTo(LocalDate.of(1991, 2, 3));
        assertThat(incomeDataRequest.fromDate()).isEqualTo(LocalDate.of(2013, 4, 5));
        assertThat(incomeDataRequest.toDate()).isEqualTo(LocalDate.of(2018, 6, 7));
        assertThat(incomeDataRequest.aliasSurnames()).isEqualTo("some alias surnames");
    }

    @Test
    public void shouldDeserializeRequestWithoutAliases() throws Exception {
        String request = getRequestWithoutAlias();

        IncomeDataRequest incomeDataRequest = objectMapper.readValue(request, IncomeDataRequest.class);

        assertThat(incomeDataRequest).isNotNull();
        assertThat(incomeDataRequest.firstName()).isEqualTo("some first name");
        assertThat(incomeDataRequest.aliasSurnames()).isEmpty();
    }

    @Test
    public void shouldDeserializeRequestWithNullAlias() throws Exception {
        String request = getRequestWithNullAlias();

        IncomeDataRequest incomeDataRequest = objectMapper.readValue(request, IncomeDataRequest.class);

        assertThat(incomeDataRequest).isNotNull();
        assertThat(incomeDataRequest.firstName()).isEqualTo("some first name");
        assertThat(incomeDataRequest.aliasSurnames()).isEmpty();
    }

    @Test
    public void shouldFailIfNoFirstNameKey() throws IOException {
        String request = getRequestAndRemoveLineWithKey("firstName");

        expectedException.expect(MismatchedInputException.class);
        expectedException.expectMessage("firstName");

        objectMapper.readValue(request, IncomeDataRequest.class);
    }

    @Test
    public void shouldFailIfNoFirstName() throws IOException {
        String request = getRequestAndReplaceValueWithNull("firstName");

        expectedException.expect(InvalidDefinitionException.class);
        expectedException.expectMessage("firstName");

        objectMapper.readValue(request, IncomeDataRequest.class);
    }

    @Test
    public void shouldFailIfNoLastNameKey() throws IOException {
        String request = getRequestAndRemoveLineWithKey("lastName");

        expectedException.expect(MismatchedInputException.class);
        expectedException.expectMessage("lastName");

        objectMapper.readValue(request, IncomeDataRequest.class);
    }

    @Test
    public void shouldFailIfNoLastName() throws IOException {
        String request = getRequestAndReplaceValueWithNull("lastName");

        expectedException.expect(InvalidDefinitionException.class);
        expectedException.expectMessage("lastName");

        objectMapper.readValue(request, IncomeDataRequest.class);
    }

    @Test
    public void shouldFailIfNoNinoKey() throws IOException {
        String request = getRequestAndRemoveLineWithKey("nino");

        expectedException.expect(MismatchedInputException.class);
        expectedException.expectMessage("nino");

        objectMapper.readValue(request, IncomeDataRequest.class);
    }

    @Test
    public void shouldFailIfNoNino() throws IOException {
        String request = getRequestAndReplaceValueWithNull("nino");

        expectedException.expect(InvalidDefinitionException.class);
        expectedException.expectMessage("nino");

        objectMapper.readValue(request, IncomeDataRequest.class);
    }

    @Test
    public void shouldFailIfNoDateOfBirthKey() throws IOException {
        String request = getRequestAndRemoveLineWithKey("dateOfBirth");

        expectedException.expect(MismatchedInputException.class);
        expectedException.expectMessage("dateOfBirth");

        objectMapper.readValue(request, IncomeDataRequest.class);
    }

    @Test
    public void shouldFailIfNoDateOfBirth() throws IOException {
        String request = getRequestAndReplaceValueWithNull("dateOfBirth");

        expectedException.expect(InvalidDefinitionException.class);
        expectedException.expectMessage("dateOfBirth");

        objectMapper.readValue(request, IncomeDataRequest.class);
    }

    @Test
    public void shoulFailIfMalformedDateOfBirth() throws IOException {
        String request = getRequestAndReplaceValue("dateOfBirth", "bad_date");

        expectedException.expect(InvalidFormatException.class);
        expectedException.expectMessage("dateOfBirth");

        objectMapper.readValue(request, IncomeDataRequest.class);
    }

    @Test
    public void shouldFailIfNoFromDateKey() throws IOException {
        String request = getRequestAndRemoveLineWithKey("fromDate");

        expectedException.expect(MismatchedInputException.class);
        expectedException.expectMessage("fromDate");

        objectMapper.readValue(request, IncomeDataRequest.class);
    }

    @Test
    public void shouldFailIfNoFromDate() throws IOException {
        String request = getRequestAndReplaceValueWithNull("fromDate");

        expectedException.expect(InvalidDefinitionException.class);
        expectedException.expectMessage("fromDate");

        objectMapper.readValue(request, IncomeDataRequest.class);
    }

    @Test
    public void shoulFailIfMalformedFromDateOfBirth() throws IOException {
        String request = getRequestAndReplaceValue("fromDate", "bad_date");

        expectedException.expect(InvalidFormatException.class);
        expectedException.expectMessage("fromDate");

        objectMapper.readValue(request, IncomeDataRequest.class);
    }

    @Test
    public void shouldFailIfNoToDateKey() throws IOException {
        String request = getRequestAndRemoveLineWithKey("toDate");

        expectedException.expect(MismatchedInputException.class);
        expectedException.expectMessage("toDate");

        objectMapper.readValue(request, IncomeDataRequest.class);
    }

    @Test
    public void shouldFailIfNoToDate() throws IOException {
        String request = getRequestAndReplaceValueWithNull("toDate");

        expectedException.expect(InvalidDefinitionException.class);
        expectedException.expectMessage("toDate");

        objectMapper.readValue(request, IncomeDataRequest.class);
    }

    @Test
    public void shoulFailIfMalformedToDateOfBirth() throws IOException {
        String request = getRequestAndReplaceValue("toDate", "bad_date");

        expectedException.expect(InvalidFormatException.class);
        expectedException.expectMessage("toDate");

        objectMapper.readValue(request, IncomeDataRequest.class);
    }
}
