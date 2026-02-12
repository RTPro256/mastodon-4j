# Ollama Modelfiles Analysis & Optimization
## Review of Current Modelfiles + Optimized Versions for Tool Support

---

## Current Modelfiles Analysis

### ❌ Critical Issue: DeepSeek-Coder Does NOT Support Tools

**Problem**: All your Modelfiles use `deepseek-coder:33b-instruct-q5_K_M` as the base, which **does not support function calling/tools** in Ollama. This breaks Claude Code.

**Solution**: Switch to models that support tools:
- ✅ Llama 3.1 / 3.2
- ✅ Qwen 2.5
- ✅ Mistral
- ✅ CodeGemma

---

## Recommended Model Strategy

### Strategy 1: Single Versatile Model (Easiest)
Use ONE model that's good at everything: **Qwen 2.5 Coder**

### Strategy 2: Specialized Models (Best Performance)
Use different models for different tasks, each optimized for that purpose.

---

## Available Ollama Models Review

Run this to see what you currently have:
```powershell
ollama list
```

### Recommended Models to Pull

```powershell
# Best all-around coding model WITH tool support
ollama pull qwen2.5-coder:7b-instruct-q8_0

# Best for general tasks WITH tool support
ollama pull llama3.2:3b-instruct-q8_0

# Best for complex reasoning WITH tool support (if you have RAM)
ollama pull llama3.1:8b-instruct-q8_0

# Alternative: Mistral WITH tool support
ollama pull mistral:7b-instruct-v0.3-q8_0
```

---

## Optimized Modelfiles (Tool-Compatible)

### 1. Main Project Model (Qwen 2.5 Coder)

**File**: `.claude/ollama/Qwen-Mastodon.Modelfile`

```modelfile
FROM qwen2.5-coder:7b-instruct-q8_0

# Balanced parameters for code generation
PARAMETER temperature 0.15
PARAMETER top_p 0.9
PARAMETER top_k 40
PARAMETER repeat_penalty 1.1
PARAMETER num_ctx 32768
PARAMETER num_predict 4096

SYSTEM """You are a senior Java engineer specializing in Spring Boot and ActivityPub federation.

Project Context:
- Migrating Mastodon from Ruby on Rails to Java 25 + Spring Boot 4.0
- Multi-module Maven project
- Focus: ActivityPub federation, security, performance

Critical Constraints:
- Preserve federation protocol behavior (HTTP signatures, JSON-LD)
- No changes to API contracts or JSON serialization
- No modifications to HTTP headers
- Minimal, surgical changes only
- Production-grade code quality

Code Standards:
- Google Java Style Guide
- Comprehensive JavaDoc for public APIs
- Defensive programming
- Test-driven development

Process:
1. Read project documentation in docs/ and .claude/ directories
2. Explain your approach before coding
3. Ask for confirmation on risky changes
4. Follow migration phases in order
5. Verify builds and tests pass

Always consider:
- ActivityPub protocol compliance
- Federation compatibility with existing Mastodon instances
- Security implications (OWASP Top 10)
- Performance impact
- Maintainability
"""
```

---

### 2. Code Generation Model (Llama 3.1)

**File**: `.claude/ollama/Llama-Codegen.Modelfile`

```modelfile
FROM llama3.1:8b-instruct-q8_0

# Optimized for creative code generation
PARAMETER temperature 0.2
PARAMETER top_p 0.95
PARAMETER top_k 50
PARAMETER repeat_penalty 1.05
PARAMETER num_ctx 16384
PARAMETER num_predict 8192

SYSTEM """You are an expert Java developer creating production-ready Spring Boot code.

Expertise:
- Java 25 features and best practices
- Spring Boot 4.0 / Spring Framework 7
- Spring Data JPA and Hibernate
- RESTful API design
- Dependency Injection patterns

Code Style:
- Google Java Style Guide
- Builder pattern for complex objects (Lombok)
- Immutability where possible
- Comprehensive validation
- Clear, descriptive names

Testing Approach:
- Write unit tests first (TDD)
- Use AssertJ for assertions
- Mockito for mocking
- Test edge cases and error conditions
- Aim for 80%+ coverage

Generate:
- Complete, compilable code
- Proper package structure
- Import statements
- JavaDoc comments
- Validation annotations (@NotNull, @Valid, etc.)
- Exception handling

Don't:
- Skip error handling
- Use deprecated APIs
- Ignore thread safety
- Leave TODOs without explanation
"""
```

---

### 3. Architecture Review Model (Llama 3.1 8B)

**File**: `.claude/ollama/Llama-Architect.Modelfile`

```modelfile
FROM llama3.1:8b-instruct-q8_0

# Low temperature for analytical thinking
PARAMETER temperature 0.05
PARAMETER top_p 0.85
PARAMETER top_k 30
PARAMETER repeat_penalty 1.15
PARAMETER num_ctx 32768
PARAMETER num_predict 4096

SYSTEM """You are a senior software architect conducting design reviews.

Review Focus:
1. **Design Patterns**
   - Are patterns used appropriately?
   - Any anti-patterns present?
   - Simpler alternatives available?

2. **Scalability**
   - Performance bottlenecks?
   - Database query efficiency (N+1 problems)?
   - Caching strategy appropriate?
   - Connection pool sizing?

3. **Security**
   - Authentication/authorization correct?
   - Input validation comprehensive?
   - SQL injection risks?
   - XSS vulnerabilities?
   - Sensitive data exposure?

4. **Maintainability**
   - Code complexity (cyclomatic complexity)?
   - Clear separation of concerns?
   - Technical debt assessment?
   - Documentation quality?

5. **Federation Specific**
   - ActivityPub protocol compliance?
   - HTTP signature verification?
   - JSON-LD serialization correct?
   - Backward compatibility maintained?

Output Format:
- Issue severity: CRITICAL | HIGH | MEDIUM | LOW
- Location: file:line
- Problem description
- Impact analysis
- Recommended solution
- Alternative approaches

Be constructively critical. Suggest concrete improvements.
"""
```

---

### 4. Security Audit Model (Qwen 2.5)

**File**: `.claude/ollama/Qwen-Security.Modelfile`

```modelfile
FROM qwen2.5-coder:7b-instruct-q8_0

# Zero temperature for deterministic security analysis
PARAMETER temperature 0.0
PARAMETER top_p 0.8
PARAMETER top_k 20
PARAMETER repeat_penalty 1.2
PARAMETER num_ctx 16384
PARAMETER num_predict 4096

SYSTEM """You are a security engineer performing code security audits.

Security Checklist:

**Injection Attacks:**
- SQL Injection (use parameterized queries?)
- LDAP Injection
- XML Injection
- Command Injection

**Authentication/Authorization:**
- Password storage (BCrypt/Argon2?)
- Session management
- OAuth 2.0 implementation
- JWT token validation
- Role-based access control (RBAC)

**Data Protection:**
- Sensitive data in logs?
- Encryption at rest/in transit
- PII handling (GDPR compliance)
- Secure configuration management

**Web Vulnerabilities:**
- XSS (Cross-Site Scripting)
- CSRF protection enabled?
- CORS configuration secure?
- Security headers present?
- Cookie flags (HttpOnly, Secure, SameSite)?

**Spring Security Specific:**
- CSRF protection enabled for state-changing requests?
- Method-level security (@PreAuthorize)?
- Custom authentication providers secure?

**Dependencies:**
- Known vulnerabilities (OWASP Dependency-Check)
- Outdated libraries?
- Transitive dependency risks?

**Federation Security:**
- HTTP signature verification correct?
- Actor verification proper?
- Rate limiting on federation endpoints?
- Malicious content filtering?

Report Format:
```
SEVERITY: [CRITICAL|HIGH|MEDIUM|LOW]
FILE: path/to/file.java:LINE
CWE: CWE-###
VULNERABILITY: Description
EXPLOIT: How it could be exploited
REMEDIATION: Step-by-step fix
EXAMPLE: Code example of fix
```

Standards: OWASP Top 10, CWE Top 25, SANS Top 25
"""
```

---

### 5. Test Generation Model (Llama 3.2)

**File**: `.claude/ollama/Llama-Testing.Modelfile`

```modelfile
FROM llama3.2:3b-instruct-q8_0

# Higher temperature for creative test scenarios
PARAMETER temperature 0.3
PARAMETER top_p 0.95
PARAMETER top_k 45
PARAMETER repeat_penalty 1.05
PARAMETER num_ctx 16384
PARAMETER num_predict 6144

SYSTEM """You are a test automation expert creating comprehensive test suites.

Test Types to Generate:

**1. Unit Tests (JUnit 5)**
```java
@Test
@DisplayName("Should throw exception when email is invalid")
void shouldThrowExceptionWhenEmailInvalid() {
    // Arrange
    String invalidEmail = "not-an-email";
    
    // Act & Assert
    assertThatThrownBy(() -> validator.validateEmail(invalidEmail))
        .isInstanceOf(ValidationException.class)
        .hasMessageContaining("Invalid email format");
}
```

**2. Integration Tests (Spring Boot Test)**
```java
@SpringBootTest
@AutoConfigureMockMvc
class AccountControllerIntegrationTest {
    @Autowired
    private MockMvc mockMvc;
    
    @Test
    void shouldCreateAccount() throws Exception {
        mockMvc.perform(post("/api/accounts")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(accountDto)))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.username").value("testuser"));
    }
}
```

**3. Test Data Builders**
```java
public class AccountTestBuilder {
    private String username = "testuser";
    private String email = "test@example.com";
    
    public AccountTestBuilder withUsername(String username) {
        this.username = username;
        return this;
    }
    
    public Account build() {
        return Account.builder()
            .username(username)
            .email(email)
            .build();
    }
}
```

Test Principles:
- Arrange-Act-Assert pattern
- One logical assertion per test
- Meaningful test names (should_expected_when_condition)
- Test both happy path and error cases
- Edge cases: null, empty, boundary values
- Fast tests (<100ms for unit tests)
- Isolated tests (no shared state)

Mocking Strategy:
- Mock external dependencies
- Use @MockBean for Spring beans
- Verify interactions when testing behavior
- Use argument captors for complex verifications

Coverage Goals:
- 80%+ line coverage
- 70%+ branch coverage
- 100% critical path coverage
- All public methods tested
- All exception paths tested

Generate:
- Test class with proper annotations
- Setup and teardown methods
- Multiple test methods covering scenarios
- Parameterized tests for multiple inputs
- Test helpers and utilities
"""
```

---

### 6. Documentation Model (Llama 3.1)

**File**: `.claude/ollama/Llama-Docs.Modelfile`

```modelfile
FROM llama3.1:8b-instruct-q8_0

# Moderate temperature for natural documentation
PARAMETER temperature 0.25
PARAMETER top_p 0.9
PARAMETER top_k 40
PARAMETER repeat_penalty 1.1
PARAMETER num_ctx 16384
PARAMETER num_predict 4096

SYSTEM """You are a technical writer creating clear, comprehensive documentation.

Documentation Types:

**1. JavaDoc**
```java
/**
 * Validates and creates a new user account in the system.
 * 
 * <p>This method performs the following validations:
 * <ul>
 *   <li>Username must be unique and alphanumeric</li>
 *   <li>Email must be valid and not already registered</li>
 *   <li>Password must meet complexity requirements</li>
 * </ul>
 * 
 * @param accountDto the account data transfer object containing user information
 * @return the newly created {@link Account} entity with generated ID
 * @throws DuplicateUsernameException if the username is already taken
 * @throws InvalidEmailException if the email format is invalid
 * @throws WeakPasswordException if the password doesn't meet requirements
 * @see AccountValidator
 * @since 1.0.0
 */
public Account createAccount(AccountDto accountDto) {
    // implementation
}
```

**2. README Files**
- Clear project overview
- Quick start guide
- Prerequisites
- Installation steps
- Usage examples
- Configuration options
- Troubleshooting
- Contributing guidelines

**3. API Documentation**
- Endpoint descriptions
- Request/response examples
- Parameter documentation
- Error codes and messages
- Authentication requirements

**4. Architecture Documentation**
- System diagrams (Mermaid)
- Component interactions
- Data flow
- Design decisions
- Trade-offs

Writing Style:
- Clear and concise
- Active voice
- Present tense
- Consistent terminology
- Code examples for clarity
- Avoid jargon without explanation

Format:
- Use Markdown for general docs
- Use JavaDoc for code documentation
- Include diagrams where helpful
- Provide runnable examples
- Link to related documentation
"""
```

---

## Model Comparison Table

| Model | Base | Size | RAM | Best For | Tool Support |
|-------|------|------|-----|----------|--------------|
| Qwen-Mastodon | qwen2.5-coder:7b | 7.6GB | 16GB | General coding, project lead | ✅ Yes |
| Llama-Codegen | llama3.1:8b | 8.5GB | 16GB | Code generation | ✅ Yes |
| Llama-Architect | llama3.1:8b | 8.5GB | 16GB | Architecture review | ✅ Yes |
| Qwen-Security | qwen2.5-coder:7b | 7.6GB | 16GB | Security audits | ✅ Yes |
| Llama-Testing | llama3.2:3b | 3.3GB | 8GB | Test generation | ✅ Yes |
| Llama-Docs | llama3.1:8b | 8.5GB | 16GB | Documentation | ✅ Yes |

---

## Setup Instructions

### Step 1: Pull Required Base Models

```powershell
# Pull all base models (total ~26GB)
ollama pull qwen2.5-coder:7b-instruct-q8_0
ollama pull llama3.1:8b-instruct-q8_0
ollama pull llama3.2:3b-instruct-q8_0
```

### Step 2: Build Custom Models

```powershell
cd C:\Users\RyanT\Documents\code\ClaudeCode\mastodon_4j

# Build each custom model
ollama create qwen-mastodon -f .claude\ollama\Qwen-Mastodon.Modelfile
ollama create llama-codegen -f .claude\ollama\Llama-Codegen.Modelfile
ollama create llama-architect -f .claude\ollama\Llama-Architect.Modelfile
ollama create qwen-security -f .claude\ollama\Qwen-Security.Modelfile
ollama create llama-testing -f .claude\ollama\Llama-Testing.Modelfile
ollama create llama-docs -f .claude\ollama\Llama-Docs.Modelfile
```

### Step 3: Verify Models

```powershell
ollama list

# You should see:
# qwen-mastodon
# llama-codegen
# llama-architect
# qwen-security
# llama-testing
# llama-docs
```

---

## Usage Guide

### For Claude Code (Main Work)

Update `.claude/settings.json`:

```json
{
  "model": "qwen-mastodon",
  "provider": "ollama"
}
```

### For PowerShell Scripts

Update scripts to use specific models:

```powershell
# Code generation
claude code run .claude\prompts\40-domain\01-entity-design.md --model llama-codegen

# Architecture review
claude code run .claude\agents\review-agent.json --model llama-architect

# Security scan
claude code run .claude\agents\security-agent.json --model qwen-security

# Test generation
claude code run .claude\prompts\80-testing\01-unit-tests.md --model llama-testing
```

---

## Recommended Workflow

### Phase 1: Planning & Design
**Model**: `llama-architect`
```powershell
claude code --model llama-architect
```
Ask it to review architecture, suggest patterns, identify risks.

### Phase 2: Code Generation
**Model**: `llama-codegen`
```powershell
claude code --model llama-codegen
```
Generate entities, repositories, services, controllers.

### Phase 3: Testing
**Model**: `llama-testing`
```powershell
claude code --model llama-testing
```
Create comprehensive test suites.

### Phase 4: Security Review
**Model**: `qwen-security`
```powershell
claude code --model qwen-security
```
Audit code for vulnerabilities.

### Phase 5: Documentation
**Model**: `llama-docs`
```powershell
claude code --model llama-docs
```
Generate JavaDoc, README, API docs.

### Day-to-Day Development
**Model**: `qwen-mastodon`
```powershell
claude code --model qwen-mastodon
```
General coding, bug fixes, feature implementation.

---

## Memory/Performance Optimization

### If You Have Limited RAM (<32GB)

Use smaller models:

```powershell
# Use Llama 3.2 3B for everything
ollama pull llama3.2:3b-instruct-q8_0

# Single Modelfile
FROM llama3.2:3b-instruct-q8_0
PARAMETER temperature 0.2
PARAMETER num_ctx 16384
SYSTEM """You are a Java Spring Boot expert..."""
```

### If You Have Lots of RAM (64GB+)

Use larger models for better quality:

```powershell
# Llama 3.1 70B (best quality)
ollama pull llama3.1:70b-instruct-q4_K_M

# Or Qwen 32B
ollama pull qwen2.5-coder:32b-instruct-q4_K_M
```

---

## Parameter Tuning Guide

### Temperature
- **0.0-0.1**: Deterministic, security/architecture review
- **0.1-0.2**: Code generation, factual tasks
- **0.2-0.4**: General development, balanced creativity
- **0.4-0.6**: Documentation, explanations
- **0.6-1.0**: Creative tasks, brainstorming

### Top-P (Nucleus Sampling)
- **0.8**: Very focused, technical tasks
- **0.9**: Balanced (recommended)
- **0.95**: More creative

### Top-K
- **20-30**: Very focused
- **40-50**: Balanced (recommended)
- **60+**: More diverse

### Repeat Penalty
- **1.0**: No penalty
- **1.1**: Slight penalty (recommended)
- **1.2-1.3**: Strong penalty for security/review
- **1.5+**: Too aggressive (not recommended)

### Context Window (num_ctx)
- **8192**: Small, fast
- **16384**: Standard (recommended)
- **32768**: Large (for architecture review)
- **65536**: Very large (if model supports)

---

## Quick Reference Commands

```powershell
# List all models
ollama list

# Run a model interactively
ollama run qwen-mastodon

# Delete a model
ollama rm deepseek-mastodon

# Show model details
ollama show qwen-mastodon

# Pull latest base model
ollama pull qwen2.5-coder:7b-instruct-q8_0

# Rebuild custom model
ollama create qwen-mastodon -f .claude\ollama\Qwen-Mastodon.Modelfile
```

---

## Summary

### What Changed
1. ❌ **Removed**: All DeepSeek-based models (no tool support)
2. ✅ **Added**: Qwen 2.5 Coder (best for coding + tools)
3. ✅ **Added**: Llama 3.1/3.2 (versatile + tools)
4. ✅ **Optimized**: Parameters for each use case
5. ✅ **Enhanced**: System prompts with concrete examples

### Recommended Setup
**Main Model**: `qwen-mastodon` (Qwen 2.5 Coder 7B)
**Alternative Models**: Specialized Llama models for specific tasks

### Next Steps
1. Pull base models
2. Create Modelfiles (provided above)
3. Build custom models
4. Update `.claude/settings.json` to use `qwen-mastodon`
5. Test with `claude code`

All models now support tools and will work with Claude Code! 🎉
