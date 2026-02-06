# Mark Message

## Overview

Marks an email message as read or unread in Gmail, allowing management of email read status for organization and workflow purposes.

## Request Details

- **Area**: Messaging
- **Type**: CHANGE_SYSTEM
- **Retry Support**: ✅ Yes (validation errors prompt for re-entry)

## Input Parameters

| Parameter Name | Type | Required | Description | Example |
|----------------|------|----------|-------------|---------|
| Message ID | Text | Yes | Unique identifier of the email message to mark | "18c5f2a3b4d6e789" |
| Label | PickOne(Read\|Unread) | Yes | Mark as "Read" or "Unread" | "Read" |

### Parameter Details

**Message ID**: The unique Gmail message identifier. This can be obtained from other catalog requests like Fetch Inbox, Fetch Mail By Message Id, or email notification events.

**Label**: The read status to apply to the email. Must be one of:
- `Read` - Mark the email as read (removes UNREAD label)
- `Unread` - Mark the email as unread (adds UNREAD label)

**Note**: The label parameter is case-insensitive ("read", "Read", "READ" all work).

## Output Parameters

| Parameter Name | Type | Description |
|----------------|------|-------------|
| Response | Text | Status message indicating success or failure |

**Success Response**: `"success"`

**Failure Responses**:
- `"Invalid message id"` - Message ID not found
- `"Invalid label"` - Label is not "Read" or "Unread"
- `"Error occurred while marking message"` - System error

## Validation Rules

| Validation | Error Message | Resolution |
|------------|---------------|------------|
| Message ID required | "Message ID cannot be empty" | Provide a valid message ID |
| Message ID format | "Invalid message ID format" | Ensure message ID is in correct format |
| Label value | "Invalid label" | Use "Read" or "Unread" only |

## Error Handling

### Input Errors (INPUT_ERROR)

**Cause**: Invalid or missing message ID

**Common Scenarios**:
- Empty message ID
- Malformed message ID
- Message ID with invalid characters

**Resolution**:
1. Verify message ID is correct
2. Obtain message ID from a fetch operation
3. Check for typos or extra spaces
4. Re-enter corrected value when prompted

### Logic Errors (LOGIC_ERROR)

**Cause**: Message ID not found or invalid label value

**Common Scenarios**:
- Message has been deleted
- Message ID from different account
- Message not accessible
- Label is not "Read" or "Unread"

**Resolution**:
1. Verify the message still exists
2. Check you're using the correct Gmail account
3. Ensure message is accessible
4. Use only "Read" or "Unread" for label parameter
5. Use a different message ID

### System Errors (SYSTEM_ERROR)

**Cause**: Gmail API errors, network issues, or permission problems

**Error Message**: "Error occurred while marking message"

**Common Scenarios**:
- Network connectivity problems
- Gmail API temporarily unavailable
- Permission issues
- API rate limits exceeded

**Resolution**:
1. Check network connectivity
2. Verify Gmail API is accessible
3. Ensure proper authentication
4. Retry after a brief delay
5. Check Gmail API quotas

### Authorization Errors (MustAuthorizeException)

**Cause**: User not authenticated or token expired

**Resolution**:
1. Complete OAuth authentication flow
2. Verify authentication credentials
3. Re-authenticate if token expired
4. Check OAuth scopes include gmail.modify

## Usage Examples

### Example 1: Mark as Read

**Input**:
```
Message ID: "18c5f2a3b4d6e789"
Label: "Read"
```

**Output**:
```json
{
  "Response": "success"
}
```

**Result**: Email marked as read successfully

### Example 2: Mark as Unread

**Input**:
```
Message ID: "18c5f2a3b4d6e789"
Label: "Unread"
```

**Output**:
```json
{
  "Response": "success"
}
```

**Result**: Email marked as unread successfully

### Example 3: Case-Insensitive Label

**Input**:
```
Message ID: "18c5f2a3b4d6e789"
Label: "read"
```

**Output**:
```json
{
  "Response": "success"
}
```

**Result**: Email marked as read (case-insensitive)

### Example 4: Invalid Message ID

**Input**:
```
Message ID: "invalid-id"
Label: "Read"
```

**Output**:
```json
{
  "Response": "Invalid message id"
}
```

**Result**: Operation failed due to invalid message ID

### Example 5: Invalid Label

**Input**:
```
Message ID: "18c5f2a3b4d6e789"
Label: "Archived"
```

**Output**:
```json
{
  "Response": "Invalid label"
}
```

**Result**: Operation failed due to invalid label value

### Example 6: Validation Error with Retry

**Input**:
```
Message ID: ""
Label: "Read"
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

## Business Rules

1. **Message ID Required**: A valid message ID must be provided
2. **Label Values**: Only "Read" and "Unread" are valid label values
3. **Case Insensitive**: Label parameter is case-insensitive
4. **Read Status**: "Read" removes UNREAD label, "Unread" adds UNREAD label
5. **Immediate Effect**: Status change is immediate in Gmail
6. **Idempotent**: Marking as read when already read has no effect

## Limitations

1. **Gmail API Limits**: Subject to Gmail API quotas and rate limits
2. **Two States Only**: Can only mark as read or unread (no other states)
3. **Message Existence**: Can only mark messages that exist
4. **Account Scope**: Can only mark messages in authenticated user's account
5. **Scope Requirements**: Requires `gmail.modify` OAuth scope
6. **No Bulk Operations**: Marks one message at a time

## Best Practices

### 1. Validate Message ID
Verify the message exists before attempting to mark it.

### 2. Use Consistent Case
While case-insensitive, use consistent casing ("Read"/"Unread") for clarity.

### 3. Handle Errors Gracefully
Implement proper error handling for all failure scenarios.

### 4. Batch Processing
For marking multiple messages, implement batch processing with error handling.

### 5. Verify Success
Check response for "success" before proceeding with dependent operations.

### 6. Monitor Operations
Track mark success rates and log failures for troubleshooting.

## Common Use Cases

### 1. Mark Processed Emails as Read
```
Scenario: Mark emails as read after processing in workflow
Action: Process email then mark as read using Mark Message
Result: Processed emails marked as read automatically
```

### 2. Mark Important Emails as Unread
```
Scenario: Mark important emails as unread for follow-up
Action: Identify important emails and mark as unread
Result: Important emails remain visible as unread
```

### 3. Inbox Zero Workflow
```
Scenario: Mark all inbox emails as read after review
Action: Fetch inbox emails and mark each as read
Result: Inbox shows no unread emails
```

### 4. Email Triage
```
Scenario: Mark emails as read/unread based on priority
Action: Evaluate email priority and set read status accordingly
Result: Read status reflects email priority
```

### 5. Notification Management
```
Scenario: Mark notification emails as read after processing
Action: Process notification then mark as read
Result: Notification emails don't clutter unread count
```

## Related Catalog Requests

- [Move Message](pages/MoveMessage.md) - Move emails between folders
- [Fetch Mail By Message Id](pages/FetchMailByMessageId.md) - Retrieve email details
- [Fetch Inbox](pages/FetchInbox.md) - Retrieve inbox emails
- [Fetch Mails By Label](pages/FetchMailsByLabel.md) - Retrieve emails by label

## Technical Implementation

### Helper Class
- **Class**: MessagingArea
- **Package**: app.krista.extensions.essentials.collaboration.gmail.catalog
- **Validation**: ValidationOrchestrator validates message ID
- **Service**: Delegates to Account.getEmail() and Email.markAsRead()/markAsUnread() for status change

### Telemetry Metrics
- **TELEMETRY_MARK_MESSAGE**: Total number of mark requests
- **Tags**: message_id, label, validation_count
- **Success Tracking**: Records successful marks with message ID and label
- **Error Tracking**: Records validation errors and system errors
- **Retry Tracking**: Records when validation prompts for re-entry

## Troubleshooting

### Issue: "Invalid message id"

**Cause**: Message ID not found in Gmail

**Solution**:
1. Verify the message ID is correct
2. Check the message hasn't been deleted
3. Ensure you're using the correct Gmail account
4. Obtain a fresh message ID from a fetch operation

### Issue: "Invalid label"

**Cause**: Label value is not "Read" or "Unread"

**Solution**:
1. Use only "Read" or "Unread" for the label parameter
2. Check for typos in label value
3. Verify parameter is being passed correctly

### Issue: "Error occurred while marking message"

**Cause**: System error during mark operation

**Solution**:
1. Check network connectivity
2. Verify Gmail API is accessible
3. Ensure authentication is valid
4. Retry the operation
5. Review logs for specific error details

### Issue: "Message ID cannot be empty"

**Cause**: Missing message ID parameter

**Solution**:
1. Provide a valid message ID
2. Obtain message ID from fetch operations
3. Verify parameter is being passed correctly

## See Also

- [Extension Configuration](pages/ExtensionConfiguration.md)
- [Authentication Guide](pages/Authentication.md)
- [Gmail API Modify Message](https://developers.google.com/gmail/api/reference/rest/v1/users.messages/modify)
- [Move Message](pages/MoveMessage.md)

---
*Documentation updated according to Krista Extension Documentation Guidelines*

