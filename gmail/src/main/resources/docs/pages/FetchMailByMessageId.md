# Fetch Mail By Message Id

## Overview

Retrieves a specific email message from Gmail using its unique message identifier, returning complete email details including sender, recipients, subject, body, and attachments.

## Request Details

- **Area**: Messaging
- **Type**: QUERY_SYSTEM
- **Retry Support**: ✅ Yes (validation errors prompt for re-entry)

## Input Parameters

| Parameter Name | Type | Required | Description | Example |
|----------------|------|----------|-------------|---------|
| Message ID | Text | Yes | Unique identifier of the email message to retrieve from Gmail | "18c5f2a3b4d6e789" |

### Parameter Details

**Message ID**: The unique Gmail message identifier. This identifier is returned by other catalog requests such as Fetch Inbox, Fetch Sent, search operations, or email notification events. Each email in Gmail has a unique, permanent message ID.

## Output Parameters

| Parameter Name | Type | Description |
|----------------|------|-------------|
| Mail | Entity(Mail Details) | Complete email message details |

### Mail Details Entity

The Mail Details entity contains:
- **Message ID**: Unique identifier
- **Thread ID**: Conversation thread identifier
- **From**: Sender email address
- **To**: Recipient email addresses
- **Cc**: Carbon copy recipients
- **Bcc**: Blind carbon copy recipients
- **Subject**: Email subject line
- **Body**: Email message content
- **Date**: Email sent/received date
- **Labels**: Applied Gmail labels
- **Attachments**: List of attached files
- **Is Read**: Read/unread status

**Success Response**: Mail Details entity with complete email information

**Empty Response**: `null` if message ID is invalid or not found

## Validation Rules

| Validation | Error Message | Resolution |
|------------|---------------|------------|
| Message ID required | "Message ID cannot be empty" | Provide a valid message ID |
| Message ID format | "Invalid message ID format" | Ensure message ID is in correct Gmail format |

## Error Handling

### Input Errors (INPUT_ERROR)

**Cause**: Invalid or missing message ID

**Common Scenarios**:
- Empty message ID
- Malformed message ID format
- Message ID with invalid characters

**Resolution**:
1. Verify message ID is provided
2. Check message ID format is correct
3. Obtain message ID from a valid source
4. Re-enter corrected value when prompted

### Logic Errors (LOGIC_ERROR)

**Cause**: Message ID not found or inaccessible

**Error Message**: "We couldn't fetch the email because the message ID appears to be incorrect. Please check the message ID and try again."

**Common Scenarios**:
- Message has been permanently deleted
- Message ID from different Gmail account
- Message not accessible due to permissions
- Typo in message ID

**Resolution**:
1. Verify the message ID is correct
2. Check the message hasn't been deleted
3. Ensure you're using the correct Gmail account
4. Obtain a fresh message ID from a fetch operation
5. Verify account has access to the message

### Authorization Errors (MustAuthorizeException)

**Cause**: User not authenticated or token expired

**Resolution**:
1. Complete OAuth authentication flow
2. Verify authentication credentials
3. Re-authenticate if token expired
4. Check OAuth scopes include gmail.readonly or gmail.modify

## Usage Examples

### Example 1: Fetch Specific Email

**Input**:
```
Message ID: "18c5f2a3b4d6e789"
```

**Output**:
```json
{
  "Mail": {
    "messageId": "18c5f2a3b4d6e789",
    "threadId": "18c5f2a3b4d6e789",
    "from": "sender@example.com",
    "to": ["recipient@example.com"],
    "subject": "Project Update",
    "body": "The project is progressing well...",
    "date": "2025-10-04T10:30:00Z",
    "labels": ["INBOX", "UNREAD"],
    "attachments": [],
    "isRead": false
  }
}
```

**Result**: Complete email details retrieved successfully

### Example 2: Fetch Email with Attachments

**Input**:
```
Message ID: "18c5f2a3b4d6e789"
```

**Output**:
```json
{
  "Mail": {
    "messageId": "18c5f2a3b4d6e789",
    "from": "client@example.com",
    "to": ["you@example.com"],
    "subject": "Invoice #12345",
    "body": "Please find the invoice attached.",
    "attachments": [
      {
        "filename": "invoice.pdf",
        "mimeType": "application/pdf",
        "size": 245678
      }
    ]
  }
}
```

**Result**: Email with attachment details retrieved

### Example 3: Invalid Message ID

**Input**:
```
Message ID: "invalid-id-12345"
```

**Output**:
```json
{
  "Mail": null
}
```

**Result**: Empty response for invalid message ID

### Example 4: Empty Message ID with Retry

**Input**:
```
Message ID: ""
```

**Output**:
```json
{
  "error": {
    "type": "INPUT_ERROR",
    "message": "Message ID cannot be empty"
  },
  "retryPrompt": true
}
```

**Result**: Validation error prompts user to provide message ID

### Example 5: Message Not Found

**Input**:
```
Message ID: "18c5f2a3b4d6e789"
```

**Output**:
```json
{
  "error": {
    "type": "LOGIC_ERROR",
    "message": "We couldn't fetch the email because the message ID appears to be incorrect. Please check the message ID and try again."
  }
}
```

**Result**: Logic error for non-existent message

## Business Rules

1. **Unique Identifier**: Each email has a unique, permanent message ID in Gmail
2. **Complete Details**: Returns all available email metadata and content
3. **Attachment Information**: Includes attachment metadata (filename, size, type)
4. **Label Information**: Returns all labels applied to the email
5. **Thread Association**: Includes thread ID for conversation tracking
6. **Empty Response**: Returns null for invalid or non-existent message IDs

## Limitations

1. **Gmail API Limits**: Subject to Gmail API quotas and rate limits
2. **Account Scope**: Can only fetch messages from authenticated user's account
3. **Deleted Messages**: Cannot retrieve permanently deleted messages
4. **Attachment Content**: Returns attachment metadata, not full content (use separate download)
5. **Scope Requirements**: Requires `gmail.readonly` or `gmail.modify` OAuth scope

## Best Practices

### 1. Validate Message IDs
Always verify message IDs are from valid sources before attempting to fetch.

### 2. Handle Empty Responses
Check for null responses and handle gracefully when message is not found.

### 3. Cache Message Details
Consider caching frequently accessed email details to reduce API calls.

### 4. Error Handling
Implement proper error handling for all scenarios including not found, invalid ID, and authorization errors.

### 5. Use for Verification
Use this request to verify message existence before performing operations like reply or forward.

### 6. Monitor API Usage
Track fetch operations to stay within Gmail API quotas.

## Common Use Cases

### 1. Email Detail Display
```
Scenario: Display complete email details in user interface
Action: Fetch email by message ID when user selects an email
Result: Full email content and metadata displayed to user
```

### 2. Email Processing Workflow
```
Scenario: Process specific emails in automated workflow
Action: Fetch email details using message ID from notification
Result: Email content available for workflow processing
```

### 3. Attachment Verification
```
Scenario: Check if email has attachments before processing
Action: Fetch email by ID and check attachments array
Result: Workflow proceeds based on attachment presence
```

### 4. Email Validation
```
Scenario: Verify email exists before replying or forwarding
Action: Fetch email by message ID to confirm existence
Result: Proceed with reply/forward only if email exists
```

### 5. Audit Trail
```
Scenario: Retrieve email details for audit logging
Action: Fetch email by message ID and log complete details
Result: Complete email information stored in audit log
```

## Related Catalog Requests

- [Fetch Inbox](pages/FetchInbox.md) - Retrieve inbox emails with pagination
- [Fetch Sent](pages/FetchSent.md) - Retrieve sent emails
- [Fetch Mail Details By Query](pages/FetchMailDetailsByQuery.md) - Search emails using queries
- [Reply To Mail](pages/ReplyToMail.md) - Reply to fetched email
- [Forward Mail](pages/ForwardMail.md) - Forward fetched email

## Technical Implementation

### Helper Class
- **Class**: MessagingArea
- **Package**: app.krista.extensions.essentials.collaboration.gmail.catalog
- **Validation**: ValidationOrchestrator validates message ID format
- **Service**: Delegates to Account.getEmail() for message retrieval
- **Entity Conversion**: CatalogTypes.fromEmail() converts Gmail Email to Mail Details entity

### Telemetry Metrics
- **TELEMETRY_FETCH_MAIL_BY_MESSAGE_ID**: Total number of fetch requests
- **Tags**: message_id, validation_count
- **Success Tracking**: Records successful fetches with message ID
- **Error Tracking**: Records validation errors for invalid IDs
- **Retry Tracking**: Records when validation prompts for re-entry

## Troubleshooting

### Issue: Returns null for valid-looking message ID

**Cause**: Message doesn't exist or is inaccessible

**Solution**:
1. Verify the message hasn't been deleted
2. Check you're using the correct Gmail account
3. Ensure message ID is from the same account
4. Try fetching a different message to verify connectivity

### Issue: "Message ID cannot be empty"

**Cause**: Missing message ID parameter

**Solution**:
1. Provide a valid message ID
2. Obtain message ID from fetch operations or notifications
3. Verify parameter is being passed correctly

### Issue: "We couldn't fetch the email"

**Cause**: Message ID incorrect or message not accessible

**Solution**:
1. Double-check the message ID for typos
2. Verify message exists in Gmail
3. Ensure proper authentication
4. Try obtaining a fresh message ID

### Issue: Authentication errors

**Cause**: Invalid or expired OAuth token

**Solution**:
1. Re-authenticate with Gmail
2. Verify OAuth scopes include gmail.readonly
3. Check token hasn't expired
4. Review authentication configuration

## See Also

- [Extension Configuration](pages/ExtensionConfiguration.md)
- [Authentication Guide](pages/Authentication.md)
- [Gmail API Get Message](https://developers.google.com/gmail/api/reference/rest/v1/users.messages/get)
- [Fetch Inbox](pages/FetchInbox.md)

---
*Documentation updated according to Krista Extension Documentation Guidelines*

