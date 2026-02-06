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
import app.krista.extensions.essentials.collaboration.gmail.impl.connectors.GmailProviderFactory;
import app.krista.extensions.essentials.collaboration.gmail.impl.stores.KristaMediaClient;
import app.krista.extensions.essentials.collaboration.gmail.service.Email;
import app.krista.extensions.essentials.collaboration.gmail.service.EmailBuilder;
import app.krista.extensions.essentials.collaboration.gmail.service.Folder;
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
 * Comprehensive test suite for AccountImpl class.
 * Tests all public methods, error scenarios, and edge cases.
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class AccountImplTest {

    @Mock
    private GmailProviderFactory mockProviderFactory;
    
    @Mock
    private GmailProvider mockProvider;
    
    @Mock
    private RequestContext mockRequestContext;
    
    @Mock
    private KristaMediaClient mockKristaMediaClient;
    
    @Mock
    private Gmail mockGmail;
    
    @Mock
    private Gmail.Users mockUsers;
    
    @Mock
    private Gmail.Users.Messages mockMessages;
    
    @Mock
    private Gmail.Users.Messages.Get mockMessagesGet;
    
    @Mock
    private Gmail.Users.Messages.List mockMessagesList;
    
    @Mock
    private Gmail.Users.Labels mockLabels;
    
    @Mock
    private Gmail.Users.Labels.List mockLabelsList;
    
    @Mock
    private Message mockMessage;
    
    @Mock
    private ListMessagesResponse mockListMessagesResponse;
    
    @Mock
    private ListLabelsResponse mockListLabelsResponse;
    
    @Mock
    private GoogleJsonResponseException mockGoogleException;

    private AccountImpl accountImpl;

    @BeforeEach
    void setUp() throws IOException {
        when(mockProviderFactory.create()).thenReturn(mockProvider);
        when(mockProvider.getGmailClient()).thenReturn(mockGmail);
        when(mockGmail.users()).thenReturn(mockUsers);
        when(mockUsers.messages()).thenReturn(mockMessages);
        when(mockUsers.labels()).thenReturn(mockLabels);
        
        accountImpl = new AccountImpl(mockProviderFactory, mockRequestContext, mockKristaMediaClient);
    }

    @Test
    void testNewEmail_ShouldReturnEmailBuilderImpl() {
        // When
        EmailBuilder result = accountImpl.newEmail();

        // Then
        assertThat(result).isNotNull();
        assertThat(result).isInstanceOf(EmailBuilderImpl.class);
    }

    @Test
    void testGetEmail_WithValidMessageId_ShouldReturnEmail() throws IOException {
        // Given
        String messageId = "test-message-id";
        when(mockMessages.get(eq("me"), eq(messageId))).thenReturn(mockMessagesGet);
        when(mockMessagesGet.execute()).thenReturn(mockMessage);

        // When
        Email result = accountImpl.getEmail(messageId);

        // Then
        assertThat(result).isNotNull();
        assertThat(result).isInstanceOf(EmailImpl.class);
        verify(mockMessages).get("me", messageId);
        verify(mockMessagesGet).execute();
    }

    @Test
    void testGetEmail_WithIOException_ShouldReturnNull() throws IOException {
        // Given
        String messageId = "invalid-message-id";
        when(mockMessages.get(eq("me"), eq(messageId))).thenReturn(mockMessagesGet);
        when(mockMessagesGet.execute()).thenThrow(mockGoogleException);

        // When
        Email result = accountImpl.getEmail(messageId);

        // Then
        assertThat(result).isNull();
        verify(mockMessages).get("me", messageId);
    }

    @Test
    void testFetchAllMessageIds_ShouldReturnAllMessageIds() throws IOException {
        // Given
        List<Message> messages1 = Arrays.asList(
            createMockMessage("msg1"),
            createMockMessage("msg2")
        );
        List<Message> messages2 = Arrays.asList(
            createMockMessage("msg3")
        );
        
        ListMessagesResponse response1 = new ListMessagesResponse()
            .setMessages(messages1)
            .setNextPageToken("token1");
        ListMessagesResponse response2 = new ListMessagesResponse()
            .setMessages(messages2);

        when(mockMessages.list("me")).thenReturn(mockMessagesList);
        when(mockMessagesList.execute())
            .thenReturn(response1)
            .thenReturn(response2);
        when(mockMessagesList.setPageToken("token1")).thenReturn(mockMessagesList);

        // When
        Set<String> result = accountImpl.fetchAllMessageIds();

        // Then
        assertThat(result).containsExactlyInAnyOrder("msg1", "msg2", "msg3");
        verify(mockMessages, times(2)).list("me");
        verify(mockMessagesList).setPageToken("token1");
    }

    @Test
    void testFetchAllMessageIds_WithIOException_ShouldThrowIOException() throws IOException {
        // Given
        when(mockMessages.list("me")).thenReturn(mockMessagesList);
        when(mockMessagesList.execute()).thenThrow(new IOException("Network error"));

        // When & Then
        assertThatThrownBy(() -> accountImpl.fetchAllMessageIds())
            .isInstanceOf(IOException.class)
            .hasMessageContaining("Network error");
    }

    @Test
    void testGetFolderNames_ShouldReturnAllFolderNames() throws IOException {
        // Given
        List<Label> labels = Arrays.asList(
            createMockLabel("INBOX", "inbox"),
            createMockLabel("SENT", "sent"),
            createMockLabel("DRAFT", "draft")
        );

        ListLabelsResponse response = new ListLabelsResponse().setLabels(labels);
        when(mockLabels.list("me")).thenReturn(mockLabelsList);
        when(mockLabelsList.execute()).thenReturn(response);

        // When
        List<String> result = accountImpl.getFolderNames();

        // Then
        assertThat(result).hasSize(3);
        assertThat(result).containsExactlyInAnyOrder("inbox", "sent", "draft");
        verify(mockLabels).list("me");
        verify(mockLabelsList).execute();
    }

    @Test
    void testGetFolderNames_WithIOException_ShouldReturnEmptyList() throws IOException {
        // Given
        when(mockLabels.list("me")).thenReturn(mockLabelsList);
        when(mockLabelsList.execute()).thenThrow(new IOException("Network error"));

        // When
        List<String> result = accountImpl.getFolderNames();

        // Then
        assertThat(result).isEmpty();
        verify(mockLabels).list("me");
    }

    @Test
    void testGetFolder_WithValidFolderId_ShouldReturnFolder() throws IOException {
        // Given
        String folderId = "INBOX";
        List<Label> labels = Arrays.asList(createMockLabel("INBOX", "inbox"));
        ListLabelsResponse response = new ListLabelsResponse().setLabels(labels);

        when(mockLabels.list("me")).thenReturn(mockLabelsList);
        when(mockLabelsList.execute()).thenReturn(response);

        // When
        Folder result = accountImpl.getFolder(folderId);

        // Then
        assertThat(result).isNotNull();
        assertThat(result).isInstanceOf(FolderImpl.class);
    }

    @Test
    void testGetFolder_WithInvalidFolderId_ShouldReturnNull() throws IOException {
        // Given
        String folderId = "NONEXISTENT";
        List<Label> labels = Arrays.asList(createMockLabel("INBOX", "inbox"));
        ListLabelsResponse response = new ListLabelsResponse().setLabels(labels);

        when(mockLabels.list("me")).thenReturn(mockLabelsList);
        when(mockLabelsList.execute()).thenReturn(response);

        // When
        Folder result = accountImpl.getFolder(folderId);

        // Then
        assertThat(result).isNull();
    }

    @Test
    void testGetFolder_WithIOException_ShouldThrowIllegalStateException() throws IOException {
        // Given
        String folderId = "INBOX";
        when(mockLabels.list("me")).thenReturn(mockLabelsList);
        when(mockLabelsList.execute()).thenThrow(new IOException("Network error"));

        // When & Then
        assertThatThrownBy(() -> accountImpl.getFolder(folderId))
            .isInstanceOf(IllegalStateException.class)
            .hasMessageContaining("No Labels found");
    }

    private Message createMockMessage(String id) {
        Message message = new Message();
        message.setId(id);
        return message;
    }

    @Test
    void testGetFolderByName_WithValidName_ShouldReturnFolder() throws IOException {
        // Given
        String folderName = "inbox";
        List<Label> labels = Arrays.asList(createMockLabel("INBOX", "inbox"));
        ListLabelsResponse response = new ListLabelsResponse().setLabels(labels);

        when(mockLabels.list("me")).thenReturn(mockLabelsList);
        when(mockLabelsList.execute()).thenReturn(response);

        // When
        Folder result = accountImpl.getFolderByName(folderName);

        // Then
        assertThat(result).isNotNull();
        assertThat(result).isInstanceOf(FolderImpl.class);
    }

    @Test
    void testGetFolderByName_WithInvalidName_ShouldReturnNull() throws IOException {
        // Given
        String folderName = "nonexistent";
        List<Label> labels = Arrays.asList(createMockLabel("INBOX", "inbox"));
        ListLabelsResponse response = new ListLabelsResponse().setLabels(labels);

        when(mockLabels.list("me")).thenReturn(mockLabelsList);
        when(mockLabelsList.execute()).thenReturn(response);

        // When
        Folder result = accountImpl.getFolderByName(folderName);

        // Then
        assertThat(result).isNull();
    }

    @Test
    void testGetInboxFolder_ShouldReturnInboxFolder() throws IOException {
        // Given
        List<Label> labels = Arrays.asList(createMockLabel("INBOX", "inbox"));
        ListLabelsResponse response = new ListLabelsResponse().setLabels(labels);

        when(mockLabels.list("me")).thenReturn(mockLabelsList);
        when(mockLabelsList.execute()).thenReturn(response);

        // When
        Folder result = accountImpl.getInboxFolder();

        // Then
        assertThat(result).isNotNull();
        assertThat(result).isInstanceOf(FolderImpl.class);
    }

    @Test
    void testGetSentFolder_ShouldReturnSentFolder() throws IOException {
        // Given
        List<Label> labels = Arrays.asList(createMockLabel("SENT", "sent"));
        ListLabelsResponse response = new ListLabelsResponse().setLabels(labels);

        when(mockLabels.list("me")).thenReturn(mockLabelsList);
        when(mockLabelsList.execute()).thenReturn(response);

        // When
        Folder result = accountImpl.getSentFolder();

        // Then
        assertThat(result).isNotNull();
        assertThat(result).isInstanceOf(FolderImpl.class);
    }

    @Test
    void testGetFolderIds_ShouldReturnAllFolderIds() throws IOException {
        // Given
        List<Label> labels = Arrays.asList(
            createMockLabel("INBOX", "inbox"),
            createMockLabel("SENT", "sent"),
            createMockLabel("DRAFT", "draft")
        );

        ListLabelsResponse response = new ListLabelsResponse().setLabels(labels);
        when(mockLabels.list("me")).thenReturn(mockLabelsList);
        when(mockLabelsList.execute()).thenReturn(response);

        // When
        List<String> result = accountImpl.getFolderIds();

        // Then
        assertThat(result).hasSize(3);
        assertThat(result).containsExactlyInAnyOrder("INBOX", "SENT", "DRAFT");
    }

    @Test
    void testSearchEmails_WithValidQuery_ShouldReturnEmails() throws IOException {
        // Given
        String query = "from:test@example.com";
        List<Message> messages = Arrays.asList(createMockMessage("msg1"));
        ListMessagesResponse response = new ListMessagesResponse().setMessages(messages);

        when(mockMessages.list("me")).thenReturn(mockMessagesList);
        when(mockMessagesList.setQ(query)).thenReturn(mockMessagesList);
        when(mockMessagesList.setMaxResults(10L)).thenReturn(mockMessagesList);
        when(mockMessagesList.execute()).thenReturn(response);
        when(mockMessages.get(eq("me"), eq("msg1"))).thenReturn(mockMessagesGet);
        when(mockMessagesGet.execute()).thenReturn(mockMessage);

        // When
        List<Email> result = accountImpl.searchEmails(query);

        // Then
        assertThat(result).hasSize(1);
        verify(mockMessages).list("me");
        verify(mockMessagesList).setQ(query);
        verify(mockMessagesList).setMaxResults(10L);
    }

    private Label createMockLabel(String id, String name) {
        Label label = new Label();
        label.setId(id);
        label.setName(name);
        return label;
    }
}
