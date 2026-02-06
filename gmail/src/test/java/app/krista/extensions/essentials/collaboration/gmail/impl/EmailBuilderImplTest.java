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

import app.krista.extension.authorization.MustAuthorizeException;
import app.krista.extensions.essentials.collaboration.gmail.impl.connectors.GmailProvider;
import app.krista.extensions.essentials.collaboration.gmail.impl.stores.KristaMediaClient;
import app.krista.extensions.essentials.collaboration.gmail.service.EmailAddress;
import app.krista.extensions.essentials.collaboration.gmail.service.EmailBuilder;
import app.krista.ksdk.context.RequestContext;
import app.krista.model.base.File;
import com.google.api.client.googleapis.json.GoogleJsonResponseException;
import com.google.api.services.gmail.Gmail;
import com.google.api.services.gmail.model.Message;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import javax.mail.Address;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.Session;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Base64;
import java.util.Collections;
import java.util.List;
import java.util.Properties;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 * Comprehensive test class for EmailBuilderImpl to achieve 100% code coverage.
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class EmailBuilderImplTest {

    @Mock
    private GmailProvider mockGmailProvider;

    @Mock
    private RequestContext mockRequestContext;

    @Mock
    private KristaMediaClient mockMediaClient;

    @Mock
    private Gmail mockGmailClient;

    @Mock
    private Gmail.Users mockUsers;

    @Mock
    private Gmail.Users.Messages mockMessages;

    @Mock
    private Gmail.Users.Messages.Send mockSend;

    private EmailBuilderImpl emailBuilder;
    private MimeMessage mimeMessage;

    @TempDir
    Path tempDir;

    @BeforeEach
    void setUp() {
        Properties props = new Properties();
        Session session = Session.getDefaultInstance(props, null);
        mimeMessage = new MimeMessage(session);
        
        emailBuilder = new EmailBuilderImpl(mockGmailProvider, mimeMessage, mockRequestContext, mockMediaClient);
    }

    @Test
    void testFactoryMethod_ShouldCreateInstanceWithImageImpl() {
        // When
        EmailBuilderImpl createdBuilder = EmailBuilderImpl.create(mockGmailProvider, mockRequestContext, mockMediaClient);

        // Then
        assertThat(createdBuilder).isNotNull();
    }

    @Test
    void testWithTo_WithValidEmailAddresses_ShouldAddToRecipients() throws MessagingException {
        // Given
        List<EmailAddress> toAddresses = Arrays.asList(
            createEmailAddress("test1@example.com"),
            createEmailAddress("test2@example.com")
        );

        // When
        EmailBuilderImpl result = emailBuilder.withTo(toAddresses);

        // Then
        assertThat(result).isSameAs(emailBuilder);
        Address[] recipients = mimeMessage.getRecipients(javax.mail.Message.RecipientType.TO);
        assertThat(recipients).hasSize(2);
        assertThat(recipients[0].toString()).isEqualTo("test1@example.com");
        assertThat(recipients[1].toString()).isEqualTo("test2@example.com");
    }

    @Test
    void testWithCc_WithValidEmailAddresses_ShouldAddCcRecipients() throws MessagingException {
        // Given
        List<EmailAddress> ccAddresses = Arrays.asList(
            createEmailAddress("cc1@example.com"),
            createEmailAddress("cc2@example.com")
        );

        // When
        EmailBuilderImpl result = emailBuilder.withCc(ccAddresses);

        // Then
        assertThat(result).isSameAs(emailBuilder);
        Address[] recipients = mimeMessage.getRecipients(javax.mail.Message.RecipientType.CC);
        assertThat(recipients).hasSize(2);
        assertThat(recipients[0].toString()).isEqualTo("cc1@example.com");
        assertThat(recipients[1].toString()).isEqualTo("cc2@example.com");
    }

    @Test
    void testWithBcc_WithValidEmailAddresses_ShouldAddBccRecipients() throws MessagingException {
        // Given
        List<EmailAddress> bccAddresses = Arrays.asList(
            createEmailAddress("bcc1@example.com"),
            createEmailAddress("bcc2@example.com")
        );

        // When
        EmailBuilderImpl result = emailBuilder.withBcc(bccAddresses);

        // Then
        assertThat(result).isSameAs(emailBuilder);
        Address[] recipients = mimeMessage.getRecipients(javax.mail.Message.RecipientType.BCC);
        assertThat(recipients).hasSize(2);
        assertThat(recipients[0].toString()).isEqualTo("bcc1@example.com");
        assertThat(recipients[1].toString()).isEqualTo("bcc2@example.com");
    }

    @Test
    void testWithSubject_WithValidSubject_ShouldSetSubject() throws MessagingException {
        // Given
        String subject = "Test Email Subject";

        // When
        EmailBuilderImpl result = emailBuilder.withSubject(subject);

        // Then
        assertThat(result).isSameAs(emailBuilder);
        assertThat(mimeMessage.getSubject()).isEqualTo(subject);
    }

    @Test
    void testWithContent_WithValidContent_ShouldSetContent() throws MessagingException, IOException {
        // Given
        String content = "<p>Hello World</p>";

        // When
        EmailBuilderImpl result = emailBuilder.withContent(content);

        // Then
        assertThat(result).isSameAs(emailBuilder);
        Object messageContent = mimeMessage.getContent();
        assertThat(messageContent).isInstanceOf(String.class);
        assertThat((String) messageContent).contains("Hello World");
    }

    @Test
    void testWithContent_WithImageTags_ShouldProcessImages() throws MessagingException, IOException {
        // Given
        String htmlContent = "<p>Hello <img src=\"/media/123456\" alt=\"Test Image\"> World</p>";
        java.io.File mockImageFile = createMockImageFile();
        
        when(mockMediaClient.toJavaFile(any(File.class))).thenReturn(mockImageFile);

        // When
        EmailBuilderImpl result = emailBuilder.withContent(htmlContent);

        // Then
        assertThat(result).isSameAs(emailBuilder);
        Object content = mimeMessage.getContent();
        assertThat(content).isInstanceOf(String.class);
        String processedContent = (String) content;
        
        assertThat(processedContent).contains("data:image/*;base64,");
        assertThat(processedContent).contains("alt=\"Test Image\"");
        assertThat(processedContent).contains("style=\"max-width:100%;height:auto;\"");
        assertThat(processedContent).doesNotContain("src=\"/media/123456\"");
    }

    @Test
    void testWithReplyTo_WithValidAddresses_ShouldSetReplyToAddresses() throws MessagingException {
        // Given
        List<EmailAddress> replyToAddresses = Arrays.asList(
            createEmailAddress("reply1@example.com"),
            createEmailAddress("reply2@example.com")
        );

        // When
        EmailBuilderImpl result = emailBuilder.withReplyTo(replyToAddresses);

        // Then
        assertThat(result).isSameAs(emailBuilder);
        Address[] replyTo = mimeMessage.getReplyTo();
        assertThat(replyTo).hasSize(2);
        assertThat(replyTo[0].toString()).isEqualTo("reply1@example.com");
        assertThat(replyTo[1].toString()).isEqualTo("reply2@example.com");
    }

    @Test
    void testWithAttachment_WithValidAttachments_ShouldAddAttachments() throws MessagingException, IOException {
        // Given
        emailBuilder.withContent("Test email content");
        
        File attachment = createMockFile("test.txt");
        List<File> attachments = Collections.singletonList(attachment);
        
        java.io.File mockJavaFile = createMockTextFile("test.txt", "Test content");
        when(mockMediaClient.toJavaFile(attachment)).thenReturn(mockJavaFile);

        // When
        EmailBuilderImpl result = emailBuilder.withAttachment(attachments, mockMediaClient);

        // Then
        assertThat(result).isSameAs(emailBuilder);
        verify(mockMediaClient).toJavaFile(attachment);
    }

    @Test
    void testSend_WithValidEmail_ShouldSendSuccessfully() throws IOException {
        // Given
        setupGmailMocks();
        emailBuilder.withContent("Test content");

        // When
        emailBuilder.send();

        // Then
        verify(mockGmailProvider).getGmailClient();
        verify(mockSend).execute();
    }

    @Test
    void testSend_WithGoogleJsonResponseException401_InvokeAsUserFalse_ShouldThrowIllegalStateException() throws IOException {
        // Given
        setupGmailMocks();
        emailBuilder.withContent("Test content"); // Add content so send() actually executes
        GoogleJsonResponseException exception = mock(GoogleJsonResponseException.class);
        when(exception.getStatusCode()).thenReturn(401);
        when(mockSend.execute()).thenThrow(exception);
        when(mockRequestContext.invokeAsUser()).thenReturn(false);

        // When & Then
        assertThatThrownBy(() -> emailBuilder.send())
            .isInstanceOf(IllegalStateException.class)
            .hasMessageContaining("You are not authorized");
    }

    @Test
    void testSend_WithGoogleJsonResponseException401_InvokeAsUserTrue_ShouldThrowAuthorizationException() throws IOException {
        // Given
        setupGmailMocks();
        emailBuilder.withContent("Test content"); // Add content so send() actually executes
        GoogleJsonResponseException exception = mock(GoogleJsonResponseException.class);
        when(exception.getStatusCode()).thenReturn(401);
        when(mockSend.execute()).thenThrow(exception);
        when(mockRequestContext.invokeAsUser()).thenReturn(true);

        MustAuthorizeException authException = new MustAuthorizeException("Authorization required", null);
        when(mockGmailProvider.createMustAuthorizationException(anyString(), eq(true))).thenReturn(authException);
        when(mockGmailProvider.getUserId(false)).thenReturn("test-user-id");

        // When & Then
        assertThatThrownBy(() -> emailBuilder.send())
            .isInstanceOf(MustAuthorizeException.class)
            .hasMessageContaining("Authorization required");
    }

    @Test
    void testSend_WithAttachments_ShouldSendWithMultipart() throws IOException, MessagingException {
        // Given
        setupGmailMocks();
        emailBuilder.withContent("Test content");

        File attachment = createMockFile("test.txt");
        java.io.File mockJavaFile = createMockTextFile("test.txt", "Test content");
        when(mockMediaClient.toJavaFile(attachment)).thenReturn(mockJavaFile);

        emailBuilder.withAttachment(Collections.singletonList(attachment), mockMediaClient);

        // When
        emailBuilder.send();

        // Then
        verify(mockSend).execute();

        // Verify multipart was set
        Object content = mimeMessage.getContent();
        assertThat(content).isInstanceOf(Multipart.class);
    }

    @Test
    void testWithTo_WithInvalidEmailAddress_ShouldHandleMessagingException() {
        // Given
        List<EmailAddress> toAddresses = Collections.singletonList(createEmailAddress("invalid-email"));

        // When & Then - should not throw exception, just log error
        EmailBuilderImpl result = emailBuilder.withTo(toAddresses);
        assertThat(result).isSameAs(emailBuilder);
    }

    @Test
    void testWithCc_WithInvalidEmailAddress_ShouldHandleMessagingException() {
        // Given
        List<EmailAddress> ccAddresses = Collections.singletonList(createEmailAddress("invalid-email"));

        // When & Then - should not throw exception, just log error
        EmailBuilderImpl result = emailBuilder.withCc(ccAddresses);
        assertThat(result).isSameAs(emailBuilder);
    }

    @Test
    void testWithBcc_WithInvalidEmailAddress_ShouldHandleMessagingException() {
        // Given
        List<EmailAddress> bccAddresses = Collections.singletonList(createEmailAddress("invalid-email"));

        // When & Then - should not throw exception, just log error
        EmailBuilderImpl result = emailBuilder.withBcc(bccAddresses);
        assertThat(result).isSameAs(emailBuilder);
    }

    @Test
    void testWithSubject_WithNullSubject_ShouldHandleGracefully() throws MessagingException {
        // When
        EmailBuilderImpl result = emailBuilder.withSubject(null);

        // Then
        assertThat(result).isSameAs(emailBuilder);
        assertThat(mimeMessage.getSubject()).isNull();
    }

    @Test
    void testWithAttachment_WithIOException_ShouldHandleGracefully() throws IOException {
        // Given
        File attachment = createMockFile("test.txt");
        List<File> attachments = Collections.singletonList(attachment);

        when(mockMediaClient.toJavaFile(attachment)).thenThrow(new IOException("File access error"));

        // When & Then - should not throw exception, just log error
        EmailBuilderImpl result = emailBuilder.withAttachment(attachments, mockMediaClient);
        assertThat(result).isSameAs(emailBuilder);
    }

    @Test
    void testWithReplyTo_WithInvalidEmailAddress_ShouldHandleAddressException() {
        // Given
        List<EmailAddress> replyToAddresses = Collections.singletonList(createEmailAddress("invalid-email"));

        // When & Then - should not throw exception, just log error
        EmailBuilderImpl result = emailBuilder.withReplyTo(replyToAddresses);
        assertThat(result).isSameAs(emailBuilder);
    }

    @Test
    void testSend_WithIOException_ShouldHandleGracefully() throws IOException {
        // Given
        setupGmailMocks();
        emailBuilder.withContent("Test content"); // Add content so send() actually executes
        when(mockSend.execute()).thenThrow(new IOException("Network error"));

        // When & Then - should not throw exception, just log error
        emailBuilder.send();

        verify(mockSend).execute();
    }

    @Test
    void testWithContent_WithMessagingException_ShouldThrowIllegalStateException() throws MessagingException {
        // Given
        MimeMessage faultyMessage = mock(MimeMessage.class);
        doThrow(new MessagingException("Content error")).when(faultyMessage).setContent(anyString(), anyString());

        EmailBuilderImpl faultyBuilder = new EmailBuilderImpl(mockGmailProvider, faultyMessage, mockRequestContext, mockMediaClient);

        // When & Then
        assertThatThrownBy(() -> faultyBuilder.withContent("test content"))
            .isInstanceOf(IllegalStateException.class)
            .hasCauseInstanceOf(MessagingException.class);
    }

    @Test
    void testWithReplyTo_WithMessagingException_ShouldThrowIllegalStateException() throws MessagingException {
        // Given
        MimeMessage faultyMessage = mock(MimeMessage.class);
        doThrow(new MessagingException("Reply-to error")).when(faultyMessage).setReplyTo(any(Address[].class));

        EmailBuilderImpl faultyBuilder = new EmailBuilderImpl(mockGmailProvider, faultyMessage, mockRequestContext, mockMediaClient);
        List<EmailAddress> replyToAddresses = Collections.singletonList(createEmailAddress("reply@example.com"));

        // When & Then
        assertThatThrownBy(() -> faultyBuilder.withReplyTo(replyToAddresses))
            .isInstanceOf(IllegalStateException.class)
            .hasCauseInstanceOf(MessagingException.class);
    }

    @Test
    void testSend_WithMessagingException_ShouldHandleGracefully() throws IOException, MessagingException {
        // Given
        setupGmailMocks();
        // Create a MimeMessage that will throw MessagingException when writeTo is called
        MimeMessage faultyMessage = mock(MimeMessage.class);
        doThrow(new MessagingException("Message error")).when(faultyMessage).writeTo(any());
        when(faultyMessage.getContent()).thenReturn("test content");

        EmailBuilderImpl faultyBuilder = new EmailBuilderImpl(mockGmailProvider, faultyMessage, mockRequestContext, mockMediaClient);

        // When & Then - should not throw exception, just log error
        faultyBuilder.send();
    }

    @Test
    void testCompleteEmailWorkflow_ShouldWorkEndToEnd() throws MessagingException, IOException {
        // Given
        setupGmailMocks();

        List<EmailAddress> toAddresses = Collections.singletonList(createEmailAddress("to@example.com"));
        List<EmailAddress> ccAddresses = Collections.singletonList(createEmailAddress("cc@example.com"));
        List<EmailAddress> bccAddresses = Collections.singletonList(createEmailAddress("bcc@example.com"));
        List<EmailAddress> replyToAddresses = Collections.singletonList(createEmailAddress("reply@example.com"));

        String subject = "Test Subject";
        String content = "<p>Hello World</p>";

        File attachment = createMockFile("test.txt");
        java.io.File mockJavaFile = createMockTextFile("test.txt", "Test content");
        when(mockMediaClient.toJavaFile(attachment)).thenReturn(mockJavaFile);

        // When
        emailBuilder
            .withTo(toAddresses)
            .withCc(ccAddresses)
            .withBcc(bccAddresses)
            .withReplyTo(replyToAddresses)
            .withSubject(subject)
            .withContent(content)
            .withAttachment(Collections.singletonList(attachment), mockMediaClient)
            .send();

        // Then
        assertThat(mimeMessage.getSubject()).isEqualTo(subject);
        assertThat(mimeMessage.getRecipients(javax.mail.Message.RecipientType.TO)).hasSize(1);
        assertThat(mimeMessage.getRecipients(javax.mail.Message.RecipientType.CC)).hasSize(1);
        assertThat(mimeMessage.getRecipients(javax.mail.Message.RecipientType.BCC)).hasSize(1);
        assertThat(mimeMessage.getReplyTo()).hasSize(1);

        verify(mockSend).execute();
    }

    // Helper methods
    private void setupGmailMocks() throws IOException {
        when(mockGmailProvider.getGmailClient()).thenReturn(mockGmailClient);
        when(mockGmailClient.users()).thenReturn(mockUsers);
        when(mockUsers.messages()).thenReturn(mockMessages);
        when(mockMessages.send(anyString(), any(Message.class))).thenReturn(mockSend);
        when(mockSend.execute()).thenReturn(new Message());
    }

    private EmailAddress createEmailAddress(String email) {
        EmailAddress emailAddress = mock(EmailAddress.class);
        when(emailAddress.getEmailAddress()).thenReturn(email);
        return emailAddress;
    }

    private File createMockFile(String fileName) {
        File file = mock(File.class);
        when(file.getFileName()).thenReturn(fileName);
        return file;
    }

    private java.io.File createMockImageFile() throws IOException {
        Path imagePath = tempDir.resolve("test-image.png");

        // Create a simple test image content (1x1 PNG)
        byte[] pngBytes = Base64.getDecoder().decode(
            "iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAYAAAAfFcSJAAAADUlEQVR42mNkYPhfDwAChAI9jU77yQAAAABJRU5ErkJggg=="
        );

        Files.write(imagePath, pngBytes);
        return imagePath.toFile();
    }

    private java.io.File createMockTextFile(String fileName, String content) throws IOException {
        Path filePath = tempDir.resolve(fileName);
        Files.write(filePath, content.getBytes());
        return filePath.toFile();
    }

    // ===== TESTS FOR 100% COVERAGE =====

    @Test
    void testDefaultMethods_WithTo_StringOnly() {
        // Test default method: withTo(String emailAddress)
        EmailBuilderImpl result = (EmailBuilderImpl) emailBuilder.withTo("test@example.com");
        assertThat(result).isSameAs(emailBuilder);
    }

    @Test
    void testDefaultMethods_WithTo_NameAndEmail() {
        // Test default method: withTo(String name, String emailAddress)
        EmailBuilderImpl result = (EmailBuilderImpl) emailBuilder.withTo("John Doe", "john@example.com");
        assertThat(result).isSameAs(emailBuilder);
    }

    @Test
    void testDefaultMethods_WithCc_StringOnly() {
        // Test default method: withCc(String emailAddress)
        EmailBuilderImpl result = (EmailBuilderImpl) emailBuilder.withCc("cc@example.com");
        assertThat(result).isSameAs(emailBuilder);
    }

    @Test
    void testDefaultMethods_WithCc_NameAndEmail() {
        // Test default method: withCc(String name, String emailAddress)
        EmailBuilderImpl result = (EmailBuilderImpl) emailBuilder.withCc("Jane Doe", "jane@example.com");
        assertThat(result).isSameAs(emailBuilder);
    }

    @Test
    void testDefaultMethods_WithBcc_StringOnly() {
        // Test default method: withBcc(String emailAddress)
        EmailBuilderImpl result = (EmailBuilderImpl) emailBuilder.withBcc("bcc@example.com");
        assertThat(result).isSameAs(emailBuilder);
    }

    @Test
    void testDefaultMethods_WithBcc_NameAndEmail() {
        // Test default method: withBcc(String name, String emailAddress)
        EmailBuilderImpl result = (EmailBuilderImpl) emailBuilder.withBcc("Bob Smith", "bob@example.com");
        assertThat(result).isSameAs(emailBuilder);
    }

    @Test
    void testSend_WithoutContent_ShouldReturnEarly() throws IOException {
        // Test the early return path when no content is set
        setupGmailMocks();

        // When - call send without setting any content
        emailBuilder.send();

        // Then - should not interact with Gmail API since no content was set
        verify(mockGmailProvider, never()).getGmailClient();
    }

    @Test
    void testSend_WithMultipartContent_ShouldSetMultipartContent() throws IOException, MessagingException {
        // Test the multipart.getCount() > 0 branch using a mock message
        setupGmailMocks();

        // Create a mock message for this test
        MimeMessage mockMessage = mock(MimeMessage.class);
        EmailBuilderImpl testBuilder = new EmailBuilderImpl(mockGmailProvider, mockMessage, mockRequestContext, mockMediaClient);

        // Add some content to trigger multipart setup
        testBuilder.withContent("Test content");

        // Add an attachment to create multipart content
        File attachment = createMockFile("test.txt");
        java.io.File mockJavaFile = createMockTextFile("test.txt", "Test content");
        when(mockMediaClient.toJavaFile(attachment)).thenReturn(mockJavaFile);
        testBuilder.withAttachment(Collections.singletonList(attachment), mockMediaClient);

        // When
        testBuilder.send();

        // Then - should call setContent with multipart when multipart has content
        verify(mockMessage).setContent(any(javax.mail.Multipart.class));
        verify(mockGmailProvider).getGmailClient();
    }

    @Test
    void testWithAttachment_WithNullExistingContent_ShouldHandleGracefully() throws IOException, MessagingException {
        // Test the branch where existingContent is null in withAttachment using a mock message
        setupGmailMocks();

        // Create a mock message for this test
        MimeMessage mockMessage = mock(MimeMessage.class);
        EmailBuilderImpl testBuilder = new EmailBuilderImpl(mockGmailProvider, mockMessage, mockRequestContext, mockMediaClient);

        // Mock message.getContent() to return null
        when(mockMessage.getContent()).thenReturn(null);

        // Add an attachment without setting content first
        File attachment = createMockFile("test.txt");
        java.io.File mockJavaFile = createMockTextFile("test.txt", "Test content");
        when(mockMediaClient.toJavaFile(attachment)).thenReturn(mockJavaFile);

        // When
        testBuilder.withAttachment(Collections.singletonList(attachment), mockMediaClient);

        // Then - should handle null content gracefully
        verify(mockMessage).getContent();
        // Should not add text part when content is null
        verify(mockMessage, never()).setContent(any(javax.mail.Multipart.class));
    }

    @Test
    void testWithSubject_WithNullSubject_ShouldHandleNullGracefully() throws MessagingException, IOException {
        // Test edge case with null subject using a mock message
        setupGmailMocks();

        // Create a mock message for this test
        MimeMessage mockMessage = mock(MimeMessage.class);
        EmailBuilderImpl testBuilder = new EmailBuilderImpl(mockGmailProvider, mockMessage, mockRequestContext, mockMediaClient);

        // When
        EmailBuilder result = testBuilder.withSubject(null);

        // Then
        assertThat(result).isEqualTo(testBuilder);
        verify(mockMessage).setSubject(null);
    }

    @Test
    void testSend_WithNoMultipartContent_ShouldNotSetMultipart() throws IOException, MessagingException {
        // Test the branch where multipart.getCount() == 0 in send() method
        setupGmailMocks();

        // Create a mock message for this test
        MimeMessage mockMessage = mock(MimeMessage.class);
        EmailBuilderImpl testBuilder = new EmailBuilderImpl(mockGmailProvider, mockMessage, mockRequestContext, mockMediaClient);

        // Add only content (no attachments) so multipart count remains 0
        testBuilder.withContent("Test content");

        // When
        testBuilder.send();

        // Then - should NOT call setContent with multipart when multipart count is 0
        verify(mockMessage, never()).setContent(any(javax.mail.Multipart.class));
        verify(mockGmailProvider).getGmailClient();
    }

    @Test
    void testSend_WithGoogleJsonResponseException401_InvokeAsUserTrue_ShouldThrowMustAuthorizeException() throws IOException, MessagingException {
        // Test the missing branch where invokeAsUser() returns true in the 401 error handling
        setupGmailMocks();

        // Mock requestContext to return true for invokeAsUser
        when(mockRequestContext.invokeAsUser()).thenReturn(true);

        // Mock provider to throw MustAuthorizeException
        MustAuthorizeException mustAuthorizeException = mock(MustAuthorizeException.class);
        when(mockGmailProvider.createMustAuthorizationException("user123", true)).thenReturn(mustAuthorizeException);
        when(mockGmailProvider.getUserId(false)).thenReturn("user123");

        // Mock Gmail API to throw 401 GoogleJsonResponseException
        GoogleJsonResponseException googleException = mock(GoogleJsonResponseException.class);
        when(googleException.getStatusCode()).thenReturn(401);
        when(mockSend.execute()).thenThrow(googleException);

        emailBuilder.withContent("Test content");

        // When & Then
        assertThatThrownBy(() -> emailBuilder.send())
            .isInstanceOf(MustAuthorizeException.class);

        verify(mockRequestContext).invokeAsUser();
        verify(mockGmailProvider).createMustAuthorizationException("user123", true);
    }

    @Test
    void testSend_WithGoogleJsonResponseExceptionNon401_ShouldNotThrowSpecialException() throws IOException, MessagingException {
        // Test the missing branch where GoogleJsonResponseException has status code != 401
        setupGmailMocks();

        // Mock Gmail API to throw GoogleJsonResponseException with status code 403 (not 401)
        GoogleJsonResponseException googleException = mock(GoogleJsonResponseException.class);
        when(googleException.getStatusCode()).thenReturn(403); // Not 401
        when(mockSend.execute()).thenThrow(googleException);

        emailBuilder.withContent("Test content");

        // When & Then - should not throw IllegalStateException or MustAuthorizeException
        // The exception should be caught but no special handling should occur
        try {
            emailBuilder.send();
            // If we reach here, no exception was thrown (which is expected)
        } catch (IllegalStateException | MustAuthorizeException e) {
            // These should NOT be thrown for non-401 status codes
            throw new AssertionError("Unexpected exception thrown: " + e.getClass().getSimpleName(), e);
        }

        // Verify that invokeAsUser is not called since status code is not 401
        verify(mockRequestContext, never()).invokeAsUser();
        verify(mockGmailProvider, never()).createMustAuthorizationException(anyString(), anyBoolean());
    }
}
