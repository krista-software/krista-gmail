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

import app.krista.extension.executor.RemediationAction;

import java.util.Map;

public class SubCatalogRequestAction implements RemediationAction {

    private final String subCatalogRequestName;
    private final String message;
    private final Map<String, Object> additionalData;

    private SubCatalogRequestAction(String subCatalogRequestName, String message, Map<String, Object> additionalData) {
        this.subCatalogRequestName = subCatalogRequestName;
        this.message = message;
        this.additionalData = additionalData;
    }

    public static SubCatalogRequestAction create(String subCatalogRequestName, String message, Map<String, Object> additionalData) {
        return new SubCatalogRequestAction(subCatalogRequestName, message, additionalData);
    }

    @Override
    public String getMessage() {
        return message;
    }

    @Override
    public RecipientType getRecipientType() {
        return RecipientType.ACTIVE_USER;
    }

    public String getSubCatalogRequestName() {
        return subCatalogRequestName;
    }

    public Map<String, Object> getAdditionalData() {
        return additionalData;
    }

    @Override
    public String toString() {
        return "SubCatalogRequestAction{" +
                "subCatalogRequestName='" + subCatalogRequestName + '\'' +
                ", message='" + message + '\'' +
                ", additionalData=" + additionalData +
                '}';
    }

    @Override
    public int compareTo(RemediationAction o) {
        return 0;
    }
}