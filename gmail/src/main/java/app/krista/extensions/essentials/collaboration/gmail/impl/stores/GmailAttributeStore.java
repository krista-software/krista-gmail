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

package app.krista.extensions.essentials.collaboration.gmail.impl.stores;

import app.krista.extension.executor.Invoker;
import app.krista.extension.request.RoutingInfo;
import app.krista.extension.request.protos.http.HttpProtocol;
import app.krista.extensions.essentials.collaboration.gmail.GmailAttributes;
import app.krista.extensions.essentials.collaboration.gmail.impl.util.Constants;
import app.krista.extensions.util.KeyValueStore;
import org.jvnet.hk2.annotations.Service;

import javax.inject.Inject;
import java.io.IOException;
import java.util.Map;
import java.util.UUID;

/**
 * Store for managing Gmail extension attributes and authentication contexts.
 * Handles persistence and retrieval of Gmail configuration data including client credentials,
 * email addresses, and routing information. Uses a key-value store for data persistence
 * and provides methods for saving, loading, and removing Gmail attribute configurations.
 */
@Service
public class GmailAttributeStore {

    private final Invoker invoker;
    private final KeyValueStore store;

    /**
     * Constructor for dependency injection.
     * Initializes the store with required dependencies for Gmail attribute management.
     *
     * @param invoker the extension invoker for routing information
     * @param store   the key-value store for data persistence
     */
    @Inject
    public GmailAttributeStore(Invoker invoker, KeyValueStore store) {
        this.invoker = invoker;
        this.store = store;
    }

    /**
     * Loads Gmail attributes from the store using the provided authentication context ID.
     * Retrieves stored configuration data and constructs a GmailAttributes object
     * with routing URL and credential information.
     *
     * @param authContextId the unique identifier for the authentication context
     * @return GmailAttributes object containing configuration data
     * @throws IOException if loading or parsing the attributes fails
     */
    public GmailAttributes load(String authContextId) throws IOException {
        var attributes = Constants.GSON.fromJson(String.valueOf(store.get(authContextId)), Map.class);
        return new GmailAttributes(invoker.getRoutingInfo().getRoutingURL(HttpProtocol.PROTOCOL_NAME, RoutingInfo.Type.APPLIANCE),
                (String) attributes.get(GmailAttributes.CLIENT_ID),
                (String) attributes.get(GmailAttributes.CLIENT_SECRET),
                (String) attributes.get(GmailAttributes.EMAIL));
    }

    /**
     * Saves Gmail attributes to the store and returns a unique authentication context ID.
     * Serializes the Gmail configuration data and stores it with a generated UUID key.
     *
     * @param gmailAttributes the Gmail configuration to save
     * @return unique authentication context ID for the saved attributes
     */
    public String save(GmailAttributes gmailAttributes) {
        String authContextId = UUID.randomUUID().toString();
        store.put(authContextId, Constants.GSON.toJson(Map.of(GmailAttributes.CLIENT_ID, gmailAttributes.getClientId(),
                GmailAttributes.CLIENT_SECRET, gmailAttributes.getClientSecret(),
                GmailAttributes.EMAIL, gmailAttributes.getMailId())));
        return authContextId;
    }

    /**
     * Removes Gmail attributes from the store using the authentication context ID.
     * Permanently deletes the stored configuration data for the specified context.
     *
     * @param authContextId the unique identifier for the authentication context to remove
     */
    public void remove(String authContextId) {
        store.remove(authContextId);
    }

}
