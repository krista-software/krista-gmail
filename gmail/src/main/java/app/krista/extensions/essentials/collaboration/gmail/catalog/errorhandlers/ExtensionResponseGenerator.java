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

package app.krista.extensions.essentials.collaboration.gmail.catalog.errorhandlers;


import app.krista.extension.executor.ExtensionResponse;
import app.krista.extensions.essentials.collaboration.gmail.catalog.MessagingAreaSubCatalogRequests;
import app.krista.extensions.essentials.collaboration.gmail.catalog.extresp.ExtensionResponseFactory;
import app.krista.extensions.essentials.collaboration.gmail.catalog.extresp.NamedFieldFactory;
import app.krista.extensions.essentials.collaboration.gmail.catalog.extresp.RemediationActionFactory;
import app.krista.extensions.essentials.collaboration.gmail.catalog.validators.ValidationOrchestrator;
import app.krista.model.field.NamedField;
import org.jvnet.hk2.annotations.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.StringJoiner;

/**
 * Generates extension responses for error handling and validation workflows.
 * Creates confirmation, fetch, and denial responses based on validation results.
 */
@Service
public class ExtensionResponseGenerator {

    public static final String ERROR_MESSAGE = "ErrorMessage";
    public static final String STEP_MESSAGE = "StepMessage";
    public static final String FIELD = "Field";
    private static final Logger LOGGER = LoggerFactory.getLogger(ExtensionResponseGenerator.class);

    /**
     * Generates a confirmation response for validation errors.
     * Creates an ask action with a reenter switch field for user confirmation.
     *
     * @param exceptionType         the type of exception that occurred
     * @param validationResults     list of validation results containing error details
     * @param subCatalogRequestName name of the sub-catalog request to handle reentry
     * @param state                 current state data to preserve
     * @return ExtensionResponse with confirmation prompt and reenter option
     */
    public ExtensionResponse generateConfirmationResponse(
            ExtensionResponse.Error.ExceptionType exceptionType,
            List<ValidationOrchestrator.ValidationResult> validationResults,
            String subCatalogRequestName,
            Map<String, Object> state) {

        List<NamedField> fields = List.of(NamedFieldFactory.createSwitchField(MessagingAreaSubCatalogRequests.REENTER));
        Map<String, Object> stringStringMap = generateResponse(validationResults, false);
        return ExtensionResponseFactory.create((String) stringStringMap.get(ERROR_MESSAGE), exceptionType,
                List.of(RemediationActionFactory.createAskAction((String) stringStringMap.get(STEP_MESSAGE), fields)),
                subCatalogRequestName, state);
    }

    /**
     * Generates response data from validation results.
     * Creates step messages, error messages, and field lists based on validation failures.
     *
     * @param validationResults list of validation results to process
     * @param fetchResponse     whether to generate fetch-specific response data
     * @return Map containing step message, error message, and fields
     */
    private Map<String, Object> generateResponse(List<ValidationOrchestrator.ValidationResult> validationResults, boolean fetchResponse) {
        StringBuilder stepMessage = new StringBuilder();
        StringBuilder errMessage = new StringBuilder();
        List<NamedField> fields = new ArrayList<>();
        for (ValidationOrchestrator.ValidationResult validationResult : validationResults) {
            stepMessage.append(!fetchResponse ? validationResult.getConfirmStepMessage() : validationResult.getFetchStepMessage()).append("\n");
            errMessage.append(validationResult.getErrMessage()).append("\n");
            if (fetchResponse) {
                fields.add(NamedFieldFactory.createField(validationResult.getFetchFieldName(), validationResult.getFieldType()));
            }
        }
        return Map.of(STEP_MESSAGE, stepMessage.toString(), ERROR_MESSAGE, errMessage.toString(), FIELD, fields);
    }

    /**
     * Generates a fetch response for validation errors.
     * Creates an ask action with input fields for correcting validation failures.
     *
     * @param exceptionType         the type of exception that occurred
     * @param validationResults     list of validation results containing error details
     * @param subCatalogRequestName name of the sub-catalog request to handle reentry
     * @param state                 current state data to preserve
     * @return ExtensionResponse with input fields for data correction
     */
    @SuppressWarnings("unchecked")
    public ExtensionResponse generateFetchResponse(
            ExtensionResponse.Error.ExceptionType exceptionType,
            List<ValidationOrchestrator.ValidationResult> validationResults,
            String subCatalogRequestName,
            Map<String, Object> state) {

        LOGGER.info("Validation failed for : {}", validationResults.size());
        Map<String, Object> stringStringMap = generateResponse(validationResults, true);
        return ExtensionResponseFactory.create((String) stringStringMap.get(ERROR_MESSAGE), exceptionType,
                List.of(RemediationActionFactory.createAskAction((String) stringStringMap.get(STEP_MESSAGE), (List<NamedField>) stringStringMap.get(FIELD))),
                subCatalogRequestName, state);
    }

    /**
     * Generates a denial response when user chooses not to reenter data.
     * Creates an inform action notifying that required values were not provided.
     *
     * @param exceptionType         the type of exception that occurred
     * @param validationResults     list of validation results containing field names
     * @param subCatalogRequestName name of the sub-catalog request (can be null)
     * @param state                 current state data to preserve
     * @return ExtensionResponse with denial notification message
     */
    public ExtensionResponse generateFetchDenyResponse(
            ExtensionResponse.Error.ExceptionType exceptionType,
            List<ValidationOrchestrator.ValidationResult> validationResults,
            String subCatalogRequestName,
            Map<String, Object> state
    ) {
        StringJoiner stepMessage = new StringJoiner(", ", "Required values for ", " were not provided. Please provide the missing information to continue.");
        StringBuilder errMessage = new StringBuilder();
        for (ValidationOrchestrator.ValidationResult validationResult : validationResults) {
            stepMessage.add("'" + validationResult.getFetchFieldName() + "'");
            errMessage.append(validationResult.getErrMessage()).append("\n");
        }
        return ExtensionResponseFactory.create(errMessage.toString(), exceptionType,
                List.of(RemediationActionFactory.createInformActionALLParticipants(stepMessage.toString(), List.of())),
                subCatalogRequestName, state);
    }
}
