# Move Message

## Overview

Moves an email message from one folder (label) to another in Gmail, allowing organization and management of emails across different folders.

## Request Details

- **Area**: Messaging
- **Type**: CHANGE_SYSTEM
- **Retry Support**: ✅ Yes (validation errors prompt for re-entry)

## Input Parameters

| Parameter Name | Type | Required | Description | Example |
|----------------|------|----------|-------------|---------|
| Message ID | Text | Yes | Unique identifier of the email message to be moved | "18c5f2a3b4d6e789" |
| Folder Name | Text | Yes | Name of the destination folder where the email will be moved | "Archive" |

### Parameter Details

**Message ID**: The unique Gmail message identifier. This can be obtained from other catalog requests like Fetch Inbox, Fetch Mail By Message Id, or email notification events.

**Folder Name**: The name of the destination Gmail folder (label). Common folder names include:
- `INBOX` - Main inbox
- `SENT` - Sent emails
- `TRASH` - Trash/deleted items
- `SPAM` - Spam folder
- `DRAFT` - Draft emails
- Custom label names (e.g., "Archive", "Important", "Work")

## Output Parameters

| Parameter Name | Type | Description |
|----------------|------|-------------|
| Response | Text | Status message indicating success or failure |

**Success Response**: `"success"`

**Failure Responses**:
- `"Invalid message id"` - Message ID not found
- `"failed."` - Folder name not found
- `"Error occurred while moving message to folder"` - System error

## Validation Rules

| Validation | Error Message | Resolution |
|------------|---------------|------------|
| Message ID required | "Message ID cannot be empty" | Provide a valid message ID |
| Message ID format | "Invalid message ID format" | Ensure message ID is in correct format |
| Folder name required | "Folder name cannot be empty" | Provide a valid folder name |
| Folder name exists | "Folder not found" | Use an existing folder name |

## Error Handling

### Input Errors (INPUT_ERROR)

**Cause**: Invalid or missing message ID or folder name

**Common Scenarios**:
- Empty message ID or folder name
- Malformed message ID
- Invalid characters in parameters

**Resolution**:
1. Verify message ID is correct
2. Verify folder name exists in Gmail
3. Check for typos or extra spaces
4. Re-enter corrected values when prompted

### Logic Errors (LOGIC_ERROR)

**Cause**: Message ID or folder name not found

**Common Scenarios**:
- Message has been deleted
- Folder/label doesn't exist
- Message not accessible
- Incorrect folder name spelling

**Resolution**:
1. Verify the message still exists
2. Check folder name spelling (case-sensitive)
3. Use Fetch All Labels to get valid folder names
4. Create the folder if it doesn't exist
5. Ensure message is accessible

### System Errors (SYSTEM_ERROR)

**Cause**: Gmail API errors, network issues, or permission problems

**Error Message**: "Error occurred while moving message to folder"

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

## Usage Examples

### Example 1: Move to Archive

**Input**:
```
Message ID: "18c5f2a3b4d6e789"
Folder Name: "Archive"
```

**Output**:
```json
{
  "Response": "success"
}
```

**Result**: Email moved successfully to Archive folder

### Example 2: Move to Trash

**Input**:
```
Message ID: "18c5f2a3b4d6e789"
Folder Name: "TRASH"
```

**Output**:
```json
{
  "Response": "success"
}
```

**Result**: Email moved to Trash folder

### Example 3: Move to Custom Label

**Input**:
```
Message ID: "18c5f2a3b4d6e789"
Folder Name: "Important"
```

**Output**:
```json
{
  "Response": "success"
}
```

**Result**: Email moved to custom "Important" label

### Example 4: Invalid Message ID

**Input**:
```
Message ID: "invalid-id"
Folder Name: "Archive"
```

**Output**:
```json
{
  "Response": "Invalid message id"
}
```

**Result**: Operation failed due to invalid message ID

### Example 5: Invalid Folder Name

**Input**:
```
Message ID: "18c5f2a3b4d6e789"
Folder Name: "NonExistentFolder"
```

**Output**:
```json
{
  "Response": "failed."
}
```

**Result**: Operation failed because folder doesn't exist

### Example 6: Validation Error with Retry

**Input**:
```
Message ID: ""
Folder Name: "Archive"
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
2. **Folder Must Exist**: Destination folder must exist in Gmail
3. **Single Folder**: Email is moved to exactly one destination folder
4. **Label System**: Gmail uses labels; moving changes the primary label
5. **Folder Names**: Folder names are case-sensitive
6. **System Folders**: Can move to system folders (INBOX, TRASH, SPAM, etc.)

## Limitations

1. **Gmail API Limits**: Subject to Gmail API quotas and rate limits
2. **Folder Existence**: Cannot move to non-existent folders
3. **Message Existence**: Can only move messages that exist
4. **Account Scope**: Can only move messages in authenticated user's account
5. **Scope Requirements**: Requires `gmail.modify` OAuth scope
6. **Label Behavior**: Gmail's label system may differ from traditional folder behavior

## Best Practices

### 1. Verify Folder Exists
Use Fetch All Labels to get valid folder names before moving messages.

### 2. Validate Message ID
Verify the message exists before attempting to move it.

### 3. Handle Errors Gracefully
Implement proper error handling for all failure scenarios.

### 4. Use Standard Folder Names
Prefer standard Gmail folder names (INBOX, TRASH, SPAM) for reliability.

### 5. Batch Operations
For moving multiple messages, consider implementing batch processing with error handling.

### 6. Monitor Operations
Track move success rates and log failures for troubleshooting.

## Common Use Cases

### 1. Email Organization
```
Scenario: Automatically organize emails into folders based on rules
Action: Move messages to appropriate folders using Move Message
Result: Emails organized automatically without manual intervention
```

### 2. Archive Old Emails
```
Scenario: Archive emails older than a certain date
Action: Fetch old emails and move them to Archive folder
Result: Inbox cleaned up with old emails archived
```

### 3. Spam Management
```
Scenario: Move suspected spam to spam folder
Action: Identify spam emails and move to SPAM folder
Result: Spam emails isolated from inbox
```

### 4. Workflow Processing
```
Scenario: Move processed emails to "Completed" folder
Action: After processing email, move to custom "Completed" label
Result: Processed emails tracked in dedicated folder
```

### 5. Cleanup Automation
```
Scenario: Move promotional emails to dedicated folder
Action: Identify promotional emails and move to "Promotions" folder
Result: Inbox focused on important emails
```

## Related Catalog Requests

- [Mark Message](pages/MarkMessage.md) - Mark emails as read or unread
- [Fetch All Labels](pages/FetchAllLabels.md) - Get list of available folders
- [Fetch Mail By Message Id](pages/FetchMailByMessageId.md) - Retrieve email details
- [Fetch Inbox](pages/FetchInbox.md) - Retrieve inbox emails
- [Fetch Mails By Label](pages/FetchMailsByLabel.md) - Retrieve emails from specific folder

## Technical Implementation

### Helper Class
- **Class**: MessagingArea
- **Package**: app.krista.extensions.essentials.collaboration.gmail.catalog
- **Validation**: ValidationOrchestrator validates message ID and folder name
- **Service**: Delegates to Account.getEmail() and Email.moveToFolder() for move operations

### Telemetry Metrics
- **TELEMETRY_MOVE_MESSAGE**: Total number of move requests
- **Tags**: message_id, folder_name, validation_count
- **Success Tracking**: Records successful moves with message ID and folder
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

### Issue: "failed."

**Cause**: Folder name not found

**Solution**:
1. Use Fetch All Labels to get valid folder names
2. Check folder name spelling (case-sensitive)
3. Verify the folder exists in Gmail
4. Create the folder if needed
5. Use standard folder names (INBOX, TRASH, etc.)

### Issue: "Error occurred while moving message to folder"

**Cause**: System error during move operation

**Solution**:
1. Check network connectivity
2. Verify Gmail API is accessible
3. Ensure authentication is valid
4. Check API quotas and rate limits
5. Retry the operation
6. Review logs for specific error details

### Issue: "Message ID cannot be empty"

**Cause**: Missing message ID parameter

**Solution**:
1. Provide a valid message ID
2. Obtain message ID from fetch operations
3. Verify parameter is being passed correctly

### Issue: "Folder name cannot be empty"

**Cause**: Missing folder name parameter

**Solution**:
1. Provide a valid folder name
2. Use Fetch All Labels to get available folders
3. Verify parameter is being passed correctly

## See Also

- [Extension Configuration](pages/ExtensionConfiguration.md)
- [Authentication Guide](pages/Authentication.md)
- [Gmail API Modify Message](https://developers.google.com/gmail/api/reference/rest/v1/users.messages/modify)
- [Fetch All Labels](pages/FetchAllLabels.md)

---
*Documentation updated according to Krista Extension Documentation Guidelines*

