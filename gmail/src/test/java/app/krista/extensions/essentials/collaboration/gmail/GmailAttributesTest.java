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

package app.krista.extensions.essentials.collaboration.gmail;

import app.krista.extension.executor.Invoker;
import app.krista.extension.request.RoutingInfo;
import app.krista.extension.request.protos.http.HttpProtocol;
import com.github.scribejava.core.oauth.OAuth20Service;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

/**
 * Comprehensive test suite for GmailAttributes to achieve high code coverage.
 * Tests configuration management, OAuth service creation, and attribute handling.
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class GmailAttributesTest {

    @Mock
    private Invoker mockInvoker;

    @Mock
    private RoutingInfo mockRoutingInfo;

    private GmailAttributes gmailAttributes;
    private Map<String, Object> testAttributes;

    @BeforeEach
    void setUp() {
        testAttributes = new HashMap<>();
        testAttributes.put(GmailAttributes.CLIENT_ID, "test-client-id");
        testAttributes.put(GmailAttributes.CLIENT_SECRET, "test-client-secret");
        testAttributes.put(GmailAttributes.EMAIL, "test@example.com");
        testAttributes.put(GmailAttributes.TOPIC_KEY, "projects/test-project/topics/test-topic");
        testAttributes.put(GmailAttributes.ALERT_KEY, true);

        // Mock invoker methods
        when(mockInvoker.getAttributes()).thenReturn(testAttributes);
        when(mockInvoker.getRoutingInfo()).thenReturn(mockRoutingInfo);
        when(mockRoutingInfo.getRoutingURL(HttpProtocol.PROTOCOL_NAME, RoutingInfo.Type.APPLIANCE))
                .thenReturn("http://test-routing-url.com");

        gmailAttributes = new GmailAttributes(mockInvoker);
    }

    @Test
    void testConstructor_WithInvoker_ShouldInitializeCorrectly() {
        // Given - constructor called in setUp()
        
        // Then
        assertThat(gmailAttributes).isNotNull();
        assertThat(gmailAttributes.getClientId()).isEqualTo("test-client-id");
        assertThat(gmailAttributes.getClientSecret()).isEqualTo("test-client-secret");
        assertThat(gmailAttributes.getMailId()).isEqualTo("test@example.com");
        assertThat(gmailAttributes.getTopic()).isEqualTo("projects/test-project/topics/test-topic");
        assertThat(gmailAttributes.getAlert()).isEqualTo(true);
    }

    @Test
    void testCreate_WithValidAttributes_ShouldReturnGmailAttributes() {
        // Given
        Map<String, Object> attributes = new HashMap<>();
        attributes.put(GmailAttributes.CLIENT_ID, "create-client-id");
        attributes.put(GmailAttributes.CLIENT_SECRET, "create-client-secret");
        attributes.put(GmailAttributes.EMAIL, "create@example.com");

        // When
        GmailAttributes result = GmailAttributes.create(mockInvoker, attributes);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getClientId()).isEqualTo("create-client-id");
        assertThat(result.getClientSecret()).isEqualTo("create-client-secret");
        assertThat(result.getMailId()).isEqualTo("create@example.com");
    }

    @Test
    void testGetClientId_WithValidAttribute_ShouldReturnValue() {
        // When
        String clientId = gmailAttributes.getClientId();

        // Then
        assertThat(clientId).isEqualTo("test-client-id");
    }

    @Test
    void testGetClientId_WithNullAttribute_ShouldReturnNull() {
        // Given
        when(mockInvoker.getAttributes()).thenReturn(Map.of());
        GmailAttributes attributes = new GmailAttributes(mockInvoker);

        // When
        String clientId = attributes.getClientId();

        // Then
        assertThat(clientId).isNull();
    }

    @Test
    void testGetClientSecret_WithValidAttribute_ShouldReturnValue() {
        // When
        String clientSecret = gmailAttributes.getClientSecret();

        // Then
        assertThat(clientSecret).isEqualTo("test-client-secret");
    }

    @Test
    void testGetEmail_WithValidAttribute_ShouldReturnValue() {
        // When
        String email = gmailAttributes.getMailId();

        // Then
        assertThat(email).isEqualTo("test@example.com");
    }

    @Test
    void testGetTopic_WithValidAttribute_ShouldReturnValue() {
        // When
        String topic = gmailAttributes.getTopic();

        // Then
        assertThat(topic).isEqualTo("projects/test-project/topics/test-topic");
    }

    @Test
    void testGetAlert_WithValidAttribute_ShouldReturnValue() {
        // When
        Boolean alert = gmailAttributes.getAlert();

        // Then
        assertThat(alert).isEqualTo(true);
    }

    @Test
    void testGetOAuth20Service_WithValidCredentials_ShouldReturnOAuth20Service() {
        // When
        OAuth20Service oAuthService = gmailAttributes.getOAuth20Service();

        // Then
        assertThat(oAuthService).isNotNull();
    }

    @Test
    void testGetOAuth20Service_WithNullClientId_ShouldThrowException() {
        // Given
        Map<String, Object> nullAttributes = new HashMap<>();
        nullAttributes.put(GmailAttributes.CLIENT_ID, null);
        nullAttributes.put(GmailAttributes.CLIENT_SECRET, "test-secret");
        nullAttributes.put(GmailAttributes.EMAIL, "test@example.com");

        GmailAttributes attributes = new GmailAttributes("http://test.com", nullAttributes);

        // When & Then
        assertThatThrownBy(() -> attributes.getOAuth20Service())
                .isInstanceOf(RuntimeException.class);
    }

    @Test
    void testGetOAuth20Service_CalledMultipleTimes_ShouldReturnSameInstance() {
        // When
        OAuth20Service service1 = gmailAttributes.getOAuth20Service();
        OAuth20Service service2 = gmailAttributes.getOAuth20Service();

        // Then
        assertThat(service1).isSameAs(service2);
    }
}
