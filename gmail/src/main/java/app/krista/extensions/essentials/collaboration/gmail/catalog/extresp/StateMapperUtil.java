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

package app.krista.extensions.essentials.collaboration.gmail.catalog.extresp;

import app.krista.extensions.essentials.collaboration.gmail.resources.GmailResources;
import app.krista.model.base.File;

import javax.validation.constraints.NotNull;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Utility class for creating metadata maps for Gmail operations.
 * Provides static methods to build standardized metadata structures for different email actions.
 */
public class StateMapperUtil {

    private StateMapperUtil() {
    }

    /**
     * Creates metadata map for reply-to-all operation with all email fields.
     */
    @NotNull
    public static Map<String, Object> addReplyToALLFieldsMetaToMap(String messageId, String to, String cc, String bcc, String replyTo, String message, List<File> attachments, String bodyType, String stateId) {
        return MetadataBuilder.create()
                .stateId(stateId)
                .messageId(messageId)
                .to(to)
                .cc(cc)
                .bcc(bcc)
                .replyTo(replyTo)
                .message(message)
                .attachments(attachments)
                .bodyType(bodyType)
                .build();
    }

    /**
     * Creates metadata map for basic reply-to-all operation.
     */
    @NotNull
    public static Map<String, Object> addReplyToALLMetaToMap(String messageId, String message, List<File> attachments, String bodyType, String stateId) {
        return MetadataBuilder.create()
                .stateId(stateId)
                .messageId(messageId)
                .message(message)
                .attachments(attachments)
                .bodyType(bodyType)
                .build();
    }

    /**
     * Creates metadata map for email forwarding operation.
     */
    @NotNull
    public static Map<String, Object> addForwardMailMetaToMap(String messageId, String message, String to, String bodyType, String stateId) {
        return MetadataBuilder.create()
                .stateId(stateId)
                .messageId(messageId)
                .message(message)
                .to(to)
                .bodyType(bodyType)
                .build();
    }

    /**
     * Creates metadata map for sending new email.
     */
    public static Map<String, Object> addSendMailMetaToMap(String subject, String to, String cc, String bcc, String replyTo, String message, List<File> attachments, String bodyType, String stateId) {
        return MetadataBuilder.create()
                .stateId(stateId)
                .subject(subject)
                .message(message)
                .attachments(attachments)
                .to(to)
                .bcc(bcc)
                .cc(cc)
                .replyTo(replyTo)
                .bodyType(bodyType)
                .build();
    }

    /**
     * Builder class for creating metadata maps with fluent API.
     */
    public static class MetadataBuilder {
        private final Map<String, Object> metaData = new LinkedHashMap<>();

        private MetadataBuilder() {
        }

        public static MetadataBuilder create() {
            return new MetadataBuilder();
        }

        public MetadataBuilder stateId(String stateId) {
            if (stateId != null) {
                metaData.put(GmailResources.STATE_ID, stateId);
            }
            return this;
        }

        public MetadataBuilder messageId(String messageId) {
            if (messageId != null) {
                metaData.put(GmailResources.MESSAGE_ID, messageId);
            }
            return this;
        }

        public MetadataBuilder subject(String subject) {
            if (subject != null) {
                metaData.put(GmailResources.SUBJECT, subject);
            }
            return this;
        }

        public MetadataBuilder message(String message) {
            if (message != null) {
                metaData.put(GmailResources.MESSAGE, message);
            }
            return this;
        }

        public MetadataBuilder to(String to) {
            if (to != null) {
                metaData.put(GmailResources.TO, to);
            }
            return this;
        }

        public MetadataBuilder cc(String cc) {
            if (cc != null) {
                metaData.put(GmailResources.CC, cc);
            }
            return this;
        }

        public MetadataBuilder bcc(String bcc) {
            if (bcc != null) {
                metaData.put(GmailResources.BCC, bcc);
            }
            return this;
        }

        public MetadataBuilder replyTo(String replyTo) {
            if (replyTo != null) {
                metaData.put(GmailResources.REPLY_TO, replyTo);
            }
            return this;
        }

        public MetadataBuilder attachments(List<File> attachments) {
            if (attachments != null) {
                metaData.put(GmailResources.ATTACHMENTS, attachments);
            }
            return this;
        }

        public MetadataBuilder bodyType(String bodyType) {
            if (bodyType != null) {
                metaData.put(GmailResources.BODY_TYPE, bodyType);
            }
            return this;
        }

        public Map<String, Object> build() {
            return new LinkedHashMap<>(metaData);
        }
    }
}