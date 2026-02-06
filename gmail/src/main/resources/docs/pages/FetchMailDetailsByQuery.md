# Fetch Mail Details By Query

## Overview

Searches and retrieves emails from Gmail using a search query string, supporting Gmail's powerful search syntax for finding specific emails based on various criteria.

## Request Details

- **Area**: Messaging
- **Type**: QUERY_SYSTEM
- **Retry Support**: ✅ Yes (validation errors prompt for re-entry)

## Input Parameters

| Parameter Name | Type | Required | Description | Example |
|----------------|------|----------|-------------|---------|
| Query | Text | Yes | Search query to find emails using Gmail search syntax | "from:user@example.com subject:report" |

### Parameter Details

**Query**: Gmail search query string. Supports Gmail's advanced search operators including:

**Common Search Operators**:
- `from:sender@example.com` - Emails from specific sender
- `to:recipient@example.com` - Emails to specific recipient
- `subject:keyword` - Emails with keyword in subject
- `has:attachment` - Emails with attachments
- `is:unread` - Unread emails
- `is:read` - Read emails
- `is:starred` - Starred emails
- `after:2025/01/01` - Emails after specific date
- `before:2025/12/31` - Emails before specific date
- `newer_than:7d` - Emails newer than 7 days
- `older_than:30d` - Emails older than 30 days
- `label:important` - Emails with specific label
- `filename:pdf` - Emails with PDF attachments

**Combining Operators**:
- Use spaces for AND: `from:user@example.com has:attachment`
- Use OR: `from:user1@example.com OR from:user2@example.com`
- Use negation: `-from:spam@example.com`
- Use quotes for exact phrases: `subject:"quarterly report"`

## Output Parameters

| Parameter Name | Type | Description |
|----------------|------|-------------|
| Mails | [ Entity(Mail Details) ] | List of email messages matching the search query |

### Mail Details Entity

Each email in the list contains:
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

**Success Response**: Array of Mail Details entities matching query

**Empty Response**: Empty array `[]` if no emails match query or error occurs

## Validation Rules

| Validation | Error Message | Resolution |
|------------|---------------|------------|
| Query required | "Query cannot be empty" | Provide a valid search query |
| Query format | "Invalid query format" | Use valid Gmail search syntax |

## Error Handling

### Input Errors (INPUT_ERROR)

**Cause**: Invalid or missing search query

**Common Scenarios**:
- Empty query string
- Invalid search syntax
- Malformed query operators

**Resolution**:
1. Verify query is not empty
2. Check Gmail search syntax is correct
3. Test query in Gmail web interface first
4. Re-enter corrected query when prompted

### System Errors (SYSTEM_ERROR)

**Cause**: Gmail API errors, network issues, or search failures

**Common Scenarios**:
- Network connectivity problems
- Gmail API temporarily unavailable
- Search query too complex
- API rate limits exceeded
- Authentication issues

**Resolution**:
1. Check network connectivity
2. Verify Gmail API is accessible
3. Simplify search query if too complex
4. Retry after a brief delay
5. Check Gmail API quotas

**Note**: On system errors, returns empty array instead of throwing exception

### Authorization Errors (MustAuthorizeException)

**Cause**: User not authenticated or token expired

**Resolution**:
1. Complete OAuth authentication flow
2. Verify authentication credentials
3. Re-authenticate if token expired
4. Check OAuth scopes include gmail.readonly or gmail.modify

## Usage Examples

### Example 1: Search by Sender

**Input**:
```
Query: "from:manager@example.com"
```

**Output**:
```json
{
  "Mails": [
    {
      "messageId": "18c5f2a3b4d6e789",
      "from": "manager@example.com",
      "subject": "Weekly Report",
      "date": "2025-10-04T10:30:00Z"
    }
    // ... more matching emails
  ]
}
```

**Result**: All emails from manager@example.com retrieved

### Example 2: Search by Subject

**Input**:
```
Query: "subject:invoice"
```

**Output**:
```json
{
  "Mails": [
    // All emails with "invoice" in subject
  ]
}
```

**Result**: Emails with "invoice" in subject retrieved

### Example 3: Search Unread Emails with Attachments

**Input**:
```
Query: "is:unread has:attachment"
```

**Output**:
```json
{
  "Mails": [
    // All unread emails with attachments
  ]
}
```

**Result**: Unread emails with attachments retrieved

### Example 4: Search by Date Range

**Input**:
```
Query: "after:2025/10/01 before:2025/10/31"
```

**Output**:
```json
{
  "Mails": [
    // All emails from October 2025
  ]
}
```

**Result**: Emails from October 2025 retrieved

### Example 5: Complex Search Query

**Input**:
```
Query: "from:client@example.com subject:proposal has:attachment newer_than:7d"
```

**Output**:
```json
{
  "Mails": [
    // Emails from client with "proposal" in subject, with attachments, from last 7 days
  ]
}
```

**Result**: Highly specific emails retrieved

### Example 6: No Results Found

**Input**:
```
Query: "from:nonexistent@example.com"
```

**Output**:
```json
{
  "Mails": []
}
```

**Result**: Empty array when no emails match

### Example 7: Empty Query Validation

**Input**:
```
Query: ""
```

**Output**:
```json
{
  "error": {
    "type": "INPUT_ERROR",
    "message": "Query cannot be empty"
  },
  "retryPrompt": true
}
```

**Result**: Validation error prompts for query

## Business Rules

1. **Query Required**: A search query must be provided
2. **Gmail Search Syntax**: Uses standard Gmail search operators
3. **Case Insensitive**: Search is case-insensitive by default
4. **All Folders**: Searches across all folders/labels
5. **Result Limit**: May be limited by Gmail API response size
6. **Empty Results**: Returns empty array if no matches or errors

## Limitations

1. **Gmail API Limits**: Subject to Gmail API quotas and rate limits
2. **Search Complexity**: Very complex queries may fail or timeout
3. **Result Size**: Large result sets may be truncated
4. **Account Scope**: Only searches authenticated user's account
5. **Scope Requirements**: Requires `gmail.readonly` or `gmail.modify` OAuth scope
6. **No Pagination**: Does not support pagination parameters

## Best Practices

### 1. Test Queries in Gmail
Test search queries in Gmail web interface before using in automation.

### 2. Use Specific Queries
More specific queries return more relevant results and perform better.

### 3. Combine Operators Effectively
- Use multiple operators to narrow results
- Use OR for alternative criteria
- Use negation to exclude unwanted results

### 4. Handle Empty Results
Always check for empty arrays and handle gracefully.

### 5. Implement Error Handling
- Catch and log all errors
- Provide clear error messages
- Implement retry logic for transient failures

### 6. Monitor Performance
- Track query execution times
- Optimize slow queries
- Consider caching frequent searches

## Common Use Cases

### 1. Find Unread Emails
```
Scenario: Identify unread emails for processing
Action: Search with "is:unread" query
Result: All unread emails retrieved for processing
```

### 2. Search by Sender
```
Scenario: Find all emails from specific client
Action: Search with "from:client@example.com" query
Result: Complete email history with client retrieved
```

### 3. Find Emails with Attachments
```
Scenario: Locate emails with specific file types
Action: Search with "has:attachment filename:pdf" query
Result: All emails with PDF attachments found
```

### 4. Date-Based Search
```
Scenario: Find emails from specific time period
Action: Search with date range query
Result: Emails from specified period retrieved
```

### 5. Label-Based Search
```
Scenario: Find emails with specific label
Action: Search with "label:important" query
Result: All important labeled emails retrieved
```

## Related Catalog Requests

- [Fetch Inbox](pages/FetchInbox.md) - Retrieve inbox emails
- [Fetch Sent](pages/FetchSent.md) - Retrieve sent emails
- [Fetch Mails By Label](pages/FetchMailsByLabel.md) - Retrieve emails by label
- [Fetch Mail By Message Id](pages/FetchMailByMessageId.md) - Get specific email

## Technical Implementation

### Helper Class
- **Class**: MessagingArea
- **Package**: app.krista.extensions.essentials.collaboration.gmail.catalog
- **Validation**: ValidationOrchestrator validates query parameter
- **Service**: Delegates to Account.searchEmails() for search operations
- **Entity Conversion**: CatalogTypes.fromEmail() converts Gmail Email to Mail Details entity

### Telemetry Metrics
- **TELEMETRY_FETCH_MAIL_DETAILS_BY_QUERY**: Total number of search requests
- **Tags**: query, validation_count
- **Success Tracking**: Records successful searches with query
- **Error Tracking**: Records validation errors and system errors
- **Retry Tracking**: Records when validation prompts for re-entry

## Troubleshooting

### Issue: Empty array returned for valid query

**Cause**: No emails match query or system error

**Solution**:
1. Test query in Gmail web interface
2. Verify query syntax is correct
3. Check emails exist that match criteria
4. Review logs for errors
5. Try simpler query to isolate issue

### Issue: "Query cannot be empty"

**Cause**: Missing or empty query parameter

**Solution**:
1. Provide a valid search query
2. Verify parameter is being passed correctly
3. Check for null or empty strings

### Issue: Search returns unexpected results

**Cause**: Query syntax or operator usage

**Solution**:
1. Review Gmail search operator documentation
2. Test query in Gmail web interface
3. Use quotes for exact phrase matching
4. Check operator spelling and syntax

### Issue: System error during search

**Cause**: Gmail API error or network issue

**Solution**:
1. Check network connectivity
2. Verify Gmail API is accessible
3. Simplify complex queries
4. Retry the operation
5. Review logs for specific error details

## See Also

- [Extension Configuration](pages/ExtensionConfiguration.md)
- [Authentication Guide](pages/Authentication.md)
- [Gmail Search Operators](https://support.google.com/mail/answer/7190)
- [Gmail API Search](https://developers.google.com/gmail/api/guides/filtering)

---
*Documentation updated according to Krista Extension Documentation Guidelines*

