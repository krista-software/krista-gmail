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
import app.krista.extensions.essentials.collaboration.gmail.impl.util.Constants;
import app.krista.extensions.essentials.collaboration.gmail.service.Account;
import app.krista.extensions.essentials.collaboration.gmail.service.Email;
import app.krista.extensions.essentials.collaboration.gmail.service.Folder;
import app.krista.ksdk.context.RequestContext;
import com.google.api.client.googleapis.json.GoogleJsonResponseException;
import com.google.api.services.gmail.model.Label;
import com.google.api.services.gmail.model.Message;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Implementation of the Folder interface for Gmail labels/folders.
 * Provides functionality for managing Gmail folders including retrieving emails,
 * handling pagination, managing folder hierarchy, and performing folder operations.
 * <p>
 * Gmail uses labels as folders, and this class abstracts that complexity to provide
 * a traditional folder-like interface while handling Gmail's label-based organization.
 */
public class FolderImpl implements Folder {

    private static final Logger LOGGER = LoggerFactory.getLogger(FolderImpl.class);
    private final Account account;
    private final GmailProvider provider;
    private final Label mailFolder;
    private final RequestContext requestContext;
    private Folder parentFolder;

    /**
     * Constructor for creating a folder implementation.
     * Initializes the folder with account, provider, label data, and request context.
     *
     * @param account        the Gmail account this folder belongs to
     * @param provider       the Gmail provider for API access and authentication
     * @param mailFolder     the Gmail Label object representing this folder
     * @param requestContext the current request execution context
     */
    public FolderImpl(Account account, GmailProvider provider, Label mailFolder, RequestContext requestContext) {
        this.account = account;
        this.provider = provider;
        this.mailFolder = mailFolder;
        this.requestContext = requestContext;
    }

    /**
     * Gets the unique identifier of the folder.
     * Returns the Gmail label ID that uniquely identifies this folder.
     *
     * @return the folder/label ID
     */
    @Override
    public String getFolderId() {
        return mailFolder.getId();
    }

    /**
     * Gets the display name of the folder.
     * Returns the human-readable name of the Gmail label/folder.
     *
     * @return the folder display name
     */
    @Override
    public String getFolderName() {
        return mailFolder.getName();
    }

    /**
     * Gets the parent folder of this folder.
     * Searches for and returns the parent folder based on Gmail's label hierarchy.
     *
     * @return the parent Folder object, or null if this is a top-level folder
     */
    @Override
    public Folder getParent() {
        if (parentFolder == null) {
            String folderName = mailFolder.getName();
            String parentFolderName = folderName.substring(0, folderName.lastIndexOf("/"));
            List<Label> foldersList;
            try {
                foldersList = getAllFoldersList();
                List<Label> parentNameMatchingFodlers = foldersList.stream().filter(label -> label.getName().equalsIgnoreCase(parentFolderName)).collect(Collectors.toList());
                if (parentNameMatchingFodlers.isEmpty()) {
                    return null;
                }
                parentFolder = new FolderImpl(account, provider, parentNameMatchingFodlers.getFirst(), requestContext);
            } catch (IOException | GeneralSecurityException cause) {
                if (cause instanceof GoogleJsonResponseException && ((GoogleJsonResponseException) cause).getStatusCode() == 401) {
                    if (!requestContext.invokeAsUser()) {
                        throw new IllegalStateException("You are not authorized. Please ask admin to validate the attributes.");
                    } else {
                        throw provider.createMustAuthorizationException(provider.getUserId(false), true);
                    }
                }
                throw new RuntimeException(cause);
            }
        }
        return parentFolder;

    }

    /**
     * Retrieves all labels/folders from the Gmail account.
     * Internal method used for folder hierarchy operations.
     *
     * @return list of Label objects representing all folders
     * @throws IOException              if API call fails
     * @throws GeneralSecurityException if authentication fails
     */
    public List<Label> getAllFoldersList() throws IOException, GeneralSecurityException {
        return provider.getGmailClient().users().labels().list(Constants.ME).execute().getLabels();
    }

    @Override
    public List<String> getFolderPath() {
        Folder parentFolder = getParent();
        List<String> names = parentFolder != null ? parentFolder.getFolderPath() : new ArrayList<>(1);
        names.add(getFolderName());
        return names;
    }

    /**
     * Retrieves emails from this folder with pagination support.
     * Fetches emails belonging to this folder/label with specified page number and size.
     * Gmail API reference: <a href="https://developers.google.com/gmail/api/reference/rest/v1/users.messages/list">...</a>
     *
     * @param pageNumber the page number to retrieve (1-based)
     * @param pageSize   the number of emails per page
     * @return list of Email objects from the specified page
     */
    @Override
    public List<Email> getEmails(Double pageNumber, Double pageSize) {
        int intPageNumber = validatePageNumber(pageNumber);
        int intPageSize = validatePageSize(pageSize);
        long maxResults = (long) intPageNumber * intPageSize;
        try {
            List<Message> messages = provider.getGmailClient().users().messages().list(Constants.ME)
                    .setLabelIds(Collections.singletonList(mailFolder.getId())).setMaxResults(maxResults).execute().getMessages();
            int startIndex = (intPageNumber - 1) * intPageSize;
            if (messages != null && messages.size() < startIndex) {
                LOGGER.info("Not enough mails, mails got are: {}", messages.size());
                return List.of();
            }
            int endIndex = Math.min(startIndex + intPageSize + 1, Objects.requireNonNull(messages).size());
            List<Message> result = messages.subList(startIndex, endIndex);
            List<Email> emails = new ArrayList<>();
            result.forEach(message -> {
                try {
                    Message tempMessage = provider.getGmailClient().users().messages().get(Constants.ME, message.getId()).execute();
                    emails.add(new EmailImpl(provider, tempMessage, requestContext));
                } catch (IOException cause) {
                    LOGGER.error("Error occurred in get mail request :{}", cause.getMessage());
                }
            });
            return emails;
        } catch (IOException cause) {
            if (cause instanceof GoogleJsonResponseException && ((GoogleJsonResponseException) cause).getStatusCode() == 401) {
                if (!requestContext.invokeAsUser()) {
                    throw new IllegalStateException("You are not authorized. Please ask admin to validate the attributes.");
                } else {
                    throw provider.createMustAuthorizationException(provider.getUserId(false), true);
                }
            }
            throw new RuntimeException("Failed to get emails for folder. " + mailFolder.getName(), cause);
        }
    }

    /**
     * Validates and converts page size to integer.
     * Ensures page size is within valid range and converts from Double to int.
     *
     * @param pageSize the page size to validate
     * @return validated page size as integer
     * @throws IllegalArgumentException if page size is invalid
     */

    private int validatePageSize(Double pageSize) {
        if (pageSize == null) {
            return 15;
        } else if (pageSize < 1) {
            LOGGER.error("Got error for fetch mails. Invalid page size.");
            throw new IllegalArgumentException("Incorrect page size value for fetching mails");
        } else if (pageSize > 15) {
            LOGGER.error("Got error for fetch mails. Page size up to 15 messages is currently supported.");
            throw new IllegalArgumentException("Page size up to 15 messages is currently supported for fetch mail by label request");
        } else {
            return pageSize.intValue();
        }
    }

    /**
     * Validates and converts page number to integer.
     * Ensures page number is within valid range and converts from Double to int.
     *
     * @param pageNumber the page number to validate
     * @return validated page number as integer
     * @throws IllegalArgumentException if page number is invalid
     */

    private int validatePageNumber(Double pageNumber) {
        if (pageNumber == null) {
            return 1;
        } else if (pageNumber < 1) {
            LOGGER.error("Got error for fetch mails. Invalid page number.");
            throw new IllegalArgumentException("Incorrect page number value for fetching mails");
        } else {
            return pageNumber.intValue();
        }
    }

}
