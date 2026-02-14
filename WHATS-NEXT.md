# What's Next - Getting Started Guide

## 🎉 BUILD SUCCESS! Now What?

Your mastodon_4j project is now built and ready to run. Here's your roadmap.

---

## 🚀 Step 1: Run the Application

### Start the Server

```powershell
cd mastodon-web
..\mvnw.cmd spring-boot:run
```

**What to expect:**
- Server starts on port 8080
- H2 database auto-creates in `./data/` folder
- Spring Boot banner appears
- You'll see log messages showing initialization

**Look for this line:**
```
Started MastodonApplication in X.XXX seconds
```

### Access Points

Once running, you can access:

| What | URL | Purpose |
|------|-----|---------|
| **Main Application** | http://localhost:8080 | REST API endpoints |
| **H2 Database Console** | http://localhost:8080/h2-console | View/query database |
| **Health Check** | http://localhost:8080/actuator/health | Verify app is running |

---

## 🗄️ Step 2: Explore the Database

### Access H2 Console

1. Open: http://localhost:8080/h2-console
2. Use these credentials:
   - **JDBC URL:** `jdbc:h2:file:./data/mastodon_dev`
   - **Username:** `sa`
   - **Password:** (leave empty)
3. Click "Connect"

### What You'll See

The database should have tables created by Hibernate:
- `accounts` - User accounts
- `statuses` - Posts/toots
- `follows` - Follow relationships
- And more...

**Try a query:**
```sql
SELECT * FROM accounts;
```

---

## 🔍 Step 3: Test the API

### Using PowerShell (Invoke-RestMethod)

```powershell
# Health check
Invoke-RestMethod http://localhost:8080/actuator/health

# Check API info (if endpoint exists)
Invoke-RestMethod http://localhost:8080/api/v1/instance
```

### Using curl (Git Bash)

```bash
# Health check
curl http://localhost:8080/actuator/health

# Get instance info
curl http://localhost:8080/api/v1/instance
```

### Using Browser

Simply visit: http://localhost:8080

---

## 📚 Step 4: Understand the Architecture

### Module Breakdown

Your project has 8 modules working together:

```
mastodon_4j/
├── mastodon-core         → Domain models, repositories
├── mastodon-web          → REST API, main application ⭐
├── mastodon-activitypub  → Federation protocol
├── mastodon-federation   → Inter-server communication
├── mastodon-media        → Image/video processing
├── mastodon-streaming    → Real-time WebSocket/SSE
├── mastodon-jobs         → Background tasks
└── mastodon-ui           → Web frontend (planned)
```

**⭐ mastodon-web** is where you'll spend most of your time!

### Key Files to Know

| File | Purpose | When to Edit |
|------|---------|--------------|
| `application.yml` | Configuration | Change ports, database settings |
| `MastodonApplication.java` | Main class | Add global configurations |
| `*Controller.java` | API endpoints | Add new REST APIs |
| `*Entity.java` | Database models | Change data structure |
| `*Repository.java` | Database queries | Add custom queries |

---

## 💻 Step 5: Start Developing

### Development Workflow

**1. Make changes to code**

**2. Rebuild and restart:**
```powershell
# Stop the app (Ctrl+C)
# Then rebuild and run:
..\mvnw.cmd spring-boot:run
```

**3. Test your changes**

**4. Repeat!**

### Quick Restart (Faster)

If you only changed Java code (no dependencies):
```powershell
# Just restart (no rebuild needed if using spring-boot-devtools)
Ctrl+C
..\mvnw.cmd spring-boot:run
```

---

## 🎯 Beginner-Friendly First Tasks

### Task 1: Create a Simple "Hello World" Endpoint

**File:** `mastodon-web/src/main/java/org/joinmastodon/web/api/HelloController.java`

```java
package org.joinmastodon.web.api;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1")
public class HelloController {
    
    @GetMapping("/hello")
    public String hello() {
        return "Hello from Mastodon Java!";
    }
}
```

**Test it:**
```powershell
Invoke-RestMethod http://localhost:8080/api/v1/hello
```

---

### Task 2: Explore Existing Endpoints

Check what's already implemented:

```powershell
# List all endpoints (if actuator mappings enabled)
Invoke-RestMethod http://localhost:8080/actuator/mappings
```

Or manually explore:
- Look in `mastodon-web/src/main/java/org/joinmastodon/web/api/`
- Each `*Controller.java` file defines endpoints

---

### Task 3: Create Your First Account

Once you understand the structure, try:
1. Find or create `AccountController.java`
2. Add a method to create accounts
3. Test with PowerShell:

```powershell
$body = @{
    username = "testuser"
    email = "test@example.com"
} | ConvertTo-Json

Invoke-RestMethod -Method POST -Uri http://localhost:8080/api/v1/accounts -Body $body -ContentType "application/json"
```

---

## 📖 Step 6: Read the Documentation

Your project has great docs! Start here:

1. **CLAUDE.md** - Main documentation hub
2. **docs/domain-models.md** - Understand data structures
3. **docs/api-endpoints.md** - See what APIs are planned
4. **docs/migration-strategy.md** - 24-week roadmap

**Read these to understand the big picture!**

---

## 🛠️ Development Tools

### Recommended IDE Setup

**IntelliJ IDEA (Best for Spring Boot):**
1. Open project folder
2. Wait for Maven import
3. Right-click `MastodonApplication.java` → Run

**VS Code:**
1. Install "Extension Pack for Java"
2. Install "Spring Boot Extension Pack"
3. Open project folder

### Useful Commands

```powershell
# Build without running
mvnw.cmd clean install

# Run with specific profile
mvnw.cmd spring-boot:run -Dspring-boot.run.profiles=postgres

# Run tests (when you write them)
mvnw.cmd test -DskipTests=false

# Clean everything (including database)
mvnw.cmd clean
Remove-Item -Recurse -Force data/
```

---

## 🎓 Learning Resources

### Spring Boot Basics
- Official docs: https://spring.io/guides/gs/spring-boot/
- REST API tutorial: https://spring.io/guides/gs/rest-service/
- Data JPA: https://spring.io/guides/gs/accessing-data-jpa/

### Understanding Mastodon
- Mastodon API docs: https://docs.joinmastodon.org/api/
- ActivityPub spec: https://www.w3.org/TR/activitypub/
- Original Mastodon: https://github.com/mastodon/mastodon

---

## 🎯 Suggested Learning Path

### Week 1: Get Comfortable
- ✅ Run the application
- ✅ Explore the database
- ✅ Create a "Hello World" endpoint
- ✅ Read through existing controllers
- ✅ Understand the module structure

### Week 2: Simple Features
- Add more REST endpoints
- Create basic CRUD operations
- Learn JPA repositories
- Test with PowerShell/curl

### Week 3: Core Features
- Implement account creation
- Add authentication
- Create status (post) endpoints
- Build timeline functionality

### Week 4+: Advanced Topics
- ActivityPub implementation
- Federation between instances
- Media upload/processing
- Real-time streaming

---

## 🐛 Troubleshooting

### Application won't start?
```powershell
# Check if port 8080 is in use
netstat -ano | findstr :8080

# Change port in application.yml:
server:
  port: 8081
```

### Database issues?
```powershell
# Reset database (deletes all data!)
Remove-Item -Recurse -Force data/
# Restart app - database recreates automatically
```

### Build issues?
```powershell
# Clean rebuild
mvnw.cmd clean install
```

---

## 🤖 Using with Ollama

You're set up to use Ollama! Here's how:

### Start Development Session

**Terminal 1:**
```powershell
ollama serve
```

**Terminal 2:**
```powershell
claude
```

**Terminal 3:**
```powershell
cd mastodon-web
..\mvnw.cmd spring-boot:run
```

### Ask Claude to Help

With Claude Code + Ollama, you can:
- Ask for code explanations
- Generate new endpoints
- Debug issues
- Refactor code
- Write tests

**Example prompts:**
- "Add a new endpoint to get all accounts"
- "Explain how the Account entity works"
- "Create a service class for status management"
- "Add validation to the account creation endpoint"

---

## 📊 Your Development Environment Status

✅ **Build System:** Maven (optimized)  
✅ **Database:** H2 embedded (no Docker needed)  
✅ **AI Assistant:** Ollama with qwen-mastodon  
✅ **Java Version:** OpenJDK 25  
✅ **Framework:** Spring Boot 4.0.1  
✅ **Tests:** Configured (skipped by default for speed)  

**You're all set up for productive development!**

---

## 🎯 Your First Goal

**Suggested first milestone:**

Create a working "post a status" feature:
1. Create `StatusController.java`
2. Implement `POST /api/v1/statuses` endpoint
3. Save to database using `StatusRepository`
4. Return the created status as JSON
5. Test with PowerShell

**This teaches you:**
- Spring MVC controllers
- JPA repositories
- Request/response handling
- Database operations
- REST API design

---

## 🚀 Ready to Code!

**Start here:**
```powershell
# Make sure app is running
cd mastodon-web
..\mvnw.cmd spring-boot:run

# In another terminal, test it works
Invoke-RestMethod http://localhost:8080/actuator/health
```

**Then:**
1. Open your IDE
2. Explore existing code
3. Try creating your first endpoint
4. Ask Claude for help when stuck!

---

## 📝 Quick Reference

**Start app:**
```powershell
cd mastodon-web && ..\mvnw.cmd spring-boot:run
```

**Access points:**
- App: http://localhost:8080
- Database: http://localhost:8080/h2-console
- Health: http://localhost:8080/actuator/health

**Stop app:** `Ctrl+C`

**Reset database:** Delete `data/` folder

---

## 🎉 You're Ready!

You've successfully:
- ✅ Built the project
- ✅ Fixed all compilation errors  
- ✅ Set up Windows 11 development environment
- ✅ Configured Ollama integration
- ✅ Removed Docker dependency

**Now it's time to code!** 🚀

Start with small tasks, use Claude for help, and gradually build your way up to more complex features.

**Happy coding!** 💻

---

**Need help?** Ask Claude:
- "How do I create a new REST endpoint?"
- "Explain the Account entity to me"
- "Help me implement user authentication"
- "Show me how to add a database migration"
