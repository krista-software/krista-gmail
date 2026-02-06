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

package app.krista.extensions.essentials.collaboration.gmail;

import app.krista.extension.authorization.RequestAuthenticator;
import app.krista.extension.event.WaitForEventListener;
import app.krista.extension.executor.Invoker;
import app.krista.extension.impl.anno.*;
import app.krista.extensions.essentials.collaboration.gmail.impl.GmailNotificationChannel;
import app.krista.extensions.essentials.collaboration.gmail.impl.connectors.GmailProviderFactory;
import app.krista.extensions.essentials.collaboration.gmail.impl.stores.GmailAttributeStore;
import app.krista.extensions.essentials.collaboration.gmail.impl.stores.HistoryIdStore;
import app.krista.ksdk.context.AuthorizationContext;
import com.google.api.services.gmail.model.WatchResponse;

import javax.inject.Inject;
import java.io.IOException;
import java.util.Map;

import static app.krista.extensions.essentials.collaboration.gmail.impl.GmailNotificationChannel.TO_BE_USED_HISTORY_ID;

/**
 * Main extension class for Gmail integration with the Krista platform.
 * Provides comprehensive Gmail functionality including email operations, authentication,
 * configuration management, and real-time notification handling. This class serves as
 * the primary entry point for all Gmail-related operations within the Krista ecosystem.
 * <p>
 * The extension supports OAuth 2.0 authentication, Gmail API operations, push notifications
 * via Google Cloud Pub/Sub, and comprehensive email management capabilities. It handles
 * both service account and user account authentication modes.
 * <p>
 * Key features include:
 * - OAuth 2.0 authentication flow management
 * - Gmail API integration for email operations
 * - Real-time email notifications via webhooks
 * - Configuration validation and testing
 * - History tracking for efficient email synchronization
 */
@Field.Text(value = GmailAttributes.CLIENT_ID)
@Field.Text(value = GmailAttributes.CLIENT_SECRET, isSecured = true)
@Field.Text(value = GmailAttributes.EMAIL)
@Field.Text(value = GmailAttributes.TOPIC_KEY, required = false)
@Field.Boolean(value = GmailAttributes.ALERT_KEY, required = false)
@Extension(version = "2.0.17", name = "Gmail Extension")
@Java(version = Java.Version.JAVA_21)
@StaticResource(path = "docs", file = "docs")
public class GmailExtension {

    public static final String EITHER_BOTH_TOPIC_AND_ALERT_MUST_BE_PROVIDED_OR_BOTH_MUST_BE_MISSING = "Either both topic and alert must be provided, or both must be missing.";
    private final Invoker invoker;
    private final GmailAttributes gmailAttributes;
    private final GmailProviderFactory clientProviderFactory;
    private final GmailRequestAuthenticator requestAuthenticator;
    private final GmailNotificationChannel gmailNotificationChannel;
    private final HistoryIdStore historyIdStore;

    /**
     * Primary constructor for dependency injection.
     * Creates a GmailExtension instance with all required dependencies injected by the Krista framework.
     * Automatically configures the request authenticator using the provided components.
     *
     * @param invoker                  the Krista Invoker for executing extension operations
     * @param gmailAttributes          configuration attributes for Gmail API access
     * @param clientProviderFactory    factory for creating Gmail API client providers
     * @param gmailAttributeStore      store for managing Gmail configuration data
     * @param gmailNotificationChannel service for handling Gmail push notifications
     * @param historyIdStore           store for tracking Gmail history IDs for synchronization
     * @param context                  authorization context for user authentication
     */
    @Inject
    public GmailExtension(Invoker invoker, GmailAttributes gmailAttributes, GmailProviderFactory clientProviderFactory, GmailAttributeStore gmailAttributeStore, GmailNotificationChannel gmailNotificationChannel, HistoryIdStore historyIdStore, AuthorizationContext context) {
        this(invoker, gmailAttributes,
                clientProviderFactory,
                new GmailRequestAuthenticator(gmailAttributeStore, gmailAttributes, context), gmailNotificationChannel, historyIdStore);
    }

    /**
     * Constructor for testing and custom configuration.
     * Creates a GmailExtension instance with explicitly provided dependencies.
     * Useful for testing scenarios or when custom authenticator configuration is needed.
     *
     * @param invoker                  the Krista Invoker for executing extension operations
     * @param gmailAttributes          configuration attributes for Gmail API access
     * @param clientProviderFactory    factory for creating Gmail API client providers
     * @param requestAuthenticator     custom request authenticator for handling authentication
     * @param gmailNotificationChannel service for handling Gmail push notifications
     * @param historyIdStore           store for tracking Gmail history IDs for synchronization
     */
    public GmailExtension(Invoker invoker, GmailAttributes gmailAttributes, GmailProviderFactory clientProviderFactory, GmailRequestAuthenticator requestAuthenticator, GmailNotificationChannel gmailNotificationChannel, HistoryIdStore historyIdStore) {
        this.invoker = invoker;
        this.gmailAttributes = gmailAttributes;
        this.clientProviderFactory = clientProviderFactory;
        this.requestAuthenticator = requestAuthenticator;
        this.gmailNotificationChannel = gmailNotificationChannel;
        this.historyIdStore = historyIdStore;
    }

    /**
     * Provides the request authenticator for this extension.
     * Returns the configured authenticator that handles OAuth flows and user authentication
     * for Gmail API operations. This method is called by the Krista framework to obtain
     * the authenticator for processing requests that require authentication.
     *
     * @return the configured GmailRequestAuthenticator instance
     */
    @InvokerRequest(InvokerRequest.Type.AUTHENTICATOR)
    public RequestAuthenticator getRequestAuthenticator() {
        return requestAuthenticator;
    }

    /**
     * Provides custom tab configuration for the extension interface.
     * Returns a map of custom tabs that will be displayed in the Krista interface
     * for this extension. Currently provides access to the documentation tab.
     *
     * @return map of tab names to their corresponding paths
     */
    @InvokerRequest(InvokerRequest.Type.CUSTOM_TABS)
    public Map<String, String> customTabs() {
        return Map.of("Documentation", "static/docs");
    }

    /**
     * Validates Gmail connection and configuration during extension setup.
     * Tests the Gmail API connection using the provided configuration attributes
     * without saving them to the database. This method is called when users
     * configure the extension to verify their credentials and settings.
     * <p>
     * Performs the following validations:
     * - Tests Gmail API authentication with provided credentials
     * - Validates topic and alert configuration consistency
     * - Initiates notification channel if topic is configured
     * - Stores initial history ID for synchronization
     *
     * @param connectionAttributes map containing configuration attributes to validate
     * @throws IOException if Gmail API connection fails or configuration is invalid
     */
    @InvokerRequest(InvokerRequest.Type.VALIDATE_ATTRIBUTES)
    public void validateConnection(Map<String, Object> connectionAttributes) throws IOException {
        GmailAttributes attributes = GmailAttributes.create(invoker, connectionAttributes);
        testConnection(attributes);
        alertValidation(attributes.getTopic(), attributes.getAlert());
        if (attributes.getTopic() != null && !attributes.getTopic().isEmpty()) {
            currentHistoryIdFromValidate(gmailNotificationChannel.initiate(attributes));
        }
    }

    private static void alertValidation(String topic, Boolean alert) {
        if (topic == null && alert != null) {
            if (alert) {
                throw new IllegalArgumentException(EITHER_BOTH_TOPIC_AND_ALERT_MUST_BE_PROVIDED_OR_BOTH_MUST_BE_MISSING);
            }
        } else if (topic != null && alert == null) {
            throw new IllegalArgumentException(EITHER_BOTH_TOPIC_AND_ALERT_MUST_BE_PROVIDED_OR_BOTH_MUST_BE_MISSING);
        } else if (topic != null && !alert) {
            throw new IllegalArgumentException(EITHER_BOTH_TOPIC_AND_ALERT_MUST_BE_PROVIDED_OR_BOTH_MUST_BE_MISSING);
        }
    }

    /**
     * Tests Gmail connection using saved configuration.
     * Verifies that the Gmail API connection works with the currently saved
     * configuration attributes. This method is called to test existing
     * extension configurations without modifying them.
     *
     * @throws IOException if Gmail API connection fails with saved configuration
     */
    @InvokerRequest(InvokerRequest.Type.TEST_CONNECTION)
    public void testConnection() throws IOException {
        testConnection(gmailAttributes);
    }

    /**
     * Handles updates to extension configuration attributes.
     * Called when extension configuration is modified through the Krista interface.
     * Updates the internal configuration with new values and ensures consistency.
     *
     * @param oldAttributes map containing the previous configuration values
     * @param newAttributes map containing the updated configuration values
     */
    @InvokerRequest(InvokerRequest.Type.INVOKER_UPDATED)
    public void attributesUpdated(Map<String, Object> oldAttributes, Map<String, Object> newAttributes) {
        gmailAttributes.update(newAttributes);
    }

    /**
     * Registers an event listener for Gmail notifications.
     * Sets up the extension to receive and process Gmail push notifications.
     * This method is called when the extension needs to start listening for
     * real-time Gmail events such as new emails or mailbox changes.
     * <p>
     * Initializes the current history ID for tracking Gmail changes and
     * ensures proper synchronization state for notification processing.
     *
     * @param listener the event listener that will receive Gmail notifications
     */
    @InvokerRequest(InvokerRequest.Type.REGISTER_EVENT_LISTENER)
    public void registerEventListener(WaitForEventListener listener) {
        currentHistoryId();
    }

    /**
     * Initializes the current Gmail history ID for change tracking.
     * Sets up the history ID that will be used as a baseline for detecting
     * new Gmail changes. This method checks if a notification topic is configured
     * and initiates the Gmail watch request if notifications are enabled.
     */
    private void currentHistoryId() {
        if (String.valueOf(invoker.getAttributes().get(GmailAttributes.TOPIC_KEY)) != null) {
            currentHistoryIdFromValidate(gmailNotificationChannel.initiate(gmailAttributes));
        }
    }

    /**
     * Stores the history ID from Gmail watch response for future synchronization.
     * Extracts and stores the history ID from a Gmail watch response, which will
     * be used as a reference point for detecting new changes in subsequent
     * notification processing.
     *
     * @param gmailNotificationChannel the Gmail watch response containing the history ID
     */
    private void currentHistoryIdFromValidate(WatchResponse gmailNotificationChannel) {
        historyIdStore.put(TO_BE_USED_HISTORY_ID, String.valueOf(gmailNotificationChannel.getHistoryId()));
    }

    /**
     * Tests Gmail API connection with specified attributes.
     * Performs a connection test using the provided Gmail configuration.
     * This is a utility method used by both validation and testing operations
     * to verify Gmail API connectivity and authentication.
     *
     * @param gmailAttributes the Gmail configuration to test
     * @throws IOException if the connection test fails
     */
    private void testConnection(GmailAttributes gmailAttributes) throws IOException {
        clientProviderFactory.create(gmailAttributes).testConnection();
    }

}
