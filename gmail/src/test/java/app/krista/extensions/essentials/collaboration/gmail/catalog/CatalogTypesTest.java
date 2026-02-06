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

import app.krista.extensions.essentials.collaboration.gmail.catalog.entities.MailDetails;
import app.krista.extensions.essentials.collaboration.gmail.impl.stores.KristaMediaClient;
import app.krista.extensions.essentials.collaboration.gmail.service.Attachment;
import app.krista.extensions.essentials.collaboration.gmail.service.Email;
import app.krista.extensions.essentials.collaboration.gmail.service.EmailAddress;
import app.krista.model.base.File;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Comprehensive test suite for CatalogTypes to achieve high code coverage.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("CatalogTypes Tests")
class CatalogTypesTest {

    @Mock
    private KristaMediaClient mockKristaMediaClient;

    @Mock
    private Email mockEmail;

    @Mock
    private EmailAddress mockSenderAddress;

    @Mock
    private Attachment mockAttachment;

    @Mock
    private File mockFile;

    @Nested
    @DisplayName("fromEmail Tests")
    class FromEmailTests {

        @Test
        @DisplayName("Should return empty MailDetails when email is null")
        void testFromEmail_WithNullEmail_ShouldReturnEmptyMailDetails() {
            // When
            MailDetails result = CatalogTypes.fromEmail(null, mockKristaMediaClient);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.from).isNull();
            assertThat(result.to).isNull();
        }

        @Test
        @DisplayName("Should convert email with all fields populated")
        void testFromEmail_WithCompleteEmail_ShouldMapAllFields() {
            // Given
            EmailAddress senderAddress = new EmailAddress("Sender", "sender@example.com");
            EmailAddress toAddress1 = new EmailAddress("To1", "to1@example.com");
            EmailAddress toAddress2 = new EmailAddress("To2", "to2@example.com");
            EmailAddress ccAddress = new EmailAddress("CC", "cc@example.com");
            EmailAddress bccAddress = new EmailAddress("BCC", "bcc@example.com");
            EmailAddress replyToAddress = new EmailAddress("ReplyTo", "replyto@example.com");

            when(mockEmail.getSenderEmailAddress()).thenReturn(senderAddress);
            when(mockEmail.getToEmailAddresses()).thenReturn(List.of(toAddress1, toAddress2));
            when(mockEmail.getCcEmailAddresses()).thenReturn(List.of(ccAddress));
            when(mockEmail.getBccEmailAddresses()).thenReturn(List.of(bccAddress));
            when(mockEmail.getReplyToEmailAddresses()).thenReturn(List.of(replyToAddress));
            when(mockEmail.getContent()).thenReturn("Test message content");
            when(mockEmail.getSubject()).thenReturn("Test Subject");
            when(mockEmail.getEmailId()).thenReturn("msg-12345");
            when(mockEmail.getRead()).thenReturn(true);
            when(mockEmail.getSendDateAndTime()).thenReturn(1234567890L);
            when(mockEmail.getReceivedDateAndTime()).thenReturn(1234567900L);
            when(mockEmail.getFileAttachments()).thenReturn(List.of());

            // When
            MailDetails result = CatalogTypes.fromEmail(mockEmail, mockKristaMediaClient);

            // Then
            assertThat(result.from).isEqualTo("sender@example.com");
            assertThat(result.to).isEqualTo("to1@example.com,to2@example.com");
            assertThat(result.cc).isEqualTo("cc@example.com");
            assertThat(result.bcc).isEqualTo("bcc@example.com");
            assertThat(result.replyTo).isEqualTo("replyto@example.com");
            assertThat(result.message).isEqualTo("Test message content");
            assertThat(result.subject).isEqualTo("Test Subject");
            assertThat(result.messageID).isEqualTo("msg-12345");
            assertThat(result.isRead).isTrue();
            assertThat(result.sendDateAndTime).isEqualTo(1234567890L);
            assertThat(result.receivedDateAndTime).isEqualTo(1234567900L);
        }

        @Test
        @DisplayName("Should handle email with null sender")
        void testFromEmail_WithNullSender_ShouldSetFromToNull() {
            // Given
            when(mockEmail.getSenderEmailAddress()).thenReturn(null);
            when(mockEmail.getToEmailAddresses()).thenReturn(List.of());
            when(mockEmail.getCcEmailAddresses()).thenReturn(List.of());
            when(mockEmail.getBccEmailAddresses()).thenReturn(List.of());
            when(mockEmail.getReplyToEmailAddresses()).thenReturn(List.of());
            when(mockEmail.getFileAttachments()).thenReturn(List.of());

            // When
            MailDetails result = CatalogTypes.fromEmail(mockEmail, mockKristaMediaClient);

            // Then
            assertThat(result.from).isNull();
        }

        @Test
        @DisplayName("Should handle email with empty recipient lists")
        void testFromEmail_WithEmptyRecipients_ShouldSetEmptyStrings() {
            // Given
            when(mockEmail.getSenderEmailAddress()).thenReturn(mockSenderAddress);
            when(mockSenderAddress.getEmailAddress()).thenReturn("sender@example.com");
            when(mockEmail.getToEmailAddresses()).thenReturn(List.of());
            when(mockEmail.getCcEmailAddresses()).thenReturn(List.of());
            when(mockEmail.getBccEmailAddresses()).thenReturn(List.of());
            when(mockEmail.getReplyToEmailAddresses()).thenReturn(List.of());
            when(mockEmail.getFileAttachments()).thenReturn(List.of());

            // When
            MailDetails result = CatalogTypes.fromEmail(mockEmail, mockKristaMediaClient);

            // Then
            assertThat(result.to).isEmpty();
            assertThat(result.cc).isEmpty();
            assertThat(result.bcc).isEmpty();
            assertThat(result.replyTo).isEmpty();
        }

        @Test
        @DisplayName("Should handle email with null recipient lists")
        void testFromEmail_WithNullRecipients_ShouldSetEmptyStrings() {
            // Given
            when(mockEmail.getSenderEmailAddress()).thenReturn(mockSenderAddress);
            when(mockSenderAddress.getEmailAddress()).thenReturn("sender@example.com");
            when(mockEmail.getToEmailAddresses()).thenReturn(null);
            when(mockEmail.getCcEmailAddresses()).thenReturn(null);
            when(mockEmail.getBccEmailAddresses()).thenReturn(null);
            when(mockEmail.getReplyToEmailAddresses()).thenReturn(null);
            when(mockEmail.getFileAttachments()).thenReturn(List.of());

            // When
            MailDetails result = CatalogTypes.fromEmail(mockEmail, mockKristaMediaClient);

            // Then
            assertThat(result.to).isEmpty();
            assertThat(result.cc).isEmpty();
            assertThat(result.bcc).isEmpty();
            assertThat(result.replyTo).isEmpty();
        }

        @Test
        @DisplayName("Should download and filter attachments")
        void testFromEmail_WithAttachments_ShouldDownloadAndFilter() {
            // Given
            Attachment attachment1 = mock(Attachment.class);
            Attachment attachment2 = mock(Attachment.class);
            Attachment attachment3 = mock(Attachment.class);
            
            File file1 = mock(File.class);
            File file2 = mock(File.class);
            
            when(attachment1.download(mockKristaMediaClient)).thenReturn(file1);
            when(attachment2.download(mockKristaMediaClient)).thenReturn(null); // This should be filtered out
            when(attachment3.download(mockKristaMediaClient)).thenReturn(file2);
            
            when(mockEmail.getSenderEmailAddress()).thenReturn(mockSenderAddress);
            when(mockSenderAddress.getEmailAddress()).thenReturn("sender@example.com");
            when(mockEmail.getToEmailAddresses()).thenReturn(List.of());
            when(mockEmail.getCcEmailAddresses()).thenReturn(List.of());
            when(mockEmail.getBccEmailAddresses()).thenReturn(List.of());
            when(mockEmail.getReplyToEmailAddresses()).thenReturn(List.of());
            when(mockEmail.getFileAttachments()).thenReturn(List.of(attachment1, attachment2, attachment3));

            // When
            MailDetails result = CatalogTypes.fromEmail(mockEmail, mockKristaMediaClient);

            // Then
            assertThat(result.fileAttachment).hasSize(2);
            assertThat(result.fileAttachment).containsExactly(file1, file2);
        }
    }

    @Nested
    @DisplayName("toEmailAddresses Tests")
    class ToEmailAddressesTests {

        @Test
        @DisplayName("Should return empty list for null input")
        void testToEmailAddresses_WithNull_ShouldReturnEmptyList() {
            // When
            List<EmailAddress> result = CatalogTypes.toEmailAddresses(null);

            // Then
            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("Should return empty list for blank input")
        void testToEmailAddresses_WithBlank_ShouldReturnEmptyList() {
            // When
            List<EmailAddress> result = CatalogTypes.toEmailAddresses("   ");

            // Then
            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("Should convert single valid email address")
        void testToEmailAddresses_WithSingleEmail_ShouldConvert() {
            // When
            List<EmailAddress> result = CatalogTypes.toEmailAddresses("test@example.com");

            // Then
            assertThat(result).hasSize(1);
            assertThat(result.get(0).getEmailAddress()).isEqualTo("test@example.com");
        }

        @Test
        @DisplayName("Should convert multiple valid email addresses")
        void testToEmailAddresses_WithMultipleEmails_ShouldConvertAll() {
            // When
            List<EmailAddress> result = CatalogTypes.toEmailAddresses("test1@example.com,test2@example.com,test3@example.com");

            // Then
            assertThat(result).hasSize(3);
            assertThat(result.get(0).getEmailAddress()).isEqualTo("test1@example.com");
            assertThat(result.get(1).getEmailAddress()).isEqualTo("test2@example.com");
            assertThat(result.get(2).getEmailAddress()).isEqualTo("test3@example.com");
        }

        @Test
        @DisplayName("Should throw exception for invalid email address")
        void testToEmailAddresses_WithInvalidEmail_ShouldThrowException() {
            // When & Then
            assertThatThrownBy(() -> CatalogTypes.toEmailAddresses("invalid-email"))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("Mail address is not valid, please provide correct mail address.");
        }

        @Test
        @DisplayName("Should throw exception when one of multiple emails is invalid")
        void testToEmailAddresses_WithOneInvalidEmail_ShouldThrowException() {
            // When & Then
            assertThatThrownBy(() -> CatalogTypes.toEmailAddresses("valid@example.com,invalid-email,another@example.com"))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("Mail address is not valid, please provide correct mail address.");
        }
    }

    @Nested
    @DisplayName("toAttachments Tests")
    class ToAttachmentsTests {

        @Test
        @DisplayName("Should return empty list for null attachments")
        void testToAttachments_WithNull_ShouldReturnEmptyList() throws IOException {
            // When
            List<java.io.File> result = CatalogTypes.toAttachments(null, mockKristaMediaClient);

            // Then
            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("Should return empty list for empty attachments")
        void testToAttachments_WithEmptyList_ShouldReturnEmptyList() throws IOException {
            // When
            List<java.io.File> result = CatalogTypes.toAttachments(List.of(), mockKristaMediaClient);

            // Then
            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("Should convert single attachment")
        void testToAttachments_WithSingleAttachment_ShouldConvert() throws IOException {
            // Given
            java.io.File expectedFile = new java.io.File("/tmp/test.txt");
            when(mockKristaMediaClient.toJavaFile(mockFile)).thenReturn(expectedFile);

            // When
            List<java.io.File> result = CatalogTypes.toAttachments(List.of(mockFile), mockKristaMediaClient);

            // Then
            assertThat(result).hasSize(1);
            assertThat(result.get(0)).isEqualTo(expectedFile);
            verify(mockKristaMediaClient).toJavaFile(mockFile);
        }

        @Test
        @DisplayName("Should convert multiple attachments")
        void testToAttachments_WithMultipleAttachments_ShouldConvertAll() throws IOException {
            // Given
            File file1 = mock(File.class);
            File file2 = mock(File.class);
            File file3 = mock(File.class);
            
            java.io.File javaFile1 = new java.io.File("/tmp/file1.txt");
            java.io.File javaFile2 = new java.io.File("/tmp/file2.pdf");
            java.io.File javaFile3 = new java.io.File("/tmp/file3.doc");
            
            when(mockKristaMediaClient.toJavaFile(file1)).thenReturn(javaFile1);
            when(mockKristaMediaClient.toJavaFile(file2)).thenReturn(javaFile2);
            when(mockKristaMediaClient.toJavaFile(file3)).thenReturn(javaFile3);

            // When
            List<java.io.File> result = CatalogTypes.toAttachments(List.of(file1, file2, file3), mockKristaMediaClient);

            // Then
            assertThat(result).hasSize(3);
            assertThat(result).containsExactly(javaFile1, javaFile2, javaFile3);
        }
    }
}

