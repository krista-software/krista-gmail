# Release Notes

## Overview

This page documents version history, new features, improvements, and bug fixes for the Gmail Extension.

## Version 2.0.17 (Current)

Developer Name : Simran Sethi
Krista Service APIs Java : 1.0.120
Global Catalog Version : GC-2026.2.1

**Release Date**: 02.02.2026

**Status**: Current Stable Release

### Improvements

- **Documentation Enhancement**: Updated and standardized documentation structure across README.md and sidebar
  navigation for improved user experience and consistency

---

## Version 2.0.16

Developer Name : Vrushali Gaikwad
Krista Service APIs Java : 1.0.120
Global Catalog Version : GC-2026.1.4

**Release Date**: 17.12.2025

**Status**: Previous Stable Release

### New Features

- Comprehensive email management capabilities
- Support for rich text email formatting
- Multiple attachment handling
- Advanced email search with Gmail query syntax
- Real-time push notifications for new emails
- Automated subscription renewal

### Catalog Requests

**Email Operations**:

- Send Mail
- Reply To Mail
- Reply To All
- Reply To Mail With CC and BCC
- Reply To All With CC and BCC
- Forward Mail

**Email Retrieval**:

- Fetch Inbox
- Fetch Sent
- Fetch Mail By Message Id
- Fetch Mail Details By Query
- Fetch Mails By Label
- Fetch Latest Mail

**Email Management**:

- Move Message
- Mark Message
- Fetch All Labels

**Event-Driven Operations**:

- Trigger When New Email Arrived
- Get Latest Mail
- Renew Subscription

### Improvements

- **Allow Retry Feature**: Added optional "Allow Retry" parameter to all catalog request methods
    - Users can now enable/disable retry prompts for validation errors
    - When enabled, users are prompted to re-enter invalid inputs instead of receiving immediate errors
    - Applies to all 13 catalog request methods: Fetch Mail By Message Id, Move Message, Reply To All, Fetch Sent,
      Forward Mail, Fetch Mail Details By Query, Fetch Inbox, Mark Message, Fetch Mails By Label, Send Mail, Reply To
      Mail, Reply To Mail With Fields, Reply To All With Fields
    - Default value is `false` for backward compatibility
- Enhanced validation with retry prompts for invalid inputs
- Comprehensive telemetry tracking for all operations
- Improved error handling and user feedback
- Detailed logging for troubleshooting
- Support for pagination in email retrieval

### Bug Fixes

- **Fixed NullPointerException with Optional Parameters**: Resolved critical issue where null values for optional
  parameters (`pageNumber`, `pageSize`) caused application crashes
    - Fixed in `MessagingArea.java`: Updated `fetchSent`, `fetchInbox`, and `fetchMailsByLabel` methods to use `HashMap`
      instead of `Map.of()` for state management and confirmation responses
    - Fixed in `MessagingAreaSubCatalogRequests.java`: Updated `confirmReenterFetchSent`, `confirmReenterFetchInbox`,
      and `confirmReenterFetchMailByLabel` methods to properly handle null values
    - Root cause: `Map.of()` doesn't allow null values, but optional parameters can be null
    - Impact: All retry flows now work correctly when users don't provide optional pagination parameters

### Technical Details

- **Domain**: Collaboration
- **Ecosystem**: Essentials
- **API**: Gmail API v1
- **Authentication**: OAuth 2.0
- **Scopes**: gmail.modify, gmail.send, gmail.readonly, gmail.labels

### Known Limitations

- Attachment size limited to 25 MB per email (Gmail API limitation)
- Rate limits apply based on Gmail API quotas
- Push notification subscriptions expire after 7 days
- Refresh tokens expire after 7 days for unverified apps in testing mode

## Previous Versions

### Version 2.0.13

Developer Name : Hamitha Chandu
Krista Service APIs Java : 1.0.118
Global Catalog Version : GC-2025.11.1

**Release Date**: 10.10.2025

**Status**: Previous Stable Release

- Comprehensive email management capabilities
- Support for rich text email formatting
- Multiple attachment handling
- Advanced email search with Gmail query syntax
- Real-time push notifications for new emails
- Automated subscription renewal

### Version 2.0.x

Earlier versions of the 2.0 series with incremental improvements and bug fixes.

### Version 1.x

Legacy versions with basic Gmail integration capabilities.

## Upgrade Notes

### Upgrading to 2.0.14

**Breaking Changes**: None

**New Requirements**: None

**Migration Steps**:

1. Update extension to version 2.0.14
2. Rebuild the application: `mvn clean install` or `./gradlew clean build`
3. Restart the application to load the new compiled code
4. Review the new "Allow Retry" parameter available on all catalog requests
5. Update workflows to leverage the retry feature if desired
6. Test validation and retry flows with optional parameters

**Compatibility**: Fully backward compatible with previous 2.0.x versions

- The "Allow Retry" parameter is optional and defaults to `false`
- Existing workflows will continue to work without modification

### Upgrading to 2.0.13

**Breaking Changes**: None

**New Requirements**: None

**Migration Steps**:

1. Update extension to version 2.0.13
2. Review new catalog requests available
3. Update workflows to leverage new features
4. Test authentication and basic operations

**Compatibility**: Fully backward compatible with previous 2.0.x versions

## Deprecation Notices

No features are currently deprecated.

## Future Roadmap

Planned features for future releases:

- Enhanced attachment handling for large files
- Batch email operations
- Advanced filtering and search capabilities
- Email template support
- Improved push notification reliability

## Support

For questions or issues:

1. Review [Known Issues](pages/KnownIssues.md)
2. Check [Documentation](README.md)
3. Contact your Krista administrator
4. Report bugs with detailed information

## See Also

- [Known Issues](pages/KnownIssues.md)
- [Extension Configuration](pages/ExtensionConfiguration.md)
- [Authentication Guide](pages/Authentication.md)
- [Gmail API Release Notes](https://developers.google.com/gmail/api/release-notes)

---
*Documentation updated according to Krista Extension Documentation Guidelines*

