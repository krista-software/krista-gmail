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

import app.krista.ksdk.telemetry.TelemetryMetrics;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.verify;

/**
 * Unit tests for TelemetryHelper.
 * Tests telemetry recording for success, errors, retries, and validation failures.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("TelemetryHelper Tests")
class TelemetryHelperTest {

    @Mock
    private TelemetryMetrics mockTelemetry;

    @Captor
    private ArgumentCaptor<Map<String, String>> tagsCaptor;

    private TelemetryHelper telemetryHelper;

    @BeforeEach
    void setUp() {
        telemetryHelper = new TelemetryHelper(mockTelemetry);
    }

    @Test
    @DisplayName("Should record success with correct metrics")
    void testRecordSuccess() {
        long startTime = System.currentTimeMillis() - 1000;
        Map<String, String> tags = Map.of("key1", "value1");

        telemetryHelper.recordSuccess("test.operation", startTime, tags);

        verify(mockTelemetry).incrementCounter("test.operation.success", 1);
        verify(mockTelemetry).recordDuration(eq("test.operation.duration"), anyLong(), tagsCaptor.capture());

        Map<String, String> capturedTags = tagsCaptor.getValue();
        assertThat(capturedTags).containsEntry("status", "success");
        assertThat(capturedTags).containsEntry("key1", "value1");
    }

    @Test
    @DisplayName("Should record success with null tags")
    void testRecordSuccessWithNullTags() {
        long startTime = System.currentTimeMillis() - 500;

        telemetryHelper.recordSuccess("test.operation", startTime, null);

        verify(mockTelemetry).incrementCounter("test.operation.success", 1);
        verify(mockTelemetry).recordDuration(eq("test.operation.duration"), anyLong(), tagsCaptor.capture());

        Map<String, String> capturedTags = tagsCaptor.getValue();
        assertThat(capturedTags).containsEntry("status", "success");
    }

    @Test
    @DisplayName("Should record retry prompted with correct metrics")
    void testRecordRetryPrompted() {
        long startTime = System.currentTimeMillis() - 2000;
        Map<String, String> tags = Map.of("validation_count", "3");

        telemetryHelper.recordRetryPrompted("test.operation", startTime, tags);

        verify(mockTelemetry).incrementCounter("test.operation.retry_prompted", 1);
        verify(mockTelemetry).recordDuration(eq("test.operation.duration"), anyLong(), tagsCaptor.capture());

        Map<String, String> capturedTags = tagsCaptor.getValue();
        assertThat(capturedTags).containsEntry("status", "retry_prompted");
        assertThat(capturedTags).containsEntry("validation_count", "3");
    }

    @Test
    @DisplayName("Should record validation error with error details")
    void testRecordValidationError() {
        long startTime = System.currentTimeMillis() - 1500;
        String errorMessage = "Invalid email format";
        Map<String, String> tags = Map.of("field", "email");

        telemetryHelper.recordValidationError("test.operation", startTime, errorMessage, tags);

        verify(mockTelemetry).incrementCounter("test.operation.mustAuthorizeException", 1);
        verify(mockTelemetry).recordDuration(eq("test.operation.duration"), anyLong(), tagsCaptor.capture());

        Map<String, String> capturedTags = tagsCaptor.getValue();
        assertThat(capturedTags).containsEntry("status", "validation_error");
        assertThat(capturedTags).containsEntry("error_type", "validation");
        assertThat(capturedTags).containsEntry("error_message", "Invalid email format");
        assertThat(capturedTags).containsEntry("field", "email");
    }

    @Test
    @DisplayName("Should record validation error with null error message")
    void testRecordValidationErrorWithNullMessage() {
        long startTime = System.currentTimeMillis() - 1000;

        telemetryHelper.recordValidationError("test.operation", startTime, null, Map.of());

        verify(mockTelemetry).recordDuration(eq("test.operation.duration"), anyLong(), tagsCaptor.capture());

        Map<String, String> capturedTags = tagsCaptor.getValue();
        assertThat(capturedTags).containsEntry("error_message", "Unknown error");
    }

    @Test
    @DisplayName("Should record general error with exception details")
    void testRecordError() {
        long startTime = System.currentTimeMillis() - 3000;
        Exception exception = new RuntimeException("Database connection failed");
        Map<String, String> tags = Map.of("database", "postgres");

        telemetryHelper.recordError("test.operation", startTime, exception, tags);

        verify(mockTelemetry).incrementCounter("test.operation.error", 1);
        verify(mockTelemetry).recordDuration(eq("test.operation.duration"), anyLong(), tagsCaptor.capture());

        Map<String, String> capturedTags = tagsCaptor.getValue();
        assertThat(capturedTags).containsEntry("status", "error");
        assertThat(capturedTags).containsEntry("error_type", "general");
        assertThat(capturedTags).containsEntry("error_message", "Database connection failed");
        assertThat(capturedTags).containsEntry("error_class", "RuntimeException");
        assertThat(capturedTags).containsEntry("database", "postgres");
    }

    @Test
    @DisplayName("Should record error with null exception message")
    void testRecordErrorWithNullMessage() {
        long startTime = System.currentTimeMillis() - 1000;
        Exception exception = new RuntimeException();

        telemetryHelper.recordError("test.operation", startTime, exception, Map.of());

        verify(mockTelemetry).recordDuration(eq("test.operation.duration"), anyLong(), tagsCaptor.capture());

        Map<String, String> capturedTags = tagsCaptor.getValue();
        assertThat(capturedTags).containsEntry("error_message", "Unknown error");
    }

    @Test
    @DisplayName("Should increment count")
    void testIncrementCount() {
        telemetryHelper.incrementCount("test.operation");

        verify(mockTelemetry).incrementCounter("test.operation.count", 1);
    }

    @Test
    @DisplayName("Should sanitize tags with null values")
    void testSanitizeTagsWithNullValues() {
        long startTime = System.currentTimeMillis() - 1000;
        Map<String, String> tags = Map.of("key1", "value1");
        // Create a mutable map to test null values
        java.util.Map<String, String> mutableTags = new java.util.HashMap<>();
        mutableTags.put("key1", "value1");
        mutableTags.put("key2", null);

        telemetryHelper.recordSuccess("test.operation", startTime, mutableTags);

        verify(mockTelemetry).recordDuration(eq("test.operation.duration"), anyLong(), tagsCaptor.capture());

        Map<String, String> capturedTags = tagsCaptor.getValue();
        assertThat(capturedTags).containsEntry("key1", "value1");
        assertThat(capturedTags).containsEntry("key2", "NA");
    }

    @Test
    @DisplayName("Should create safe tag map with key-value pairs")
    void testSafeTagMap() {
        Map<String, String> result = TelemetryHelper.safeTagMap("key1", "value1", "key2", "value2");

        assertThat(result).hasSize(2);
        assertThat(result).containsEntry("key1", "value1");
        assertThat(result).containsEntry("key2", "value2");
    }

    @Test
    @DisplayName("Should create safe tag map with null values")
    void testSafeTagMapWithNullValues() {
        Map<String, String> result = TelemetryHelper.safeTagMap("key1", "value1", "key2", null);

        assertThat(result).hasSize(2);
        assertThat(result).containsEntry("key1", "value1");
        assertThat(result).containsEntry("key2", "NA");
    }

    @Test
    @DisplayName("Should create empty safe tag map with no arguments")
    void testSafeTagMapWithNoArgs() {
        Map<String, String> result = TelemetryHelper.safeTagMap();

        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("Should create safe tag map with odd number of arguments")
    void testSafeTagMapWithOddArgs() {
        Map<String, String> result = TelemetryHelper.safeTagMap("key1", "value1", "key2");

        assertThat(result).hasSize(1);
        assertThat(result).containsEntry("key1", "value1");
    }

    @Test
    @DisplayName("Should record counter with custom value and tags")
    void testRecordCounter() {
        Map<String, String> tags = Map.of("operation", "fetch");

        telemetryHelper.recordCounter("test.metric", 5L, tags);

        verify(mockTelemetry).incrementCounter("test.metric", 5L, tags);
    }

    @Test
    @DisplayName("Should record gauge with value")
    void testRecordGauge() {
        telemetryHelper.recordGauge("test.gauge", 42.5);

        verify(mockTelemetry).observeGauge("test.gauge", 42.5);
    }

    @Test
    @DisplayName("Should handle empty additional tags in recordSuccess")
    void testRecordSuccessWithEmptyTags() {
        long startTime = System.currentTimeMillis() - 1000;

        telemetryHelper.recordSuccess("test.operation", startTime, Map.of());

        verify(mockTelemetry).incrementCounter("test.operation.success", 1);
        verify(mockTelemetry).recordDuration(eq("test.operation.duration"), anyLong(), tagsCaptor.capture());

        Map<String, String> capturedTags = tagsCaptor.getValue();
        assertThat(capturedTags).containsEntry("status", "success");
    }

    @Test
    @DisplayName("Should handle multiple tags in recordError")
    void testRecordErrorWithMultipleTags() {
        long startTime = System.currentTimeMillis() - 2000;
        Exception exception = new IllegalArgumentException("Invalid input");
        Map<String, String> tags = Map.of(
                "user_id", "12345",
                "operation_type", "create",
                "resource", "email"
        );

        telemetryHelper.recordError("test.operation", startTime, exception, tags);

        verify(mockTelemetry).recordDuration(eq("test.operation.duration"), anyLong(), tagsCaptor.capture());

        Map<String, String> capturedTags = tagsCaptor.getValue();
        assertThat(capturedTags).containsEntry("user_id", "12345");
        assertThat(capturedTags).containsEntry("operation_type", "create");
        assertThat(capturedTags).containsEntry("resource", "email");
        assertThat(capturedTags).containsEntry("error_class", "IllegalArgumentException");
    }

    @Test
    @DisplayName("Should calculate duration correctly")
    void testDurationCalculation() {
        long startTime = System.currentTimeMillis() - 5000; // 5 seconds ago

        telemetryHelper.recordSuccess("test.operation", startTime, Map.of());

        ArgumentCaptor<Long> durationCaptor = ArgumentCaptor.forClass(Long.class);
        verify(mockTelemetry).recordDuration(eq("test.operation.duration"), durationCaptor.capture(), any());

        Long duration = durationCaptor.getValue();
        assertThat(duration).isGreaterThanOrEqualTo(4900L); // Allow some tolerance
        assertThat(duration).isLessThanOrEqualTo(5100L);
    }
}

