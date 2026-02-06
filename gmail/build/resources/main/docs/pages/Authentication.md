# Authentication

## Overview

The Gmail Extension uses OAuth 2.0 for secure authentication with Google's Gmail API. This document provides comprehensive information about authentication modes, OAuth scopes, grant types, token management, and security best practices.

## Authentication Modes

### User Account Authentication

The Gmail Extension supports OAuth 2.0 user authentication, allowing users to grant the extension access to their Gmail account.

**When to Use**:
- Accessing individual user mailboxes
- User-specific email operations (send, read, manage emails)
- Scenarios requiring user consent and delegated access

**How it Works**:
1. User initiates authentication through Krista
2. User is redirected to Google's OAuth consent screen
3. User grants requested permissions
4. Google returns an authorization code
5. Extension exchanges code for access and refresh tokens
6. Extension uses tokens to access Gmail API on behalf of the user

**Benefits**:
- User-specific access control
- Granular permission management
- Audit trail of user actions
- Automatic token refresh

## OAuth 2.0 Scopes and Permissions

The Gmail Extension requires specific OAuth 2.0 scopes to access Gmail functionality. Scopes define what the extension can do with the user's Gmail account.

### Required Scopes

| Scope | Permission Level | Description | What It Allows |
|-------|-----------------|-------------|----------------|
| `https://www.googleapis.com/auth/gmail.modify` | Read/Write | Full access to Gmail mailbox except delete | Send, read, modify emails and labels |
| `https://www.googleapis.com/auth/gmail.send` | Write | Send emails | Send emails on behalf of the user |
| `https://www.googleapis.com/auth/gmail.readonly` | Read | Read-only access | View emails and settings |
| `https://www.googleapis.com/auth/gmail.labels` | Read/Write | Manage labels | Create, read, update, delete labels |

### Primary Scope Used

**`https://www.googleapis.com/auth/gmail.modify`**

This scope provides comprehensive access for most Gmail operations:
- ✅ Read emails and metadata
- ✅ Send emails
- ✅ Modify emails (mark as read, move, label)
- ✅ Manage labels
- ❌ Permanently delete emails (requires `gmail.readonly` scope)

### Why Not Use Other Scopes?

**`https://mail.google.com/`** (Full Gmail Access):
- Provides complete access including permanent deletion
- Too broad for most use cases
- Violates principle of least privilege
- Not recommended unless absolutely necessary

**`https://www.googleapis.com/auth/gmail.compose`** (Compose Only):
- Limited to composing and sending emails
- Cannot read or modify existing emails
- Too restrictive for comprehensive email automation

## OAuth 2.0 Grant Types

The Gmail Extension uses two OAuth 2.0 grant types for authentication and token refresh.

### Authorization Code Grant

**Grant Type**: `authorization_code`

**Used For**: Initial user authentication

**Flow**:
1. User clicks "Authenticate" in Krista
2. User is redirected to Google OAuth consent screen
3. User grants permissions
4. Google redirects back with authorization code
5. Extension exchanges code for tokens

**Request Example**:
```http
POST https://oauth2.googleapis.com/token
Content-Type: application/x-www-form-urlencoded

code=4/0AY0e-g7xxxxxxxxxxxxxxxxxxx
client_id=123456789.apps.googleusercontent.com
client_secret=GOCSPX-abc123def456
redirect_uri=<Extension Base URL>/rest/gmail/callback
grant_type=authorization_code
```

**Response Example**:
```json
{
  "access_token": "ya29.a0AfH6SMBxxxxxxxxxxxxx",
  "expires_in": 3600,
  "refresh_token": "1//0gxxxxxxxxxxxxxxxxxx",
  "scope": "https://www.googleapis.com/auth/gmail.modify",
  "token_type": "Bearer"
}
```

### Refresh Token Grant

**Grant Type**: `refresh_token`

**Used For**: Obtaining new access tokens when they expire

**Flow**:
1. Access token expires (after 1 hour)
2. Extension automatically uses refresh token
3. Google issues new access token
4. Extension continues operations seamlessly

**Request Example**:
```http
POST https://oauth2.googleapis.com/token
Content-Type: application/x-www-form-urlencoded

client_id=123456789.apps.googleusercontent.com
client_secret=GOCSPX-abc123def456
refresh_token=1//0gxxxxxxxxxxxxxxxxxx
grant_type=refresh_token
```

**Response Example**:
```json
{
  "access_token": "ya29.a0AfH6SMBxxxxxxxxxxxxx",
  "expires_in": 3600,
  "scope": "https://www.googleapis.com/auth/gmail.modify",
  "token_type": "Bearer"
}
```

## Authentication Flow

### Complete OAuth 2.0 Flow

```
┌─────────┐                                  ┌─────────┐                    ┌─────────┐
│  User   │                                  │ Krista  │                    │ Google  │
└────┬────┘                                  └────┬────┘                    └────┬────┘
     │                                            │                              │
     │  1. Initiate Authentication                │                              │
     ├───────────────────────────────────────────>│                              │
     │                                            │                              │
     │  2. Redirect to Google OAuth               │                              │
     │<───────────────────────────────────────────┤                              │
     │                                            │                              │
     │  3. User Grants Permissions                │                              │
     ├────────────────────────────────────────────┼─────────────────────────────>│
     │                                            │                              │
     │  4. Redirect with Authorization Code       │                              │
     │<───────────────────────────────────────────┼──────────────────────────────┤
     │                                            │                              │
     │  5. Send Authorization Code                │                              │
     ├───────────────────────────────────────────>│                              │
     │                                            │                              │
     │                                            │  6. Exchange Code for Tokens │
     │                                            ├─────────────────────────────>│
     │                                            │                              │
     │                                            │  7. Return Access & Refresh  │
     │                                            │<─────────────────────────────┤
     │                                            │                              │
     │  8. Authentication Complete                │                              │
     │<───────────────────────────────────────────┤                              │
     │                                            │                              │
```

### Authorization Code Exchange Details

**Step 1: User Authorization**
- User is redirected to Google's OAuth consent screen
- URL includes client_id, redirect_uri, scope, and response_type=code
- User reviews and grants permissions

**Step 2: Authorization Code Return**
- Google redirects to redirect_uri with authorization code
- Code is valid for 10 minutes
- Code can only be used once

**Step 3: Token Exchange**
- Extension sends authorization code to Google token endpoint
- Includes client_id, client_secret, and redirect_uri for verification
- Google validates and returns tokens

**Step 4: Token Storage**
- Access token and refresh token are stored securely
- Tokens are associated with the authenticated user
- Tokens are encrypted at rest

## Token Management

### Access Token

**Lifetime**: 1 hour (3600 seconds)

**Characteristics**:
- Short-lived for security
- Used for all Gmail API requests
- Automatically refreshed before expiration
- Included in Authorization header: `Bearer <access_token>`

**Usage Example**:
```http
GET https://gmail.googleapis.com/gmail/v1/users/me/messages
Authorization: Bearer ya29.a0AfH6SMBxxxxxxxxxxxxx
```

### Refresh Token

**Lifetime**: Varies based on OAuth consent screen verification status

**Verified Apps** (Production):
- Refresh tokens do not expire unless revoked
- Valid indefinitely with regular use
- Revoked after 6 months of inactivity

**Unverified Apps** (Testing):
- Refresh tokens expire after 7 days
- Requires re-authentication after expiration
- Limited to 100 test users

**Characteristics**:
- Long-lived credential
- Used only to obtain new access tokens
- Never sent to Gmail API
- Stored securely and encrypted

### Token Refresh Flow

The extension automatically refreshes access tokens:

1. **Before Expiration**: Extension checks token expiration before each request
2. **Refresh Request**: If token expires in < 5 minutes, refresh is triggered
3. **New Access Token**: Google issues new access token
4. **Seamless Operation**: User experiences no interruption
5. **Error Handling**: If refresh fails, user is prompted to re-authenticate

## Token Expiration Scenarios

### Access Token Expiration

**When**: After 1 hour of issuance

**Handling**:
```
1. Extension detects token expiration
2. Automatically uses refresh token
3. Obtains new access token
4. Retries original request
5. Operation continues seamlessly
```

**User Impact**: None (automatic and transparent)

### Refresh Token Expiration

**Causes**:
1. **User Revocation**: User revokes access in Google Account settings
2. **Password Change**: User changes Google account password
3. **Security Events**: Suspicious activity detected by Google
4. **Token Limit**: User exceeds maximum number of refresh tokens (50 per client)
5. **Inactivity**: 6 months of no use (verified apps only)
6. **Testing Expiration**: 7 days for unverified apps

**Handling**:
```
1. Extension attempts to refresh access token
2. Refresh request fails with invalid_grant error
3. Extension marks authentication as expired
4. User is prompted to re-authenticate
5. User completes OAuth flow again
6. New tokens are issued
```

**User Impact**: Requires re-authentication

### Expiration Handling Flow

```
┌─────────────────────────┐
│  API Request Initiated  │
└───────────┬─────────────┘
            │
            ▼
┌─────────────────────────┐
│ Check Access Token      │
│ Expiration              │
└───────────┬─────────────┘
            │
            ▼
      ┌─────────┐
      │ Expired?│
      └────┬────┘
           │
     ┌─────┴─────┐
     │           │
    Yes         No
     │           │
     ▼           ▼
┌─────────┐  ┌──────────┐
│ Refresh │  │ Use Token│
│ Token   │  │ Directly │
└────┬────┘  └──────────┘
     │
     ▼
┌─────────────┐
│ Refresh     │
│ Successful? │
└──────┬──────┘
       │
  ┌────┴────┐
  │         │
 Yes       No
  │         │
  ▼         ▼
┌────┐  ┌──────────────┐
│Use │  │ Prompt User  │
│New │  │ Re-auth      │
│Token│  └──────────────┘
└────┘
```

## Security Best Practices

### Credential Security

1. **Never Expose Credentials**
   - Don't commit Client ID/Secret to version control
   - Use environment variables or secure vaults
   - Rotate credentials periodically

2. **Secure Token Storage**
   - Encrypt tokens at rest
   - Use secure key-value stores
   - Implement access controls

3. **HTTPS Only**
   - Always use HTTPS for redirect URIs
   - Implement proper SSL/TLS certificates
   - Validate certificate chains

### Access Control

1. **Principle of Least Privilege**
   - Request minimum required scopes
   - Review scope requirements regularly
   - Remove unnecessary permissions

2. **User Consent**
   - Clearly explain why permissions are needed
   - Allow users to review granted permissions
   - Provide easy revocation mechanism

3. **Audit and Monitoring**
   - Log all authentication events
   - Monitor for suspicious patterns
   - Alert on failed authentication attempts

### Token Management

1. **Automatic Refresh**
   - Refresh tokens before expiration
   - Implement exponential backoff for retries
   - Handle refresh failures gracefully

2. **Token Revocation**
   - Revoke tokens when no longer needed
   - Implement user-initiated revocation
   - Clean up expired tokens

3. **Error Handling**
   - Catch and log token errors
   - Provide clear error messages to users
   - Implement fallback mechanisms

## Troubleshooting Authentication

### "Authentication failed. Please re-authenticate to continue"

**Cause**: Your authentication session has expired or is invalid

**Resolution**:
1. Click the authentication link in the error message
2. Complete the OAuth flow by signing into your Google account
3. Grant the necessary permissions to the application
4. Retry your operation

### "Authentication required. Please authenticate to access your Gmail account"

**Cause**: You haven't authenticated with the Gmail Extension yet

**Resolution**:
1. Navigate to the extension configuration
2. Click "Authenticate" or "Connect to Gmail"
3. Follow the OAuth authentication process
4. Ensure you grant all required permissions

### "Your access token has expired. Please re-authorize the application"

**Cause**: The refresh token has expired (typically after 6 months of inactivity)

**Resolution**:
1. Go to extension settings and remove current authentication
2. Re-authenticate from scratch to generate new tokens
3. This will restore full access to your Gmail account

### "Access denied. Please contact your administrator"

**Cause**: Insufficient permissions or incorrect application configuration

**Resolution**:
1. Contact your system administrator
2. Verify your user account has the necessary permissions
3. Ensure the application is properly configured with correct OAuth scopes
4. Check that the Gmail API is enabled for your organization

### Issue: "Invalid Client"

**Cause**: Incorrect Client ID or Client Secret

**Resolution**:
1. Verify credentials in Google Cloud Console
2. Check for typos or extra spaces
3. Ensure credentials are from correct project
4. Regenerate if necessary

### Issue: "Redirect URI Mismatch"

**Cause**: Redirect URI doesn't match Google Cloud Console

**Resolution**:
1. Check exact URI in Google Cloud Console
2. Ensure protocol (https://) matches
3. Verify trailing slashes
4. Update configuration to match

### Issue: "Token Expired"

**Cause**: Refresh token expired or revoked

**Resolution**:
1. Prompt user to re-authenticate
2. Check if user revoked access
3. Verify OAuth consent screen status
4. Complete new authentication flow

### Issue: "Invalid Grant"

**Cause**: Authorization code already used or expired

**Resolution**:
1. Restart authentication flow
2. Ensure code is used within 10 minutes
3. Don't reuse authorization codes
4. Check for clock skew issues

## See Also

- [Extension Configuration](pages/ExtensionConfiguration.md)
- [Creating Gmail App](pages/CreatingGmailApp.md)
- [Gmail API Scopes](https://developers.google.com/gmail/api/auth/scopes)
- [OAuth 2.0 Documentation](https://developers.google.com/identity/protocols/oauth2)

---
*Documentation updated according to Krista Extension Documentation Guidelines*

