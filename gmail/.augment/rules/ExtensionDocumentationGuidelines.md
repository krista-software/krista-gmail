---
type: "always_apply"
---

# Extension Documentation Rules and Guidelines

## Overview

This document provides comprehensive rules and guidelines for creating high-quality, user-friendly technical documentation for Krista Extensions. These guidelines are based on best practices established during the Google Calendar Extension documentation project.

## Document Structure Requirements

### 1. Core Documentation Files

Every extension MUST have the following core files:

#### 1.1 README.md
- **Purpose**: Main landing page with overview and navigation
- **Location**: `src/main/resources/docs/README.md`
- **Required Sections**:
  - Overview (what the extension does)
  - Key Features (bullet list)
  - Quick Start Guide (getting started steps)
  - Documentation Structure (organized by category)
  - Support & Resources (version, API info)
  - Links to all documentation pages

#### 1.2 _sidebar.md
- **Purpose**: Navigation sidebar for docsify
- **Location**: `src/main/resources/docs/_sidebar.md`
- **Required Sections**:
  - Getting Started (configuration, authentication, setup)
  - Catalog Requests (organized by functional category)
  - Links to every documentation page
- **Organization**: Group catalog requests by logical categories (e.g., Event Management, Query Operations)

#### 1.3 Pages Folder
- **Purpose**: Contains individual documentation pages
- **Location**: `src/main/resources/docs/pages/`
- **Required Files**:
  - ExtensionConfiguration.md
  - Authentication.md
  - Creating[ServiceName]App.md (e.g., CreatingGoogleApp.md)
  - Individual catalog request pages (one per request)

### 2. Configuration Documentation

#### ExtensionConfiguration.md

**Required Sections**:
1. **Overview**: Brief description of configuration purpose
2. **Configuration Parameters**: Table with all setup parameters
   - Parameter Name
   - Type
   - Required/Optional
   - Description
   - Example Value
3. **Step-by-Step Setup**: Numbered instructions
4. **Authentication Type Selection**: Service account vs user authentication
5. **Redirect URI Configuration**: How to configure callback URLs
6. **Allow Retry Flag**: Explanation of retry mechanism
7. **Security Considerations**: Best practices
8. **Troubleshooting**: Common configuration issues

**Parameter Table Format**:
```markdown
| Parameter | Type | Required | Description | Example |
|-----------|------|----------|-------------|---------|
| Client ID | Text | Yes | OAuth 2.0 client identifier | "123456.apps.googleusercontent.com" |
```

### 3. Authentication Documentation

#### Authentication.md

**Required Sections**:
1. **Overview**: Authentication modes available
2. **Authentication Modes**:
   - Service Account Authentication
   - User Account Authentication
   - When to use each mode
3. **OAuth 2.0 Scopes and Permissions**:
   - Required scopes with full URLs
   - Permission level explanation
   - What each scope allows
   - Alternative scopes (if any) and why they're not used
4. **OAuth 2.0 Grant Types**:
   - Authorization Code Grant (initial authentication)
   - Refresh Token Grant (token refresh)
   - Complete flow with code examples
   - Request/response examples
5. **Authentication Flow**:
   - Step-by-step OAuth flow
   - Authorization code exchange details
   - Token storage mechanism
6. **Token Management**:
   - Access token expiration (with exact timeframes)
   - Refresh token expiration (verified vs unverified apps)
   - Token refresh flow
   - Re-authentication scenarios
7. **Token Expiration Scenarios**:
   - Access token expiration handling
   - Refresh token expiration policies
   - Revocation scenarios
   - Expiration handling flow
8. **Security Best Practices**
9. **Troubleshooting Authentication**

**Token Expiration Documentation Must Include**:
- Access token lifetime (e.g., 1 hour)
- Refresh token lifetime (different for verified/unverified apps)
- Automatic refresh mechanism
- Revocation scenarios (user action, password change, security events, token limits)
- Error handling for each scenario

**Grant Type Documentation Must Include**:
```markdown
### Authorization Code Grant
- **Grant Type**: `authorization_code`
- **Used For**: Initial authentication
- **Request Example**: (code snippet)
- **Response Example**: (JSON snippet)

### Refresh Token Grant
- **Grant Type**: `refresh_token`
- **Used For**: Token refresh
- **Request Example**: (code snippet)
- **Response Example**: (JSON snippet)
```

### 4. Service Setup Documentation

#### Creating[ServiceName]App.md

**Required Sections**:
1. **Overview**: What will be created
2. **Prerequisites**: Account requirements
3. **Step-by-Step Instructions**:
   - Creating project/app
   - Enabling APIs
   - Configuring OAuth consent screen
   - Creating credentials
   - Setting up redirect URIs
4. **Screenshots**: Visual guides for each major step
5. **Verification**: How to verify setup is correct
6. **Next Steps**: Link to authentication/configuration

## Catalog Request Documentation

### 5. Individual Catalog Request Pages

Each catalog request MUST have its own dedicated markdown file.

#### File Naming Convention
- Use PascalCase (e.g., `ScheduleMeeting.md`, `GetEvent.md`)
- Name should match the catalog request name
- Be descriptive and clear

#### Required Sections for Each Catalog Request

**1. Overview**
- Clear, concise description of what the request does
- 1-2 sentences maximum

**2. Request Details**
- **Area**: The functional area (e.g., Meetings, Contacts)
- **Type**: CHANGE_SYSTEM, QUERY_SYSTEM, etc.
- **Retry Support**: ✅ Yes or ❌ No (with explanation)
- **DO NOT INCLUDE**: Catalog Request ID (internal implementation detail)

**3. Input Parameters**
- Table format with all input parameters
- Required columns:
  - Parameter Name
  - Type
  - Required (Yes/No)
  - Description
  - Example
- Detailed parameter descriptions below the table
- Validation rules for each parameter

**4. Output Parameters**
- Table format with output parameters
- Entity structure details if applicable
- Example output values

**5. Validation Rules**
- Table format showing validation logic
- Columns: Validation, Error Message, Resolution
- Cover all validation scenarios
- Based on actual code implementation

**6. Error Handling**
- **Input Errors (INPUT_ERROR)**: Invalid parameters
- **Logic Errors (LOGIC_ERROR)**: Business logic failures
- **System Errors (SYSTEM_ERROR)**: System-level failures
- **Authorization Errors**: Authentication/permission issues
- Each error type must include:
  - Cause
  - Error message (exact text from code)
  - Common scenarios
  - Resolution steps

**7. Usage Examples**
- At least 2-3 real-world examples
- Input values
- Expected output
- Result description
- Include retry scenario example if retry is supported

**8. Business Rules**
- Numbered list of important rules
- Constraints and limitations
- Behavioral expectations

**9. Limitations**
- Technical limitations
- API limitations
- Rate limits
- Scope restrictions

**10. Best Practices**
- Numbered list with clear headings
- Practical recommendations
- Do's and don'ts

**11. Common Use Cases**
- Real-world scenarios
- Step-by-step workflows
- Expected results

**12. Related Catalog Requests**
- Links to related operations
- Cross-references to complementary requests

**13. Technical Implementation**
- Helper class name and package
- Validation flow description
- Service delegation
- Telemetry metrics tracked

**14. Troubleshooting**
- Common issues with solutions
- Error scenarios
- Resolution steps

**15. See Also**
- Links to related documentation
- Configuration references
- Authentication requirements

**16. Images**
- Include images to help explain complex concepts
- Use alt text for accessibility
- Use captions for additional context
- Use consistent naming conventions like <MD_FILE_NAME>_<IMAGE_NAME>.png
- Store images in `src/main/resources/docs/_media/`
- Link to images using markdown syntax: `![Alt Text](../_media/image.png)`

### 6. Code Review Requirements

**CRITICAL**: Every catalog request page MUST be reviewed against the actual source code:

1. **Review Helper Class**: 
   - Location: `src/main/java/.../catalog/helper/[RequestName]Helper.java`
   - Verify validation rules match code
   - Verify error messages match code
   - Verify telemetry metrics match code

2. **Review Area Class**:
   - Location: `src/main/java/.../catalog/[AreaName]Area.java`
   - Verify @CatalogRequest annotation details
   - Verify parameter names and types
   - Verify request type (CHANGE_SYSTEM, QUERY_SYSTEM)

3. **Review Service Class**:
   - Verify business logic implementation
   - Verify error handling scenarios
   - Verify API interactions

4. **Review Validator Classes**:
   - Verify exact validation logic
   - Verify error messages
   - Verify validation parameters

## Content Guidelines

### 7. Writing Style

**Tone**:
- Professional but approachable
- Clear and concise
- User-focused (not developer-focused)
- Action-oriented

**Language**:
- Use active voice
- Use present tense
- Avoid jargon unless necessary
- Define technical terms when first used
- Use "you" to address the user

**Formatting**:
- Use headings hierarchically (##, ###, ####)
- Use bullet points for lists
- Use numbered lists for sequential steps
- Use tables for structured data
- Use code blocks for examples
- Use bold for emphasis on important terms
- Use emojis sparingly for visual cues (✅, ❌, ⚠️, 📧, 👥)

### 8. Examples and Code Snippets

**Input/Output Examples**:
```markdown
**Input**:
\`\`\`
Parameter 1: "value1"
Parameter 2: "value2"
\`\`\`

**Output**:
\`\`\`json
{
  "result": "success",
  "data": { ... }
}
\`\`\`
```

**Code Examples**:
- Use proper syntax highlighting
- Include comments for clarity
- Show realistic values
- Include error handling examples

### 9. Visual Elements

**Tables**:
- Use markdown tables for structured data
- Keep columns aligned
- Use clear, concise headers
- Include examples in tables

**Warnings and Notes**:
```markdown
> **⚠️ Warning**: This is a destructive operation that cannot be undone.

> **📝 Note**: This feature requires additional configuration.

> **💡 Tip**: For better performance, consider...
```

**Comparison Tables**:
```markdown
| Feature | Option A | Option B |
|---------|----------|----------|
| Speed   | Fast     | Slow     |
| Cost    | High     | Low      |
```

## Organization Guidelines

### 10. Sidebar Organization

Group catalog requests by functional categories:

```markdown
- **Getting Started**
  - [Extension Configuration](pages/ExtensionConfiguration.md)
  - [Authentication](pages/Authentication.md)
  - [Creating Service App](pages/CreatingServiceApp.md)

- **[Category 1 Name]** (e.g., Event Management)
  - [Request 1](pages/Request1.md)
  - [Request 2](pages/Request2.md)

- **[Category 2 Name]** (e.g., Query Operations)
  - [Request 3](pages/Request3.md)
  - [Request 4](pages/Request4.md)
```

**Category Examples**:
- Event Management
- Invitation Management
- Attendee Management
- Query Operations
- Contact Management
- File Operations
- Notification Management

### 11. Cross-Referencing

**Internal Links**:
- Use relative paths: `[Link Text](PageName.md)`
- Link to related catalog requests
- Link to configuration/authentication when relevant
- Use descriptive link text (not "click here")

**External Links**:
- Link to official API documentation
- Link to service provider documentation
- Use full URLs
- Open in new tab when appropriate

## Quality Checklist

### 12. Pre-Publication Checklist

Before publishing documentation, verify:

- [ ] All core files exist (README.md, _sidebar.md)
- [ ] All catalog requests have individual pages
- [ ] All pages follow the required section structure
- [ ] All code examples are accurate and tested
- [ ] All error messages match actual code
- [ ] All validation rules match actual code
- [ ] All links work correctly
- [ ] All tables are properly formatted
- [ ] Spelling and grammar are correct
- [ ] Screenshots are clear and up-to-date
- [ ] OAuth scopes are documented with full URLs
- [ ] Grant types are documented with examples
- [ ] Token expiration scenarios are comprehensive
- [ ] No catalog request IDs are exposed
- [ ] Telemetry metrics match code implementation

### 13. Code Accuracy Verification

For each catalog request page:

- [ ] Helper class reviewed
- [ ] Validation logic verified
- [ ] Error messages verified (exact text)
- [ ] Telemetry metrics verified
- [ ] Business rules verified
- [ ] Limitations verified
- [ ] Parameter types verified
- [ ] Return types verified

## Maintenance Guidelines

### 14. Version Updates

When extension version changes:

- Update version number in README.md
- Update release notes
- Review all catalog requests for changes
- Update screenshots if UI changed
- Update code examples if API changed

### 15. Continuous Improvement

- Collect user feedback
- Track common support questions
- Update troubleshooting sections
- Add new examples based on user needs
- Improve clarity based on user confusion

## Template Files

### 16. Catalog Request Template

Use this template for new catalog request pages:

```markdown
# [Request Name]

## Overview

[Brief description of what this request does]

## Request Details

- **Area**: [Area Name]
- **Type**: [CHANGE_SYSTEM | QUERY_SYSTEM]
- **Retry Support**: [✅ Yes | ❌ No] (explanation)

## Input Parameters

| Parameter Name | Type | Required | Description | Example |
|----------------|------|----------|-------------|---------|
| [Name] | [Type] | [Yes/No] | [Description] | [Example] |

## Output Parameters

| Parameter Name | Type | Description |
|----------------|------|-------------|
| [Name] | [Type] | [Description] |

## Validation Rules

[When Allow Retry = true]

| Validation | Error Message | Resolution |
|------------|---------------|------------|
| [Rule] | [Message] | [Solution] |

## Error Handling

### Input Errors (INPUT_ERROR)
**Cause**: [Description]
**Resolution**: [Steps]

### Logic Errors (LOGIC_ERROR)
**Cause**: [Description]
**Resolution**: [Steps]

### System Errors (SYSTEM_ERROR)
**Cause**: [Description]
**Resolution**: [Steps]

## Usage Examples

### Example 1: [Scenario]

**Input**:
\`\`\`
[Input values]
\`\`\`

**Output**:
\`\`\`json
[Output JSON]
\`\`\`

## Business Rules

1. [Rule 1]
2. [Rule 2]

## Limitations

1. [Limitation 1]
2. [Limitation 2]

## Best Practices

### 1. [Practice Name]
[Description]

## Common Use Cases

### 1. [Use Case Name]
\`\`\`
Scenario: [Description]
Action: [Steps]
Result: [Outcome]
\`\`\`

## Related Catalog Requests

- [Related Request 1](RelatedRequest1.md)
- [Related Request 2](RelatedRequest2.md)

## Technical Implementation

### Helper Class
- **Class**: [ClassName]
- **Package**: [package.name]
- **Validation**: [Description]
- **Service**: [Service method]

### Telemetry Metrics
- [METRIC_NAME_1]: [Description]
- [METRIC_NAME_2]: [Description]

## Troubleshooting

### [Issue Name]
**Cause**: [Description]
**Solution**: [Steps]

## See Also

- [Configuration](ExtensionConfiguration.md)
- [Authentication](Authentication.md)
```

## Summary

Following these guidelines ensures:

✅ **Consistency**: All extensions documented in the same format
✅ **Completeness**: All necessary information is included
✅ **Accuracy**: Documentation matches actual code implementation
✅ **Usability**: Users can easily find and understand information
✅ **Maintainability**: Documentation is easy to update and maintain
✅ **Professionalism**: High-quality, production-ready documentation

Apply these guidelines to all Krista Extension documentation projects for best results.

