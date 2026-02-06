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

package app.krista.extensions.essentials.collaboration.gmail.impl.stores;

import app.krista.extensions.util.KeyValueStore;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigInteger;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

/**
 * Comprehensive test suite for all Store classes to achieve high code coverage.
 * Tests HistoryIdStore, MessageIdStore, and RefreshTokenStore.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("Store Classes Tests")
class StoresTest {

    @Mock
    private KeyValueStore mockKeyValueStore;

    @Nested
    @DisplayName("HistoryIdStore Tests")
    class HistoryIdStoreTests {

        private HistoryIdStore historyIdStore;

        @BeforeEach
        void setUp() {
            historyIdStore = new HistoryIdStore(mockKeyValueStore);
        }

        @Test
        @DisplayName("Should store history ID successfully")
        void testPut_ShouldStoreHistoryId() {
            // Given
            String key = "user123";
            String historyId = "12345";

            // When
            historyIdStore.put(key, historyId);

            // Then
            verify(mockKeyValueStore).put(key, historyId);
        }

        @Test
        @DisplayName("Should retrieve history ID as BigInteger")
        void testGet_ShouldRetrieveHistoryIdAsBigInteger() {
            // Given
            String key = "user123";
            String storedValue = "98765";
            when(mockKeyValueStore.get(key)).thenReturn(storedValue);

            // When
            BigInteger result = historyIdStore.get(key);

            // Then
            assertThat(result).isEqualTo(new BigInteger("98765"));
            verify(mockKeyValueStore).get(key);
        }

        @Test
        @DisplayName("Should remove history ID successfully")
        void testRemove_ShouldRemoveHistoryId() {
            // Given
            String key = "user123";

            // When
            historyIdStore.remove(key);

            // Then
            verify(mockKeyValueStore).remove(key);
        }

        @Test
        @DisplayName("Should retrieve all keys")
        void testGetKeys_ShouldReturnAllKeys() {
            // Given
            Set<String> expectedKeys = Set.of("key1", "key2", "key3");
            when(mockKeyValueStore.getKeys()).thenReturn(expectedKeys);

            // When
            Set<String> result = historyIdStore.getKeys();

            // Then
            assertThat(result).isEqualTo(expectedKeys);
            assertThat(result).hasSize(3);
            verify(mockKeyValueStore).getKeys();
        }

        @Test
        @DisplayName("Should handle large history ID values")
        void testGet_WithLargeHistoryId_ShouldHandleCorrectly() {
            // Given
            String key = "user123";
            String largeHistoryId = "999999999999999999999";
            when(mockKeyValueStore.get(key)).thenReturn(largeHistoryId);

            // When
            BigInteger result = historyIdStore.get(key);

            // Then
            assertThat(result).isEqualTo(new BigInteger(largeHistoryId));
        }
    }

    @Nested
    @DisplayName("MessageIdStore Tests")
    class MessageIdStoreTests {

        private MessageIdStore messageIdStore;

        @BeforeEach
        void setUp() {
            messageIdStore = new MessageIdStore(mockKeyValueStore);
        }

        @Test
        @DisplayName("Should store message ID successfully")
        void testPut_ShouldStoreMessageId() {
            // Given
            String key = "msg-key-1";
            String messageId = "msg-12345";

            // When
            messageIdStore.put(key, messageId);

            // Then
            verify(mockKeyValueStore).put(key, messageId);
        }

        @Test
        @DisplayName("Should retrieve message ID successfully")
        void testGet_ShouldRetrieveMessageId() {
            // Given
            String key = "msg-key-1";
            String expectedMessageId = "msg-67890";
            when(mockKeyValueStore.get(key)).thenReturn(expectedMessageId);

            // When
            String result = messageIdStore.get(key);

            // Then
            assertThat(result).isEqualTo(expectedMessageId);
            verify(mockKeyValueStore).get(key);
        }

        @Test
        @DisplayName("Should return null when message ID not found")
        void testGet_WhenNotFound_ShouldReturnNull() {
            // Given
            String key = "non-existent-key";
            when(mockKeyValueStore.get(key)).thenReturn(null);

            // When
            String result = messageIdStore.get(key);

            // Then
            assertThat(result).isNull();
            verify(mockKeyValueStore).get(key);
        }

        @Test
        @DisplayName("Should remove message ID successfully")
        void testRemove_ShouldRemoveMessageId() {
            // Given
            String key = "msg-key-1";

            // When
            messageIdStore.remove(key);

            // Then
            verify(mockKeyValueStore).remove(key);
        }

        @Test
        @DisplayName("Should retrieve all keys")
        void testGetKeys_ShouldReturnAllKeys() {
            // Given
            Set<String> expectedKeys = Set.of("msg1", "msg2", "msg3", "msg4");
            when(mockKeyValueStore.getKeys()).thenReturn(expectedKeys);

            // When
            Set<String> result = messageIdStore.getKeys();

            // Then
            assertThat(result).isEqualTo(expectedKeys);
            assertThat(result).hasSize(4);
            verify(mockKeyValueStore).getKeys();
        }

        @Test
        @DisplayName("Should handle empty key set")
        void testGetKeys_WhenEmpty_ShouldReturnEmptySet() {
            // Given
            Set<String> emptySet = Set.of();
            when(mockKeyValueStore.getKeys()).thenReturn(emptySet);

            // When
            Set<String> result = messageIdStore.getKeys();

            // Then
            assertThat(result).isEmpty();
            verify(mockKeyValueStore).getKeys();
        }
    }

    @Nested
    @DisplayName("RefreshTokenStore Tests")
    class RefreshTokenStoreTests {

        private RefreshTokenStore refreshTokenStore;

        @BeforeEach
        void setUp() {
            refreshTokenStore = new RefreshTokenStore(mockKeyValueStore);
        }

        @Test
        @DisplayName("Should store refresh token successfully")
        void testPut_ShouldStoreRefreshToken() {
            // Given
            String key = "user@example.com";
            String refreshToken = "1//abc123xyz789";

            // When
            refreshTokenStore.put(key, refreshToken);

            // Then
            verify(mockKeyValueStore).put(key, refreshToken);
        }

        @Test
        @DisplayName("Should retrieve refresh token successfully")
        void testGet_ShouldRetrieveRefreshToken() {
            // Given
            String key = "user@example.com";
            String expectedToken = "1//def456uvw012";
            when(mockKeyValueStore.get(key)).thenReturn(expectedToken);

            // When
            String result = refreshTokenStore.get(key);

            // Then
            assertThat(result).isEqualTo(expectedToken);
            verify(mockKeyValueStore).get(key);
        }

        @Test
        @DisplayName("Should return null when refresh token not found")
        void testGet_WhenNotFound_ShouldReturnNull() {
            // Given
            String key = "unknown@example.com";
            when(mockKeyValueStore.get(key)).thenReturn(null);

            // When
            String result = refreshTokenStore.get(key);

            // Then
            assertThat(result).isNull();
            verify(mockKeyValueStore).get(key);
        }

        @Test
        @DisplayName("Should remove refresh token successfully")
        void testRemove_ShouldRemoveRefreshToken() {
            // Given
            String key = "user@example.com";

            // When
            refreshTokenStore.remove(key);

            // Then
            verify(mockKeyValueStore).remove(key);
        }

        @Test
        @DisplayName("Should handle multiple put operations for same key")
        void testPut_MultipleTimesForSameKey_ShouldUpdateToken() {
            // Given
            String key = "user@example.com";
            String token1 = "1//old-token";
            String token2 = "1//new-token";

            // When
            refreshTokenStore.put(key, token1);
            refreshTokenStore.put(key, token2);

            // Then
            verify(mockKeyValueStore, times(2)).put(eq(key), anyString());
            verify(mockKeyValueStore).put(key, token1);
            verify(mockKeyValueStore).put(key, token2);
        }

        @Test
        @DisplayName("Should handle long refresh tokens")
        void testPut_WithLongToken_ShouldStoreSuccessfully() {
            // Given
            String key = "user@example.com";
            String longToken = "1//" + "a".repeat(500);

            // When
            refreshTokenStore.put(key, longToken);

            // Then
            verify(mockKeyValueStore).put(key, longToken);
        }
    }
}

