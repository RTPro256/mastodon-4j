# Project Overview

This repository is a Java implementation of the Mastodon federated social network with full web UI parity.

Primary goals:
- Full web UI parity with upstream Mastodon
- API compatibility with Mastodon v4.5.6 (released 2026-02-03)
- Java 25 and Spring Boot 4.x baseline
- Postgres-only architecture (no Redis, no Elasticsearch)
- Preserve federation behavior and API contracts

Non-goals:
- Feature development beyond upstream parity
- Introducing Redis or Elasticsearch
- Breaking API contracts or changing serialization formats
