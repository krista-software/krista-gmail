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
 * Validator for Gmail folder names.
 * Verifies that folder names exist in the user's Gmail account.
 */
public class FolderNameValidator implements Validator {

    private final Account account;

    private static final Logger logger = LoggerFactory.getLogger(FolderNameValidator.class);

    /**
     * Constructs a folder name validator with the specified Gmail account.
     *
     * @param account the Gmail account to validate folder names against
     */
    public FolderNameValidator(Account account) {
        this.account = account;
    }

    /**
     * Validates that the folder name exists in the user's Gmail account.
     *
     * @param resourceId the folder name to validate
     * @param context    validation context (not used for folder name validation)
     * @return true if the folder exists in the account, false otherwise
     */
    @Override
    public Boolean validate(String resourceId, Map<ValidationResource, String> context) {
        try {
            Folder folder = account.getFolderByName(resourceId);
            return folder != null;
        } catch (MustAuthorizeException cause) {
            logger.info(cause.getMessage());
            throw cause;
        } catch (Exception cause) {
            logger.info(cause.getMessage());
            return false;
        }
    }

    @Override
    public String getFetchFieldName() {
        return GmailResources.FOLDER_NAME;
    }

    @Override
    public String getFieldType() {
        return FieldTypes.TEXT_FIELD;
    }

    @Override
    public String getFetchStepMessage() {
        return "Please enter a valid Gmail folder name (e.g., INBOX, SENT, DRAFT, or a custom folder name).";
    }

    @Override
    public String getConfirmationStepMessage(String resourceId, Map<ValidationResource, String> context) {
        return String.format("The folder '%s' does not exist in your Gmail account. Please check the name and try again.", resourceId);
    }

    @Override
    public String getErrMessage(String resourceId) {
        return String.format("The folder name '%s' is not valid or does not exist in your Gmail account.", resourceId);
    }
}