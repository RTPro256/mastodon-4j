# Runtime & JVM Constraints

## Java
- Target runtime: Java 25
- No preview features
- No incubator modules
- Bytecode level must match runtime

## JVM Flags
- Remove illegal-access flags
- Avoid --add-opens unless strictly required
- Prefer framework-supported access

## Compatibility
- All third-party libraries must officially support Java 25
- If not, flag and stop before upgrading
