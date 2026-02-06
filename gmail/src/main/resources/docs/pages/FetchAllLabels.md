# Fetch All Labels

## Overview

Retrieves a complete list of all Gmail labels (folders) available in the authenticated user's account, including both system labels and custom user-created labels.

## Request Details

- **Area**: Messaging
- **Type**: QUERY_SYSTEM
- **Retry Support**: ❌ No (no input parameters to validate)

## Input Parameters

This catalog request requires no input parameters.

## Output Parameters

| Parameter Name | Type | Description |
|----------------|------|-------------|
| Labels | [ Text ] | List of all label names in the Gmail account |

### Label Types

The returned list includes:

**System Labels**:
- `INBOX` - Main inbox
- `SENT` - Sent emails
- `DRAFT` - Draft emails
- `TRASH` - Deleted emails
- `SPAM` - Spam folder
- `STARRED` - Starred emails
- `IMPORTANT` - Important emails
- `UNREAD` - Unread emails
- `CATEGORY_PERSONAL` - Personal category
- `CATEGORY_SOCIAL` - Social category
- `CATEGORY_PROMOTIONS` - Promotions category
- `CATEGORY_UPDATES` - Updates category
- `CATEGORY_FORUMS` - Forums category

**Custom Labels**:
- User-created labels (e.g., "Work", "Archive", "Important", "Projects")

**Success Response**: Array of label name strings

## Validation Rules

No validation rules - this request has no input parameters.

## Error Handling

### Authorization Errors (MustAuthorizeException)

**Cause**: User not authenticated or token expired

**Resolution**:
1. Complete OAuth authentication flow
2. Verify authentication credentials
3. Re-authenticate if token expired
4. Check OAuth scopes include gmail.readonly or gmail.modify

### System Errors (SYSTEM_ERROR)

**Cause**: Gmail API errors, network issues, or service unavailability

**Common Scenarios**:
- Network connectivity problems
- Gmail API temporarily unavailable
- Authentication issues
- API rate limits exceeded

**Resolution**:
1. Check network connectivity
2. Verify Gmail API is accessible
3. Ensure authentication is valid
4. Retry after a brief delay
5. Check Gmail API quotas

## Usage Examples

### Example 1: Fetch All Labels

**Input**:
```
(No input required)
```

**Output**:
```json
{
  "Labels": [
    "INBOX",
    "SENT",
    "DRAFT",
    "TRASH",
    "SPAM",
    "STARRED",
    "IMPORTANT",
    "UNREAD",
    "Work",
    "Personal",
    "Archive",
    "Projects"
  ]
}
```

**Result**: Complete list of system and custom labels retrieved

### Example 2: Account with Only System Labels

**Input**:
```
(No input required)
```

**Output**:
```json
{
  "Labels": [
    "INBOX",
    "SENT",
    "DRAFT",
    "TRASH",
    "SPAM",
    "STARRED"
  ]
}
```

**Result**: System labels only (no custom labels created)

### Example 3: Account with Many Custom Labels

**Input**:
```
(No input required)
```

**Output**:
```json
{
  "Labels": [
    "INBOX",
    "SENT",
    "Work",
    "Personal",
    "Finance",
    "Travel",
    "Receipts",
    "Important",
    "Archive",
    "Follow-up"
  ]
}
```

**Result**: Mix of system and custom labels

## Business Rules

1. **No Input Required**: This request requires no parameters
2. **Complete List**: Returns all labels in the account
3. **System Labels**: Always includes Gmail system labels
4. **Custom Labels**: Includes all user-created labels
5. **Label Names**: Returns exact label names as they appear in Gmail
6. **Case Sensitive**: Label names are case-sensitive

## Limitations

1. **Gmail API Limits**: Subject to Gmail API quotas and rate limits
2. **Account Scope**: Only retrieves labels from authenticated user's account
3. **Read-Only**: This request only retrieves labels, doesn't create or modify them
4. **Scope Requirements**: Requires `gmail.readonly` or `gmail.modify` OAuth scope
5. **No Filtering**: Returns all labels without filtering options

## Best Practices

### 1. Cache Label List
Cache the label list to reduce API calls, as labels don't change frequently.

### 2. Use for Validation
Use this request to validate folder names before moving or filtering emails.

### 3. Populate Dropdowns
Use the label list to populate UI dropdowns for folder selection.

### 4. Check Before Creating
Fetch labels before creating new ones to avoid duplicates.

### 5. Monitor for Changes
Periodically refresh label list to detect new custom labels.

### 6. Handle System Labels
Be aware of system labels and handle them appropriately in your logic.

## Common Use Cases

### 1. Folder Selection UI
```
Scenario: Provide dropdown list of folders for user selection
Action: Fetch all labels and populate dropdown menu
Result: Users can select from available folders
```

### 2. Email Organization
```
Scenario: Validate destination folder before moving emails
Action: Fetch all labels to verify folder exists
Result: Prevent errors from invalid folder names
```

### 3. Label Management
```
Scenario: Display all labels for management interface
Action: Fetch all labels and display in management UI
Result: Users can view and manage their labels
```

### 4. Workflow Configuration
```
Scenario: Configure email routing rules based on labels
Action: Fetch labels to show available routing destinations
Result: Workflows configured with valid label names
```

### 5. Email Filtering
```
Scenario: Filter emails by label in search interface
Action: Fetch labels to provide filter options
Result: Users can filter emails by any label
```

## Related Catalog Requests

- [Move Message](pages/MoveMessage.md) - Move emails to specific labels
- [Fetch Mails By Label](pages/FetchMailsByLabel.md) - Retrieve emails from specific label
- [Fetch Inbox](pages/FetchInbox.md) - Retrieve inbox emails
- [Mark Message](pages/MarkMessage.md) - Mark emails as read/unread

## Technical Implementation

### Helper Class
- **Class**: MessagingArea
- **Package**: app.krista.extensions.essentials.collaboration.gmail.catalog
- **Validation**: No validation required (no input parameters)
- **Service**: Delegates to Account.getFolderNames() for label retrieval

### Telemetry Metrics
- No specific telemetry metrics for this simple request
- Standard request/response logging applies

## Troubleshooting

### Issue: Empty label list returned

**Cause**: Account has no labels or authentication issue

**Solution**:
1. Verify authentication is valid
2. Check you're using the correct Gmail account
3. Confirm account has labels (all accounts have system labels)
4. Review logs for authentication errors

### Issue: Missing custom labels

**Cause**: Custom labels not synced or account issue

**Solution**:
1. Verify custom labels exist in Gmail web interface
2. Check account synchronization
3. Retry the request
4. Verify authentication is for correct account

### Issue: Authentication errors

**Cause**: Invalid or expired OAuth token

**Solution**:
1. Re-authenticate with Gmail
2. Verify OAuth scopes include gmail.readonly
3. Check token hasn't expired
4. Review authentication configuration

### Issue: System labels in unexpected format

**Cause**: Gmail API returns system labels in specific format

**Solution**:
1. Use exact label names as returned (e.g., "INBOX" not "Inbox")
2. Handle case-sensitive label names
3. Reference Gmail API documentation for system label names

## See Also

- [Extension Configuration](pages/ExtensionConfiguration.md)
- [Authentication Guide](pages/Authentication.md)
- [Gmail API Labels](https://developers.google.com/gmail/api/reference/rest/v1/users.labels)
- [Move Message](pages/MoveMessage.md)

---
*Documentation updated according to Krista Extension Documentation Guidelines*

