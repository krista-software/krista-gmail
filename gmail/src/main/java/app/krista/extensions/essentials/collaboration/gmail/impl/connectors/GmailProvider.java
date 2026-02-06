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

import app.krista.extension.authorization.MustAuthorizeException;
import app.krista.extensions.essentials.collaboration.gmail.GmailAttributes;
import app.krista.extensions.essentials.collaboration.gmail.impl.stores.RefreshTokenStore;
import app.krista.extensions.essentials.collaboration.gmail.impl.util.Constants;
import app.krista.ksdk.context.AuthorizationContext;
import app.krista.ksdk.context.RequestContext;
import app.krista.model.field.NamedValuedField;
import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.googleapis.json.GoogleJsonResponseException;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.gmail.Gmail;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Provider class for Gmail API client management and authentication.
 * Handles OAuth authentication, token management, and Gmail service client creation.
 * <p>
 * This class manages the complexity of Gmail API authentication including:
 * - OAuth refresh token storage and retrieval
 * - Google credential creation and refresh
 * - Gmail service client instantiation
 * - Authentication error handling and user re-authorization
 * - Support for both user and admin authentication contexts
 * <p>
 * The provider supports different authentication modes based on request context
 * and can handle both service account and user account authentication flows.
 */
public class GmailProvider {
    static final JsonFactory JSON_FACTORY = GsonFactory.getDefaultInstance();
    private static final Logger LOGGER = LoggerFactory.getLogger(GmailProvider.class);
    private final RefreshTokenStore refreshTokenStore;
    private final GmailAttributes gmailAttributes;
    private final RequestContext requestContext;
    private final AuthorizationContext authorizationContext;
    private final String authContextId;

    /**
     * Constructor for dependency injection with default auth context ID.
     * Creates a GmailProvider instance with null auth context ID.
     *
     * @param refreshTokenStore    store for managing OAuth refresh tokens
     * @param gmailAttributes      Gmail extension configuration attributes
     * @param requestContext       current request execution context
     * @param authorizationContext user authorization context
     */
    @Inject
    public GmailProvider(RefreshTokenStore refreshTokenStore, GmailAttributes gmailAttributes, RequestContext requestContext, AuthorizationContext authorizationContext) {
        this(refreshTokenStore, gmailAttributes, requestContext, authorizationContext, null);
    }

    /**
     * Constructor with custom auth context ID.
     * Creates a GmailProvider instance with specified auth context ID for tracking.
     *
     * @param refreshTokenStore    store for managing OAuth refresh tokens
     * @param gmailAttributes      Gmail extension configuration attributes
     * @param requestContext       current request execution context
     * @param authorizationContext user authorization context
     * @param authContextId        unique identifier for the authorization context
     */
    public GmailProvider(RefreshTokenStore refreshTokenStore, GmailAttributes gmailAttributes, RequestContext requestContext, AuthorizationContext authorizationContext, String authContextId) {
        this.refreshTokenStore = refreshTokenStore;
        this.gmailAttributes = gmailAttributes;
        this.requestContext = requestContext;
        this.authorizationContext = authorizationContext;
        this.authContextId = authContextId;
    }

    /**
     * Gets the Gmail attributes configuration.
     *
     * @return GmailAttributes containing client ID, secret, and other settings
     */
    public GmailAttributes getGmailAttributes() {
        return gmailAttributes;
    }

    /**
     * Gets a Gmail client instance based on the current request context.
     * Uses admin credentials if not invoking as user, otherwise uses user credentials.
     *
     * @return authenticated Gmail client instance
     * @throws IOException if authentication or client creation fails
     */
    public Gmail getGmailClient() throws IOException {
        return getGmailClient(!requestContext.invokeAsUser());
    }

    /**
     * Gets a Gmail client instance using admin credentials.
     * Always uses the configured admin email for authentication.
     *
     * @return authenticated Gmail client instance for admin operations
     * @throws IOException if authentication or client creation fails
     */
    public Gmail getGmailClientForAdmin() throws IOException {
        return getGmailClient(true);
    }

    /**
     * Creates an authenticated Gmail client instance.
     * Retrieves refresh token and builds Gmail client with proper credentials.
     *
     * @param useEmail whether to use email-based authentication
     * @return authenticated Gmail client instance
     * @throws RuntimeException if authentication fails or client creation fails
     */
    private Gmail getGmailClient(boolean useEmail) {
        try {
            String userId = getUserId(useEmail);
            String refreshToken = refreshTokenStore.get(userId);
            if (refreshToken == null) {
                throw createMustAuthorizationException(userId, false);
            }
            return getGmail(userId, refreshToken, useEmail);
        } catch (RuntimeException cause) {
            LOGGER.error(Constants.FAILED_TO_GET_GRAPH_CLIENT + "{} {}", cause.getClass().getName(), cause.getMessage());
            throw cause;
        }
    }

    /**
     * Builds a Gmail service instance with authenticated credentials.
     * Creates HTTP transport, refreshes credentials, and builds Gmail service.
     *
     * @param userId       the user identifier for logging purposes
     * @param refreshToken OAuth refresh token for authentication
     * @param isUseEmail   whether email-based authentication is being used
     * @return configured Gmail service instance
     * @throws IllegalStateException  if user is not authorized
     * @throws MustAuthorizeException if re-authentication is required
     */
    @SuppressWarnings("deprecation")
    private Gmail getGmail(String userId, String refreshToken, Boolean isUseEmail) {
        try {
            final NetHttpTransport httpTransport = GoogleNetHttpTransport.newTrustedTransport();
            GoogleCredential credential = getGoogleCredential(refreshToken, httpTransport, isUseEmail);
            return new Gmail.Builder(httpTransport, JSON_FACTORY, credential)
                    .setApplicationName(Constants.GOOGLE_MAIL)
                    .build();
        } catch (IOException | GeneralSecurityException cause) {
            if (!requestContext.invokeAsUser()) {
                throw new IllegalStateException("You are not authorized. Please ask admin to validate the attributes.");
            } else {
                throw createMustAuthorizationException(userId, true);
            }
        }
    }

    /**
     * Creates and configures Google OAuth credentials.
     * Sets up client secrets, refresh token, and performs token refresh.
     *
     * @param refreshToken   OAuth refresh token for authentication
     * @param HTTP_TRANSPORT HTTP transport for API calls
     * @param isUseEmail     whether email-based authentication is being used
     * @return configured and refreshed GoogleCredential instance
     * @throws IOException if credential creation or token refresh fails
     */
    @SuppressWarnings("deprecation")
    private GoogleCredential getGoogleCredential(String refreshToken, NetHttpTransport HTTP_TRANSPORT, Boolean isUseEmail) throws IOException {
        // validate if extn attributes has the required values else return err
        GoogleCredential credential =
                new GoogleCredential.Builder()
                        .setClientSecrets(gmailAttributes.getClientId(), gmailAttributes.getClientSecret())
                        .setJsonFactory(GsonFactory.getDefaultInstance())
                        .setTransport(HTTP_TRANSPORT)
                        .build();
        credential.setRefreshToken(refreshToken);
        credential.refreshToken();
        return credential;
    }

    /**
     * Creates a MustAuthorizeException for authentication failures.
     * Builds exception with user details and appropriate error message.
     *
     * @param userId           the user identifier that failed authentication
     * @param reAuthentication whether this is a re-authentication scenario
     * @return MustAuthorizeException with user details and error message
     */
    public MustAuthorizeException createMustAuthorizationException(String userId, boolean reAuthentication) {
        LOGGER.error(reAuthentication ? Constants.GOT_ERROR_FOR_AUTHENTICATION_SO_SENDING_FOR_RE_AUTHENTICATION : Constants.GOT_ERROR_FOR_AUTHENTICATION_SO_SENDING_FOR_AUTHENTICATION);
        List<NamedValuedField> details = new ArrayList<>();
        NamedValuedField userIdField = new NamedValuedField(Constants.USER_ID, Constants.TEXT, userId, new HashMap<>(), new HashMap<>());
        details.add(userIdField);
        if (authContextId != null) {
            NamedValuedField contextIdField = new NamedValuedField(Constants.AUTH_CONTEXT_ID, Constants.TEXT, authContextId, new HashMap<>(), new HashMap<>());
            details.add(contextIdField);
        }
        return new MustAuthorizeException(reAuthentication ? Constants.REFRESH_TOKEN_EXPIRED_PLEASE_REAUTHORIZE_YOURSELF : Constants.AUTHORIZATION_PROMPT, details);
    }

    /**
     * Generates a unique user identifier for token storage.
     * Combines user account ID with client credentials to create unique key.
     *
     * @param calledFromValidateAttributes whether called from attribute validation
     * @return unique user identifier combining account ID and client credentials
     * @throws IllegalStateException if user account cannot be determined
     */
    public String getUserId(boolean calledFromValidateAttributes) {
        String userId = calledFromValidateAttributes || !requestContext.invokeAsUser()
                ? gmailAttributes.getMailId()
                : authorizationContext.getAuthorizedAccount().getAccountId();
        if (userId == null) {
            throw new IllegalStateException(Constants.FAILED_TO_GET_ACCOUNT);
        }
        return userId + Constants.HASH + gmailAttributes.getClientId() + gmailAttributes.getClientSecret();
    }

    /**
     * Tests the Gmail connection by attempting to fetch user profile.
     * Validates authentication and throws appropriate exceptions for failures.
     * If there is an exception we are parsing to check if it Unauthorised. If so creating must authorise exception.
     *
     * @throws MustAuthorizeException if authentication fails with 401 status
     * @throws RuntimeException       if other connection errors occur
     */
    public void testConnection() {
        try {
            getGmailClientForAdmin().users().getProfile(Constants.ME).execute();
        } catch (IOException exception) {
            if (((GoogleJsonResponseException) exception).getStatusCode() == 401) {
                throw createMustAuthorizationException(getUserId(true), true);
            }
        }
    }

}
