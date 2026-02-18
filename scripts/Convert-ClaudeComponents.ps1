# Convert-ClaudeComponents.ps1
# Converts and upgrades all Claude Code components to modern format

param(
    [switch]$DryRun,
    [switch]$Backup = $true,
    [switch]$Force
)

$ErrorActionPreference = "Stop"

Write-Host "========================================" -ForegroundColor Cyan
Write-Host "Claude Code Components Upgrade Script" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

# Define paths
$projectRoot = "C:\Users\RyanT\Documents\code\ClaudeCode\mastodon_4j"
$claudeDir = Join-Path $projectRoot ".claude"

# Check if .claude directory exists
if (-not (Test-Path $claudeDir)) {
    Write-Host "❌ .claude directory not found at: $claudeDir" -ForegroundColor Red
    exit 1
}

Write-Host "✓ Found .claude directory" -ForegroundColor Green
Write-Host ""

# 1. BACKUP
if ($Backup -and -not $DryRun) {
    Write-Host "Step 1: Creating backup..." -ForegroundColor Yellow
    $timestamp = Get-Date -Format "yyyyMMdd-HHmmss"
    $backupPath = Join-Path $projectRoot ".claude-backup-$timestamp"
    
    Copy-Item -Path $claudeDir -Destination $backupPath -Recurse
    Write-Host "✓ Backup created: $backupPath" -ForegroundColor Green
    Write-Host ""
}

# 2. CREATE NEW DIRECTORY STRUCTURE
Write-Host "Step 2: Creating new directory structure..." -ForegroundColor Yellow

$newDirs = @(
    # Skills
    "skills/mastodon-migration/references",
    "skills/mastodon-migration/examples",
    "skills/mastodon-migration/scripts",
    "skills/activitypub-federation/references",
    "skills/activitypub-federation/examples",
    "skills/spring-boot-patterns/references",
    "skills/spring-boot-patterns/examples",
    "skills/java-patterns/references",
    "skills/java-patterns/examples",
    "skills/testing-strategies/references",
    "skills/testing-strategies/examples",
    
    # Templates
    "templates/spring",
    "templates/tests",
    "templates/patterns/builder-pattern",
    "templates/patterns/factory-pattern",
    "templates/patterns/strategy-pattern",
    "templates/patterns/repository-pattern",
    "templates/activitypub",
    "templates/react",
    
    # Teams
    "teams/workflows",
    "teams/templates",
    "teams/playbooks",
    
    # Agents (keep existing but add new structure)
    "agents/prompts",
    "agents/coordination",
    
    # New directories
    "metrics",
    "reports",
    "documentation"
)

foreach ($dir in $newDirs) {
    $fullPath = Join-Path $claudeDir $dir
    if (-not (Test-Path $fullPath)) {
        if (-not $DryRun) {
            New-Item -ItemType Directory -Path $fullPath -Force | Out-Null
        }
        Write-Host "  Created: $dir" -ForegroundColor Gray
    }
}

Write-Host "✓ Directory structure created" -ForegroundColor Green
Write-Host ""

# 3. CONVERT AGENTS JSON TO MD
Write-Host "Step 3: Converting agents from JSON to MD format..." -ForegroundColor Yellow

$agentFiles = Get-ChildItem -Path (Join-Path $claudeDir "agents") -Filter "*.json"
$agentsConverted = 0

foreach ($agentFile in $agentFiles) {
    $agentName = $agentFile.BaseName
    $mdPath = Join-Path $agentFile.DirectoryName "$agentName.md"
    
    if (Test-Path $mdPath) {
        Write-Host "  ⚠ Already exists: $agentName.md (skipping)" -ForegroundColor Yellow
        continue
    }
    
    try {
        $jsonContent = Get-Content $agentFile.FullName -Raw | ConvertFrom-Json
        
        $mdContent = @"
---
name: $($jsonContent.name)
description: $($jsonContent.description)
version: $($jsonContent.version)
model: $($jsonContent.model)
temperature: $($jsonContent.temperature)
---

# $($jsonContent.name)

$($jsonContent.description)

## Configuration

**Model:** ``$($jsonContent.model)``  
**Temperature:** $($jsonContent.temperature)  
**Version:** $($jsonContent.version)

## Capabilities

$($jsonContent.capabilities -join "`n- " | ForEach-Object { "- $_" })

## Workflow

**Mode:** $($jsonContent.workflow.mode)  
**Require Confirmation:** $($jsonContent.workflow.require_confirmation)  
**Max Files Per Change:** $($jsonContent.workflow.max_files_per_change)

## Safety Checks

$($jsonContent.safety_checks -join "`n- " | ForEach-Object { "- $_" })

## Context Files

$($jsonContent.context_files -join "`n- " | ForEach-Object { "- $_" })

---

*Note: This agent was automatically converted from JSON format.*
*Last updated: $(Get-Date -Format "yyyy-MM-dd")*
"@
        
        if (-not $DryRun) {
            $mdContent | Out-File -FilePath $mdPath -Encoding UTF8
        }
        
        Write-Host "  ✓ Converted: $agentName.json → $agentName.md" -ForegroundColor Green
        $agentsConverted++
        
    } catch {
        Write-Host "  ✗ Failed to convert: $agentName.json" -ForegroundColor Red
        Write-Host "    Error: $_" -ForegroundColor Red
    }
}

Write-Host "✓ Converted $agentsConverted agents" -ForegroundColor Green
Write-Host ""

# 4. CONVERT SKILLS JSON TO SKILL.MD
Write-Host "Step 4: Converting skills from JSON to SKILL.md format..." -ForegroundColor Yellow

$skillFiles = Get-ChildItem -Path (Join-Path $claudeDir "skills") -Filter "*.json"
$skillsConverted = 0

foreach ($skillFile in $skillFiles) {
    $skillName = $skillFile.BaseName
    $skillDir = Join-Path $skillFile.DirectoryName $skillName
    $skillMdPath = Join-Path $skillDir "SKILL.md"
    
    if (Test-Path $skillMdPath) {
        Write-Host "  ⚠ Already exists: $skillName/SKILL.md (skipping)" -ForegroundColor Yellow
        continue
    }
    
    try {
        $jsonContent = Get-Content $skillFile.FullName -Raw | ConvertFrom-Json
        
        # Create skill directory
        if (-not (Test-Path $skillDir)) {
            if (-not $DryRun) {
                New-Item -ItemType Directory -Path $skillDir -Force | Out-Null
            }
        }
        
        # Create SKILL.md
        $skillContent = @"
---
name: $($jsonContent.name)
version: $($jsonContent.version)
description: $($jsonContent.description)
invoke: auto
triggers:
  - migration tasks
  - code generation
  - architecture decisions
---

# $($skillName.ToUpper() -replace '-', ' ')

$($jsonContent.description)

## When to Use This Skill

Claude should use this skill when:
- Working on Mastodon migration tasks
- Implementing Java/Spring Boot features
- Making architecture decisions
- Generating code following best practices

## Knowledge Areas

This skill provides expertise in:

$($jsonContent.knowledge_areas.PSObject.Properties | ForEach-Object {
    "### $($_.Name)`n`nLevel: $($_.Value.level)`n"
})

## Best Practices

$($jsonContent.best_practices.PSObject.Properties | ForEach-Object {
    "### $($_.Name)`n`n" + ($_.Value -join "`n- " | ForEach-Object { "- $_" }) + "`n"
})

## Common Patterns

$($jsonContent.common_patterns.PSObject.Properties | ForEach-Object {
    "### $($_.Name)`n`n**Description:** $($_.Value.description)`n**Example:** $($_.Value.example)`n"
})

## For More Information

See additional documentation in:
- ``references/`` - Detailed specifications and guides
- ``examples/`` - Code examples and templates
- ``scripts/`` - Helper scripts and tools

---

*Last Updated: $(Get-Date -Format "yyyy-MM-dd")*
"@
        
        if (-not $DryRun) {
            $skillContent | Out-File -FilePath $skillMdPath -Encoding UTF8
            
            # Create placeholder files
            $referencesDir = Join-Path $skillDir "references"
            $examplesDir = Join-Path $skillDir "examples"
            
            if (-not (Test-Path $referencesDir)) {
                New-Item -ItemType Directory -Path $referencesDir -Force | Out-Null
            }
            if (-not (Test-Path $examplesDir)) {
                New-Item -ItemType Directory -Path $examplesDir -Force | Out-Null
            }
            
            "# References`n`nDetailed documentation extracted from original skill." | 
                Out-File -FilePath (Join-Path $referencesDir "overview.md") -Encoding UTF8
        }
        
        Write-Host "  ✓ Converted: $skillName.json → $skillName/SKILL.md" -ForegroundColor Green
        $skillsConverted++
        
    } catch {
        Write-Host "  ✗ Failed to convert: $skillName.json" -ForegroundColor Red
        Write-Host "    Error: $_" -ForegroundColor Red
    }
}

Write-Host "✓ Converted $skillsConverted skills" -ForegroundColor Green
Write-Host ""

# 5. CREATE COORDINATION PROTOCOL
Write-Host "Step 5: Creating agent coordination protocol..." -ForegroundColor Yellow

$coordinationPath = Join-Path $claudeDir "agents/coordination/handoff-protocol.md"

$coordinationContent = @"
# Agent Handoff Protocol

## Purpose

This protocol ensures smooth transitions between agents during multi-agent workflows.

## Handoff Template

When one agent completes its task and hands off to another:

### 1. Context Summary
**Completed by:** [Agent Name]  
**Completed:** [Date/Time]  
**Task:** [What was accomplished]

### 2. Key Decisions Made
- Decision 1
- Decision 2
- Decision 3

### 3. Files Created/Modified
- \`path/to/file1.java\` - [Description]
- \`path/to/file2.java\` - [Description]

### 4. Next Steps Required
1. [Next action]
2. [Next action]
3. [Next action]

### 5. Important Context
- [Key information for next agent]
- [Constraints or limitations]
- [Dependencies or blockers]

### 6. Questions for Next Agent
- [ ] Question 1
- [ ] Question 2

### 7. Acceptance Criteria
For the next agent to consider the handoff successful:
- [ ] Criteria 1
- [ ] Criteria 2

## Example Handoff

\`\`\`markdown
### Context Summary
**Completed by:** migration-agent  
**Completed:** 2026-02-14 14:30  
**Task:** Planned ActivityPub federation implementation

### Key Decisions Made
- Use Jackson for JSON-LD serialization
- Implement HTTP Signatures per Mastodon spec
- Cache remote actors for 24 hours

### Files Created/Modified
- \`docs/federation-plan.md\` - Complete implementation plan
- \`docs/activitypub-models.md\` - Required model classes
- \`.claude/tasks/federation-implementation.md\` - Task breakdown

### Next Steps Required
1. Implement ActivityPub models (federation-agent)
2. Set up HTTP signature generation
3. Create WebFinger endpoint
4. Implement inbox processing

### Important Context
- Must maintain compatibility with Mastodon 4.x
- Use existing authentication system
- Performance target: < 100ms per activity

### Questions for Next Agent
- [ ] Should we implement LD Signatures or just HTTP Signatures?
- [ ] What's the caching strategy for remote actor keys?

### Acceptance Criteria
- [ ] All ActivityPub models created
- [ ] HTTP signature generation working
- [ ] Inbox can process basic activities
- [ ] Tests pass for protocol compliance
\`\`\`

---

*Created: $(Get-Date -Format "yyyy-MM-dd")*
"@

if (-not $DryRun) {
    $coordinationContent | Out-File -FilePath $coordinationPath -Encoding UTF8
}

Write-Host "✓ Created coordination protocol" -ForegroundColor Green
Write-Host ""

# 6. SUMMARY
Write-Host "========================================" -ForegroundColor Cyan
Write-Host "Conversion Summary" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

if ($DryRun) {
    Write-Host "DRY RUN - No changes made" -ForegroundColor Yellow
    Write-Host ""
}

Write-Host "What was done:" -ForegroundColor White
Write-Host "  • Created new directory structure" -ForegroundColor Gray
Write-Host "  • Converted $agentsConverted agents to .md format" -ForegroundColor Gray
Write-Host "  • Converted $skillsConverted skills to SKILL.md format" -ForegroundColor Gray
Write-Host "  • Created agent coordination protocol" -ForegroundColor Gray
Write-Host ""

Write-Host "Next steps:" -ForegroundColor White
Write-Host "  1. Review converted files" -ForegroundColor Gray
Write-Host "  2. Populate template directories" -ForegroundColor Gray
Write-Host "  3. Create team playbooks" -ForegroundColor Gray
Write-Host "  4. Test with a simple feature" -ForegroundColor Gray
Write-Host ""

if ($Backup) {
    Write-Host "Backup location: $backupPath" -ForegroundColor Cyan
}

Write-Host ""
Write-Host "Conversion complete! 🚀" -ForegroundColor Green
