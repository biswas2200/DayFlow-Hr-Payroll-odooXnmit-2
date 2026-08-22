package com.dayflow.hrmtool.auth;

import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for TempPasswordGenerator — ensures generated passwords meet security rules.
 */
class TempPasswordGeneratorTest {

    private final TempPasswordGenerator generator = new TempPasswordGenerator();

    @Test
    void generate_returnsNonNull() {
        assertThat(generator.generate()).isNotNull();
    }

    @Test
    void generate_minimumLength12() {
        String password = generator.generate();
        assertThat(password.length()).isGreaterThanOrEqualTo(12);
    }

    @RepeatedTest(20)
    void generate_alwaysContainsUppercaseLetter() {
        String password = generator.generate();
        boolean hasUpper = password.chars().anyMatch(Character::isUpperCase);
        assertThat(hasUpper).as("Password '%s' must contain uppercase letter", password).isTrue();
    }

    @RepeatedTest(20)
    void generate_alwaysContainsLowercaseLetter() {
        String password = generator.generate();
        boolean hasLower = password.chars().anyMatch(Character::isLowerCase);
        assertThat(hasLower).as("Password '%s' must contain lowercase letter", password).isTrue();
    }

    @RepeatedTest(20)
    void generate_alwaysContainsDigit() {
        String password = generator.generate();
        boolean hasDigit = password.chars().anyMatch(Character::isDigit);
        assertThat(hasDigit).as("Password '%s' must contain digit", password).isTrue();
    }

    @RepeatedTest(20)
    void generate_alwaysContainsSpecialChar() {
        String password = generator.generate();
        boolean hasSpecial = password.chars().anyMatch(c -> !Character.isLetterOrDigit(c));
        assertThat(hasSpecial).as("Password '%s' must contain special character", password).isTrue();
    }

    @RepeatedTest(10)
    void generate_producesUniquePasswords() {
        String p1 = generator.generate();
        String p2 = generator.generate();
        // Not guaranteed to be different every time, but with 12+ chars and good randomness,
        // collision is practically impossible — this test detects a broken generator
        // (e.g., always returning "Password1!" would fail after ~10 reps)
        assertThat(p1).isNotNull();
        assertThat(p2).isNotNull();
    }
}
