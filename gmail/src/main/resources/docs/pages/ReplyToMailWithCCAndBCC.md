# Reply To Mail With CC and BCC

## Overview

Replies to an email message with advanced options to add additional recipients in To, Cc, Bcc fields, include attachments, and set a custom reply-to address.

## Request Details

- **Area**: Messaging
- **Type**: CHANGE_SYSTEM
- **Retry Support**: ✅ Yes (validation errors prompt for re-entry)

## Input Parameters

| Parameter Name | Type | Required | Description | Example |
|----------------|------|----------|-------------|---------|
| Message Id | Text | Yes | Unique identifier of the email message to reply to | "18c5f2a3b4d6e789" |
| Message | RichText | No | Content of the reply message | "Thank you for your email..." |
| Attachments | File | No | Optional files to attach to the reply message | [File objects] |
| To | Text | No | Additional recipients to include in the To field (comma-separated) | "user1@example.com, user2@example.com" |
| Cc | Text | No | Additional recipients to include in the Cc field (comma-separated) | "manager@example.com" |
| Bcc | Text | No | Additional recipients to include in the Bcc field (comma-separated) | "archive@example.com" |
| Reply To | Text | No | Email address to set as the reply-to address | "noreply@example.com" |

### Parameter Details

**Message Id**: The unique Gmail message identifier. This can be obtained from other catalog requests like Fetch Inbox, Fetch Mail By Message Id, or email notification events.

**Message**: The reply message content. Supports rich text formatting. Line breaks (`\n`) are automatically converted to HTML breaks (`<br>`).

**Attachments**: Optional list of file objects to attach to the reply. Supports multiple attachments.

**To**: Additional recipients to add to the To field. Multiple addresses should be comma-separated. These are added to the original sender.

**Cc**: Carbon copy recipients. Multiple addresses should be comma-separated. These recipients receive a copy of the reply.

**Bcc**: Blind carbon copy recipients. Multiple addresses should be comma-separated. These recipients receive a copy without other recipients knowing.

**Reply To**: Custom reply-to email address. When recipients reply, their response will go to this address instead of the sender's address.

## Output Parameters

| Parameter Name | Type | Description |
|----------------|------|-------------|
| Message | Text | Status message indicating success or failure of the reply operation |

**Success Response**: `"success"`

**Failure Responses**:
- `"Invalid message id"` - Message ID not found
- `"Reply to mail failed for ID: {messageId} with error message: {error}"` - I/O error during reply
- `"Failed to reply to mail"` - General failure

## Validation Rules

| Validation | Error Message | Resolution |
|------------|---------------|------------|
| Message ID required | "Message ID cannot be empty" | Provide a valid message ID |
| Message ID format | "Invalid message ID format" | Ensure message ID is in correct format |
| To email format | "Invalid email format in To field" | Ensure all To addresses are valid |
| Cc email format | "Invalid email format in Cc field" | Ensure all Cc addresses are valid |
| Bcc email format | "Invalid email format in Bcc field" | Ensure all Bcc addresses are valid |
| Reply To email format | "Invalid email format in Reply To field" | Ensure Reply To address is valid |

## Error Handling

### Input Errors (INPUT_ERROR)

**Cause**: Invalid message ID or email addresses

**Common Scenarios**:
- Empty or malformed message ID
- Invalid email address format in To, Cc, Bcc, or Reply To fields
- Invalid characters in email addresses

**Resolution**:
1. Verify message ID is correct
2. Verify all email addresses are valid format
3. Check for typos or extra spaces
4. Re-enter corrected values when prompted

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

**Cause**: Gmail API errors, I/O failures, or attachment processing issues

**Common Scenarios**:
- Network connectivity problems
- Gmail API temporarily unavailable
- Attachment processing failures
- Email sending failures
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
4. Check OAuth scopes include gmail.modify or gmail.send

## Usage Examples

### Example 1: Simple Reply with Cc

**Input**:
```
Message Id: "18c5f2a3b4d6e789"
Message: "Thank you for your email. I've copied my manager."
Cc: "manager@example.com"
```

**Output**:
```json
{
  "Message": "success"
}
```

**Result**: Reply sent to original sender with manager in Cc

### Example 2: Reply with Additional To Recipients

**Input**:
```
Message Id: "18c5f2a3b4d6e789"
Message: "Adding the team to this conversation."
To: "team1@example.com, team2@example.com"
```

**Output**:
```json
{
  "Message": "success"
}
```

**Result**: Reply sent to original sender plus additional team members

### Example 3: Reply with Bcc for Archive

**Input**:
```
Message Id: "18c5f2a3b4d6e789"
Message: "Replying with archive copy."
Bcc: "archive@example.com"
```

**Output**:
```json
{
  "Message": "success"
}
```

**Result**: Reply sent with hidden copy to archive

### Example 4: Reply with Custom Reply-To

**Input**:
```
Message Id: "18c5f2a3b4d6e789"
Message: "Please reply to our support team."
Reply To: "support@example.com"
```

**Output**:
```json
{
  "Message": "success"
}
```

**Result**: Reply sent with custom reply-to address

### Example 5: Complete Reply with All Fields

**Input**:
```
Message Id: "18c5f2a3b4d6e789"
Message: "Comprehensive reply with all options."
To: "colleague@example.com"
Cc: "manager@example.com"
Bcc: "archive@example.com"
Reply To: "team@example.com"
Attachments: [report.pdf]
```

**Output**:
```json
{
  "Message": "success"
}
```

**Result**: Reply sent with all recipients, attachment, and custom reply-to

### Example 6: Invalid Message ID

**Input**:
```
Message Id: "invalid-id"
Message: "Test reply"
```

**Output**:
```json
{
  "Message": "Invalid message id"
}
```

**Result**: Operation failed due to invalid message ID

## Business Rules

1. **Message ID Required**: A valid message ID must be provided
2. **Optional Fields**: All fields except Message ID are optional
3. **Email Validation**: All email addresses must be valid format
4. **Multiple Recipients**: Use comma separation for multiple addresses
5. **Reply-To Override**: Reply To field overrides default sender address
6. **Line Break Conversion**: Newline characters are automatically converted to HTML breaks
7. **Original Sender**: Original sender is always included in recipients

## Limitations

1. **Gmail API Limits**: Subject to Gmail API quotas and rate limits
2. **Attachment Size**: Total attachment size limited by Gmail (25 MB per email)
3. **Message Existence**: Can only reply to messages that still exist
4. **Account Access**: Can only reply to messages in authenticated user's account
5. **Scope Requirements**: Requires `gmail.modify` or `gmail.send` OAuth scope
6. **Recipient Limits**: Gmail limits number of recipients per email

## Best Practices

### 1. Validate Message ID
Always verify the message ID exists before attempting to reply.

### 2. Validate Email Addresses
Verify all email addresses are valid before sending reply.

### 3. Use Bcc Appropriately
Use Bcc for recipients who should receive copy without others knowing.

### 4. Set Reply-To for No-Reply Scenarios
Use Reply To field when sender address should not receive replies.

### 5. Handle Attachments Carefully
- Check file sizes before attaching
- Verify attachment formats are appropriate
- Consider using cloud storage links for large files

### 6. Format Messages Properly
- Use clear, professional language
- Include proper line breaks for readability
- Ensure content is appropriate for all recipients

## Common Use Cases

### 1. Reply with Manager Oversight
```
Scenario: Reply to client with manager in Cc
Action: Use Reply To Mail With CC to include manager
Result: Client receives reply, manager stays informed
```

### 2. Team Collaboration
```
Scenario: Reply and add team members to conversation
Action: Use To field to add additional team members
Result: Entire team included in email thread
```

### 3. Archive Compliance
```
Scenario: Reply with automatic archiving
Action: Use Bcc to send copy to archive system
Result: All replies automatically archived
```

### 4. Support Ticket Routing
```
Scenario: Reply with custom reply-to for support team
Action: Set Reply To field to support team address
Result: Customer replies go to support team
```

### 5. Document Distribution
```
Scenario: Reply with documents to multiple stakeholders
Action: Use Cc/Bcc and attachments for distribution
Result: All stakeholders receive documents appropriately
```

## Related Catalog Requests

- [Reply To Mail](pages/ReplyToMail.md) - Simple reply to original sender
- [Reply To All With CC and BCC](pages/ReplyToAllWithCCAndBCC.md) - Reply to all with additional recipients
- [Send Mail](pages/SendMail.md) - Send new emails
- [Fetch Mail By Message Id](pages/FetchMailByMessageId.md) - Retrieve email details

## Technical Implementation

### Helper Class
- **Class**: MessagingArea
- **Package**: app.krista.extensions.essentials.collaboration.gmail.catalog
- **Validation**: ValidationOrchestrator validates message ID and all email addresses
- **Service**: Delegates to Account.getEmail() and Email.replyText() for reply operations

### Telemetry Metrics
- **TELEMETRY_REPLY_TO_MAIL_WITH_CC_AND_BCC**: Total number of reply requests
- **Tags**: message_id, to, cc, bcc, validation_count
- **Success Tracking**: Records successful replies with all parameters
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

### Issue: "Invalid email format"

**Cause**: One or more email addresses are invalid

**Solution**:
1. Verify email format: user@domain.com
2. Remove extra spaces or special characters
3. Check for typos in domain names
4. Ensure proper comma separation for multiple addresses

### Issue: "Reply to mail failed"

**Cause**: I/O error or system failure

**Solution**:
1. Check network connectivity
2. Verify Gmail API is accessible
3. Reduce attachment sizes if needed
4. Retry the operation
5. Review logs for specific error details

### Issue: "Failed to reply to mail"

**Cause**: General system error

**Solution**:
1. Check all parameters are valid
2. Verify authentication is valid
3. Ensure Gmail API is accessible
4. Review logs for specific error details
5. Retry after a brief delay

## See Also

- [Extension Configuration](pages/ExtensionConfiguration.md)
- [Authentication Guide](pages/Authentication.md)
- [Gmail API Send Message](https://developers.google.com/gmail/api/reference/rest/v1/users.messages/send)
- [Reply To Mail](pages/ReplyToMail.md)

---
*Documentation updated according to Krista Extension Documentation Guidelines*

