# Known Issues

## Overview

This page documents known issues, limitations, and workarounds for the Gmail Extension. Review this page before reporting issues to see if your problem has a known solution.

## Current Known Issues

### Issue 1: Token Refresh in Testing Mode

**Status**: Known Limitation

**Description**: When using an unverified OAuth app in testing mode, refresh tokens expire after 7 days, requiring users to re-authenticate.

**Affected Versions**: All versions

**Workaround**:
1. Complete Google's app verification process for production use
2. Use "Internal" user type for Google Workspace organizations
3. Re-authenticate every 7 days during testing

**Resolution**: Publish and verify your OAuth app with Google

### Issue 2: Large Attachment Handling

**Status**: Gmail API Limitation

**Description**: Emails with attachments larger than 25 MB cannot be sent through Gmail API.

**Affected Versions**: All versions

**Workaround**:
1. Compress large files before attaching
2. Use cloud storage links instead of direct attachments
3. Split large files across multiple emails

**Resolution**: This is a Gmail API limitation and cannot be changed

### Issue 3: Rate Limiting

**Status**: Gmail API Limitation

**Description**: Gmail API has rate limits that may cause temporary failures during high-volume operations.

**Affected Versions**: All versions

**Symptoms**:
- "Rate limit exceeded" errors
- Temporary failures during bulk operations
- Slow response times

**Workaround**:
1. Implement exponential backoff retry logic
2. Reduce request frequency
3. Batch operations when possible
4. Monitor quota usage in Google Cloud Console

**Resolution**: Stay within Gmail API quotas and implement proper retry logic

### Issue 4: Subscription Renewal

**Status**: Known Behavior

**Description**: Gmail push notification subscriptions expire after 7 days and must be renewed.

**Affected Versions**: All versions

**Symptoms**:
- Push notifications stop working after 7 days
- "Trigger When New Email Arrived" stops receiving events

**Workaround**:
- Use the "Renew Subscription" catalog request to maintain active subscriptions
- Set up automated renewal before expiration

**Resolution**: This is expected Gmail API behavior. Implement automated renewal.

## Resolved Issues

### Version 2.0.10

No resolved issues in this version.

### Previous Versions

Check release notes for issues resolved in earlier versions.

## Reporting New Issues

If you encounter an issue not listed here:

1. **Check Documentation**: Review all relevant documentation pages
2. **Verify Configuration**: Ensure extension is properly configured
3. **Check Authentication**: Verify OAuth tokens are valid
4. **Review Logs**: Check extension logs for error details
5. **Contact Support**: Provide detailed information including:
   - Extension version
   - Error messages
   - Steps to reproduce
   - Expected vs actual behavior

## See Also

- [Release Notes](pages/ReleaseNotes.md)
- [Troubleshooting Guide](pages/ExtensionConfiguration.md#8-troubleshooting)
- [Authentication Issues](pages/Authentication.md#troubleshooting-authentication)
- [Gmail API Status](https://status.cloud.google.com/)

---
*Documentation updated according to Krista Extension Documentation Guidelines*

