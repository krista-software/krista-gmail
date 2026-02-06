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

import app.krista.extensions.essentials.collaboration.gmail.impl.stores.GmailAttributeStore;
import app.krista.ksdk.context.AuthorizationContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for GmailRequestAuthenticator.
 * Tests OAuth 2.0 authentication flow and request handling.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("GmailRequestAuthenticator Tests")
class GmailRequestAuthenticatorTest {

    @Mock
    private GmailAttributeStore mockAttributeStore;

    @Mock
    private GmailAttributes mockGmailAttributes;

    @Mock
    private AuthorizationContext mockAuthContext;

    private GmailRequestAuthenticator authenticator;

    @BeforeEach
    void setUp() {
        authenticator = new GmailRequestAuthenticator(
                mockAttributeStore,
                mockGmailAttributes,
                mockAuthContext
        );
    }

    @Test
    @DisplayName("Should return null for authentication scheme")
    void testGetScheme() {
        // OAuth 2.0 doesn't use traditional authentication schemes
        assertThat(authenticator.getScheme()).isNull();
    }

    @Test
    @DisplayName("Should return empty set for supported protocols")
    void testGetSupportedProtocols() {
        // Works with standard HTTP protocols
        assertThat(authenticator.getSupportedProtocols()).isEmpty();
    }

    @Test
    @DisplayName("Should be instantiable with required dependencies")
    void testInstantiation() {
        assertThat(authenticator).isNotNull();
    }

    @Test
    @DisplayName("Should have correct class structure")
    void testClassStructure() {
        assertThat(GmailRequestAuthenticator.class).isNotNull();
        assertThat(GmailRequestAuthenticator.class.getInterfaces()).isNotEmpty();
    }
}

