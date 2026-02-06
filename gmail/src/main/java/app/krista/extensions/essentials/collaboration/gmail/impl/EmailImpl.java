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

import app.krista.extensions.essentials.collaboration.gmail.catalog.CatalogTypes;
import app.krista.extensions.essentials.collaboration.gmail.impl.connectors.GmailProvider;
import app.krista.extensions.essentials.collaboration.gmail.impl.connectors.GmailProviderFactory;
import app.krista.extensions.essentials.collaboration.gmail.impl.util.Constants;
import app.krista.extensions.essentials.collaboration.gmail.impl.util.Validators;
import app.krista.extensions.essentials.collaboration.gmail.service.Attachment;
import app.krista.extensions.essentials.collaboration.gmail.service.Email;
import app.krista.extensions.essentials.collaboration.gmail.service.EmailAddress;
import app.krista.extensions.essentials.collaboration.gmail.service.Folder;
import app.krista.ksdk.context.RequestContext;
import com.google.api.client.googleapis.json.GoogleJsonResponseException;
import com.google.api.services.gmail.Gmail;
import com.google.api.services.gmail.model.*;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.activation.FileDataSource;
import javax.inject.Inject;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.Session;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;


/**
 * Implementation of the Email interface for Gmail messages.
 * Provides comprehensive email functionality including reading message properties,
 * managing email state (read/unread), moving between folders, and composing replies.
 * <p>
 * This class handles the complexity of Gmail API message structure and provides
 * a simplified interface for email operations while managing authentication,
 * error handling, and proper email formatting for replies and forwards.
 */
public class EmailImpl implements Email {

    private static final Logger LOGGER = LoggerFactory.getLogger(EmailImpl.class);
    private final GmailProvider provider;
    private final Message message;
    private final RequestContext requestContext;


    public EmailImpl(GmailProviderFactory factory, Message message, RequestContext requestContext) {
        this(factory.create(), message, requestContext);
    }

    @Inject
    public EmailImpl(GmailProvider provider, Message message, RequestContext requestContext) {
        this.provider = provider;
        this.message = message;
        this.requestContext = requestContext;
    }

    private static void webLinkAdder(String attachmentProviderUrl, String attachmentFileName, List<String> attachmentList) {
        String webLink = "<a href=\"" + attachmentProviderUrl + "\">" + attachmentFileName + "</a>";
        attachmentList.add(webLink);
        LOGGER.info("Added ITEM Attachment : {}", webLink);
    }

    /**
     * Gets the unique identifier of the email message.
     *
     * @return the Gmail message ID
     */
    @Override
    public String getEmailId() {
        return message.getId();
    }

    /**
     * Extracts and returns the email subject line.
     * Searches through message headers to find the Subject header and returns its value.
     *
     * @return the email subject, or empty string if no subject is found
     */
    @Override
    public String getSubject() {
        List<MessagePartHeader> headers = message.getPayload().getHeaders();
        List<MessagePartHeader> subjectHeaders = new ArrayList<>();
        headers.forEach(messagePartHeader -> {
            if (messagePartHeader.getName().equalsIgnoreCase("Subject")) {
                subjectHeaders.add(messagePartHeader);
            }
        });
        if (subjectHeaders.isEmpty()) {
            return "";
        }
        MessagePartHeader messagePartHeader = subjectHeaders.getFirst();
        return messagePartHeader.getValue();
    }

    /**
     * Gets the sender's email address from the From header.
     * Extracts the first email address from the From header field.
     *
     * @return EmailAddress object representing the sender, or null if not found
     */
    @Override
    public EmailAddress getSenderEmailAddress() {
        List<EmailAddress> fromEmailAddress = getEmailAddresses("From");
        if (!fromEmailAddress.isEmpty()) {
            return fromEmailAddress.getFirst();
        }
        return null;
    }

    /**
     * Gets all TO recipients of the email.
     * Extracts email addresses from the To header field.
     *
     * @return list of EmailAddress objects representing TO recipients
     */
    @Override
    public List<EmailAddress> getToEmailAddresses() {
        return getEmailAddresses("To");
    }

    /**
     * Gets all Reply-To addresses of the email.
     * Extracts email addresses from the Reply-To header field.
     *
     * @return list of EmailAddress objects representing Reply-To addresses
     */
    @Override
    public List<EmailAddress> getReplyToEmailAddresses() {
        return getEmailAddresses("In-Reply-To");
    }

    /**
     * Gets all CC recipients of the email.
     * Extracts email addresses from the CC header field.
     *
     * @return list of EmailAddress objects representing CC recipients
     */
    @Override
    public List<EmailAddress> getCcEmailAddresses() {
        return getEmailAddresses("CC");
    }

    private List<EmailAddress> getEmailAddresses(String headerType) {
        MessagePart messagePart = message.getPayload();
        List<MessagePartHeader> headers = messagePart.getHeaders();
        List<MessagePartHeader> messagePartHeaders = new ArrayList<>();
        headers.forEach(messagePartHeader -> {
            if (messagePartHeader != null && messagePartHeader.getName().equalsIgnoreCase(headerType)) {
                messagePartHeaders.add(messagePartHeader);
            }
        });
        List<EmailAddress> result = new ArrayList<>();
        messagePartHeaders.forEach(header -> result.add(new EmailAddress(header.getName(), header.getValue())));
        return result;
    }

    /**
     * Extracts email addresses from message headers for all recipient types.
     * Internal method that searches for To, CC, and BCC headers and returns all found addresses.
     *
     * @return list of EmailAddress objects from all recipient headers
     */
    private List<EmailAddress> getEmailAddresses() {
        return message.getPayload().getHeaders().stream()
                .filter(Objects::nonNull)
                .filter(header -> header.getName().equalsIgnoreCase("To") ||
                        header.getName().equalsIgnoreCase("CC") ||
                        header.getName().equalsIgnoreCase("Bcc"))
                .map(header -> new EmailAddress(header.getName(), header.getValue()))
                .collect(Collectors.toList());
    }

    /**
     * Gets all BCC recipients of the email.
     * Extracts email addresses from the BCC header field.
     *
     * @return list of EmailAddress objects representing BCC recipients
     */
    @Override
    public List<EmailAddress> getBccEmailAddresses() {
        return getEmailAddresses("BCC");
    }

    /**
     * Checks if the email has been read.
     * Determines read status by checking if the UNREAD label is present.
     *
     * @return true if the email has been read (no UNREAD label), false otherwise
     */
    @Override
    public Boolean getRead() {
        return !message.getLabelIds().contains(Constants.UNREAD);
    }

    /**
     * Gets the timestamp when the email was sent.
     * Returns the internal date from Gmail which represents when the message was received by Gmail.
     *
     * @return timestamp in milliseconds since epoch
     */
    @Override
    public Long getSendDateAndTime() {
        return message.getInternalDate();
    }

    /**
     * Gets the timestamp when the email was received.
     * Returns the internal date from Gmail which represents when the message was received by Gmail.
     *
     * @return timestamp in milliseconds since epoch
     */
    @Override
    public Long getReceivedDateAndTime() {
        return message.getInternalDate();
    }

    @Override
    public String getContentType() {
        return message.getPayload().getMimeType();
    }

    @Override
    public String getContent() {
        List<MessagePart> messageParts = message.getPayload().getParts();
        if (messageParts == null) return "";
        StringBuilder stringBuilder = new StringBuilder();
        getMessageContent(messageParts, stringBuilder);
        return stringBuilder.toString();
    }

    @SuppressWarnings("deprecation")
    private void getMessageContent(List<MessagePart> messageParts, StringBuilder stringBuilder) {
        for (MessagePart messagePart : messageParts) {
            if (messagePart == null) continue;
            if ("text/html".equals(messagePart.getMimeType())) {
                byte[] bodyData = com.google.api.client.util.Base64.decodeBase64(messagePart.getBody().getData());
                if (bodyData != null) {
                    String content = new String(bodyData, StandardCharsets.UTF_8);
                    Document document = Jsoup.parse(content);
                    document.select("img").remove();
                    content = document.html();
                    stringBuilder.append(content);
                }
            }
            if (messagePart.getParts() != null && !messagePart.getParts().isEmpty()) {
                getMessageContent(messagePart.getParts(), stringBuilder);
            }
        }
    }

    /**
     * Marks the email as read by removing the UNREAD label.
     * Updates both the local message object and the Gmail server.
     */
    @Override
    public void markAsRead() {
        List<String> labelIds = message.getLabelIds();
        if (labelIds != null) {
            labelIds.remove(Constants.UNREAD);
        }
        try {
            ModifyMessageRequest mods = new ModifyMessageRequest().setRemoveLabelIds(Collections.singletonList(Constants.UNREAD));
            Message response = getMessages().modify(Constants.ME, message.getId(), mods).execute();
            LOGGER.info(Constants.GSON.toJson(response));
        } catch (IOException cause) {
            throwMustAuthorizationException((GoogleJsonResponseException) cause);
            throw new IllegalStateException("Failed to mark the message read", cause);
        }
    }

    /**
     * Marks the email as unread by adding the UNREAD label.
     * Updates both the local message object and the Gmail server.
     */
    @Override
    public void markAsUnread() {
        List<String> labelIds = message.getLabelIds();
        if (labelIds != null) {
            labelIds.add(Constants.UNREAD);
        } else {
            labelIds = new ArrayList<>(Collections.singletonList(Constants.UNREAD));
        }
        try {
            ModifyMessageRequest mods = new ModifyMessageRequest().setAddLabelIds(labelIds);
            Message response = getMessages().modify(Constants.ME, message.getId(), mods).execute();
            LOGGER.info(Constants.GSON.toJson(response));
        } catch (IOException cause) {
            throwMustAuthorizationException((GoogleJsonResponseException) cause);
            throw new IllegalStateException("Failed to mark the message unread", cause);
        }
    }

    /**
     * Moves the email to a specified folder.
     * Removes current labels (except sent and draft) and adds the new folder label.
     *
     * @param folder the target folder to move the email to
     */
    @Override
    public void moveToFolder(Folder folder) {
        Objects.requireNonNull(folder);
        List<String> labelIds = message.getLabelIds();
        List<String> newIds = labelIds.stream().filter(label -> !label.equalsIgnoreCase("sent") &&
                !label.equalsIgnoreCase("draft")).collect(Collectors.toList());
        message.getLabelIds().removeAll(message.getLabelIds());
        message.getLabelIds().add(folder.getFolderId());   //Add new folder Id
    }

    // TODO:  Add attachment data

    /**
     * Replies to the email with specified content and recipients.
     * Creates a reply message with proper threading and sends it.
     *
     * @param messageText the reply message content
     * @param attachments list of files to attach to the reply
     * @param cc          additional CC recipients for the reply
     * @param bcc         additional BCC recipients for the reply
     * @param to          additional TO recipients for the reply
     * @return Email object representing the sent reply
     */
    @Override
    public Email replyText(String messageText, List<File> attachments, String cc, String bcc, String to) {
        Message gmail = getMessageForReply(messageText, attachments, false, cc, bcc, to);
        try {
            getMessages().send(Constants.ME, gmail).execute();
            LOGGER.info("Successfully sent reply for message ID: {}", message.getId());
        } catch (IOException cause) {
            LOGGER.error("Failed to send reply for message ID: {}, error: {}", message.getId(), cause.getMessage());
            throwMustAuthorizationException((GoogleJsonResponseException) cause);
            throw new IllegalStateException("Failed to reply text message", cause);
        }
        return new EmailImpl(provider, gmail, requestContext);
    }

    // TODO:  Add attachment data

    /**
     * Replies to all recipients of the email with specified content.
     * Creates a reply-all message including all original recipients and sends it.
     *
     * @param messageText the reply message content
     * @param attachments list of files to attach to the reply
     * @param cc          additional CC recipients for the reply
     * @param bcc         additional BCC recipients for the reply
     * @param to          additional TO recipients for the reply
     * @return Email object representing the sent reply
     */
    @Override
    public Email replyToAll(String messageText, List<File> attachments, String cc, String bcc, String to) {
        Message messageForReply = getMessageForReply(messageText, attachments, true, cc, bcc, to);
        try {
            Message execute = getMessages().send(Constants.ME, messageForReply).execute();
            LOGGER.info(Constants.GSON.toJson(execute));
        } catch (IOException cause) {
            throwMustAuthorizationException((GoogleJsonResponseException) cause);
            throw new IllegalStateException("Failed to reply All text messageText", cause);
        }
        return new EmailImpl(provider, messageForReply, requestContext);
    }

    @Override
    public void forward(String message, String to) {
        if (message == null || message.isBlank() || to == null || to.isBlank()) {
            LOGGER.info(Constants.EMPTY_MESSAGE);
        }
        Message replyMessage = new Message();
        MessagePartBody messagePartBody = new MessagePartBody();
        messagePartBody.setData(message);
        MessagePart messagePart = new MessagePart();
        messagePart.setBody(messagePartBody);
        replyMessage.setLabelIds(List.of("To", Objects.requireNonNull(to)));
        replyMessage.setPayload(messagePart);
        try {
            Message execute = getMessages().send(Constants.ME, replyMessage).execute();
            LOGGER.info(Constants.GSON.toJson(execute));
        } catch (IOException cause) {
            throwMustAuthorizationException((GoogleJsonResponseException) cause);
            throw new IllegalStateException("Failed to forward message", cause);
        }
    }

    @Override
    public List<Attachment> getFileAttachments() {
        List<Attachment> attachmentList = new ArrayList<>();
        List<MessagePart> messageParts = message.getPayload().getParts();
        if (messageParts == null) {
            return List.of();
        }
        for (MessagePart messagePart : messageParts) {
            if (messagePart.getBody().getAttachmentId() != null && messagePart.getFilename() != null && !messagePart.getFilename().isBlank()) {
                Attachment attachment = new AttachmentImpl(provider, messagePart, message.getId());
                attachmentList.add(attachment);
            }
        }
        return attachmentList;
    }

    /**
     * This method parse the message headers which contains details of the ITEM attachment as below
     * {
     * "name": "Document-Reference",
     * "value": "https%3a%2f%2fkristasoft-my.sharepoint.com%2f%3aw%3a%2fp%2fdeepak_shingan%2fEcoFIS9cwS9PiHRjWfShPPIBSmiMKjM52h56ZZlZP-HNqA; FileName=Krista_New_Access_Request_Ticket_Template.docx; ProviderType=OneDrivePro; ProviderUrl=https%3a%2f%2fkristasoft-my.sharepoint.com%2fpersonal%2fdeepak_shingan_kristasoft_com; Permission=AnonymousEdit; ContentType=application%2fvnd.openxmlformats-officedocument.wordprocessingml.document"
     * }
     * @return a list of attachment web links (each entry contains a resolved link
     *         to the file using the extracted ProviderUrl and FileName).
     */
    @Override
    public List<String> getItemAttachments() {
        List<String> attachmentList = new LinkedList<>();
        List<MessagePartHeader> headers = message.getPayload().getHeaders();
        for (MessagePartHeader messagePartHeader : headers) {
            if (messagePartHeader.getName().equalsIgnoreCase("Document-Reference")) {
                String value = messagePartHeader.getValue();
                String[] itemAttachmentProperties = value.split(";");
                String attachmentFileName = "";
                String attachmentProviderUrl = "";
                for (String itemAttachmentProperty : itemAttachmentProperties) {
                    if (itemAttachmentProperty.contains("FileName")) {
                        String[] fileNameValue = itemAttachmentProperty.split("=");
                        attachmentFileName = fileNameValue[1];
                    } else if (itemAttachmentProperty.contains("ProviderUrl")) {
                        String[] providerUrl = itemAttachmentProperty.split("=");
                        attachmentProviderUrl = providerUrl[1];
                    }
                }
                webLinkAdder(attachmentProviderUrl, attachmentFileName, attachmentList);
            }
        }
        return attachmentList;
    }

    public void withAttachment(List<File> attachments, Multipart multipart) {
        if (attachments != null && !attachments.isEmpty()) {
            attachments.forEach(attachment -> {
                try {
                    MimeBodyPart mimeBodyPart = new MimeBodyPart();
                    DataSource source = new FileDataSource(attachment);
                    mimeBodyPart.setDataHandler(new DataHandler(source));
                    mimeBodyPart.setFileName(attachment.getName());
                    multipart.addBodyPart(mimeBodyPart);
                } catch (MessagingException cause) {
                    LOGGER.error("Error while processing attachment : {}", cause.getMessage());
                }
            });
        }
    }


    private String getMessageIdFromHeader() {
        List<MessagePartHeader> headers = message.getPayload().getHeaders();
        for (MessagePartHeader header : headers) {
            if (header.getName().equalsIgnoreCase("Message-ID")) {
                return header.getValue();
            }
        }
        return "";
    }

    private Message getMessageForReply(String messageText, List<File> attachments, boolean replyToAll, String cc, String bcc, String to) {
        try {
            MimeMessage result = createMimeMessage();

            addRecipients(result, replyToAll);
            addCcRecipients(result, cc);
            addBccRecipients(result, bcc);
            addToRecipient(result, to);
            addHeaders(result);
            addMessageContent(result, messageText, attachments);
            return encodeAndSetProperties(result);
        } catch (Exception cause) {
            LOGGER.error("Error occurred sending mail {}", cause.getMessage());
            handleException(cause);
        }
        return null;
    }

    private MimeMessage createMimeMessage() {
        Properties props = new Properties();
        Session session = Session.getDefaultInstance(props, null);
        return new MimeMessage(session);
    }

    private void addRecipients(MimeMessage message, boolean replyToAll) throws MessagingException {
        message.addRecipient(javax.mail.Message.RecipientType.TO,
                new InternetAddress(getEmailAddresses("From").getFirst().getEmailAddress()));

        if (replyToAll) {
            List<EmailAddress> emailAddresses = getEmailAddresses();
            for (EmailAddress address : emailAddresses) {
                validateAndAddRecipient(message, address.getEmailAddress(), javax.mail.Message.RecipientType.TO);
            }
        }
    }

    /**
     * Adds CC recipients to a MIME message with validation.
     * Parses CC string, validates email addresses, and adds them to the message.
     *
     * @param message the MIME message to add recipients to
     * @param cc      comma-separated string of CC email addresses
     * @throws MessagingException if adding recipients fails
     */
    private void addCcRecipients(MimeMessage message, String cc) throws MessagingException {
        if (cc != null && !cc.isBlank()) {
            List<EmailAddress> emailAddresses = CatalogTypes.toEmailAddresses(cc);
            for (EmailAddress emailAddress : emailAddresses) {
                validateAndAddRecipient(message, emailAddress.getEmailAddress(), javax.mail.Message.RecipientType.CC);
            }
        }
    }

    /**
     * Adds BCC recipients to a MIME message with validation.
     * Parses BCC string, validates email addresses, and adds them to the message.
     *
     * @param message the MIME message to add recipients to
     * @param bcc     comma-separated string of BCC email addresses
     * @throws MessagingException if adding recipients fails
     */
    private void addBccRecipients(MimeMessage message, String bcc) throws MessagingException {
        if (bcc != null && !bcc.isBlank()) {
            List<EmailAddress> emailAddresses = CatalogTypes.toEmailAddresses(bcc);
            for (EmailAddress emailAddress : emailAddresses) {
                validateAndAddRecipient(message, emailAddress.getEmailAddress(), javax.mail.Message.RecipientType.BCC);
            }
        }
    }

    private void addToRecipient(MimeMessage message, String to) throws MessagingException {
        if (to != null && !to.isBlank()) {
            List<EmailAddress> emailAddresses = CatalogTypes.toEmailAddresses(to);
            for (EmailAddress emailAddress : emailAddresses) {
                validateAndAddRecipient(message, emailAddress.getEmailAddress(), javax.mail.Message.RecipientType.TO);
            }
        }
    }

    /**
     * Validates an email address and adds it as a recipient to the message.
     * Checks email format before adding to prevent invalid recipients.
     *
     * @param message       the MIME message to add the recipient to
     * @param emailAddress  the email address to validate and add
     * @param recipientType the type of recipient (TO, CC, BCC)
     * @throws MessagingException if adding the recipient fails
     */
    private void validateAndAddRecipient(MimeMessage message, String emailAddress, javax.mail.Message.RecipientType recipientType) throws MessagingException {
        try {
            if (!Validators.isStringNullOrBlank(emailAddress) && Validators.isEmailValid(emailAddress)) {
                message.addRecipient(recipientType, new InternetAddress(emailAddress));
            } else {
                throw new IllegalArgumentException(Constants.INVALID_EMAIL_MESSAGE);
            }
        } catch (MessagingException cause) {
            LOGGER.error("Error occurred in adding recipient: {} {}", cause.getMessage(), cause);
        }
    }

    private void addHeaders(MimeMessage message) throws MessagingException {
        String messageIdFromHeader = getMessageIdFromHeader();
        message.addHeader("In-Reply-To", messageIdFromHeader);
        message.addHeader("References", messageIdFromHeader);
    }

    private void addMessageContent(MimeMessage message, String messageText, List<File> attachments) throws MessagingException, IOException {
        Multipart multipart = new MimeMultipart("mixed");
        MimeBodyPart textPart = new MimeBodyPart();
        textPart.setContent(messageText, "text/html");
        multipart.addBodyPart(textPart);
        message.setContent(multipart);
        message.setSubject(getSubject());
        withAttachment(attachments, multipart);
    }

    @SuppressWarnings("deprecation")
    private Message encodeAndSetProperties(MimeMessage result) throws MessagingException, IOException {
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        result.writeTo(buffer);
        byte[] rawMessageBytes = buffer.toByteArray();
        String encodedEmail = com.google.api.client.util.Base64.encodeBase64URLSafeString(rawMessageBytes);
        Message gmail = new Message();
        gmail.setThreadId(message.getThreadId());
        gmail.setRaw(encodedEmail);
        return gmail;
    }

    private void handleException(Exception cause) {
        LOGGER.error("Error occurred in Message for Reply request: {}", cause.getMessage());
        if (cause instanceof GoogleJsonResponseException) {
            throwMustAuthorizationException((GoogleJsonResponseException) cause);
        }
        throw new IllegalStateException(cause);
    }


    /**
     * Gets the Gmail Messages API client for making API calls.
     * Internal method for accessing Gmail API functionality.
     *
     * @return Gmail.Users.Messages client for API operations
     * @throws IllegalStateException if client creation fails
     */
    private Gmail.Users.Messages getMessages() {
        try {
            return provider.getGmailClient().users().messages();
        } catch (IOException cause) {
            LOGGER.error(cause.getMessage());
            throwMustAuthorizationException((GoogleJsonResponseException) cause);
            throw new IllegalStateException(cause);
        }
    }

    /**
     * Handles authentication exceptions and throws appropriate errors.
     * Checks for 401 unauthorized responses and creates proper exception types.
     *
     * @param cause the GoogleJsonResponseException to handle
     */
    private void throwMustAuthorizationException(GoogleJsonResponseException cause) {
        if (cause.getStatusCode() == 401) {
            if (!requestContext.invokeAsUser()) {
                throw new IllegalStateException("Access denied. Please contact your administrator to verify your permissions and validate the application attributes.");
            } else {
                throw provider.createMustAuthorizationException(provider.getUserId(false), true);
            }
        }
    }

}