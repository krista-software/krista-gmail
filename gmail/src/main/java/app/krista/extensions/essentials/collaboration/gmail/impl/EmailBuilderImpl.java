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
import app.krista.extensions.essentials.collaboration.gmail.impl.stores.KristaMediaClient;
import app.krista.extensions.essentials.collaboration.gmail.impl.util.Constants;
import app.krista.extensions.essentials.collaboration.gmail.impl.util.InlineImageConverter;
import app.krista.extensions.essentials.collaboration.gmail.service.EmailAddress;
import app.krista.extensions.essentials.collaboration.gmail.service.EmailBuilder;
import app.krista.ksdk.context.RequestContext;
import app.krista.model.base.File;
import com.google.api.client.googleapis.json.GoogleJsonResponseException;
import com.google.api.services.gmail.model.Message;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.activation.FileDataSource;
import javax.mail.Address;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.Session;
import javax.mail.internet.*;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.*;

/**
 * Implementation of the EmailBuilder interface for composing Gmail emails.
 * Provides a fluent API for building email messages with recipients, content, attachments,
 * and other email properties. Uses JavaMail API for message construction and Gmail API for sending.
 * <p>
 * This class handles the complexity of email composition including MIME message creation,
 * attachment handling, and proper encoding for Gmail API transmission.
 */
public class EmailBuilderImpl implements EmailBuilder {

    private static final Logger LOGGER = LoggerFactory.getLogger(EmailBuilderImpl.class);
    private final MimeMessage message;
    private final GmailProvider provider;
    private final Multipart multipart;
    private final RequestContext requestContext;
    private final KristaMediaClient kristaMediaClient;
    private final InlineImageConverter inlineImageConverter;

    public EmailBuilderImpl(GmailProvider provider, MimeMessage message, RequestContext requestContext, KristaMediaClient kristaMediaClient) {
        this.provider = provider;
        this.message = message;
        this.requestContext = requestContext;
        this.kristaMediaClient = kristaMediaClient;
        this.inlineImageConverter = new InlineImageConverter(kristaMediaClient);
        this.multipart = new MimeMultipart("mixed");
    }

    /**
     * Factory method for creating EmailBuilder instances.
     * Creates a new EmailBuilder with default JavaMail session and MIME message.
     *
     * @param provider       the Gmail provider for API access and authentication
     * @param requestContext the current request execution context
     * @return new EmailBuilderImpl instance ready for email composition
     */
    public static EmailBuilderImpl create(GmailProvider provider, RequestContext requestContext, KristaMediaClient kristaMediaClient) {
        Properties props = new Properties();
        Session session = Session.getDefaultInstance(props, null);
        return new EmailBuilderImpl(provider, new MimeMessage(session), requestContext, kristaMediaClient);
    }

    /**
     * Adds TO recipients to the email.
     * Converts EmailAddress objects to JavaMail InternetAddress format and adds them as TO recipients.
     *
     * @param emailAddress list of EmailAddress objects for TO recipients
     * @return this EmailBuilder instance for method chaining
     */
    @Override
    public EmailBuilderImpl withTo(List<EmailAddress> emailAddress) {
        emailAddress.forEach(address -> {
            try {
                message.addRecipient(javax.mail.Message.RecipientType.TO,
                        new InternetAddress(address.getEmailAddress()));
            } catch (MessagingException cause) {
                LOGGER.error("Error occurred in With to request :{}", cause.getMessage());
            }
        });
        return this;
    }

    /**
     * Adds CC recipients to the email.
     * Converts EmailAddress objects to JavaMail InternetAddress format and adds them as CC recipients.
     *
     * @param emailAddress list of EmailAddress objects for CC recipients
     * @return this EmailBuilder instance for method chaining
     */
    @Override
    public EmailBuilderImpl withCc(List<EmailAddress> emailAddress) {
        emailAddress.forEach(address -> {
            try {
                message.addRecipient(javax.mail.Message.RecipientType.CC,
                        new InternetAddress(address.getEmailAddress()));
            } catch (MessagingException cause) {
                LOGGER.error("Error occurred in With Cc request :{}", cause.getMessage());
            }
        });
        return this;
    }

    /**
     * Adds BCC recipients to the email.
     * Converts EmailAddress objects to JavaMail InternetAddress format and adds them as BCC recipients.
     *
     * @param emailAddress list of EmailAddress objects for BCC recipients
     * @return this EmailBuilder instance for method chaining
     */
    @Override
    public EmailBuilderImpl withBcc(List<EmailAddress> emailAddress) {
        emailAddress.forEach(address -> {
            try {
                message.addRecipient(javax.mail.Message.RecipientType.BCC,
                        new InternetAddress(address.getEmailAddress()));
            } catch (MessagingException cause) {
                LOGGER.error("Error occurred in With Bcc request :{}", cause.getMessage());
            }
        });
        return this;
    }

    /**
     * Sets the email content/body.
     * Creates a MIME body part with the specified text content and adds it to the multipart message.
     *
     * @param content the text content for the email body
     * @return this EmailBuilder instance for method chaining
     */
    @Override
    public EmailBuilderImpl withContent(String content) {
        try {
            String processedContent = inlineImageConverter.convertImagesToBase64Html(content);
            processedContent = processedContent.replaceAll(
                    Constants.PLACEHOLDER_LINK_REGEX,
                    Constants.PLACEHOLDER_LINK_REPLACEMENT
            );
            message.setContent(processedContent, "text/html; charset=utf-8");
            return this;
        } catch (MessagingException cause) {
            LOGGER.error("Error occurred in With Content request :{}", cause.getMessage());
            throw new IllegalStateException(cause);
        }
    }

    /**
     * Sets the email subject line.
     * Adds the subject to the MIME message header.
     *
     * @param textContent the subject text for the email
     * @return this EmailBuilder instance for method chaining
     */
    @Override
    public EmailBuilderImpl withSubject(String textContent) {
        try {
            message.setSubject(textContent);
        } catch (MessagingException cause) {
            LOGGER.error("Failed to add subject :{}", cause.getMessage());
        }
        return this;
    }

    /**
     * Adds file attachments to the email.
     * Converts Krista File objects to JavaMail attachments and adds them to the multipart message.
     *
     * @param attachments       list of Krista File objects to attach
     * @param kristaMediaClient client for downloading files from Krista's media server
     * @return this EmailBuilder instance for method chaining
     */
    @Override
    public EmailBuilderImpl withAttachment(List<File> attachments, KristaMediaClient kristaMediaClient) {
        attachments.forEach(attachment -> {
            try {
                if (multipart.getCount() == 0) {
                    Object existingContent = message.getContent();
                    if (existingContent != null) {
                        MimeBodyPart textPart = new MimeBodyPart();
                        textPart.setContent(existingContent, "text/html; charset=utf-8");
                        multipart.addBodyPart(textPart);
                    }
                }
                MimeBodyPart mimeBodyPart = new MimeBodyPart();
                java.io.File file = kristaMediaClient.toJavaFile(attachment);
                DataSource source = new FileDataSource(file);
                mimeBodyPart.setDataHandler(new DataHandler(source));
                mimeBodyPart.setFileName(attachment.getFileName());
                this.multipart.addBodyPart(mimeBodyPart);
            } catch (MessagingException | IOException cause) {
                LOGGER.error("Error occurred while processing attachment: {}", cause.getMessage());
            }
        });
        return this;
    }

    /**
     * Sends the composed email using Gmail API.
     * Finalizes the MIME message, encodes it for Gmail API, and sends it through Gmail.
     * Handles authentication errors and provides appropriate error messages.
     */
    @Override
    public void send() {
        try {
            if (multipart.getCount() > 0) {
                message.setContent(multipart);
            }
            ByteArrayOutputStream buffer = new ByteArrayOutputStream();
            message.writeTo(buffer);
            byte[] rawMessageBytes = buffer.toByteArray();
            buffer.close();
            String encodedEmail = Base64.getUrlEncoder().encodeToString(rawMessageBytes);
            Message gmail = new Message();
            gmail.setRaw(encodedEmail);
            provider.getGmailClient().users().messages().send(Constants.ME, gmail).execute();
        } catch (IOException | MessagingException cause) {
            LOGGER.error("Failed to send mail :{}", cause.getMessage());
            if (cause instanceof GoogleJsonResponseException && ((GoogleJsonResponseException) cause).getStatusCode() == 401) {
                if (!requestContext.invokeAsUser()) {
                    throw new IllegalStateException("You are not authorized. Please ask admin to validate the attributes.");
                } else {
                    throw provider.createMustAuthorizationException(provider.getUserId(false), true);
                }
            }
        }
    }

    /**
     * Sets Reply-To addresses for the email.
     * Configures the email to direct replies to specified addresses instead of the sender.
     *
     * @param replyToAddress list of EmailAddress objects for Reply-To header
     * @return this EmailBuilder instance for method chaining
     */
    @Override
    public EmailBuilderImpl withReplyTo(List<EmailAddress> replyToAddress) {
        try {
            List<InternetAddress> addresses = new ArrayList<>();
            replyToAddress.forEach(address -> {
                try {
                    addresses.add(new InternetAddress(address.getEmailAddress()));
                } catch (AddressException cause) {
                    LOGGER.error("Error occurred while reply to mail :{}", cause.getMessage());
                }
            });
            message.setReplyTo(addresses.toArray(new Address[replyToAddress.size()]));
            return this;
        } catch (MessagingException cause) {
            throw new IllegalStateException(cause);
        }
    }
}
