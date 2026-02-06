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
import app.krista.extensions.essentials.collaboration.gmail.impl.util.Constants;
import app.krista.extensions.essentials.collaboration.gmail.service.Attachment;
import com.google.api.services.gmail.model.MessagePart;
import com.google.api.services.gmail.model.MessagePartBody;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * Implementation of the Attachment interface for Gmail email attachments.
 * Handles downloading and processing of email attachments from Gmail API.
 * Manages attachment metadata, content retrieval, and file conversion between
 * Gmail's attachment format and Krista's file system.
 * <p>
 * This class provides functionality to download attachments from Gmail messages
 * and convert them to Krista File objects for use within the platform.
 */
public class AttachmentImpl implements Attachment {

    private static final Logger LOGGER = LoggerFactory.getLogger(AttachmentImpl.class);
    private final GmailProvider provider;
    private final String messageId;
    private final MessagePart messagePart;


    public AttachmentImpl(GmailProvider provider, MessagePart messagePart, String messageId) {
        this.provider = provider;
        this.messagePart = messagePart;
        this.messageId = messageId;
    }

    private static String getFileExtension(String file) {
        return file.substring(file.lastIndexOf('.') + 1);
    }

    @Override
    public String getName() {
        return messagePart.getFilename();
    }

    public String getId() {
        return messagePart.getBody().getAttachmentId();
    }

    @Override
    public String getMimeType() {
        return messagePart.getBody().getData();
    }

    /**
     * Gets the size of the attachment in bytes.
     * Returns the attachment size as reported by Gmail API.
     *
     * @return the size of the attachment in bytes, or null if not available
     */
    @Override
    public int getSize() {
        return messagePart.getBody().getSize();
    }

    /**
     * Downloads the attachment content and converts it to a Krista File object.
     * Retrieves the attachment data from Gmail API, creates a temporary file,
     * and uploads it to Krista's media server for platform integration.
     *
     * @param kristaMediaClient the client for uploading files to Krista's media server
     * @return Krista File object representing the downloaded attachment, or null if download fails
     */
    @Override
    public app.krista.model.base.File download(KristaMediaClient kristaMediaClient) {
        MessagePartBody execute = null;
        if (messagePart.getBody().getAttachmentId() == null || messagePart.getBody().isEmpty()) return null;
        try {
            execute = provider.getGmailClient().users().messages().attachments().get(Constants.ME, messageId, messagePart.getBody().getAttachmentId()).execute();
            byte[] bytes = execute.decodeData();
            if (bytes == null) return null;
            File fileToUpload = getFileFromBytes(getName(), bytes);
            app.krista.model.base.File kristaFile = null;
            kristaFile = kristaMediaClient.toKristaFile(fileToUpload);
            return kristaFile;
        } catch (IOException cause) {
            LOGGER.error("Error occurred while downloading file {} ", cause.getMessage());
            throw new IllegalStateException(cause);
        }
    }

    /**
     * Creates a temporary file from byte array data.
     * Writes the attachment bytes to a temporary file for processing and upload.
     *
     * @param fileName the name to use for the temporary file
     * @param bytes    the attachment content as byte array
     * @return File object representing the temporary file with attachment content
     */
    private File getFileFromBytes(String fileName, byte[] bytes) {
        File file = new File(fileName);
        LOGGER.info("file name: {}", fileName);
        try (FileOutputStream out = new FileOutputStream(fileName)) {
            out.write(bytes);
            return file;
        } catch (IOException ioException) {
            throw new IllegalStateException("Error occurred during fetching attachments", ioException.getCause());
        }
    }
}
