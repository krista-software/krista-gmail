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

import app.krista.extension.executor.RemediationAction;
import app.krista.extension.executor.impl.AskAPersonAction;
import app.krista.extension.executor.impl.InformAPersonAction;
import app.krista.model.field.NamedField;
import app.krista.model.field.NamedValuedField;

import java.util.List;
import java.util.Map;

/**
 * Factory class for creating RemediationAction instances.
 * Provides methods to create different types of remediation actions for error handling and user interaction.
 */
public class RemediationActionFactory {

    private RemediationActionFactory() {
    }

    /**
     * Creates an ask action that prompts the active user for input.
     * Used when additional information is needed from the user to proceed.
     *
     * @param message the message to display to the user
     * @param fields  list of input fields to present to the user
     * @return RemediationAction configured to ask the active user for input
     */
    public static RemediationAction createAskAction(String message,
                                                    List<NamedField> fields) {
        return AskAPersonAction.create(message, RemediationAction.RecipientType.ACTIVE_USER, fields);
    }

    /**
     * Creates an inform action that notifies the active user with information.
     * Used to provide feedback or status updates to the current user.
     *
     * @param message the informational message to display
     * @param fields  list of fields containing additional data to display
     * @return RemediationAction configured to inform the active user
     */
    public static RemediationAction createInformAction(String message,
                                                       List<NamedValuedField> fields) {
        return InformAPersonAction.create(message, RemediationAction.RecipientType.ACTIVE_USER, fields);
    }

    /**
     * Creates an inform action that notifies all participants in the conversation.
     * Used to broadcast information to everyone involved in the current context.
     *
     * @param message the informational message to display to all participants
     * @param fields  list of fields containing additional data to display
     * @return RemediationAction configured to inform all participants
     */
    public static RemediationAction createInformActionALLParticipants(String message,
                                                                      List<NamedValuedField> fields) {
        return InformAPersonAction.create(message, RemediationAction.RecipientType.ALL_PARTICIPANTS, fields);
    }

    /**
     * Creates a sub-catalog request action for handling complex workflows.
     * Used to delegate processing to a specific sub-catalog request handler.
     *
     * @param subCatalogRequestName the name of the sub-catalog request to invoke
     * @param message               the message to display during the sub-catalog request
     * @param additionalData        additional data to pass to the sub-catalog request
     * @return RemediationAction configured as a sub-catalog request
     */
    public static RemediationAction createSubCatalogRequestAction(String subCatalogRequestName,
                                                                  String message,
                                                                  Map<String, Object> additionalData) {
        return SubCatalogRequestAction.create(subCatalogRequestName, message, additionalData);
    }
}