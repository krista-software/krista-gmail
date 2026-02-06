# Get Latest Mail

## Overview

Event-driven catalog request that retrieves Gmail history ID and expiration information when email update events occur, supporting event-based email monitoring workflows.

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

**eventData**: System-provided free-form data containing the new email update information, including history ID.

**Note**: These parameters are automatically provided by the Gmail notification system when email updates occur. Users do not manually provide these values.

## Output Parameters

| Parameter Name | Type | Description |
|----------------|------|-------------|
| Data | Object | Object containing History ID and Expiration information |

### Data Object Structure

```json
{
  "History ID ": "string",
  "Expiration": 0
}
```

**History ID**: Gmail history identifier for tracking changes since last sync

**Expiration**: Expiration value (currently returns 0)

**Success Response**: Object with History ID and Expiration

**Empty Response**: Empty object `{}` if event is not GMAIL_UPDATE

## Validation Rules

No validation rules - this is an event-driven request with system-provided parameters.

## Error Handling

### System Errors (SYSTEM_ERROR)

**Cause**: Gmail API errors, notification channel issues, or service unavailability

**Common Scenarios**:
- Gmail notification channel expired
- Network connectivity problems
- Gmail API temporarily unavailable
- Event data parsing errors

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

### Example 1: Gmail Update Event

**Event Trigger**:
```
eventName: "GMAIL_UPDATE"
eventData: {
  "NEW_EMAIL_UPDATE": "12345678"
}
```

**Output**:
```json
{
  "Data": {
    "History ID ": "12345678",
    "Expiration": 0
  }
}
```

**Result**: History ID retrieved for tracking email changes

### Example 2: Non-Gmail Update Event

**Event Trigger**:
```
eventName: "OTHER_EVENT"
eventData: {some data}
```

**Output**:
```json
{
  "Data": {}
}
```

**Result**: Empty object returned for non-Gmail update events

### Example 3: Email Update with History Tracking

**Event Trigger**:
```
eventName: "GMAIL_UPDATE"
eventData: {
  "NEW_EMAIL_UPDATE": "98765432"
}
```

**Output**:
```json
{
  "Data": {
    "History ID ": "98765432",
    "Expiration": 0
  }
}
```

**Result**: New history ID for incremental sync

## Business Rules

1. **Event-Driven**: Triggered automatically by Gmail notification system
2. **History Tracking**: Provides history ID for incremental email synchronization
3. **GMAIL_UPDATE Only**: Only processes GMAIL_UPDATE events
4. **Subscription Required**: Requires active Gmail notification subscription
5. **Automatic Renewal**: Subscription must be renewed periodically
6. **Empty for Other Events**: Returns empty object for non-Gmail update events

## Limitations

1. **Subscription Expiration**: Gmail subscriptions expire after 7 days and must be renewed
2. **Gmail API Limits**: Subject to Gmail API quotas and rate limits
3. **Event-Specific**: Only responds to GMAIL_UPDATE events
4. **Account Scope**: Only monitors authenticated user's account
5. **Scope Requirements**: Requires `gmail.readonly` or `gmail.modify` OAuth scope
6. **Topic Configuration**: Requires Google Cloud Pub/Sub topic configuration
7. **History ID Only**: Returns history ID, not actual email content

## Best Practices

### 1. Use for Incremental Sync
Use history ID to implement incremental email synchronization instead of full sync.

### 2. Store History ID
Store the history ID to track changes between sync operations.

### 3. Monitor Subscription Status
Regularly check and renew Gmail notification subscription before expiration.

### 4. Combine with Other Requests
Use with Trigger When New Email Arrived for complete email processing.

### 5. Handle Empty Responses
Check for empty objects when event is not GMAIL_UPDATE.

### 6. Log All Events
Log all trigger events and history IDs for debugging and audit purposes.

## Common Use Cases

### 1. Incremental Email Sync
```
Scenario: Synchronize only new/changed emails since last sync
Action: Use history ID to fetch changes since last known state
Result: Efficient incremental synchronization
```

### 2. Change Tracking
```
Scenario: Track all changes to Gmail mailbox
Action: Monitor history ID changes to detect updates
Result: Complete audit trail of mailbox changes
```

### 3. Event Monitoring
```
Scenario: Monitor Gmail for any updates
Action: Trigger on GMAIL_UPDATE and log history ID
Result: Real-time monitoring of Gmail activity
```

### 4. Sync Optimization
```
Scenario: Optimize email synchronization performance
Action: Use history ID to avoid full mailbox scans
Result: Faster, more efficient synchronization
```

### 5. Workflow Coordination
```
Scenario: Coordinate workflows based on Gmail updates
Action: Use history ID to trigger appropriate workflows
Result: Event-driven workflow automation
```

## Related Catalog Requests

- [Trigger When New Email Arrived](pages/TriggerWhenNewEmailArrived.md) - Event-driven new email detection
- [Renew Subscription](pages/RenewSubscription.md) - Renew Gmail notification subscription
- [Fetch Latest Mail](pages/FetchLatestMail.md) - Query-based latest email retrieval
- [Fetch Inbox](pages/FetchInbox.md) - Retrieve inbox emails

## Technical Implementation

### Helper Class
- **Class**: MessagingArea
- **Package**: app.krista.extensions.essentials.collaboration.gmail.catalog
- **Event Handling**: Listens for GMAIL_UPDATE events
- **Data Extraction**: Extracts NEW_EMAIL_UPDATE from event data
- **Response**: Returns map with History ID and Expiration

### Gmail Notification Channel
- **Technology**: Google Cloud Pub/Sub
- **Subscription Duration**: 7 days (requires renewal)
- **Event Type**: GMAIL_UPDATE
- **Delivery**: Push notifications to configured endpoint

### History ID Usage
- **Purpose**: Track incremental changes in Gmail
- **Format**: String identifier
- **Usage**: Pass to Gmail API to fetch changes since this point

## Troubleshooting

### Issue: Empty object returned

**Cause**: Event is not GMAIL_UPDATE type

**Solution**:
1. Verify event name is "GMAIL_UPDATE"
2. Check event data contains NEW_EMAIL_UPDATE
3. Review logs for event details
4. This is expected behavior for non-Gmail events

### Issue: Events not triggering

**Cause**: Subscription expired or not configured

**Solution**:
1. Check Gmail notification subscription status
2. Use Renew Subscription to refresh subscription
3. Verify Google Cloud Pub/Sub topic is configured
4. Check extension configuration for topic settings
5. Review logs for subscription errors

### Issue: Missing history ID in event data

**Cause**: Event data format issue or parsing error

**Solution**:
1. Review event data structure in logs
2. Verify NEW_EMAIL_UPDATE field exists
3. Check Gmail notification channel configuration
4. Review Gmail API documentation for event format

### Issue: Subscription expired

**Cause**: 7-day subscription period elapsed

**Solution**:
1. Use Renew Subscription catalog request
2. Implement automatic renewal schedule (every 6 days)
3. Monitor subscription expiration dates
4. Set up alerts for expiration

### Issue: History ID not updating

**Cause**: No new Gmail activity or sync issue

**Solution**:
1. Verify Gmail has new activity
2. Check notification channel is active
3. Review logs for event delivery
4. Test with known Gmail activity (send test email)

## See Also

- [Extension Configuration](pages/ExtensionConfiguration.md)
- [Authentication Guide](pages/Authentication.md)
- [Gmail Push Notifications](https://developers.google.com/gmail/api/guides/push)
- [Gmail History API](https://developers.google.com/gmail/api/guides/sync)
- [Renew Subscription](pages/RenewSubscription.md)

---
*Documentation updated according to Krista Extension Documentation Guidelines*

