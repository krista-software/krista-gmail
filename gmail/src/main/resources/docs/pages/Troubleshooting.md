# Troubleshooting Guide

## Overview

This guide provides solutions to common issues you may encounter when using the Gmail Extension. Error messages have been designed to be user-friendly and actionable, providing clear guidance on how to resolve problems.

## Authentication Issues

### "Authentication failed. Please re-authenticate to continue"

**Cause**: Your authentication session has expired or is invalid.

**Solution**:
1. Click the authentication link provided in the error message
2. Complete the OAuth flow by signing into your Google account
3. Grant the necessary permissions to the application
4. Retry your operation

### "Authentication required. Please authenticate to access your Gmail account"

**Cause**: You haven't authenticated with the Gmail Extension yet.

**Solution**:
1. Navigate to the extension configuration
2. Click "Authenticate" or "Connect to Gmail"
3. Follow the OAuth authentication process
4. Ensure you grant all required permissions

### "Your access token has expired. Please re-authorize the application"

**Cause**: The refresh token has expired (typically after 6 months of inactivity).

**Solution**:
1. Go to extension settings
2. Remove the current authentication
3. Re-authenticate from scratch
4. This will generate new tokens

### "Access denied. Please contact your administrator"

**Cause**: You don't have the required permissions or the application configuration is incorrect.

**Solution**:
1. Contact your system administrator
2. Verify your user account has the necessary permissions
3. Ensure the application is properly configured
4. Check that OAuth scopes are correctly set

## Email Validation Errors

### "The 'To' email addresses are not valid"

**Cause**: One or more email addresses in the To field have incorrect format.

**Solution**:
1. Check each email address follows the format: `user@domain.com`
2. Remove any extra spaces or special characters
3. Ensure domain names are valid
4. Separate multiple addresses with commas

**Examples of valid formats**:
- `john.doe@company.com`
- `user+tag@domain.org`
- `firstname.lastname@subdomain.domain.com`

### "The CC/BCC email addresses are not valid"

**Cause**: Similar to To field validation errors.

**Solution**:
1. Apply the same validation rules as To field
2. Ensure all CC and BCC addresses are properly formatted
3. Remove any invalid characters or formatting

### "The reply-to email address could not be validated"

**Cause**: The reply-to address format is incorrect.

**Solution**:
1. Ensure the reply-to address is a valid email format
2. This field typically accepts only one email address
3. Verify the domain exists and is accessible

## Message and Folder Validation

### "No email found with Message ID"

**Cause**: The specified message ID doesn't exist in your Gmail account.

**Solution**:
1. Verify the message ID is correct
2. Check that the email hasn't been deleted
3. Ensure you're using the correct Gmail account
4. Message IDs are case-sensitive - check for typos

### "The folder does not exist in your Gmail account"

**Cause**: The specified folder name is incorrect or doesn't exist.

**Solution**:
1. Check the folder name spelling (case-sensitive)
2. Use standard Gmail folder names: `INBOX`, `SENT`, `DRAFT`, `TRASH`
3. For custom folders, verify they exist in your Gmail account
4. Folder names should not include special characters

### "The label is not valid or does not exist"

**Cause**: The Gmail label doesn't exist in your account.

**Solution**:
1. Verify the label exists in your Gmail account
2. Check spelling and capitalization
3. Create the label in Gmail if it doesn't exist
4. Use system labels like `IMPORTANT`, `STARRED`, `UNREAD`

## Input Validation Issues

### "Please enter a valid page number (1-15)"

**Cause**: Page number is outside the allowed range.

**Solution**:
1. Use page numbers between 1 and 15 only
2. Page numbers must be positive integers
3. For large result sets, use multiple requests with different page numbers

### "Required values were not provided"

**Cause**: Mandatory fields are missing or empty.

**Solution**:
1. Review the error message to identify missing fields
2. Provide values for all required parameters
3. Ensure values are not just whitespace
4. Check that all mandatory fields are properly filled

## Connection and API Issues

### "Unable to connect to Gmail services"

**Cause**: Network connectivity or Gmail API issues.

**Solution**:
1. Check your internet connection
2. Verify Gmail services are accessible
3. Try again after a few minutes
4. Check if your organization blocks Gmail API access
5. Contact your IT administrator if issues persist

### Rate Limiting Errors

**Cause**: Too many requests sent to Gmail API in a short time.

**Solution**:
1. Wait a few minutes before retrying
2. Reduce the frequency of requests
3. Implement delays between operations
4. Contact administrator if limits are consistently exceeded

## General Troubleshooting Steps

### Step 1: Verify Inputs
1. Double-check all email addresses for correct format
2. Ensure required fields are filled
3. Verify folder and label names exist

### Step 2: Check Authentication
1. Confirm you're authenticated to the correct Gmail account
2. Re-authenticate if tokens have expired
3. Verify permissions are granted

### Step 3: Test with Simple Operations
1. Try basic operations like fetching inbox
2. Gradually test more complex operations
3. Isolate the specific issue

### Step 4: Review Error Messages
1. Read error messages carefully - they provide specific guidance
2. Follow the suggested actions in the error message
3. Check this troubleshooting guide for detailed solutions

## Getting Additional Help

If you continue to experience issues after following this guide:

1. **Document the Error**: Note the exact error message and steps that led to it
2. **Check Logs**: Review application logs for additional details
3. **Contact Support**: Reach out to your Krista administrator with:
   - The specific error message
   - Steps to reproduce the issue
   - Your Gmail account information (without passwords)
   - Any relevant screenshots

## See Also

- [Extension Configuration](ExtensionConfiguration.md)
- [Authentication Guide](Authentication.md)
- [Creating Gmail App](CreatingGmailApp.md)
- [Gmail API Documentation](https://developers.google.com/gmail/api)

---
*Documentation updated according to Krista Extension Documentation Guidelines*
