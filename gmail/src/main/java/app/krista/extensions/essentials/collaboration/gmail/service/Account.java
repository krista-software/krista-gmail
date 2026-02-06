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

package app.krista.extensions.essentials.collaboration.gmail.service;

import java.io.IOException;
import java.util.List;
import java.util.Set;


/**
 * Interface for Gmail account operations and management.
 * Provides high-level access to Gmail account functionality including folder management,
 * email retrieval, search operations, and email composition. This interface abstracts
 * the complexity of Gmail API interactions and provides a simplified, consistent
 * interface for all account-level operations.
 * <p>
 * Implementations of this interface handle authentication, error management,
 * and proper integration with Gmail's label-based folder system.
 */
public interface Account {

    /**
     * Retrieves a folder by its display name.
     * Searches through all available Gmail labels/folders to find one matching the specified name.
     * The search is typically case-insensitive depending on the implementation.
     *
     * @param name the display name of the folder to retrieve
     * @return Folder object if found, null if no folder with the specified name exists
     */
    Folder getFolderByName(String name);

    /**
     * Retrieves the sent folder from the Gmail account.
     * Returns the folder containing emails that have been sent by the user.
     * This is typically the folder with the "SENT" label in Gmail.
     *
     * @return Folder object representing the sent folder
     */
    Folder getSentFolder();

    /**
     * Retrieves the inbox folder from the Gmail account.
     * Returns the folder containing incoming emails.
     * This is typically the folder with the "INBOX" label in Gmail.
     *
     * @return Folder object representing the inbox folder
     */
    Folder getInboxFolder();

    /**
     * Retrieves a folder by its unique identifier.
     * Uses the Gmail label ID to locate and return the corresponding folder.
     *
     * @param folderId the unique Gmail label ID of the folder to retrieve
     * @return Folder object if found, null if no folder with the specified ID exists
     */
    Folder getFolder(String folderId);

    /**
     * Retrieves a list of all folder names in the Gmail account.
     * Returns the display names of all available Gmail labels/folders.
     * This includes both system folders (Inbox, Sent, etc.) and user-created labels.
     *
     * @return list of folder display names, empty list if no folders are available or if retrieval fails
     */
    List<String> getFolderNames();

    /**
     * Retrieves a list of all folder IDs in the Gmail account.
     * Returns the unique Gmail label IDs for all available folders.
     * These IDs can be used with getFolder(String folderId) to retrieve specific folders.
     *
     * @return list of folder IDs, empty list if no folders are available or if retrieval fails
     */
    List<String> getFolderIds();

    /**
     * Retrieves a specific email by its message ID.
     * Fetches the complete email message including headers, body content, and attachments.
     *
     * @param emailId the unique Gmail message ID of the email to retrieve
     * @return Email object representing the message, null if no email with the specified ID exists
     */
    Email getEmail(String emailId);

    /**
     * Searches for emails using Gmail's search query syntax.
     * Supports Gmail's advanced search operators and filters for finding specific emails.
     * Examples of valid queries: "from:sender@example.com", "subject:meeting", "has:attachment"
     *
     * @param query the search query string using Gmail search syntax
     * @return list of Email objects matching the search criteria, empty list if no matches found
     */
    List<Email> searchEmails(String query);

    /**
     * Creates a new email builder for composing emails.
     * Returns an EmailBuilder instance that provides a fluent API for constructing
     * and sending new email messages with recipients, content, attachments, and other properties.
     *
     * @return EmailBuilder instance ready for email composition
     */
    EmailBuilder newEmail();

    /**
     * Retrieves all message IDs from the Gmail account.
     * Fetches the unique identifiers for all messages across all folders in the account.
     * This operation may be resource-intensive for accounts with large numbers of emails.
     *
     * @return set of all message IDs in the account
     * @throws IOException if the operation fails due to network issues or authentication problems
     */
    Set<String> fetchAllMessageIds() throws IOException;

}
