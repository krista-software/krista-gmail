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
import java.math.BigInteger;
import java.util.Set;

/**
 * Store for managing Gmail history IDs used for tracking email changes.
 * Provides persistence and retrieval of Gmail history identifiers which are used
 * to track changes in Gmail mailboxes for incremental synchronization and notifications.
 * History IDs are stored as strings but retrieved as BigInteger for Gmail API compatibility.
 */
@Service
public class HistoryIdStore {
    private final KeyValueStore keyValueStore;

    /**
     * Constructor for dependency injection.
     * Initializes the store with the required key-value store dependency.
     *
     * @param keyValueStore the underlying key-value store for data persistence
     */
    @Inject
    public HistoryIdStore(KeyValueStore keyValueStore) {
        this.keyValueStore = keyValueStore;
    }

    /**
     * Stores a Gmail history ID associated with the specified key.
     * History IDs are used to track the last known state of a Gmail mailbox.
     *
     * @param key       the unique identifier for the history ID (typically user or mailbox identifier)
     * @param historyID the Gmail history ID to store
     */
    public void put(String key, String historyID) {
        keyValueStore.put(key, historyID);
    }

    /**
     * Retrieves a Gmail history ID for the specified key.
     * Converts the stored string value to BigInteger as required by Gmail API.
     *
     * @param key the unique identifier for the history ID
     * @return the Gmail history ID as BigInteger, or null if not found
     */
    public BigInteger get(String key) {
        return new BigInteger(String.valueOf(keyValueStore.get(key)));
    }

    /**
     * Removes a Gmail history ID from the store.
     * Permanently deletes the history ID associated with the specified key.
     *
     * @param key the unique identifier for the history ID to remove
     */
    public void remove(String key) {
        keyValueStore.remove(key);
    }

    /**
     * Retrieves all keys currently stored in the history ID store.
     * Useful for iterating over all tracked Gmail mailboxes or users.
     *
     * @return set of all keys in the store
     */
    public Set<String> getKeys() {
        return keyValueStore.getKeys();
    }

}