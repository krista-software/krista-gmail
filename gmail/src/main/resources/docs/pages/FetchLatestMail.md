# Fetch Latest Mail

## Overview

Retrieves the most recent email received in the Gmail inbox, providing quick access to the latest incoming message.

## Request Details

- **Area**: Messaging
- **Type**: QUERY_SYSTEM
- **Retry Support**: ❌ No (no input parameters to validate)

## Input Parameters

This catalog request requires no input parameters.

## Output Parameters

| Parameter Name | Type | Description |
|----------------|------|-------------|
| New Email | Entity(Mail Details) | The most recent email from the inbox, or null if inbox is empty |

### Mail Details Entity

The returned email contains:
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

**Success Response**: Mail Details entity for the latest email

**Empty Response**: `null` if inbox is empty

## Validation Rules

No validation rules - this request has no input parameters.

## Error Handling

### System Errors (SYSTEM_ERROR)

**Cause**: Gmail API errors, network issues, or service unavailability

**Error Message**: "Error occurred while Fetch Latest Mail"

**Common Scenarios**:
- Network connectivity problems
- Gmail API temporarily unavailable
- Authentication issues
- API rate limits exceeded
- Inbox access issues

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

### Example 1: Fetch Latest Email

**Input**:
```
(No input required)
```

**Output**:
```json
{
  "New Email": {
    "messageId": "18c5f2a3b4d6e789",
    "from": "sender@example.com",
    "to": "user@example.com",
    "subject": "Important Update",
    "body": "Please review the attached document...",
    "date": "2025-10-04T14:30:00Z",
    "isRead": false,
    "attachments": ["document.pdf"]
  }
}
```

**Result**: Latest email retrieved successfully

### Example 2: Empty Inbox

**Input**:
```
(No input required)
```

**Output**:
```json
{
  "New Email": null
}
```

**Result**: No emails in inbox, returns null

### Example 3: Latest Unread Email

**Input**:
```
(No input required)
```

**Output**:
```json
{
  "New Email": {
    "messageId": "18c5f2a3b4d6e789",
    "from": "client@example.com",
    "subject": "Urgent Request",
    "isRead": false
  }
}
```

**Result**: Latest email (unread) retrieved

## Business Rules

1. **No Input Required**: This request requires no parameters
2. **Inbox Only**: Returns only the latest email from inbox
3. **Single Email**: Returns exactly one email (the most recent)
4. **Null for Empty**: Returns null if inbox is empty
5. **Complete Details**: Returns full email metadata and content
6. **Most Recent**: Always returns the newest email by date

## Limitations

1. **Gmail API Limits**: Subject to Gmail API quotas and rate limits
2. **Inbox Only**: Only retrieves from inbox, not other folders
3. **Single Email**: Returns only one email (the latest)
4. **No Filtering**: Cannot filter by read status or other criteria
5. **Account Scope**: Only retrieves from authenticated user's account
6. **Scope Requirements**: Requires `gmail.readonly` or `gmail.modify` OAuth scope

## Best Practices

### 1. Check for Null
Always check if the returned email is null before processing.

### 2. Use for Monitoring
Ideal for monitoring inbox for new emails in polling scenarios.

### 3. Combine with Other Requests
Use with Mark Message or Move Message for workflow automation.

### 4. Handle Empty Inbox
Implement proper handling when inbox is empty (null response).

### 5. Monitor API Usage
Track fetch operations to stay within Gmail API quotas.

### 6. Implement Caching
Cache the latest email ID to detect new emails efficiently.

## Common Use Cases

### 1. New Email Notification
```
Scenario: Check for new emails periodically
Action: Fetch latest mail and compare with last known email ID
Result: New emails detected and processed
```

### 2. Inbox Monitoring
```
Scenario: Monitor inbox for urgent emails
Action: Fetch latest mail and check for urgent keywords
Result: Urgent emails identified immediately
```

### 3. Auto-Response Trigger
```
Scenario: Trigger auto-response for new emails
Action: Fetch latest mail and send automated reply
Result: New emails receive immediate auto-response
```

### 4. Email Processing Workflow
```
Scenario: Process latest email in workflow
Action: Fetch latest mail and route to appropriate handler
Result: Latest email processed automatically
```

### 5. Inbox Status Check
```
Scenario: Check if inbox has any emails
Action: Fetch latest mail to verify inbox is not empty
Result: Inbox status determined
```

## Related Catalog Requests

- [Fetch Inbox](pages/FetchInbox.md) - Retrieve multiple inbox emails with pagination
- [Fetch Mail By Message Id](pages/FetchMailByMessageId.md) - Get specific email by ID
- [Trigger When New Email Arrived](pages/TriggerWhenNewEmailArrived.md) - Event-driven new email detection
- [Get Latest Mail](pages/GetLatestMail.md) - Event-driven latest email retrieval
- [Mark Message](pages/MarkMessage.md) - Mark emails as read/unread

## Technical Implementation

### Helper Class
- **Class**: MessagingArea
- **Package**: app.krista.extensions.essentials.collaboration.gmail.catalog
- **Validation**: No validation required (no input parameters)
- **Service**: Delegates to Account.getInboxFolder().getEmails(1.0, 1.0) to retrieve latest email
- **Entity Conversion**: CatalogTypes.fromEmail() converts Gmail Email to Mail Details entity

### Telemetry Metrics
- **gmail.fetchLatestMail**: Total number of fetch latest mail requests
- **Success Tracking**: Records successful fetches
- **Error Tracking**: Records system errors and authorization errors

## Troubleshooting

### Issue: Returns null

**Cause**: Inbox is empty

**Solution**:
1. Verify inbox has emails in Gmail web interface
2. Check authentication is for correct account
3. Ensure emails exist in inbox (not archived or deleted)
4. This is expected behavior for empty inbox

### Issue: "Error occurred while Fetch Latest Mail"

**Cause**: System error during fetch operation

**Solution**:
1. Check network connectivity
2. Verify Gmail API is accessible
3. Ensure authentication is valid
4. Retry the operation
5. Review logs for specific error details

### Issue: Authentication errors

**Cause**: Invalid or expired OAuth token

**Solution**:
1. Re-authenticate with Gmail
2. Verify OAuth scopes include gmail.readonly
3. Check token hasn't expired
4. Review authentication configuration

### Issue: Returns old email instead of latest

**Cause**: Inbox sorting or synchronization issue

**Solution**:
1. Verify Gmail inbox is synchronized
2. Check email dates in Gmail web interface
3. Retry the operation
4. Review Gmail API response in logs

## See Also

- [Extension Configuration](pages/ExtensionConfiguration.md)
- [Authentication Guide](pages/Authentication.md)
- [Gmail API List Messages](https://developers.google.com/gmail/api/reference/rest/v1/users.messages/list)
- [Fetch Inbox](pages/FetchInbox.md)

---
*Documentation updated according to Krista Extension Documentation Guidelines*

