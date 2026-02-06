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

package app.krista.extensions.essentials.collaboration.gmail.catalog.validators;

import app.krista.extensions.essentials.collaboration.gmail.service.Account;
import org.jvnet.hk2.annotations.Service;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Orchestrates validation of multiple resources using appropriate validators.
 * Manages a collection of validators and coordinates validation results.
 */
@Service
public class ValidationOrchestrator {

    /**
     * Constructs the validation orchestrator and initializes all validators.
     * Sets up validators for different resource types including email fields and pagination.
     *
     * @param account the Gmail account instance for validators that need account access
     */
    @Inject
    public ValidationOrchestrator(Account account) {
        validators.put(Validator.ValidationResource.MESSAGE_ID, new MessageIdValidator(account));
        validators.put(Validator.ValidationResource.TO, new TOEmailValidator());
        validators.put(Validator.ValidationResource.CC, new CCEmailValidator());
        validators.put(Validator.ValidationResource.BCC, new BCCEmailValidator());
        validators.put(Validator.ValidationResource.REPLY_TO, new ReplyToEmailValidator());
        validators.put(Validator.ValidationResource.FOLDER_NAME, new FolderNameValidator(account));
        validators.put(Validator.ValidationResource.LABEL, new LabelValidator(account));
        validators.put(Validator.ValidationResource.PAGE_NUMBER, new PageNumberValidator());
        validators.put(Validator.ValidationResource.PAGE_SIZE, new PageSizeValidator());
        validators.put(Validator.ValidationResource.QUERY, new QueryValidator(account));
    }

    /**
     * Represents the result of a validation operation.
     * Contains all necessary information for error handling and user feedback.
     */
    public static class ValidationResult {
        private final String confirmStepMessage;
        private final String fetchFieldName;
        private final String fetchStepMessage;
        private final String errMessage;
        private final String fieldType;

        public ValidationResult(String confirmStepMessage, String fetchFieldName, String fetchStepMessage, String errMessage, String fieldType) {
            this.confirmStepMessage = confirmStepMessage;
            this.fetchFieldName = fetchFieldName;
            this.fetchStepMessage = fetchStepMessage;
            this.errMessage = errMessage;
            this.fieldType = fieldType;

        }

        public String getConfirmStepMessage() {
            return confirmStepMessage;
        }

        public String getErrMessage() {
            return errMessage;
        }

        public String getFetchFieldName() {
            return fetchFieldName;
        }

        public String getFieldType() {
            return fieldType;
        }

        public String getFetchStepMessage() {
            return fetchStepMessage;
        }
    }

    private final Map<Validator.ValidationResource, Validator> validators = new HashMap<>();

    /**
     * Validates multiple resources and returns a list of validation failures.
     * Only returns results for resources that fail validation.
     *
     * @param resources map of validation resources to their string values
     * @return list of ValidationResult objects for failed validations (empty if all pass)
     */
    public List<ValidationResult> validate(Map<Validator.ValidationResource, String> resources) {
        List<ValidationResult> results = new ArrayList<>();
        for (Map.Entry<Validator.ValidationResource, String> entry : resources.entrySet()) {
            Validator validator = validators.get(entry.getKey());
            assert validator != null;
            if (!validator.validate(entry.getValue(), resources)) {
                results.add(new ValidationResult(validator.getConfirmationStepMessage(entry.getValue(), resources),
                        validator.getFetchFieldName(), validator.getFetchStepMessage(),
                        validator.getErrMessage(entry.getValue()), validator.getFieldType()));
            }
        }
        return results;
    }
}

