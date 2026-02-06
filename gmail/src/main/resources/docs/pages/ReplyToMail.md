# Reply To Mail

## Overview

Replies to an existing email message with optional attachments, sending the response to the original sender.

## Request Details

- **Area**: Messaging
- **Type**: CHANGE_SYSTEM
- **Retry Support**: ✅ Yes (validation errors prompt for re-entry)

## Input Parameters

| Parameter Name | Type | Required | Description | Example |
|----------------|------|----------|-------------|---------|
| Message ID | Text | Yes | Unique identifier of the email message to reply to | "18c5f2a3b4d6e789" |
| Message | RichText | Yes | Content of the reply message | "Thank you for your email..." |
| Attachments | File | No | Files to attach to the reply | [File objects] |

### Parameter Details

**Message ID**: The unique Gmail message identifier. This can be obtained from other catalog requests like Fetch Inbox, Fetch Mail By Message Id, or email notification events.

**Message**: The reply message content. Supports rich text formatting including HTML. Line breaks (`\n`) are automatically converted to HTML breaks (`<br>`).

**Attachments**: Optional list of file objects to attach to the reply. Supports multiple attachments.

## Output Parameters

| Parameter Name | Type | Description |
|----------------|------|-------------|
| Message | Text | Status message indicating success or failure |

**Success Response**: `"success"`

**Failure Responses**: 
- `"Invalid message id"` - Message ID not found
- `"Reply to mail failed for ID: {messageID} with error message: {error}"` - System error
- `"Error occurred while replying to mail"` - Generic error

## Validation Rules

| Validation | Error Message | Resolution |
|------------|---------------|------------|
| Message ID required | "Message ID cannot be empty" | Provide a valid message ID |
| Message ID format | "Invalid message ID format" | Ensure message ID is in correct format |

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

**Cause**: Gmail API errors, network issues, or I/O failures

**Common Scenarios**:
- Network connectivity problems
- Gmail API temporarily unavailable
- Attachment processing failures
- Permission issues

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

### Example 1: Simple Reply

**Input**:
```
Message ID: "18c5f2a3b4d6e789"
Message: "Thank you for your email. I will review and get back to you soon."
```

**Output**:
```json
{
  "Message": "success"
}
```

**Result**: Reply sent successfully to original sender

### Example 2: Reply with Attachment

**Input**:
```
Message ID: "18c5f2a3b4d6e789"
Message: "Please find the requested document attached."
Attachments: [document.pdf]
```

**Output**:
```json
{
  "Message": "success"
}
```

**Result**: Reply sent with PDF attachment

### Example 3: Reply with Rich Text

**Input**:
```
Message ID: "18c5f2a3b4d6e789"
Message: "Hello,\n\nThank you for reaching out.\n\nBest regards,\nJohn"
```

**Output**:
```json
{
  "Message": "success"
}
```

**Result**: Reply sent with formatted line breaks

### Example 4: Invalid Message ID

**Input**:
```
Message ID: "invalid-id"
Message: "Test reply"
```

**Output**:
```json
{
  "Message": "Invalid message id"
}
```

**Result**: Operation failed due to invalid message ID

### Example 5: Validation Error with Retry

**Input**:
```
Message ID: ""
Message: "Test reply"
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

1. **Message ID Required**: A valid message ID must be provided to identify the email to reply to
2. **Original Sender**: Reply is sent to the original sender of the email
3. **Thread Preservation**: Reply maintains the email thread/conversation
4. **Subject Line**: Reply uses "Re: " prefix with original subject
5. **Rich Text Support**: Message field supports HTML formatting
6. **Line Break Conversion**: Newline characters are automatically converted to HTML breaks

## Limitations

1. **Gmail API Limits**: Subject to Gmail API quotas and rate limits
2. **Attachment Size**: Total attachment size limited by Gmail (25 MB per email)
3. **Message Existence**: Can only reply to messages that still exist in Gmail
4. **Account Access**: Can only reply to messages in the authenticated user's account
5. **Scope Requirements**: Requires `gmail.modify` or `gmail.send` OAuth scope

## Best Practices

### 1. Validate Message ID
Always verify the message ID exists before attempting to reply to avoid errors.

### 2. Handle Attachments Carefully
- Check file sizes before attaching
- Verify file types are allowed
- Consider using cloud storage links for large files

### 3. Format Messages Properly
- Use clear, professional language
- Include proper line breaks for readability
- Test rich text formatting before sending

### 4. Implement Error Handling
- Catch and log all errors
- Provide clear error messages to users
- Implement retry logic for transient failures

### 5. Preserve Context
- Include relevant context in the reply
- Reference the original message when appropriate
- Maintain professional email etiquette

### 6. Monitor Operations
- Track reply success rates
- Monitor for failures
- Log message IDs for troubleshooting

## Common Use Cases

### 1. Automated Responses
```
Scenario: Automatically reply to customer inquiries
Action: Use Reply To Mail with predefined response templates
Result: Customers receive timely automated responses
```

### 2. Document Sharing
```
Scenario: Reply to requests with attached documents
Action: Use Reply To Mail with file attachments
Result: Recipients receive requested documents in reply
```

### 3. Workflow Automation
```
Scenario: Reply to emails as part of approval workflow
Action: Trigger Reply To Mail when approval is granted
Result: Automated workflow responses sent to requesters
```

### 4. Support Ticket Responses
```
Scenario: Reply to support tickets with solutions
Action: Use Reply To Mail with formatted solution text
Result: Support responses sent maintaining ticket thread
```

## Related Catalog Requests

- [Reply To All](pages/ReplyToAll.md) - Reply to all recipients
- [Reply To Mail With CC and BCC](pages/ReplyToMailWithCCAndBCC.md) - Reply with additional recipients
- [Forward Mail](pages/ForwardMail.md) - Forward emails to other recipients
- [Fetch Mail By Message Id](pages/FetchMailByMessageId.md) - Retrieve email details
- [Send Mail](pages/SendMail.md) - Send new emails

## Technical Implementation

### Helper Class
- **Class**: MessagingArea
- **Package**: app.krista.extensions.essentials.collaboration.gmail.catalog
- **Validation**: ValidationOrchestrator validates message ID
- **Service**: Delegates to Account.getEmail() and Email.replyText() for reply operations

### Telemetry Metrics
- **TELEMETRY_REPLY_TO_MAIL**: Total number of reply requests
- **Tags**: message_id, validation_count
- **Success Tracking**: Records successful replies with message ID
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

### Issue: "Reply to mail failed"

**Cause**: System error during reply operation

**Solution**:
1. Check error message for specific details
2. Verify network connectivity
3. Ensure Gmail API is accessible
4. Check attachment sizes if applicable
5. Retry the operation

### Issue: "Error occurred while replying to mail"

**Cause**: Generic system error

**Solution**:
1. Review extension logs for details
2. Check authentication is valid
3. Verify Gmail API permissions
4. Ensure message is accessible
5. Contact support if issue persists

### Issue: "Message ID cannot be empty"

**Cause**: Missing message ID parameter

**Solution**:
1. Provide a valid message ID
2. Obtain message ID from fetch operations
3. Verify parameter is being passed correctly

## See Also

- [Extension Configuration](pages/ExtensionConfiguration.md)
- [Authentication Guide](pages/Authentication.md)
- [Gmail API Reply](https://developers.google.com/gmail/api/guides/sending#replying_to_a_message)
- [Send Mail](pages/SendMail.md)

---
*Documentation updated according to Krista Extension Documentation Guidelines*

