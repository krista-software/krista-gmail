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

package app.krista.extensions.essentials.collaboration.gmail.catalog.extresp;

import app.krista.extensions.essentials.collaboration.gmail.catalog.validators.Validator;
import app.krista.extensions.essentials.collaboration.gmail.service.EmailAddress;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for ValidationResourceUtil.
 * Tests validation map preparation and utility methods.
 */
@DisplayName("ValidationResourceUtil Tests")
class ValidationResourceUtilTest {

    @Test
    @DisplayName("Should prepare label validation map with all parameters")
    void testPrepareLabelMapWithAllParams() {
        Map<Validator.ValidationResource, String> result =
                ValidationResourceUtil.prepareValidateLabelMap("INBOX", 5.0, 10.0);

        assertThat(result).isNotNull();
        assertThat(result.get(Validator.ValidationResource.LABEL)).isEqualTo("INBOX");
        assertThat(result.get(Validator.ValidationResource.PAGE_NUMBER)).isEqualTo("5.0");
        assertThat(result.get(Validator.ValidationResource.PAGE_SIZE)).isEqualTo("10.0");
    }

    @Test
    @DisplayName("Should prepare label validation map with null pagination")
    void testPrepareLabelMapWithNullPagination() {
        Map<Validator.ValidationResource, String> result =
                ValidationResourceUtil.prepareValidateLabelMap("Work", null, null);

        assertThat(result).isNotNull();
        assertThat(result.get(Validator.ValidationResource.LABEL)).isEqualTo("Work");
        assertThat(result.get(Validator.ValidationResource.PAGE_NUMBER)).isEqualTo("1");
        assertThat(result.get(Validator.ValidationResource.PAGE_SIZE)).isEqualTo("1");
    }

    @Test
    @DisplayName("Should prepare fetch inbox map with valid parameters (empty map)")
    void testPrepareFetchInboxMapWithValidParams() {
        Map<Validator.ValidationResource, String> result =
                ValidationResourceUtil.prepareValidateFetchInboxMap(5.0, 10.0);

        // Valid parameters (1-15) should result in empty map
        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("Should prepare fetch inbox map with invalid page number")
    void testPrepareFetchInboxMapWithInvalidPageNumber() {
        Map<Validator.ValidationResource, String> result =
                ValidationResourceUtil.prepareValidateFetchInboxMap(20.0, 10.0);

        assertThat(result).isNotEmpty();
        assertThat(result.get(Validator.ValidationResource.PAGE_NUMBER)).isEqualTo("20.0");
        assertThat(result.get(Validator.ValidationResource.PAGE_SIZE)).isNull();
    }

    @Test
    @DisplayName("Should prepare fetch inbox map with invalid page size")
    void testPrepareFetchInboxMapWithInvalidPageSize() {
        Map<Validator.ValidationResource, String> result =
                ValidationResourceUtil.prepareValidateFetchInboxMap(5.0, 20.0);

        assertThat(result).isNotEmpty();
        assertThat(result.get(Validator.ValidationResource.PAGE_NUMBER)).isNull();
        assertThat(result.get(Validator.ValidationResource.PAGE_SIZE)).isEqualTo("20.0");
    }

    @Test
    @DisplayName("Should prepare fetch inbox map with both invalid parameters")
    void testPrepareFetchInboxMapWithBothInvalid() {
        Map<Validator.ValidationResource, String> result =
                ValidationResourceUtil.prepareValidateFetchInboxMap(0.0, 20.0);

        assertThat(result).isNotEmpty();
        assertThat(result.get(Validator.ValidationResource.PAGE_NUMBER)).isEqualTo("0.0");
        assertThat(result.get(Validator.ValidationResource.PAGE_SIZE)).isEqualTo("20.0");
    }

    @Test
    @DisplayName("Should prepare fetch inbox map with null parameters (empty map)")
    void testPrepareFetchInboxMapWithNullParams() {
        Map<Validator.ValidationResource, String> result =
                ValidationResourceUtil.prepareValidateFetchInboxMap(null, null);

        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("Should remove trailing zeros from double values")
    void testRemoveTrailingZeros() {
        assertThat(ValidationResourceUtil.removeTrailingZeros(5.0)).isEqualTo("5");
        assertThat(ValidationResourceUtil.removeTrailingZeros(10.0)).isEqualTo("10");
        assertThat(ValidationResourceUtil.removeTrailingZeros(5.5)).isEqualTo("5.5");
        assertThat(ValidationResourceUtil.removeTrailingZeros(10.50)).isEqualTo("10.5");
        assertThat(ValidationResourceUtil.removeTrailingZeros(1.0)).isEqualTo("1");
        assertThat(ValidationResourceUtil.removeTrailingZeros(15.0)).isEqualTo("15");
    }

    @Test
    @DisplayName("Should handle edge cases for trailing zeros")
    void testRemoveTrailingZerosEdgeCases() {
        assertThat(ValidationResourceUtil.removeTrailingZeros(0.0)).isEqualTo("0");
        assertThat(ValidationResourceUtil.removeTrailingZeros(0.1)).isEqualTo("0.1");
        assertThat(ValidationResourceUtil.removeTrailingZeros(100.0)).isEqualTo("100");
    }

    @Test
    @DisplayName("Should parse valid email addresses")
    void testToEmailAddressesWithValidEmails() {
        List<EmailAddress> invalidEmails = new ArrayList<>();
        List<EmailAddress> result = ValidationResourceUtil.toEmailAddresses(
                "user@example.com", invalidEmails);

        assertThat(result).isEmpty(); // Valid emails return empty list
    }

    @Test
    @DisplayName("Should detect invalid email addresses")
    void testToEmailAddressesWithInvalidEmails() {
        List<EmailAddress> invalidEmails = new ArrayList<>();
        List<EmailAddress> result = ValidationResourceUtil.toEmailAddresses(
                "invalid-email", invalidEmails);

        assertThat(invalidEmails).isNotEmpty();
    }

    @Test
    @DisplayName("Should handle null email string")
    void testToEmailAddressesWithNull() {
        List<EmailAddress> invalidEmails = new ArrayList<>();
        List<EmailAddress> result = ValidationResourceUtil.toEmailAddresses(null, invalidEmails);

        assertThat(result).isEmpty();
        assertThat(invalidEmails).isEmpty();
    }

    @Test
    @DisplayName("Should handle blank email string")
    void testToEmailAddressesWithBlank() {
        List<EmailAddress> invalidEmails = new ArrayList<>();
        List<EmailAddress> result = ValidationResourceUtil.toEmailAddresses("   ", invalidEmails);

        assertThat(result).isEmpty();
        assertThat(invalidEmails).isEmpty();
    }

    @Test
    @DisplayName("Should handle multiple email addresses")
    void testToEmailAddressesWithMultiple() {
        List<EmailAddress> invalidEmails = new ArrayList<>();
        List<EmailAddress> result = ValidationResourceUtil.toEmailAddresses(
                "user1@example.com,user2@example.com", invalidEmails);

        assertThat(result).isEmpty(); // All valid
    }

    @Test
    @DisplayName("Should detect mixed valid and invalid emails")
    void testToEmailAddressesWithMixed() {
        List<EmailAddress> invalidEmails = new ArrayList<>();
        List<EmailAddress> result = ValidationResourceUtil.toEmailAddresses(
                "user@example.com,invalid-email,another@test.com", invalidEmails);

        assertThat(invalidEmails).hasSize(1); // One invalid email
    }

    @Test
    @DisplayName("Should handle boundary values for page number validation")
    void testBoundaryValuesForPageNumber() {
        // Exactly 1 - valid
        Map<Validator.ValidationResource, String> result1 =
                ValidationResourceUtil.prepareValidateFetchInboxMap(1.0, 10.0);
        assertThat(result1).isEmpty();

        // Exactly 15 - valid
        Map<Validator.ValidationResource, String> result2 =
                ValidationResourceUtil.prepareValidateFetchInboxMap(15.0, 10.0);
        assertThat(result2).isEmpty();

        // Just below 1 - invalid
        Map<Validator.ValidationResource, String> result3 =
                ValidationResourceUtil.prepareValidateFetchInboxMap(0.9, 10.0);
        assertThat(result3).isNotEmpty();

        // Just above 15 - invalid
        Map<Validator.ValidationResource, String> result4 =
                ValidationResourceUtil.prepareValidateFetchInboxMap(15.1, 10.0);
        assertThat(result4).isNotEmpty();
    }

    @Test
    @DisplayName("Should handle boundary values for page size validation")
    void testBoundaryValuesForPageSize() {
        // Exactly 1 - valid
        Map<Validator.ValidationResource, String> result1 =
                ValidationResourceUtil.prepareValidateFetchInboxMap(5.0, 1.0);
        assertThat(result1).isEmpty();

        // Exactly 15 - valid
        Map<Validator.ValidationResource, String> result2 =
                ValidationResourceUtil.prepareValidateFetchInboxMap(5.0, 15.0);
        assertThat(result2).isEmpty();

        // Just below 1 - invalid
        Map<Validator.ValidationResource, String> result3 =
                ValidationResourceUtil.prepareValidateFetchInboxMap(5.0, 0.9);
        assertThat(result3).isNotEmpty();

        // Just above 15 - invalid
        Map<Validator.ValidationResource, String> result4 =
                ValidationResourceUtil.prepareValidateFetchInboxMap(5.0, 15.1);
        assertThat(result4).isNotEmpty();
    }
}

