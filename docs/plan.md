# Execution Plan

This file is the official entrypoint for executing the Mastodon Java migration plan.

## Current Scope
- Full web UI parity with upstream Mastodon.
- Postgres-only architecture (no Redis, no Elasticsearch).
- API compatibility target: Mastodon v4.5.6 (released 2026-02-03).

## Primary References
- `docs/milestones.md` (phased plan and checklists)
- `docs/ui-contracts.md` (UI and API data contracts)
- `docs/guardrails.md` and `docs/federation.md` (constraints)
- `docs/project.md` and `README.md` (scope and setup)

## Execution Prompt (use with `qwen-mastodon`)
```text
You are the project AI for mastodon_4j. Start with Milestone 0 in docs/milestones.md.

Requirements:
- Full UI parity with upstream Mastodon.
- Postgres-only architecture (no Redis, no Elasticsearch).
- API compatibility target: Mastodon v4.5.6.
- Follow guardrails in docs/guardrails.md and federation constraints in docs/federation.md.
- Use docs/ui-contracts.md to align UI and API payloads.

Task:
1) Review docs/project.md, README.md, docs/milestones.md, docs/ui-contracts.md.
2) Propose a step-by-step implementation plan for Milestone 0.
3) Implement the first concrete change: create `mastodon-ui` module scaffolding and wire it into `pom.xml` and `mastodon-web` build pipeline.
4) Keep each step buildable, and do not introduce new dependencies without approval.

Output:
- Short plan
- List of files to change
- Then implement the first change
```
