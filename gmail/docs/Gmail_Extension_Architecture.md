# Gmail Extension Architecture Documentation

## Table of Contents
1. [Overview](#overview)
2. [5-Layer Architecture](#5-layer-architecture)
3. [Component Architecture](#component-architecture)
4. [Data Flow Architecture](#data-flow-architecture)
5. [Authentication Flow](#authentication-flow)
6. [Error Handling Architecture](#error-handling-architecture)
7. [Performance Limitations](#performance-limitations)
8. [Error Scenarios](#error-scenarios)
9. [Integration Patterns](#integration-patterns)

## Overview

The Gmail Extension follows a sophisticated 5-layer architecture pattern designed for scalability, maintainability, and robust error handling. The extension integrates with Gmail API, OAuth 2.0 authentication, Google Pub/Sub notifications, and the Krista platform ecosystem.

### Key Architectural Principles
- **Separation of Concerns**: Clear layer boundaries with specific responsibilities
- **Dependency Injection**: HK2-based service management
- **Event-Driven Architecture**: Real-time notifications via webhooks
- **Comprehensive Error Handling**: Multi-level validation and recovery
- **Performance Optimization**: Pagination, caching, and resource management

## 5-Layer Architecture

```mermaid
graph TB
    subgraph "Presentation Layer"
        A[MessagingArea<br/>Catalog Requests]
        B[MessagingAreaSubCatalogRequests<br/>Re-entry Flows]
        C[AuthenticationResource<br/>REST Endpoints]
    end
    
    subgraph "Service Layer"
        D[Account Interface<br/>Email Operations]
        E[Email Interface<br/>Message Management]
        F[Folder Interface<br/>Label Operations]
        G[EmailBuilder<br/>Composition]
        H[ValidationOrchestrator<br/>Input Validation]
    end
    
    subgraph "Connector Layer"
        I[GmailProvider<br/>API Client]
        J[GmailProviderFactory<br/>Client Factory]
        K[GmailRequestAuthenticator<br/>OAuth Handler]
        L[GmailNotificationChannel<br/>Push Notifications]
    end
    
    subgraph "Data Layer"
        M[RefreshTokenStore<br/>OAuth Tokens]
        N[GmailAttributeStore<br/>Configuration]
        O[HistoryIdStore<br/>Sync Tracking]
        P[ErrorHandlingStateManager<br/>Validation State]
    end
    
    subgraph "Integration Layer"
        Q[Gmail API<br/>Google Services]
        R[OAuth 2.0 Service<br/>Authentication]
        S[Google Pub/Sub<br/>Notifications]
        T[Krista Platform<br/>Host Integration]
    end
    
    A --> D
    B --> D
    C --> K
    D --> I
    E --> I
    F --> I
    G --> I
    H --> D
    I --> J
    J --> M
    J --> N
    K --> M
    L --> O
    I --> Q
    K --> R
    L --> S
    A --> T
    B --> T
```

### Layer Responsibilities

#### 1. Presentation Layer
- **MessagingArea**: Primary catalog requests (15 main operations)
- **MessagingAreaSubCatalogRequests**: Validation error recovery and re-entry flows
- **AuthenticationResource**: REST endpoints for OAuth callbacks and webhooks

#### 2. Service Layer
- **Account Interface**: High-level email account operations
- **Email Interface**: Individual email message management
- **Folder Interface**: Gmail label/folder operations
- **EmailBuilder**: Fluent API for email composition
- **ValidationOrchestrator**: Centralized input validation

#### 3. Connector Layer
- **GmailProvider**: Gmail API client management and authentication
- **GmailProviderFactory**: Factory pattern for provider creation
- **GmailRequestAuthenticator**: OAuth 2.0 flow handling
- **GmailNotificationChannel**: Push notification management

#### 4. Data Layer
- **RefreshTokenStore**: Secure OAuth token persistence
- **GmailAttributeStore**: Extension configuration storage
- **HistoryIdStore**: Gmail history tracking for synchronization
- **ErrorHandlingStateManager**: Validation state management

#### 5. Integration Layer
- **Gmail API**: Google's REST API for email operations
- **OAuth 2.0 Service**: Google authentication service
- **Google Pub/Sub**: Real-time notification delivery
- **Krista Platform**: Host platform integration and services

## Component Architecture

```mermaid
graph LR
    subgraph "Gmail Extension Core"
        A[GmailExtension<br/>@Extension<br/>Version 2.0.12]
        B[GmailAttributes<br/>@Service<br/>Configuration]
        C[GmailApplication<br/>@ApplicationPath<br/>REST Config]
    end
    
    subgraph "Validation Framework"
        D[ValidationOrchestrator<br/>@Service]
        E[MessageIdValidator]
        F[EmailValidators<br/>TO/CC/BCC/ReplyTo]
        G[PaginationValidators<br/>Page/Size]
        H[QueryValidator]
        I[LabelValidator]
    end
    
    subgraph "Telemetry & Monitoring"
        J[TelemetryHelper<br/>@Service]
        K[TelemetryMetrics<br/>Platform Service]
    end
    
    subgraph "Error Management"
        L[ExtensionResponseGenerator<br/>@Service]
        M[ErrorHandlingStateManager<br/>@Service]
        N[MustAuthorizeException<br/>Auth Errors]
    end
    
    A --> B
    A --> C
    D --> E
    D --> F
    D --> G
    D --> H
    D --> I
    J --> K
    L --> M
    
    style A fill:#e1f5fe
    style D fill:#f3e5f5
    style J fill:#e8f5e8
    style L fill:#fff3e0
```

## Data Flow Architecture

```mermaid
sequenceDiagram
    participant U as User/Krista
    participant P as Presentation Layer
    participant S as Service Layer
    participant C as Connector Layer
    participant D as Data Layer
    participant G as Gmail API
    
    U->>P: Catalog Request
    P->>S: Validate Input
    S->>S: ValidationOrchestrator
    
    alt Validation Success
        S->>C: Execute Operation
        C->>D: Get Auth Token
        D-->>C: Return Token
        C->>G: Gmail API Call
        G-->>C: API Response
        C-->>S: Processed Data
        S-->>P: Success Response
        P-->>U: Result Data
    else Validation Failure
        S->>D: Store Error State
        S-->>P: Validation Error
        P->>P: Generate Re-entry Flow
        P-->>U: Ask for Correction
        U->>P: Corrected Input
        P->>S: Retry Operation
    end
    
    Note over P,G: Telemetry recorded at each step
```

## Authentication Flow

```mermaid
sequenceDiagram
    participant U as User
    participant K as Krista Platform
    participant A as GmailRequestAuthenticator
    participant O as OAuth 2.0 Service
    participant T as TokenStore
    participant G as Gmail API
    
    U->>K: Configure Extension
    K->>A: Validate Connection
    A->>O: Generate Auth URL
    O-->>A: Authorization URL
    A-->>U: Redirect to Google
    U->>O: Grant Permissions
    O->>A: Authorization Code
    A->>O: Exchange for Tokens
    O-->>A: Access & Refresh Tokens
    A->>T: Store Refresh Token
    A->>G: Test Connection
    G-->>A: Profile Data
    A-->>K: Validation Success
    
    Note over A,T: Tokens encrypted in storage
    Note over A,G: Auto-refresh on expiry
```

## Error Handling Architecture

```mermaid
graph TD
    A[Request Input] --> B{Validation}
    B -->|Pass| C[Execute Operation]
    B -->|Fail| D[Generate Error State]
    
    C --> E{Authentication}
    E -->|Valid| F[Gmail API Call]
    E -->|Invalid| G[MustAuthorizeException]
    
    F --> H{API Response}
    H -->|Success| I[Return Data]
    H -->|Error| J[Handle API Error]
    
    D --> K[Store State ID]
    K --> L[Generate Re-entry Flow]
    L --> M[Ask User for Correction]
    M --> N[Sub-Catalog Request]
    N --> A
    
    G --> O[Trigger OAuth Flow]
    O --> P[User Re-authentication]
    P --> A
    
    J --> Q{Error Type}
    Q -->|Rate Limit| R[Exponential Backoff]
    Q -->|Network| S[Retry Logic]
    Q -->|System| T[Graceful Degradation]
    
    style D fill:#ffebee
    style G fill:#fff3e0
    style J fill:#fce4ec
```

### Error Types and Handling

#### 1. Validation Errors
- **Input Validation**: Email format, pagination bounds, message ID existence
- **Recovery**: Guided re-entry flows with specific error messages
- **State Management**: Temporary state storage for retry operations

#### 2. Authentication Errors
- **MustAuthorizeException**: OAuth token expired or invalid
- **Recovery**: Automatic re-authorization flow
- **User Experience**: Seamless re-authentication prompts

#### 3. API Errors
- **Rate Limiting**: Gmail API quota exceeded
- **Network Errors**: Connection timeouts, DNS failures
- **Service Errors**: Gmail service unavailable
- **Recovery**: Exponential backoff, retry strategies

#### 4. System Errors
- **Memory Constraints**: Large email processing limits
- **Storage Errors**: Token store unavailability
- **Configuration Errors**: Invalid extension setup
- **Recovery**: Graceful degradation with user notification

## Performance Limitations

### Gmail API Quotas
```mermaid
graph LR
    A[Daily Quota<br/>1B units/day] --> B[Per-User Quota<br/>250 units/100s]
    B --> C[Operation Costs]
    
    C --> D[List Messages: 5 units]
    C --> E[Get Message: 5 units]
    C --> F[Send Message: 100 units]
    C --> G[Modify Message: 5 units]
    C --> H[Watch Mailbox: 10 units]
    
    style A fill:#e3f2fd
    style B fill:#fff3e0
    style C fill:#f3e5f5
```

### Pagination Constraints
- **Maximum Page Size**: 15 emails per request
- **Maximum Pages**: 15 pages per operation
- **Total Email Limit**: 225 emails per catalog request
- **Rationale**: Balance API efficiency with memory usage

### Memory and Resource Limits
- **Attachment Size**: 25 MB per email (Gmail API limitation)
- **Email Caching**: Minimal caching to reduce memory footprint
- **Connection Pooling**: Reuse HTTP connections for efficiency
- **Timeout Configuration**: Optimized timeout values

### Performance Optimization Strategies
- **Streaming**: Large attachments streamed rather than loaded
- **Pagination**: Prevents loading large email sets simultaneously
- **Caching Strategy**: Cache folder lists (5 min), user profiles (1 hour)
- **Garbage Collection**: Prompt cleanup of temporary objects

## Error Scenarios

### Common Error Scenarios and Resolutions

#### 1. Authentication Failures
```mermaid
graph TD
    A[Authentication Error] --> B{Error Type}
    B -->|Token Expired| C[Auto-refresh Token]
    B -->|Invalid Credentials| D[Re-authorization Flow]
    B -->|Insufficient Scopes| E[Scope Validation]
    
    C --> F{Refresh Success}
    F -->|Yes| G[Continue Operation]
    F -->|No| D
    
    D --> H[Generate Auth URL]
    H --> I[User Authorization]
    I --> J[Store New Tokens]
    J --> G
    
    E --> K[Update OAuth Scopes]
    K --> D
```

#### 2. Validation Error Recovery
```mermaid
graph TD
    A[Validation Error] --> B[Generate State ID]
    B --> C[Store Error Context]
    C --> D[Create Re-entry Flow]
    D --> E[Present Correction Form]
    E --> F[User Input]
    F --> G[Validate Correction]
    G --> H{Valid}
    H -->|Yes| I[Execute Original Request]
    H -->|No| D
    I --> J[Success Response]
```

#### 3. Rate Limiting Handling
```mermaid
graph TD
    A[API Rate Limit] --> B[Detect 429 Response]
    B --> C[Calculate Backoff Delay]
    C --> D[Wait Period]
    D --> E[Retry Request]
    E --> F{Success}
    F -->|Yes| G[Continue Operation]
    F -->|No| H{Max Retries}
    H -->|Not Reached| C
    H -->|Reached| I[Return Error]
```

## Integration Patterns

### Event-Driven Integration
- **Gmail Webhooks**: Real-time email notifications via Google Pub/Sub
- **History API**: Efficient change detection and synchronization
- **Event Processing**: Asynchronous handling of email events

### Service Integration
- **Dependency Injection**: HK2 container for service management
- **Factory Pattern**: GmailProviderFactory for client creation
- **Observer Pattern**: Event listeners for real-time updates

### Data Integration
- **Entity Mapping**: Convert Gmail API responses to Krista entities
- **Type Conversion**: Handle different data types and formats
- **Attachment Processing**: Convert between Krista Files and Java Files

This architecture ensures robust, scalable, and maintainable Gmail integration with comprehensive error handling and performance optimization.
