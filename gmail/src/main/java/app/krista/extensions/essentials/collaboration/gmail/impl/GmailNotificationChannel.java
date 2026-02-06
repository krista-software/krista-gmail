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


import app.krista.extension.executor.Invoker;
import app.krista.extensions.essentials.collaboration.gmail.GmailAttributes;
import app.krista.extensions.essentials.collaboration.gmail.catalog.CatalogTypes;
import app.krista.extensions.essentials.collaboration.gmail.catalog.entities.MailDetails;
import app.krista.extensions.essentials.collaboration.gmail.impl.connectors.GmailProvider;
import app.krista.extensions.essentials.collaboration.gmail.impl.connectors.GmailProviderFactory;
import app.krista.extensions.essentials.collaboration.gmail.impl.stores.HistoryIdStore;
import app.krista.extensions.essentials.collaboration.gmail.impl.stores.KristaMediaClient;
import app.krista.extensions.essentials.collaboration.gmail.service.Account;
import app.krista.extensions.essentials.collaboration.gmail.service.Email;
import com.google.api.services.gmail.Gmail;
import com.google.api.services.gmail.model.*;
import org.jvnet.hk2.annotations.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Service for managing Gmail push notifications and email synchronization.
 * Handles Gmail watch requests, processes notification webhooks, and manages
 * email history tracking for real-time email updates. Integrates with Google Cloud
 * Pub/Sub for receiving Gmail change notifications.
 * <p>
 * This class provides functionality for setting up Gmail push notifications,
 * processing incoming notifications, and retrieving new emails based on
 * Gmail's history API for efficient synchronization.
 */
@Service
public class GmailNotificationChannel {

    public static final String INBOX = "INBOX";

    public static final String TO_BE_USED_HISTORY_ID = "toBeUsedHistoryID";

    public static final String SENT = "SENT";

    public static final String INCLUDE = "include";
    private static final Logger LOGGER = LoggerFactory.getLogger(GmailNotificationChannel.class);
    private final String emailID;
    private final GmailProvider provider;
    private final GmailProviderFactory clientProviderFactory;
    private final Account account;
    private final HistoryIdStore historyIdStore;
    private final KristaMediaClient kristaMediaClient;

    /**
     * Constructor for dependency injection.
     * Initializes the notification channel with required dependencies for Gmail operations.
     *
     * @param account           the Gmail account for email operations
     * @param historyIdStore    store for managing Gmail history IDs
     * @param kristaMediaClient client for file operations
     */
    @Inject
    public GmailNotificationChannel(GmailProviderFactory factory, Account account, HistoryIdStore historyIdStore, Invoker invoker, KristaMediaClient kristaMediaClient) {
        clientProviderFactory = factory;
        this.emailID = String.valueOf(invoker.getAttributes().get(GmailAttributes.EMAIL));
        this.provider = factory.create();
        this.account = account;
        this.historyIdStore = historyIdStore;
        this.kristaMediaClient = kristaMediaClient;
    }

    /**
     * Adds message IDs from Gmail history to the provided set.
     * Processes history entries and extracts message IDs from messages that were added.
     *
     * @param history    list of Gmail History objects containing message changes
     * @param messageIds set to add the extracted message IDs to
     */
    private static void addMessageIDS(List<History> history, Set<String> messageIds) {
        for (History eachHistory : history) {
            if (eachHistory == null || eachHistory.getMessagesAdded() == null) {
                continue;
            }
            for (HistoryMessageAdded messageAdded : eachHistory.getMessagesAdded()) {
                if (messageAdded == null) {
                    continue;
                }
                Message message = messageAdded.getMessage();
                extractMessageIds(messageIds, message);
            }
        }
    }

    /**
     * Extracts message ID from a Gmail message if it's not a sent message.
     * Filters out sent messages and adds valid message IDs to the collection.
     *
     * @param messageIds set to add the message ID to
     * @param message    the Gmail Message object to extract ID from
     */
    private static void extractMessageIds(Set<String> messageIds, Message message) {
        if (message != null && !isMailSent(message)) {
            String id = message.getId();
            if (id != null) {
                messageIds.add(id);
            }
        }
    }

    /**
     * Checks if a Gmail message is a sent message.
     * Determines if the message has the "SENT" label indicating it was sent by the user.
     *
     * @param message the Gmail Message object to check
     * @return true if the message is a sent message, false otherwise
     */
    private static boolean isMailSent(Message message) {
        return message != null && message.getLabelIds() != null && message.getLabelIds().contains("SENT");
    }

    /**
     * Initiates Gmail push notifications for the specified Gmail attributes.
     * Sets up a watch request to receive notifications when the Gmail mailbox changes.
     *
     * @param gmailAttributes the Gmail configuration containing topic and email settings
     * @return WatchResponse containing the watch configuration details
     */
    public WatchResponse initiate(GmailAttributes gmailAttributes) {
        return getWatchResponse(gmailAttributes);
    }

    /**
     * Retrieves all new emails based on Gmail history changes.
     * Fetches message IDs from history and converts them to MailDetails objects.
     *
     * @return list of MailDetails objects representing new emails
     */
    public List<MailDetails> getAllNewMails() {
        List<MailDetails> allNewMailDetails = new ArrayList<>();
        Set<String> ids = messageIds();
        for (String messageIds : ids) {
            Email email = account.getEmail(messageIds);
            if (email == null) {
                continue;
            }
            MailDetails mailDetails = CatalogTypes.fromEmail(email, kristaMediaClient);
            allNewMailDetails.add(mailDetails);
        }
        return allNewMailDetails;
    }

    /**
     * Creates and executes a Gmail watch request.
     * Sets up push notifications for the specified Gmail topic and email account.
     *
     * @param gmailAttributes the Gmail configuration containing topic and email settings
     * @return WatchResponse with watch configuration, or empty response if setup fails
     */
    private WatchResponse getWatchResponse(GmailAttributes gmailAttributes) {
        try {
            if (gmailAttributes.getTopic() != null && !gmailAttributes.getTopic().isEmpty()) {
                Gmail service = initializeGmailServiceAdmin(gmailAttributes);
                WatchRequest watchRequest = getWatchRequest(gmailAttributes.getTopic());
                return service.users().watch(gmailAttributes.getMailId(), watchRequest).execute();
            }

        } catch (IOException cause) {
            LOGGER.error("Failed to setup mail alert event - {}", cause.getMessage());
            throw new IllegalArgumentException("Invalid topic name format. Should follow projects/my-project-id/topics/my-topic-id");
        }
        return new WatchResponse();
    }

    private WatchRequest getWatchRequest(String topicName) {
        WatchRequest watchRequest = new WatchRequest();
        watchRequest.setLabelIds(List.of(INBOX, SENT));
        LOGGER.info("Topic Name : :{}", topicName);
        watchRequest.setTopicName(topicName);
        watchRequest.setLabelFilterBehavior(INCLUDE);
        return watchRequest;
    }

    private Gmail initializeGmailService() {
        try {
            return provider.getGmailClient();
        } catch (IOException cause) {
            throw new IllegalStateException(cause);
        }
    }

    private Gmail initializeGmailServiceAdmin(GmailAttributes gmailAttributes) {
        try {
            return clientProviderFactory.create(gmailAttributes).getGmailClientForAdmin();
        } catch (IOException cause) {
            LOGGER.error("Error occurred while initializing Gmail Admin Service :{}", cause.getMessage());
            throw new IllegalStateException(cause.getMessage(), cause);
        }
    }

    private Set<String> messageIds() {
        Set<String> messageIds = new HashSet<>();
        ListHistoryResponse execute = null;
        Gmail service = initializeGmailService();
        try {
            execute = service.users().history().list(emailID).setStartHistoryId(historyIdStore.get(TO_BE_USED_HISTORY_ID)).execute();
            String id = String.valueOf(execute.getHistoryId());
            historyIdStore.put(TO_BE_USED_HISTORY_ID, id);
            List<History> history = execute.getHistory();
            if (history != null && !history.isEmpty()) {
                addMessageIDS(history, messageIds);
            }
        } catch (IOException cause) {
            throw new IllegalStateException(cause.getMessage());
        }
        return (messageIds);
    }

}