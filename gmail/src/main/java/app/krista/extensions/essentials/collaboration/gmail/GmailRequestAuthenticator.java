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

import app.krista.extension.authorization.MustAuthenticateException;
import app.krista.extension.authorization.MustAuthorizeException;
import app.krista.extension.authorization.RequestAuthenticator;
import app.krista.extension.request.ProtoRequest;
import app.krista.extension.request.ProtoResponse;
import app.krista.extension.request.protos.http.HttpRequest;
import app.krista.extensions.essentials.collaboration.gmail.impl.stores.GmailAttributeStore;
import app.krista.extensions.essentials.collaboration.gmail.impl.util.Constants;
import app.krista.ksdk.context.AuthorizationContext;
import app.krista.model.field.NamedField;
import app.krista.model.field.NamedValuedField;
import com.github.scribejava.core.oauth.OAuth20Service;

import javax.ws.rs.core.MultivaluedMap;
import java.io.IOException;
import java.util.*;

/**
 * Request authenticator implementation for Gmail extension OAuth 2.0 authentication.
 * Handles authentication and authorization flows for Gmail API access within the Krista platform.
 * Manages OAuth callback processing, user identification, and authorization response generation
 * for both service account and user account authentication modes.
 * <p>
 * This authenticator supports:
 * - OAuth 2.0 authorization code flow
 * - Gmail API callback processing
 * - Webhook notification authentication
 * - Multi-context authentication (service vs user accounts)
 * - Authorization URL generation with proper state management
 */
public class GmailRequestAuthenticator implements RequestAuthenticator {

    private final GmailAttributeStore gmailAttributeStore;
    private final GmailAttributes gmailAttributes;
    private final AuthorizationContext context;

    /**
     * Constructs a Gmail request authenticator with required dependencies.
     * Initializes the authenticator with access to Gmail configuration storage,
     * current Gmail attributes, and authorization context for handling
     * authentication requests and responses.
     *
     * @param gmailAttributeStore store for managing Gmail configuration data across contexts
     * @param gmailAttributes     current Gmail configuration attributes
     * @param context             authorization context providing access to current user information
     */
    public GmailRequestAuthenticator(GmailAttributeStore gmailAttributeStore, GmailAttributes gmailAttributes, AuthorizationContext context) {
        this.gmailAttributeStore = gmailAttributeStore;
        this.gmailAttributes = gmailAttributes;
        this.context = context;
    }

    /**
     * Gets the authentication scheme used by this authenticator.
     * Returns null as this authenticator uses OAuth 2.0 which doesn't require
     * a specific authentication scheme header.
     *
     * @return null (OAuth 2.0 doesn't use traditional authentication schemes)
     */
    @Override
    public String getScheme() {
        return null;
    }

    /**
     * Gets the set of protocols supported by this authenticator.
     * Returns an empty set as this authenticator works with standard HTTP protocols
     * and doesn't require specific protocol support declarations.
     *
     * @return empty set (works with standard HTTP protocols)
     */
    @Override
    public Set<String> getSupportedProtocols() {
        return Set.of();
    }

    /**
     * Extracts the authenticated account ID from the incoming request.
     * Analyzes the request to determine the user account ID based on the request type:
     * - For OAuth callbacks: extracts user ID from the state parameter
     * - For webhook notifications: uses the current authorized account
     * - Handles both simple user ID and composite state (user ID + auth context)
     *
     * @param protoRequest the incoming request to analyze for authentication information
     * @return the authenticated user account ID
     * @throws RuntimeException if account ID extraction fails or request type is unsupported
     */
    @Override
    public String getAuthenticatedAccountId(ProtoRequest protoRequest) {
        try {
            HttpRequest httpRequest = (HttpRequest) protoRequest;
            if (httpRequest.getUri().getPath().equals(Constants.GMAIL_CALLBACK)) {
                httpRequest.bufferBody();
                MultivaluedMap<String, String> queryParameters = httpRequest.getQueryParameters();
                String state = String.valueOf(queryParameters.get(Constants.STATE).getFirst());
                String[] parts = state.split(Constants.HASH);
                return parts[0];
            } else if (httpRequest.getUri().getPath().equals(Constants.EMAIL_NOTIFICATION)) {
                return context.getAuthorizedAccount().getAccountId();
            } else {
                throw new RuntimeException("Failed to get authenticated userid.");
            }
        } catch (IOException cause) {
            throw new RuntimeException(Constants.FAILED_TO_AUTHENTICATE_USER + cause.getMessage(), cause.getCause());
        }
    }

    /**
     * Sets service-level authorization (not implemented).
     * This method is not used in the Gmail extension as it relies on user-specific
     * OAuth authentication rather than service-level authorization.
     *
     * @param s the service authorization string (unused)
     * @return false (service authorization not supported)
     */
    @Override
    public boolean setServiceAuthorization(String s) {
        return false;
    }

    /**
     * Gets attribute fields for authentication configuration.
     * Returns an empty map as this authenticator doesn't require additional
     * attribute configuration beyond the standard Gmail extension attributes.
     *
     * @return empty map (no additional attribute fields required)
     */
    @Override
    public Map<String, NamedField> getAttributeFields() {
        return Map.of();
    }

    /**
     * Generates response for authentication requirement (not implemented).
     * This method is not used as the Gmail extension handles authentication
     * through OAuth redirects rather than inline authentication responses.
     *
     * @param cause   the authentication exception that triggered this response
     * @param request the original request that required authentication
     * @return null (OAuth redirects used instead of inline responses)
     */
    @Override
    public ProtoResponse getMustAuthenticateResponse(MustAuthenticateException cause, ProtoRequest request) {
        return null;
    }

    /**
     * Generates authentication response for authentication requirement (not implemented).
     * This method is not used as the Gmail extension handles authentication
     * through OAuth redirects rather than custom authentication responses.
     *
     * @param cause the authentication exception that triggered this response
     * @return null (OAuth redirects used instead of custom responses)
     */
    @Override
    public AuthorizationResponse getMustAuthenticateResponse(MustAuthenticateException cause) {
        return null;
    }

    /**
     * Generates response for authorization requirement (not implemented).
     * This method is not used as the Gmail extension handles authorization
     * through OAuth redirects rather than inline authorization responses.
     *
     * @param cause   the authorization exception that triggered this response
     * @param request the original request that required authorization
     * @return null (OAuth redirects used instead of inline responses)
     */
    @Override
    public ProtoResponse getMustAuthorizeResponse(MustAuthorizeException cause, ProtoRequest request) {
        return null;
    }

    /**
     * Generates OAuth authorization response for authorization requirement.
     * Creates an OAuth authorization URL that redirects users to Google's authorization
     * server for Gmail API access. Handles both service account and user account
     * authentication contexts by selecting appropriate Gmail attributes and
     * constructing proper state parameters.
     * <p>
     * The method:
     * - Extracts user ID and optional auth context ID from exception details
     * - Selects appropriate Gmail attributes (stored vs current)
     * - Constructs state parameter for OAuth flow tracking
     * - Generates Google OAuth authorization URL with offline access
     * - Returns authorization response with redirect URL
     *
     * @param cause the authorization exception containing user and context information
     * @return AuthorizationResponse containing the OAuth authorization URL
     * @throws RuntimeException if authorization response generation fails
     */
    @Override
    public AuthorizationResponse getMustAuthorizeResponse(MustAuthorizeException cause) {
        try {
            String userId = (String) cause.getDetails().getFirst().getValue();
            Optional<NamedValuedField> authContextIdField = cause.getDetails().stream().filter(f -> Objects.equals(f.getName(), Constants.AUTH_CONTEXT_ID)).findFirst();

            GmailAttributes effectiveGmailAttributes;
            String state;
            if (authContextIdField.isPresent()) {
                String authContextId = (String) authContextIdField.get().getValue();
                effectiveGmailAttributes = gmailAttributeStore.load(authContextId);
                state = userId + Constants.HASH + authContextId;
            } else {
                effectiveGmailAttributes = gmailAttributes;
                state = userId;
            }

            OAuth20Service oAuth20Service = effectiveGmailAttributes.getOAuth20Service();
            String url = oAuth20Service.getAuthorizationUrl(state);
            String urlQueryParam = Constants.ACCESS_TYPE_OFFLINE_APPROVAL_PROMPT_FORCE;
            return new AuthorizationResponse(url + urlQueryParam, List.of());
        } catch (IOException ioException) {
            throw new RuntimeException(Constants.FAILED_TO_GET_AUTHORIZE_RESPONSE, ioException);
        }
    }

}
