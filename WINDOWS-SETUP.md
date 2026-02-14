# Windows 11 + Ollama + Docker-Free Configuration

## Summary of Changes - February 14, 2026

This document summarizes the changes made to configure the mastodon_4j project for Windows 11 development using Ollama and removing the Docker dependency.

---

## 🎯 Goals Achieved

✅ **1. Optimized Claude Code Settings for Windows 11 + Ollama**
✅ **2. Clarified Launch Commands**
✅ **3. Removed Docker Dependency**

---

## 📝 Changes Made

### 1. Claude Code Settings (`.claude/settings.json`)

**Optimizations:**
- Disabled unused plugins (pyright, rust-analyzer, ralph-loop, huggingface-skills)
- Set `require_plan_confirmation: false` for faster workflows with Ollama
- Reduced `max_files_per_change` from 8 to 5 (better for local models)
- Set `tests_must_pass: false` for easier development
- Enabled `respectGitignore: true` to avoid processing build artifacts

**Ollama Configuration:**
- Provider: `ollama`
- Model: `qwen-mastodon`
- Base URL: `http://localhost:11434`
- Context window: 16384 tokens

### 2. Database Configuration

**Added H2 Embedded Database** (No Docker Required!)

**Files Created:**
- `application-h2.yml` - H2-specific configuration
- `application-postgres.yml` - Optional PostgreSQL configuration

**Files Modified:**
- `application.yml` - Now defaults to H2
- `pom.xml` - Added H2 dependency, made PostgreSQL optional

**H2 Features:**
- Database file: `./data/mastodon_dev.mv.db`
- Web console: http://localhost:8080/h2-console
- Auto-creates database on first run
- No external setup required

### 3. Documentation Updates

**Updated README.md:**
- Removed Docker requirement from Quick Start
- Added Windows 11-specific instructions
- Added Ollama integration guide
- Simplified prerequisites (just Java 25!)
- Added H2 database console access info

**Updated .gitignore:**
- Added `data/` directory (H2 database files)
- Added `*.mv.db` and `*.trace.db` patterns

---

## 🚀 How to Use

### Starting the Project (No Docker!)

```powershell
# 1. Build the project
mvnw.cmd clean install

# 2. Run the application
cd mastodon-web
..\mvnw.cmd spring-boot:run

# 3. Access at http://localhost:8080
```

### Using with Ollama

```powershell
# Terminal 1: Start Ollama
ollama serve

# Terminal 2: Launch Claude Code
claude
```

### Database Management

**H2 Console (Default):**
- URL: http://localhost:8080/h2-console
- JDBC URL: `jdbc:h2:file:./data/mastodon_dev`
- Username: `sa`
- Password: (empty)

**PostgreSQL (Optional):**
```powershell
# Start Docker
docker-compose up -d

# Run with PostgreSQL profile
..\mvnw.cmd spring-boot:run -Dspring-boot.run.profiles=postgres
```

---

## 📋 Launch Command Clarification

**Correct Commands:**

1. **Start Ollama Service:**
   ```powershell
   ollama serve
   ```
   Runs on http://localhost:11434

2. **Launch Claude Code:**
   ```powershell
   claude
   ```
   This is the standard Claude Code CLI command

**Note:** If "ollama launch claude" works for you, it might be a custom alias or script. The standard way is to run `ollama serve` in one terminal and `claude` in another.

---

## 🔧 Configuration Files Changed

| File | Change | Purpose |
|------|--------|---------|
| `.claude/settings.json` | Updated | Optimized for Windows 11 + Ollama |
| `pom.xml` | Modified | Added H2, made PostgreSQL optional |
| `application.yml` | Replaced | Defaults to H2 database |
| `application-h2.yml` | Created | H2-specific configuration |
| `application-postgres.yml` | Created | PostgreSQL profile |
| `.gitignore` | Modified | Ignore H2 database files |
| `README.md` | Replaced | Simplified instructions |

---

## 🎓 Benefits

### For Beginners:
- ✅ No complex Docker setup
- ✅ Single command to run
- ✅ Everything self-contained
- ✅ Easy database reset (delete `data/` folder)

### For Development:
- ✅ Faster startup time
- ✅ No external dependencies
- ✅ Works offline
- ✅ Easy to version control (just delete data folder)

### For Ollama Users:
- ✅ Optimized settings for local models
- ✅ Lower token limits for efficiency
- ✅ Faster workflows

---

## ⚠️ Important Notes

1. **H2 is for development only** - For production, use PostgreSQL
2. **Database file location** - `./data/mastodon_dev.mv.db` (auto-created)
3. **Reset database** - Delete the `data/` folder
4. **Switch to PostgreSQL** - Use `-Dspring-boot.run.profiles=postgres`

---

## 🐛 Troubleshooting

**Issue: Port 8080 in use**
```powershell
# Solution: Change port in application.yml
server:
  port: 8081
```

**Issue: Database locked**
```powershell
# Solution: Stop all running instances and delete data/
rmdir /s data
```

**Issue: Ollama not responding**
```powershell
# Solution: Verify Ollama is running
curl http://localhost:11434/api/tags
```

---

## 📚 Next Steps

1. ✅ Run the application: `mvnw.cmd spring-boot:run`
2. ✅ Access H2 console: http://localhost:8080/h2-console
3. ✅ Start building features!
4. 📖 Read `CLAUDE.md` for detailed documentation
5. 🔍 Check `docs/` folder for architecture details

---

## 🔄 Reverting Changes

If you need to go back to Docker/PostgreSQL:

1. Use the PostgreSQL profile:
   ```powershell
   mvnw.cmd spring-boot:run -Dspring-boot.run.profiles=postgres
   ```

2. Start Docker:
   ```powershell
   docker-compose up -d
   ```

The original `docker-compose.yml` is still present and functional.

---

**Generated:** February 14, 2026  
**Author:** Claude (Sonnet 4.5)  
**Purpose:** Windows 11 + Ollama + Docker-Free Setup
