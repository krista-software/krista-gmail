# Reply To All

## Overview

Replies to all recipients of an email message, including the original sender and all recipients in the To and Cc fields, with optional attachments.

## Request Details

- **Area**: Messaging
- **Type**: CHANGE_SYSTEM
- **Retry Support**: ✅ Yes (validation errors prompt for re-entry)

## Input Parameters

| Parameter Name | Type | Required | Description | Example |
|----------------|------|----------|-------------|---------|
| Message Id | Text | Yes | Unique identifier of the email message to reply to all recipients | "18c5f2a3b4d6e789" |
| Body | Paragraph | Yes | Content of the reply message that will be sent to all recipients | "Thank you all for your input..." |
| Attachments | File | No | Optional files to attach to the reply message | [File objects] |

### Parameter Details

**Message Id**: The unique Gmail message identifier. This can be obtained from other catalog requests like Fetch Inbox, Fetch Mail By Message Id, or email notification events.

**Body**: The reply message content. Supports paragraph text formatting. Line breaks (`\n`) are automatically converted to HTML breaks (`<br>`).

**Attachments**: Optional list of file objects to attach to the reply. Supports multiple attachments.

## Output Parameters

| Parameter Name | Type | Description |
|----------------|------|-------------|
| Is Successful | Switch (Boolean) | Indicates whether the reply to all operation was successful |

**Success Response**: `true`

**Failure Response**: `false`

## Validation Rules

| Validation | Error Message | Resolution |
|------------|---------------|------------|
| Message ID required | "Message ID cannot be empty" | Provide a valid message ID |
| Message ID format | "Invalid message ID format" | Ensure message ID is in correct format |

## Error Handling

### Input Errors (INPUT_ERROR)

**Cause**: Invalid or missing message ID

**Error Message**: "Failed to reply all"

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

**Cause**: Message ID not found in Gmail

**Common Scenarios**:
- Message has been deleted
- Message ID from different account
- Message not accessible

**Resolution**:
1. Verify the message still exists
2. Check you're using the correct Gmail account
3. Ensure message is accessible
4. Use a different message ID

### System Errors (SYSTEM_ERROR)

**Cause**: Gmail API errors, messaging exceptions, or I/O failures

**Common Scenarios**:
- Network connectivity problems
- Gmail API temporarily unavailable
- Attachment processing failures
- Permission issues
- Email sending failures

**Resolution**:
1. Check network connectivity
2. Verify Gmail API is accessible
3. Reduce attachment sizes if needed
4. Retry after a brief delay
5. Check authentication is valid

### Authorization Errors (MustAuthorizeException)

**Cause**: User not authenticated or token expired

**Resolution**:
1. Complete OAuth authentication flow
2. Verify authentication credentials
3. Re-authenticate if token expired
4. Check OAuth scopes include gmail.modify

## Usage Examples

### Example 1: Simple Reply to All

**Input**:
```
Message Id: "18c5f2a3b4d6e789"
Body: "Thank you all for your feedback. I will incorporate these suggestions."
```

**Output**:
```json
{
  "Is Successful": true
}
```

**Result**: Reply sent successfully to all recipients

### Example 2: Reply to All with Attachment

**Input**:
```
Message Id: "18c5f2a3b4d6e789"
Body: "Please find the updated document attached for everyone's review."
Attachments: [updated_document.pdf]
```

**Output**:
```json
{
  "Is Successful": true
}
```

**Result**: Reply sent to all recipients with PDF attachment

### Example 3: Reply to All with Formatted Text

**Input**:
```
Message Id: "18c5f2a3b4d6e789"
Body: "Team,\n\nThank you for the discussion.\n\nNext steps:\n1. Review proposal\n2. Schedule follow-up\n\nBest regards"
```

**Output**:
```json
{
  "Is Successful": true
}
```

**Result**: Reply sent with formatted line breaks to all recipients

### Example 4: Invalid Message ID

**Input**:
```
Message Id: "invalid-id"
Body: "Test reply"
```

**Output**:
```json
{
  "Is Successful": false
}
```

**Result**: Operation failed due to invalid message ID

### Example 5: Validation Error with Retry

**Input**:
```
Message Id: ""
Body: "Test reply"
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
2. **All Recipients**: Reply is sent to original sender and all To/Cc recipients
3. **Thread Preservation**: Reply maintains the email thread/conversation
4. **Subject Line**: Reply uses "Re: " prefix with original subject
5. **Recipient Visibility**: All recipients can see who else received the reply
6. **Line Break Conversion**: Newline characters are automatically converted to HTML breaks

## Limitations

1. **Gmail API Limits**: Subject to Gmail API quotas and rate limits
2. **Attachment Size**: Total attachment size limited by Gmail (25 MB per email)
3. **Message Existence**: Can only reply to messages that still exist in Gmail
4. **Account Access**: Can only reply to messages in the authenticated user's account
5. **Scope Requirements**: Requires `gmail.modify` or `gmail.send` OAuth scope
6. **Recipient Limits**: Gmail limits number of recipients per email

## Best Practices

### 1. Validate Message ID
Always verify the message ID exists before attempting to reply to all.

### 2. Consider Recipient Count
Be aware of how many recipients will receive the reply to all.

### 3. Use Appropriate Content
Ensure reply content is relevant to all recipients on the thread.

### 4. Handle Attachments Carefully
- Check file sizes before attaching
- Verify all recipients need the attachments
- Consider using cloud storage links for large files

### 5. Format Messages Properly
- Use clear, professional language
- Include proper line breaks for readability
- Ensure content is appropriate for all recipients

### 6. Implement Error Handling
- Catch and log all errors
- Provide clear error messages to users
- Implement retry logic for transient failures

## Common Use Cases

### 1. Team Collaboration
```
Scenario: Reply to team discussions with updates for everyone
Action: Use Reply To All to keep entire team informed
Result: All team members receive updates in context
```

### 2. Meeting Follow-ups
```
Scenario: Send meeting notes to all attendees
Action: Reply to all with meeting summary and action items
Result: All attendees receive consistent information
```

### 3. Document Distribution
```
Scenario: Share updated documents with all stakeholders
Action: Reply to all with attached updated documents
Result: All stakeholders receive latest version
```

### 4. Group Decisions
```
Scenario: Communicate decisions to all involved parties
Action: Reply to all with decision and rationale
Result: All parties informed of decision simultaneously
```

### 5. Status Updates
```
Scenario: Provide project status to all team members
Action: Reply to all with status update
Result: Entire team stays synchronized on project status
```

## Related Catalog Requests

- [Reply To Mail](pages/ReplyToMail.md) - Reply to original sender only
- [Reply To All With CC and BCC](pages/ReplyToAllWithCCAndBCC.md) - Reply to all with additional recipients
- [Forward Mail](pages/ForwardMail.md) - Forward emails to other recipients
- [Fetch Mail By Message Id](pages/FetchMailByMessageId.md) - Retrieve email details
- [Send Mail](pages/SendMail.md) - Send new emails

## Technical Implementation

### Helper Class
- **Class**: MessagingArea
- **Package**: app.krista.extensions.essentials.collaboration.gmail.catalog
- **Validation**: ValidationOrchestrator validates message ID
- **Service**: Delegates to Account.getEmail() and Email.replyToAll() for reply operations

### Telemetry Metrics
- **TELEMETRY_REPLY_TO_ALL**: Total number of reply to all requests
- **Tags**: message_id, validation_count
- **Success Tracking**: Records successful replies with message ID
- **Error Tracking**: Records validation errors and system errors
- **Retry Tracking**: Records when validation prompts for re-entry

## Troubleshooting

### Issue: Returns false for valid message ID

**Cause**: Message not found or system error during reply

**Solution**:
1. Verify the message ID is correct
2. Check the message hasn't been deleted
3. Review logs for specific error details
4. Ensure Gmail API is accessible
5. Retry the operation

### Issue: "Failed to reply all"

**Cause**: Input error or system failure

**Solution**:
1. Check error logs for specific details
2. Verify message ID is valid
3. Ensure network connectivity
4. Check authentication is valid
5. Verify all recipients are valid

### Issue: "Message ID cannot be empty"

**Cause**: Missing message ID parameter

**Solution**:
1. Provide a valid message ID
2. Obtain message ID from fetch operations
3. Verify parameter is being passed correctly

### Issue: Attachment failures

**Cause**: Attachment size or processing errors

**Solution**:
1. Check attachment file sizes
2. Reduce attachment sizes if needed
3. Verify attachment file formats
4. Try without attachments to isolate issue

## See Also

- [Extension Configuration](pages/ExtensionConfiguration.md)
- [Authentication Guide](pages/Authentication.md)
- [Gmail API Reply](https://developers.google.com/gmail/api/guides/sending#replying_to_a_message)
- [Reply To Mail](pages/ReplyToMail.md)

---
*Documentation updated according to Krista Extension Documentation Guidelines*

