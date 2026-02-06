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

import java.util.Map;

/**
 * Validator for page number parameters in Gmail operations.
 * Ensures page number values are within the valid range of 1-15.
 */
public class PageNumberValidator implements Validator {

    /**
     * Validates that the page number is a valid number within the allowed range.
     *
     * @param resourceId the page number value to validate
     * @param context    validation context (not used for page number validation)
     * @return true if page number is valid (1-15), false otherwise
     */
    @Override
    public Boolean validate(String resourceId, Map<ValidationResource, String> context) {
        try {
            return isNumberValid(resourceId);
        } catch (RuntimeException cause) {
            return false;
        }
    }

    /**
     * Checks if the page number is within the valid range.
     *
     * @param resourceId the page number value as string
     * @return true if the value is greater than 0 and less than or equal to 15
     */
    private Boolean isNumberValid(String resourceId) {
        double number = Double.parseDouble(resourceId);
        return number > 0 && number <= 15;//Maximum limit for Page Number
    }

    @Override
    public String getFetchFieldName() {
        return GmailResources.PAGE_NUMBER;
    }

    @Override
    public String getFieldType() {
        return FieldTypes.NUMBER_FIELD;
    }

    @Override
    public String getFetchStepMessage() {
        return "Please enter a valid page number (1-15). This determines which page of results to retrieve.";
    }

    @Override
    public String getConfirmationStepMessage(String resourceId, Map<ValidationResource, String> context) {
        return String.format("The provided Page number : %s should be greater than 0 and less than or equal to 15.",
                ValidationResourceUtil.removeTrailingZeros(Double.parseDouble(resourceId)));
    }

    @Override
    public String getErrMessage(String resourceId) {
        return String.format("The provided Page number : %s should be greater than 0 and less than or equal to 15.",
                ValidationResourceUtil.removeTrailingZeros(Double.parseDouble(resourceId)));
    }
}