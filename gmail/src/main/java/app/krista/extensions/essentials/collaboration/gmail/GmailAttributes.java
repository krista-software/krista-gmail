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

import app.krista.extension.executor.Invoker;
import app.krista.extension.request.RoutingInfo;
import app.krista.extension.request.protos.http.HttpProtocol;
import app.krista.extension.util.InvokerAttributes;
import com.github.scribejava.apis.GoogleApi20;
import com.github.scribejava.core.builder.ServiceBuilder;
import com.github.scribejava.core.oauth.OAuth20Service;
import com.google.api.services.gmail.GmailScopes;
import org.jvnet.hk2.annotations.Service;

import javax.inject.Inject;
import java.util.Map;

/**
 * Service class for managing Gmail extension configuration attributes and OAuth authentication.
 * Handles storage and retrieval of Gmail API credentials, routing information, and OAuth service
 * configuration. Provides centralized management of all Gmail-related configuration data
 * including client credentials, email addresses, notification settings, and OAuth service setup.
 * <p>
 * This class supports both dependency injection through the Krista framework and direct
 * instantiation for testing or specialized use cases. It manages OAuth 2.0 service creation
 * and configuration for Gmail API authentication flows.
 */
@Service
public class GmailAttributes {

    public static final String CLIENT_ID = "clientId";
    public static final String CLIENT_SECRET = "clientSecret";
    public static final String EMAIL = "email";
    public static final String TOPIC_KEY = "topic";
    public static final String ALERT_KEY = "alert";
    private final String routingUrl;
    private String topic;
    private Boolean alert;
    private String clientId;
    private String clientSecret;
    private String mailId;

    private OAuth20Service oAuth20Service;

    /**
     * Primary constructor for dependency injection.
     * Creates a GmailAttributes instance using configuration from the provided Invoker.
     * Extracts routing URL and attribute values from the invoker's configuration.
     *
     * @param invoker the Krista Invoker containing extension configuration and routing information
     */
    @Inject
    public GmailAttributes(Invoker invoker) {
        this(invoker.getRoutingInfo().getRoutingURL(HttpProtocol.PROTOCOL_NAME, RoutingInfo.Type.APPLIANCE),
                invoker.getAttributes());
    }

    /**
     * Constructor for creating GmailAttributes from a routing URL and attribute map.
     * Extracts individual configuration values from the provided attributes map
     * and initializes the instance with routing and credential information.
     *
     * @param routingUrl the base URL for routing HTTP requests to this extension
     * @param attributes map containing configuration attributes (clientId, clientSecret, email, topic, alert)
     */
    public GmailAttributes(String routingUrl, Map<String, Object> attributes) {
        this(routingUrl, InvokerAttributes.getStringOrNull(attributes, CLIENT_ID),
                InvokerAttributes.getStringOrNull(attributes, CLIENT_SECRET),
                InvokerAttributes.getStringOrNull(attributes, EMAIL), InvokerAttributes.getStringOrNull(attributes, GmailAttributes.TOPIC_KEY), InvokerAttributes.getBooleanOrNull(attributes, GmailAttributes.ALERT_KEY));
    }

    /**
     * Full constructor with all configuration parameters including notification settings.
     * Creates a GmailAttributes instance with complete configuration including
     * OAuth credentials, email address, and notification topic/alert settings.
     *
     * @param routingUrl   the base URL for routing HTTP requests to this extension
     * @param clientId     the OAuth 2.0 client ID for Gmail API authentication
     * @param clientSecret the OAuth 2.0 client secret for Gmail API authentication
     * @param mailId       the email address associated with this Gmail account
     * @param topic        the Google Cloud Pub/Sub topic for Gmail notifications (optional)
     * @param alert        the alert configuration for Gmail notifications (optional)
     */
    public GmailAttributes(String routingUrl, String clientId, String clientSecret, String mailId, String topic, Boolean alert) {
        this(routingUrl, clientId, clientSecret, mailId);
        this.topic = topic;
        this.alert = alert;
    }

    /**
     * Basic constructor with essential OAuth and email configuration.
     * Creates a GmailAttributes instance with the minimum required configuration
     * for Gmail API authentication and basic operations.
     *
     * @param routingUrl   the base URL for routing HTTP requests to this extension
     * @param clientId     the OAuth 2.0 client ID for Gmail API authentication
     * @param clientSecret the OAuth 2.0 client secret for Gmail API authentication
     * @param mailId       the email address associated with this Gmail account
     */
    public GmailAttributes(String routingUrl, String clientId, String clientSecret, String mailId) {
        this.routingUrl = routingUrl;
        this.clientId = clientId;
        this.clientSecret = clientSecret;
        this.mailId = mailId;
    }

    /**
     * Factory method for creating GmailAttributes from an Invoker and custom attributes.
     * Provides a convenient way to create GmailAttributes instances with custom
     * attribute values while still using the invoker's routing configuration.
     *
     * @param invoker    the Krista Invoker providing routing information
     * @param attributes map containing custom configuration attributes
     * @return new GmailAttributes instance configured with the provided values
     */
    public static GmailAttributes create(Invoker invoker, Map<String, Object> attributes) {
        return new GmailAttributes(
                invoker.getRoutingInfo().getRoutingURL(HttpProtocol.PROTOCOL_NAME, RoutingInfo.Type.APPLIANCE),
                attributes);
    }

    /**
     * Updates the configuration attributes with new values.
     * Refreshes the stored configuration with updated values from the provided map.
     * This method is typically called when extension configuration is modified
     * through the Krista administration interface.
     *
     * @param newAttributes map containing updated configuration values
     */
    public void update(Map<String, Object> newAttributes) {
        clientId = InvokerAttributes.getStringOrNull(newAttributes, CLIENT_ID);
        clientSecret = InvokerAttributes.getStringOrNull(newAttributes, CLIENT_SECRET);
        mailId = InvokerAttributes.getStringOrNull(newAttributes, EMAIL);
        topic = InvokerAttributes.getStringOrNull(newAttributes, TOPIC_KEY);
    }

    /**
     * Gets the OAuth 2.0 client ID.
     * Returns the client ID used for Gmail API authentication.
     * This value is obtained from Google Cloud Console when setting up OAuth credentials.
     *
     * @return the OAuth 2.0 client ID
     */
    public String getClientId() {
        return clientId;
    }

    /**
     * Gets the OAuth 2.0 client secret.
     * Returns the client secret used for Gmail API authentication.
     * This value is obtained from Google Cloud Console and should be kept secure.
     *
     * @return the OAuth 2.0 client secret
     */
    public String getClientSecret() {
        return clientSecret;
    }

    /**
     * Gets the email address associated with this Gmail account.
     * Returns the email address that will be used for Gmail operations.
     *
     * @return the Gmail account email address
     */
    public String getMailId() {
        return mailId;
    }

    /**
     * Gets the Google Cloud Pub/Sub topic for Gmail notifications.
     * Returns the topic name used for receiving Gmail push notifications.
     * This is optional and may be null if notifications are not configured.
     *
     * @return the Pub/Sub topic name, or null if not configured
     */
    public String getTopic() {
        return topic;
    }

    /**
     * Gets the alert configuration for Gmail notifications.
     * Returns the boolean flag indicating whether alerts are enabled for this configuration.
     * This is optional and may be null if not explicitly configured.
     *
     * @return true if alerts are enabled, false if disabled, null if not configured
     */
    public Boolean getAlert() {
        return alert;
    }

    /**
     * Constructs the OAuth callback URL for Gmail authentication.
     * Builds the complete callback URL that Gmail will redirect to after user authorization.
     * This URL must be registered in the Google Cloud Console OAuth configuration.
     *
     * @return the complete OAuth callback URL for Gmail authentication
     */
    public String getCallbackUrl() {
        return routingUrl + "/rest/gmail/callback";
    }

    /**
     * Gets or creates the OAuth 2.0 service for Gmail authentication.
     * Returns a configured OAuth20Service instance for handling Gmail API authentication.
     * The service is created lazily and cached for subsequent use. It includes the
     * necessary scopes, callback URL, and API configuration for Gmail operations.
     * <p>
     * This method is thread-safe and ensures only one OAuth service instance is created
     * per GmailAttributes instance.
     *
     * @return configured OAuth20Service for Gmail API authentication
     */
    public synchronized OAuth20Service getOAuth20Service() {
        if (oAuth20Service == null) {
            oAuth20Service = new ServiceBuilder(clientId)
                    .apiSecret(clientSecret)
                    .defaultScope(GmailScopes.GMAIL_MODIFY)
                    .callback(getCallbackUrl())
                    .build(GoogleApi20.instance());
        }
        return oAuth20Service;
    }
}
