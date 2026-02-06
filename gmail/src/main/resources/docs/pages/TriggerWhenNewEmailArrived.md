# Trigger When New Email Arrived

## Overview

Event-driven catalog request that triggers automatically when new emails arrive in the Gmail inbox, enabling real-time email processing and automation workflows.

## Request Details

- **Area**: Messaging
- **Type**: WAIT_FOR_EVENT
- **Retry Support**: ❌ No (event-driven, no user input validation)

## Input Parameters

| Parameter Name | Type | Required | Description | Example |
|----------------|------|----------|-------------|---------|
| eventName | Text | Yes | Name of the event (system-provided) | "GMAIL_UPDATE" |
| eventData | FreeForm | Yes | Event data containing update information (system-provided) | {event data} |

### Parameter Details

**eventName**: System-provided event name. When a Gmail update occurs, this will be `"GMAIL_UPDATE"`.

**eventData**: System-provided free-form data containing information about the Gmail update event.

**Note**: These parameters are automatically provided by the Gmail notification system when new emails arrive. Users do not manually provide these values.

## Output Parameters

| Parameter Name | Type | Description |
|----------------|------|-------------|
| All Mails | [ Entity(Mail Details) ] | List of all new email messages that triggered the event |

### Mail Details Entity

Each new email in the list contains:
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

**Success Response**: Array of Mail Details entities for all new emails

**Error Response**: Throws `IllegalArgumentException` if no new messages at trigger time

## Validation Rules

No validation rules - this is an event-driven request with system-provided parameters.

## Error Handling

### Logic Errors (LOGIC_ERROR)

**Cause**: No new messages available when event triggered

**Error Message**: "There is no new message at this point"

**Common Scenarios**:
- Event triggered but emails already processed
- Timing issue between notification and retrieval
- Emails deleted before retrieval

**Resolution**:
1. This is a transient condition
2. System will trigger again when new emails arrive
3. No user action required
4. Check logs for event timing details

### System Errors (SYSTEM_ERROR)

**Cause**: Gmail API errors, notification channel issues, or service unavailability

**Common Scenarios**:
- Gmail notification channel expired
- Network connectivity problems
- Gmail API temporarily unavailable
- Subscription renewal needed

**Resolution**:
1. Check Gmail notification subscription status
2. Verify network connectivity
3. Ensure Gmail API is accessible
4. Use Renew Subscription to refresh notification channel
5. Review logs for specific error details

### Authorization Errors (MustAuthorizeException)

**Cause**: User not authenticated or token expired

**Resolution**:
1. Complete OAuth authentication flow
2. Verify authentication credentials
3. Re-authenticate if token expired
4. Check OAuth scopes include gmail.readonly or gmail.modify

## Usage Examples

### Example 1: New Email Arrives

**Event Trigger**:
```
eventName: "GMAIL_UPDATE"
eventData: {new email update data}
```

**Output**:
```json
{
  "All Mails": [
    {
      "messageId": "18c5f2a3b4d6e789",
      "from": "sender@example.com",
      "to": "user@example.com",
      "subject": "New Project Request",
      "body": "I have a new project request...",
      "date": "2025-10-04T15:30:00Z",
      "isRead": false
    }
  ]
}
```

**Result**: New email details provided for processing

### Example 2: Multiple New Emails

**Event Trigger**:
```
eventName: "GMAIL_UPDATE"
eventData: {multiple email updates}
```

**Output**:
```json
{
  "All Mails": [
    {
      "messageId": "18c5f2a3b4d6e789",
      "from": "client1@example.com",
      "subject": "Urgent Request"
    },
    {
      "messageId": "18c5f2a3b4d6e790",
      "from": "client2@example.com",
      "subject": "Follow-up Question"
    }
  ]
}
```

**Result**: Multiple new emails provided for batch processing

### Example 3: No New Messages

**Event Trigger**:
```
eventName: "GMAIL_UPDATE"
eventData: {update data}
```

**Output**:
```
Error: IllegalArgumentException
Message: "There is no new message at this point"
```

**Result**: No emails to process at this time

## Business Rules

1. **Event-Driven**: Triggered automatically by Gmail notification system
2. **Real-Time**: Provides near real-time notification of new emails
3. **Batch Processing**: Can return multiple new emails in single trigger
4. **Subscription Required**: Requires active Gmail notification subscription
5. **Automatic Renewal**: Subscription must be renewed periodically (see Renew Subscription)
6. **Inbox Focus**: Monitors inbox for new arrivals

## Limitations

1. **Subscription Expiration**: Gmail subscriptions expire after 7 days and must be renewed
2. **Gmail API Limits**: Subject to Gmail API quotas and rate limits
3. **Notification Delays**: May have slight delays (typically seconds) in notification delivery
4. **Account Scope**: Only monitors authenticated user's account
5. **Scope Requirements**: Requires `gmail.readonly` or `gmail.modify` OAuth scope
6. **Topic Configuration**: Requires Google Cloud Pub/Sub topic configuration
7. **Network Dependency**: Requires stable network connection for notifications

## Best Practices

### 1. Implement Error Handling
Handle the "no new message" exception gracefully as it's a normal condition.

### 2. Process Emails Efficiently
Process new emails quickly to avoid backlog when multiple emails arrive.

### 3. Monitor Subscription Status
Regularly check and renew Gmail notification subscription before expiration.

### 4. Implement Idempotency
Handle duplicate notifications gracefully (same email may trigger multiple times).

### 5. Log All Events
Log all trigger events for debugging and audit purposes.

### 6. Handle Batch Processing
Be prepared to process multiple emails in a single trigger event.

## Common Use Cases

### 1. Auto-Response System
```
Scenario: Automatically respond to new emails
Action: Trigger processes new email and sends auto-response
Result: Immediate auto-response to incoming emails
```

### 2. Email Routing
```
Scenario: Route emails to appropriate handlers based on content
Action: Trigger analyzes new email and routes to handler
Result: Emails automatically routed to correct department
```

### 3. Priority Email Alerts
```
Scenario: Alert users of high-priority emails
Action: Trigger checks email priority and sends alert
Result: Users notified immediately of urgent emails
```

### 4. Email Archiving
```
Scenario: Automatically archive incoming emails
Action: Trigger processes new email and archives it
Result: All emails automatically archived in real-time
```

### 5. Workflow Automation
```
Scenario: Trigger workflows based on email content
Action: Trigger parses email and initiates appropriate workflow
Result: Email-driven workflows execute automatically
```

## Related Catalog Requests

- [Get Latest Mail](pages/GetLatestMail.md) - Event-driven latest email retrieval
- [Renew Subscription](pages/RenewSubscription.md) - Renew Gmail notification subscription
- [Fetch Latest Mail](pages/FetchLatestMail.md) - Query-based latest email retrieval
- [Fetch Inbox](pages/FetchInbox.md) - Retrieve inbox emails

## Technical Implementation

### Helper Class
- **Class**: MessagingArea
- **Package**: app.krista.extensions.essentials.collaboration.gmail.catalog
- **Event Handling**: Listens for GMAIL_UPDATE events
- **Service**: Delegates to gmailNotificationChannel.getAllNewMails() for email retrieval
- **Entity Conversion**: Returns list of MailDetails entities

### Gmail Notification Channel
- **Technology**: Google Cloud Pub/Sub
- **Subscription Duration**: 7 days (requires renewal)
- **Event Type**: GMAIL_UPDATE
- **Delivery**: Push notifications to configured endpoint

### Telemetry Metrics
- Event trigger tracking
- New email count tracking
- Error tracking for failed retrievals

## Troubleshooting

### Issue: "There is no new message at this point"

**Cause**: Event triggered but no new emails available

**Solution**:
1. This is normal behavior, not an error
2. Emails may have been processed already
3. Wait for next trigger event
4. No user action required

### Issue: Events not triggering

**Cause**: Subscription expired or not configured

**Solution**:
1. Check Gmail notification subscription status
2. Use Renew Subscription to refresh subscription
3. Verify Google Cloud Pub/Sub topic is configured
4. Check extension configuration for topic settings
5. Review logs for subscription errors

### Issue: Duplicate email notifications

**Cause**: Gmail may send duplicate notifications

**Solution**:
1. Implement idempotency in email processing
2. Track processed message IDs
3. Skip already-processed emails
4. This is expected Gmail API behavior

### Issue: Missing emails in trigger

**Cause**: Timing or synchronization issue

**Solution**:
1. Check Gmail API synchronization
2. Verify network connectivity
3. Review logs for retrieval errors
4. Consider using Fetch Inbox as backup

### Issue: Subscription expired

**Cause**: 7-day subscription period elapsed

**Solution**:
1. Use Renew Subscription catalog request
2. Implement automatic renewal schedule (every 6 days)
3. Monitor subscription expiration dates
4. Set up alerts for expiration

## See Also

- [Extension Configuration](pages/ExtensionConfiguration.md)
- [Authentication Guide](pages/Authentication.md)
- [Gmail Push Notifications](https://developers.google.com/gmail/api/guides/push)
- [Renew Subscription](pages/RenewSubscription.md)
- [Get Latest Mail](pages/GetLatestMail.md)

---
*Documentation updated according to Krista Extension Documentation Guidelines*

