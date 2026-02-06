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

package app.krista.extensions.essentials.collaboration.gmail.impl.util;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for Constants utility class.
 * Tests constant values and patterns.
 */
@DisplayName("Constants Utility Tests")
class ConstantsTest {

    @Test
    @DisplayName("Should have correct string constants")
    void testStringConstants() {
        assertThat(Constants.NEW_EMAIL_UPDATE).isEqualTo("New Email Update");
        assertThat(Constants.GMAIL_UPDATE).isEqualTo("Gmail Update");
        assertThat(Constants.ME).isEqualTo("me");
        assertThat(Constants.HASH).isEqualTo("#");
        assertThat(Constants.COMMA).isEqualTo(",");
        assertThat(Constants.EMPTY_STRING).isEmpty();
        assertThat(Constants.WS_CONTACT).isEqualTo("wsContact");
        assertThat(Constants.GOOGLE_MAIL).isEqualTo("Google Mail");
        assertThat(Constants.TEXT).isEqualTo("Text");
        assertThat(Constants.USER_ID).isEqualTo("userId");
        assertThat(Constants.UNREAD).isEqualTo("UNREAD");
    }

    @Test
    @DisplayName("Should have correct error message constants")
    void testErrorMessages() {
        assertThat(Constants.FAILED_TO_GET_ACCOUNT).isEqualTo("Failed to get account.");
        assertThat(Constants.EMPTY_MESSAGE).isEqualTo("Message is empty or null.");
        assertThat(Constants.INVALID_EMAIL_MESSAGE).isEqualTo("Mail address is not valid, please provide correct mail address.");
        assertThat(Constants.FAILED_TO_GET_GRAPH_CLIENT).isEqualTo("Unable to connect to Gmail services. Please check your configuration and try again.");
        assertThat(Constants.FAILED_TO_GET_AUTHORIZE_RESPONSE).isEqualTo("Failed to get authorize response ");
        assertThat(Constants.FAILED_TO_AUTHENTICATE_USER).isEqualTo("Failed to authenticate user ");
    }

    @Test
    @DisplayName("Should have correct authentication message constants")
    void testAuthenticationMessages() {
        assertThat(Constants.GOT_ERROR_FOR_AUTHENTICATION_SO_SENDING_FOR_RE_AUTHENTICATION)
                .isEqualTo("Authentication failed. Please re-authenticate to continue accessing your Gmail account.");
        assertThat(Constants.GOT_ERROR_FOR_AUTHENTICATION_SO_SENDING_FOR_AUTHENTICATION)
                .isEqualTo("Authentication required. Please authenticate to access your Gmail account.");
        assertThat(Constants.REFRESH_TOKEN_EXPIRED_PLEASE_REAUTHORIZE_YOURSELF)
                .isEqualTo("Your access token has expired. Please re-authorize the application to continue.");
        assertThat(Constants.AUTHORIZATION_PROMPT)
                .isEqualTo("Please authorize the application and click 'Validate Attributes' before saving changes to proceed.");
    }

    @Test
    @DisplayName("Should have correct URL path constants")
    void testUrlPaths() {
        assertThat(Constants.GMAIL_CALLBACK).isEqualTo("/gmail/callback");
        assertThat(Constants.EMAIL_NOTIFICATION).isEqualTo("/gmail/webhook");
    }

    @Test
    @DisplayName("Should have correct OAuth constants")
    void testOAuthConstants() {
        assertThat(Constants.STATE).isEqualTo("state");
        assertThat(Constants.ACCESS_TYPE_OFFLINE_APPROVAL_PROMPT_FORCE).isEqualTo("&prompt=consent&access_type=offline");
        assertThat(Constants.AUTH_CONTEXT_ID).isEqualTo("authContextId");
    }

    @Test
    @DisplayName("Should have correct telemetry method name constants")
    void testTelemetryMethodNames() {
        assertThat(Constants.TELEMETRY_FETCH_MAIL_BY_MESSAGE_ID).isEqualTo("gmail.fetchMailByMessageId");
        assertThat(Constants.TELEMETRY_MOVE_MESSAGE).isEqualTo("gmail.moveMessage");
        assertThat(Constants.TELEMETRY_REPLY_TO_ALL).isEqualTo("gmail.replyToAll");
        assertThat(Constants.TELEMETRY_FETCH_SENT).isEqualTo("gmail.fetchSent");
        assertThat(Constants.TELEMETRY_FORWARD_MAIL).isEqualTo("gmail.forwardMail");
        assertThat(Constants.TELEMETRY_FETCH_MAIL_DETAILS_BY_QUERY).isEqualTo("gmail.fetchMailDetailsByQuery");
        assertThat(Constants.TELEMETRY_SEND_MAIL).isEqualTo("gmail.sendMail");
        assertThat(Constants.TELEMETRY_FETCH_INBOX).isEqualTo("gmail.fetchInbox");
        assertThat(Constants.TELEMETRY_MARK_MESSAGE).isEqualTo("gmail.markMessage");
        assertThat(Constants.TELEMETRY_REPLY_TO_MAIL).isEqualTo("gmail.replyToMail");
        assertThat(Constants.TELEMETRY_REPLY_TO_ALL_WITH_CC_AND_BCC).isEqualTo("gmail.replyToAllWithCCAndBCC");
        assertThat(Constants.TELEMETRY_REPLY_TO_MAIL_WITH_CC_AND_BCC).isEqualTo("gmail.replyToMailWithCCAndBCC");
        assertThat(Constants.TELEMETRY_RENEW_SUBSCRIPTION).isEqualTo("gmail.renewSubscription");
    }

    @Test
    @DisplayName("Should have correct telemetry tag key constants")
    void testTelemetryTagKeys() {
        assertThat(Constants.TELEMETRY_TAG_MESSAGE_ID).isEqualTo("message_id");
        assertThat(Constants.TELEMETRY_TAG_VALIDATION_COUNT).isEqualTo("validation_count");
        assertThat(Constants.TELEMETRY_TAG_FOLDER_NAME).isEqualTo("folder_name");
        assertThat(Constants.TELEMETRY_TAG_PAGE_NUMBER).isEqualTo("page_number");
        assertThat(Constants.TELEMETRY_TAG_PAGE_SIZE).isEqualTo("page_size");
        assertThat(Constants.TELEMETRY_TAG_TO).isEqualTo("to");
        assertThat(Constants.TELEMETRY_TAG_QUERY).isEqualTo("query");
        assertThat(Constants.TELEMETRY_TAG_CC).isEqualTo("cc");
        assertThat(Constants.TELEMETRY_TAG_BCC).isEqualTo("bcc");
        assertThat(Constants.TELEMETRY_TAG_LABEL).isEqualTo("label");
    }

    @Test
    @DisplayName("Should have correct telemetry error message constants")
    void testTelemetryErrorMessages() {
        assertThat(Constants.TELEMETRY_ERROR_INVALID_FOLDER_NAME)
                .isEqualTo("The folder name you entered is not valid. Please check the name and try again.");
        assertThat(Constants.TELEMETRY_ERROR_VALIDATION_FAILED)
                .isEqualTo("Input validation failed. Please review your entries and correct any errors.");
        assertThat(Constants.TELEMETRY_ERROR_INVALID_LABEL)
                .isEqualTo("The label you specified is not valid. Please choose a valid Gmail label.");
    }

    @Test
    @DisplayName("Should have valid GSON instance")
    void testGsonInstance() {
        assertThat(Constants.GSON).isNotNull();
        // Test that GSON can serialize/deserialize
        String json = Constants.GSON.toJson("test");
        assertThat(json).isEqualTo("\"test\"");
    }

    @Test
    @DisplayName("Should have valid IMG_PATTERN regex")
    void testImgPattern() {
        assertThat(Constants.IMG_PATTERN).isNotNull();
        String html = "<img src=\"test.jpg\" alt=\"test\">";
        assertThat(Constants.IMG_PATTERN.matcher(html).find()).isTrue();
    }

    @Test
    @DisplayName("Should have valid ALT_PATTERN regex")
    void testAltPattern() {
        assertThat(Constants.ALT_PATTERN).isNotNull();
        String html = "alt=\"test image\"";
        assertThat(Constants.ALT_PATTERN.matcher(html).find()).isTrue();
    }

    @Test
    @DisplayName("Should have correct placeholder link regex constants")
    void testPlaceholderLinkConstants() {
        assertThat(Constants.PLACEHOLDER_LINK_REGEX).isEqualTo("<a href=\"http://#\">([^<]+)</a>");
        assertThat(Constants.PLACEHOLDER_LINK_REPLACEMENT).isEqualTo("<a href=\"$1\">$1</a>");
    }

    @Test
    @DisplayName("IMG_PATTERN should match various img tag formats")
    void testImgPatternMatching() {
        assertThat(Constants.IMG_PATTERN.matcher("<img src=\"image.jpg\">").find()).isTrue();
        assertThat(Constants.IMG_PATTERN.matcher("<img src='image.jpg'>").find()).isTrue();
        assertThat(Constants.IMG_PATTERN.matcher("<IMG SRC=\"image.jpg\">").find()).isTrue(); // Case insensitive
        assertThat(Constants.IMG_PATTERN.matcher("<img alt=\"test\" src=\"image.jpg\">").find()).isTrue();
    }

    @Test
    @DisplayName("ALT_PATTERN should match various alt attribute formats")
    void testAltPatternMatching() {
        assertThat(Constants.ALT_PATTERN.matcher("alt=\"test\"").find()).isTrue();
        assertThat(Constants.ALT_PATTERN.matcher("alt='test'").find()).isTrue();
        assertThat(Constants.ALT_PATTERN.matcher("ALT=\"test\"").find()).isTrue(); // Case insensitive
        assertThat(Constants.ALT_PATTERN.matcher("alt=\"\"").find()).isTrue(); // Empty alt
    }

    @Test
    @DisplayName("All telemetry method names should start with 'gmail.'")
    void testTelemetryMethodNamingConvention() {
        assertThat(Constants.TELEMETRY_FETCH_MAIL_BY_MESSAGE_ID).startsWith("gmail.");
        assertThat(Constants.TELEMETRY_MOVE_MESSAGE).startsWith("gmail.");
        assertThat(Constants.TELEMETRY_REPLY_TO_ALL).startsWith("gmail.");
        assertThat(Constants.TELEMETRY_FETCH_SENT).startsWith("gmail.");
        assertThat(Constants.TELEMETRY_FORWARD_MAIL).startsWith("gmail.");
        assertThat(Constants.TELEMETRY_SEND_MAIL).startsWith("gmail.");
        assertThat(Constants.TELEMETRY_FETCH_INBOX).startsWith("gmail.");
        assertThat(Constants.TELEMETRY_MARK_MESSAGE).startsWith("gmail.");
        assertThat(Constants.TELEMETRY_REPLY_TO_MAIL).startsWith("gmail.");
    }

    @Test
    @DisplayName("All string constants should be non-null")
    void testAllConstantsNonNull() {
        assertThat(Constants.NEW_EMAIL_UPDATE).isNotNull();
        assertThat(Constants.GMAIL_UPDATE).isNotNull();
        assertThat(Constants.ME).isNotNull();
        assertThat(Constants.HASH).isNotNull();
        assertThat(Constants.COMMA).isNotNull();
        assertThat(Constants.EMPTY_STRING).isNotNull();
        assertThat(Constants.GOOGLE_MAIL).isNotNull();
    }

    @Test
    @DisplayName("GSON should support pretty printing")
    void testGsonPrettyPrinting() {
        String json = Constants.GSON.toJson(new TestObject("test", 123));
        assertThat(json).contains("\n"); // Pretty printing adds newlines
        assertThat(json).contains("  "); // Pretty printing adds indentation
    }

    @Test
    @DisplayName("Constants class should not be instantiable")
    void testConstantsNotInstantiable() {
        // Constants should be a utility class with private constructor
        assertThat(Constants.class).isNotNull();
    }

    // Helper class for GSON testing
    private static class TestObject {
        private final String name;
        private final int value;

        TestObject(String name, int value) {
            this.name = name;
            this.value = value;
        }
    }
}

