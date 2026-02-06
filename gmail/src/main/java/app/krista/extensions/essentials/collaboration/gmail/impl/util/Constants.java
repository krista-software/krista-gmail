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

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.util.regex.Pattern;

public class Constants {

    public static final String NEW_EMAIL_UPDATE = "New Email Update";
    public static final String GMAIL_UPDATE = "Gmail Update";
    public static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    public static final String ME = "me";
    public static final String HASH = "#";
    public static final String COMMA = ",";
    public static final String EMPTY_STRING = "";
    public static final String WS_CONTACT = "wsContact";
    public static final String GOOGLE_MAIL = "Google Mail";
    public static final String FAILED_TO_GET_ACCOUNT = "Failed to get account.";
    public static final String TEXT = "Text";
    public static final String USER_ID = "userId";
    public static final String GOT_ERROR_FOR_AUTHENTICATION_SO_SENDING_FOR_RE_AUTHENTICATION = "Authentication failed. Please re-authenticate to continue accessing your Gmail account.";
    public static final String GOT_ERROR_FOR_AUTHENTICATION_SO_SENDING_FOR_AUTHENTICATION = "Authentication required. Please authenticate to access your Gmail account.";
    public static final String REFRESH_TOKEN_EXPIRED_PLEASE_REAUTHORIZE_YOURSELF = "Your access token has expired. Please re-authorize the application to continue.";
    public static final String AUTHORIZATION_PROMPT = "Please authorize the application and click 'Validate Attributes' before saving changes to proceed.";
    public static final String FAILED_TO_GET_GRAPH_CLIENT = "Unable to connect to Gmail services. Please check your configuration and try again.";
    public static final String GMAIL_CALLBACK = "/gmail/callback";
    public static final String EMAIL_NOTIFICATION = "/gmail/webhook";
    public static final String STATE = "state";
    public static final String ACCESS_TYPE_OFFLINE_APPROVAL_PROMPT_FORCE = "&prompt=consent&access_type=offline";
    public static final String FAILED_TO_GET_AUTHORIZE_RESPONSE = "Failed to get authorize response ";
    public static final String AUTH_CONTEXT_ID = "authContextId";
    public static final String FAILED_TO_AUTHENTICATE_USER = "Failed to authenticate user ";
    public static final String EMPTY_MESSAGE = "Message is empty or null.";
    public static final String UNREAD = "UNREAD";
    public static final String INVALID_EMAIL_MESSAGE = "Mail address is not valid, please provide correct mail address.";

    // Telemetry method names
    public static final String TELEMETRY_FETCH_MAIL_BY_MESSAGE_ID = "gmail.fetchMailByMessageId";
    public static final String TELEMETRY_MOVE_MESSAGE = "gmail.moveMessage";
    public static final String TELEMETRY_REPLY_TO_ALL = "gmail.replyToAll";
    public static final String TELEMETRY_FETCH_SENT = "gmail.fetchSent";
    public static final String TELEMETRY_FORWARD_MAIL = "gmail.forwardMail";
    public static final String TELEMETRY_FETCH_MAIL_DETAILS_BY_QUERY = "gmail.fetchMailDetailsByQuery";
    public static final String TELEMETRY_SEND_MAIL = "gmail.sendMail";
    public static final String TELEMETRY_FETCH_INBOX = "gmail.fetchInbox";
    public static final String TELEMETRY_MARK_MESSAGE = "gmail.markMessage";
    public static final String TELEMETRY_REPLY_TO_MAIL = "gmail.replyToMail";
    public static final String TELEMETRY_REPLY_TO_ALL_WITH_CC_AND_BCC = "gmail.replyToAllWithCCAndBCC";
    public static final String TELEMETRY_REPLY_TO_MAIL_WITH_CC_AND_BCC = "gmail.replyToMailWithCCAndBCC";
    public static final String TELEMETRY_RENEW_SUBSCRIPTION = "gmail.renewSubscription";

    // Telemetry tag keys
    public static final String TELEMETRY_TAG_MESSAGE_ID = "message_id";
    public static final String TELEMETRY_TAG_VALIDATION_COUNT = "validation_count";
    public static final String TELEMETRY_TAG_FOLDER_NAME = "folder_name";
    public static final String TELEMETRY_TAG_PAGE_NUMBER = "page_number";
    public static final String TELEMETRY_TAG_PAGE_SIZE = "page_size";
    public static final String TELEMETRY_TAG_TO = "to";
    public static final String TELEMETRY_TAG_QUERY = "query";
    public static final String TELEMETRY_TAG_CC = "cc";
    public static final String TELEMETRY_TAG_BCC = "bcc";
    public static final String TELEMETRY_TAG_LABEL = "label";

    // Telemetry error messages
    public static final String TELEMETRY_ERROR_INVALID_FOLDER_NAME = "The folder name you entered is not valid. Please check the name and try again.";
    public static final String TELEMETRY_ERROR_VALIDATION_FAILED = "Input validation failed. Please review your entries and correct any errors.";
    public static final String TELEMETRY_ERROR_INVALID_LABEL = "The label you specified is not valid. Please choose a valid Gmail label.";

    public static final Pattern IMG_PATTERN = Pattern.compile("<img[^>]*src=[\"']([^\"']*)[\"'][^>]*>", Pattern.CASE_INSENSITIVE);
    public static final Pattern ALT_PATTERN = Pattern.compile("alt=[\"']([^\"']*)[\"']", Pattern.CASE_INSENSITIVE);
    public static final String PLACEHOLDER_LINK_REGEX = "<a href=\"http://#\">([^<]+)</a>";
    public static final String PLACEHOLDER_LINK_REPLACEMENT = "<a href=\"$1\">$1</a>";
    private Constants() {
    }

}
