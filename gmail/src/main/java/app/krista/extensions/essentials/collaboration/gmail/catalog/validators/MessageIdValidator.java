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

import app.krista.extension.authorization.MustAuthorizeException;
import app.krista.extensions.essentials.collaboration.gmail.catalog.extresp.FieldTypes;
import app.krista.extensions.essentials.collaboration.gmail.resources.GmailResources;
import app.krista.extensions.essentials.collaboration.gmail.service.Account;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.Set;

/**
 * Validator for Gmail message IDs.
 * Verifies that message IDs exist in the user's Gmail account.
 */
public class MessageIdValidator implements Validator {

    private final Account account;

    private static final Logger logger = LoggerFactory.getLogger(MessageIdValidator.class);

    /**
     * Constructs a message ID validator with the specified Gmail account.
     *
     * @param account the Gmail account to validate message IDs against
     */
    public MessageIdValidator(Account account) {
        this.account = account;
    }

    /**
     * Validates that the message ID exists in the user's Gmail account.
     *
     * @param resourceId the message ID to validate
     * @param context    validation context (not used for message ID validation)
     * @return true if the message ID exists in the account, false otherwise
     */
    @Override
    public Boolean validate(String resourceId, Map<ValidationResource, String> context) {
        try {
            Set<String> messageIds = account.fetchAllMessageIds();
            return !messageIds.isEmpty() && messageIds.contains(resourceId);
        } catch (MustAuthorizeException cause) {
            logger.error("Exception thrown while authorizing user : {} {}", cause.getMessage(), cause);
            throw cause;
        } catch (Exception cause) {
            logger.error("Exception thrown while validating message ID: {}", cause.getMessage(), cause);
            return false;
        }
    }

    @Override
    public String getFetchFieldName() {
        return GmailResources.MESSAGE_ID;
    }

    @Override
    public String getFieldType() {
        return FieldTypes.TEXT_FIELD;
    }

    @Override
    public String getFetchStepMessage() {
        return "Please enter a valid Gmail message ID (a unique identifier for the email).";
    }

    @Override
    public String getConfirmationStepMessage(String resourceId, Map<ValidationResource, String> context) {
        return String.format("No email found with Message ID: %s. Please verify the ID is correct and the email exists in your account.", resourceId);
    }

    @Override
    public String getErrMessage(String resourceId) {
        return String.format("The Message ID '%s' is not valid or does not exist in your Gmail account.", resourceId);
    }
}