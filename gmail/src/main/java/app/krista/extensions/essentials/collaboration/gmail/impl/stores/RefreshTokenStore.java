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

/**
 * Store for managing OAuth refresh tokens for Gmail authentication.
 * Handles secure storage and retrieval of OAuth refresh tokens used to maintain
 * persistent authentication with Gmail API without requiring frequent user re-authorization.
 * Refresh tokens are critical for maintaining long-term access to Gmail services.
 */
@Service
public final class RefreshTokenStore {

    private final KeyValueStore keyValueStore;

    /**
     * Constructor for dependency injection.
     * Initializes the store with the required key-value store dependency.
     *
     * @param keyValueStore the underlying key-value store for secure token persistence
     */
    @Inject
    public RefreshTokenStore(KeyValueStore keyValueStore) {
        this.keyValueStore = keyValueStore;
    }

    /**
     * Stores an OAuth refresh token associated with the specified key.
     * Refresh tokens are used to obtain new access tokens without user interaction.
     *
     * @param key      the unique identifier for the refresh token (typically user ID)
     * @param refToken the OAuth refresh token to store securely
     */
    public void put(String key, String refToken) {
        keyValueStore.put(key, refToken);
    }

    /**
     * Retrieves an OAuth refresh token for the specified key.
     * Returns the stored refresh token for authentication purposes.
     *
     * @param key the unique identifier for the refresh token
     * @return the OAuth refresh token, or null if not found
     */
    public String get(String key) {
        return (String) keyValueStore.get(key);
    }

    /**
     * Removes an OAuth refresh token from the store.
     * Permanently deletes the refresh token, requiring user re-authorization.
     *
     * @param key the unique identifier for the refresh token to remove
     */
    public void remove(String key) {
        keyValueStore.remove(key);
    }

}
