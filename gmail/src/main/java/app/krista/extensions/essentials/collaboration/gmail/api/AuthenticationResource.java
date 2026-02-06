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

package app.krista.extensions.essentials.collaboration.gmail.api;

import app.krista.extensions.essentials.collaboration.gmail.GmailAttributes;
import app.krista.extensions.essentials.collaboration.gmail.impl.connectors.GmailProvider;
import app.krista.extensions.essentials.collaboration.gmail.impl.connectors.GmailProviderFactory;
import app.krista.extensions.essentials.collaboration.gmail.impl.stores.GmailAttributeStore;
import app.krista.extensions.essentials.collaboration.gmail.impl.stores.RefreshTokenStore;
import app.krista.extensions.essentials.collaboration.gmail.impl.util.Constants;
import app.krista.extensions.util.EventHandler;
import app.krista.ksdk.authentication.AuthorizationListener;
import app.krista.ksdk.context.AuthorizationContext;
import app.krista.model.base.FreeForm;
import com.github.scribejava.core.exceptions.OAuthException;
import com.github.scribejava.core.model.OAuth2AccessToken;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ExecutionException;

/**
 * REST resource for handling Gmail authentication and webhook notifications.
 * Provides endpoints for OAuth callback processing and email alert webhooks.
 */
@Path("/")
public final class AuthenticationResource {

    private static final String CODE = "code";

    private static final String STATE = "state";
    private static final Logger LOGGER = LoggerFactory.getLogger(AuthenticationResource.class);
    private final GmailAttributeStore gmailAttributeStore;
    private final RefreshTokenStore refreshTokenStore;
    private final GmailProviderFactory providerFactory;
    private final AuthorizationListener authorizationListener;
    private final AuthorizationContext context;
    private final EventHandler eventHandler;


    @Inject
    public AuthenticationResource(GmailAttributeStore gmailAttributeStore, RefreshTokenStore refreshTokenStore, GmailProviderFactory providerFactory, AuthorizationListener authorizationListener, AuthorizationContext context, EventHandler eventHandler) {
        this.gmailAttributeStore = gmailAttributeStore;
        this.refreshTokenStore = refreshTokenStore;
        this.providerFactory = providerFactory;
        this.authorizationListener = authorizationListener;
        this.context = context;
        this.eventHandler = eventHandler;
    }

    /**
     * Handles incoming email notifications from Gmail webhook.
     * Processes new email alerts and triggers appropriate events.
     *
     * @param newEmail JSON string containing new email notification data
     * @return Response indicating success or failure of webhook processing
     */
    @POST
    @Path("/webhook")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response newEmailAlert(String newEmail) {
        if (newEmail != null) {
            try {
                FreeForm freeForm = new FreeForm();
                freeForm.put(Constants.NEW_EMAIL_UPDATE, "Text", newEmail);
                eventHandler.handleEvent(Constants.GMAIL_UPDATE, freeForm);
                return Response.status(Response.Status.OK).entity("Webhook received and processed").build();
            } catch (Exception cause) {
                LOGGER.error("Error while processing email {}", cause.getMessage());
                return Response.serverError().entity("Error processing email").build();
            }
        }
        return Response.status(Response.Status.BAD_REQUEST).entity("Invalid request").build();
    }

    /**
     * Handles OAuth callback from Gmail authorization flow.
     * Processes authorization code and state parameters to complete authentication.
     *
     * @param code  OAuth authorization code from Gmail
     * @param state State parameter containing user and context information
     * @return String message indicating authentication result
     */
    @GET
    @Path("/callback")
    public String getCallBack(@QueryParam(CODE) String code, @QueryParam(STATE) String state) {
        Objects.requireNonNull(code);
        String[] parts = state.split(Constants.HASH);
        if (parts[0].isBlank() || parts.length > 3) {
            throw new BadRequestException("Invalid state parameters!");
        }
        String key = parts[0] + Constants.HASH + parts[1];
        String authContextId = parts.length == 3 ? parts[2] : null;

        try {
            GmailProvider clientProvider = getGmailClientProvider(authContextId);
            OAuth2AccessToken accessToken = clientProvider.getGmailAttributes().getOAuth20Service().getAccessToken(code);
            String refreshTokenString = accessToken.getRefreshToken();
            String accessTokenString = accessToken.getAccessToken();
            String refreshTokenStored = refreshTokenStore.get(key);

            // Google Provides 1 refresh token at a time
            // For studio user key starts with setup emailId
            // For client user key starts with wsContact
            // if the refresh token fetched is for client then store access token
            if (key.startsWith(Constants.WS_CONTACT) && refreshTokenString == null) {
                refreshTokenStore.put(key, accessTokenString); // Storing access token for client
            }
            // if the refresh token stored for studio user is access token then revoke the token
            else if (refreshTokenStored != null && refreshTokenStored.startsWith("ya29")) {
                LOGGER.error("Refresh token access removed. Re-authorization is needed.");
                // Revoking access token to get refresh token for existing studio users
                clientProvider.getGmailAttributes().getOAuth20Service().revokeToken(accessToken.getAccessToken());
                refreshTokenStore.remove(key);
                return "We need to remove your access token permissions. Please validate again to regain access.";
            }
            // if the refresh token is not null and key doesn't start with wsContact
            // store refresh token for studio user
            else {
                refreshTokenStore.put(key, refreshTokenString);
            }

            if (!key.startsWith(Constants.WS_CONTACT) && !hasUserAccess(clientProvider)) {
                refreshTokenStore.remove(key);
                return "Unauthorised user. User email '" + key.split("#")[0] + "' configured in the setup does not match with authenticated user.";
            }
            if (context.isAuthenticated()) {
                authorizationListener.authorized();
                return "User authenticated successfully.";
            }
            return "User authenticated successfully. Save the changes.";
        } catch (OAuthException cause) {
            throw new IllegalStateException(getErrorDescription(cause), cause.getCause());
        } catch (IOException | ExecutionException cause) {
            throw new IllegalStateException("Error occurred during authorization ", cause.getCause());
        } catch (InterruptedException interruptedException) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Error occurred during authorization ", interruptedException.getCause());
        } finally {
            if (authContextId != null) {
                gmailAttributeStore.remove(authContextId);
            }
        }
    }

    /**
     * Creates Gmail client provider based on authentication context.
     *
     * @param authContextId Optional authentication context identifier
     * @return GmailProvider instance configured for the context
     * @throws IOException if provider creation fails
     */
    private GmailProvider getGmailClientProvider(String authContextId) throws IOException {
        GmailProvider clientProvider;
        if (authContextId == null) {
            clientProvider = providerFactory.create();
        } else {
            GmailAttributes effectiveGmailAttributes = gmailAttributeStore.load(authContextId);
            clientProvider = providerFactory.create(effectiveGmailAttributes);
        }
        return clientProvider;
    }

    /**
     * Extracts error description from OAuth exception.
     *
     * @param cause OAuth exception containing error details
     * @return Formatted error description string
     */
    private String getErrorDescription(OAuthException cause) {
        String errorDescription = "Error occurred during authorization. ";
        if (cause.getMessage() != null && !cause.getMessage().isBlank()) {
            var exceptionMap = Constants.GSON.fromJson(cause.getMessage(), Map.class);
            if (exceptionMap.containsKey("error")) {
                errorDescription += exceptionMap.get("error") + ". ";
            }
            if (exceptionMap.containsKey("error_description")) {
                errorDescription += exceptionMap.get("error_description");
            }
        }
        return errorDescription;
    }

    /**
     * Verifies user has access to Gmail account.
     * Attempts to fetch user profile to validate permissions.
     *
     * @param gmailProvider Gmail provider instance to test
     * @return true if user has access, false otherwise
     */
    private boolean hasUserAccess(GmailProvider gmailProvider) {
        try {
            gmailProvider.getGmailClientForAdmin().users().getProfile(gmailProvider.getGmailAttributes().getMailId()).execute();
            return true;
        } catch (IOException cause) {
            LOGGER.error("-------IGNORED EXCEPTION-----{}", cause.getMessage());
            return false;
        }
    }

}
