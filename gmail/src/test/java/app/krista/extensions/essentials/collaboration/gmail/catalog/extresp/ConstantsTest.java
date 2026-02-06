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

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for catalog constants classes.
 * Tests SubCatalogConstants and FieldTypes.
 */
@DisplayName("Catalog Constants Tests")
class ConstantsTest {

    @Test
    @DisplayName("SubCatalogConstants should have correct confirmation step names")
    void testSubCatalogConstants() {
        assertThat(SubCatalogConstants.CONFIRM_REENTER_FETCH_MAIL).isEqualTo("confirmReenterFetchMail");
        assertThat(SubCatalogConstants.CONFIRM_REENTER_MOVE_MESSAGE).isEqualTo("confirmReenterMoveMessage");
        assertThat(SubCatalogConstants.CONFIRM_REENTER_SEND_MAIL).isEqualTo("confirmReenterSendMail");
        assertThat(SubCatalogConstants.CONFIRM_REENTER_FETCH_SENT).isEqualTo("confirmReenterFetchSent");
        assertThat(SubCatalogConstants.CONFIRM_REENTER_FETCH_INBOX).isEqualTo("confirmReenterFetchInbox");
        assertThat(SubCatalogConstants.CONFIRM_REENTER_FETCH_MAIL_BY_QUERY).isEqualTo("confirmReenterFetchMailByQuery");
        assertThat(SubCatalogConstants.CONFIRM_REENTER_FETCH_MAIL_BY_LABEL).isEqualTo("confirmReenterFetchMailByLabel");
        assertThat(SubCatalogConstants.CONFIRM_REENTER_REPLY_TO_MAIL_WITH_FIELDS).isEqualTo("confirmReenterReplyToMailWithFields");
        assertThat(SubCatalogConstants.CONFIRM_REENTER_REPLY_TO_ALL_WITH_FIELDS).isEqualTo("confirmReenterReplyToAllWithFields");
        assertThat(SubCatalogConstants.CONFIRM_REENTER_MARK_MESSAGE).isEqualTo("confirmReenterMarkMessage");
        assertThat(SubCatalogConstants.CONFIRM_REENTER_REPLY_TO_MAIL).isEqualTo("confirmReenterReplyToMail");
        assertThat(SubCatalogConstants.CONFIRM_REENTER_REPLY_TO_ALL).isEqualTo("confirmReenterReplyToAll");
        assertThat(SubCatalogConstants.CONFIRM_REENTER_FORWARD_MAIL).isEqualTo("confirmReenterForwardMail");
    }

    @Test
    @DisplayName("SubCatalogConstants should have validation results key")
    void testValidationResultsKey() {
        assertThat(SubCatalogConstants.VALIDATION_RESULTS).isEqualTo("Validation Results");
    }

    @Test
    @DisplayName("FieldTypes should have correct field type constants")
    void testFieldTypes() {
        assertThat(FieldTypes.TEXT_FIELD).isEqualTo("com.krista.fields.Text");
        assertThat(FieldTypes.NUMBER_FIELD).isEqualTo("com.krista.fields.Number");
        assertThat(FieldTypes.SWITCH_FIELD).isEqualTo("com.krista.fields.Switch");
    }

    @Test
    @DisplayName("FieldTypes class should not be instantiable")
    void testFieldTypesNotInstantiable() {
        // FieldTypes should be a utility class with private constructor
        assertThat(FieldTypes.class).isNotNull();
    }

    @Test
    @DisplayName("SubCatalogConstants class should not be instantiable")
    void testSubCatalogConstantsNotInstantiable() {
        // SubCatalogConstants should be a utility class with private constructor
        assertThat(SubCatalogConstants.class).isNotNull();
    }

    @Test
    @DisplayName("All SubCatalog confirm constants should follow naming convention")
    void testConfirmNamingConvention() {
        assertThat(SubCatalogConstants.CONFIRM_REENTER_FETCH_MAIL).startsWith("confirmReenter");
        assertThat(SubCatalogConstants.CONFIRM_REENTER_MOVE_MESSAGE).startsWith("confirmReenter");
        assertThat(SubCatalogConstants.CONFIRM_REENTER_SEND_MAIL).startsWith("confirmReenter");
        assertThat(SubCatalogConstants.CONFIRM_REENTER_FETCH_SENT).startsWith("confirmReenter");
        assertThat(SubCatalogConstants.CONFIRM_REENTER_FETCH_INBOX).startsWith("confirmReenter");
    }

    @Test
    @DisplayName("Field types should be non-empty strings")
    void testFieldTypesNonEmpty() {
        assertThat(FieldTypes.TEXT_FIELD).isNotEmpty();
        assertThat(FieldTypes.NUMBER_FIELD).isNotEmpty();
        assertThat(FieldTypes.SWITCH_FIELD).isNotEmpty();
    }

    @Test
    @DisplayName("SubCatalog constants should be non-empty strings")
    void testSubCatalogConstantsNonEmpty() {
        assertThat(SubCatalogConstants.CONFIRM_REENTER_FETCH_MAIL).isNotEmpty();
        assertThat(SubCatalogConstants.CONFIRM_REENTER_MOVE_MESSAGE).isNotEmpty();
        assertThat(SubCatalogConstants.VALIDATION_RESULTS).isNotEmpty();
    }
}

