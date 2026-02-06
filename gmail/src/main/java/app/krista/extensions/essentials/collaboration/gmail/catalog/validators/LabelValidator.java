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
import app.krista.extensions.essentials.collaboration.gmail.service.Folder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

/**
 * Validator for Gmail labels in Gmail operations.
 * Verifies that label names exist in the user's Gmail account.
 * Labels in Gmail are equivalent to folders and are used for organizing emails.
 */
public class LabelValidator implements Validator {

    private final Account account;
    private static final Logger logger = LoggerFactory.getLogger(LabelValidator.class);

    /**
     * Constructs a label validator with the specified Gmail account.
     *
     * @param account the Gmail account to validate labels against
     */
    public LabelValidator(Account account) {
        this.account = account;
    }

    /**
     * Validates that the label exists in the user's Gmail account.
     * Searches for the label by name in the account's folder/label list.
     *
     * @param resourceId the label name to validate
     * @param context    validation context (not used for label validation)
     * @return true if the label exists in the account, false otherwise
     */
    @Override
    public Boolean validate(String resourceId, Map<ValidationResource, String> context) {
        try {
            if (resourceId == null || resourceId.trim().isEmpty()) {
                logger.info("Label name is null or empty");
                return false;
            }

            Folder folder = account.getFolderByName(resourceId.trim());
            return folder != null;
        } catch (MustAuthorizeException cause) {
            logger.info("Authorization required for label validation: {}", cause.getMessage());
            throw cause;
        } catch (Exception cause) {
            logger.error("Error validating label '{}': {}", resourceId, cause.getMessage());
            return false;
        }
    }

    @Override
    public String getFetchFieldName() {
        return GmailResources.LABEL;
    }

    @Override
    public String getFieldType() {
        return FieldTypes.TEXT_FIELD;
    }

    @Override
    public String getFetchStepMessage() {
        return "Please enter a valid Gmail label name (e.g., Important, Work, or a custom label you've created).";
    }

    @Override
    public String getConfirmationStepMessage(String resourceId, Map<ValidationResource, String> context) {
        return String.format("The provided label '%s' does not exist in your Gmail account.", resourceId);
    }

    @Override
    public String getErrMessage(String resourceId) {
        return String.format("Invalid label name: '%s'. Please check that the label exists in your Gmail account.", resourceId);
    }
}