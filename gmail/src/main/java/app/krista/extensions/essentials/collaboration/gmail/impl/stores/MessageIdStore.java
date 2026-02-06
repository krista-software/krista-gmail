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

import app.krista.extensions.util.KeyValueStore;
import org.jvnet.hk2.annotations.Service;

import javax.inject.Inject;
import java.util.Set;

/**
 * Store for managing Gmail message IDs.
 * Provides persistence and retrieval of Gmail message identifiers used for
 * tracking email messages, managing message state, and supporting email operations
 * like replies, forwards, and status updates.
 */
@Service
public class MessageIdStore {

    private final KeyValueStore keyValueStore;

    /**
     * Constructor for dependency injection.
     * Initializes the store with the required key-value store dependency.
     *
     * @param keyValueStore the underlying key-value store for data persistence
     */
    @Inject
    public MessageIdStore(KeyValueStore keyValueStore) {
        this.keyValueStore = keyValueStore;
    }

    /**
     * Stores a Gmail message ID associated with the specified key.
     * Used to track and reference specific email messages.
     *
     * @param key       the unique identifier for the message ID mapping
     * @param messageId the Gmail message ID to store
     */
    public void put(String key, String messageId) {
        keyValueStore.put(key, messageId);
    }

    /**
     * Retrieves a Gmail message ID for the specified key.
     * Returns the stored message ID or null if not found.
     *
     * @param key the unique identifier for the message ID
     * @return the Gmail message ID, or null if not found
     */
    public String get(String key) {
        return (String) keyValueStore.get(key);
    }

    /**
     * Removes a Gmail message ID from the store.
     * Permanently deletes the message ID associated with the specified key.
     *
     * @param key the unique identifier for the message ID to remove
     */
    public void remove(String key) {
        keyValueStore.remove(key);
    }

    /**
     * Retrieves all keys currently stored in the message ID store.
     * Useful for iterating over all tracked Gmail messages.
     *
     * @return set of all keys in the store
     */
    public Set<String> getKeys() {
        return keyValueStore.getKeys();
    }

}
