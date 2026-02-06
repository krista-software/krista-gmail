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
import app.krista.extensions.essentials.collaboration.gmail.service.Account;
import app.krista.extensions.essentials.collaboration.gmail.service.Email;
import app.krista.extensions.essentials.collaboration.gmail.service.Folder;
import app.krista.ksdk.context.RequestContext;
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
import java.math.BigInteger;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Comprehensive test suite for FolderImpl class.
 * Tests all public methods, error scenarios, and edge cases.
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class FolderImplTest {

    @Mock
    private Account mockAccount;

    @Mock
    private GmailProvider mockProvider;

    @Mock
    private Label mockLabel;

    @Mock
    private RequestContext mockRequestContext;
    
    @Mock
    private Gmail mockGmail;
    
    @Mock
    private Gmail.Users mockUsers;
    
    @Mock
    private Gmail.Users.Messages mockMessages;
    
    @Mock
    private Gmail.Users.Messages.List mockMessagesList;
    
    @Mock
    private ListMessagesResponse mockListMessagesResponse;
    
    @Mock
    private Gmail.Users.Messages.Get mockMessagesGet;
    
    @Mock
    private Message mockMessage;

    private FolderImpl folderImpl;
    private final String labelId = "test-label-id";
    private final String labelName = "Test Folder";

    @BeforeEach
    void setUp() throws IOException {
        when(mockLabel.getId()).thenReturn(labelId);
        when(mockLabel.getName()).thenReturn(labelName);
        when(mockLabel.getMessagesTotal()).thenReturn(10);
        when(mockLabel.getMessagesUnread()).thenReturn(3);
        
        when(mockProvider.getGmailClient()).thenReturn(mockGmail);
        when(mockGmail.users()).thenReturn(mockUsers);
        when(mockUsers.messages()).thenReturn(mockMessages);
        
        folderImpl = new FolderImpl(mockAccount, mockProvider, mockLabel, mockRequestContext);
    }

    @Test
    void testGetFolderId_ShouldReturnLabelId() {
        // When
        String result = folderImpl.getFolderId();

        // Then
        assertThat(result).isEqualTo(labelId);
        verify(mockLabel).getId();
    }

    @Test
    void testGetFolderName_ShouldReturnLabelName() {
        // When
        String result = folderImpl.getFolderName();

        // Then
        assertThat(result).isEqualTo(labelName);
        verify(mockLabel).getName();
    }

    @Test
    void testGetEmails_WithValidFolder_ShouldReturnEmails() throws IOException {
        // Given
        com.google.api.services.gmail.model.Message messageRef1 = new com.google.api.services.gmail.model.Message();
        messageRef1.setId("message-1");
        messageRef1.setThreadId("thread-1");
        
        com.google.api.services.gmail.model.Message messageRef2 = new com.google.api.services.gmail.model.Message();
        messageRef2.setId("message-2");
        messageRef2.setThreadId("thread-2");
        
        when(mockMessages.list("me")).thenReturn(mockMessagesList);
        when(mockMessagesList.setLabelIds(Arrays.asList(labelId))).thenReturn(mockMessagesList);
        when(mockMessagesList.setMaxResults(10L)).thenReturn(mockMessagesList);
        when(mockMessagesList.execute()).thenReturn(mockListMessagesResponse);
        when(mockListMessagesResponse.getMessages()).thenReturn(Arrays.asList(messageRef1, messageRef2));
        
        when(mockMessages.get("me", "message-1")).thenReturn(mockMessagesGet);
        when(mockMessages.get("me", "message-2")).thenReturn(mockMessagesGet);
        when(mockMessagesGet.execute()).thenReturn(mockMessage);

        // When
        List<Email> result = folderImpl.getEmails(1.0, 10.0);

        // Then
        assertThat(result).hasSize(2);
        assertThat(result.get(0)).isInstanceOf(EmailImpl.class);
        assertThat(result.get(1)).isInstanceOf(EmailImpl.class);

        verify(mockMessages).list("me");
        verify(mockMessagesList).setLabelIds(Arrays.asList(labelId));
        verify(mockMessagesList).setMaxResults(10L);
        verify(mockMessagesList).execute();
        verify(mockMessages, times(2)).get(eq("me"), anyString());
        verify(mockMessagesGet, times(2)).execute();
    }

    @Test
    void testGetEmails_WithIOException_ShouldThrowIllegalStateException() throws IOException {
        // Given
        when(mockMessages.list("me")).thenReturn(mockMessagesList);
        when(mockMessagesList.setLabelIds(Arrays.asList(labelId))).thenReturn(mockMessagesList);
        when(mockMessagesList.setMaxResults(10L)).thenReturn(mockMessagesList);
        when(mockMessagesList.execute()).thenThrow(new IOException("Network error"));

        // When & Then
        assertThatThrownBy(() -> folderImpl.getEmails(1.0, 10.0))
            .isInstanceOf(RuntimeException.class)
            .hasMessageContaining("Failed to get emails for folder");
    }

    @Test
    void testGetEmails_WithLimitParameter_ShouldRespectLimit() throws IOException {
        // Given
        com.google.api.services.gmail.model.Message messageRef = new com.google.api.services.gmail.model.Message();
        messageRef.setId("message-1");
        messageRef.setThreadId("thread-1");
        
        when(mockMessages.list("me")).thenReturn(mockMessagesList);
        when(mockMessagesList.setLabelIds(Arrays.asList(labelId))).thenReturn(mockMessagesList);
        when(mockMessagesList.setMaxResults(5L)).thenReturn(mockMessagesList);
        when(mockMessagesList.execute()).thenReturn(mockListMessagesResponse);
        when(mockListMessagesResponse.getMessages()).thenReturn(Arrays.asList(messageRef));
        
        when(mockMessages.get("me", "message-1")).thenReturn(mockMessagesGet);
        when(mockMessagesGet.execute()).thenReturn(mockMessage);

        // When
        List<Email> result = folderImpl.getEmails(1.0, 5.0);

        // Then
        assertThat(result).hasSize(1);
        verify(mockMessagesList).setMaxResults(5L);
    }

}
