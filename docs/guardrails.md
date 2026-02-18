# Guardrails

Claude must NOT:
- Perform repo-wide automated refactors without a plan
- Replace libraries without explicit justification
- Introduce preview Java features
- Change concurrency models
- Modify serialization formats

Claude MUST:
- Explain breaking changes before applying them
- Cite Spring or JDK migration rationale when possible
- Keep each step buildable

Additional Federation Guardrails:
- Preserve HTTP headers exactly where required
- Do not change TLS, cipher, or signature defaults
- Do not upgrade crypto providers unless forced
