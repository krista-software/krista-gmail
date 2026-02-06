# Fetch Inbox

## Overview

Retrieves a paginated list of emails from the Gmail inbox, allowing efficient browsing of inbox messages with customizable page size and page number.

## Request Details

- **Area**: Messaging
- **Type**: QUERY_SYSTEM
- **Retry Support**: ✅ Yes (validation errors prompt for re-entry)

## Input Parameters

| Parameter Name | Type | Required | Description | Example |
|----------------|------|----------|-------------|---------|
| Page Number | Number | No | Page number for pagination (1-15, default: 1) | 1 |
| Page Size | Number | No | Number of emails per page (1-15, default: 15) | 10 |

### Parameter Details

**Page Number**: The page number to retrieve. Valid range is 1-15. If not provided, defaults to 1 (first page).

**Page Size**: The number of emails to return per page. Valid range is 1-15. If not provided, defaults to 15 (maximum page size).

**Pagination Calculation**:
- Page 1 with size 10: Returns emails 1-10
- Page 2 with size 10: Returns emails 11-20
- Page 3 with size 10: Returns emails 21-30

## Output Parameters

| Parameter Name | Type | Description |
|----------------|------|-------------|
| Inbox Mails | [ Entity(Mail Details) ] | List of email messages from inbox |

### Mail Details Entity

Each email in the list contains:
- **Message ID**: Unique identifier
- **Thread ID**: Conversation thread identifier
- **From**: Sender email address
- **To**: Recipient email addresses
- **Cc**: Carbon copy recipients
- **Subject**: Email subject line
- **Body**: Email message content (preview or full)
- **Date**: Email sent/received date
- **Labels**: Applied Gmail labels
- **Attachments**: List of attached files
- **Is Read**: Read/unread status

**Success Response**: Array of Mail Details entities

**Empty Response**: Empty array `[]` if no emails found or invalid parameters

## Validation Rules

| Validation | Error Message | Resolution |
|------------|---------------|------------|
| Page number range | "Page number must be between 1 and 15" | Use a value between 1-15 |
| Page size range | "Page size must be between 1 and 15" | Use a value between 1-15 |
| Page number numeric | "Page number must be a number" | Provide a numeric value |
| Page size numeric | "Page size must be a number" | Provide a numeric value |

## Error Handling

### Input Errors (INPUT_ERROR)

**Cause**: Invalid page number or page size

**Common Scenarios**:
- Page number or size outside valid range (1-15)
- Non-numeric values for page number or size
- Negative values

**Resolution**:
1. Verify page number is between 1 and 15
2. Verify page size is between 1 and 15
3. Ensure values are numeric
4. Re-enter corrected values when prompted

### Logic Errors (LOGIC_ERROR)

**Cause**: Invalid pagination parameters

**Error Message**: "We couldn't fetch your inbox because the page number or page size is invalid. Please enter a number between 1 and 15."

**Common Scenarios**:
- Page number exceeds available pages
- Invalid parameter combinations

**Resolution**:
1. Use valid page numbers (1-15)
2. Use valid page sizes (1-15)
3. Start with page 1 if unsure
4. Use default values by omitting parameters

### System Errors (SYSTEM_ERROR)

**Cause**: Gmail API errors, network issues, or service unavailability

**Error Message**: "Error occurred while fetching inbox"

**Common Scenarios**:
- Network connectivity problems
- Gmail API temporarily unavailable
- Authentication issues
- API rate limits exceeded

**Resolution**:
1. Check network connectivity
2. Verify Gmail API is accessible
3. Ensure authentication is valid
4. Retry after a brief delay
5. Check Gmail API quotas

### Authorization Errors (MustAuthorizeException)

**Cause**: User not authenticated or token expired

**Resolution**:
1. Complete OAuth authentication flow
2. Verify authentication credentials
3. Re-authenticate if token expired
4. Check OAuth scopes include gmail.readonly or gmail.modify

## Usage Examples

### Example 1: Fetch First Page (Default)

**Input**:
```
Page Number: 1
Page Size: 15
```

**Output**:
```json
{
  "Inbox Mails": [
    {
      "messageId": "18c5f2a3b4d6e789",
      "from": "sender@example.com",
      "subject": "Project Update",
      "date": "2025-10-04T10:30:00Z",
      "isRead": false
    },
    // ... up to 15 emails
  ]
}
```

**Result**: First 15 emails from inbox retrieved

### Example 2: Fetch with Custom Page Size

**Input**:
```
Page Number: 1
Page Size: 5
```

**Output**:
```json
{
  "Inbox Mails": [
    // 5 most recent emails
  ]
}
```

**Result**: First 5 emails from inbox retrieved

### Example 3: Fetch Second Page

**Input**:
```
Page Number: 2
Page Size: 10
```

**Output**:
```json
{
  "Inbox Mails": [
    // Emails 11-20
  ]
}
```

**Result**: Emails 11-20 from inbox retrieved

### Example 4: Fetch with Defaults (No Parameters)

**Input**:
```
(No parameters provided)
```

**Output**:
```json
{
  "Inbox Mails": [
    // First 15 emails (default)
  ]
}
```

**Result**: First 15 emails retrieved using defaults

### Example 5: Invalid Page Number

**Input**:
```
Page Number: 20
Page Size: 10
```

**Output**:
```json
{
  "error": {
    "type": "INPUT_ERROR",
    "message": "Page number must be between 1 and 15"
  },
  "retryPrompt": true
}
```

**Result**: Validation error prompts for valid page number

### Example 6: Invalid Page Size

**Input**:
```
Page Number: 1
Page Size: 25
```

**Output**:
```json
{
  "error": {
    "type": "LOGIC_ERROR",
    "message": "We couldn't fetch your inbox because the page number or page size is invalid. Please enter a number between 1 and 15."
  }
}
```

**Result**: Logic error for invalid page size

## Business Rules

1. **Default Values**: Page number defaults to 1, page size defaults to 15
2. **Range Limits**: Both page number and size must be between 1 and 15
3. **Inbox Only**: Returns only emails in the INBOX label
4. **Chronological Order**: Emails returned in reverse chronological order (newest first)
5. **Complete Details**: Each email includes full metadata and content
6. **Empty Results**: Returns empty array if no emails match criteria

## Limitations

1. **Gmail API Limits**: Subject to Gmail API quotas and rate limits
2. **Page Range**: Maximum page number is 15
3. **Page Size**: Maximum 15 emails per page
4. **Total Emails**: Can retrieve up to 225 emails (15 pages × 15 emails)
5. **Inbox Only**: Only retrieves emails from inbox, not other folders
6. **Scope Requirements**: Requires `gmail.readonly` or `gmail.modify` OAuth scope

## Best Practices

### 1. Use Pagination Efficiently
Start with page 1 and increment as needed rather than jumping to high page numbers.

### 2. Choose Appropriate Page Size
- Use smaller page sizes (5-10) for UI display
- Use larger page sizes (15) for batch processing
- Balance between API calls and data volume

### 3. Handle Empty Results
Always check for empty arrays and handle gracefully when no emails are found.

### 4. Implement Caching
Cache fetched emails to reduce API calls for frequently accessed pages.

### 5. Monitor API Usage
Track fetch operations to stay within Gmail API quotas.

### 6. Error Handling
Implement retry logic for transient failures and proper error messages for users.

## Common Use Cases

### 1. Email List Display
```
Scenario: Display inbox emails in user interface
Action: Fetch inbox with page size matching UI display
Result: Users see paginated list of inbox emails
```

### 2. Email Processing Workflow
```
Scenario: Process recent inbox emails in batches
Action: Fetch inbox page by page and process each batch
Result: All inbox emails processed systematically
```

### 3. Unread Email Check
```
Scenario: Check for new unread emails
Action: Fetch first page and filter for unread status
Result: Recent unread emails identified
```

### 4. Email Search Interface
```
Scenario: Provide browsable email list for search
Action: Fetch inbox with pagination for user navigation
Result: Users can browse through inbox pages
```

### 5. Inbox Monitoring
```
Scenario: Monitor inbox for new emails periodically
Action: Fetch first page at regular intervals
Result: New emails detected and processed
```

## Related Catalog Requests

- [Fetch Sent](pages/FetchSent.md) - Retrieve sent emails
- [Fetch Mail By Message Id](pages/FetchMailByMessageId.md) - Get specific email
- [Fetch Mail Details By Query](pages/FetchMailDetailsByQuery.md) - Search emails
- [Fetch Mails By Label](pages/FetchMailsByLabel.md) - Retrieve emails by label
- [Fetch Latest Mail](pages/FetchLatestMail.md) - Get most recent email

## Technical Implementation

### Helper Class
- **Class**: MessagingArea
- **Package**: app.krista.extensions.essentials.collaboration.gmail.catalog
- **Validation**: ValidationOrchestrator validates page number and page size ranges
- **Service**: Delegates to Account.getInboxFolder().getEmails() for email retrieval
- **Entity Conversion**: CatalogTypes.fromEmail() converts Gmail Email to Mail Details entity

### Telemetry Metrics
- **TELEMETRY_FETCH_INBOX**: Total number of fetch inbox requests
- **Tags**: page_number, page_size, validation_count
- **Success Tracking**: Records successful fetches with pagination parameters
- **Error Tracking**: Records validation errors for invalid parameters
- **Retry Tracking**: Records when validation prompts for re-entry

## Troubleshooting

### Issue: Empty array returned

**Cause**: No emails in inbox or page number exceeds available emails

**Solution**:
1. Verify inbox has emails
2. Try page 1 to confirm emails exist
3. Reduce page number if too high
4. Check authentication is for correct account

### Issue: "Page number must be between 1 and 15"

**Cause**: Page number outside valid range

**Solution**:
1. Use page number between 1 and 15
2. Start with page 1
3. Increment page number sequentially

### Issue: "We couldn't fetch your inbox"

**Cause**: Invalid pagination parameters

**Solution**:
1. Verify page number is 1-15
2. Verify page size is 1-15
3. Use numeric values only
4. Try default values by omitting parameters

### Issue: "Error occurred while fetching inbox"

**Cause**: System error during fetch operation

**Solution**:
1. Check network connectivity
2. Verify Gmail API is accessible
3. Ensure authentication is valid
4. Retry the operation
5. Review logs for specific error details

## See Also

- [Extension Configuration](pages/ExtensionConfiguration.md)
- [Authentication Guide](pages/Authentication.md)
- [Gmail API List Messages](https://developers.google.com/gmail/api/reference/rest/v1/users.messages/list)
- [Fetch Sent](pages/FetchSent.md)

---
*Documentation updated according to Krista Extension Documentation Guidelines*

