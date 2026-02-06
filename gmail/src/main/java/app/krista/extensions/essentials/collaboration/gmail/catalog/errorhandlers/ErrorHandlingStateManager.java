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

import app.krista.extensions.util.KeyValueStore;
import org.jvnet.hk2.annotations.Service;

import javax.inject.Inject;
import java.util.Map;

import static app.krista.extensions.essentials.collaboration.gmail.impl.util.Constants.GSON;

/**
 * Manages internal state for error handling and validation workflows.
 * Provides storage for temporary state data and metadata during catalog request processing.
 */
@Service
public class ErrorHandlingStateManager {

    private final KeyValueStore internalState;

    @Inject
    public ErrorHandlingStateManager(KeyValueStore internalState) {
        this.internalState = internalState;

    }

    /**
     * Stores a key-value pair in the internal state store.
     * The value is serialized to JSON before storage.
     *
     * @param key   the unique identifier for the state data
     * @param value the object to store (will be serialized to JSON)
     */
    public void put(String key, Object value) {
        internalState.put(key, value);
    }

    /**
     * Retrieves and deserializes state data by key.
     * Returns the stored JSON data as a Map object.
     *
     * @param key the unique identifier for the state data
     * @return Map containing the deserialized state data, or null if not found
     */
    @SuppressWarnings("unchecked")
    public Map<String, Object> get(String key) {
        final String map = String.valueOf(internalState.get(key));
        return GSON.fromJson(map, Map.class);
    }
}
