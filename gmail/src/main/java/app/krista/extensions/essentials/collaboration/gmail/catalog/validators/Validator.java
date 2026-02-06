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

import java.util.Map;

/**
 * Interface defining the contract for resource validators.
 * Provides validation logic and user feedback messages for different resource types.
 */
public interface Validator {
    enum ValidationResource {
        MESSAGE_ID, TO, CC, BCC, REPLY_TO, FOLDER_NAME, LABEL, PAGE_NUMBER, PAGE_SIZE, QUERY
    }

    /**
     * Validates a resource value within the given context.
     *
     * @param resourceId the value to validate
     * @param context    map containing all validation resources for contextual validation
     * @return true if the resource is valid, false otherwise
     */
    Boolean validate(String resourceId, Map<ValidationResource, String> context);

    /**
     * Gets the field name used for fetching user input.
     *
     * @return the field name for user input forms
     */
    String getFetchFieldName();

    /**
     * Gets the field type for UI rendering.
     *
     * @return the field type identifier (e.g., TEXT_FIELD, NUMBER_FIELD)
     */
    String getFieldType();

    /**
     * Gets the message to display when asking user to provide valid input.
     *
     * @return user-friendly message for input prompts
     */
    String getFetchStepMessage();

    /**
     * Gets the confirmation message explaining what needs to be corrected.
     *
     * @param resourceId the invalid resource value
     * @param context    map containing all validation resources for context
     * @return detailed confirmation message for the user
     */
    String getConfirmationStepMessage(String resourceId, Map<ValidationResource, String> context);

    /**
     * Gets the error message describing why validation failed.
     *
     * @param resourceId the invalid resource value
     * @return error message explaining the validation failure
     */
    String getErrMessage(String resourceId);
}
