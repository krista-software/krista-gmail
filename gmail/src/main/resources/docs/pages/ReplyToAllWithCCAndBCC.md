# Reply To All With CC and BCC

## Overview

Replies to all recipients of an email message with advanced options to add additional recipients in To, Cc, Bcc fields, include attachments, and set a custom reply-to address.

## Request Details

- **Area**: Messaging
- **Type**: CHANGE_SYSTEM
- **Retry Support**: ✅ Yes (validation errors prompt for re-entry)

## Input Parameters

| Parameter Name | Type | Required | Description | Example |
|----------------|------|----------|-------------|---------|
| Message Id | Text | Yes | Unique identifier of the email message to reply to all recipients | "18c5f2a3b4d6e789" |
| To | Text | No | Additional recipients to include in the To field (comma-separated) | "user1@example.com, user2@example.com" |
| Cc | Text | No | Additional recipients to include in the Cc field (comma-separated) | "manager@example.com" |
| Bcc | Text | No | Additional recipients to include in the Bcc field (comma-separated) | "archive@example.com" |
| Message | RichText | No | Content of the reply message that will be sent to all recipients | "Thank you all for your input..." |
| Attachments | File | No | Optional files to attach to the reply message | [File objects] |
| Reply To | Text | No | Email address to set as the reply-to address | "noreply@example.com" |

### Parameter Details

**Message Id**: The unique Gmail message identifier. This can be obtained from other catalog requests like Fetch Inbox, Fetch Mail By Message Id, or email notification events.

**To**: Additional recipients to add to the To field beyond the original recipients. Multiple addresses should be comma-separated.

**Cc**: Additional carbon copy recipients beyond the original Cc list. Multiple addresses should be comma-separated.

**Bcc**: Blind carbon copy recipients. Multiple addresses should be comma-separated. These recipients receive a copy without other recipients knowing.

**Message**: The reply message content. Supports rich text formatting. Line breaks (`\n`) are automatically converted to HTML breaks (`<br>`).

**Attachments**: Optional list of file objects to attach to the reply. Supports multiple attachments.

**Reply To**: Custom reply-to email address. When recipients reply, their response will go to this address instead of the sender's address.

## Output Parameters

| Parameter Name | Type | Description |
|----------------|------|-------------|
| Is Successful | Boolean (Switch) | Indicates whether the reply to all operation was successful |

**Success Response**: `true`

**Failure Response**: `false`

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

**Cause**: Gmail API errors, messaging exceptions, or I/O failures

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

### Example 1: Reply to All with Additional Cc

**Input**:
```
Message Id: "18c5f2a3b4d6e789"
Message: "Thank you all. I've added our manager to this thread."
Cc: "manager@example.com"
```

**Output**:
```json
{
  "Is Successful": true
}
```

**Result**: Reply sent to all original recipients plus manager in Cc

### Example 2: Reply to All with Additional Team Members

**Input**:
```
Message Id: "18c5f2a3b4d6e789"
Message: "Adding the extended team to this discussion."
To: "team1@example.com, team2@example.com"
```

**Output**:
```json
{
  "Is Successful": true
}
```

**Result**: Reply sent to all original recipients plus additional team members

### Example 3: Reply to All with Bcc Archive

**Input**:
```
Message Id: "18c5f2a3b4d6e789"
Message: "Replying to all with archive copy."
Bcc: "archive@example.com"
```

**Output**:
```json
{
  "Is Successful": true
}
```

**Result**: Reply sent to all with hidden copy to archive

### Example 4: Reply to All with Attachment

**Input**:
```
Message Id: "18c5f2a3b4d6e789"
Message: "Please find the updated document attached for everyone's review."
Attachments: [updated_report.pdf]
```

**Output**:
```json
{
  "Is Successful": true
}
```

**Result**: Reply sent to all recipients with PDF attachment

### Example 5: Complete Reply with All Options

**Input**:
```
Message Id: "18c5f2a3b4d6e789"
To: "stakeholder@example.com"
Cc: "manager@example.com"
Bcc: "archive@example.com"
Message: "Comprehensive update for everyone with all stakeholders included."
Attachments: [report.pdf, data.xlsx]
Reply To: "team@example.com"
```

**Output**:
```json
{
  "Is Successful": true
}
```

**Result**: Reply sent to all with additional recipients, attachments, and custom reply-to

### Example 6: Invalid Message ID

**Input**:
```
Message Id: "invalid-id"
Message: "Test reply"
```

**Output**:
```json
{
  "Is Successful": false
}
```

**Result**: Operation failed due to invalid message ID

## Business Rules

1. **Message ID Required**: A valid message ID must be provided
2. **Reply to All**: Reply is sent to original sender and all To/Cc recipients
3. **Optional Additional Recipients**: To, Cc, Bcc fields are optional to add more recipients
4. **Email Validation**: All email addresses must be valid format
5. **Multiple Recipients**: Use comma separation for multiple addresses
6. **Reply-To Override**: Reply To field overrides default sender address
7. **Line Break Conversion**: Newline characters are automatically converted to HTML breaks
8. **Thread Preservation**: Reply maintains the email thread/conversation

## Limitations

1. **Gmail API Limits**: Subject to Gmail API quotas and rate limits
2. **Attachment Size**: Total attachment size limited by Gmail (25 MB per email)
3. **Message Existence**: Can only reply to messages that still exist
4. **Account Access**: Can only reply to messages in authenticated user's account
5. **Scope Requirements**: Requires `gmail.modify` or `gmail.send` OAuth scope
6. **Recipient Limits**: Gmail limits number of recipients per email

## Best Practices

### 1. Validate Message ID
Always verify the message ID exists before attempting to reply to all.

### 2. Consider Recipient Count
Be aware of how many recipients will receive the reply (original + additional).

### 3. Use Appropriate Recipients
Ensure all additional recipients need to be included in the conversation.

### 4. Validate Email Addresses
Verify all email addresses are valid before sending reply.

### 5. Handle Attachments Carefully
- Check file sizes before attaching
- Verify all recipients need the attachments
- Consider using cloud storage links for large files

### 6. Format Messages Properly
- Use clear, professional language
- Include proper line breaks for readability
- Ensure content is appropriate for all recipients

## Common Use Cases

### 1. Team Collaboration with Oversight
```
Scenario: Reply to team discussion with manager oversight
Action: Reply to all and add manager in Cc
Result: All team members and manager receive update
```

### 2. Stakeholder Communication
```
Scenario: Reply to all and include additional stakeholders
Action: Use To field to add stakeholders to conversation
Result: All relevant parties included in discussion
```

### 3. Compliance and Archiving
```
Scenario: Reply to all with automatic archiving
Action: Use Bcc to send copy to compliance archive
Result: All replies automatically archived for compliance
```

### 4. Document Distribution to Group
```
Scenario: Share documents with entire email thread
Action: Reply to all with attachments
Result: All participants receive documents
```

### 5. Escalation with Context
```
Scenario: Escalate issue to management while keeping team informed
Action: Reply to all and add management in Cc
Result: Team and management both informed with full context
```

## Related Catalog Requests

- [Reply To All](pages/ReplyToAll.md) - Simple reply to all recipients
- [Reply To Mail With CC and BCC](pages/ReplyToMailWithCCAndBCC.md) - Reply to sender with additional recipients
- [Send Mail](pages/SendMail.md) - Send new emails
- [Fetch Mail By Message Id](pages/FetchMailByMessageId.md) - Retrieve email details

## Technical Implementation

### Helper Class
- **Class**: MessagingArea
- **Package**: app.krista.extensions.essentials.collaboration.gmail.catalog
- **Validation**: ValidationOrchestrator validates message ID and all email addresses
- **Service**: Delegates to Account.getEmail() and Email.replyToAll() for reply operations

### Telemetry Metrics
- **gmail.replyToAllWithCCAndBCC**: Total number of reply to all requests
- **Tags**: message_id, to, cc, bcc, validation_count
- **Success Tracking**: Records successful replies with all parameters
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

### Issue: "Invalid email format"

**Cause**: One or more email addresses are invalid

**Solution**:
1. Verify email format: user@domain.com
2. Remove extra spaces or special characters
3. Check for typos in domain names
4. Ensure proper comma separation for multiple addresses

### Issue: "Error occurred while processing attachment"

**Cause**: Attachment processing failure

**Solution**:
1. Check attachment file sizes
2. Reduce attachment sizes if needed
3. Verify attachment file formats
4. Try without attachments to isolate issue
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
- [Gmail API Reply](https://developers.google.com/gmail/api/guides/sending#replying_to_a_message)
- [Reply To All](pages/ReplyToAll.md)

---
*Documentation updated according to Krista Extension Documentation Guidelines*

