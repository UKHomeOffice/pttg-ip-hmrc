package uk.gov.digital.ho.pttg.application.util.namenormalizer;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.digital.ho.pttg.dto.HmrcIndividual;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class CompositeNameNormalizerTest {
    @Mock
    private HmrcIndividual mockInputIndividual;

    @Mock
    private HmrcIndividual mockOutputIndividual;

    private CompositeNameNormalizer compositeNameNormalizer;

    @Test(expected = NullPointerException.class)
    public void shouldThrowNullPointerWhenArrayIsNull() {
        new CompositeNameNormalizer(null);
    }

    @Test
    public void shouldReturnInputtedIndividualWhenNoNameNormalizers() {
        // given
        NameNormalizer[] nameNormalizers = {};
        compositeNameNormalizer = new CompositeNameNormalizer(nameNormalizers);

        // when
        HmrcIndividual actualIndividual = compositeNameNormalizer.normalizeNames(mockInputIndividual);

        // then
        assertThat(actualIndividual).isEqualTo(mockInputIndividual);
        verifyNoMoreInteractions(mockInputIndividual);
    }

    @Test
    public void shouldCallSingleNameNormalizer() {
        // given

        NameNormalizer mockNameNormalizerOne = setupNameNormalizerMock();
        NameNormalizer[] nameNormalizers = {mockNameNormalizerOne};

        compositeNameNormalizer = new CompositeNameNormalizer(nameNormalizers);

        // when
        HmrcIndividual actualIndividual = compositeNameNormalizer.normalizeNames(mockInputIndividual);

        // then
        verify(mockNameNormalizerOne).normalizeNames(mockInputIndividual);

        assertThat(actualIndividual).isEqualTo(mockOutputIndividual);
        verifyNoMoreInteractions(mockOutputIndividual);
    }

    @Test
    public void shouldCallMultipleNameNormalizers() {
        // given

        NameNormalizer mockNameNormalizerOne = setupNameNormalizerMock();
        NameNormalizer mockNameNormalizerTwo = setupNameNormalizerMock();
        NameNormalizer[] nameNormalizers = {mockNameNormalizerOne, mockNameNormalizerTwo};

        compositeNameNormalizer = new CompositeNameNormalizer(nameNormalizers);

        // when
        HmrcIndividual actualIndividual = compositeNameNormalizer.normalizeNames(mockInputIndividual);

        // then
        verify(mockNameNormalizerOne).normalizeNames(mockInputIndividual);
        verify(mockNameNormalizerTwo).normalizeNames(mockOutputIndividual);

        assertThat(actualIndividual).isEqualTo(mockOutputIndividual);
        verifyNoMoreInteractions(mockOutputIndividual);
    }

    @Test
    public void shouldStripCharactersThatDoNotMapToLetters() {
        NameNormalizer[] nameNormalizers = {new DiacriticNameNormalizer(), new InvalidCharacterNameNormalizer()};
        compositeNameNormalizer = new CompositeNameNormalizer(nameNormalizers);

        String[] charactersThatDoNotMapToLetters = {"ǝ",
                "Ʌ",
                "Ɑ",
                "Ɒ",
                "ⱱ",
                "Ⱳ",
                "ⱳ",
                "ⱴ",
                "ⱸ",
                "ⱺ",
                "ⱻ",
                "ⱼ",
                "ⱽ",
                "Ȿ",
                "Ɀ",
                "ẜ",
                "ẝ",
                "Ỻ",
                "ỻ",
                "Ỿ",
                "Ə",
                "ƻ",
                "Ƽ",
                "ƽ",
                "ǀ",
                "ǁ",
                "ǂ",
                "ǃ",
                "Ɂ",
                "ɂ",
                "Ⱶ",
                "ⱶ",
                "ⱷ",
                "ⱹ",
                "ẟ",
                "Ỽ",
                "ỽ",
                "Ƅ",
                "ƅ",
                "Ǝ",
                "Ƨ",
                "ƨ"};

        LocalDate someDob = LocalDate.now();
        for (String nonMappingCharacter : charactersThatDoNotMapToLetters) {
            HmrcIndividual inputIndividual = new HmrcIndividual(nonMappingCharacter, nonMappingCharacter, "some nino", someDob);
            HmrcIndividual expectedNormalizedIndividual = new HmrcIndividual("", "", "some nino", someDob);

            assertThat(compositeNameNormalizer.normalizeNames(inputIndividual)).isEqualTo(expectedNormalizedIndividual);
        }
    }

    private NameNormalizer setupNameNormalizerMock() {
        NameNormalizer mock = mock(NameNormalizer.class);
        when(mock.normalizeNames(any())).thenReturn(mockOutputIndividual);
        return mock;
    }
}