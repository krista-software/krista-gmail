# Gmail Extension

## Overview

The Gmail Extension provides seamless integration with Google Gmail, enabling automated email management, message operations, and workflow automation through Krista's intelligent automation platform. This extension allows you to send, receive, search, and manage emails programmatically using Gmail's powerful API.

## Key Features

- ✅ **Email Management**: Send, reply, forward, and manage emails programmatically
- ✅ **Advanced Search**: Query emails using Gmail's powerful search syntax
- ✅ **Label Operations**: Organize emails with labels and folders
- ✅ **Real-time Notifications**: Receive notifications when new emails arrive
- ✅ **OAuth 2.0 Authentication**: Secure user and service account authentication
- ✅ **Attachment Support**: Send and receive email attachments
- ✅ **Robust Error Handling**: Comprehensive validation and retry mechanisms
- ✅ **Detailed Telemetry**: Track operations with built-in metrics

## Quick Start Guide

1. **Create Google Cloud Project**: Set up a Google Cloud project and enable Gmail API
2. **Configure OAuth Credentials**: Create OAuth 2.0 credentials for authentication
3. **Configure Extension**: Set up client ID, client secret, and redirect URI in Krista
4. **Authenticate**: Complete OAuth 2.0 authentication flow
5. **Start Automating**: Use catalog requests to automate email workflows

## Documentation Structure

### Getting Started
- [Extension Configuration](pages/ExtensionConfiguration.md) - Complete setup and configuration guide
- [Authentication](pages/Authentication.md) - OAuth 2.0 authentication, scopes, and token management
- [Creating Gmail App](pages/CreatingGmailApp.md) - Step-by-step Google Cloud project setup

### Email Operations
- [Send Mail](pages/SendMail.md) - Send emails with attachments
- [Reply To Mail](pages/ReplyToMail.md) - Reply to emails
- [Reply To All](pages/ReplyToAll.md) - Reply to all recipients
- [Reply To Mail With CC and BCC](pages/ReplyToMailWithCCAndBCC.md) - Reply with CC and BCC
- [Reply To All With CC and BCC](pages/ReplyToAllWithCCAndBCC.md) - Reply to all with CC and BCC
- [Forward Mail](pages/ForwardMail.md) - Forward emails to other recipients

### Email Retrieval
- [Fetch Inbox](pages/FetchInbox.md) - Retrieve inbox emails with pagination
- [Fetch Sent](pages/FetchSent.md) - Retrieve sent emails
- [Fetch Mail By Message Id](pages/FetchMailByMessageId.md) - Get specific email by ID
- [Fetch Mail Details By Query](pages/FetchMailDetailsByQuery.md) - Search emails using queries
- [Fetch Mails By Label](pages/FetchMailsByLabel.md) - Retrieve emails by label
- [Fetch Latest Mail](pages/FetchLatestMail.md) - Get the most recent email

### Email Management
- [Move Message](pages/MoveMessage.md) - Move emails between folders
- [Mark Message](pages/MarkMessage.md) - Mark emails as read or unread
- [Fetch All Labels](pages/FetchAllLabels.md) - Get all available labels

### Event-Driven Operations
- [Trigger When New Email Arrived](pages/TriggerWhenNewEmailArrived.md) - Receive notifications for new emails
- [Get Latest Mail](pages/GetLatestMail.md) - Event-driven latest email retrieval
- [Renew Subscription](pages/RenewSubscription.md) - Maintain Gmail push notifications

## Troubleshooting Common Issues

### Authentication Errors
- **"Authentication failed. Please re-authenticate to continue"**: Your session has expired. Click the authentication link to sign in again.
- **"Access denied. Please contact your administrator"**: You don't have the required permissions. Contact your system administrator.

### Validation Errors
- **Email format errors**: Ensure email addresses follow the format `user@domain.com`
- **"No email found with Message ID"**: Verify the message ID is correct and the email exists in your account
- **"Folder does not exist"**: Check that the folder name is spelled correctly and exists in your Gmail account

### Input Validation
- **Page numbers**: Must be between 1-15
- **Labels**: Must be existing Gmail labels (system or custom)
- **Required fields**: All mandatory fields must be provided before proceeding

For detailed troubleshooting guides, see individual operation documentation pages.

## Support & Resources

- **Extension Version**: 2.0.10
- **API Compatibility**: Gmail API v1
- **Domain**: Collaboration
- **Ecosystem**: Essentials
- **Documentation**: Comprehensive guides with examples
- **Support**: Contact your Krista administrator

## See Also

- [Extension Configuration](pages/ExtensionConfiguration.md)
- [Authentication Guide](pages/Authentication.md)
- [Creating Gmail App](pages/CreatingGmailApp.md)
- [Troubleshooting Guide](pages/Troubleshooting.md)
- [Gmail API Documentation](https://developers.google.com/gmail/api)

---
*Documentation updated according to Krista Extension Documentation Guidelines*
