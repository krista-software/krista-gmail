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

/**
 * Represents an email address with optional display name.
 * Encapsulates both the email address and an associated display name,
 * providing a structured way to handle email recipient information
 * throughout the Gmail extension.
 * <p>
 * This class is used for representing senders, recipients, and other
 * email address fields in a consistent format across the application.
 */
public class EmailAddress {

    private final String name;
    private final String emailAddress;

    /**
     * Constructs an EmailAddress with a display name and email address.
     * Creates a new EmailAddress instance with both display name and email address components.
     *
     * @param name         the display name associated with the email address (can be null or empty)
     * @param emailAddress the actual email address (should be a valid email format)
     */
    public EmailAddress(String name, String emailAddress) {
        this.name = name;
        this.emailAddress = emailAddress;
    }

    /**
     * Gets the display name associated with the email address.
     * Returns the human-readable name that may be displayed alongside the email address.
     * This is typically the person's name or organization name.
     *
     * @return the display name, or null/empty string if no display name is set
     */
    public String getName() {
        return name;
    }

    /**
     * Gets the actual email address.
     * Returns the email address portion that can be used for sending emails.
     * This should be in a valid email format (e.g., "user@example.com").
     *
     * @return the email address string
     */
    public String getEmailAddress() {
        return emailAddress;
    }
}
