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

import app.krista.model.field.NamedField;

import java.util.Map;

/**
 * Factory class for creating NamedField instances.
 * Provides convenient methods to create form fields with predefined types.
 */
public class NamedFieldFactory {

    private NamedFieldFactory() {
    }

    /**
     * Creates a text input field with the specified name.
     *
     * @param fieldName the name/label for the text field
     * @return NamedField configured as a text input field
     */
    public static NamedField createTextField(String fieldName) {
        return new NamedField(fieldName, FieldTypes.TEXT_FIELD, Map.of(), Map.of());
    }

    /**
     * Creates a switch/toggle field with the specified name.
     *
     * @param fieldName the name/label for the switch field
     * @return NamedField configured as a boolean switch field
     */
    public static NamedField createSwitchField(String fieldName) {
        return new NamedField(fieldName, FieldTypes.SWITCH_FIELD, Map.of(), Map.of());
    }

    /**
     * Creates a field with the specified name and custom field type.
     *
     * @param fieldName the name/label for the field
     * @param fieldType the type identifier for the field (e.g., from FieldTypes constants)
     * @return NamedField configured with the specified type
     */
    public static NamedField createField(String fieldName, String fieldType) {
        return new NamedField(fieldName, fieldType, Map.of(), Map.of());
    }

}
