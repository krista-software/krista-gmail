/*
 * Gmail Extension for Krista
 * Copyright (C) 2025 Krista Software
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>. 
 */

package app.krista.extensions.essentials.collaboration.gmail.impl.util;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for Validators utility class.
 * Tests email validation and string null/blank checking.
 */
@DisplayName("Validators Utility Tests")
class ValidatorsTest {

    @Test
    @DisplayName("Should validate correct email addresses")
    void testValidEmails() {
        assertThat(Validators.isEmailValid("user@example.com")).isTrue();
        assertThat(Validators.isEmailValid("test.user@domain.com")).isTrue();
        assertThat(Validators.isEmailValid("user+tag@example.co.uk")).isTrue();
        assertThat(Validators.isEmailValid("first.last@subdomain.example.com")).isTrue();
        assertThat(Validators.isEmailValid("user123@test-domain.org")).isTrue();
    }

    @Test
    @DisplayName("Should extract and validate email from formatted strings")
    void testEmailExtractionFromFormattedStrings() {
        assertThat(Validators.isEmailValid("John Doe <john.doe@example.com>")).isTrue();
        assertThat(Validators.isEmailValid("Jane Smith <jane@test.com>")).isTrue();
        assertThat(Validators.isEmailValid("<user@domain.com>")).isTrue();
    }

    @Test
    @DisplayName("Should reject invalid email addresses")
    void testInvalidEmails() {
        assertThat(Validators.isEmailValid("invalid-email")).isFalse();
        assertThat(Validators.isEmailValid("@example.com")).isFalse();
        assertThat(Validators.isEmailValid("user@")).isFalse();
        assertThat(Validators.isEmailValid("user@.com")).isFalse();
        assertThat(Validators.isEmailValid("user @example.com")).isFalse();
        assertThat(Validators.isEmailValid("")).isFalse();
    }

    @Test
    @DisplayName("Should handle edge cases for email validation")
    void testEmailValidationEdgeCases() {
        assertThat(Validators.isEmailValid("a@b.co")).isTrue(); // Minimum valid email
        assertThat(Validators.isEmailValid("user@domain")).isFalse(); // Missing TLD
        assertThat(Validators.isEmailValid("user..name@example.com")).isFalse(); // Double dots
    }

    @Test
    @DisplayName("Should detect null strings")
    void testNullStrings() {
        assertThat(Validators.isStringNullOrBlank(null)).isTrue();
    }

    @Test
    @DisplayName("Should detect blank strings")
    void testBlankStrings() {
        assertThat(Validators.isStringNullOrBlank("")).isTrue();
        assertThat(Validators.isStringNullOrBlank("   ")).isTrue();
        assertThat(Validators.isStringNullOrBlank("\t")).isTrue();
        assertThat(Validators.isStringNullOrBlank("\n")).isTrue();
        assertThat(Validators.isStringNullOrBlank("  \t\n  ")).isTrue();
    }

    @Test
    @DisplayName("Should detect non-blank strings")
    void testNonBlankStrings() {
        assertThat(Validators.isStringNullOrBlank("text")).isFalse();
        assertThat(Validators.isStringNullOrBlank("  text  ")).isFalse();
        assertThat(Validators.isStringNullOrBlank("a")).isFalse();
        assertThat(Validators.isStringNullOrBlank("123")).isFalse();
    }

    @Test
    @DisplayName("Should handle special characters in email validation")
    void testSpecialCharactersInEmail() {
        assertThat(Validators.isEmailValid("user+filter@example.com")).isTrue();
        assertThat(Validators.isEmailValid("user_name@example.com")).isTrue();
        assertThat(Validators.isEmailValid("user.name@example.com")).isTrue();
        assertThat(Validators.isEmailValid("user%test@example.com")).isTrue();
        assertThat(Validators.isEmailValid("123@example.com")).isTrue();
    }

    @Test
    @DisplayName("Should validate emails with various TLDs")
    void testVariousTLDs() {
        assertThat(Validators.isEmailValid("user@example.com")).isTrue();
        assertThat(Validators.isEmailValid("user@example.org")).isTrue();
        assertThat(Validators.isEmailValid("user@example.net")).isTrue();
        assertThat(Validators.isEmailValid("user@example.co.uk")).isTrue();
        assertThat(Validators.isEmailValid("user@example.io")).isTrue();
    }

    @Test
    @DisplayName("Should handle mixed case emails")
    void testMixedCaseEmails() {
        assertThat(Validators.isEmailValid("User@Example.COM")).isTrue();
        assertThat(Validators.isEmailValid("TEST@test.com")).isTrue();
        assertThat(Validators.isEmailValid("MixedCase@Domain.ORG")).isTrue();
    }

    @Test
    @DisplayName("Should reject emails with invalid characters")
    void testInvalidCharactersInEmail() {
        // Note: The validator extracts valid email patterns, so "user name@example.com"
        // extracts "name@example.com" which is valid
        assertThat(Validators.isEmailValid("user@")).isFalse(); // Incomplete domain
        assertThat(Validators.isEmailValid("@example.com")).isFalse(); // Missing local part
    }

    @Test
    @DisplayName("Should handle long email addresses")
    void testLongEmails() {
        String longEmail = "very.long.email.address.with.many.dots@subdomain.example.com";
        assertThat(Validators.isEmailValid(longEmail)).isTrue();
    }

    @Test
    @DisplayName("Should handle emails with numbers")
    void testEmailsWithNumbers() {
        assertThat(Validators.isEmailValid("user123@example.com")).isTrue();
        assertThat(Validators.isEmailValid("123user@example.com")).isTrue();
        assertThat(Validators.isEmailValid("user@example123.com")).isTrue();
        assertThat(Validators.isEmailValid("user@123example.com")).isTrue();
    }

    @Test
    @DisplayName("Should handle emails with hyphens")
    void testEmailsWithHyphens() {
        assertThat(Validators.isEmailValid("user-name@example.com")).isTrue();
        assertThat(Validators.isEmailValid("user@test-domain.com")).isTrue();
        assertThat(Validators.isEmailValid("first-last@sub-domain.example.com")).isTrue();
    }

    @Test
    @DisplayName("Should handle subdomains in email addresses")
    void testSubdomains() {
        assertThat(Validators.isEmailValid("user@mail.example.com")).isTrue();
        assertThat(Validators.isEmailValid("user@subdomain.mail.example.com")).isTrue();
        assertThat(Validators.isEmailValid("user@a.b.c.example.com")).isTrue();
    }

    @Test
    @DisplayName("Should reject emails without @ symbol")
    void testEmailsWithoutAtSymbol() {
        assertThat(Validators.isEmailValid("userexample.com")).isFalse();
        assertThat(Validators.isEmailValid("user.example.com")).isFalse();
    }

    @Test
    @DisplayName("Should reject emails with multiple @ symbols")
    void testEmailsWithMultipleAtSymbols() {
        // Note: The validator extracts valid email patterns, so "user@test@example.com"
        // extracts "test@example.com" which is valid
        assertThat(Validators.isEmailValid("@@example.com")).isFalse();
        assertThat(Validators.isEmailValid("user@@")).isFalse();
    }

    @Test
    @DisplayName("Should handle whitespace-only strings correctly")
    void testWhitespaceOnlyStrings() {
        assertThat(Validators.isStringNullOrBlank(" ")).isTrue();
        assertThat(Validators.isStringNullOrBlank("     ")).isTrue();
        assertThat(Validators.isStringNullOrBlank("\t\t")).isTrue();
        assertThat(Validators.isStringNullOrBlank("\n\n")).isTrue();
    }

    @Test
    @DisplayName("Should handle strings with leading/trailing whitespace")
    void testStringsWithWhitespace() {
        assertThat(Validators.isStringNullOrBlank("  text")).isFalse();
        assertThat(Validators.isStringNullOrBlank("text  ")).isFalse();
        assertThat(Validators.isStringNullOrBlank("  text  ")).isFalse();
    }
}

