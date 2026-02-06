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
import app.krista.extensions.essentials.collaboration.gmail.resources.GmailResources;
import app.krista.extensions.essentials.collaboration.gmail.service.Account;
import app.krista.extensions.essentials.collaboration.gmail.service.Email;

import java.util.List;
import java.util.Map;

public class QueryValidator implements Validator {
    private final Account account;

    public QueryValidator(Account account) {
        this.account = account;
    }

    /**
     * Validates that the query is not null, not empty, and not just whitespace.
     *
     * @param resourceId the query string to validate
     * @param context    validation context (not used for query validation)
     * @return true if the query is valid, false otherwise
     */
    @Override
    public Boolean validate(String resourceId, Map<ValidationResource, String> context) {
        List<Email> emails = account.searchEmails(resourceId);
        if (emails.isEmpty()) {
            return false;
        }
        return resourceId != null && !resourceId.trim().isEmpty();

    }

    @Override
    public String getFetchFieldName() {
        return GmailResources.QUERY;
    }

    @Override
    public String getFieldType() {
        return FieldTypes.TEXT_FIELD;
    }

    @Override
    public String getFetchStepMessage() {
        return "Please enter a valid search query.";
    }

    @Override
    public String getConfirmationStepMessage(String resourceId, Map<ValidationResource, String> context) {
        return "The search query cannot be empty. Please provide a valid search query.";
    }

    @Override
    public String getErrMessage(String resourceId) {
        return "Invalid query: Query cannot be empty or null.";
    }
}