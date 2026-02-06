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
import app.krista.extensions.essentials.collaboration.gmail.impl.util.Constants;
import app.krista.extensions.essentials.collaboration.gmail.impl.util.Validators;
import app.krista.extensions.essentials.collaboration.gmail.service.EmailAddress;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Utility class for preparing validation resource maps and formatting numeric values.
 * Provides methods to create validation contexts for different Gmail operations.
 */
public class ValidationResourceUtil {

    private ValidationResourceUtil() {
    }

    /**
     * Prepares validation map for label-based operations.
     * Includes label and optional pagination parameters for validation.
     *
     * @param label      the Gmail label to validate
     * @param pageNumber page number for pagination (can be null)
     * @param pageSize   number of items per page (can be null)
     * @return Map containing validation resources for label operations
     */
    public static Map<Validator.ValidationResource, String> prepareValidateLabelMap(String label, Double pageNumber, Double pageSize) {
        Map<Validator.ValidationResource, String> map = new HashMap<>();
        map.put(Validator.ValidationResource.LABEL, label);
        map.put(Validator.ValidationResource.PAGE_NUMBER, pageNumber != null ? pageNumber.toString() : "1");
        map.put(Validator.ValidationResource.PAGE_SIZE, pageSize != null ? pageSize.toString() : "1");
        return map;
    }

    /**
     * Checks if a Double value is not null.
     *
     * @param input the Double value to check
     * @return true if the input is not null, false otherwise
     */
    private static boolean isNotNull(Double input) {
        return input != null;
    }

    /**
     * Prepares validation map for inbox fetch operations.
     * Only includes pagination parameters that are outside the valid range (1-15).
     * This method filters out valid values and only adds invalid ones for validation.
     *
     * @param pageNumber page number to validate (valid range: 1-15)
     * @param pageSize   number of items per page to validate (valid range: 1-15)
     * @return Map containing only invalid pagination parameters for validation
     */
    public static Map<Validator.ValidationResource, String> prepareValidateFetchInboxMap(Double pageNumber, Double pageSize) {
        Map<Validator.ValidationResource, String> map = new HashMap<>();

        // Add page number to validation map if it's OUTSIDE valid range (1-15 inclusive)
        if (isNotNull(pageNumber) && (pageNumber < 1 || pageNumber > 15)) {
            map.put(Validator.ValidationResource.PAGE_NUMBER, pageNumber.toString());
        }

        // Add page size to validation map if it's OUTSIDE valid range (1-15 inclusive)
        if (isNotNull(pageSize) && (pageSize < 1 || pageSize > 15)) {
            map.put(Validator.ValidationResource.PAGE_SIZE, pageSize.toString());
        }
        return map;
    }

    /**
     * Removes trailing zeros from a double value's string representation.
     * Converts decimal numbers to their cleanest string form (e.g., 5.0 becomes "5").
     *
     * @param number the double value to format
     * @return String representation with trailing zeros removed
     */
    public static String removeTrailingZeros(double number) {
        return new BigDecimal(String.valueOf(number))
                .stripTrailingZeros()
                .toPlainString();
    }

    /**
     * Parses and validates a comma-separated string of email addresses.
     * Collects invalid email addresses in the static emailAddresses list.
     *
     * @param emailAddressesString comma-separated email addresses
     * @return list of invalid EmailAddress objects (empty if all valid)
     */
    public static List<EmailAddress> toEmailAddresses(String emailAddressesString, List<EmailAddress> emailAddresses) {
        if (emailAddressesString == null || emailAddressesString.isBlank()) {
            return List.of();
        }
        for (String emailAddressString : emailAddressesString.split(Constants.COMMA)) {
            if (Validators.isStringNullOrBlank(emailAddressString) || !Validators.isEmailValid(emailAddressString)) {
                emailAddresses.add(new EmailAddress(Constants.EMPTY_STRING, emailAddressString));
            }
        }
        return emailAddresses;
    }
}
