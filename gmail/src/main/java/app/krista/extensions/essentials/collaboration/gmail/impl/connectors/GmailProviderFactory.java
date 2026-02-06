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

package app.krista.extensions.essentials.collaboration.gmail.impl.connectors;

import app.krista.extensions.essentials.collaboration.gmail.GmailAttributes;
import app.krista.extensions.essentials.collaboration.gmail.impl.stores.GmailAttributeStore;
import app.krista.extensions.essentials.collaboration.gmail.impl.stores.RefreshTokenStore;
import app.krista.ksdk.context.AuthorizationContext;
import app.krista.ksdk.context.RequestContext;
import org.jvnet.hk2.annotations.Service;

import javax.inject.Inject;
import java.io.IOException;

/**
 * Factory class for creating GmailProvider instances.
 * Manages the creation of Gmail service providers with different configurations and authentication contexts.
 * Supports both default providers using injected attributes and custom providers with specific Gmail attributes.
 * <p>
 * This factory handles the complexity of provider creation by managing attribute storage,
 * authentication context tracking, and dependency injection for Gmail service access.
 */
@Service
public class GmailProviderFactory {

    private final RefreshTokenStore refreshTokenStore;
    private final GmailAttributeStore gmailAttributeStore;
    private final RequestContext requestContext;
    private final AuthorizationContext authorizationContext;
    private final GmailProvider defaultClientProvider;

    /**
     * Constructor for dependency injection with default GmailProvider creation.
     * Creates a factory instance with a default provider using injected attributes.
     *
     * @param refreshTokenStore    store for managing OAuth refresh tokens
     * @param gmailAttributeStore  store for Gmail configuration attributes
     * @param gmailAttributes      Gmail extension configuration attributes
     * @param requestContext       current request execution context
     * @param authorizationContext user authorization context
     */
    @Inject
    public GmailProviderFactory(RefreshTokenStore refreshTokenStore, GmailAttributeStore gmailAttributeStore, GmailAttributes gmailAttributes, RequestContext requestContext, AuthorizationContext authorizationContext) {
        this(refreshTokenStore, gmailAttributeStore, requestContext, authorizationContext,
                new GmailProvider(refreshTokenStore, gmailAttributes, requestContext, authorizationContext, null));
    }

    /**
     * Constructor with custom default GmailProvider.
     * Creates a factory instance with a specified default provider.
     *
     * @param refreshTokenStore     store for managing OAuth refresh tokens
     * @param gmailAttributeStore   store for Gmail configuration attributes
     * @param requestContext        current request execution context
     * @param authorizationContext  user authorization context
     * @param defaultClientProvider pre-configured default GmailProvider instance
     */
    public GmailProviderFactory(RefreshTokenStore refreshTokenStore, GmailAttributeStore gmailAttributeStore, RequestContext requestContext, AuthorizationContext authorizationContext, GmailProvider defaultClientProvider) {
        this.refreshTokenStore = refreshTokenStore;
        this.gmailAttributeStore = gmailAttributeStore;
        this.requestContext = requestContext;
        this.authorizationContext = authorizationContext;
        this.defaultClientProvider = defaultClientProvider;
    }

    /**
     * Creates a new GmailProvider with specific Gmail attributes.
     * Saves the attributes to store and returns a provider with unique auth context ID.
     *
     * @param gmailAttributes Gmail configuration attributes for the new provider
     * @return GmailProvider instance configured with the provided attributes
     * @throws IOException if saving attributes or creating provider fails
     */
    public GmailProvider create(GmailAttributes gmailAttributes) throws IOException {
        String authContextId = gmailAttributeStore.save(gmailAttributes);
        return new GmailProvider(refreshTokenStore, gmailAttributes, requestContext, authorizationContext, authContextId);
    }

    /**
     * Returns the default GmailProvider instance.
     * Uses the pre-configured provider created during factory initialization.
     *
     * @return default GmailProvider instance
     */
    public GmailProvider create() {
        return defaultClientProvider;
    }

}