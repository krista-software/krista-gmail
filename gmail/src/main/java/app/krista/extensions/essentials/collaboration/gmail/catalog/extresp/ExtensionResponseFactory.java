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

import app.krista.extension.executor.ExtensionResponse;
import app.krista.extension.executor.RemediationAction;
import app.krista.extension.executor.RemediationActions;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * Factory class for creating ExtensionResponse objects.
 * Provides static methods to create success and failure responses with various configurations.
 */
public class ExtensionResponseFactory {

    private ExtensionResponseFactory() {
    }

    /**
     * Creates a successful extension response with return values.
     *
     * @param values map containing the response data to return
     * @return ExtensionResponse with SUCCESS result and provided values
     */
    public static ExtensionResponse create(Map<String, Object> values) {
        return new ExtensionResponse(ExtensionResponse.Result.SUCCESS, values,
                null, null, null);
    }

    /**
     * Creates a failure extension response from an exception.
     * Automatically generates an inform action with the error message.
     *
     * @param cause         the exception that caused the failure
     * @param message       user-friendly error message
     * @param exceptionType type of exception for categorization
     * @return ExtensionResponse with FAILURE result and inform action
     */
    public static ExtensionResponse create(Exception cause,
                                           String message,
                                           ExtensionResponse.Error.ExceptionType exceptionType) {
        ExtensionResponse.Error error = new ExtensionResponse.Error(message, System.currentTimeMillis(),
                exceptionType, Arrays.toString(cause.getStackTrace()));
        RemediationActions remediationActions = new RemediationActions(List.of(RemediationActionFactory.createInformAction(message, List.of())), null);
        return new ExtensionResponse(ExtensionResponse.Result.FAILURE,
                null, error, remediationActions, Map.of());
    }

    /**
     * Creates a failure extension response with custom remediation actions and state.
     * Includes exception stack trace in the error details.
     *
     * @param cause                 the exception that caused the failure
     * @param message               user-friendly error message
     * @param exceptionType         type of exception for categorization
     * @param actions               list of remediation actions for error recovery
     * @param subCatalogRequestName name of sub-catalog request for handling recovery
     * @param state                 current state data to preserve
     * @return ExtensionResponse with FAILURE result and custom actions
     */
    public static ExtensionResponse create(Exception cause,
                                           String message,
                                           ExtensionResponse.Error.ExceptionType exceptionType,
                                           List<RemediationAction> actions,
                                           String subCatalogRequestName,
                                           Map<String, Object> state) {
        RemediationActions remediationActions = new RemediationActions(actions, subCatalogRequestName);
        ExtensionResponse.Error error = new ExtensionResponse.Error(message, System.currentTimeMillis(),
                exceptionType, Arrays.toString(cause.getStackTrace()));
        return new ExtensionResponse(ExtensionResponse.Result.FAILURE, null, error,
                remediationActions, state);
    }

    /**
     * Creates a failure extension response without exception details.
     * Used for validation errors or business logic failures.
     *
     * @param message               user-friendly error message
     * @param exceptionType         type of exception for categorization
     * @param actions               list of remediation actions for error recovery
     * @param subCatalogRequestName name of sub-catalog request for handling recovery
     * @param state                 current state data to preserve
     * @return ExtensionResponse with FAILURE result and empty stack trace
     */
    public static ExtensionResponse create(String message,
                                           ExtensionResponse.Error.ExceptionType exceptionType,
                                           List<RemediationAction> actions,
                                           String subCatalogRequestName,
                                           Map<String, Object> state) {
        RemediationActions remediationActions = new RemediationActions(actions, subCatalogRequestName);
        ExtensionResponse.Error error = new ExtensionResponse.Error(message, System.currentTimeMillis(),
                exceptionType, "");
        return new ExtensionResponse(ExtensionResponse.Result.FAILURE, null, error,
                remediationActions, state);
    }

    /**
     * Creates an extension response with custom result type.
     * Allows specifying whether the response is SUCCESS or FAILURE.
     *
     * @param message               user-friendly error message
     * @param exceptionType         type of exception for categorization
     * @param actions               list of remediation actions for error recovery
     * @param subCatalogRequestName name of sub-catalog request for handling recovery
     * @param state                 current state data to preserve
     * @param result                the result type (SUCCESS or FAILURE)
     * @return ExtensionResponse with specified result and empty stack trace
     */
    public static ExtensionResponse create(String message, ExtensionResponse.Error.ExceptionType exceptionType,
                                           List<RemediationAction> actions, String subCatalogRequestName,
                                           Map<String, Object> state, ExtensionResponse.Result result) {
        RemediationActions remediationActions = new RemediationActions(actions, subCatalogRequestName);
        ExtensionResponse.Error error = new ExtensionResponse.Error(message, System.currentTimeMillis(), exceptionType, "");
        return new ExtensionResponse(result, null, error, remediationActions, state);
    }
}
