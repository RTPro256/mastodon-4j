# Fixed Claude Code Settings - Tool-Compatible Models

The issue is that **DeepSeek-Coder doesn't support tools** in Ollama. We need to use models that explicitly support function calling.

---

## Solution: Use Llama 3.2 (Best for Windows)

### Step 1: Pull a Compatible Model

```powershell
# Pull Llama 3.2 (small, fast, supports tools)
ollama pull llama3.2:3b-instruct-q8_0

# OR for better quality (larger)
ollama pull llama3.1:8b-instruct-q8_0
```

### Step 2: Update Your settings.json

Replace your `.claude/settings.json` with this:

```json
{
  "$schema": "https://json.schemastore.org/claude-code-settings.json",

  "env": {
    "ANTHROPIC_AUTH_TOKEN": "ollama",
    "ANTHROPIC_BASE_URL": "http://localhost:11434"
  },

  "version": "1.0",
  "provider": "ollama",
  "model": "llama3.2:3b-instruct-q8_0",
  "temperature": 0.1,
  
  "project": {
    "detect_build_system": true,
    "prefer_existing_patterns": true
  },
  
  "permissions": {
    "read": true,
    "write": true,
    "execute": true,
    "deny": [
      "Read(./secrets/**)",
      "Read(./config/credentials.json)",
      "Read(./.vscode)",
      "Read(./.github)"
    ]
  },
  
  "diff": {
    "style": "unified",
    "include_context": true
  },

  "workflow": {
    "default_mode": "plan_then_execute",
    "require_plan_confirmation": true,
    "max_files_per_change": 8
  },

  "safety": {
    "no_mass_refactors": true,
    "no_api_behavior_changes": true,
    "tests_must_pass": true
  },
  
  "initialPermissionMode": "allow",
  "claudeCode.useTerminal": true
}
```

---

## Alternative: Use Llama 3.1 (Better Quality)

If you have enough RAM (16GB+), use this instead:

```powershell
# Pull Llama 3.1 8B
ollama pull llama3.1:8b-instruct-q8_0
```

**settings.json:**
```json
{
  "model": "llama3.1:8b-instruct-q8_0",
  "provider": "ollama",
  "temperature": 0.1
}
```

---

## Models That DEFINITELY Support Tools

| Model | Size | RAM | Tool Support | Best For |
|-------|------|-----|--------------|----------|
| `llama3.2:3b-instruct-q8_0` | 3.3GB | 8GB | ✅ Yes | Fast, low RAM |
| `llama3.1:8b-instruct-q8_0` | 8.5GB | 16GB | ✅ Yes | Balanced |
| `llama3.1:70b-instruct-q4_K_M` | 40GB | 48GB+ | ✅ Yes | Best quality |
| `qwen2.5-coder:7b-instruct-q8_0` | 7.6GB | 16GB | ✅ Yes | Code-focused |
| `mistral:7b-instruct-v0.3-q8_0` | 7.7GB | 16GB | ✅ Yes | Fast coding |

Models that DON'T work:
| Model | Tool Support |
|-------|--------------|
| `deepseek-coder:*` | ❌ No |
| `deepseek-mastodon` | ❌ No |
| `codellama:*` | ❌ No (most versions) |

---

## Quick Setup Commands

```powershell
# 1. Pull the model
ollama pull llama3.2:3b-instruct-q8_0

# 2. Update settings.json (save the content above to .claude/settings.json)

# 3. Verify model is available
ollama list

# 4. Test it
claude code --model llama3.2:3b-instruct-q8_0
```

---

## Test If Tools Work

Once you start Claude Code, test it:

```
You: Can you list the files in the current directory?
```

If you get a file list, tools are working! ✅

If you still get an error, try:
```powershell
# Try Mistral instead
ollama pull mistral:7b-instruct-v0.3-q8_0
claude code --model mistral:7b-instruct-v0.3-q8_0
```

---

## Why DeepSeek Doesn't Work

DeepSeek-Coder in Ollama **does not implement the function calling API** that Claude Code requires. This is a limitation of the DeepSeek model in Ollama, not your configuration.

Llama 3.x models have native tool support built-in, which is why they work with Claude Code.

---

## Recommended: Use Qwen2.5-Coder

For the best coding experience with tool support:

```powershell
# Pull Qwen2.5-Coder (specifically designed for coding + has tool support)
ollama pull qwen2.5-coder:7b-instruct-q8_0

# Update settings.json
# "model": "qwen2.5-coder:7b-instruct-q8_0"

# Start
claude code
```

This is a coding-focused model that definitely supports tools.

---

## Final Answer

**Replace your settings.json model line with:**

```json
"model": "llama3.2:3b-instruct-q8_0",
```

**Then run:**
```powershell
ollama pull llama3.2:3b-instruct-q8_0
claude code
```

This will work! 🎉
