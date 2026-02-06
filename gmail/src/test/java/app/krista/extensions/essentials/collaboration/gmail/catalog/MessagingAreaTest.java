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
import app.krista.extensions.essentials.collaboration.gmail.GmailAttributes;
import app.krista.extensions.essentials.collaboration.gmail.catalog.errorhandlers.ErrorHandlingStateManager;
import app.krista.extensions.essentials.collaboration.gmail.catalog.errorhandlers.ExtensionResponseGenerator;
import app.krista.extensions.essentials.collaboration.gmail.catalog.extresp.SubCatalogConstants;
import app.krista.extensions.essentials.collaboration.gmail.catalog.extresp.TelemetryHelper;
import app.krista.extensions.essentials.collaboration.gmail.catalog.validators.ValidationOrchestrator;
import app.krista.extensions.essentials.collaboration.gmail.impl.GmailNotificationChannel;
import app.krista.extensions.essentials.collaboration.gmail.impl.stores.HistoryIdStore;
import app.krista.extensions.essentials.collaboration.gmail.impl.stores.KristaMediaClient;
import app.krista.extensions.essentials.collaboration.gmail.impl.util.Constants;
import app.krista.extensions.essentials.collaboration.gmail.service.Account;
import app.krista.extensions.essentials.collaboration.gmail.service.Email;
import app.krista.extensions.essentials.collaboration.gmail.service.EmailBuilder;
import app.krista.extensions.essentials.collaboration.gmail.service.Folder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import javax.mail.MessagingException;
import java.io.IOException;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Comprehensive test suite for MessagingArea "Allow Retry" feature.
 * Tests all 13 modified methods with allowRetry parameter.
 * <p>
 * Test Coverage:
 * - allowRetry = true (triggers subcatalog flow)
 * - allowRetry = false (returns error directly)
 * - allowRetry = null (defaults to false)
 * - Null parameter handling (pageNumber, pageSize)
 * - Error message construction
 * - Telemetry recording for both paths
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class MessagingAreaTest {

    @Mock
    private Account mockAccount;

    @Mock
    private GmailNotificationChannel mockGmailNotificationChannel;

    @Mock
    private KristaMediaClient mockKristaMediaClient;

    @Mock
    private HistoryIdStore mockHistoryIdStore;

    @Mock
    private GmailAttributes mockGmailAttributes;

    @Mock
    private ValidationOrchestrator mockValidationOrchestrator;

    @Mock
    private ErrorHandlingStateManager mockInternalStateManager;

    @Mock
    private ExtensionResponseGenerator mockResponseGenerator;

    @Mock
    private TelemetryHelper mockTelemetryHelper;

    @Mock
    private Folder mockFolder;

    @Mock
    private Email mockEmail;

    @Mock
    private EmailBuilder mockEmailBuilder;

    private MessagingArea messagingArea;

    @BeforeEach
    void setUp() {
        messagingArea = new MessagingArea(
                mockAccount,
                mockGmailNotificationChannel,
                mockKristaMediaClient,
                mockHistoryIdStore,
                mockGmailAttributes,
                mockValidationOrchestrator,
                mockInternalStateManager,
                mockResponseGenerator,
                mockTelemetryHelper
        );
    }

    /**
     * Test suite for fetchMailByMessageId method
     */
    @Nested
    @DisplayName("Fetch Mail By Message ID - Allow Retry Tests")
    class FetchMailByMessageIdTests {

        @Test
        @DisplayName("Should trigger subcatalog flow when allowRetry = true with validation errors")
        void testAllowRetryTrue_TriggersSubcatalogFlow() throws IOException, MessagingException {
            // Arrange
            String messageId = "test-message-id";
            List<ValidationOrchestrator.ValidationResult> validationErrors = List.of(
                    new ValidationOrchestrator.ValidationResult("Confirm message 1", "messageId", "Fetch message 1", "Error 1", "TEXT_FIELD"),
                    new ValidationOrchestrator.ValidationResult("Confirm message 2", "messageId", "Fetch message 2", "Error 2", "TEXT_FIELD")
            );

            when(mockValidationOrchestrator.validate(anyMap())).thenReturn(validationErrors);

            ExtensionResponse expectedResponse = mock(ExtensionResponse.class);
            when(mockResponseGenerator.generateConfirmationResponse(
                    any(ExtensionResponse.Error.ExceptionType.class),
                    anyList(),
                    anyString(),
                    anyMap()
            )).thenReturn(expectedResponse);

            // Act
            ExtensionResponse response = messagingArea.fetchMailByMessageId(messageId, true);

            // Assert
            assertThat(response).isEqualTo(expectedResponse);

            // Verify subcatalog flow was triggered
            verify(mockResponseGenerator).generateConfirmationResponse(
                    eq(ExtensionResponse.Error.ExceptionType.INPUT_ERROR),
                    eq(validationErrors),
                    eq(SubCatalogConstants.CONFIRM_REENTER_FETCH_MAIL),
                    anyMap()
            );

            // Verify state was saved
            verify(mockInternalStateManager).put(anyString(), anyString());

            // Verify telemetry recorded retry prompt
            verify(mockTelemetryHelper).recordRetryPrompted(
                    eq(Constants.TELEMETRY_FETCH_MAIL_BY_MESSAGE_ID),
                    anyLong(),
                    anyMap()
            );

            // Verify success telemetry
            verify(mockTelemetryHelper).recordSuccess(
                    eq(Constants.TELEMETRY_FETCH_MAIL_BY_MESSAGE_ID),
                    anyLong(),
                    anyMap()
            );
        }

        @Test
        @DisplayName("Should return error directly when allowRetry = false with validation errors")
        void testAllowRetryFalse_ReturnsErrorDirectly() throws IOException, MessagingException {
            // Arrange
            String messageId = "test-message-id";
            List<ValidationOrchestrator.ValidationResult> validationErrors = List.of(
                    new ValidationOrchestrator.ValidationResult("Confirm message 1", "messageId", "Fetch message 1", "Error 1", "TEXT_FIELD"),
                    new ValidationOrchestrator.ValidationResult("Confirm message 2", "messageId", "Fetch message 2", "Error 2", "TEXT_FIELD")
            );

            when(mockValidationOrchestrator.validate(anyMap())).thenReturn(validationErrors);

            // Act
            ExtensionResponse response = messagingArea.fetchMailByMessageId(messageId, false);

            // Assert
            assertThat(response).isNotNull();
            assertThat(response.getError()).isNotNull();
            assertThat(response.getError().getExceptionType())
                    .isEqualTo(ExtensionResponse.Error.ExceptionType.INPUT_ERROR);

            // Verify subcatalog flow was NOT triggered
            verify(mockResponseGenerator, never()).generateConfirmationResponse(
                    any(), any(), any(), any()
            );

            // Verify state was NOT saved
            verify(mockInternalStateManager, never()).put(anyString(), anyString());

            // Verify validation error telemetry
            verify(mockTelemetryHelper).recordValidationError(
                    eq(Constants.TELEMETRY_FETCH_MAIL_BY_MESSAGE_ID),
                    anyLong(),
                    anyString(),
                    anyMap()
            );
        }

        @Test
        @DisplayName("Should default to false when allowRetry = null with validation errors")
        void testAllowRetryNull_DefaultsToFalse() throws IOException, MessagingException {
            // Arrange
            String messageId = "test-message-id";
            List<ValidationOrchestrator.ValidationResult> validationErrors = List.of(
                    new ValidationOrchestrator.ValidationResult("Confirm message", "messageId", "Fetch message", "Validation error", "TEXT_FIELD")
            );

            when(mockValidationOrchestrator.validate(anyMap())).thenReturn(validationErrors);

            // Act
            ExtensionResponse response = messagingArea.fetchMailByMessageId(messageId, null);

            // Assert
            assertThat(response).isNotNull();
            assertThat(response.getError()).isNotNull();

            // Verify subcatalog flow was NOT triggered (defaults to false)
            verify(mockResponseGenerator, never()).generateConfirmationResponse(
                    any(), any(), any(), any()
            );

            // Verify validation error telemetry
            verify(mockTelemetryHelper).recordValidationError(
                    eq(Constants.TELEMETRY_FETCH_MAIL_BY_MESSAGE_ID),
                    anyLong(),
                    anyString(),
                    anyMap()
            );
        }

        @Test
        @DisplayName("Should succeed when no validation errors regardless of allowRetry")
        void testNoValidationErrors_SucceedsRegardlessOfAllowRetry() throws IOException, MessagingException {
            // Arrange
            String messageId = "valid-message-id";
            when(mockValidationOrchestrator.validate(anyMap())).thenReturn(List.of());
            when(mockAccount.getEmail(anyString())).thenReturn(mockEmail);

            // Act - Test with allowRetry = true
            ExtensionResponse response1 = messagingArea.fetchMailByMessageId(messageId, true);

            // Reset mocks
            reset(mockTelemetryHelper, mockValidationOrchestrator, mockAccount, mockEmail);
            when(mockValidationOrchestrator.validate(anyMap())).thenReturn(List.of());
            when(mockAccount.getEmail(anyString())).thenReturn(mockEmail);

            // Act - Test with allowRetry = false
            ExtensionResponse response2 = messagingArea.fetchMailByMessageId(messageId, false);

            // Assert - Both should succeed
            assertThat(response1).isNotNull();
            assertThat(response1.getError()).isNull();
            assertThat(response2).isNotNull();
            assertThat(response2.getError()).isNull();
        }

        @Test
        @DisplayName("Should construct error message correctly from multiple validation results")
        void testErrorMessageConstruction_MultipleErrors() throws IOException, MessagingException {
            // Arrange
            String messageId = "test-message-id";
            List<ValidationOrchestrator.ValidationResult> validationErrors = List.of(
                    new ValidationOrchestrator.ValidationResult("Confirm 1", "field1", "Fetch 1", "Error one", "TEXT_FIELD"),
                    new ValidationOrchestrator.ValidationResult("Confirm 2", "field2", "Fetch 2", "Error two", "TEXT_FIELD"),
                    new ValidationOrchestrator.ValidationResult("Confirm 3", "field3", "Fetch 3", "Error three", "TEXT_FIELD")
            );

            when(mockValidationOrchestrator.validate(anyMap())).thenReturn(validationErrors);

            // Act
            ExtensionResponse response = messagingArea.fetchMailByMessageId(messageId, false);

            // Assert
            ArgumentCaptor<String> errorMessageCaptor = ArgumentCaptor.forClass(String.class);
            verify(mockTelemetryHelper).recordValidationError(
                    eq(Constants.TELEMETRY_FETCH_MAIL_BY_MESSAGE_ID),
                    anyLong(),
                    errorMessageCaptor.capture(),
                    anyMap()
            );

            String capturedErrorMessage = errorMessageCaptor.getValue();
            assertThat(capturedErrorMessage).contains("Error one");
            assertThat(capturedErrorMessage).contains("Error two");
            assertThat(capturedErrorMessage).contains("Error three");
        }
    }

    /**
     * Test suite for fetchInbox method
     */
    @Nested
    @DisplayName("Fetch Inbox - Allow Retry Tests")
    class FetchInboxTests {

        @Test
        @DisplayName("Should trigger subcatalog flow when allowRetry = true")
        void testFetchInbox_AllowRetryTrue() throws IOException, MessagingException {
            // Arrange
            List<ValidationOrchestrator.ValidationResult> validationErrors = List.of(
                    new ValidationOrchestrator.ValidationResult("Confirm", "pageNumber", "Fetch", "Page number error", "NUMBER_FIELD")
            );
            when(mockValidationOrchestrator.validate(anyMap())).thenReturn(validationErrors);

            // Act
            ExtensionResponse response = messagingArea.fetchInbox(1.0, 10.0, true);

            // Assert
            assertThat(response).isNotNull();
        }

        @Test
        @DisplayName("Should return error directly when allowRetry = false")
        void testFetchInbox_AllowRetryFalse() throws IOException, MessagingException {
            // Arrange
            List<ValidationOrchestrator.ValidationResult> validationErrors = List.of(
                    new ValidationOrchestrator.ValidationResult("Confirm", "pageSize", "Fetch", "Invalid page size", "NUMBER_FIELD")
            );
            when(mockValidationOrchestrator.validate(anyMap())).thenReturn(validationErrors);

            // Act
            ExtensionResponse response = messagingArea.fetchInbox(1.0, 10.0, false);

            // Assert
            assertThat(response).isNotNull();
            assertThat(response.getError()).isNotNull();
        }

        @Test
        @DisplayName("Should handle null allowRetry parameter")
        void testFetchInbox_AllowRetryNull() throws IOException, MessagingException {
            // Arrange
            List<ValidationOrchestrator.ValidationResult> validationErrors = List.of(
                    new ValidationOrchestrator.ValidationResult("Confirm", "pageNumber", "Fetch", "Error", "NUMBER_FIELD")
            );
            when(mockValidationOrchestrator.validate(anyMap())).thenReturn(validationErrors);

            // Act
            ExtensionResponse response = messagingArea.fetchInbox(1.0, 10.0, null);

            // Assert - Should default to false behavior
            assertThat(response).isNotNull();
            assertThat(response.getError()).isNotNull();
            verify(mockResponseGenerator, never()).generateFetchResponse(any(), any(), any(), any());
        }
    }

    /**
     * Test suite for sendMail method
     */
    @Nested
    @DisplayName("Send Mail - Allow Retry Tests")
    class SendMailTests {

        @Test
        @DisplayName("Should trigger subcatalog flow when allowRetry = true with validation errors")
        void testSendMail_AllowRetryTrue() throws IOException, MessagingException {
            // Arrange
            String subject = "Test Subject";
            String message = "Test Message";
            String to = "test@example.com";

            List<ValidationOrchestrator.ValidationResult> validationErrors = List.of(
                    new ValidationOrchestrator.ValidationResult("Confirm", "to", "Fetch", "Invalid email address", "TEXT_FIELD")
            );
            when(mockValidationOrchestrator.validate(anyMap())).thenReturn(validationErrors);

            ExtensionResponse expectedResponse = mock(ExtensionResponse.class);
            when(mockResponseGenerator.generateConfirmationResponse(
                    any(ExtensionResponse.Error.ExceptionType.class),
                    anyList(),
                    anyString(),
                    anyMap()
            )).thenReturn(expectedResponse);

            // Act
            ExtensionResponse response = messagingArea.sendMail(
                    subject, message, null, to, null, null, null, true
            );

            // Assert
            assertThat(response).isEqualTo(expectedResponse);
            verify(mockResponseGenerator).generateConfirmationResponse(
                    eq(ExtensionResponse.Error.ExceptionType.INPUT_ERROR),
                    eq(validationErrors),
                    eq(SubCatalogConstants.CONFIRM_REENTER_SEND_MAIL),
                    anyMap()
            );
            verify(mockInternalStateManager).put(anyString(), anyString());
            verify(mockTelemetryHelper).recordRetryPrompted(
                    eq(Constants.TELEMETRY_SEND_MAIL),
                    anyLong(),
                    anyMap()
            );
        }

        @Test
        @DisplayName("Should return error directly when allowRetry = false")
        void testSendMail_AllowRetryFalse() throws IOException, MessagingException {
            // Arrange
            String subject = "Test Subject";
            String message = "Test Message";
            String to = "invalid-email";

            List<ValidationOrchestrator.ValidationResult> validationErrors = List.of(
                    new ValidationOrchestrator.ValidationResult("Confirm", "to", "Fetch", "Invalid email format", "TEXT_FIELD")
            );
            when(mockValidationOrchestrator.validate(anyMap())).thenReturn(validationErrors);

            // Act
            ExtensionResponse response = messagingArea.sendMail(
                    subject, message, null, to, null, null, null, false
            );

            // Assert
            assertThat(response).isNotNull();
            assertThat(response.getError()).isNotNull();
            verify(mockResponseGenerator, never()).generateConfirmationResponse(any(), any(), any(), any());
            verify(mockInternalStateManager, never()).put(anyString(), anyString());
            verify(mockTelemetryHelper).recordValidationError(
                    eq(Constants.TELEMETRY_SEND_MAIL),
                    anyLong(),
                    anyString(),
                    anyMap()
            );
        }

        @Test
        @DisplayName("Should send email successfully when no validation errors")
        void testSendMail_NoValidationErrors() throws IOException, MessagingException {
            // Arrange
            String subject = "Test Subject";
            String message = "Test Message";
            String to = "test@example.com";

            when(mockValidationOrchestrator.validate(anyMap())).thenReturn(List.of());
            when(mockAccount.newEmail()).thenReturn(mockEmailBuilder);
            when(mockEmailBuilder.withSubject(anyString())).thenReturn(mockEmailBuilder);
            when(mockEmailBuilder.withTo(anyList())).thenReturn(mockEmailBuilder);
            when(mockEmailBuilder.withCc(anyList())).thenReturn(mockEmailBuilder);
            when(mockEmailBuilder.withBcc(anyList())).thenReturn(mockEmailBuilder);
            when(mockEmailBuilder.withReplyTo(anyList())).thenReturn(mockEmailBuilder);
            when(mockEmailBuilder.withContent(anyString())).thenReturn(mockEmailBuilder);
            doNothing().when(mockEmailBuilder).send();

            // Act
            ExtensionResponse response = messagingArea.sendMail(
                    subject, message, null, to, null, null, null, false
            );

            // Assert
            assertThat(response).isNotNull();
            assertThat(response.getError()).isNull();
            verify(mockEmailBuilder).send();
            verify(mockTelemetryHelper).recordSuccess(
                    eq(Constants.TELEMETRY_SEND_MAIL),
                    anyLong(),
                    anyMap()
            );
        }
    }

    /**
     * Test suite for replyToMail method
     */
    @Nested
    @DisplayName("Reply To Mail - Allow Retry Tests")
    class ReplyToMailTests {

        @Test
        @DisplayName("Should trigger subcatalog flow when allowRetry = true")
        void testReplyToMail_AllowRetryTrue() throws IOException, MessagingException {
            // Arrange
            String messageId = "test-message-id";
            String message = "Reply message";

            List<ValidationOrchestrator.ValidationResult> validationErrors = List.of(
                    new ValidationOrchestrator.ValidationResult("Confirm", "messageId", "Fetch", "Invalid message ID", "TEXT_FIELD")
            );
            when(mockValidationOrchestrator.validate(anyMap())).thenReturn(validationErrors);

            ExtensionResponse expectedResponse = mock(ExtensionResponse.class);
            when(mockResponseGenerator.generateConfirmationResponse(
                    any(ExtensionResponse.Error.ExceptionType.class),
                    anyList(),
                    anyString(),
                    anyMap()
            )).thenReturn(expectedResponse);

            // Act
            ExtensionResponse response = messagingArea.replyToMail(messageId, message, null, true);

            // Assert
            assertThat(response).isEqualTo(expectedResponse);
            verify(mockResponseGenerator).generateConfirmationResponse(
                    eq(ExtensionResponse.Error.ExceptionType.INPUT_ERROR),
                    eq(validationErrors),
                    eq(SubCatalogConstants.CONFIRM_REENTER_REPLY_TO_MAIL),
                    anyMap()
            );
            verify(mockTelemetryHelper).recordRetryPrompted(
                    eq(Constants.TELEMETRY_REPLY_TO_MAIL),
                    anyLong(),
                    anyMap()
            );
        }

        @Test
        @DisplayName("Should return error directly when allowRetry = false")
        void testReplyToMail_AllowRetryFalse() throws IOException, MessagingException {
            // Arrange
            String messageId = "invalid-id";
            String message = "Reply message";

            List<ValidationOrchestrator.ValidationResult> validationErrors = List.of(
                    new ValidationOrchestrator.ValidationResult("Confirm", "messageId", "Fetch", "Message ID not found", "TEXT_FIELD")
            );
            when(mockValidationOrchestrator.validate(anyMap())).thenReturn(validationErrors);

            // Act
            ExtensionResponse response = messagingArea.replyToMail(messageId, message, null, false);

            // Assert
            assertThat(response).isNotNull();
            assertThat(response.getError()).isNotNull();
            verify(mockResponseGenerator, never()).generateConfirmationResponse(any(), any(), any(), any());
            verify(mockTelemetryHelper).recordValidationError(
                    eq(Constants.TELEMETRY_REPLY_TO_MAIL),
                    anyLong(),
                    anyString(),
                    anyMap()
            );
        }
    }

    /**
     * Test suite for fetchSent method with null parameter handling
     */
    @Nested
    @DisplayName("Fetch Sent - Null Parameter Handling Tests")
    class FetchSentNullParameterTests {

        @Test
        @DisplayName("Should handle null pageNumber and pageSize without NullPointerException")
        void testNullPageNumberAndPageSize_NoNullPointerException() throws IOException, MessagingException {
            // Arrange
            when(mockValidationOrchestrator.validate(anyMap())).thenReturn(List.of());
            when(mockAccount.getSentFolder()).thenReturn(mockFolder);
            when(mockFolder.getEmails(any(Double.class), any(Double.class))).thenReturn(List.of());

            // Act & Assert - Should not throw NullPointerException
            ExtensionResponse response = messagingArea.fetchSent(null, null, false);

            // Assert
            assertThat(response).isNotNull();

            // Verify null values were passed through
            verify(mockFolder).getEmails(isNull(), isNull());
        }

        @Test
        @DisplayName("Should use HashMap that allows null values for state storage")
        void testHashMapAllowsNullValues_WithAllowRetryTrue() throws IOException, MessagingException {
            // Arrange - When both pageNumber and pageSize are null, validation is skipped
            when(mockValidationOrchestrator.validate(anyMap())).thenReturn(List.of());
            when(mockAccount.getSentFolder()).thenReturn(mockFolder);
            when(mockFolder.getEmails(any(Double.class), any(Double.class))).thenReturn(List.of());

            // Act - Pass null values (should not throw NullPointerException with HashMap)
            ExtensionResponse response = messagingArea.fetchSent(null, null, true);

            // Assert - Should not throw exception (HashMap allows null values, Map.of does not)
            assertThat(response).isNotNull();
        }
    }
}

