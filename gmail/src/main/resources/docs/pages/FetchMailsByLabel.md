# Fetch Mails By Label

## Overview

Retrieves a paginated list of emails from a specific Gmail label (folder), allowing efficient browsing of emails organized by labels with customizable page size and page number.

## Request Details

- **Area**: Messaging
- **Type**: QUERY_SYSTEM
- **Retry Support**: ✅ Yes (validation errors prompt for re-entry)

## Input Parameters

| Parameter Name | Type | Required | Description | Example |
|----------------|------|----------|-------------|---------|
| Label | Text | Yes | Name of the label or folder to fetch emails from | "Work" |
| Page Number | Number | No | Page number for pagination (1-15, default: 1) | 1 |
| Page Size | Number | No | Number of emails per page (1-15, default: 15) | 10 |

### Parameter Details

**Label**: The name of the Gmail label (folder) to fetch emails from. Can be:
- System labels: `INBOX`, `SENT`, `DRAFT`, `TRASH`, `SPAM`, `STARRED`, `IMPORTANT`, `UNREAD`
- Custom labels: User-created labels like "Work", "Personal", "Archive", "Projects"
- Label names are case-sensitive

**Page Number**: The page number to retrieve. Valid range is 1-15. If not provided, defaults to 1 (first page).

**Page Size**: The number of emails to return per page. Valid range is 1-15. If not provided, defaults to 15 (maximum page size).

## Output Parameters

| Parameter Name | Type | Description |
|----------------|------|-------------|
| Mails | [ Entity(Mail Details) ] | List of email messages from the specified label |

### Mail Details Entity

Each email in the list contains:
- **Message ID**: Unique identifier
- **Thread ID**: Conversation thread identifier
- **From**: Sender email address
- **To**: Recipient email addresses
- **Cc**: Carbon copy recipients
- **Subject**: Email subject line
- **Body**: Email message content
- **Date**: Email sent/received date
- **Labels**: Applied Gmail labels
- **Attachments**: List of attached files
- **Is Read**: Read/unread status

**Success Response**: Array of Mail Details entities from the label

**Empty Response**: Empty array `[]` if label doesn't exist or has no emails

## Validation Rules

| Validation | Error Message | Resolution |
|------------|---------------|------------|
| Label required | "Label cannot be empty" | Provide a valid label name |
| Page number range | "Page number must be between 1 and 15" | Use a value between 1-15 |
| Page size range | "Page size must be between 1 and 15" | Use a value between 1-15 |
| Page number numeric | "Page number must be a number" | Provide a numeric value |
| Page size numeric | "Page size must be a number" | Provide a numeric value |

## Error Handling

### Input Errors (INPUT_ERROR)

**Cause**: Invalid or missing label, page number, or page size

**Common Scenarios**:
- Empty label name
- Page number or size outside valid range (1-15)
- Non-numeric values for page number or size
- Negative values

**Resolution**:
1. Verify label name is not empty
2. Verify page number is between 1 and 15
3. Verify page size is between 1 and 15
4. Ensure numeric values are provided
5. Re-enter corrected values when prompted

### System Errors (SYSTEM_ERROR)

**Cause**: Gmail API errors, network issues, or service unavailability

**Error Message**: "Error occurred while fetching mails by label"

**Common Scenarios**:
- Network connectivity problems
- Gmail API temporarily unavailable
- Authentication issues
- API rate limits exceeded
- Label access issues

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

### Example 1: Fetch from Custom Label

**Input**:
```
Label: "Work"
Page Number: 1
Page Size: 15
```

**Output**:
```json
{
  "Mails": [
    {
      "messageId": "18c5f2a3b4d6e789",
      "from": "colleague@example.com",
      "subject": "Project Update",
      "labels": ["Work", "INBOX"]
    }
    // ... up to 15 emails
  ]
}
```

**Result**: First 15 emails from "Work" label retrieved

### Example 2: Fetch from System Label

**Input**:
```
Label: "STARRED"
Page Number: 1
Page Size: 10
```

**Output**:
```json
{
  "Mails": [
    // 10 starred emails
  ]
}
```

**Result**: First 10 starred emails retrieved

### Example 3: Fetch with Pagination

**Input**:
```
Label: "Important"
Page Number: 2
Page Size: 5
```

**Output**:
```json
{
  "Mails": [
    // Emails 6-10 from Important label
  ]
}
```

**Result**: Second page of Important emails retrieved

### Example 4: Non-Existent Label

**Input**:
```
Label: "NonExistentLabel"
Page Number: 1
Page Size: 15
```

**Output**:
```json
{
  "Mails": []
}
```

**Result**: Empty array when label doesn't exist

### Example 5: Empty Label Validation

**Input**:
```
Label: ""
Page Number: 1
Page Size: 10
```

**Output**:
```json
{
  "error": {
    "type": "INPUT_ERROR",
    "message": "Label cannot be empty"
  },
  "retryPrompt": true
}
```

**Result**: Validation error prompts for label name

### Example 6: Invalid Page Number

**Input**:
```
Label: "Work"
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

## Business Rules

1. **Label Required**: A label name must be provided
2. **Default Values**: Page number defaults to 1, page size defaults to 15
3. **Range Limits**: Page number and size must be between 1 and 15
4. **Case Sensitive**: Label names are case-sensitive
5. **Non-Existent Labels**: Returns empty array if label doesn't exist
6. **Chronological Order**: Emails returned in reverse chronological order (newest first)

## Limitations

1. **Gmail API Limits**: Subject to Gmail API quotas and rate limits
2. **Page Range**: Maximum page number is 15
3. **Page Size**: Maximum 15 emails per page
4. **Total Emails**: Can retrieve up to 225 emails per label (15 pages × 15 emails)
5. **Label Existence**: Returns empty array if label doesn't exist
6. **Scope Requirements**: Requires `gmail.readonly` or `gmail.modify` OAuth scope
7. **Case Sensitivity**: Label names must match exactly (case-sensitive)

## Best Practices

### 1. Verify Label Exists
Use Fetch All Labels to verify label exists before fetching emails.

### 2. Use Correct Case
Ensure label name matches exact case (e.g., "Work" not "work").

### 3. Handle Empty Results
Always check for empty arrays when label doesn't exist or has no emails.

### 4. Choose Appropriate Page Size
- Use smaller page sizes (5-10) for UI display
- Use larger page sizes (15) for batch processing
- Balance between API calls and data volume

### 5. Implement Caching
Cache fetched emails to reduce API calls for frequently accessed labels.

### 6. Monitor API Usage
Track fetch operations to stay within Gmail API quotas.

## Common Use Cases

### 1. Organize Emails by Project
```
Scenario: View all emails for specific project
Action: Fetch emails from project-specific label
Result: All project emails displayed together
```

### 2. Priority Email Management
```
Scenario: Process high-priority emails
Action: Fetch emails from "Important" or "Urgent" label
Result: Priority emails identified and processed
```

### 3. Category-Based Processing
```
Scenario: Process emails by category
Action: Fetch emails from category labels (Work, Personal, etc.)
Result: Emails processed by category
```

### 4. Archive Review
```
Scenario: Review archived emails
Action: Fetch emails from "Archive" label
Result: Archived emails available for review
```

### 5. Label-Based Workflows
```
Scenario: Automate workflows based on labels
Action: Fetch emails from workflow-specific labels
Result: Workflow automation based on email labels
```

## Related Catalog Requests

- [Fetch All Labels](pages/FetchAllLabels.md) - Get list of available labels
- [Fetch Inbox](pages/FetchInbox.md) - Retrieve inbox emails
- [Fetch Sent](pages/FetchSent.md) - Retrieve sent emails
- [Move Message](pages/MoveMessage.md) - Move emails to different labels
- [Fetch Mail Details By Query](pages/FetchMailDetailsByQuery.md) - Search emails

## Technical Implementation

### Helper Class
- **Class**: MessagingArea
- **Package**: app.krista.extensions.essentials.collaboration.gmail.catalog
- **Validation**: ValidationOrchestrator validates label, page number, and page size
- **Service**: Delegates to Account.getFolderByName() and Folder.getEmails() for email retrieval
- **Entity Conversion**: CatalogTypes.fromEmail() converts Gmail Email to Mail Details entity

### Telemetry Metrics
- **gmail.fetchMailsByLabel**: Total number of fetch by label requests
- **Tags**: label, page_number, page_size, validation_count
- **Success Tracking**: Records successful fetches with label and pagination parameters
- **Error Tracking**: Records validation errors and system errors
- **Retry Tracking**: Records when validation prompts for re-entry

## Troubleshooting

### Issue: Empty array returned

**Cause**: Label doesn't exist or has no emails

**Solution**:
1. Use Fetch All Labels to verify label exists
2. Check label name spelling (case-sensitive)
3. Verify label has emails in Gmail web interface
4. Try with a known system label (e.g., "INBOX")

### Issue: "Label cannot be empty"

**Cause**: Missing or empty label parameter

**Solution**:
1. Provide a valid label name
2. Verify parameter is being passed correctly
3. Check for null or empty strings

### Issue: "Page number must be between 1 and 15"

**Cause**: Page number outside valid range

**Solution**:
1. Use page number between 1 and 15
2. Start with page 1
3. Increment page number sequentially

### Issue: "Error occurred while fetching mails by label"

**Cause**: System error during fetch operation

**Solution**:
1. Check network connectivity
2. Verify Gmail API is accessible
3. Ensure authentication is valid
4. Retry the operation
5. Review logs for specific error details

### Issue: Case sensitivity issues

**Cause**: Label name case doesn't match

**Solution**:
1. Use exact label name as it appears in Gmail
2. Use Fetch All Labels to get exact label names
3. Remember system labels are uppercase (INBOX, SENT, etc.)

## See Also

- [Extension Configuration](pages/ExtensionConfiguration.md)
- [Authentication Guide](pages/Authentication.md)
- [Gmail API List Messages](https://developers.google.com/gmail/api/reference/rest/v1/users.messages/list)
- [Fetch All Labels](pages/FetchAllLabels.md)

---
*Documentation updated according to Krista Extension Documentation Guidelines*

