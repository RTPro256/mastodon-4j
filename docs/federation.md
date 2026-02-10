# Federation & ActivityPub Constraints

This project participates in a federated ecosystem.

Claude must NOT:
- Change JSON serialization formats
- Change field ordering or defaults
- Modify HTTP signatures
- Change actor, inbox, or outbox behavior
- Modify timestamp formats or precision

High-risk areas:
- Jackson ObjectMapper configuration
- HTTP client defaults
- Security filters and request wrappers
- Streaming / SSE endpoints

If a framework upgrade affects these areas:
- Stop
- Explain the impact
- Propose mitigation

FEDERATION SAFETY RULES (CRITICAL):

DO NOT:
- Change JSON field names
- Change default values
- Change Jackson configuration
- Change HTTP signature behavior
- Change date/time precision

ASSUME:
- Remote servers depend on current behavior

STOP immediately if a change affects serialization or headers.
