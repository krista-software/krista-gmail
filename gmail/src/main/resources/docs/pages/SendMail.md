# Send Mail

## Overview

Sends an email message through Gmail with support for multiple recipients, attachments, CC, BCC, and custom reply-to addresses.

## Request Details

- **Area**: Messaging
- **Type**: CHANGE_SYSTEM
- **Retry Support**: ✅ Yes (validation errors prompt for re-entry)

## Input Parameters

| Parameter Name | Type | Required | Description | Example |
|----------------|------|----------|-------------|---------|
| Subject | Text | No | Subject line of the email | "Meeting Reminder" |
| Message | RichText | No | Content of the email message (supports rich text formatting) | "Please join the meeting at 2 PM" |
| To | Text | Yes | Primary recipient email addresses (comma-separated for multiple) | "user@example.com, team@example.com" |
| Cc | Text | No | Carbon copy recipients (comma-separated for multiple) | "manager@example.com" |
| Bcc | Text | No | Blind carbon copy recipients (comma-separated for multiple) | "archive@example.com" |
| Reply To | Text | No | Email address for replies to be sent to | "noreply@example.com" |
| Attachments | File | No | Files to attach to the email | [File objects] |

### Parameter Details

**Subject**: The subject line appears in the recipient's inbox. While optional, it's highly recommended for clarity.

**Message**: The email body content. Supports rich text formatting including HTML. Line breaks (`\n`) are automatically converted to HTML breaks (`<br>`).

**To**: Required field. Must contain at least one valid email address. Multiple addresses should be comma-separated.

**Cc**: Carbon copy recipients who will receive the email and see other recipients. Multiple addresses should be comma-separated.

**Bcc**: Blind carbon copy recipients who will receive the email but won't see other recipients. Multiple addresses should be comma-separated.

**Reply To**: Specifies where replies should be sent. If not provided, replies go to the sender's address.

**Attachments**: List of file objects to attach. Supports multiple attachments.

## Output Parameters

| Parameter Name | Type | Description |
|----------------|------|-------------|
| Message | Text | Status message indicating success or failure |

**Success Response**: `"success"`

**Failure Response**: `"Failed to send mail"`

## Validation Rules

| Validation | Error Message | Resolution |
|------------|---------------|------------|
| To address required | "To address cannot be empty" | Provide at least one recipient email address |
| Invalid To email format | "Invalid email format in To field" | Ensure all To addresses are valid email format |
| Invalid Cc email format | "Invalid email format in Cc field" | Ensure all Cc addresses are valid email format |
| Invalid Bcc email format | "Invalid email format in Bcc field" | Ensure all Bcc addresses are valid email format |
| Invalid Reply To format | "Invalid email format in Reply To field" | Ensure Reply To address is valid email format |

## Error Handling

### Input Errors (INPUT_ERROR)

**Cause**: Invalid email addresses in To, Cc, Bcc, or Reply To fields

**Common Scenarios**:
- Empty To field
- Malformed email addresses
- Invalid characters in email addresses

**Resolution**:
1. Verify all email addresses are in valid format (user@domain.com)
2. Check for typos or extra spaces
3. Ensure comma separation for multiple addresses
4. Re-enter corrected values when prompted

### Authorization Errors (MustAuthorizeException)

**Cause**: User not authenticated or token expired

**Resolution**:
1. Complete OAuth authentication flow
2. Verify authentication credentials
3. Re-authenticate if token expired

### System Errors (SYSTEM_ERROR)

**Cause**: Gmail API errors, network issues, or service unavailability

**Common Scenarios**:
- Network connectivity problems
- Gmail API rate limits exceeded
- Gmail service temporarily unavailable
- Attachment size exceeds limits

**Resolution**:
1. Check network connectivity
2. Verify Gmail API is accessible
3. Reduce attachment sizes if needed
4. Retry after a brief delay
5. Check Gmail API quotas and limits

## Usage Examples

### Example 1: Simple Email

**Input**:
```
Subject: "Project Update"
Message: "The project is on track for completion next week."
To: "team@example.com"
```

**Output**:
```json
{
  "Message": "success"
}
```

**Result**: Email sent successfully to team@example.com

### Example 2: Email with CC and BCC

**Input**:
```
Subject: "Quarterly Report"
Message: "Please find the quarterly report attached."
To: "manager@example.com"
Cc: "team@example.com, stakeholders@example.com"
Bcc: "archive@example.com"
```

**Output**:
```json
{
  "Message": "success"
}
```

**Result**: Email sent to manager with CC to team and stakeholders, BCC to archive

### Example 3: Email with Attachments

**Input**:
```
Subject: "Invoice #12345"
Message: "Please find the invoice attached."
To: "client@example.com"
Attachments: [invoice.pdf, receipt.pdf]
```

**Output**:
```json
{
  "Message": "success"
}
```

**Result**: Email sent with two PDF attachments

### Example 4: Email with Custom Reply-To

**Input**:
```
Subject: "Support Ticket #789"
Message: "Your support ticket has been created."
To: "customer@example.com"
Reply To: "support@example.com"
```

**Output**:
```json
{
  "Message": "success"
}
```

**Result**: Email sent with replies directed to support@example.com

### Example 5: Validation Error with Retry

**Input**:
```
Subject: "Test"
Message: "Test message"
To: "invalid-email"
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

**Result**: Validation error prompts user to re-enter correct email address

## Business Rules

1. **Recipient Requirement**: At least one recipient must be specified in the To field
2. **Email Format**: All email addresses must follow standard format (user@domain.com)
3. **Multiple Recipients**: Use comma separation for multiple addresses in To, Cc, or Bcc
4. **Rich Text Support**: Message field supports HTML formatting
5. **Line Break Conversion**: Newline characters are automatically converted to HTML breaks
6. **Attachment Handling**: Multiple attachments are supported through file list

## Limitations

1. **Gmail API Limits**: Subject to Gmail API quotas and rate limits
2. **Attachment Size**: Total attachment size limited by Gmail (25 MB per email)
3. **Recipient Limits**: Gmail limits number of recipients per email (approximately 2000)
4. **Daily Send Limit**: Google Workspace accounts have daily sending limits
5. **Scope Requirements**: Requires `gmail.modify` or `gmail.send` OAuth scope

## Best Practices

### 1. Validate Email Addresses
Always validate email addresses before sending to avoid validation errors and improve success rate.

### 2. Use Meaningful Subjects
Include clear, descriptive subject lines to improve email deliverability and recipient engagement.

### 3. Handle Attachments Carefully
- Check file sizes before attaching
- Verify file types are allowed
- Consider using cloud storage links for large files

### 4. Manage Recipients Wisely
- Use Bcc for mass emails to protect recipient privacy
- Verify recipient lists before sending
- Consider batch sending for large recipient lists

### 5. Implement Error Handling
- Catch and log all errors
- Provide clear error messages to users
- Implement retry logic for transient failures

### 6. Monitor Sending Patterns
- Track send success rates
- Monitor for bounces and failures
- Stay within Gmail sending limits

## Common Use Cases

### 1. Automated Notifications
```
Scenario: Send automated status updates to team members
Action: Use Send Mail with predefined templates and dynamic recipient lists
Result: Team receives timely notifications without manual intervention
```

### 2. Document Distribution
```
Scenario: Distribute reports with attachments to stakeholders
Action: Send email with PDF attachments to multiple recipients using Cc
Result: All stakeholders receive reports simultaneously
```

### 3. Customer Communication
```
Scenario: Send order confirmations to customers
Action: Use Send Mail with custom Reply-To for customer support
Result: Customers receive confirmations and can easily reply to support
```

### 4. Internal Announcements
```
Scenario: Send company-wide announcements
Action: Use Bcc for all employees to protect privacy
Result: All employees receive announcement without seeing other recipients
```

## Related Catalog Requests

- [Reply To Mail](pages/ReplyToMail.md) - Reply to existing emails
- [Reply To All](pages/ReplyToAll.md) - Reply to all recipients
- [Forward Mail](pages/ForwardMail.md) - Forward emails to other recipients
- [Fetch Sent](pages/FetchSent.md) - Retrieve sent emails

## Technical Implementation

### Helper Class
- **Class**: MessagingArea
- **Package**: app.krista.extensions.essentials.collaboration.gmail.catalog
- **Validation**: ValidationOrchestrator validates To, Cc, Bcc, and Reply To email addresses
- **Service**: Delegates to Account.newEmail() and EmailBuilder for email construction and sending

### Telemetry Metrics
- **TELEMETRY_SEND_MAIL**: Total number of send mail requests
- **Tags**: to, cc, bcc, validation_count
- **Success Tracking**: Records successful sends with recipient information
- **Error Tracking**: Records validation errors and system errors
- **Retry Tracking**: Records when validation prompts for re-entry

## Troubleshooting

### Issue: "Failed to send mail"

**Cause**: Generic system error during send operation

**Solution**:
1. Check network connectivity
2. Verify Gmail API is accessible
3. Review error logs for specific details
4. Ensure authentication is valid
5. Retry the operation

### Issue: "Invalid email format"

**Cause**: Email address doesn't match expected format

**Solution**:
1. Verify email format: user@domain.com
2. Remove extra spaces or special characters
3. Check for typos in domain names
4. Ensure proper comma separation for multiple addresses

### Issue: "Authentication required"

**Cause**: User not authenticated or token expired

**Solution**:
1. Complete OAuth authentication flow
2. Check token expiration
3. Re-authenticate if necessary
4. Verify OAuth scopes include gmail.send or gmail.modify

### Issue: "Attachment too large"

**Cause**: Attachment exceeds Gmail size limits

**Solution**:
1. Reduce attachment file size
2. Use compression for large files
3. Consider using cloud storage links instead
4. Split into multiple emails if necessary

## See Also

- [Extension Configuration](pages/ExtensionConfiguration.md)
- [Authentication Guide](pages/Authentication.md)
- [Gmail API Send Message](https://developers.google.com/gmail/api/reference/rest/v1/users.messages/send)
- [Gmail Sending Limits](https://support.google.com/a/answer/166852)

---
*Documentation updated according to Krista Extension Documentation Guidelines*

