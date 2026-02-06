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

package app.krista.extensions.essentials.collaboration.gmail.service;

import app.krista.extensions.essentials.collaboration.gmail.impl.stores.KristaMediaClient;
import app.krista.model.base.File;

/**
 * Interface for Gmail email attachment operations.
 * Provides access to attachment metadata and content download functionality.
 * Handles the conversion between Gmail's attachment format and Krista's file system,
 * enabling seamless integration of email attachments within the platform.
 * <p>
 * Implementations of this interface manage attachment data retrieval from Gmail API
 * and provide methods for accessing attachment properties and downloading content.
 */
public interface Attachment {

    /**
     * Gets the filename of the attachment.
     * Returns the original filename as it was attached to the email message.
     * This is the name that will be used when downloading or displaying the attachment.
     *
     * @return the filename of the attachment, or null if filename is not available
     */
    String getName();

    /**
     * Gets the size of the attachment in bytes.
     * Returns the total size of the attachment content as reported by Gmail API.
     * This can be used for display purposes or to validate download capacity.
     *
     * @return the size of the attachment in bytes, or 0 if size information is not available
     */
    int getSize();

    /**
     * Gets the MIME type of the attachment.
     * Returns the content type that describes the format of the attachment file.
     * Examples include "application/pdf", "image/jpeg", "text/plain", etc.
     *
     * @return the MIME type of the attachment, or null if MIME type is not available
     */
    String getMimeType();

    /**
     * Downloads the attachment content and converts it to a Krista File object.
     * Retrieves the attachment data from Gmail API, processes it through the media client,
     * and returns a Krista File object that can be used within the platform.
     * <p>
     * The download process includes:
     * - Fetching attachment data from Gmail API
     * - Creating a temporary local file
     * - Uploading to Krista's media server
     * - Returning a platform-compatible File object
     *
     * @param kristaMediaClient the client for uploading files to Krista's media server
     * @return Krista File object representing the downloaded attachment, or null if download fails
     */
    File download(KristaMediaClient kristaMediaClient);

}
