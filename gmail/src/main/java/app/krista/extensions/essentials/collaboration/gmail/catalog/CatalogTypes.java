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

package app.krista.extensions.essentials.collaboration.gmail.catalog;

import app.krista.extensions.essentials.collaboration.gmail.catalog.entities.MailDetails;
import app.krista.extensions.essentials.collaboration.gmail.impl.stores.KristaMediaClient;
import app.krista.extensions.essentials.collaboration.gmail.impl.util.Validators;
import app.krista.extensions.essentials.collaboration.gmail.service.Attachment;
import app.krista.extensions.essentials.collaboration.gmail.service.Email;
import app.krista.extensions.essentials.collaboration.gmail.service.EmailAddress;
import app.krista.model.base.File;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

// To/From BO (Logical Data Model)
public final class CatalogTypes {

    private static final String DELIMITER = ",";
    private final KristaMediaClient kristaMediaClient;

    /**
     * Private constructor to prevent instantiation with KristaMediaClient.
     * This class is designed to be used with static methods only.
     *
     * @param kristaMediaClient the media client for file operations
     */
    private CatalogTypes(KristaMediaClient kristaMediaClient) {
        this.kristaMediaClient = kristaMediaClient;
    }

    /**
     * Converts an Email object to a MailDetails entity.
     * Maps all email properties including sender, recipients, content, attachments, and metadata.
     *
     * @param email             the Email object to convert (can be null)
     * @param kristaMediaClient the media client for downloading attachments
     * @return MailDetails entity with mapped email data (empty if email is null)
     */
    public static MailDetails fromEmail(Email email, KristaMediaClient kristaMediaClient) {
        MailDetails mailDetails = new MailDetails();
        if (email == null) return mailDetails;

        mailDetails.from = email.getSenderEmailAddress() != null ? email.getSenderEmailAddress().getEmailAddress() : null;
        mailDetails.to = getCommaSeparatedEmail(email.getToEmailAddresses());
        mailDetails.message = email.getContent();
        mailDetails.subject = email.getSubject();
        List<Attachment> attachments = email.getFileAttachments();
        mailDetails.fileAttachment = attachments.stream()
                .map(attachment -> attachment.download(kristaMediaClient))
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
        mailDetails.messageID = email.getEmailId();
        mailDetails.cc = getCommaSeparatedEmail(email.getCcEmailAddresses());
        mailDetails.bcc = getCommaSeparatedEmail(email.getBccEmailAddresses());
        mailDetails.isRead = email.getRead();
        mailDetails.replyTo = getCommaSeparatedEmail(email.getReplyToEmailAddresses());
        mailDetails.sendDateAndTime = email.getSendDateAndTime();
        mailDetails.receivedDateAndTime = email.getReceivedDateAndTime();

        return mailDetails;
    }

    /**
     * Converts a list of EmailAddress objects to a comma-separated string.
     * Returns empty string if the list is null or empty.
     *
     * @param emails list of EmailAddress objects to convert
     * @return comma-separated string of email addresses
     */
    private static String getCommaSeparatedEmail(List<EmailAddress> emails) {
        if (emails == null || emails.isEmpty()) {
            return "";
        }
        StringBuilder emailString = new StringBuilder();
        for (EmailAddress email : emails) {
            emailString.append(email.getEmailAddress()).append(DELIMITER);
        }
        emailString.setLength(emailString.length() - 1);
        return emailString.toString();
    }

    /**
     * Converts a comma-separated string of email addresses to a list of EmailAddress objects.
     * Validates each email address and throws IllegalArgumentException for invalid addresses.
     *
     * @param emailAddressesString comma-separated string of email addresses
     * @return list of valid EmailAddress objects
     * @throws IllegalArgumentException if any email address is invalid
     */
    public static List<EmailAddress> toEmailAddresses(String emailAddressesString) {
        if (emailAddressesString == null || emailAddressesString.isBlank()) {
            return List.of();
        }
        List<EmailAddress> emailAddresses = new ArrayList<>();
        for (String emailAddressString : emailAddressesString.split(DELIMITER)) {
            EmailAddress emailAddress = toEmailAddress(emailAddressString);
            if (!Validators.isStringNullOrBlank(emailAddressString) && Validators.isEmailValid(emailAddressString)) {
                emailAddresses.add(emailAddress);
            } else {
                throw new IllegalArgumentException("Mail address is not valid, please provide correct mail address.");
            }
        }
        return emailAddresses;
    }

    /**
     * Converts a single email address string to an EmailAddress object.
     * Returns null if the input is null or blank.
     *
     * @param emailAddressString the email address string to convert
     * @return EmailAddress object or null if input is invalid
     */
    private static EmailAddress toEmailAddress(String emailAddressString) {
        if (emailAddressString == null || emailAddressString.isBlank()) {
            return null;
        }
        // do conversion // "Tom Burger" <tom.burger@kritasoft.com>,  tom.burger@kritasoft.com
        return new EmailAddress("", emailAddressString);
    }

    /**
     * Converts a list of Krista File objects to Java File objects.
     * Downloads files using the provided KristaMediaClient.
     *
     * @param attachments       list of Krista File objects to convert
     * @param kristaMediaClient the media client for file conversion
     * @return list of Java File objects
     * @throws IOException if file conversion fails
     */
    public static List<java.io.File> toAttachments(List<File> attachments, KristaMediaClient kristaMediaClient) throws IOException {
        List<java.io.File> files = new ArrayList<>();

        if (attachments == null || attachments.isEmpty()) {
            return files;
        }
        for (File attachment : attachments) {
            java.io.File file = kristaMediaClient.toJavaFile(attachment);
            files.add(file);
        }
        return files;
    }
}
