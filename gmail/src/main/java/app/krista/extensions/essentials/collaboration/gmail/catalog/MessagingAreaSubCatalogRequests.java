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

import app.krista.extension.executor.ExtensionResponse;
import app.krista.extension.impl.anno.Attribute;
import app.krista.extension.impl.anno.CatalogRequest;
import app.krista.extension.impl.anno.Field;
import app.krista.extension.impl.anno.SubCatalogRequest;
import app.krista.extensions.essentials.collaboration.gmail.catalog.entities.MailDetails;
import app.krista.extensions.essentials.collaboration.gmail.catalog.errorhandlers.ErrorHandlingStateManager;
import app.krista.extensions.essentials.collaboration.gmail.catalog.errorhandlers.ExtensionResponseGenerator;
import app.krista.extensions.essentials.collaboration.gmail.catalog.extresp.ExtensionResponseFactory;
import app.krista.extensions.essentials.collaboration.gmail.catalog.extresp.SubCatalogConstants;
import app.krista.extensions.essentials.collaboration.gmail.catalog.validators.ValidationOrchestrator;
import app.krista.extensions.essentials.collaboration.gmail.impl.stores.KristaMediaClient;
import app.krista.extensions.essentials.collaboration.gmail.impl.util.Constants;
import app.krista.extensions.essentials.collaboration.gmail.resources.GmailResources;
import app.krista.extensions.essentials.collaboration.gmail.service.Account;
import app.krista.extensions.essentials.collaboration.gmail.service.Email;
import app.krista.extensions.essentials.collaboration.gmail.service.EmailBuilder;
import app.krista.extensions.essentials.collaboration.gmail.service.Folder;
import app.krista.model.base.File;
import org.jvnet.hk2.annotations.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.mail.MessagingException;
import javax.validation.constraints.NotNull;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class MessagingAreaSubCatalogRequests {
    public static final String REENTER = "Reenter";
    public static final String HANDLE_REENTER_REPLY_TO_ALL_WITH_FIELDS = "handleReenterReplyToAllWithFields";
    public static final String HANDLE_REENTER_REPLY_TO_MAIL_WITH_FIELDS = "handleReenterReplyToMailWithFields";


    private static final Logger LOGGER = LoggerFactory.getLogger(MessagingAreaSubCatalogRequests.class);
    private final ErrorHandlingStateManager internalStateManager;
    private final Account account;
    private final ExtensionResponseGenerator responseGenerator;
    private final KristaMediaClient kristaMediaClient;


    @Inject
    public MessagingAreaSubCatalogRequests(ErrorHandlingStateManager internalStateManager, Account account, ExtensionResponseGenerator responseGenerator, KristaMediaClient kristaMediaClient) {
        this.internalStateManager = internalStateManager;
        this.account = account;
        this.responseGenerator = responseGenerator;
        this.kristaMediaClient = kristaMediaClient;
    }

    @SubCatalogRequest(
            name = SubCatalogConstants.CONFIRM_REENTER_FETCH_MAIL,
            description = "Checks if user wants to re enter message id and if yes, sends prompt to do so",
            type = CatalogRequest.Type.QUERY_SYSTEM
    )
    @SuppressWarnings("unchecked")
    public ExtensionResponse confirmReenterFetchMail(@Field.Desc(name = "inputMap",
            type = "{ Reenter: Boolean, stateId: Text, Message ID: Text}") Map<String, Object> map) {
        Boolean reenter = (Boolean) map.get(REENTER);
        String stateId = (String) map.get(GmailResources.STATE_ID);
        Map<String, Object> state = internalStateManager.get(stateId);

        List<ValidationOrchestrator.ValidationResult> validationResults = getValidationResults(state);
        if (Boolean.FALSE.equals(reenter)) {
            return responseGenerator.generateFetchDenyResponse(ExtensionResponse.Error.ExceptionType.INPUT_ERROR,
                    validationResults, null,
                    Map.of());
        }
        LOGGER.info("Reenter was true hence continuing");
        return responseGenerator.generateFetchResponse(ExtensionResponse.Error.ExceptionType.INPUT_ERROR,
                validationResults, "handleReenterFetchMail",
                Map.of(GmailResources.STATE_ID, stateId, GmailResources.MESSAGE_ID, map.get(GmailResources.MESSAGE_ID)));
    }

    @SubCatalogRequest(
            name = "handleReenterFetchMail",
            description = "Handle reenter message id",
            type = CatalogRequest.Type.QUERY_SYSTEM)
    @Field.Desc(name = "Mail", type = "Entity(Mail Details)", required = false)
    public ExtensionResponse handleReenterFetchMail(
            @Field.Desc(name = "inputMap", type = "{stateId: Text, Message ID: Text}") Map<String, Object> map) {
        LOGGER.info("SubCatalogRequest handleReenterFetchMail start: {}", map);
        String messageID = (String) map.get(GmailResources.MESSAGE_ID);

        try {
            Email email = account.getEmail(messageID);
            return ExtensionResponseFactory.create(Map.of("Mail", CatalogTypes.fromEmail(email, kristaMediaClient)));
        } catch (Exception cause) {
            return ExtensionResponseFactory.create(cause, "We couldn't fetch the email because the message ID appears to " +
                            "be incorrect. Please check the message ID and try again.",
                    ExtensionResponse.Error.ExceptionType.LOGIC_ERROR);
        }
    }

    @NotNull
    private static List<ValidationOrchestrator.ValidationResult> getValidationResults(Map<String, Object> state) {
        List<ValidationOrchestrator.ValidationResult> validationResults = new ArrayList<>();
        List<?> results = (List<?>) state.get(SubCatalogConstants.VALIDATION_RESULTS);
        for (Object item : results) {
            validationResults.add(Constants.GSON.fromJson(Constants.GSON.toJson(item), ValidationOrchestrator.ValidationResult.class));
        }
        return validationResults;
    }

    @SubCatalogRequest(
            name = SubCatalogConstants.CONFIRM_REENTER_MOVE_MESSAGE,
            description = "Checks if user wants to re enter move message and if yes, sends prompt to do so",
            type = CatalogRequest.Type.CHANGE_SYSTEM
    )
    @SuppressWarnings("unchecked")
    public ExtensionResponse confirmReenterMoveMessage(@Field.Desc(name = "inputMap",
            type = "{ Reenter: Boolean, stateId: Text, Message ID: Text, Folder Name: Text}") Map<String, Object> map) {
        Boolean reenter = (Boolean) map.get(REENTER);
        String stateId = (String) map.get(GmailResources.STATE_ID);
        Map<String, Object> state = internalStateManager.get(stateId);
        List<ValidationOrchestrator.ValidationResult> validationResults = getValidationResults(state);
        if (Boolean.FALSE.equals(reenter)) {
            return responseGenerator.generateFetchDenyResponse(ExtensionResponse.Error.ExceptionType.INPUT_ERROR,
                    validationResults, null,
                    Map.of());
        }
        LOGGER.info("Reenter was true hence continuing");
        return responseGenerator.generateFetchResponse(ExtensionResponse.Error.ExceptionType.INPUT_ERROR,
                validationResults, "handleReenterMoveMessage",
                Map.of(GmailResources.STATE_ID, stateId,
                        GmailResources.MESSAGE_ID, map.get(GmailResources.MESSAGE_ID),
                        GmailResources.FOLDER_NAME, map.get(GmailResources.FOLDER_NAME)));
    }

    @SubCatalogRequest(
            name = "handleReenterMoveMessage",
            description = "Handle reenter message id and folder name",
            type = CatalogRequest.Type.CHANGE_SYSTEM)
    @Field(name = "Response", type = "Text", required = false)
    public ExtensionResponse handleReenterMoveMessage(
            @Field.Desc(name = "inputMap", type = "{stateId: Text, Message ID: Text, Folder Name: Text}") Map<String, Object> map) {
        LOGGER.info("SubCatalogRequest handleReenterMoveMessage start: {}", map);
        String messageID = (String) map.get(GmailResources.MESSAGE_ID);
        String folderName = (String) map.get(GmailResources.FOLDER_NAME);

        try {
            Email email = account.getEmail(messageID);
            if (email == null) {
                return ExtensionResponseFactory.create(Map.of("Response", GmailResources.INVALID_ID));
            }
            Folder folder = account.getFolderByName(folderName);
            if (folder == null) {
                return ExtensionResponseFactory.create(Map.of("Response", "failed."));
            }
            email.moveToFolder(folder);
            return ExtensionResponseFactory.create(Map.of("Response", GmailResources.SUCCESS));
        } catch (Exception cause) {
            return ExtensionResponseFactory.create(cause, "We couldn't move the message because either the message ID " +
                            "is incorrect or the folder doesn't exist. Please check and try again.",
                    ExtensionResponse.Error.ExceptionType.LOGIC_ERROR);
        }
    }

    @SubCatalogRequest(
            name = SubCatalogConstants.CONFIRM_REENTER_REPLY_TO_ALL,
            description = "Checks if user wants to re enter reply to all and if yes, sends prompt to do so",
            type = CatalogRequest.Type.QUERY_SYSTEM
    )
    @SuppressWarnings("unchecked")
    public ExtensionResponse confirmReenterReplyToAll(@Field.Desc(name = "inputMap",
            type = "{ Reenter: Boolean, stateId: Text, Message ID: Text, Message: Paragraph, Attachments: File }") Map<String, Object> map) {
        Boolean reenter = (Boolean) map.get(REENTER);
        String stateId = (String) map.get(GmailResources.STATE_ID);
        Map<String, Object> state = internalStateManager.get(stateId);
        List<ValidationOrchestrator.ValidationResult> validationResults = getValidationResults(state);
        if (Boolean.FALSE.equals(reenter)) {
            return responseGenerator.generateFetchDenyResponse(ExtensionResponse.Error.ExceptionType.INPUT_ERROR,
                    validationResults, null,
                    Map.of());
        }
        LOGGER.info("Reenter was true hence continuing");
        return responseGenerator.generateFetchResponse(ExtensionResponse.Error.ExceptionType.INPUT_ERROR,
                validationResults, "handleReenterReplyToAll",
                Map.of(GmailResources.STATE_ID, stateId,
                        GmailResources.MESSAGE_ID, map.get(GmailResources.MESSAGE_ID),
                        GmailResources.MESSAGE, map.get(GmailResources.MESSAGE),
                        GmailResources.ATTACHMENTS, map.get(GmailResources.ATTACHMENTS)));
    }

    @SubCatalogRequest(
            name = "handleReenterReplyToAll",
            description = "Handle Reply To ALL",
            type = CatalogRequest.Type.CHANGE_SYSTEM)
    @Field(name = "Is Successful", type = "Switch", required = false)
    @SuppressWarnings("unchecked")
    public ExtensionResponse handleReenterReplyToAll(
            @Field.Desc(name = "inputMap", type = "{ stateId: Text, Message ID: Text, Message: Paragraph, Attachments: File }") Map<String, Object> map) {
        LOGGER.info("SubCatalogRequest handleReenterReplyToAll start: {}", map);
        try {
            List<File> attachments = (List<File>) map.get(GmailResources.ATTACHMENTS);
            String messageId = (String) map.get(GmailResources.MESSAGE_ID);
            String message = (String) map.get(GmailResources.MESSAGE);

            Email email = account.getEmail(messageId);
            if (email == null) {
                return ExtensionResponseFactory.create(Map.of("Is Successful", false));
            }
            try {
                message = message != null ? message.replace("\n", "<br>") : message;
                email.replyToAll(message, CatalogTypes.toAttachments(attachments, kristaMediaClient), null, null, null);
                return ExtensionResponseFactory.create(Map.of("Is Successful", true));
            } catch (MessagingException | IOException cause) {
                LOGGER.error("Reply to all failed with ID: {} with error message: {}", messageId, cause.getMessage());
                return ExtensionResponseFactory.create(Map.of("Is Successful", false));
            }
        } catch (Exception cause) {
            return ExtensionResponseFactory.create(cause, "Failed to reply all Message",
                    ExtensionResponse.Error.ExceptionType.LOGIC_ERROR);
        }
    }

    @SubCatalogRequest(
            name = SubCatalogConstants.CONFIRM_REENTER_FETCH_SENT,
            description = "Checks if user wants to re enter fetch Sent mail Page Size or Number and if yes, sends prompt to do so",
            type = CatalogRequest.Type.QUERY_SYSTEM
    )
    @SuppressWarnings("unchecked")
    public ExtensionResponse confirmReenterFetchSent(@Field.Desc(name = "inputMap",
            type = "{ Reenter: Boolean, stateId: Text, Page Number: Number, Page Size: Number }") Map<String, Object> map) {
        Boolean reenter = (Boolean) map.get(REENTER);
        String stateId = (String) map.get(GmailResources.STATE_ID);
        Map<String, Object> state = internalStateManager.get(stateId);
        List<ValidationOrchestrator.ValidationResult> validationResults = getValidationResults(state);
        if (Boolean.FALSE.equals(reenter)) {
            return responseGenerator.generateFetchDenyResponse(ExtensionResponse.Error.ExceptionType.INPUT_ERROR,
                    validationResults, null,
                    Map.of());
        }
        LOGGER.info("Reenter was true hence continuing");
        Map<String, Object> fetchResponseMap = new java.util.HashMap<>(3);
        fetchResponseMap.put(GmailResources.STATE_ID, stateId);
        fetchResponseMap.put(GmailResources.PAGE_NUMBER, map.get(GmailResources.PAGE_NUMBER));
        fetchResponseMap.put(GmailResources.PAGE_SIZE, map.get(GmailResources.PAGE_SIZE));
        return responseGenerator.generateFetchResponse(ExtensionResponse.Error.ExceptionType.INPUT_ERROR,
                validationResults, "handleReenterFetchSent",
                fetchResponseMap);
    }

    @SubCatalogRequest(
            name = "handleReenterFetchSent",
            description = "Handle reenter fetch sent mail",
            type = CatalogRequest.Type.QUERY_SYSTEM)
    @Field.Desc(name = "Sent Mails", type = "[ Entity(Mail Details) ]", required = false)
    public ExtensionResponse handleReenterFetchSent(
            @Field.Desc(name = "inputMap", type = "{ stateId: Text, Page Number: Number, Page Size: Number }") Map<String, Object> map) {
        LOGGER.info("SubCatalogRequest handleReenterFetchSent start: {}", map);
        Double pageNumber = (Double) map.get(GmailResources.PAGE_NUMBER);
        Double pageSize = (Double) map.get(GmailResources.PAGE_SIZE);

        try {
            List<Email> emails = account.getSentFolder().getEmails(pageNumber, pageSize);
            List<MailDetails> mailDetailsList = new ArrayList<>();
            for (Email email : emails) {
                mailDetailsList.add(CatalogTypes.fromEmail(email, kristaMediaClient));
            }
            return ExtensionResponseFactory.create(Map.of("Sent Mails", mailDetailsList));
        } catch (Exception cause) {
            return ExtensionResponseFactory.create(cause, "Failed to fetch Sent Mails",
                    ExtensionResponse.Error.ExceptionType.LOGIC_ERROR);
        }
    }

    @SubCatalogRequest(
            name = SubCatalogConstants.CONFIRM_REENTER_FORWARD_MAIL,
            description = "Checks if user wants to re enter forward mail and if yes, sends prompt to do so",
            type = CatalogRequest.Type.QUERY_SYSTEM
    )
    @SuppressWarnings("unchecked")
    public ExtensionResponse confirmReenterForwardMail(@Field.Desc(name = "inputMap",
            type = "{ Reenter: Boolean, stateId: Text, Message ID: Text, To: Text, Message: Paragraph }") Map<String, Object> map) {
        Boolean reenter = (Boolean) map.get(REENTER);
        String stateId = (String) map.get(GmailResources.STATE_ID);
        Map<String, Object> state = internalStateManager.get(stateId);
        List<ValidationOrchestrator.ValidationResult> validationResults = getValidationResults(state);
        if (Boolean.FALSE.equals(reenter)) {
            return responseGenerator.generateFetchDenyResponse(ExtensionResponse.Error.ExceptionType.INPUT_ERROR,
                    validationResults, null,
                    Map.of());
        }
        LOGGER.info("Reenter was true hence continuing");
        return responseGenerator.generateFetchResponse(ExtensionResponse.Error.ExceptionType.INPUT_ERROR,
                validationResults, "handleReenterForwardMail",
                Map.of(GmailResources.STATE_ID, stateId,
                        GmailResources.MESSAGE_ID, map.get(GmailResources.MESSAGE_ID),
                        GmailResources.TO, map.get(GmailResources.TO),
                        GmailResources.MESSAGE, map.get(GmailResources.MESSAGE)));
    }

    @SubCatalogRequest(
            name = "handleReenterForwardMail",
            description = "Handle Forward Mail",
            type = CatalogRequest.Type.CHANGE_SYSTEM)
    @Field(name = "Is Forwarded", type = "Switch", required = false)
    public ExtensionResponse handleReenterForwardMail(
            @Field.Desc(name = "inputMap", type = "{ stateId: Text, Message ID: Text, To: Text, Message: Paragraph }") Map<String, Object> map) {
        LOGGER.info("SubCatalogRequest handleReenterForwardMail start: {}", map);
        try {
            String messageId = (String) map.get(GmailResources.MESSAGE_ID);
            String to = (String) map.get(GmailResources.TO);
            String message = (String) map.get(GmailResources.MESSAGE);

            Email email = account.getEmail(messageId);
            if (email == null) {
                return ExtensionResponseFactory.create(Map.of("Is Forwarded", false));
            }
            EmailBuilder builder = account.newEmail();
            builder.withSubject(email.getSubject());
            builder.withTo(CatalogTypes.toEmailAddresses(to));
            if (message != null) {
                message = message.replace("\n", "<br>");
                builder.withContent(message);
            }
            builder.send();
            return ExtensionResponseFactory.create(Map.of("Is Forwarded", true));
        } catch (Exception cause) {
            return ExtensionResponseFactory.create(cause, "We couldn't forward the email because the message ID or " +
                            "recipient email address seems to be incorrect. Please double-check and try again.",
                    ExtensionResponse.Error.ExceptionType.LOGIC_ERROR);
        }
    }

    @SubCatalogRequest(
            name = SubCatalogConstants.CONFIRM_REENTER_SEND_MAIL,
            description = "Checks if user wants to re enter send mail parameters and if yes, sends prompt to do so",
            type = CatalogRequest.Type.CHANGE_SYSTEM
    )
    public ExtensionResponse confirmReenterSendMail(
            @Field.Desc(name = "inputMap", type = "{ Reenter: Boolean, stateId: Text, To: Text, Cc: Text, Bcc: Text, Subject: Text, Message: RichText, Attachments: File, Reply To: Text }") Map<String, Object> map) {
        LOGGER.info("SubCatalogRequest confirmReenterSendMail start: {}", map);
        Boolean reenter = (Boolean) map.get(REENTER);
        String stateId = (String) map.get(GmailResources.STATE_ID);
        Map<String, Object> state = internalStateManager.get(stateId);
        List<ValidationOrchestrator.ValidationResult> validationResults = getValidationResults(state);
        if (Boolean.FALSE.equals(reenter)) {
            return responseGenerator.generateFetchDenyResponse(ExtensionResponse.Error.ExceptionType.INPUT_ERROR,
                    validationResults, null,
                    Map.of());
        }

        return responseGenerator.generateFetchResponse(
                ExtensionResponse.Error.ExceptionType.INPUT_ERROR, validationResults,
                "handleReenterSendMail",
                Map.of(GmailResources.STATE_ID, stateId,
                        GmailResources.TO, map.get(GmailResources.TO),
                        GmailResources.CC, map.get(GmailResources.CC),
                        GmailResources.BCC, map.get(GmailResources.BCC),
                        GmailResources.SUBJECT, map.get(GmailResources.SUBJECT),
                        GmailResources.MESSAGE, map.get(GmailResources.MESSAGE),
                        GmailResources.ATTACHMENTS, map.get(GmailResources.ATTACHMENTS),
                        GmailResources.REPLY_TO, map.get(GmailResources.REPLY_TO)
                ));
    }

    @SubCatalogRequest(
            name = "handleReenterSendMail",
            description = "Handle Send Mail",
            type = CatalogRequest.Type.CHANGE_SYSTEM)
    @Field(name = "Message", type = "Text", required = false)
    @SuppressWarnings("unchecked")
    public ExtensionResponse handleReenterSendMail(
            @Field.Desc(name = "inputMap", type = "{ stateId: Text, Subject: Text, Message: RichText, To: Text, Bcc: Text, Cc: Text, Reply To: Text, Attachments: File }") Map<String, Object> map) {
        LOGGER.info("SubCatalogRequest handleReenterSendMail start: {}", map);
        try {
            List<File> attachments = (List<File>) map.get(GmailResources.ATTACHMENTS);
            String subject = (String) map.get(GmailResources.SUBJECT);
            String message = (String) map.get(GmailResources.MESSAGE);
            String cc = (String) map.get(GmailResources.CC);
            String to = (String) map.get(GmailResources.TO);
            String bcc = (String) map.get(GmailResources.BCC);
            String replyTo = (String) map.get(GmailResources.REPLY_TO);

            EmailBuilder builder = account.newEmail();
            builder.withSubject(subject);
            builder.withTo(CatalogTypes.toEmailAddresses(to));
            builder.withCc(CatalogTypes.toEmailAddresses(cc));
            builder.withBcc(CatalogTypes.toEmailAddresses(bcc));
            builder.withReplyTo(CatalogTypes.toEmailAddresses(replyTo));
            if (message != null) {
                message = message.replace("\n", "<br>");
                builder.withContent(message);
            }
            if (attachments != null && !attachments.isEmpty()) {
                builder.withAttachment(attachments, kristaMediaClient);
            }
            builder.send();
            return ExtensionResponseFactory.create(Map.of("Message", GmailResources.SUCCESS));
        } catch (Exception cause) {
            return ExtensionResponseFactory.create(cause, "Failed to Send Mail",
                    ExtensionResponse.Error.ExceptionType.LOGIC_ERROR);
        }
    }

    @SubCatalogRequest(
            name = SubCatalogConstants.CONFIRM_REENTER_FETCH_INBOX,
            description = "Checks if user wants to re enter fetch Inbox Page Size or Number and if yes, sends prompt to do so",
            type = CatalogRequest.Type.QUERY_SYSTEM
    )
    @SuppressWarnings("unchecked")
    public ExtensionResponse confirmReenterFetchInbox(@Field.Desc(name = "inputMap",
            type = "{ Reenter: Boolean, stateId: Text, Page Number: Number, Page Size: Number }") Map<String, Object> map) {
        Boolean reenter = (Boolean) map.get(REENTER);
        String stateId = (String) map.get(GmailResources.STATE_ID);
        Map<String, Object> state = internalStateManager.get(stateId);
        List<ValidationOrchestrator.ValidationResult> validationResults = getValidationResults(state);
        if (Boolean.FALSE.equals(reenter)) {
            return responseGenerator.generateFetchDenyResponse(ExtensionResponse.Error.ExceptionType.INPUT_ERROR,
                    validationResults, null,
                    Map.of());
        }
        LOGGER.info("Reenter was true hence continuing");
        Map<String, Object> fetchResponseMap = new java.util.HashMap<>(3);
        fetchResponseMap.put(GmailResources.STATE_ID, stateId);
        fetchResponseMap.put(GmailResources.PAGE_NUMBER, map.get(GmailResources.PAGE_NUMBER));
        fetchResponseMap.put(GmailResources.PAGE_SIZE, map.get(GmailResources.PAGE_SIZE));
        return responseGenerator.generateFetchResponse(ExtensionResponse.Error.ExceptionType.INPUT_ERROR,
                validationResults, "handleReenterFetchInbox",
                fetchResponseMap);
    }

    @SubCatalogRequest(
            name = "handleReenterFetchInbox",
            description = "Handle reenter fetch Inbox",
            type = CatalogRequest.Type.QUERY_SYSTEM)
    @Field.Desc(name = "Inbox Mails", type = "[ Entity(Mail Details) ]", required = false)
    public ExtensionResponse handleReenterFetchInbox(
            @Field.Desc(name = "inputMap", type = "{ stateId: Text, Page Number: Number, Page Size: Number }") Map<String, Object> map) {
        LOGGER.info("SubCatalogRequest handleReenterFetchInbox start: {}", map);
        Double pageNumber = (Double) map.get(GmailResources.PAGE_NUMBER);
        Double pageSize = (Double) map.get(GmailResources.PAGE_SIZE);

        try {
            List<Email> emails = account.getInboxFolder().getEmails(pageNumber, pageSize);
            List<MailDetails> mailDetailsList = new ArrayList<>();
            for (Email email : emails) {
                mailDetailsList.add(CatalogTypes.fromEmail(email, kristaMediaClient));
            }
            return ExtensionResponseFactory.create(Map.of("Inbox Mails", mailDetailsList));
        } catch (Exception cause) {
            return ExtensionResponseFactory.create(cause, "We couldn't fetch your inbox because the page number " +
                            "or page size is invalid. Please enter a number between 1 and 15.",
                    ExtensionResponse.Error.ExceptionType.LOGIC_ERROR);
        }
    }

    @SubCatalogRequest(
            name = SubCatalogConstants.CONFIRM_REENTER_MARK_MESSAGE,
            description = "Checks if user wants to re mark message and if yes, sends prompt to do so",
            type = CatalogRequest.Type.QUERY_SYSTEM
    )
    @SuppressWarnings("unchecked")
    public ExtensionResponse confirmReenterMarkMessage(@Field.Desc(name = "inputMap",
            type = "{ Reenter: Boolean, stateId: Text, Message ID: Text, Label: PickOne(Read|Unread) }") Map<String, Object> map) {
        Boolean reenter = (Boolean) map.get(REENTER);
        String stateId = (String) map.get(GmailResources.STATE_ID);
        Map<String, Object> state = internalStateManager.get(stateId);
        List<ValidationOrchestrator.ValidationResult> validationResults = getValidationResults(state);
        if (Boolean.FALSE.equals(reenter)) {
            return responseGenerator.generateFetchDenyResponse(ExtensionResponse.Error.ExceptionType.INPUT_ERROR,
                    validationResults, null,
                    Map.of());
        }
        LOGGER.info("Reenter was true hence continuing");
        return responseGenerator.generateFetchResponse(ExtensionResponse.Error.ExceptionType.INPUT_ERROR,
                validationResults, "handleReenterMarkMessage",
                Map.of(GmailResources.STATE_ID, stateId,
                        GmailResources.MESSAGE_ID, map.get(GmailResources.MESSAGE_ID),
                        GmailResources.LABEL, map.get(GmailResources.LABEL)));
    }

    @SubCatalogRequest(
            name = "handleReenterMarkMessage",
            description = "Handle reenter mark message",
            type = CatalogRequest.Type.CHANGE_SYSTEM)
    @Field(name = "Response", type = "Text", required = false)
    public ExtensionResponse handleReenterMarkMessage(
            @Field.Desc(name = "inputMap", type = "{ stateId: Text, Message ID: Text, Label: PickOne(Read|Unread) }") Map<String, Object> map) {
        LOGGER.info("SubCatalogRequest handleReenterMarkMessage start: {}", map);
        String messageID = (String) map.get(GmailResources.MESSAGE_ID);
        String label = (String) map.get(GmailResources.LABEL);

        try {
            Email email = account.getEmail(messageID);
            if (email == null) {
                return ExtensionResponseFactory.create(Map.of("Response", GmailResources.INVALID_ID));
            }
            if (label.equalsIgnoreCase("read")) {
                email.markAsRead();
            } else if (label.equalsIgnoreCase("unread")) {
                email.markAsUnread();
            } else {
                return ExtensionResponseFactory.create(Map.of("Response", "Invalid label"));
            }
            return ExtensionResponseFactory.create(Map.of("Response", GmailResources.SUCCESS));
        } catch (Exception cause) {
            return ExtensionResponseFactory.create(cause, "We couldn't process the message because it seems the message ID is incorrect or missing",
                    ExtensionResponse.Error.ExceptionType.LOGIC_ERROR);
        }
    }

    @SubCatalogRequest(
            name = SubCatalogConstants.CONFIRM_REENTER_REPLY_TO_MAIL,
            description = "Checks if user wants to re enter reply to mail and if yes, sends prompt to do so",
            type = CatalogRequest.Type.QUERY_SYSTEM
    )
    @SuppressWarnings("unchecked")
    public ExtensionResponse confirmReenterReplyToMail(@Field.Desc(name = "inputMap",
            type = "{ Reenter: Boolean, stateId: Text, Message ID: Text, Message: RichText, Attachments: File }") Map<String, Object> map) {
        Boolean reenter = (Boolean) map.get(REENTER);
        String stateId = (String) map.get(GmailResources.STATE_ID);
        Map<String, Object> state = internalStateManager.get(stateId);
        List<ValidationOrchestrator.ValidationResult> validationResults = getValidationResults(state);
        if (Boolean.FALSE.equals(reenter)) {
            return responseGenerator.generateFetchDenyResponse(ExtensionResponse.Error.ExceptionType.INPUT_ERROR,
                    validationResults, null,
                    Map.of());
        }
        LOGGER.info("Reenter was true hence continuing");
        return responseGenerator.generateFetchResponse(ExtensionResponse.Error.ExceptionType.INPUT_ERROR,
                validationResults, "handleReenterReplyToMail",
                Map.of(GmailResources.STATE_ID, stateId,
                        GmailResources.MESSAGE_ID, map.get(GmailResources.MESSAGE_ID),
                        GmailResources.MESSAGE, map.get(GmailResources.MESSAGE),
                        GmailResources.ATTACHMENTS, map.get(GmailResources.ATTACHMENTS)));
    }

    @SubCatalogRequest(
            name = "handleReenterReplyToMail",
            description = "Handle Reply To Mail",
            type = CatalogRequest.Type.CHANGE_SYSTEM)
    @Field(name = "Message", type = "Text", required = false)
    @SuppressWarnings("unchecked")
    public ExtensionResponse handleReenterReplyToMail(
            @Field.Desc(name = "inputMap", type = "{ stateId: Text, Message ID: Text, Message: RichText, Attachments: File }") Map<String, Object> map) {
        LOGGER.info("SubCatalogRequest handleReenterReplyToMail start: {}", map);
        try {
            List<File> attachments = (List<File>) map.get(GmailResources.ATTACHMENTS);
            String messageID = (String) map.get(GmailResources.MESSAGE_ID);
            String message = (String) map.get(GmailResources.MESSAGE);

            Email email = account.getEmail(messageID);
            if (email == null) {
                return ExtensionResponseFactory.create(Map.of("Message", GmailResources.INVALID_ID));
            }
            try {
                message = message != null ? message.replace("\n", "<br>") : message;
                email.replyText(message, CatalogTypes.toAttachments(attachments, kristaMediaClient), null, null, null);
                return ExtensionResponseFactory.create(Map.of("Message", GmailResources.SUCCESS));
            } catch (IOException cause) {
                String errorMessage = "Reply to mail failed for ID: " + messageID + " with error message: " + cause.getMessage();
                LOGGER.error(errorMessage);
                return ExtensionResponseFactory.create(Map.of("Message", errorMessage));
            }
        } catch (Exception cause) {
            return ExtensionResponseFactory.create(cause, "Failed to Reply Mail",
                    ExtensionResponse.Error.ExceptionType.LOGIC_ERROR);
        }
    }

    @SubCatalogRequest(
            name = SubCatalogConstants.CONFIRM_REENTER_FETCH_MAIL_BY_LABEL,
            description = "Checks if user wants to re enter fetch mail by Label and if yes, sends prompt to do so",
            type = CatalogRequest.Type.QUERY_SYSTEM
    )
    @SuppressWarnings("unchecked")
    public ExtensionResponse confirmReenterFetchMailByLabel(@Field.Desc(name = "inputMap",
            type = "{ Reenter: Boolean, stateId: Text, Label: Text, Page Number: Number, Page Size: Number }") Map<String, Object> map) {
        Boolean reenter = (Boolean) map.get(REENTER);
        String stateId = (String) map.get(GmailResources.STATE_ID);
        Map<String, Object> state = internalStateManager.get(stateId);
        List<ValidationOrchestrator.ValidationResult> validationResults = getValidationResults(state);
        if (Boolean.FALSE.equals(reenter)) {
            return responseGenerator.generateFetchDenyResponse(ExtensionResponse.Error.ExceptionType.INPUT_ERROR,
                    validationResults, null,
                    Map.of());
        }
        LOGGER.info("Reenter was true hence continuing");
        Map<String, Object> fetchResponseMap = new java.util.HashMap<>(4);
        fetchResponseMap.put(GmailResources.STATE_ID, stateId);
        fetchResponseMap.put(GmailResources.LABEL, map.get(GmailResources.LABEL));
        fetchResponseMap.put(GmailResources.PAGE_NUMBER, map.get(GmailResources.PAGE_NUMBER));
        fetchResponseMap.put(GmailResources.PAGE_SIZE, map.get(GmailResources.PAGE_SIZE));
        return responseGenerator.generateFetchResponse(ExtensionResponse.Error.ExceptionType.INPUT_ERROR,
                validationResults, "handleReenterFetchMailByLabel",
                fetchResponseMap);
    }

    @SubCatalogRequest(
            name = "handleReenterFetchMailByLabel",
            description = "Handle reenter fetch mail by label",
            type = CatalogRequest.Type.QUERY_SYSTEM)
    @Field.Desc(name = "Mails", type = "[ Entity(Mail Details) ]", required = false)
    public ExtensionResponse handleReenterFetchMailByLabel(
            @Field.Desc(name = "inputMap", type = "{ stateId: Text, Label: Text, Page Number: Number, Page Size: Number }") Map<String, Object> map) {
        LOGGER.info("SubCatalogRequest handleReenterFetchMailByLabel start: {}", map);
        String label = (String) map.get(GmailResources.LABEL);
        Double pageNumber = (Double) map.get(GmailResources.PAGE_NUMBER);
        Double pageSize = (Double) map.get(GmailResources.PAGE_SIZE);

        try {
            Folder folder = account.getFolderByName(label);
            if (folder == null) {
                LOGGER.info("Fetch mails request failed. Given folder does not exist.");
                return ExtensionResponseFactory.create(Map.of("Mails", List.of()));
            }
            List<Email> emails = folder.getEmails(pageNumber, pageSize);
            List<MailDetails> result = new ArrayList<>();
            for (Email mail : emails) {
                result.add(CatalogTypes.fromEmail(mail, kristaMediaClient));
            }
            return ExtensionResponseFactory.create(Map.of("Mails", result));
        } catch (Exception cause) {
            return ExtensionResponseFactory.create(cause, "Failed to fetch mail by label",
                    ExtensionResponse.Error.ExceptionType.LOGIC_ERROR);
        }
    }

    @SubCatalogRequest(
            name = SubCatalogConstants.CONFIRM_REENTER_REPLY_TO_ALL_WITH_FIELDS,
            description = "Checks if user wants to re enter reply to all and if yes, sends prompt to do so",
            type = CatalogRequest.Type.QUERY_SYSTEM
    )
    @SuppressWarnings("unchecked")
    public ExtensionResponse confirmReenterReplyToAllWithFields(@Field.Desc(name = "inputMap",
            type = "{ Reenter: Boolean, stateId: Text, Message ID: Text, To: Text, Cc: Text, Bcc: Text, Reply To: Text, Message: RichText, Attachments: File }") Map<String, Object> map) {
        Boolean reenter = (Boolean) map.get(REENTER);
        String stateId = (String) map.get(GmailResources.STATE_ID);
        Map<String, Object> state = internalStateManager.get(stateId);
        List<ValidationOrchestrator.ValidationResult> validationResults = getValidationResults(state);
        if (Boolean.FALSE.equals(reenter)) {
            return responseGenerator.generateFetchDenyResponse(ExtensionResponse.Error.ExceptionType.INPUT_ERROR,
                    validationResults, null,
                    Map.of());
        }
        LOGGER.info("Reenter was true hence continuing");
        return responseGenerator.generateFetchResponse(ExtensionResponse.Error.ExceptionType.INPUT_ERROR,
                validationResults, HANDLE_REENTER_REPLY_TO_ALL_WITH_FIELDS,
                Map.of(GmailResources.STATE_ID, stateId,
                        GmailResources.MESSAGE_ID, map.get(GmailResources.MESSAGE_ID),
                        GmailResources.TO, map.get(GmailResources.TO),
                        GmailResources.CC, map.get(GmailResources.CC),
                        GmailResources.BCC, map.get(GmailResources.BCC),
                        GmailResources.MESSAGE, map.get(GmailResources.MESSAGE),
                        GmailResources.ATTACHMENTS, map.get(GmailResources.ATTACHMENTS),
                        GmailResources.REPLY_TO, map.get(GmailResources.REPLY_TO)));
    }

    @SubCatalogRequest(
            name = HANDLE_REENTER_REPLY_TO_ALL_WITH_FIELDS,
            description = "Handle Reply To ALL With CC BCC",
            type = CatalogRequest.Type.CHANGE_SYSTEM)
    @Field.Boolean(name = "Is Successful", required = false, attributes = {@Attribute(name = "visualWidth", value = "S")})
    @SuppressWarnings("unchecked")
    public ExtensionResponse handleReenterReplyToAllWithFields(
            @Field.Desc(name = "inputMap", type = "{ stateId: Text, Message ID: Text, To: Text, Cc: Text, Bcc: Text, Reply To: Text, Message: RichText, Attachments: File }") Map<String, Object> map) {
        LOGGER.info("SubCatalogRequest handleReenterReplyToAllWithFields start: {}", map);
        try {
            List<File> attachments = (List<File>) map.get(GmailResources.ATTACHMENTS);
            String messageId = (String) map.get(GmailResources.MESSAGE_ID);
            String message = (String) map.get(GmailResources.MESSAGE);
            String cc = (String) map.get(GmailResources.CC);
            String to = (String) map.get(GmailResources.TO);
            String bcc = (String) map.get(GmailResources.BCC);
            String replyTo = (String) map.get(GmailResources.REPLY_TO);

            Email email = account.getEmail(messageId);
            if (email == null) {
                return ExtensionResponseFactory.create(Map.of("Is Successful", false));
            }
            try {
                message = message != null ? message.replace("\n", "<br>") : message;
                email.replyToAll(message, CatalogTypes.toAttachments(attachments, kristaMediaClient), cc, bcc, to);
                return ExtensionResponseFactory.create(Map.of("Is Successful", true));
            } catch (MessagingException | IOException cause) {
                String errorMessage = "Error occurred while processing attachment for message ID: " + messageId;
                LOGGER.error(errorMessage, cause);
                return ExtensionResponseFactory.create(Map.of("Is Successful", false));
            }
        } catch (Exception cause) {
            return ExtensionResponseFactory.create(cause, "Failed to Reply all with Cc and Bcc",
                    ExtensionResponse.Error.ExceptionType.LOGIC_ERROR);
        }
    }

    @SubCatalogRequest(
            name = SubCatalogConstants.CONFIRM_REENTER_REPLY_TO_MAIL_WITH_FIELDS,
            description = "Checks if user wants to re enter reply to mail and if yes, sends prompt to do so",
            type = CatalogRequest.Type.QUERY_SYSTEM
    )
    @SuppressWarnings("unchecked")
    public ExtensionResponse confirmReenterReplyToMailWithFields(@Field.Desc(name = "inputMap",
            type = "{ Reenter: Boolean, stateId: Text, Message ID: Text, To: Text, Cc: Text, Bcc: Text, Reply To: Text, Message: RichText, Attachments: File }") Map<String, Object> map) {
        Boolean reenter = (Boolean) map.get(REENTER);
        String stateId = (String) map.get(GmailResources.STATE_ID);
        Map<String, Object> state = internalStateManager.get(stateId);
        List<ValidationOrchestrator.ValidationResult> validationResults = getValidationResults(state);
        if (Boolean.FALSE.equals(reenter)) {
            return responseGenerator.generateFetchDenyResponse(ExtensionResponse.Error.ExceptionType.INPUT_ERROR,
                    validationResults, null,
                    Map.of());
        }
        LOGGER.info("Reenter was true hence continuing");
        return responseGenerator.generateFetchResponse(ExtensionResponse.Error.ExceptionType.INPUT_ERROR,
                validationResults, HANDLE_REENTER_REPLY_TO_MAIL_WITH_FIELDS,
                Map.of(GmailResources.STATE_ID, stateId,
                        GmailResources.MESSAGE_ID, map.get(GmailResources.MESSAGE_ID),
                        GmailResources.TO, map.get(GmailResources.TO),
                        GmailResources.CC, map.get(GmailResources.CC),
                        GmailResources.BCC, map.get(GmailResources.BCC),
                        GmailResources.MESSAGE, map.get(GmailResources.MESSAGE),
                        GmailResources.ATTACHMENTS, map.get(GmailResources.ATTACHMENTS),
                        GmailResources.REPLY_TO, map.get(GmailResources.REPLY_TO)));
    }

    @SubCatalogRequest(
            name = HANDLE_REENTER_REPLY_TO_MAIL_WITH_FIELDS,
            description = "Handle Reply To Mail with cc, bcc, to, Reply to",
            type = CatalogRequest.Type.CHANGE_SYSTEM)
    @Field(name = "Message", type = "Text", required = false)
    @SuppressWarnings("unchecked")
    public ExtensionResponse handleReenterReplyToMailWithFields(
            @Field.Desc(name = "inputMap", type = "{ stateId: Text, Message ID: Text, To: Text, Cc: Text, Bcc: Text, Reply To: Text, Message: RichText, Attachments: File }") Map<String, Object> map) {
        LOGGER.info("SubCatalogRequest handleReenterReplyToMailWithFields start: {}", map);
        try {
            List<File> attachments = (List<File>) map.get(GmailResources.ATTACHMENTS);
            String messageId = (String) map.get(GmailResources.MESSAGE_ID);
            String message = (String) map.get(GmailResources.MESSAGE);
            String cc = (String) map.get(GmailResources.CC);
            String to = (String) map.get(GmailResources.TO);
            String bcc = (String) map.get(GmailResources.BCC);
            String replyTo = (String) map.get(GmailResources.REPLY_TO);

            Email email = account.getEmail(messageId);
            if (email == null) {
                return ExtensionResponseFactory.create(Map.of("Message", GmailResources.INVALID_ID));
            }
            try {
                message = message != null ? message.replace("\n", "<br>") : message;
                email.replyText(message, CatalogTypes.toAttachments(attachments, kristaMediaClient), cc, bcc, to);
                return ExtensionResponseFactory.create(Map.of("Message", GmailResources.SUCCESS));
            } catch (IOException cause) {
                String errorMessage = "Reply to mail failed for ID: " + messageId + " with error message: " + cause.getMessage();
                LOGGER.error(errorMessage);
                return ExtensionResponseFactory.create(Map.of("Message", errorMessage));
            }
        } catch (Exception cause) {
            return ExtensionResponseFactory.create(cause, "Failed to Reply Mail With CC and BCC",
                    ExtensionResponse.Error.ExceptionType.LOGIC_ERROR);
        }
    }

    @SubCatalogRequest(
            name = SubCatalogConstants.CONFIRM_REENTER_FETCH_MAIL_BY_QUERY,
            description = "Checks if user wants to re enter search query and if yes, sends prompt to do so",
            type = CatalogRequest.Type.QUERY_SYSTEM
    )
    @SuppressWarnings("unchecked")
    public ExtensionResponse confirmReenterFetchMailByQuery(@Field.Desc(name = "inputMap",
            type = "{ Reenter: Boolean, stateId: Text, Query: Text }") Map<String, Object> map) {
        Boolean reenter = (Boolean) map.get(REENTER);
        String stateId = (String) map.get(GmailResources.STATE_ID);
        Map<String, Object> state = internalStateManager.get(stateId);
        List<ValidationOrchestrator.ValidationResult> validationResults = getValidationResults(state);
        if (Boolean.FALSE.equals(reenter)) {
            return responseGenerator.generateFetchDenyResponse(ExtensionResponse.Error.ExceptionType.INPUT_ERROR,
                    validationResults, null,
                    Map.of());
        }
        LOGGER.info("Reenter was true hence continuing");
        return responseGenerator.generateFetchResponse(ExtensionResponse.Error.ExceptionType.INPUT_ERROR,
                validationResults, "handleReenterFetchMailByQuery",
                Map.of(GmailResources.STATE_ID, stateId,
                        GmailResources.QUERY, map.get(GmailResources.QUERY)));
    }

    @SubCatalogRequest(
            name = "handleReenterFetchMailByQuery",
            description = "Handle reenter search query",
            type = CatalogRequest.Type.QUERY_SYSTEM)
    @Field.Desc(name = "Mails", type = "[ Entity(Mail Details) ]", required = false)
    public ExtensionResponse handleReenterFetchMailByQuery(
            @Field.Desc(name = "inputMap", type = "{ stateId: Text, Query: Text }") Map<String, Object> map) {
        LOGGER.info("SubCatalogRequest handleReenterFetchMailByQuery start: {}", map);
        String query = (String) map.get(GmailResources.QUERY);

        try {
            List<Email> emails = account.searchEmails(query);
            List<MailDetails> mailDetailsList = new ArrayList<>();
            for (Email email : emails) {
                mailDetailsList.add(CatalogTypes.fromEmail(email, kristaMediaClient));
            }
            return ExtensionResponseFactory.create(Map.of("Mails", mailDetailsList));
        } catch (Exception cause) {
            return ExtensionResponseFactory.create(cause, "Failed to search emails with the provided query",
                    ExtensionResponse.Error.ExceptionType.LOGIC_ERROR);
        }
    }
}
