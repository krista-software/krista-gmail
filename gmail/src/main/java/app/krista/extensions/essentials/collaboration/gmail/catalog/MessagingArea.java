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

import app.krista.extension.authorization.MustAuthorizeException;
import app.krista.extension.executor.ExtensionResponse;
import app.krista.extension.impl.anno.Attribute;
import app.krista.extension.impl.anno.CatalogRequest;
import app.krista.extension.impl.anno.Domain;
import app.krista.extension.impl.anno.Field;
import app.krista.extensions.essentials.collaboration.gmail.GmailAttributes;
import app.krista.extensions.essentials.collaboration.gmail.catalog.entities.MailDetails;
import app.krista.extensions.essentials.collaboration.gmail.catalog.errorhandlers.ErrorHandlingStateManager;
import app.krista.extensions.essentials.collaboration.gmail.catalog.errorhandlers.ExtensionResponseGenerator;
import app.krista.extensions.essentials.collaboration.gmail.catalog.extresp.*;
import app.krista.extensions.essentials.collaboration.gmail.catalog.validators.ValidationOrchestrator;
import app.krista.extensions.essentials.collaboration.gmail.catalog.validators.Validator;
import app.krista.extensions.essentials.collaboration.gmail.impl.GmailNotificationChannel;
import app.krista.extensions.essentials.collaboration.gmail.impl.stores.HistoryIdStore;
import app.krista.extensions.essentials.collaboration.gmail.impl.stores.KristaMediaClient;
import app.krista.extensions.essentials.collaboration.gmail.impl.util.Constants;
import app.krista.extensions.essentials.collaboration.gmail.resources.GmailResources;
import app.krista.extensions.essentials.collaboration.gmail.service.Account;
import app.krista.extensions.essentials.collaboration.gmail.service.Email;
import app.krista.extensions.essentials.collaboration.gmail.service.EmailBuilder;
import app.krista.extensions.essentials.collaboration.gmail.service.Folder;
import app.krista.model.base.File;
import app.krista.model.base.FreeForm;
import com.google.api.services.gmail.model.WatchResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.mail.MessagingException;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import static app.krista.extensions.essentials.collaboration.gmail.impl.GmailNotificationChannel.TO_BE_USED_HISTORY_ID;

@Domain(id = "catEntryDomain_5fa2fc97-4b17-44cf-b98f-aa91a459a091",
        name = "Collaboration",
        ecosystemId = "catEntryEcosystem_84b53163-327b-4b1b-8c96-9334d292f9f5",
        ecosystemName = "Essentials",
        ecosystemVersion = "b94af183-4891-4b54-a9b0-d6096b361fc7")
public final class MessagingArea {

    private static final Logger LOGGER = LoggerFactory.getLogger(MessagingArea.class);
    private static final String SUCCESS = "success";
    private static final String INVALID_ID = "Invalid message id";
    private final Account account;
    private final GmailNotificationChannel gmailNotificationChannel;
    private final KristaMediaClient kristaMediaClient;
    private final HistoryIdStore historyIdStore;
    private final GmailAttributes gmailAttributes;
    private final ValidationOrchestrator validationOrchestrator;
    private final ErrorHandlingStateManager internalStateManager;
    private final ExtensionResponseGenerator responseGenerator;
    private final TelemetryHelper telemetryHelper;

    @Inject
    public MessagingArea(Account account, GmailNotificationChannel gmailNotificationChannel,
                         KristaMediaClient kristaMediaClient, HistoryIdStore historyIdStore,
                         GmailAttributes gmailAttributes, ValidationOrchestrator validationOrchestrator,
                         ErrorHandlingStateManager internalStateManager, ExtensionResponseGenerator responseGenerator, TelemetryHelper telemetryHelper) {
        this.account = account;
        this.gmailNotificationChannel = gmailNotificationChannel;
        this.kristaMediaClient = kristaMediaClient;
        this.historyIdStore = historyIdStore;
        this.gmailAttributes = gmailAttributes;
        this.validationOrchestrator = validationOrchestrator;
        this.internalStateManager = internalStateManager;
        this.responseGenerator = responseGenerator;
        this.telemetryHelper = telemetryHelper;
    }

    /**
     * Generates an error response from validation results without retry option.
     * Uses Java streams for efficient error message construction.
     *
     * @param validationResults List of validation errors
     * @param telemetryKey Telemetry key for tracking
     * @param startTime Request start time in milliseconds
     * @param tags Telemetry tags for additional context
     * @return ExtensionResponse with validation errors
     */
    private ExtensionResponse createValidationErrorResponse(
            List<ValidationOrchestrator.ValidationResult> validationResults,
            String telemetryKey,
            long startTime,
            Map<String, String> tags) {

        String errorMessage = validationResults.stream()
                .map(ValidationOrchestrator.ValidationResult::getErrMessage)
                .collect(Collectors.joining(" "))
                .trim();

        telemetryHelper.recordValidationError(telemetryKey, startTime, errorMessage, tags);

        return ExtensionResponseFactory.create(
                new Exception(errorMessage),
                errorMessage,
                ExtensionResponse.Error.ExceptionType.INPUT_ERROR);
    }

    @CatalogRequest(
            id = "localDomainRequest_e39048cc-1795-4eee-8400-8fc3061c4e87",
            name = "Fetch All Labels",
            description = "Returns list of labels",
            area = "Messaging",
            type = CatalogRequest.Type.QUERY_SYSTEM)
    @Field.Desc(name = "Labels", type = "[ Text ]", required = false)
    public List<String> fetchAllLabels() {
        return account.getFolderNames();
    }

    @CatalogRequest(
            id = "localDomainRequest_ac177adc-e633-4ca1-baf8-7ce7efc5c0e5",
            name = "Fetch Mail By Message Id",
            description = "Accepts message Id as input and returns mail. In case of invalid input, this will return empty data.",
            area = "Messaging",
            type = CatalogRequest.Type.QUERY_SYSTEM)
    @Field.Desc(name = "Mail", type = "Entity(Mail Details)", required = false)
    public ExtensionResponse fetchMailByMessageId(
            @Field(name = "Message ID", type = "Text", attributes = {@Attribute(name = "toolTip", value = "'Unique identifier of the email message to retrieve from Gmail'")}) String messageID,
            @Field(name = "Allow Retry", type = "Switch", required = false, attributes = {@Attribute(name = "toolTip", value = "'Enable retry option for validation errors'")}) Boolean allowRetry) {
        long startTime = System.currentTimeMillis();
        try {
            telemetryHelper.incrementCount(Constants.TELEMETRY_FETCH_MAIL_BY_MESSAGE_ID);

            boolean shouldAllowRetry = allowRetry != null && allowRetry;

            List<ValidationOrchestrator.ValidationResult> validationResults =
                    validationOrchestrator.validate(Map.of(Validator.ValidationResource.MESSAGE_ID, messageID));

            if (validationResults.isEmpty()) {
                Email email = account.getEmail(messageID);
                if (email == null) {
                    telemetryHelper.recordValidationError(Constants.TELEMETRY_FETCH_MAIL_BY_MESSAGE_ID, startTime, INVALID_ID,
                            TelemetryHelper.safeTagMap(Constants.TELEMETRY_TAG_MESSAGE_ID, messageID));
                    return ExtensionResponseFactory.create(Map.of("Mail", email));
                }
                telemetryHelper.recordSuccess(Constants.TELEMETRY_FETCH_MAIL_BY_MESSAGE_ID, startTime,
                        TelemetryHelper.safeTagMap(Constants.TELEMETRY_TAG_MESSAGE_ID, messageID));
                return ExtensionResponseFactory.create(Map.of("Mail", CatalogTypes.fromEmail(email, kristaMediaClient)));
            } else {
                // Only trigger subcatalog retry flow if allowRetry is true
                if (shouldAllowRetry) {
                    telemetryHelper.recordRetryPrompted(Constants.TELEMETRY_FETCH_MAIL_BY_MESSAGE_ID, startTime,
                            TelemetryHelper.safeTagMap(Constants.TELEMETRY_TAG_MESSAGE_ID, messageID, Constants.TELEMETRY_TAG_VALIDATION_COUNT, String.valueOf(validationResults.size())));

                    String stateId = UUID.randomUUID().toString();
                    internalStateManager.put(stateId, Constants.GSON.toJson(Map.of(
                            GmailResources.MESSAGE_ID, messageID,
                            SubCatalogConstants.VALIDATION_RESULTS, validationResults
                    ), Map.class));

                    ExtensionResponse response = responseGenerator.generateConfirmationResponse(
                            ExtensionResponse.Error.ExceptionType.INPUT_ERROR, validationResults,
                            SubCatalogConstants.CONFIRM_REENTER_FETCH_MAIL,
                            Map.of(GmailResources.STATE_ID, stateId, GmailResources.MESSAGE_ID, messageID));

                    telemetryHelper.recordSuccess(Constants.TELEMETRY_FETCH_MAIL_BY_MESSAGE_ID, startTime,
                            TelemetryHelper.safeTagMap(Constants.TELEMETRY_TAG_MESSAGE_ID, messageID));
                    return response;
                } else {
                    return createValidationErrorResponse(
                            validationResults,
                            Constants.TELEMETRY_FETCH_MAIL_BY_MESSAGE_ID,
                            startTime,
                            TelemetryHelper.safeTagMap(
                                    Constants.TELEMETRY_TAG_MESSAGE_ID, messageID,
                                    Constants.TELEMETRY_TAG_VALIDATION_COUNT, String.valueOf(validationResults.size())));
                }
            }
        } catch (MustAuthorizeException cause) {
            telemetryHelper.recordValidationError(Constants.TELEMETRY_FETCH_MAIL_BY_MESSAGE_ID, startTime, cause.getMessage(),
                    TelemetryHelper.safeTagMap(Constants.TELEMETRY_TAG_MESSAGE_ID, messageID));
            throw cause;
        } catch (Exception cause) {
            telemetryHelper.recordValidationError(Constants.TELEMETRY_FETCH_MAIL_BY_MESSAGE_ID, startTime, cause.getMessage(),
                    TelemetryHelper.safeTagMap(Constants.TELEMETRY_TAG_MESSAGE_ID, messageID));
            return ExtensionResponseFactory.create(cause, "We couldn't fetch the email because the message ID appears to " +
                            "be incorrect. Please check the message ID and try again.",
                    ExtensionResponse.Error.ExceptionType.LOGIC_ERROR);
        }
    }

    @CatalogRequest(
            id = "localDomainRequest_95edb739-f511-453c-b0ec-647daf0df206",
            name = "Move Message",
            description = "Accepts message ID, and folder name as input and move one message from source folder to another folder and returns response message.",
            area = "Messaging",
            type = CatalogRequest.Type.CHANGE_SYSTEM)
    @Field(name = "Response", type = "Text", required = false, attributes = {@Attribute(name = "toolTip", value = "'Status message indicating success or failure of the move operation'")})
    public ExtensionResponse moveMessage(
            @Field(name = "Message ID", type = "Text", attributes = {@Attribute(name = "toolTip", value = "'Unique identifier of the email message to be moved'")}) String messageID,
            @Field(name = "Folder Name", type = "Text", attributes = {@Attribute(name = "toolTip", value = "'Name of the destination folder where the email will be moved'")}) String folderName,
            @Field(name = "Allow Retry", type = "Switch", required = false, attributes = {@Attribute(name = "toolTip", value = "'Enable retry option for validation errors'")}) Boolean allowRetry) {
        long startTime = System.currentTimeMillis();
        try {
            telemetryHelper.incrementCount(Constants.TELEMETRY_MOVE_MESSAGE);
            LOGGER.info("Moving message with ID {} to folder: {}", messageID, folderName);

            boolean shouldAllowRetry = allowRetry != null && allowRetry;

            List<ValidationOrchestrator.ValidationResult> validationResults =
                    validationOrchestrator.validate(Map.of(
                            Validator.ValidationResource.MESSAGE_ID, messageID,
                            Validator.ValidationResource.FOLDER_NAME, validateString(folderName)));

            if (validationResults.isEmpty()) {
                Email email = account.getEmail(messageID);
                if (email == null) {
                    telemetryHelper.recordValidationError(Constants.TELEMETRY_MOVE_MESSAGE, startTime, INVALID_ID,
                            TelemetryHelper.safeTagMap(Constants.TELEMETRY_TAG_MESSAGE_ID, messageID, Constants.TELEMETRY_TAG_FOLDER_NAME, folderName));
                    return ExtensionResponseFactory.create(Map.of("Response", INVALID_ID));
                }
                Folder folder = account.getFolderByName(folderName);
                if (folder == null) {
                    telemetryHelper.recordValidationError(Constants.TELEMETRY_MOVE_MESSAGE, startTime, Constants.TELEMETRY_ERROR_INVALID_FOLDER_NAME,
                            TelemetryHelper.safeTagMap(Constants.TELEMETRY_TAG_MESSAGE_ID, messageID, Constants.TELEMETRY_TAG_FOLDER_NAME, folderName));
                    return ExtensionResponseFactory.create(Map.of("Response", "failed."));
                }
                email.moveToFolder(folder);
                telemetryHelper.recordSuccess(Constants.TELEMETRY_MOVE_MESSAGE, startTime,
                        TelemetryHelper.safeTagMap(Constants.TELEMETRY_TAG_MESSAGE_ID, messageID, Constants.TELEMETRY_TAG_FOLDER_NAME, folderName));
                return ExtensionResponseFactory.create(Map.of("Response", SUCCESS));
            } else {
                // Only trigger subcatalog retry flow if allowRetry is true
                if (shouldAllowRetry) {
                    telemetryHelper.recordRetryPrompted(Constants.TELEMETRY_MOVE_MESSAGE, startTime,
                            TelemetryHelper.safeTagMap(Constants.TELEMETRY_TAG_MESSAGE_ID, messageID, Constants.TELEMETRY_TAG_FOLDER_NAME, folderName,
                                    Constants.TELEMETRY_TAG_VALIDATION_COUNT, String.valueOf(validationResults.size())));

                    String stateId = UUID.randomUUID().toString();
                    internalStateManager.put(stateId, Constants.GSON.toJson(Map.of(
                            GmailResources.MESSAGE_ID, messageID,
                            GmailResources.FOLDER_NAME, validateString(folderName),
                            SubCatalogConstants.VALIDATION_RESULTS, validationResults
                    ), Map.class));

                    ExtensionResponse response = responseGenerator.generateConfirmationResponse(
                            ExtensionResponse.Error.ExceptionType.INPUT_ERROR, validationResults,
                            SubCatalogConstants.CONFIRM_REENTER_MOVE_MESSAGE,
                            Map.of(GmailResources.STATE_ID, stateId,
                                    GmailResources.MESSAGE_ID, messageID,
                                    GmailResources.FOLDER_NAME, folderName));

                    telemetryHelper.recordSuccess(Constants.TELEMETRY_MOVE_MESSAGE, startTime,
                            TelemetryHelper.safeTagMap(Constants.TELEMETRY_TAG_MESSAGE_ID, messageID, Constants.TELEMETRY_TAG_FOLDER_NAME, folderName));
                    return response;
                } else {
                    return createValidationErrorResponse(
                            validationResults,
                            Constants.TELEMETRY_MOVE_MESSAGE,
                            startTime,
                            TelemetryHelper.safeTagMap(
                                    Constants.TELEMETRY_TAG_MESSAGE_ID, messageID,
                                    Constants.TELEMETRY_TAG_FOLDER_NAME, folderName,
                                    Constants.TELEMETRY_TAG_VALIDATION_COUNT, String.valueOf(validationResults.size())));
                }
            }
        } catch (Exception cause) {
            telemetryHelper.recordError(Constants.TELEMETRY_MOVE_MESSAGE, startTime, cause,
                    TelemetryHelper.safeTagMap(Constants.TELEMETRY_TAG_MESSAGE_ID, messageID, Constants.TELEMETRY_TAG_FOLDER_NAME, folderName));
            LOGGER.error("Error occurred while moving message to folder: {}", cause.getMessage());
            return ExtensionResponseFactory.create("Error occurred while moving message to folder",
                    ExtensionResponse.Error.ExceptionType.SYSTEM_ERROR,
                    List.of(RemediationActionFactory.createInformActionALLParticipants(
                            "Error occurred while moving message to folder", List.of())),
                    null, null);
        }
    }

    @CatalogRequest(
            id = "localDomainRequest_19b1a828-2f61-49e7-a75a-9956f7d12c5c",
            name = "Reply To All",
            description = "In this request, the user can respond to everyone on the thread. Other recipients will see a message user 'Reply All' to, whether they're in the 'To' or 'Cc' fields.",
            area = "Messaging",
            type = CatalogRequest.Type.CHANGE_SYSTEM)
    @Field(name = "Is Successful", type = "Switch", required = false, attributes = {@Attribute(name = "toolTip", value = "'Indicates whether the reply to all operation was successful'")})
    public ExtensionResponse replyToAll(
            @Field(name = "Message Id", type = "Text", attributes = {@Attribute(name = "toolTip", value = "'Unique identifier of the email message to reply to all recipients'")}) String messageId,
            @Field(name = "Body", type = "Paragraph", attributes = {@Attribute(name = "toolTip", value = "'Content of the reply message that will be sent to all recipients'")}) String message,
            @Field(name = "Attachments", type = "File", required = false, attributes = {@Attribute(name = "toolTip", value = "'Optional files to attach to the reply message'")}) List<File> attachments,
            @Field(name = "Allow Retry", type = "Switch", required = false, attributes = {@Attribute(name = "toolTip", value = "'Enable retry option for validation errors'")}) Boolean allowRetry) {
        long startTime = System.currentTimeMillis();
        try {
            telemetryHelper.incrementCount(Constants.TELEMETRY_REPLY_TO_ALL);
            LOGGER.info("replyToAll: messageId: {}; message: {}", messageId, message);

            boolean shouldAllowRetry = allowRetry != null && allowRetry;

            List<ValidationOrchestrator.ValidationResult> validationResults =
                    validationOrchestrator.validate(Map.of(Validator.ValidationResource.MESSAGE_ID, messageId));

            if (validationResults.isEmpty()) {
                Email email = account.getEmail(messageId);
                if (email == null) {
                    telemetryHelper.recordValidationError(Constants.TELEMETRY_REPLY_TO_ALL, startTime, INVALID_ID,
                            TelemetryHelper.safeTagMap(Constants.TELEMETRY_TAG_MESSAGE_ID, messageId));
                    return ExtensionResponseFactory.create(Map.of("Is Successful", false));
                }
                try {
                    message = message != null ? message.replace("\n", "<br>") : message;
                    email.replyToAll(message, CatalogTypes.toAttachments(attachments, kristaMediaClient), null, null, null);
                    telemetryHelper.recordSuccess(Constants.TELEMETRY_REPLY_TO_ALL, startTime,
                            TelemetryHelper.safeTagMap(Constants.TELEMETRY_TAG_MESSAGE_ID, messageId));
                    return ExtensionResponseFactory.create(Map.of("Is Successful", true));
                } catch (MessagingException | IOException cause) {
                    telemetryHelper.recordError(Constants.TELEMETRY_REPLY_TO_ALL, startTime, cause,
                            TelemetryHelper.safeTagMap(Constants.TELEMETRY_TAG_MESSAGE_ID, messageId));
                    LOGGER.error("Reply to all failed with ID: {} with error message: {}", messageId, cause.getMessage());
                    return ExtensionResponseFactory.create(Map.of("Is Successful", false));
                }
            } else {
                // Only trigger subcatalog retry flow if allowRetry is true
                if (shouldAllowRetry) {
                    telemetryHelper.recordRetryPrompted(Constants.TELEMETRY_REPLY_TO_ALL, startTime,
                            TelemetryHelper.safeTagMap(Constants.TELEMETRY_TAG_MESSAGE_ID, messageId,
                                    Constants.TELEMETRY_TAG_VALIDATION_COUNT, String.valueOf(validationResults.size())));

                    String stateId = UUID.randomUUID().toString();
                    internalStateManager.put(stateId, Constants.GSON.toJson(Map.of(
                            GmailResources.MESSAGE_ID, messageId,
                            GmailResources.MESSAGE, validateString(message),
                            GmailResources.ATTACHMENTS, attachments != null ? attachments : List.of(),
                            SubCatalogConstants.VALIDATION_RESULTS, validationResults)));

                    ExtensionResponse response = responseGenerator.generateConfirmationResponse(
                            ExtensionResponse.Error.ExceptionType.INPUT_ERROR, validationResults,
                            SubCatalogConstants.CONFIRM_REENTER_REPLY_TO_ALL,
                            Map.of(GmailResources.STATE_ID, stateId,
                                    GmailResources.MESSAGE_ID, messageId,
                                    GmailResources.MESSAGE, validateString(message),
                                    GmailResources.ATTACHMENTS, attachments != null ? attachments : List.of()));

                    telemetryHelper.recordSuccess(Constants.TELEMETRY_REPLY_TO_ALL, startTime,
                            TelemetryHelper.safeTagMap(Constants.TELEMETRY_TAG_MESSAGE_ID, messageId));
                    return response;
                } else {
                    return createValidationErrorResponse(
                            validationResults,
                            Constants.TELEMETRY_REPLY_TO_ALL,
                            startTime,
                            TelemetryHelper.safeTagMap(
                                    Constants.TELEMETRY_TAG_MESSAGE_ID, messageId,
                                    Constants.TELEMETRY_TAG_VALIDATION_COUNT, String.valueOf(validationResults.size())));
                }
            }
        } catch (MustAuthorizeException cause) {
            telemetryHelper.recordValidationError(Constants.TELEMETRY_REPLY_TO_ALL, startTime, cause.getMessage(),
                    TelemetryHelper.safeTagMap(Constants.TELEMETRY_TAG_MESSAGE_ID, messageId));
            throw cause;
        } catch (Exception cause) {
            telemetryHelper.recordError(Constants.TELEMETRY_REPLY_TO_ALL, startTime, cause,
                    TelemetryHelper.safeTagMap(Constants.TELEMETRY_TAG_MESSAGE_ID, messageId));
            return ExtensionResponseFactory.create(cause, "Failed to reply all",
                    ExtensionResponse.Error.ExceptionType.INPUT_ERROR);
        }
    }

    @CatalogRequest(
            id = "localDomainRequest_cf7ac51e-752b-44e8-a55a-2a5dd4dbfbef",
            name = "Fetch Sent",
            description = "Accepts page number, and page size as input and returns list of mails from sent folder. Page number, and page size are optional parameters.",
            area = "Messaging",
            type = CatalogRequest.Type.QUERY_SYSTEM)
    @Field.Desc(name = "Sent Mails", type = "[ Entity(Mail Details) ]", required = false)
    public ExtensionResponse fetchSent(
            @Field(name = "Page Number", type = "Number", required = false, attributes = {@Attribute(name = "toolTip", value = "'Page number for pagination (1-15, default: 1)'")}) Double pageNumber,
            @Field(name = "Page Size", type = "Number", required = false, attributes = {@Attribute(name = "toolTip", value = "'Number of emails per page (1-15, default: 15)'")}) Double pageSize,
            @Field(name = "Allow Retry", type = "Switch", required = false, attributes = {@Attribute(name = "toolTip", value = "'Enable retry option for validation errors'")}) Boolean allowRetry) {
        long startTime = System.currentTimeMillis();
        try {
            telemetryHelper.incrementCount(Constants.TELEMETRY_FETCH_SENT);

            boolean shouldAllowRetry = allowRetry != null && allowRetry;

            if (pageNumber != null || pageSize != null) {
                List<ValidationOrchestrator.ValidationResult> validationResults =
                        validationOrchestrator.validate(Map.of(
                                Validator.ValidationResource.PAGE_NUMBER, String.valueOf(pageNumber),
                                Validator.ValidationResource.PAGE_SIZE, String.valueOf(pageSize)));

                if (validationResults.isEmpty()) {
                    return fetchSentResponse(pageNumber, pageSize);
                } else {
                    if (shouldAllowRetry) {
                        telemetryHelper.recordRetryPrompted(Constants.TELEMETRY_FETCH_SENT, startTime,
                                TelemetryHelper.safeTagMap(Constants.TELEMETRY_TAG_PAGE_NUMBER, String.valueOf(pageNumber),
                                        Constants.TELEMETRY_TAG_PAGE_SIZE, String.valueOf(pageSize),
                                        Constants.TELEMETRY_TAG_VALIDATION_COUNT, String.valueOf(validationResults.size())));
                        String stateId = UUID.randomUUID().toString();
                        Map<String, Object> stateMap = new java.util.HashMap<>(3);
                        stateMap.put(GmailResources.PAGE_NUMBER, pageNumber);
                        stateMap.put(GmailResources.PAGE_SIZE, pageSize);
                        stateMap.put(SubCatalogConstants.VALIDATION_RESULTS, validationResults);
                        internalStateManager.put(stateId, Constants.GSON.toJson(stateMap));

                        Map<String, Object> confirmationMap = new java.util.HashMap<>(3);
                        confirmationMap.put(GmailResources.STATE_ID, stateId);
                        confirmationMap.put(GmailResources.PAGE_NUMBER, pageNumber);
                        confirmationMap.put(GmailResources.PAGE_SIZE, pageSize);
                        ExtensionResponse response = responseGenerator.generateConfirmationResponse(
                                ExtensionResponse.Error.ExceptionType.INPUT_ERROR, validationResults,
                                SubCatalogConstants.CONFIRM_REENTER_FETCH_SENT,
                                confirmationMap);

                        telemetryHelper.recordSuccess(Constants.TELEMETRY_FETCH_SENT, startTime,
                                TelemetryHelper.safeTagMap(Constants.TELEMETRY_TAG_PAGE_NUMBER, String.valueOf(pageNumber),
                                        Constants.TELEMETRY_TAG_PAGE_SIZE, String.valueOf(pageSize)));
                        return response;
                    } else {
                        return createValidationErrorResponse(
                                validationResults,
                                Constants.TELEMETRY_FETCH_SENT,
                                startTime,
                                TelemetryHelper.safeTagMap(
                                        Constants.TELEMETRY_TAG_PAGE_NUMBER, String.valueOf(pageNumber),
                                        Constants.TELEMETRY_TAG_PAGE_SIZE, String.valueOf(pageSize),
                                        Constants.TELEMETRY_TAG_VALIDATION_COUNT, String.valueOf(validationResults.size())));
                    }
                }
            } else {
                return fetchSentResponse(pageNumber, pageSize);
            }
        } catch (MustAuthorizeException cause) {
            telemetryHelper.recordValidationError(Constants.TELEMETRY_FETCH_SENT, startTime, cause.getMessage(),
                    TelemetryHelper.safeTagMap(Constants.TELEMETRY_TAG_PAGE_NUMBER, String.valueOf(pageNumber),
                            Constants.TELEMETRY_TAG_PAGE_SIZE, String.valueOf(pageSize)));
            throw cause;
        } catch (Exception cause) {
            LOGGER.error("Error occurred while fetch sent:{}", cause.getMessage());
            telemetryHelper.recordError(Constants.TELEMETRY_FETCH_SENT, startTime, cause,
                    TelemetryHelper.safeTagMap(Constants.TELEMETRY_TAG_PAGE_NUMBER, String.valueOf(pageNumber),
                            Constants.TELEMETRY_TAG_PAGE_SIZE, String.valueOf(pageSize)));
            return ExtensionResponseFactory.create("Error occurred while fetch sent", ExtensionResponse.Error.ExceptionType.SYSTEM_ERROR,
                    List.of(RemediationActionFactory.createInformActionALLParticipants("Error occurred while fetch sent", List.of())),
                    null, null);
        }
    }

    private ExtensionResponse fetchSentResponse(Double pageNumber, Double pageSize) {
        List<Email> emails = account.getSentFolder().getEmails(pageNumber, pageSize);
        List<MailDetails> mailDetailsList = new ArrayList<>();
        for (Email email : emails) {
            mailDetailsList.add(CatalogTypes.fromEmail(email, kristaMediaClient));
        }
        return ExtensionResponseFactory.create(Map.of("Sent Mails", mailDetailsList));

    }

    @CatalogRequest(
            id = "localDomainRequest_7519e728-cca1-447f-8c58-ad4e80eefb00",
            name = "Forward Mail",
            description = "This request allows a sender to forward the received email to other recipients. In case of multiple recipients please provide comma-separated inputs as part of parameter 'To'.",
            area = "Messaging",
            type = CatalogRequest.Type.CHANGE_SYSTEM)
    @Field(name = "Is Forwarded", type = "Switch", required = false, attributes = {@Attribute(name = "toolTip", value = "'Indicates whether the email was successfully forwarded'")})
    public ExtensionResponse forwardMail(
            @Field(name = "Message Id", type = "Text", attributes = {@Attribute(name = "toolTip", value = "'Unique identifier of the email message to be forwarded'")}) String messageId,
            @Field(name = "To", type = "Text", attributes = {@Attribute(name = "toolTip", value = "'Recipient email addresses (comma-separated for multiple recipients)'")}) String to,
            @Field(name = "Message", type = "Paragraph", attributes = {@Attribute(name = "toolTip", value = "'Additional message content to include with the forwarded email'")}) String message,
            @Field(name = "Allow Retry", type = "Switch", required = false, attributes = {@Attribute(name = "toolTip", value = "'Enable retry option for validation errors'")}) Boolean allowRetry) {
        long startTime = System.currentTimeMillis();
        try {
            telemetryHelper.incrementCount(Constants.TELEMETRY_FORWARD_MAIL);
            LOGGER.info("Forwarding mail with messageId: {}; to: {}; message: {}", messageId, to, message);

            boolean shouldAllowRetry = allowRetry != null && allowRetry;

            List<ValidationOrchestrator.ValidationResult> validationResults =
                    validationOrchestrator.validate(Map.of(
                            Validator.ValidationResource.MESSAGE_ID, messageId,
                            Validator.ValidationResource.TO, validateString(to)));

            if (validationResults.isEmpty()) {
                Email email = account.getEmail(messageId);
                if (email == null) {
                    telemetryHelper.recordValidationError(Constants.TELEMETRY_FORWARD_MAIL, startTime, INVALID_ID,
                            TelemetryHelper.safeTagMap(Constants.TELEMETRY_TAG_MESSAGE_ID, messageId, Constants.TELEMETRY_TAG_TO, to));
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
                telemetryHelper.recordSuccess(Constants.TELEMETRY_FORWARD_MAIL, startTime,
                        TelemetryHelper.safeTagMap(Constants.TELEMETRY_TAG_MESSAGE_ID, messageId, Constants.TELEMETRY_TAG_TO, to));
                return ExtensionResponseFactory.create(Map.of("Is Forwarded", true));
            } else {
                // Only trigger subcatalog retry flow if allowRetry is true
                if (shouldAllowRetry) {
                    telemetryHelper.recordRetryPrompted(Constants.TELEMETRY_FORWARD_MAIL, startTime,
                            TelemetryHelper.safeTagMap(Constants.TELEMETRY_TAG_MESSAGE_ID, messageId, Constants.TELEMETRY_TAG_TO, to,
                                    Constants.TELEMETRY_TAG_VALIDATION_COUNT, String.valueOf(validationResults.size())));

                    String stateId = UUID.randomUUID().toString();
                    internalStateManager.put(stateId, Constants.GSON.toJson(Map.of(
                            GmailResources.MESSAGE_ID, messageId,
                            GmailResources.TO, validateString(to),
                            GmailResources.MESSAGE, validateString(message),
                            SubCatalogConstants.VALIDATION_RESULTS, validationResults)));

                    ExtensionResponse response = responseGenerator.generateConfirmationResponse(
                            ExtensionResponse.Error.ExceptionType.INPUT_ERROR, validationResults,
                            SubCatalogConstants.CONFIRM_REENTER_FORWARD_MAIL,
                            Map.of(GmailResources.STATE_ID, stateId,
                                    GmailResources.MESSAGE_ID, messageId,
                                    GmailResources.TO, validateString(to),
                                    GmailResources.MESSAGE, validateString(message)));

                    telemetryHelper.recordSuccess(Constants.TELEMETRY_FORWARD_MAIL, startTime,
                            TelemetryHelper.safeTagMap(Constants.TELEMETRY_TAG_MESSAGE_ID, messageId, Constants.TELEMETRY_TAG_TO, to));
                    return response;
                } else {
                    return createValidationErrorResponse(
                            validationResults,
                            Constants.TELEMETRY_FORWARD_MAIL,
                            startTime,
                            TelemetryHelper.safeTagMap(
                                    Constants.TELEMETRY_TAG_MESSAGE_ID, messageId,
                                    Constants.TELEMETRY_TAG_TO, to,
                                    Constants.TELEMETRY_TAG_VALIDATION_COUNT, String.valueOf(validationResults.size())));
                }
            }
        } catch (MustAuthorizeException cause) {
            telemetryHelper.recordValidationError(Constants.TELEMETRY_FORWARD_MAIL, startTime, cause.getMessage(),
                    TelemetryHelper.safeTagMap(Constants.TELEMETRY_TAG_MESSAGE_ID, messageId, Constants.TELEMETRY_TAG_TO, to));
            throw cause;
        } catch (Exception cause) {
            telemetryHelper.recordError(Constants.TELEMETRY_FORWARD_MAIL, startTime, cause,
                    TelemetryHelper.safeTagMap(Constants.TELEMETRY_TAG_MESSAGE_ID, messageId, Constants.TELEMETRY_TAG_TO, to));
            LOGGER.error("Forward mail failed for ID: {} with error message: {}", messageId, cause.getMessage());
            return ExtensionResponseFactory.create(Map.of("Is Forwarded", false));
        }
    }

    @CatalogRequest(
            id = "localDomainRequest_45b7479d-c009-4b0f-85e6-0fe38d9fc35d",
            name = "Fetch Mail Details By Query",
            description = "Accepts search query as input and returns list of mails",
            area = "Messaging",
            type = CatalogRequest.Type.QUERY_SYSTEM)
    @Field.Desc(name = "Mails", type = "[ Entity(Mail Details) ]", required = false)
    public ExtensionResponse fetchMailDetailsByQuery(
            @Field(name = "Query", type = "Text", attributes = {@Attribute(name = "toolTip", value = "'Search query to find emails'")}) String query,
            @Field(name = "Allow Retry", type = "Switch", required = false, attributes = {@Attribute(name = "toolTip", value = "'Enable retry option for validation errors'")}) Boolean allowRetry) {
        long startTime = System.currentTimeMillis();
        try {
            telemetryHelper.incrementCount(Constants.TELEMETRY_FETCH_MAIL_DETAILS_BY_QUERY);
            LOGGER.info("fetchMailDetailsByQuery: {}", query);

            boolean shouldAllowRetry = allowRetry != null && allowRetry;

            List<ValidationOrchestrator.ValidationResult> validationResults =
                    validationOrchestrator.validate(Map.of(Validator.ValidationResource.QUERY, validateString(query)));

            if (validationResults.isEmpty()) {
                List<Email> emails = account.searchEmails(query);
                List<MailDetails> response = new ArrayList<>();
                for (Email email : emails) {
                    response.add(CatalogTypes.fromEmail(email, kristaMediaClient));
                }
                return ExtensionResponseFactory.create(Map.of("Mails", response));
            } else {
                // Only trigger subcatalog retry flow if allowRetry is true
                if (shouldAllowRetry) {
                    String stateId = UUID.randomUUID().toString();
                    internalStateManager.put(stateId, Constants.GSON.toJson(Map.of(
                            GmailResources.QUERY, validateString(query),
                            SubCatalogConstants.VALIDATION_RESULTS, validationResults)));

                    ExtensionResponse response = responseGenerator.generateConfirmationResponse(
                            ExtensionResponse.Error.ExceptionType.INPUT_ERROR, validationResults,
                            SubCatalogConstants.CONFIRM_REENTER_FETCH_MAIL_BY_QUERY,
                            Map.of(GmailResources.STATE_ID, stateId,
                                    GmailResources.QUERY, validateString(query)));

                    telemetryHelper.recordSuccess(Constants.TELEMETRY_FETCH_MAIL_DETAILS_BY_QUERY, startTime,
                            TelemetryHelper.safeTagMap(Constants.TELEMETRY_TAG_QUERY, query));
                    return response;
                } else {
                    return createValidationErrorResponse(
                            validationResults,
                            Constants.TELEMETRY_FETCH_MAIL_DETAILS_BY_QUERY,
                            startTime,
                            TelemetryHelper.safeTagMap(Constants.TELEMETRY_TAG_QUERY, query));
                }
            }
        } catch (MustAuthorizeException cause) {
            telemetryHelper.recordValidationError(Constants.TELEMETRY_FETCH_MAIL_DETAILS_BY_QUERY, startTime, cause.getMessage(),
                    TelemetryHelper.safeTagMap(Constants.TELEMETRY_TAG_QUERY, query));
            throw cause;
        } catch (Exception cause) {
            telemetryHelper.recordError(Constants.TELEMETRY_FETCH_MAIL_DETAILS_BY_QUERY, startTime, cause,
                    TelemetryHelper.safeTagMap(Constants.TELEMETRY_TAG_QUERY, query));
            LOGGER.error("Error occurred while searching emails with query '{}': {}", query, cause.getMessage(), cause);
            return ExtensionResponseFactory.create(Map.of("Mails", List.of()));
        }
    }

    @CatalogRequest(
            id = "localDomainRequest_6d34be22-e420-4087-b55d-0659f899b140",
            name = "Send Mail",
            description = "Accepts subject, message, attachments, to, bcc, cc, reply to as input and returns response message. Attachments, bcc, cc, and reply to are optional inputs.",
            area = "Messaging",
            type = CatalogRequest.Type.CHANGE_SYSTEM)
    @Field(name = "Message", type = "Text", required = false, attributes = {@Attribute(name = "toolTip", value = "'Status message indicating success or failure of the send operation'")})
    public ExtensionResponse sendMail(
            @Field(name = "Subject", type = "Text", required = false, attributes = {@Attribute(name = "toolTip", value = "'Subject line of the email'")}) String subject,
            @Field(name = "Message", type = "RichText", required = false, attributes = {@Attribute(name = "toolTip", value = "'Content of the email message (supports rich text formatting)'")}) String message,
            @Field(name = "Attachments", type = "File", required = false) List<File> attachments,
            @Field(name = "To", type = "Text", attributes = {@Attribute(name = "toolTip", value = "'Primary recipient email addresses (comma-separated for multiple recipients)'")}) String to,
            @Field(name = "Bcc", type = "Text", required = false, attributes = {@Attribute(name = "toolTip", value = "'Blind carbon copy recipients (comma-separated for multiple recipients)'")}) String bcc,
            @Field(name = "Cc", type = "Text", required = false, attributes = {@Attribute(name = "toolTip", value = "'Carbon copy recipients (comma-separated for multiple recipients)'")}) String cc,
            @Field(name = "Reply To", type = "Text", required = false, attributes = {@Attribute(name = "toolTip", value = "'Email address for replies to be sent to'")}) String replyTo,
            @Field(name = "Allow Retry", type = "Switch", required = false, attributes = {@Attribute(name = "toolTip", value = "'Enable retry option for validation errors'")}) Boolean allowRetry) {
        long startTime = System.currentTimeMillis();
        try {
            telemetryHelper.incrementCount(Constants.TELEMETRY_SEND_MAIL);
            LOGGER.info("Sending email to: {}", to);

            boolean shouldAllowRetry = allowRetry != null && allowRetry;

            List<ValidationOrchestrator.ValidationResult> validationResults = validationOrchestrator.validate(Map.of(
                    Validator.ValidationResource.TO, to,
                    Validator.ValidationResource.CC, validateString(cc),
                    Validator.ValidationResource.BCC, validateString(bcc),
                    Validator.ValidationResource.REPLY_TO, validateString(replyTo)));

            if (validationResults.isEmpty()) {
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
                telemetryHelper.recordSuccess(Constants.TELEMETRY_SEND_MAIL, startTime,
                        TelemetryHelper.safeTagMap(Constants.TELEMETRY_TAG_TO, to, Constants.TELEMETRY_TAG_CC, validateString(cc), Constants.TELEMETRY_TAG_BCC, validateString(bcc)));
                return ExtensionResponseFactory.create(Map.of("Message", SUCCESS));
            } else {
                // Only trigger subcatalog retry flow if allowRetry is true
                if (shouldAllowRetry) {
                    telemetryHelper.recordRetryPrompted(Constants.TELEMETRY_SEND_MAIL, startTime,
                            TelemetryHelper.safeTagMap(Constants.TELEMETRY_TAG_TO, to, Constants.TELEMETRY_TAG_CC, validateString(cc), Constants.TELEMETRY_TAG_BCC, validateString(bcc),
                                    Constants.TELEMETRY_TAG_VALIDATION_COUNT, String.valueOf(validationResults.size())));

                    String stateId = UUID.randomUUID().toString();
                    internalStateManager.put(stateId, Constants.GSON.toJson(Map.of(
                            GmailResources.SUBJECT, validateString(subject),
                            GmailResources.MESSAGE, validateString(message),
                            GmailResources.ATTACHMENTS, attachments != null ? attachments : List.of(),
                            GmailResources.TO, validateString(to),
                            GmailResources.BCC, validateString(bcc),
                            GmailResources.CC, validateString(cc),
                            GmailResources.REPLY_TO, validateString(replyTo),
                            SubCatalogConstants.VALIDATION_RESULTS, validationResults)));

                    ExtensionResponse response = responseGenerator.generateConfirmationResponse(
                            ExtensionResponse.Error.ExceptionType.INPUT_ERROR, validationResults,
                            SubCatalogConstants.CONFIRM_REENTER_SEND_MAIL,
                            Map.of(GmailResources.STATE_ID, stateId,
                                    GmailResources.SUBJECT, validateString(subject),
                                    GmailResources.MESSAGE, validateString(message),
                                    GmailResources.ATTACHMENTS, attachments != null ? attachments : List.of(),
                                    GmailResources.TO, validateString(to),
                                    GmailResources.BCC, validateString(bcc),
                                    GmailResources.CC, validateString(cc),
                                    GmailResources.REPLY_TO, validateString(replyTo)));

                    telemetryHelper.recordSuccess(Constants.TELEMETRY_SEND_MAIL, startTime,
                            TelemetryHelper.safeTagMap(Constants.TELEMETRY_TAG_TO, to, Constants.TELEMETRY_TAG_CC, validateString(cc), Constants.TELEMETRY_TAG_BCC, validateString(bcc)));
                    return response;
                } else {
                    return createValidationErrorResponse(
                            validationResults,
                            Constants.TELEMETRY_SEND_MAIL,
                            startTime,
                            TelemetryHelper.safeTagMap(
                                    Constants.TELEMETRY_TAG_TO, to,
                                    Constants.TELEMETRY_TAG_CC, validateString(cc),
                                    Constants.TELEMETRY_TAG_BCC, validateString(bcc)));
                }
            }
        } catch (MustAuthorizeException cause) {
            telemetryHelper.recordValidationError(Constants.TELEMETRY_SEND_MAIL, startTime, cause.getMessage(),
                    TelemetryHelper.safeTagMap(Constants.TELEMETRY_TAG_TO, to, Constants.TELEMETRY_TAG_CC, validateString(cc), Constants.TELEMETRY_TAG_BCC, validateString(bcc)));
            throw cause;
        } catch (Exception cause) {
            telemetryHelper.recordError(Constants.TELEMETRY_SEND_MAIL, startTime, cause,
                    TelemetryHelper.safeTagMap(Constants.TELEMETRY_TAG_TO, to, Constants.TELEMETRY_TAG_CC, validateString(cc), Constants.TELEMETRY_TAG_BCC, validateString(bcc)));
            LOGGER.error("Send mail failed with error message: {}", cause);
            return ExtensionResponseFactory.create(Map.of("Message", "Failed to send mail"));
        }
    }

    private String validateString(String value) {
        return value != null ? value : "";
    }

    @CatalogRequest(
            id = "localDomainRequest_bbdc1184-9dc1-4448-8bdb-ec6c9ee913a7",
            name = "Fetch Inbox",
            description = "Accepts page number, and page size as input and returns list of mail. Page number, and page size are optional parameters.",
            area = "Messaging",
            type = CatalogRequest.Type.QUERY_SYSTEM)
    @Field.Desc(name = "Inbox Mails", type = "[ Entity(Mail Details) ]", required = false)
    public ExtensionResponse fetchInbox(
            @Field(name = "Page Number", type = "Number", required = false, attributes = {@Attribute(name = "toolTip", value = "'Page number for pagination (1-15, default: 1)'")}) Double pageNumber,
            @Field(name = "Page Size", type = "Number", required = false, attributes = {@Attribute(name = "toolTip", value = "'Number of emails per page (1-15, default: 15)'")}) Double pageSize,
            @Field(name = "Allow Retry", type = "Switch", required = false, attributes = {@Attribute(name = "toolTip", value = "'Enable retry option for validation errors'")}) Boolean allowRetry) {
        long startTime = System.currentTimeMillis();
        try {
            telemetryHelper.incrementCount(Constants.TELEMETRY_FETCH_INBOX);
            LOGGER.info("fetchInbox: pageNumber: {}; pageSize: {}", pageNumber, pageSize);

            boolean shouldAllowRetry = allowRetry != null && allowRetry;

            Map<Validator.ValidationResource, String> validationResourceMap = ValidationResourceUtil.prepareValidateFetchInboxMap(pageNumber, pageSize);
            if (validationResourceMap.isEmpty()) {
                ExtensionResponse response = fetchInboxResponse(pageNumber, pageSize);
                telemetryHelper.recordSuccess(Constants.TELEMETRY_FETCH_INBOX, startTime, Map.of(Constants.TELEMETRY_TAG_PAGE_NUMBER, String.valueOf(pageNumber), Constants.TELEMETRY_TAG_PAGE_SIZE, String.valueOf(pageSize)));
                return response;
            }

            List<ValidationOrchestrator.ValidationResult> validationResults = validationOrchestrator.validate(validationResourceMap);
            if (validationResults.isEmpty()) {
                ExtensionResponse response = fetchInboxResponse(pageNumber, pageSize);
                telemetryHelper.recordSuccess(Constants.TELEMETRY_FETCH_INBOX, startTime, Map.of(Constants.TELEMETRY_TAG_PAGE_NUMBER, String.valueOf(pageNumber), Constants.TELEMETRY_TAG_PAGE_SIZE, String.valueOf(pageSize)));
                return response;
            } else {
                // Only trigger subcatalog retry flow if allowRetry is true
                if (shouldAllowRetry) {
                    String stateId = UUID.randomUUID().toString();
                    Map<String, Object> stateMap = new java.util.HashMap<>(3);
                    stateMap.put(GmailResources.PAGE_NUMBER, pageNumber);
                    stateMap.put(GmailResources.PAGE_SIZE, pageSize);
                    stateMap.put(SubCatalogConstants.VALIDATION_RESULTS, validationResults);
                    internalStateManager.put(stateId, Constants.GSON.toJson(stateMap));

                    telemetryHelper.recordValidationError(Constants.TELEMETRY_FETCH_INBOX, startTime, Constants.TELEMETRY_ERROR_VALIDATION_FAILED,
                            TelemetryHelper.safeTagMap(Constants.TELEMETRY_TAG_PAGE_NUMBER, String.valueOf(pageNumber), Constants.TELEMETRY_TAG_PAGE_SIZE, String.valueOf(pageSize)));

                    Map<String, Object> confirmationMap = new java.util.HashMap<>(3);
                    confirmationMap.put(GmailResources.STATE_ID, stateId);
                    confirmationMap.put(GmailResources.PAGE_NUMBER, pageNumber);
                    confirmationMap.put(GmailResources.PAGE_SIZE, pageSize);
                    ExtensionResponse response = responseGenerator.generateConfirmationResponse(
                            ExtensionResponse.Error.ExceptionType.INPUT_ERROR, validationResults,
                            SubCatalogConstants.CONFIRM_REENTER_FETCH_INBOX,
                            confirmationMap);

                    telemetryHelper.recordSuccess(Constants.TELEMETRY_FETCH_INBOX, startTime,
                            TelemetryHelper.safeTagMap(Constants.TELEMETRY_TAG_PAGE_NUMBER, String.valueOf(pageNumber), Constants.TELEMETRY_TAG_PAGE_SIZE, String.valueOf(pageSize)));
                    return response;
                } else {
                    return createValidationErrorResponse(
                            validationResults,
                            Constants.TELEMETRY_FETCH_INBOX,
                            startTime,
                            TelemetryHelper.safeTagMap(
                                    Constants.TELEMETRY_TAG_PAGE_NUMBER, String.valueOf(pageNumber),
                                    Constants.TELEMETRY_TAG_PAGE_SIZE, String.valueOf(pageSize)));
                }
            }
        } catch (MustAuthorizeException cause) {
            telemetryHelper.recordValidationError(Constants.TELEMETRY_FETCH_INBOX, startTime, cause.getMessage(),
                    TelemetryHelper.safeTagMap(Constants.TELEMETRY_TAG_PAGE_NUMBER, String.valueOf(pageNumber), Constants.TELEMETRY_TAG_PAGE_SIZE, String.valueOf(pageSize)));
            throw cause;
        } catch (Exception cause) {
            telemetryHelper.recordError(Constants.TELEMETRY_FETCH_INBOX, startTime, cause,
                    TelemetryHelper.safeTagMap(Constants.TELEMETRY_TAG_PAGE_NUMBER, String.valueOf(pageNumber), Constants.TELEMETRY_TAG_PAGE_SIZE, String.valueOf(pageSize)));
            LOGGER.error("Error occurred while fetching inbox: pageNumber: {}; pageSize: {}; error: {}", pageNumber, pageSize, cause.getMessage(), cause);
            return ExtensionResponseFactory.create("Error occurred while fetching inbox", ExtensionResponse.Error.ExceptionType.SYSTEM_ERROR,
                    List.of(RemediationActionFactory.createInformActionALLParticipants("Error occurred while fetching inbox", List.of())),
                    null, null);
        }
    }

    private ExtensionResponse fetchInboxResponse(Double pageNumber, Double pageSize) {
        List<Email> emails = account.getInboxFolder().getEmails(pageNumber, pageSize);
        List<MailDetails> mailDetailsList = new ArrayList<>();
        for (Email email : emails) {
            mailDetailsList.add(CatalogTypes.fromEmail(email, kristaMediaClient));
        }
        return ExtensionResponseFactory.create(Map.of("Inbox Mails", mailDetailsList));
    }

    @CatalogRequest(
            id = "localDomainRequest_20716cd4-a8a9-43ab-ae5c-4bef25ed4623",
            name = "Mark Message",
            description = "Accepts message ID, and label as input and mark mail as read/unread and returns response message",
            area = "Messaging",
            type = CatalogRequest.Type.CHANGE_SYSTEM)
    @Field(name = "Response", type = "Text", required = false, attributes = {@Attribute(name = "toolTip", value = "'Status message indicating success or failure of the mark operation'")})
    public ExtensionResponse markMessage(
            @Field(name = "Message ID", type = "Text", attributes = {@Attribute(name = "toolTip", value = "'Unique identifier of the email message to mark'")}) String messageID,
            @Field.Desc(name = "Label", type = "PickOne(Read|Unread)") String label,
            @Field(name = "Allow Retry", type = "Switch", required = false, attributes = {@Attribute(name = "toolTip", value = "'Enable retry option for validation errors'")}) Boolean allowRetry) {
        long startTime = System.currentTimeMillis();
        try {
            telemetryHelper.incrementCount(Constants.TELEMETRY_MARK_MESSAGE);
            LOGGER.info("Marking message with ID {} as: {}", messageID, label);

            boolean shouldAllowRetry = allowRetry != null && allowRetry;

            List<ValidationOrchestrator.ValidationResult> validationResults =
                    validationOrchestrator.validate(Map.of(Validator.ValidationResource.MESSAGE_ID, messageID));

            if (validationResults.isEmpty()) {
                Email email = account.getEmail(messageID);
                if (email == null) {
                    telemetryHelper.recordValidationError(Constants.TELEMETRY_MARK_MESSAGE, startTime, INVALID_ID,
                            TelemetryHelper.safeTagMap(Constants.TELEMETRY_TAG_MESSAGE_ID, messageID, Constants.TELEMETRY_TAG_LABEL, label));
                    return ExtensionResponseFactory.create(Map.of("Response", INVALID_ID));
                }
                if (label.equalsIgnoreCase("read")) {
                    email.markAsRead();
                } else if (label.equalsIgnoreCase("unread")) {
                    email.markAsUnread();
                } else {
                    telemetryHelper.recordValidationError(Constants.TELEMETRY_MARK_MESSAGE, startTime, Constants.TELEMETRY_ERROR_INVALID_LABEL,
                            TelemetryHelper.safeTagMap(Constants.TELEMETRY_TAG_MESSAGE_ID, messageID, Constants.TELEMETRY_TAG_LABEL, label));
                    return ExtensionResponseFactory.create(Map.of("Response", "Invalid label"));
                }
                telemetryHelper.recordSuccess(Constants.TELEMETRY_MARK_MESSAGE, startTime,
                        TelemetryHelper.safeTagMap(Constants.TELEMETRY_TAG_MESSAGE_ID, messageID, Constants.TELEMETRY_TAG_LABEL, label));
                return ExtensionResponseFactory.create(Map.of("Response", SUCCESS));
            } else {
                // Only trigger subcatalog retry flow if allowRetry is true
                if (shouldAllowRetry) {
                    telemetryHelper.recordRetryPrompted(Constants.TELEMETRY_MARK_MESSAGE, startTime,
                            TelemetryHelper.safeTagMap(Constants.TELEMETRY_TAG_MESSAGE_ID, messageID, Constants.TELEMETRY_TAG_LABEL, label,
                                    Constants.TELEMETRY_TAG_VALIDATION_COUNT, String.valueOf(validationResults.size())));

                    String stateId = UUID.randomUUID().toString();
                    internalStateManager.put(stateId, Constants.GSON.toJson(Map.of(
                            GmailResources.MESSAGE_ID, messageID,
                            GmailResources.LABEL, label,
                            SubCatalogConstants.VALIDATION_RESULTS, validationResults)));

                    ExtensionResponse response = responseGenerator.generateConfirmationResponse(
                            ExtensionResponse.Error.ExceptionType.INPUT_ERROR, validationResults,
                            SubCatalogConstants.CONFIRM_REENTER_MARK_MESSAGE,
                            Map.of(GmailResources.STATE_ID, stateId,
                                    GmailResources.MESSAGE_ID, messageID,
                                    GmailResources.LABEL, label));

                    telemetryHelper.recordSuccess(Constants.TELEMETRY_MARK_MESSAGE, startTime,
                            TelemetryHelper.safeTagMap(Constants.TELEMETRY_TAG_MESSAGE_ID, messageID, Constants.TELEMETRY_TAG_LABEL, label));
                    return response;
                } else {
                    return createValidationErrorResponse(
                            validationResults,
                            Constants.TELEMETRY_MARK_MESSAGE,
                            startTime,
                            TelemetryHelper.safeTagMap(
                                    Constants.TELEMETRY_TAG_MESSAGE_ID, messageID,
                                    Constants.TELEMETRY_TAG_LABEL, label));
                }
            }
        } catch (MustAuthorizeException cause) {
            telemetryHelper.recordValidationError(Constants.TELEMETRY_MARK_MESSAGE, startTime, cause.getMessage(),
                    TelemetryHelper.safeTagMap(Constants.TELEMETRY_TAG_MESSAGE_ID, messageID, Constants.TELEMETRY_TAG_LABEL, label));
            throw cause;
        } catch (Exception cause) {
            telemetryHelper.recordError(Constants.TELEMETRY_MARK_MESSAGE, startTime, cause,
                    TelemetryHelper.safeTagMap(Constants.TELEMETRY_TAG_MESSAGE_ID, messageID, Constants.TELEMETRY_TAG_LABEL, label));
            LOGGER.error("Error occurred while marking message: {}", cause.getMessage());
            return ExtensionResponseFactory.create(Map.of("Response", "Error occurred while marking message"));
        }
    }

    @CatalogRequest(
            id = "localDomainRequest_c8485079-5592-4d29-9818-41d99368a35d",
            name = "Reply To Mail",
            description = "Accepts message ID, message, and attachments as input and returns response message. Attachment is optional input.",
            area = "Messaging",
            type = CatalogRequest.Type.CHANGE_SYSTEM)
    @Field(name = "Message", type = "Text", required = false, attributes = {@Attribute(name = "toolTip", value = "'Status message indicating success or failure of the reply operation'")})
    public ExtensionResponse replyToMail(
            @Field(name = "Message ID", type = "Text", attributes = {@Attribute(name = "toolTip", value = "'Unique identifier of the email message to reply to'")}) String messageID,
            @Field(name = "Message", type = "RichText", attributes = {@Attribute(name = "toolTip", value = "'Content of the reply message'")}) String message,
            @Field(name = "Attachments", type = "File", required = false) List<File> attachments,
            @Field(name = "Allow Retry", type = "Switch", required = false, attributes = {@Attribute(name = "toolTip", value = "'Enable retry option for validation errors'")}) Boolean allowRetry) {
        long startTime = System.currentTimeMillis();
        try {
            telemetryHelper.incrementCount(Constants.TELEMETRY_REPLY_TO_MAIL);
            LOGGER.info("Replying to message with ID: {}", messageID);

            boolean shouldAllowRetry = allowRetry != null && allowRetry;

            List<ValidationOrchestrator.ValidationResult> validationResults =
                    validationOrchestrator.validate(Map.of(Validator.ValidationResource.MESSAGE_ID, messageID));

            if (validationResults.isEmpty()) {
                Email email = account.getEmail(messageID);
                if (email == null) {
                    telemetryHelper.recordValidationError(Constants.TELEMETRY_REPLY_TO_MAIL, startTime, INVALID_ID,
                            TelemetryHelper.safeTagMap(Constants.TELEMETRY_TAG_MESSAGE_ID, messageID));
                    return ExtensionResponseFactory.create(Map.of("Message", INVALID_ID));
                }
                try {
                    message = message != null ? message.replace("\n", "<br>") : message;
                    email.replyText(message, CatalogTypes.toAttachments(attachments, kristaMediaClient), null, null, null);
                    telemetryHelper.recordSuccess(Constants.TELEMETRY_REPLY_TO_MAIL, startTime,
                            TelemetryHelper.safeTagMap(Constants.TELEMETRY_TAG_MESSAGE_ID, messageID));
                    return ExtensionResponseFactory.create(Map.of("Message", SUCCESS));
                } catch (IOException cause) {
                    String errorMessage = "Reply to mail failed for ID: " + messageID + " with error message: " + cause.getMessage();
                    telemetryHelper.recordError(Constants.TELEMETRY_REPLY_TO_MAIL, startTime, cause,
                            TelemetryHelper.safeTagMap(Constants.TELEMETRY_TAG_MESSAGE_ID, messageID));
                    LOGGER.error(errorMessage);
                    return ExtensionResponseFactory.create(Map.of("Message", errorMessage));
                }
            } else {
                // Only trigger subcatalog retry flow if allowRetry is true
                if (shouldAllowRetry) {
                    telemetryHelper.recordRetryPrompted(Constants.TELEMETRY_REPLY_TO_MAIL, startTime,
                            TelemetryHelper.safeTagMap(Constants.TELEMETRY_TAG_MESSAGE_ID, messageID,
                                    Constants.TELEMETRY_TAG_VALIDATION_COUNT, String.valueOf(validationResults.size())));

                    String stateId = UUID.randomUUID().toString();
                    internalStateManager.put(stateId, Constants.GSON.toJson(Map.of(
                            GmailResources.MESSAGE_ID, validateString(messageID),
                            GmailResources.MESSAGE, validateString(message),
                            GmailResources.ATTACHMENTS, attachments != null ? attachments : List.of(),
                            SubCatalogConstants.VALIDATION_RESULTS, validationResults)));

                    ExtensionResponse response = responseGenerator.generateConfirmationResponse(
                            ExtensionResponse.Error.ExceptionType.INPUT_ERROR, validationResults,
                            SubCatalogConstants.CONFIRM_REENTER_REPLY_TO_MAIL,
                            Map.of(GmailResources.STATE_ID, stateId,
                                    GmailResources.MESSAGE_ID, validateString(messageID),
                                    GmailResources.MESSAGE, validateString(message),
                                    GmailResources.ATTACHMENTS, attachments != null ? attachments : List.of()));

                    telemetryHelper.recordSuccess(Constants.TELEMETRY_REPLY_TO_MAIL, startTime,
                            TelemetryHelper.safeTagMap(Constants.TELEMETRY_TAG_MESSAGE_ID, messageID));
                    return response;
                } else {
                    return createValidationErrorResponse(
                            validationResults,
                            Constants.TELEMETRY_REPLY_TO_MAIL,
                            startTime,
                            TelemetryHelper.safeTagMap(Constants.TELEMETRY_TAG_MESSAGE_ID, messageID));
                }
            }
        } catch (MustAuthorizeException cause) {
            telemetryHelper.recordValidationError(Constants.TELEMETRY_REPLY_TO_MAIL, startTime, cause.getMessage(),
                    TelemetryHelper.safeTagMap(Constants.TELEMETRY_TAG_MESSAGE_ID, messageID));
            throw cause;
        } catch (Exception cause) {
            telemetryHelper.recordError(Constants.TELEMETRY_REPLY_TO_MAIL, startTime, cause,
                    TelemetryHelper.safeTagMap(Constants.TELEMETRY_TAG_MESSAGE_ID, messageID));
            LOGGER.error("Error occurred while replying to mail: {}", cause.getMessage());
            return ExtensionResponseFactory.create(Map.of("Message", "Error occurred while replying to mail"));
        }
    }

    @CatalogRequest(
            id = "localDomainRequest_82e8a567-a80f-4ff0-876b-8bc14072f322",
            name = "Fetch Mails By Label",
            description = "Accepts label, page number, and page size as input and returns list of mail. Page number, and page size are optional input.",
            area = "Messaging",
            type = CatalogRequest.Type.QUERY_SYSTEM)
    @Field.Desc(name = "Mails", type = "[ Entity(Mail Details) ]", required = false)
    public ExtensionResponse fetchMailsByLabel(
            @Field(name = "Label", type = "Text", attributes = {@Attribute(name = "toolTip", value = "'Name of the label or folder to fetch emails from'")}) String label,
            @Field(name = "Page Number", type = "Number", required = false, attributes = {@Attribute(name = "toolTip", value = "'Page number for pagination (1-15, default: 1)'")}) Double pageNumber,
            @Field(name = "Page Size", type = "Number", required = false, attributes = {@Attribute(name = "toolTip", value = "'Number of emails per page (1-15, default: 15)'")}) Double pageSize,
            @Field(name = "Allow Retry", type = "Switch", required = false, attributes = {@Attribute(name = "toolTip", value = "'Enable retry option for validation errors'")}) Boolean allowRetry) {
        long startTime = System.currentTimeMillis();
        LOGGER.info("fetchMailsByLabel: label: {}, pageNumber: {}, pageSize: {}", label, pageNumber, pageSize);

        try {
            telemetryHelper.incrementCount("gmail.fetchMailsByLabel");

            boolean shouldAllowRetry = allowRetry != null && allowRetry;

            Map<Validator.ValidationResource, String> validationMap = ValidationResourceUtil.prepareValidateLabelMap(label, pageNumber, pageSize);
            List<ValidationOrchestrator.ValidationResult> validationResults = validationOrchestrator.validate(validationMap);

            if (validationResults.isEmpty()) {
                telemetryHelper.recordSuccess("gmail.fetchMailsByLabel", startTime, Map.of("label", label, "page_number", String.valueOf(pageNumber), "page_size", String.valueOf(pageSize)
                ));
                return fetchMailsByLabelResponse(label, pageNumber, pageSize);
            }

            // Only trigger subcatalog retry flow if allowRetry is true
            if (shouldAllowRetry) {
                telemetryHelper.recordRetryPrompted("gmail.fetchMailsByLabel", startTime, TelemetryHelper.safeTagMap(
                        "label", label,
                        "page_number", String.valueOf(pageNumber),
                        "page_size", String.valueOf(pageSize),
                        "validation_count", String.valueOf(validationResults.size())
                ));
                String stateId = UUID.randomUUID().toString();
                Map<String, Object> stateMap = new java.util.HashMap<>(4);
                stateMap.put(GmailResources.LABEL, validateString(label));
                stateMap.put(GmailResources.PAGE_NUMBER, pageNumber);
                stateMap.put(GmailResources.PAGE_SIZE, pageSize);
                stateMap.put(SubCatalogConstants.VALIDATION_RESULTS, validationResults);
                internalStateManager.put(stateId, Constants.GSON.toJson(stateMap));
                Map<String, Object> confirmationMap = new java.util.HashMap<>(4);
                confirmationMap.put(GmailResources.STATE_ID, stateId);
                confirmationMap.put(GmailResources.LABEL, validateString(label));
                confirmationMap.put(GmailResources.PAGE_NUMBER, pageNumber);
                confirmationMap.put(GmailResources.PAGE_SIZE, pageSize);
                return responseGenerator.generateConfirmationResponse(
                        ExtensionResponse.Error.ExceptionType.INPUT_ERROR, validationResults,
                        SubCatalogConstants.CONFIRM_REENTER_FETCH_MAIL_BY_LABEL,
                        confirmationMap);
            } else {
                return createValidationErrorResponse(
                        validationResults,
                        "gmail.fetchMailsByLabel",
                        startTime,
                        TelemetryHelper.safeTagMap(
                                "label", label,
                                "page_number", String.valueOf(pageNumber),
                                "page_size", String.valueOf(pageSize)));
            }

        } catch (MustAuthorizeException cause) {
            telemetryHelper.recordValidationError("gmail.fetchMailsByLabel", startTime, cause.getMessage(), TelemetryHelper.safeTagMap(
                    "label", label,
                    "page_number", String.valueOf(pageNumber),
                    "page_size", String.valueOf(pageSize)
            ));
            throw cause;
        } catch (Exception cause) {
            LOGGER.error("Error occurred while Fetch Mails By Label: {}", cause);
            telemetryHelper.recordError("gmail.fetchMailsByLabel", startTime, cause, TelemetryHelper.safeTagMap(
                    "label", label,
                    "page_number", String.valueOf(pageNumber),
                    "page_size", String.valueOf(pageSize)
            ));
            return ExtensionResponseFactory.create("Error occurred while fetching mails by label", ExtensionResponse.Error.ExceptionType.SYSTEM_ERROR,
                    List.of(RemediationActionFactory.createInformActionALLParticipants("Error occurred while fetching mails by label", List.of())),
                    null, null);
        }
    }

    private ExtensionResponse fetchMailsByLabelResponse(String label, Double pageNumber, Double pageSize) {
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
            LOGGER.error("Fetch mails request failed. Given folder does not exist. {}", cause.getMessage());
            return ExtensionResponseFactory.create(Map.of("Mails", List.of()));
        }
    }

    @CatalogRequest(
            id = "localDomainRequest_90b24da6-d02f-4fcb-9632-ef8e6ae1550a",
            name = "Fetch Latest Mail",
            description = "Returns the latest email received from the inbox",
            area = "Messaging",
            type = CatalogRequest.Type.QUERY_SYSTEM)
    @Field.Desc(name = "New Email", type = "Entity(Mail Details)", required = false)
    public ExtensionResponse fetchLatestMail() {
        long startTime = System.currentTimeMillis();

        try {
            telemetryHelper.incrementCount("gmail.fetchLatestMail");
            LOGGER.info("fetchLatestMail: start");

            List<Email> emails = account.getInboxFolder().getEmails(1.0, 1.0);

            if (emails != null && !emails.isEmpty()) {
                Email latestEmail = emails.getFirst();
                MailDetails mailDetails = CatalogTypes.fromEmail(latestEmail, kristaMediaClient);
                LOGGER.info("fetchLatestMail: Found latest email with ID: {}", mailDetails.messageID);
                telemetryHelper.recordSuccess("gmail.fetchLatestMail", startTime, Map.of());
                return ExtensionResponseFactory.create(Map.of("New Email", mailDetails));
            } else {
                LOGGER.info("fetchLatestMail: No emails found in inbox");
                telemetryHelper.recordSuccess("gmail.fetchLatestMail", startTime, Map.of());
                return ExtensionResponseFactory.create(Map.of("New Email", null));
            }
        } catch (MustAuthorizeException cause) {
            telemetryHelper.recordValidationError("gmail.fetchLatestMail", startTime, cause.getMessage(), Map.of());
            throw cause;
        } catch (Exception cause) {
            telemetryHelper.recordError("gmail.fetchLatestMail", startTime, cause, Map.of());
            LOGGER.error("Error occurred while Fetch Latest Mail: {}", cause.getMessage(), cause);
            return ExtensionResponseFactory.create("Error occurred while Fetch Latest Mail",
                    ExtensionResponse.Error.ExceptionType.SYSTEM_ERROR,
                    List.of(RemediationActionFactory.createInformActionALLParticipants("Error occurred while Fetch Latest Mail", List.of())),
                    null, null);
        }
    }

    @CatalogRequest(
            id = "localDomainRequest_a807f616-1018-4400-9fd6-397f8415447c",
            name = "Get Latest Mail",
            description = "Get Latest Mail",
            area = "Messaging",
            type = CatalogRequest.Type.WAIT_FOR_EVENT)
    @Field.Desc(name = "Data", type = "{ History ID: Text, Expiration: Number }", required = false)
    public Map<String, Object> getLatestMail(
            @Field(name = "eventName", type = "Text") String eventName,
            @Field(name = "eventData", type = "FreeForm") FreeForm eventData) {

        if (eventName.equals(Constants.GMAIL_UPDATE)) {
            String s = (String) eventData.get(Constants.NEW_EMAIL_UPDATE);
            return Map.of("History ID ", s, "Expiration", 0);
        }
        return Map.of();

    }

    @CatalogRequest(
            id = "localDomainRequest_15ff39ae-7c21-4535-910b-c27da78e752f",
            name = "Trigger When New Email Arrived",
            description = "Request will call krista when any new email will arrived.",
            area = "Messaging",
            type = CatalogRequest.Type.WAIT_FOR_EVENT)
    @Field.Desc(name = "All Mails", type = "[ Entity(Mail Details) ]", required = false)
    public List<MailDetails> triggerWhenNewEmailArrived(
            @Field(name = "eventName", type = "Text") String eventName,
            @Field(name = "eventData", type = "FreeForm") FreeForm eventData) {

        if (eventName.equals(Constants.GMAIL_UPDATE)) {
            List<MailDetails> allNewMails = gmailNotificationChannel.getAllNewMails();
            if (allNewMails.isEmpty()) {
                throw new IllegalArgumentException("There is no new message at this point");
            }
            return allNewMails;
        }
        throw new IllegalArgumentException("Message id could be invalid");
    }

    @CatalogRequest(
            id = "localDomainRequest_61c0d792-e136-43fe-878a-4a61db40d5d8",
            name = "Reply To All With CC and BCC",
            description = "In this request, the user can respond to everyone on the thread. Other recipients will see a message user 'Reply All' to, whether they're in the 'To' or 'Cc' fields. 'To', Cc' and 'Bcc' fields are optional to update the existing users.",
            area = "Messaging",
            type = CatalogRequest.Type.CHANGE_SYSTEM)
    @Field.Boolean(name = "Is Successful", required = false, attributes = {@Attribute(name = "visualWidth", value = "S"), @Attribute(name = "toolTip", value = "'Indicates whether the reply to all operation was successful'")})
    public ExtensionResponse replyToAllWithCCAndBCC(
            @Field.Text(name = "Message Id", attributes = {@Attribute(name = "visualWidth", value = "S"), @Attribute(name = "toolTip", value = "'Unique identifier of the email message to reply to all recipients'")}) String messageId,
            @Field.Text(name = "To", required = false, attributes = {@Attribute(name = "visualWidth", value = "S"), @Attribute(name = "toolTip", value = "'Additional recipients to include in the To field (comma-separated)'")}) String to,
            @Field.Text(name = "Cc", required = false, attributes = {@Attribute(name = "visualWidth", value = "S"), @Attribute(name = "toolTip", value = "'Additional recipients to include in the Cc field (comma-separated)'")}) String cc,
            @Field.Text(name = "Bcc", required = false, attributes = {@Attribute(name = "visualWidth", value = "S"), @Attribute(name = "toolTip", value = "'Additional recipients to include in the Bcc field (comma-separated)'")}) String bcc,
            @Field(name = "Message", type = "RichText", required = false, attributes = {@Attribute(name = "visualWidth", value = "L"), @Attribute(name = "toolTip", value = "'Content of the reply message that will be sent to all recipients'")}) String message,
            @Field.File(name = "Attachments", multipleFileUpload = true, required = false, attributes = {@Attribute(name = "visualWidth", value = "S")}) List<File> attachments,
            @Field.Text(name = "Reply To", required = false, attributes = {@Attribute(name = "visualWidth", value = "S"), @Attribute(name = "toolTip", value = "'Email address to set as the reply-to address'")}) String replyTo,
            @Field(name = "Allow Retry", type = "Switch", required = false, attributes = {@Attribute(name = "toolTip", value = "'Enable retry option for validation errors'")}) Boolean allowRetry) {
        long startTime = System.currentTimeMillis();
        try {
            telemetryHelper.incrementCount("gmail.replyToAllWithCCAndBCC");
            LOGGER.info("replyToAllWithCCAndBCC: messageId: {}; message: {}", messageId, message);

            // Default allowRetry to false if not provided
            boolean shouldAllowRetry = allowRetry != null && allowRetry;

            List<ValidationOrchestrator.ValidationResult> validationResults =
                    validationOrchestrator.validate(Map.of(
                            Validator.ValidationResource.MESSAGE_ID, messageId,
                            Validator.ValidationResource.TO, validateString(to),
                            Validator.ValidationResource.CC, validateString(cc),
                            Validator.ValidationResource.BCC, validateString(bcc),
                            Validator.ValidationResource.REPLY_TO, validateString(replyTo)));

            if (validationResults.isEmpty()) {
                Email email = account.getEmail(messageId);
                if (email == null) {
                    telemetryHelper.recordValidationError("gmail.replyToAllWithCCAndBCC", startTime, INVALID_ID,
                            TelemetryHelper.safeTagMap("message_id", messageId, "to", validateString(to),
                                    "cc", validateString(cc), "bcc", validateString(bcc)));
                    return ExtensionResponseFactory.create(Map.of("Is Successful", false));
                }
                try {
                    message = message != null ? message.replace("\n", "<br>") : message;
                    email.replyToAll(message, CatalogTypes.toAttachments(attachments, kristaMediaClient), cc, bcc, to);
                    telemetryHelper.recordSuccess("gmail.replyToAllWithCCAndBCC", startTime,
                            TelemetryHelper.safeTagMap("message_id", messageId, "to", validateString(to),
                                    "cc", validateString(cc), "bcc", validateString(bcc)));
                    return ExtensionResponseFactory.create(Map.of("Is Successful", true));
                } catch (MessagingException | IOException cause) {
                    String errorMessage = "Error occurred while processing attachment for message ID: " + messageId;
                    LOGGER.error(errorMessage, cause);
                    telemetryHelper.recordError("gmail.replyToAllWithCCAndBCC", startTime, cause,
                            TelemetryHelper.safeTagMap("message_id", messageId, "to", validateString(to),
                                    "cc", validateString(cc), "bcc", validateString(bcc)));
                    return ExtensionResponseFactory.create(Map.of("Is Successful", false));
                }
            } else {
                // Only trigger subcatalog retry flow if allowRetry is true
                if (shouldAllowRetry) {
                    telemetryHelper.recordRetryPrompted("gmail.replyToAllWithCCAndBCC", startTime,
                            TelemetryHelper.safeTagMap("message_id", messageId, "to", validateString(to),
                                    "cc", validateString(cc), "bcc", validateString(bcc),
                                    "validation_count", String.valueOf(validationResults.size())));

                    String stateId = UUID.randomUUID().toString();
                    internalStateManager.put(stateId, Constants.GSON.toJson(Map.of(
                            GmailResources.MESSAGE_ID, messageId,
                            GmailResources.TO, validateString(to),
                            GmailResources.CC, validateString(cc),
                            GmailResources.BCC, validateString(bcc),
                            GmailResources.MESSAGE, validateString(message),
                            GmailResources.ATTACHMENTS, attachments != null ? attachments : List.of(),
                            GmailResources.REPLY_TO, validateString(replyTo),
                            SubCatalogConstants.VALIDATION_RESULTS, validationResults)));

                    return responseGenerator.generateConfirmationResponse(
                            ExtensionResponse.Error.ExceptionType.INPUT_ERROR, validationResults,
                            SubCatalogConstants.CONFIRM_REENTER_REPLY_TO_ALL_WITH_FIELDS,
                            Map.of(GmailResources.STATE_ID, stateId,
                                    GmailResources.MESSAGE_ID, messageId,
                                    GmailResources.TO, validateString(to),
                                    GmailResources.CC, validateString(cc),
                                    GmailResources.BCC, validateString(bcc),
                                    GmailResources.MESSAGE, validateString(message),
                                    GmailResources.ATTACHMENTS, attachments != null ? attachments : List.of(),
                                    GmailResources.REPLY_TO, validateString(replyTo)));
                } else {
                    // Return validation error directly without retry option
                    StringBuilder errorMessage = new StringBuilder();
                    for (ValidationOrchestrator.ValidationResult result : validationResults) {
                        errorMessage.append(result.getErrMessage()).append(" ");
                    }
                    telemetryHelper.recordValidationError("gmail.replyToAllWithCCAndBCC", startTime, errorMessage.toString().trim(),
                            TelemetryHelper.safeTagMap("message_id", messageId, "to", validateString(to),
                                    "cc", validateString(cc), "bcc", validateString(bcc)));
                    return ExtensionResponseFactory.create(new Exception(errorMessage.toString().trim()),
                            errorMessage.toString().trim(),
                            ExtensionResponse.Error.ExceptionType.INPUT_ERROR);
                }
            }
        } catch (MustAuthorizeException cause) {
            telemetryHelper.recordValidationError("gmail.replyToAllWithCCAndBCC", startTime, cause.getMessage(),
                    TelemetryHelper.safeTagMap("message_id", messageId, "to", validateString(to),
                            "cc", validateString(cc), "bcc", validateString(bcc)));
            throw cause;
        } catch (Exception cause) {
            telemetryHelper.recordError("gmail.replyToAllWithCCAndBCC", startTime, cause,
                    TelemetryHelper.safeTagMap("message_id", messageId, "to", validateString(to),
                            "cc", validateString(cc), "bcc", validateString(bcc)));
            LOGGER.error("Error occurred while Reply To All With CC and BCC: {}", cause);
            return ExtensionResponseFactory.create(Map.of("Is Successful", false));
        }
    }

    @CatalogRequest(
            id = "localDomainRequest_4ca7f80e-f8b9-4bec-9bdf-b7561b21c10d",
            name = "Reply To Mail With CC and BCC",
            description = "Accepts message ID, message, attachments, cc, bcc and reply to as inputs and returns response message.",
            area = "Messaging",
            type = CatalogRequest.Type.CHANGE_SYSTEM)
    @Field.Text(name = "Message", required = false, attributes = {@Attribute(name = "visualWidth", value = "S"), @Attribute(name = "toolTip", value = "'Status message indicating success or failure of the reply operation'")})
    public ExtensionResponse replyToMailWithCCAndBCC(
            @Field.Text(name = "Message Id", attributes = {@Attribute(name = "visualWidth", value = "S"), @Attribute(name = "toolTip", value = "'Unique identifier of the email message to reply to'")}) String messageId,
            @Field(name = "Message", type = "RichText", required = false, attributes = {@Attribute(name = "visualWidth", value = "L"), @Attribute(name = "toolTip", value = "'Content of the reply message'")}) String message,
            @Field.File(name = "Attachments", multipleFileUpload = true, required = false, attributes = {@Attribute(name = "visualWidth", value = "S")}) List<File> attachments,
            @Field.Text(name = "To", required = false, attributes = {@Attribute(name = "visualWidth", value = "S"), @Attribute(name = "toolTip", value = "'Additional recipients to include in the To field (comma-separated)'")}) String to,
            @Field.Text(name = "Cc", required = false, attributes = {@Attribute(name = "visualWidth", value = "S"), @Attribute(name = "toolTip", value = "'Additional recipients to include in the Cc field (comma-separated)'")}) String cc,
            @Field.Text(name = "Bcc", required = false, attributes = {@Attribute(name = "visualWidth", value = "S"), @Attribute(name = "toolTip", value = "'Additional recipients to include in the Bcc field (comma-separated)'")}) String bcc,
            @Field.Text(name = "Reply To", required = false, attributes = {@Attribute(name = "visualWidth", value = "S"), @Attribute(name = "toolTip", value = "'Email address to set as the reply-to address'")}) String replyTo,
            @Field(name = "Allow Retry", type = "Switch", required = false, attributes = {@Attribute(name = "toolTip", value = "'Enable retry option for validation errors'")}) Boolean allowRetry) {
        long startTime = System.currentTimeMillis();
        try {
            telemetryHelper.incrementCount(Constants.TELEMETRY_REPLY_TO_MAIL_WITH_CC_AND_BCC);
            LOGGER.info("replyToMailWithCCAndBCC: messageId: {}; message: {}", messageId, message);

            // Default allowRetry to false if not provided
            boolean shouldAllowRetry = allowRetry != null && allowRetry;

            List<ValidationOrchestrator.ValidationResult> validationResults =
                    validationOrchestrator.validate(Map.of(Validator.ValidationResource.MESSAGE_ID, messageId, Validator.ValidationResource.TO, validateString(to), Validator.ValidationResource.CC, validateString(cc), Validator.ValidationResource.BCC, validateString(bcc), Validator.ValidationResource.REPLY_TO, validateString(replyTo)));

            if (validationResults.isEmpty()) {
                Email email = account.getEmail(messageId);
                if (email == null) {
                    telemetryHelper.recordValidationError(Constants.TELEMETRY_REPLY_TO_MAIL_WITH_CC_AND_BCC, startTime, INVALID_ID,
                            TelemetryHelper.safeTagMap(Constants.TELEMETRY_TAG_MESSAGE_ID, messageId, Constants.TELEMETRY_TAG_TO, validateString(to),
                                    Constants.TELEMETRY_TAG_CC, validateString(cc), Constants.TELEMETRY_TAG_BCC, validateString(bcc)));
                    return ExtensionResponseFactory.create(Map.of("Message", INVALID_ID));
                }
                try {
                    message = message != null ? message.replace("\n", "<br>") : message;
                    email.replyText(message, CatalogTypes.toAttachments(attachments, kristaMediaClient), cc, bcc, to);
                    telemetryHelper.recordSuccess(Constants.TELEMETRY_REPLY_TO_MAIL_WITH_CC_AND_BCC, startTime,
                            TelemetryHelper.safeTagMap(Constants.TELEMETRY_TAG_MESSAGE_ID, messageId, Constants.TELEMETRY_TAG_TO, validateString(to),
                                    Constants.TELEMETRY_TAG_CC, validateString(cc), Constants.TELEMETRY_TAG_BCC, validateString(bcc)));
                    return ExtensionResponseFactory.create(Map.of("Message", SUCCESS));
                } catch (IOException cause) {
                    String errorMessage = "Reply to mail failed for ID: " + messageId + " with error message: " + cause.getMessage();
                    LOGGER.error(errorMessage);
                    telemetryHelper.recordError(Constants.TELEMETRY_REPLY_TO_MAIL_WITH_CC_AND_BCC, startTime, cause,
                            TelemetryHelper.safeTagMap(Constants.TELEMETRY_TAG_MESSAGE_ID, messageId, Constants.TELEMETRY_TAG_TO, validateString(to),
                                    Constants.TELEMETRY_TAG_CC, validateString(cc), Constants.TELEMETRY_TAG_BCC, validateString(bcc)));
                    return ExtensionResponseFactory.create(Map.of("Message", errorMessage));
                }
            } else {
                // Only trigger subcatalog retry flow if allowRetry is true
                if (shouldAllowRetry) {
                    telemetryHelper.recordRetryPrompted(Constants.TELEMETRY_REPLY_TO_MAIL_WITH_CC_AND_BCC, startTime,
                            TelemetryHelper.safeTagMap(Constants.TELEMETRY_TAG_MESSAGE_ID, messageId, Constants.TELEMETRY_TAG_TO, validateString(to),
                                    Constants.TELEMETRY_TAG_CC, validateString(cc), Constants.TELEMETRY_TAG_BCC, validateString(bcc),
                                    Constants.TELEMETRY_TAG_VALIDATION_COUNT, String.valueOf(validationResults.size())));

                    String stateId = UUID.randomUUID().toString();
                    internalStateManager.put(stateId, Constants.GSON.toJson(Map.of(GmailResources.MESSAGE_ID, messageId, GmailResources.TO, validateString(to), GmailResources.CC, validateString(cc), GmailResources.BCC, validateString(bcc), GmailResources.MESSAGE, validateString(message), GmailResources.ATTACHMENTS, attachments != null ? attachments : List.of(), GmailResources.REPLY_TO, validateString(replyTo), SubCatalogConstants.VALIDATION_RESULTS, validationResults)));

                    return responseGenerator.generateConfirmationResponse(
                            ExtensionResponse.Error.ExceptionType.INPUT_ERROR, validationResults,
                            SubCatalogConstants.CONFIRM_REENTER_REPLY_TO_MAIL_WITH_FIELDS,
                            Map.of(GmailResources.STATE_ID, stateId,
                                    GmailResources.MESSAGE_ID, messageId,
                                    GmailResources.TO, validateString(to),
                                    GmailResources.CC, validateString(cc),
                                    GmailResources.BCC, validateString(bcc),
                                    GmailResources.MESSAGE, validateString(message),
                                    GmailResources.ATTACHMENTS, attachments != null ? attachments : List.of(),
                                    GmailResources.REPLY_TO, validateString(replyTo)));
                } else {
                    // Return validation error directly without retry option
                    StringBuilder errorMessage = new StringBuilder();
                    for (ValidationOrchestrator.ValidationResult result : validationResults) {
                        errorMessage.append(result.getErrMessage()).append(" ");
                    }
                    telemetryHelper.recordValidationError(Constants.TELEMETRY_REPLY_TO_MAIL_WITH_CC_AND_BCC, startTime, errorMessage.toString().trim(),
                            TelemetryHelper.safeTagMap(Constants.TELEMETRY_TAG_MESSAGE_ID, messageId, Constants.TELEMETRY_TAG_TO, validateString(to),
                                    Constants.TELEMETRY_TAG_CC, validateString(cc), Constants.TELEMETRY_TAG_BCC, validateString(bcc)));
                    return ExtensionResponseFactory.create(new Exception(errorMessage.toString().trim()),
                            errorMessage.toString().trim(),
                            ExtensionResponse.Error.ExceptionType.INPUT_ERROR);
                }
            }
        } catch (MustAuthorizeException cause) {
            telemetryHelper.recordValidationError(Constants.TELEMETRY_REPLY_TO_MAIL_WITH_CC_AND_BCC, startTime, cause.getMessage(),
                    TelemetryHelper.safeTagMap(Constants.TELEMETRY_TAG_MESSAGE_ID, messageId, Constants.TELEMETRY_TAG_TO, validateString(to),
                            Constants.TELEMETRY_TAG_CC, validateString(cc), Constants.TELEMETRY_TAG_BCC, validateString(bcc)));
            throw cause;
        } catch (Exception cause) {
            telemetryHelper.recordError(Constants.TELEMETRY_REPLY_TO_MAIL_WITH_CC_AND_BCC, startTime, cause,
                    TelemetryHelper.safeTagMap(Constants.TELEMETRY_TAG_MESSAGE_ID, messageId, Constants.TELEMETRY_TAG_TO, validateString(to),
                            Constants.TELEMETRY_TAG_CC, validateString(cc), Constants.TELEMETRY_TAG_BCC, validateString(bcc)));
            LOGGER.error("Error occurred while Reply To Mail With CC and BCC: {}", cause.getMessage());
            return ExtensionResponseFactory.create(Map.of("Message", "Failed to reply to mail"));
        }
    }

    @CatalogRequest(
            id = "localDomainRequest_0dcf706d-52ef-455d-9d70-b04575ab2b42",
            name = "Renew Subscription",
            description = "System trigger event used to update subscription in specified intervals",
            area = "Messaging",
            type = CatalogRequest.Type.CHANGE_SYSTEM)
    @Field.Boolean(name = "Success", required = false, attributes = {@Attribute(name = "visualWidth", value = "S")})
    public ExtensionResponse renewSubscription() {
        long startTime = System.currentTimeMillis();
        try {
            telemetryHelper.incrementCount(Constants.TELEMETRY_RENEW_SUBSCRIPTION);
            LOGGER.info("Subscription Renewal Started");
            currentHistoryIdFromValidate(gmailNotificationChannel.initiate(gmailAttributes));
            LOGGER.info("Subscription Renewal Completed");
            telemetryHelper.recordSuccess(Constants.TELEMETRY_RENEW_SUBSCRIPTION, startTime, Map.of());
            return ExtensionResponseFactory.create(Map.of("Success", true));
        } catch (MustAuthorizeException cause) {
            telemetryHelper.recordValidationError(Constants.TELEMETRY_RENEW_SUBSCRIPTION, startTime, cause.getMessage(), Map.of());
            throw cause;
        } catch (Exception cause) {
            LOGGER.error("Failed Renew Watch Request : {} {}", cause, LocalDateTime.now());
            telemetryHelper.recordError(Constants.TELEMETRY_RENEW_SUBSCRIPTION, startTime, cause, Map.of());
            return ExtensionResponseFactory.create("Error occurred while Renew Subscription", ExtensionResponse.Error.ExceptionType.SYSTEM_ERROR,
                    List.of(RemediationActionFactory.createInformActionALLParticipants("Error occurred while Renew Subscription", List.of())),
                    null, null);
        }
    }

    private void currentHistoryIdFromValidate(WatchResponse gmailNotificationChannel) {
        LOGGER.info("After renewing subscription History ID : {}", gmailNotificationChannel.getHistoryId());
        LOGGER.info("After renewing subscription Expiration Date : {}", gmailNotificationChannel.getExpiration());
        historyIdStore.put(TO_BE_USED_HISTORY_ID, String.valueOf(gmailNotificationChannel.getHistoryId()));
    }

}
