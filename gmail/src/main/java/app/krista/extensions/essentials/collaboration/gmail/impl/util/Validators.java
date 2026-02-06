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


import org.apache.commons.validator.routines.EmailValidator;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Utility class for validating email addresses and string inputs.
 * Provides static methods for email format validation and null/blank string checking.
 * Uses Apache Commons EmailValidator for robust email validation and includes
 * email extraction capabilities for parsing email addresses from formatted strings.
 */
public class Validators {

    /**
     * Private constructor to prevent instantiation.
     * This is a utility class with only static methods.
     */
    private Validators() {
    }

    /**
     * Validates if an email address has a valid format.
     * Extracts the email address from the input string and validates it using Apache Commons EmailValidator.
     * Handles cases where email addresses are embedded in formatted strings like "Name <email@domain.com>".
     *
     * @param emailAddress the email address string to validate
     * @return true if the email address is valid, false otherwise
     */
    public static boolean isEmailValid(String emailAddress) {
        EmailValidator emailValidator = EmailValidator.getInstance();
        return emailValidator.isValid(emailExtractor(emailAddress));
    }

    /**
     * Checks if a string is null or blank.
     * Useful for validating required string inputs before processing.
     *
     * @param emailAddress the string to check (parameter name suggests email but works for any string)
     * @return true if the string is null or blank, false otherwise
     */
    public static boolean isStringNullOrBlank(String emailAddress) {
        return (emailAddress == null || emailAddress.isBlank());
    }

    /**
     * Extracts email address from a formatted string using regex pattern matching.
     * Handles cases where email addresses are embedded in formatted strings like "Name <email@domain.com>"
     * or mixed with other text. Returns the original string if no email pattern is found.
     *
     * @param email the input string that may contain an email address
     * @return the extracted email address, or the original string if no email pattern is found
     */
    private static String emailExtractor(String email) {
        String emailPattern = "[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}";
        Pattern pattern = Pattern.compile(emailPattern);
        Matcher matcher = pattern.matcher(email);
        if (matcher.find()) {
            return matcher.group();
        }
        return email;
    }
}
