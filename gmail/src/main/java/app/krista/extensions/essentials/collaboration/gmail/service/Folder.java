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

import java.util.List;

/**
 * Interface for Gmail folder (label) operations and properties.
 * Provides access to Gmail folder functionality including folder identification,
 * hierarchy navigation, and email retrieval with pagination support.
 * <p>
 * Gmail uses labels as folders, and this interface abstracts that complexity
 * to provide a traditional folder-like interface while maintaining compatibility
 * with Gmail's label-based organization system.
 */
public interface Folder {

    /**
     * Gets the unique identifier of the folder.
     * Returns the Gmail label ID that uniquely identifies this folder within the account.
     * This ID can be used for API operations that require folder identification.
     *
     * @return the Gmail label ID for this folder
     */
    String getFolderId();

    /**
     * Gets the display name of the folder.
     * Returns the human-readable name of the folder as it appears in the Gmail interface.
     * This is the name users see when browsing their folder list.
     *
     * @return the folder display name
     */
    String getFolderName();

    /**
     * Gets the parent folder of this folder.
     * Returns the parent folder in Gmail's hierarchical label structure.
     * Gmail supports nested labels using "/" as a separator (e.g., "Work/Projects").
     *
     * @return the parent Folder object, or null if this is a top-level folder
     */
    Folder getParent();

    /**
     * Gets the complete folder path from root to this folder.
     * Returns a list of folder names representing the full hierarchical path.
     * For example, a folder "Work/Projects/Current" would return ["Work", "Projects", "Current"].
     *
     * @return list of folder names representing the complete path from root to this folder
     */
    List<String> getFolderPath();

    /**
     * Retrieves emails from this folder with pagination support.
     * Fetches a specific page of emails belonging to this folder, allowing for
     * efficient handling of large folders by loading emails in manageable chunks.
     * <p>
     * The pagination is 1-based, meaning the first page is page number 1.
     * Page size determines how many emails are returned per page.
     *
     * @param pageNumber the page number to retrieve (1-based indexing)
     * @param pageSize   the number of emails to return per page
     * @return list of Email objects from the specified page, empty list if no emails or invalid page
     */
    List<Email> getEmails(Double pageNumber, Double pageSize);

}
