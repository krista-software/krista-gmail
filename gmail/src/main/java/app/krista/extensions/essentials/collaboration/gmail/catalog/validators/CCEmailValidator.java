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

package app.krista.extensions.essentials.collaboration.gmail.catalog.validators;

import app.krista.extensions.essentials.collaboration.gmail.catalog.extresp.FieldTypes;
import app.krista.extensions.essentials.collaboration.gmail.catalog.extresp.ValidationResourceUtil;
import app.krista.extensions.essentials.collaboration.gmail.resources.GmailResources;
import app.krista.extensions.essentials.collaboration.gmail.service.EmailAddress;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Validator for "CC" (Carbon Copy) email addresses in Gmail operations.
 * Validates email address format and collects invalid addresses for error reporting.
 */
public class CCEmailValidator implements Validator {

    private static final List<EmailAddress> emailAddresses = new ArrayList<>();

    /**
     * Validates that all CC email addresses have valid format.
     * Returns true if no invalid email addresses are found.
     *
     * @param resourceId comma-separated string of email addresses to validate
     * @param context    validation context (not used for email validation)
     * @return true if all email addresses are valid, false if any are invalid
     */
    @Override
    public Boolean validate(String resourceId, Map<ValidationResource, String> context) {
        try {
            return ValidationResourceUtil.toEmailAddresses(resourceId, emailAddresses).isEmpty();
        } catch (RuntimeException cause) {
            return false;
        }
    }

    @Override
    public String getFetchFieldName() {
        return GmailResources.CC;
    }

    @Override
    public String getFieldType() {
        return FieldTypes.TEXT_FIELD;
    }

    @Override
    public String getFetchStepMessage() {
        return "Please enter a valid email address for CC recipients in the format: user@domain.com";
    }

    @Override
    public String getConfirmationStepMessage(String resourceId, Map<ValidationResource, String> context) {
        return String.format("The CC email address '%s' could not be validated. Please verify the address and try again.", toStringMailIds());
    }

    @Override
    public String getErrMessage(String resourceId) {
        ValidationResourceUtil.toEmailAddresses(resourceId, emailAddresses);
        return String.format("The 'CC' email addresses are not valid: %s. Please check the format and try again.", toStringMailIds());
    }

    /**
     * Converts collected invalid email addresses to a comma-separated string.
     * Clears the emailAddresses list after creating the string.
     *
     * @return comma-separated string of invalid email addresses
     */
    private static String toStringMailIds() {
        String mailIds = CCEmailValidator.emailAddresses.stream()
                .map(EmailAddress::getEmailAddress)
                .collect(Collectors.joining(", "));
        CCEmailValidator.emailAddresses.clear();
        return mailIds;
    }
}