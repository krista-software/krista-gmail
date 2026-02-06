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

package app.krista.extensions.essentials.collaboration.gmail.impl;

import app.krista.extensions.essentials.collaboration.gmail.impl.connectors.GmailProvider;
import app.krista.extensions.essentials.collaboration.gmail.impl.connectors.GmailProviderFactory;
import app.krista.extensions.essentials.collaboration.gmail.impl.stores.KristaMediaClient;
import app.krista.extensions.essentials.collaboration.gmail.impl.util.Constants;
import app.krista.extensions.essentials.collaboration.gmail.service.Account;
import app.krista.extensions.essentials.collaboration.gmail.service.Email;
import app.krista.extensions.essentials.collaboration.gmail.service.EmailBuilder;
import app.krista.extensions.essentials.collaboration.gmail.service.Folder;
import app.krista.ksdk.context.RequestContext;
import com.google.api.client.googleapis.json.GoogleJsonResponseException;
import com.google.api.services.gmail.Gmail;
import com.google.api.services.gmail.model.Label;
import com.google.api.services.gmail.model.ListMessagesResponse;
import com.google.api.services.gmail.model.Message;
import org.jvnet.hk2.annotations.ContractsProvided;
import org.jvnet.hk2.annotations.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.io.IOException;
import java.util.*;

@Service
@ContractsProvided(Account.class)
public class AccountImpl implements Account {

    private static final Logger LOGGER = LoggerFactory.getLogger(AccountImpl.class);
    private final GmailProvider provider;
    private final RequestContext requestContext;
    private final KristaMediaClient kristaMediaClient;

    @Inject
    public AccountImpl(GmailProviderFactory factory, RequestContext requestContext, KristaMediaClient kristaMediaClient) {
        this(factory.create(), requestContext, kristaMediaClient);
    }

    public AccountImpl(GmailProvider provider, RequestContext requestContext, KristaMediaClient kristaMediaClient) {
        this.provider = provider;
        this.requestContext = requestContext;
        this.kristaMediaClient = kristaMediaClient;
    }

    /**
     * Retrieves a folder by its name from the Gmail account.
     * Searches through all available labels/folders to find a match by name (case-insensitive).
     *
     * @param folderName the name of the folder to retrieve
     * @return Folder object if found, null if not found or if name is null/empty
     */
    @Override
    public Folder getFolderByName(String folderName) {
        try {
            if (folderName == null || folderName.isEmpty()) {
                LOGGER.info("Folder name is empty or null.");
                return null;
            }
            List<Label> labels = getUsers().labels().list(Constants.ME).execute().getLabels();
            if (labels.isEmpty()) {
                throw new IllegalStateException("No Labels found");
            } else {
                for (Label label : labels) {
                    if (label.getName().equalsIgnoreCase(folderName)) {
                        return new FolderImpl(this, provider, label, requestContext);
                    }
                }
            }
            LOGGER.info("Folder name not found.");
            return null;
        } catch (IOException cause) {
            LOGGER.error("Failed to get folder :{} ", cause.getMessage());
            throwMustAuthorizationException((GoogleJsonResponseException) cause);
            return null;
        }
    }

    /**
     * Retrieves the sent folder from the Gmail account.
     * Returns the folder containing sent emails.
     *
     * @return Folder object representing the sent folder
     */
    @Override
    public Folder getSentFolder() {
        return getFolderByName("sent");
    }

    /**
     * Retrieves the inbox folder from the Gmail account.
     * Returns the folder containing incoming emails.
     *
     * @return Folder object representing the inbox folder
     */
    @Override
    public Folder getInboxFolder() {
        return getFolderByName("inbox");
    }

    /**
     * Retrieves a folder by its unique identifier.
     * Searches through all available labels/folders to find a match by ID.
     *
     * @param folderId the unique identifier of the folder to retrieve
     * @return Folder object if found, null if not found or if folderId is null/empty
     */
    @Override
    public Folder getFolder(String folderId) {
        if (folderId == null || folderId.isEmpty()) {
            LOGGER.info("Folder Id is null or empty.");
            return null;
        }
        List<Label> labels = getAllFoldersList();
        if (labels.isEmpty()) {
            throw new IllegalStateException("No Labels found");
        } else {
            for (Label label : labels) {
                if (label.getId().equalsIgnoreCase(folderId)) {
                    return new FolderImpl(this, provider, label, requestContext);
                }
            }
        }
        LOGGER.info("Folder name not found.");
        return null;
    }

    /**
     * Retrieves a list of all folder names in the Gmail account.
     * Returns the display names of all available labels/folders.
     *
     * @return list of folder names, empty list if retrieval fails
     */
    @Override
    public List<String> getFolderNames() {
        try {
            List<Label> labels = getUsers().labels().list(Constants.ME).execute().getLabels();
            List<String> folderNames = new ArrayList<>();
            labels.forEach(label -> folderNames.add(label.getName()));
            return folderNames;
        } catch (IOException cause) {
            LOGGER.error("Failed to get folder names : {}", cause.getMessage());
            if (cause instanceof GoogleJsonResponseException && ((GoogleJsonResponseException) cause).getStatusCode() == 401) {
                if (!requestContext.invokeAsUser()) {
                    throw new IllegalStateException("You are not authorized. Please ask admin to validate the attributes.");
                } else {
                    throw provider.createMustAuthorizationException(provider.getUserId(false), true);
                }
            }
            return Collections.emptyList();
        }
    }

    /**
     * Retrieves a list of all folder IDs in the Gmail account.
     * Returns the unique identifiers of all available labels/folders.
     *
     * @return list of folder IDs, empty list if retrieval fails
     */
    @Override
    public List<String> getFolderIds() {
        List<String> folderIds = new ArrayList<>();
        List<Label> allFoldersList = getAllFoldersList();
        if (allFoldersList != null) {
            allFoldersList.forEach(folder -> folderIds.add(folder.getId()));
        }
        return folderIds;
    }

    /**
     * Retrieves all labels/folders from the Gmail account.
     * Internal method used by other folder-related operations.
     *
     * @return list of Label objects representing all folders, empty list if retrieval fails
     */
    public List<Label> getAllFoldersList() {
        try {
            return getUsers().labels().list(Constants.ME).execute().getLabels();
        } catch (IOException cause) {
            LOGGER.error("Failed to get folder names : {}", cause.getMessage());
            return List.of();
        }
    }

    /**
     * Retrieves a specific email by its message ID.
     * Fetches the complete email message including headers, body, and attachments.
     *
     * @param emailMessageId the unique identifier of the email message
     * @return Email object representing the message, null if not found
     */
    @Override
    public Email getEmail(String emailMessageId) {
        try {
            if (emailMessageId == null || emailMessageId.isEmpty()) {
                LOGGER.info("Message ID is empty or null.");
                return null;
            }
            Message message = getUsers().messages().get(Constants.ME, emailMessageId).execute();
            if (message == null) {
                LOGGER.info("No message found for message Id :{}", emailMessageId);
                return null;
            }
            return new EmailImpl(provider, message, requestContext);
        } catch (IOException cause) {
            LOGGER.error("No message found for message Id :{}", cause);
            throwMustAuthorizationException((GoogleJsonResponseException) cause);
            return null;
        }
    }

    /**
     * Retrieves all message IDs from the Gmail account.
     * Fetches identifiers for all messages across all folders.
     *
     * @return set of all message IDs in the account
     * @throws IOException if retrieval fails due to network or authentication issues
     */
    @Override
    public Set<String> fetchAllMessageIds() throws IOException {
        Set<String> messageIds = new HashSet<>();
        String userId = "me";
        String pageToken = null;

        do {
            Gmail.Users.Messages.List request = getUsers().messages().list(userId);
            if (pageToken != null) {
                request.setPageToken(pageToken);
            }
            request.setMaxResults(500L);  // Max = 500
            ListMessagesResponse response = request.execute();

            List<Message> messages = response.getMessages();
            if (messages != null) {
                for (Message msg : messages) {
                    messageIds.add(msg.getId());
                }
            }

            pageToken = response.getNextPageToken();
        } while (pageToken != null);

        return messageIds;
    }

    private void throwMustAuthorizationException(GoogleJsonResponseException cause) {
        if (cause.getStatusCode() == 401) {
            if (!requestContext.invokeAsUser()) {
                throw new IllegalStateException("You are not authorized. Please ask admin to validate the attributes.");
            } else {
                throw provider.createMustAuthorizationException(provider.getUserId(false), true);
            }
        }
    }

    /**
     * Searches for emails using Gmail search query syntax.
     * Supports Gmail's advanced search operators for filtering emails.
     *
     * @param query the search query using Gmail search syntax
     * @return list of Email objects matching the search criteria
     */
    @Override
    public List<Email> searchEmails(String query) {
        try {
            if (query == null || query.isEmpty()) {
                LOGGER.info("Search string is empty or null.");
                return Collections.emptyList();
            }
            List<Message> messages = getUsers().messages().list(Constants.ME)
                    .setQ(query).setMaxResults((long) 10).execute().getMessages();
            if (messages == null) {
                LOGGER.info("Message is null.");
                return List.of();
            }
            List<Message> result = new ArrayList<>();
            for (Message message : messages) {
                Message execute = getUsers().messages().get(Constants.ME, message.getId()).execute();
                result.add(execute);
            }
            List<Email> emails = new ArrayList<>();
            result.forEach(message -> emails.add(new EmailImpl(provider, message, requestContext)));
            return emails;
        } catch (IOException cause) {
            LOGGER.error("Failed to get folder names :{}", cause.getMessage());
            throwMustAuthorizationException((GoogleJsonResponseException) cause);
            return List.of();
        }
    }

    /**
     * Creates a new email builder for composing emails.
     * Returns an EmailBuilder instance for fluent email composition.
     *
     * @return EmailBuilder instance for creating new emails
     */
    @Override
    public EmailBuilder newEmail() {
        return EmailBuilderImpl.create(provider, requestContext, kristaMediaClient);
    }

    /**
     * Gets the Gmail Users API client for making API calls.
     * Internal method for accessing Gmail API functionality.
     *
     * @return Gmail.Users client for API operations
     * @throws IllegalStateException if client creation fails
     */
    private Gmail.Users getUsers() {
        try {
            return provider.getGmailClient().users();
        } catch (IOException cause) {
            throw new IllegalStateException(cause);
        }
    }

}
