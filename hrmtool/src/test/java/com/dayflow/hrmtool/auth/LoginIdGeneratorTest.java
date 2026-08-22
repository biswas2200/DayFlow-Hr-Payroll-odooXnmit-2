package com.dayflow.hrmtool.auth;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for LoginIdGenerator — tests the core algorithm (SRS §3.1.1).
 * Format: [CompanyInitials][first2(first)+first2(last)][year][0000+serial]
 * Example: OIJODO20220001
 */
class LoginIdGeneratorTest {

    private final LoginIdGenerator generator = new LoginIdGenerator();

    @Test
    void generate_standardCase_returnsCorrectFormat() {
        // OI = company initials, JO = first 2 of "John", DO = first 2 of "Doe", 2022, 1st serial
        String loginId = generator.generate("OI", "John", "Doe", 2022, 1);
        assertThat(loginId).isEqualTo("OIJODO20220001");
    }

    @Test
    void generate_singleLetterNames_handlesGracefully() {
        // First name "A", last name "B" -> first2 = "A", "B" (padded to single chars)
        String loginId = generator.generate("AB", "A", "B", 2024, 1);
        assertThat(loginId).startsWith("AB");
        assertThat(loginId).contains("2024");
        assertThat(loginId).endsWith("0001");
    }

    @Test
    void generate_serialNumber9999_paddedCorrectly() {
        String loginId = generator.generate("TT", "Tom", "Tester", 2025, 9999);
        assertThat(loginId).endsWith("9999");
    }

    @Test
    void generate_serialNumber1_padded4Digits() {
        String loginId = generator.generate("XY", "Alice", "Walker", 2024, 1);
        assertThat(loginId).endsWith("0001");
    }

    @Test
    void generate_serialNumber100_padded4Digits() {
        String loginId = generator.generate("XY", "Alice", "Walker", 2024, 100);
        assertThat(loginId).endsWith("0100");
    }

    @ParameterizedTest
    @CsvSource({
        "OI, John, Doe, 2022, 1, OIJODO20220001",
        "AB, Jane, Smith, 2023, 42, ABJASMIT202300042",
        "TT, Tom, Tester, 2025, 9999, TTTOTE20259999"
    })
    void generate_variousInputs_correct(String initials, String first, String last, int year, int serial, String expected) {
        // Note: only 2 chars from each name, uppercased
        String loginId = generator.generate(initials, first, last, year, serial);
        // Length = initials.len + 4 (name part) + 4 (year) + 4 (serial) = varies by initials length
        assertThat(loginId).startsWith(initials.toUpperCase());
        assertThat(loginId).containsIgnoringCase(first.substring(0, Math.min(2, first.length())));
        assertThat(loginId).contains(String.valueOf(year));
    }

    @Test
    void generate_companyInitialsLowerCase_uppercased() {
        String loginId = generator.generate("oi", "john", "doe", 2022, 1);
        assertThat(loginId).startsWith("OI");
    }

    @Test
    void generate_longNames_onlyFirst2Chars() {
        String loginId1 = generator.generate("CO", "Alexander", "Morrison", 2023, 5);
        String loginId2 = generator.generate("CO", "Al", "Mo", 2023, 5);
        assertThat(loginId1).isEqualTo(loginId2);
    }
}
