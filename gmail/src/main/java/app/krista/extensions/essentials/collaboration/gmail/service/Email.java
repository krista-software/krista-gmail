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

import javax.mail.MessagingException;
import java.io.File;
import java.io.IOException;
import java.util.List;

/**
 * Interface for Gmail email message operations and properties.
 * Provides comprehensive access to email content, metadata, and management functions.
 * Supports reading email properties, managing email state, moving between folders,
 * and composing replies and forwards.
 * <p>
 * This interface abstracts the complexity of Gmail's message structure and provides
 * a simplified, consistent API for email operations while maintaining full functionality
 * for email management tasks.
 */
public interface Email {

    /**
     * Gets the unique identifier of the email message.
     * Returns the Gmail message ID that uniquely identifies this email within the account.
     *
     * @return the Gmail message ID
     */
    String getEmailId();

    /**
     * Gets the subject line of the email.
     * Extracts and returns the subject text from the email headers.
     *
     * @return the email subject line, or empty string if no subject is present
     */
    String getSubject();

    /**
     * Gets the sender's email address.
     * Extracts the sender information from the "From" header field.
     *
     * @return EmailAddress object representing the sender, or null if sender information is not available
     */
    EmailAddress getSenderEmailAddress();

    /**
     * Gets all TO recipients of the email.
     * Extracts all email addresses from the "To" header field.
     *
     * @return list of EmailAddress objects representing TO recipients, empty list if no TO recipients
     */
    List<EmailAddress> getToEmailAddresses();

    /**
     * Gets all Reply-To addresses of the email.
     * Extracts email addresses from the "Reply-To" header field, which specify
     * where replies should be sent instead of the original sender.
     *
     * @return list of EmailAddress objects representing Reply-To addresses, empty list if no Reply-To addresses
     */
    List<EmailAddress> getReplyToEmailAddresses();

    /**
     * Gets all CC (Carbon Copy) recipients of the email.
     * Extracts all email addresses from the "CC" header field.
     *
     * @return list of EmailAddress objects representing CC recipients, empty list if no CC recipients
     */
    List<EmailAddress> getCcEmailAddresses();

    /**
     * Gets all BCC (Blind Carbon Copy) recipients of the email.
     * Extracts all email addresses from the "BCC" header field.
     * Note: BCC information may not be available in received emails for privacy reasons.
     *
     * @return list of EmailAddress objects representing BCC recipients, empty list if no BCC recipients
     */
    List<EmailAddress> getBccEmailAddresses();

    /**
     * Checks if the email has been read.
     * Determines the read status based on Gmail's UNREAD label presence.
     *
     * @return true if the email has been read, false if it's still unread
     */
    Boolean getRead();

    /**
     * Gets the timestamp when the email was sent.
     * Returns the date and time when the email was originally sent by the sender.
     *
     * @return timestamp in milliseconds since epoch representing the send time
     */
    Long getSendDateAndTime();

    /**
     * Gets the timestamp when the email was received.
     * Returns the date and time when the email was received by the Gmail server.
     *
     * @return timestamp in milliseconds since epoch representing the received time
     */
    Long getReceivedDateAndTime();

    /**
     * Gets the content type of the email.
     * Returns the MIME type that describes the format of the email content.
     *
     * @return the content type string (e.g., "text/plain", "text/html")
     */
    String getContentType();

    /**
     * Gets the main content/body of the email.
     * Extracts and returns the email message content, which may be in plain text or HTML format.
     *
     * @return the email content/body text
     */
    String getContent();

    /**
     * Marks the email as read.
     * Updates the email status by removing the UNREAD label, both locally and on the Gmail server.
     * This operation is reflected immediately in the Gmail interface.
     */
    void markAsRead();

    /**
     * Marks the email as unread.
     * Updates the email status by adding the UNREAD label, both locally and on the Gmail server.
     * This operation is reflected immediately in the Gmail interface.
     */
    void markAsUnread();

    /**
     * Moves the email to a specified folder.
     * Changes the email's label assignments to move it from its current location to the target folder.
     * This operation updates the email's folder membership in Gmail.
     *
     * @param folder the target Folder object where the email should be moved
     */
    void moveToFolder(Folder folder);

    /**
     * Replies to the email with specified content and optional additional recipients.
     * Creates and sends a reply message that references the original email thread.
     * The reply is sent to the original sender unless additional recipients are specified.
     *
     * @param message     the reply message content
     * @param attachments list of File objects to attach to the reply (can be null or empty)
     * @param cc          additional CC recipients for the reply (can be null)
     * @param bcc         additional BCC recipients for the reply (can be null)
     * @param to          additional TO recipients for the reply (can be null)
     * @return Email object representing the sent reply message
     */
    Email replyText(String message, List<File> attachments, String cc, String bcc, String to);

    /**
     * Replies to all recipients of the original email.
     * Creates and sends a reply message to all original recipients (TO, CC) plus the sender.
     * This is equivalent to using "Reply All" in a standard email client.
     *
     * @param message     the reply message content
     * @param attachments list of File objects to attach to the reply (can be null or empty)
     * @param cc          additional CC recipients for the reply (can be null)
     * @param bcc         additional BCC recipients for the reply (can be null)
     * @param to          additional TO recipients for the reply (can be null)
     * @return Email object representing the sent reply message
     * @throws IOException        if the reply operation fails due to network or authentication issues
     * @throws MessagingException if the reply message construction fails
     */
    Email replyToAll(String message, List<File> attachments, String cc, String bcc, String to) throws IOException, MessagingException;

    /**
     * Forwards the email to specified recipients.
     * Creates and sends a forward message that includes the original email content
     * along with additional message text from the forwarder.
     *
     * @param message additional message text to include with the forwarded email
     * @param to      comma-separated string of recipient email addresses for the forward
     */
    void forward(String message, String to);

    List<Attachment> getFileAttachments();

    List<String> getItemAttachments();
}
