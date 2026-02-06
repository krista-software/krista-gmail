# Forward Mail

## Overview

Forwards an existing email message to one or more recipients with optional additional message content, preserving the original email's subject and content.

## Request Details

- **Area**: Messaging
- **Type**: CHANGE_SYSTEM
- **Retry Support**: ✅ Yes (validation errors prompt for re-entry)

## Input Parameters

| Parameter Name | Type | Required | Description | Example |
|----------------|------|----------|-------------|---------|
| Message Id | Text | Yes | Unique identifier of the email message to be forwarded | "18c5f2a3b4d6e789" |
| To | Text | Yes | Recipient email addresses (comma-separated for multiple recipients) | "user@example.com, team@example.com" |
| Message | Paragraph | Yes | Additional message content to include with the forwarded email | "FYI - please review" |

### Parameter Details

**Message Id**: The unique Gmail message identifier. This can be obtained from other catalog requests like Fetch Inbox, Fetch Mail By Message Id, or email notification events.

**To**: Required field containing recipient email addresses. Multiple addresses should be comma-separated. All recipients will receive the forwarded email.

**Message**: Additional message content to include with the forward. This appears before the original email content. Line breaks (`\n`) are automatically converted to HTML breaks (`<br>`).

## Output Parameters

| Parameter Name | Type | Description |
|----------------|------|-------------|
| Is Forwarded | Switch (Boolean) | Indicates whether the email was successfully forwarded |

**Success Response**: `true`

**Failure Response**: `false`

## Validation Rules

| Validation | Error Message | Resolution |
|------------|---------------|------------|
| Message ID required | "Message ID cannot be empty" | Provide a valid message ID |
| Message ID format | "Invalid message ID format" | Ensure message ID is in correct format |
| To address required | "To address cannot be empty" | Provide at least one recipient email address |
| Invalid To email format | "Invalid email format in To field" | Ensure all To addresses are valid email format |

## Error Handling

### Input Errors (INPUT_ERROR)

**Cause**: Invalid or missing message ID or recipient addresses

**Common Scenarios**:
- Empty message ID or To field
- Malformed message ID
- Invalid email addresses in To field
- Invalid characters in parameters

**Resolution**:
1. Verify message ID is correct
2. Verify all email addresses are valid
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

**Cause**: Gmail API errors, network issues, or email sending failures

**Common Scenarios**:
- Network connectivity problems
- Gmail API temporarily unavailable
- Email sending failures
- Permission issues
- Recipient validation failures

**Resolution**:
1. Check network connectivity
2. Verify Gmail API is accessible
3. Ensure authentication is valid
4. Verify recipient email addresses
5. Retry after a brief delay

### Authorization Errors (MustAuthorizeException)

**Cause**: User not authenticated or token expired

**Resolution**:
1. Complete OAuth authentication flow
2. Verify authentication credentials
3. Re-authenticate if token expired
4. Check OAuth scopes include gmail.modify or gmail.send

## Usage Examples

### Example 1: Simple Forward

**Input**:
```
Message Id: "18c5f2a3b4d6e789"
To: "colleague@example.com"
Message: "FYI - please review this email thread."
```

**Output**:
```json
{
  "Is Forwarded": true
}
```

**Result**: Email forwarded successfully to colleague

### Example 2: Forward to Multiple Recipients

**Input**:
```
Message Id: "18c5f2a3b4d6e789"
To: "team@example.com, manager@example.com, stakeholder@example.com"
Message: "Please review this important update."
```

**Output**:
```json
{
  "Is Forwarded": true
}
```

**Result**: Email forwarded to three recipients

### Example 3: Forward with Formatted Message

**Input**:
```
Message Id: "18c5f2a3b4d6e789"
To: "team@example.com"
Message: "Team,\n\nPlease review the email below.\n\nAction required by EOD.\n\nThanks"
```

**Output**:
```json
{
  "Is Forwarded": true
}
```

**Result**: Email forwarded with formatted additional message

### Example 4: Invalid Message ID

**Input**:
```
Message Id: "invalid-id"
To: "user@example.com"
Message: "FYI"
```

**Output**:
```json
{
  "Is Forwarded": false
}
```

**Result**: Operation failed due to invalid message ID

### Example 5: Invalid Email Address

**Input**:
```
Message Id: "18c5f2a3b4d6e789"
To: "invalid-email"
Message: "FYI"
```

**Output**:
```json
{
  "error": {
    "type": "INPUT_ERROR",
    "message": "Invalid email format in To field"
  },
  "retryPrompt": true
}
```

**Result**: Validation error prompts for valid email address

### Example 6: Empty To Field

**Input**:
```
Message Id: "18c5f2a3b4d6e789"
To: ""
Message: "FYI"
```

**Output**:
```json
{
  "error": {
    "type": "INPUT_ERROR",
    "message": "To address cannot be empty"
  },
  "retryPrompt": true
}
```

**Result**: Validation error prompts for recipient address

## Business Rules

1. **Message ID Required**: A valid message ID must be provided
2. **Recipients Required**: At least one recipient must be specified in To field
3. **Subject Preservation**: Forward uses "Fwd: " prefix with original subject
4. **Content Preservation**: Original email content is included in forward
5. **Multiple Recipients**: Use comma separation for multiple addresses
6. **Line Break Conversion**: Newline characters are automatically converted to HTML breaks

## Limitations

1. **Gmail API Limits**: Subject to Gmail API quotas and rate limits
2. **Message Existence**: Can only forward messages that still exist in Gmail
3. **Account Access**: Can only forward messages in the authenticated user's account
4. **Scope Requirements**: Requires `gmail.modify` or `gmail.send` OAuth scope
5. **Recipient Limits**: Gmail limits number of recipients per email (approximately 2000)
6. **No Attachment Modification**: Original attachments are not included (creates new email with subject)

## Best Practices

### 1. Validate Message ID
Always verify the message ID exists before attempting to forward.

### 2. Validate Email Addresses
Verify all recipient email addresses are valid before forwarding.

### 3. Provide Context
Include meaningful additional message content to provide context for recipients.

### 4. Use Appropriate Recipients
Ensure all recipients need to receive the forwarded email.

### 5. Format Messages Properly
- Use clear, professional language
- Include proper line breaks for readability
- Provide context for why email is being forwarded

### 6. Implement Error Handling
- Catch and log all errors
- Provide clear error messages to users
- Implement retry logic for transient failures

## Common Use Cases

### 1. Information Sharing
```
Scenario: Share important emails with team members
Action: Forward email to team with context message
Result: Team receives important information
```

### 2. Escalation
```
Scenario: Escalate customer issues to management
Action: Forward customer email to manager with notes
Result: Manager receives full context for escalation
```

### 3. Delegation
```
Scenario: Delegate tasks by forwarding emails
Action: Forward task email to appropriate team member
Result: Team member receives task with full context
```

### 4. Information Distribution
```
Scenario: Distribute announcements to multiple recipients
Action: Forward announcement to distribution list
Result: All recipients receive announcement
```

### 5. Documentation
```
Scenario: Forward emails for record-keeping
Action: Forward to archive or documentation system
Result: Email preserved in appropriate system
```

## Related Catalog Requests

- [Reply To Mail](pages/ReplyToMail.md) - Reply to original sender
- [Reply To All](pages/ReplyToAll.md) - Reply to all recipients
- [Send Mail](pages/SendMail.md) - Send new emails
- [Fetch Mail By Message Id](pages/FetchMailByMessageId.md) - Retrieve email details

## Technical Implementation

### Helper Class
- **Class**: MessagingArea
- **Package**: app.krista.extensions.essentials.collaboration.gmail.catalog
- **Validation**: ValidationOrchestrator validates message ID and To addresses
- **Service**: Delegates to Account.getEmail() and Account.newEmail() for forward operations

### Telemetry Metrics
- **TELEMETRY_FORWARD_MAIL**: Total number of forward requests
- **Tags**: message_id, to, validation_count
- **Success Tracking**: Records successful forwards with message ID and recipients
- **Error Tracking**: Records validation errors and system errors
- **Retry Tracking**: Records when validation prompts for re-entry

## Troubleshooting

### Issue: Returns false for valid inputs

**Cause**: Message not found or system error during forward

**Solution**:
1. Verify the message ID is correct
2. Check the message hasn't been deleted
3. Review logs for specific error details
4. Ensure Gmail API is accessible
5. Verify recipient email addresses are valid

### Issue: "Invalid email format in To field"

**Cause**: One or more email addresses are invalid

**Solution**:
1. Verify email format: user@domain.com
2. Remove extra spaces or special characters
3. Check for typos in domain names
4. Ensure proper comma separation for multiple addresses

### Issue: "Message ID cannot be empty"

**Cause**: Missing message ID parameter

**Solution**:
1. Provide a valid message ID
2. Obtain message ID from fetch operations
3. Verify parameter is being passed correctly

### Issue: "To address cannot be empty"

**Cause**: Missing recipient addresses

**Solution**:
1. Provide at least one recipient email address
2. Verify parameter is being passed correctly
3. Check for empty strings or null values

## See Also

- [Extension Configuration](pages/ExtensionConfiguration.md)
- [Authentication Guide](pages/Authentication.md)
- [Gmail API Send Message](https://developers.google.com/gmail/api/reference/rest/v1/users.messages/send)
- [Send Mail](pages/SendMail.md)

---
*Documentation updated according to Krista Extension Documentation Guidelines*

