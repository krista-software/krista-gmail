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
import app.krista.extensions.essentials.collaboration.gmail.service.Attachment;
import app.krista.extensions.essentials.collaboration.gmail.service.EmailAddress;
import app.krista.ksdk.context.RequestContext;
import com.google.api.client.googleapis.json.GoogleJsonResponseException;
import com.google.api.services.gmail.Gmail;
import com.google.api.services.gmail.model.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.io.IOException;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Comprehensive test suite for EmailImpl class.
 * Tests all public methods, error scenarios, and edge cases.
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class EmailImplTest {

    @Mock
    private GmailProvider mockProvider;
    
    @Mock
    private Message mockMessage;
    
    @Mock
    private RequestContext mockRequestContext;
    
    @Mock
    private Gmail mockGmail;
    
    @Mock
    private Gmail.Users mockUsers;
    
    @Mock
    private Gmail.Users.Messages mockMessages;
    
    @Mock
    private Gmail.Users.Messages.Modify mockMessagesModify;
    
    @Mock
    private MessagePart mockMessagePart;
    
    @Mock
    private MessagePartBody mockMessagePartBody;

    private EmailImpl emailImpl;
    private final String messageId = "test-message-id";
    private final String threadId = "test-thread-id";

    @BeforeEach
    void setUp() throws IOException {
        when(mockMessage.getId()).thenReturn(messageId);
        when(mockMessage.getThreadId()).thenReturn(threadId);
        when(mockMessage.getLabelIds()).thenReturn(new ArrayList<>(Arrays.asList("INBOX", "UNREAD")));
        when(mockMessage.getPayload()).thenReturn(mockMessagePart);
        
        when(mockProvider.getGmailClient()).thenReturn(mockGmail);
        when(mockGmail.users()).thenReturn(mockUsers);
        when(mockUsers.messages()).thenReturn(mockMessages);
        
        emailImpl = new EmailImpl(mockProvider, mockMessage, mockRequestContext);
    }

    @Test
    void testGetEmailId_ShouldReturnMessageId() {
        // When
        String result = emailImpl.getEmailId();

        // Then
        assertThat(result).isEqualTo(messageId);
        verify(mockMessage).getId();
    }

    @Test
    void testGetSendDateAndTime_ShouldReturnInternalDate() {
        // Given
        Long internalDate = 1635724800000L; // Example timestamp
        when(mockMessage.getInternalDate()).thenReturn(internalDate);

        // When
        Long result = emailImpl.getSendDateAndTime();

        // Then
        assertThat(result).isEqualTo(internalDate);
        verify(mockMessage).getInternalDate();
    }

    @Test
    void testGetRead_WithUnreadLabel_ShouldReturnFalse() {
        // Given
        when(mockMessage.getLabelIds()).thenReturn(Arrays.asList("INBOX", "UNREAD"));

        // When
        Boolean result = emailImpl.getRead();

        // Then
        assertThat(result).isFalse();
        verify(mockMessage).getLabelIds();
    }

    @Test
    void testGetRead_WithoutUnreadLabel_ShouldReturnTrue() {
        // Given
        when(mockMessage.getLabelIds()).thenReturn(Arrays.asList("INBOX"));

        // When
        Boolean result = emailImpl.getRead();

        // Then
        assertThat(result).isTrue();
        verify(mockMessage).getLabelIds();
    }

    @Test
    void testGetSubject_ShouldReturnSubject() {
        // Given
        MessagePartHeader subjectHeader = new MessagePartHeader();
        subjectHeader.setName("Subject");
        subjectHeader.setValue("Test Subject");
        
        when(mockMessagePart.getHeaders()).thenReturn(Arrays.asList(subjectHeader));

        // When
        String result = emailImpl.getSubject();

        // Then
        assertThat(result).isEqualTo("Test Subject");
        verify(mockMessage).getPayload();
        verify(mockMessagePart).getHeaders();
    }

    @Test
    void testGetBccEmailAddresses_ShouldReturnBccAddresses() {
        // Given
        MessagePartHeader bccHeader = new MessagePartHeader();
        bccHeader.setName("BCC");
        bccHeader.setValue("bcc1@example.com");
        
        when(mockMessagePart.getHeaders()).thenReturn(Arrays.asList(bccHeader));

        // When
        List<EmailAddress> result = emailImpl.getBccEmailAddresses();

        // Then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getEmailAddress()).isEqualTo("bcc1@example.com");
    }

    @Test
    void testMarkAsRead_ShouldRemoveUnreadLabel() throws IOException {
        // Given
        List<String> labelIds = new ArrayList<>(Arrays.asList("INBOX", "UNREAD"));
        when(mockMessage.getLabelIds()).thenReturn(labelIds);
        when(mockMessages.modify(eq("me"), eq(messageId), any(ModifyMessageRequest.class))).thenReturn(mockMessagesModify);
        when(mockMessagesModify.execute()).thenReturn(mockMessage);

        // When
        emailImpl.markAsRead();

        // Then
        assertThat(labelIds).doesNotContain("UNREAD");
        verify(mockMessages).modify(eq("me"), eq(messageId), any(ModifyMessageRequest.class));
        verify(mockMessagesModify).execute();
    }

    @Test
    void testMarkAsUnread_ShouldAddUnreadLabel() throws IOException {
        // Given
        List<String> labelIds = new ArrayList<>(Arrays.asList("INBOX"));
        when(mockMessage.getLabelIds()).thenReturn(labelIds);
        when(mockMessages.modify(eq("me"), eq(messageId), any(ModifyMessageRequest.class))).thenReturn(mockMessagesModify);
        when(mockMessagesModify.execute()).thenReturn(mockMessage);

        // When
        emailImpl.markAsUnread();

        // Then
        assertThat(labelIds).contains("UNREAD");
        verify(mockMessages).modify(eq("me"), eq(messageId), any(ModifyMessageRequest.class));
        verify(mockMessagesModify).execute();
    }

    @Test
    void testGetFileAttachments_WithAttachments_ShouldReturnAttachments() {
        // Given
        MessagePart attachmentPart = new MessagePart();
        attachmentPart.setFilename("test.pdf");
        attachmentPart.setMimeType("application/pdf");
        MessagePartBody attachmentBody = new MessagePartBody();
        attachmentBody.setAttachmentId("attachment-id");
        attachmentPart.setBody(attachmentBody);
        
        when(mockMessagePart.getParts()).thenReturn(Arrays.asList(attachmentPart));

        // When
        List<Attachment> result = emailImpl.getFileAttachments();

        // Then
        assertThat(result).hasSize(1);
        assertThat(result.get(0)).isInstanceOf(AttachmentImpl.class);
    }

    @Test
    void testGetFileAttachments_WithNoAttachments_ShouldReturnEmptyList() {
        // Given
        when(mockMessagePart.getParts()).thenReturn(null);

        // When
        List<Attachment> result = emailImpl.getFileAttachments();

        // Then
        assertThat(result).isEmpty();
    }

    @Test
    void testMarkAsRead_WithIOException_ShouldThrowMustAuthorizeException() throws IOException {
        // Given
        List<String> labelIds = new ArrayList<>(Arrays.asList("INBOX", "UNREAD"));
        when(mockMessage.getLabelIds()).thenReturn(labelIds);
        when(mockMessages.modify(eq("me"), eq(messageId), any(ModifyMessageRequest.class))).thenReturn(mockMessagesModify);
        
        GoogleJsonResponseException googleException = mock(GoogleJsonResponseException.class);
        when(mockMessagesModify.execute()).thenThrow(googleException);
        when(mockProvider.createMustAuthorizationException(anyString(), anyBoolean())).thenReturn(new MustAuthorizeException("Auth required"));

        // When & Then
        assertThatThrownBy(() -> emailImpl.markAsRead())
            .isInstanceOf(IllegalStateException.class)
            .hasMessageContaining("Failed to mark the message read");
    }

    @Test
    void testGetReceivedDateAndTime_ShouldReturnInternalDate() {
        // Given
        Long internalDate = 1635724800000L; // Example timestamp
        when(mockMessage.getInternalDate()).thenReturn(internalDate);

        // When
        Long result = emailImpl.getReceivedDateAndTime();

        // Then
        assertThat(result).isEqualTo(internalDate);
        verify(mockMessage).getInternalDate();
    }

    @Test
    void testGetReceivedDateAndTime_WithNullInternalDate_ShouldReturnNull() {
        // Given
        when(mockMessage.getInternalDate()).thenReturn(null);

        // When
        Long result = emailImpl.getReceivedDateAndTime();

        // Then
        assertThat(result).isNull();
    }
}
