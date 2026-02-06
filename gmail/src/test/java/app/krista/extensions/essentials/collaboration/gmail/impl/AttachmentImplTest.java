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
import app.krista.model.base.File;
import com.google.api.services.gmail.Gmail;
import com.google.api.services.gmail.model.MessagePart;
import com.google.api.services.gmail.model.MessagePartBody;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Comprehensive test suite for AttachmentImpl class.
 * Tests all public methods, error scenarios, and edge cases.
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class AttachmentImplTest {

    @Mock
    private GmailProvider mockProvider;
    
    @Mock
    private MessagePart mockMessagePart;
    
    @Mock
    private MessagePartBody mockMessagePartBody;
    
    @Mock
    private KristaMediaClient mockKristaMediaClient;
    
    @Mock
    private Gmail mockGmail;
    
    @Mock
    private Gmail.Users mockUsers;
    
    @Mock
    private Gmail.Users.Messages mockMessages;
    
    @Mock
    private Gmail.Users.Messages.Attachments mockAttachments;
    
    @Mock
    private Gmail.Users.Messages.Attachments.Get mockAttachmentsGet;
    
    @Mock
    private MessagePartBody mockExecuteResult;
    
    @Mock
    private File mockKristaFile;

    private AttachmentImpl attachmentImpl;
    private final String messageId = "test-message-id";
    private final String attachmentId = "test-attachment-id";
    private final String filename = "test-file.pdf";

    @BeforeEach
    void setUp() throws IOException {
        when(mockMessagePart.getFilename()).thenReturn(filename);
        when(mockMessagePart.getBody()).thenReturn(mockMessagePartBody);
        when(mockMessagePartBody.getAttachmentId()).thenReturn(attachmentId);
        when(mockMessagePartBody.getData()).thenReturn("application/pdf");
        when(mockMessagePartBody.isEmpty()).thenReturn(false);
        
        when(mockProvider.getGmailClient()).thenReturn(mockGmail);
        when(mockGmail.users()).thenReturn(mockUsers);
        when(mockUsers.messages()).thenReturn(mockMessages);
        when(mockMessages.attachments()).thenReturn(mockAttachments);
        
        attachmentImpl = new AttachmentImpl(mockProvider, mockMessagePart, messageId);
    }

    @Test
    void testGetName_ShouldReturnFilename() {
        // When
        String result = attachmentImpl.getName();

        // Then
        assertThat(result).isEqualTo(filename);
        verify(mockMessagePart).getFilename();
    }

    @Test
    void testGetId_ShouldReturnAttachmentId() {
        // When
        String result = attachmentImpl.getId();

        // Then
        assertThat(result).isEqualTo(attachmentId);
        verify(mockMessagePart).getBody();
        verify(mockMessagePartBody).getAttachmentId();
    }

    @Test
    void testGetMimeType_ShouldReturnMimeType() {
        // When
        String result = attachmentImpl.getMimeType();

        // Then
        assertThat(result).isEqualTo("application/pdf");
        verify(mockMessagePart).getBody();
        verify(mockMessagePartBody).getData();
    }

    @Test
    void testGetSize_ShouldReturnSize() {
        // Given
        Integer expectedSize = 1024;
        when(mockMessagePartBody.getSize()).thenReturn(expectedSize);

        // When
        Integer result = attachmentImpl.getSize();

        // Then
        assertThat(result).isEqualTo(expectedSize);
        verify(mockMessagePartBody).getSize();
    }

    @Test
    void testDownload_WithValidAttachment_ShouldReturnKristaFile() throws IOException {
        // Given
        byte[] attachmentData = "test attachment content".getBytes();
        when(mockAttachments.get(eq("me"), eq(messageId), eq(attachmentId))).thenReturn(mockAttachmentsGet);
        when(mockAttachmentsGet.execute()).thenReturn(mockExecuteResult);
        when(mockExecuteResult.decodeData()).thenReturn(attachmentData);
        when(mockKristaMediaClient.toKristaFile(any(java.io.File.class))).thenReturn(mockKristaFile);

        // When
        File result = attachmentImpl.download(mockKristaMediaClient);

        // Then
        assertThat(result).isEqualTo(mockKristaFile);
        verify(mockAttachments).get("me", messageId, attachmentId);
        verify(mockAttachmentsGet).execute();
        verify(mockExecuteResult).decodeData();
        verify(mockKristaMediaClient).toKristaFile(any(java.io.File.class));
    }

    @Test
    void testDownload_WithNullAttachmentId_ShouldReturnNull() {
        // Given
        when(mockMessagePartBody.getAttachmentId()).thenReturn(null);

        // When
        File result = attachmentImpl.download(mockKristaMediaClient);

        // Then
        assertThat(result).isNull();
        verifyNoInteractions(mockAttachments);
    }

    @Test
    void testDownload_WithEmptyBody_ShouldReturnNull() {
        // Given
        when(mockMessagePartBody.isEmpty()).thenReturn(true);

        // When
        File result = attachmentImpl.download(mockKristaMediaClient);

        // Then
        assertThat(result).isNull();
        verifyNoInteractions(mockAttachments);
    }

    @Test
    void testDownload_WithNullDecodedData_ShouldReturnNull() throws IOException {
        // Given
        when(mockAttachments.get(eq("me"), eq(messageId), eq(attachmentId))).thenReturn(mockAttachmentsGet);
        when(mockAttachmentsGet.execute()).thenReturn(mockExecuteResult);
        when(mockExecuteResult.decodeData()).thenReturn(null);

        // When
        File result = attachmentImpl.download(mockKristaMediaClient);

        // Then
        assertThat(result).isNull();
        verify(mockAttachments).get("me", messageId, attachmentId);
        verify(mockAttachmentsGet).execute();
        verify(mockExecuteResult).decodeData();
        verifyNoInteractions(mockKristaMediaClient);
    }

    @Test
    void testDownload_WithIOException_ShouldThrowIllegalStateException() throws IOException {
        // Given
        when(mockAttachments.get(eq("me"), eq(messageId), eq(attachmentId))).thenReturn(mockAttachmentsGet);
        when(mockAttachmentsGet.execute()).thenThrow(new IOException("Network error"));

        // When & Then
        assertThatThrownBy(() -> attachmentImpl.download(mockKristaMediaClient))
            .isInstanceOf(IllegalStateException.class)
            .hasCauseInstanceOf(IOException.class);

        verify(mockAttachments).get("me", messageId, attachmentId);
        verify(mockAttachmentsGet).execute();
        verifyNoInteractions(mockKristaMediaClient);
    }

    @Test
    void testDownload_WithKristaMediaClientException_ShouldPropagateException() throws IOException {
        // Given
        byte[] attachmentData = "test attachment content".getBytes();
        when(mockAttachments.get(eq("me"), eq(messageId), eq(attachmentId))).thenReturn(mockAttachmentsGet);
        when(mockAttachmentsGet.execute()).thenReturn(mockExecuteResult);
        when(mockExecuteResult.decodeData()).thenReturn(attachmentData);
        when(mockKristaMediaClient.toKristaFile(any(java.io.File.class)))
            .thenThrow(new RuntimeException("Media client error"));

        // When & Then
        assertThatThrownBy(() -> attachmentImpl.download(mockKristaMediaClient))
            .isInstanceOf(RuntimeException.class)
            .hasMessageContaining("Media client error");

        verify(mockAttachments).get("me", messageId, attachmentId);
        verify(mockAttachmentsGet).execute();
        verify(mockExecuteResult).decodeData();
        verify(mockKristaMediaClient).toKristaFile(any(java.io.File.class));
    }

    @Test
    void testDownload_WithDifferentFileExtensions_ShouldHandleCorrectly() throws IOException {
        // Given
        String[] filenames = {"document.pdf", "image.jpg", "archive.zip", "text.txt"};
        
        for (String testFilename : filenames) {
            when(mockMessagePart.getFilename()).thenReturn(testFilename);
            byte[] attachmentData = "test content".getBytes();
            when(mockAttachments.get(eq("me"), eq(messageId), eq(attachmentId))).thenReturn(mockAttachmentsGet);
            when(mockAttachmentsGet.execute()).thenReturn(mockExecuteResult);
            when(mockExecuteResult.decodeData()).thenReturn(attachmentData);
            when(mockKristaMediaClient.toKristaFile(any(java.io.File.class))).thenReturn(mockKristaFile);

            // When
            File result = attachmentImpl.download(mockKristaMediaClient);

            // Then
            assertThat(result).isEqualTo(mockKristaFile);
            assertThat(attachmentImpl.getName()).isEqualTo(testFilename);
        }
    }

    @Test
    void testDownload_WithLargeAttachment_ShouldHandleCorrectly() throws IOException {
        // Given
        byte[] largeAttachmentData = new byte[10 * 1024 * 1024]; // 10MB
        when(mockAttachments.get(eq("me"), eq(messageId), eq(attachmentId))).thenReturn(mockAttachmentsGet);
        when(mockAttachmentsGet.execute()).thenReturn(mockExecuteResult);
        when(mockExecuteResult.decodeData()).thenReturn(largeAttachmentData);
        when(mockKristaMediaClient.toKristaFile(any(java.io.File.class))).thenReturn(mockKristaFile);

        // When
        File result = attachmentImpl.download(mockKristaMediaClient);

        // Then
        assertThat(result).isEqualTo(mockKristaFile);
        verify(mockAttachments).get("me", messageId, attachmentId);
        verify(mockAttachmentsGet).execute();
        verify(mockExecuteResult).decodeData();
        verify(mockKristaMediaClient).toKristaFile(any(java.io.File.class));
    }
}
