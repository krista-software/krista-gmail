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

import app.krista.extensions.essentials.collaboration.gmail.impl.stores.KristaMediaClient;
import app.krista.model.base.File;

import java.util.List;

/**
 * Builder interface for composing Gmail emails using a fluent API.
 * Provides methods for setting email recipients, content, attachments, and other
 * email properties before sending. Uses the builder pattern to allow method chaining
 * and provide a clean, readable way to construct complex email messages.
 * <p>
 * This interface supports all standard email features including multiple recipient types,
 * rich content, file attachments, and custom reply-to addresses.
 */
public interface EmailBuilder {

    /**
     * Adds TO recipients to the email using a list of EmailAddress objects.
     * Sets the primary recipients who will receive the email message.
     *
     * @param emailAddress list of EmailAddress objects representing TO recipients
     * @return this EmailBuilder instance for method chaining
     */
    EmailBuilder withTo(List<EmailAddress> emailAddress);

    /**
     * Adds a single TO recipient using just the email address.
     * Convenience method for adding a TO recipient without a display name.
     *
     * @param emailAddress the email address string for the TO recipient
     * @return this EmailBuilder instance for method chaining
     */
    default EmailBuilder withTo(String emailAddress) {
        return withTo(List.of(new EmailAddress("", emailAddress)));
    }

    /**
     * Adds a single TO recipient with both display name and email address.
     * Convenience method for adding a TO recipient with a display name.
     *
     * @param name         the display name for the recipient
     * @param emailAddress the email address for the recipient
     * @return this EmailBuilder instance for method chaining
     */
    default EmailBuilder withTo(String name, String emailAddress) {
        return withTo(List.of(new EmailAddress(name, emailAddress)));
    }

    /**
     * Adds CC (Carbon Copy) recipients to the email using a list of EmailAddress objects.
     * Sets recipients who will receive a copy of the email and be visible to other recipients.
     *
     * @param emailAddress list of EmailAddress objects representing CC recipients
     * @return this EmailBuilder instance for method chaining
     */
    EmailBuilder withCc(List<EmailAddress> emailAddress);

    /**
     * Adds a single CC recipient using just the email address.
     * Convenience method for adding a CC recipient without a display name.
     *
     * @param emailAddress the email address string for the CC recipient
     * @return this EmailBuilder instance for method chaining
     */
    default EmailBuilder withCc(String emailAddress) {
        return withCc(List.of(new EmailAddress("", emailAddress)));
    }

    /**
     * Adds a single CC recipient with both display name and email address.
     * Convenience method for adding a CC recipient with a display name.
     *
     * @param name         the display name for the recipient
     * @param emailAddress the email address for the recipient
     * @return this EmailBuilder instance for method chaining
     */
    default EmailBuilder withCc(String name, String emailAddress) {
        return withCc(List.of(new EmailAddress(name, emailAddress)));
    }

    /**
     * Adds BCC (Blind Carbon Copy) recipients to the email using a list of EmailAddress objects.
     * Sets recipients who will receive a copy of the email but remain hidden from other recipients.
     *
     * @param emailAddress list of EmailAddress objects representing BCC recipients
     * @return this EmailBuilder instance for method chaining
     */
    EmailBuilder withBcc(List<EmailAddress> emailAddress);

    /**
     * Adds a single BCC recipient using just the email address.
     * Convenience method for adding a BCC recipient without a display name.
     *
     * @param emailAddress the email address string for the BCC recipient
     * @return this EmailBuilder instance for method chaining
     */
    default EmailBuilder withBcc(String emailAddress) {
        return withBcc(List.of(new EmailAddress("", emailAddress)));
    }

    /**
     * Adds a single BCC recipient with both display name and email address.
     * Convenience method for adding a BCC recipient with a display name.
     *
     * @param name         the display name for the recipient
     * @param emailAddress the email address for the recipient
     * @return this EmailBuilder instance for method chaining
     */
    default EmailBuilder withBcc(String name, String emailAddress) {
        return withBcc(List.of(new EmailAddress(name, emailAddress)));
    }

    /**
     * Sets the email content/body.
     * Defines the main message content that will be sent to recipients.
     * The content can be plain text or HTML depending on the implementation.
     *
     * @param content the text content for the email body
     * @return this EmailBuilder instance for method chaining
     */
    EmailBuilder withContent(String content);

    /**
     * Sets the email subject line.
     * Defines the subject that will appear in the email header and recipient's inbox.
     *
     * @param textContent the subject text for the email
     * @return this EmailBuilder instance for method chaining
     */
    EmailBuilder withSubject(String textContent);

    /**
     * Adds file attachments to the email.
     * Attaches files from Krista's file system to the email message.
     * The files are downloaded from Krista's media server and attached to the email.
     *
     * @param attachments       list of Krista File objects to attach to the email
     * @param kristaMediaClient client for downloading files from Krista's media server
     * @return this EmailBuilder instance for method chaining
     */
    EmailBuilder withAttachment(List<File> attachments, KristaMediaClient kristaMediaClient);

    /**
     * Sends the composed email.
     * Finalizes the email construction and sends it through Gmail API.
     * This method should be called after all email properties have been set.
     * Once called, the email is transmitted and cannot be modified.
     */
    void send();

    /**
     * Sets Reply-To addresses for the email.
     * Configures where replies to this email should be sent, which may be different
     * from the sender's address. This is useful for setting up automated responses
     * or directing replies to specific addresses.
     *
     * @param replyToAddress list of EmailAddress objects for the Reply-To header
     * @return this EmailBuilder instance for method chaining
     */
    EmailBuilder withReplyTo(List<EmailAddress> replyToAddress);

}
