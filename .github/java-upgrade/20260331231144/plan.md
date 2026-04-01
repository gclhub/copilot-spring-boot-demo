# Upgrade Plan: ecommerce-monolith (20260331231144)

- **Generated**: 2026-03-31 23:11:44
- **HEAD Branch**: intro
- **HEAD Commit ID**: 4a2a3ea

## Available Tools

**JDKs**
- JDK 17.0.18: /Library/Java/JavaVirtualMachines/temurin-17.jdk/Contents/Home (current project JDK, used by step 2)
- JDK 25.0.2: /opt/homebrew/Cellar/openjdk/25.0.2/libexec/openjdk.jdk/Contents/Home (target JDK, used by steps 4–5)

**Build Tools**
- Maven 3.9.14: /opt/homebrew/Cellar/maven/3.9.14/bin/mvn (no Maven wrapper present in project)
  - Note: Maven 3.9.14 is observed running on JDK 25.0.2 on this machine and will be used throughout. The strict Maven 4.0+ guideline for Java 25 is acknowledged; if compilation issues arise during execution, Maven may need to be upgraded to 4.0+.

## Guidelines

> Note: You can add any specific guidelines or constraints for the upgrade process here if needed, bullet points are preferred.

## Options

- Working branch: appmod/java-upgrade-20260331231144
- Run tests before and after the upgrade: true

## Upgrade Goals

- Upgrade Java from 17 to 25 (LTS)
- Upgrade Spring Boot from 3.2.0 to 3.5.x (required for official Java 25 support)

## Technology Stack

| Technology/Dependency | Current | Min Compatible | Why Incompatible |
| --------------------- | ------- | -------------- | ---------------- |
| Java | 17 | 25 | User-requested upgrade to Java 25 LTS |
| Spring Boot | 3.2.0 | 3.3.x | Spring Boot 3.2.x pre-dates Java 25; upgrading to 3.5.x ensures official Java 25 support and compatible transitive dependencies |
| Spring Framework | 6.1.x (via SB 3.2) | 6.2.x | Auto-upgraded via Spring Boot 3.5.x BOM |
| Hibernate | 6.4.x (via SB 3.2) | 6.6.x | Auto-upgraded via Spring Boot 3.5.x BOM |
| Lombok | 1.18.30 | 1.18.36 | Lombok 1.18.30 does not support Java 25 annotation processing; 1.18.36+ is required |
| maven-compiler-plugin | 3.13.0 | 3.13.0 | Already supports Java 25 via `--release 25`; only configuration update needed |
| Maven | 3.9.14 | 3.9.14 | Already running on JDK 25.0.2 on this machine; functional for this upgrade |

## Derived Upgrades

- Upgrade Spring Boot from 3.2.0 to 3.5.x (brings official Java 25 support; auto-upgrades Spring Framework 6.2.x, Hibernate 6.6.x, and other managed transitive dependencies)
- Upgrade Lombok from 1.18.30 to 1.18.36 (Java 25 requires updated annotation processor in Lombok; this is the highest-risk change)
- Update `java.version`, `maven.compiler.source`, `maven.compiler.target` properties from `17` to `25`
- Update `<release>17</release>` in maven-compiler-plugin configuration block to `<release>25</release>`

## Upgrade Steps

- **Step 1: Setup Environment**
  - **Rationale**: Confirm all required JDKs are available. No installs needed — JDK 17 (baseline) and JDK 25 (target) are already present on this machine.
  - **Changes to Make**:
    - [ ] Verify JDK 17 at `/Library/Java/JavaVirtualMachines/temurin-17.jdk/Contents/Home`
    - [ ] Verify JDK 25 at `/opt/homebrew/Cellar/openjdk/25.0.2/libexec/openjdk.jdk/Contents/Home`
    - [ ] Verify Maven 3.9.14 is executable and can resolve dependencies
  - **Verification**:
    - Command: `JAVA_HOME=/Library/Java/JavaVirtualMachines/temurin-17.jdk/Contents/Home java -version && /opt/homebrew/Cellar/openjdk/25.0.2/libexec/openjdk.jdk/Contents/Home/bin/java -version`
    - Expected: Both JDK versions reported successfully

---

- **Step 2: Setup Baseline**
  - **Rationale**: Capture pre-upgrade compile and test results with JDK 17 / Spring Boot 3.2.0 to establish acceptance criteria.
  - **Changes to Make**:
    - [ ] Run full compile (main + test sources) with JDK 17
    - [ ] Run full test suite with JDK 17
    - [ ] Record compile result and test pass/fail counts in progress.md
  - **Verification**:
    - Command: `JAVA_HOME=/Library/Java/JavaVirtualMachines/temurin-17.jdk/Contents/Home mvn clean test-compile -q && JAVA_HOME=/Library/Java/JavaVirtualMachines/temurin-17.jdk/Contents/Home mvn clean test`
    - JDK: `/Library/Java/JavaVirtualMachines/temurin-17.jdk/Contents/Home`
    - Expected: Compilation SUCCESS; baseline test results documented (prior run: 79 tests, 0 failures)

---

- **Step 3: Upgrade Spring Boot 3.2.0 to 3.5.x**
  - **Rationale**: Spring Boot 3.5.x provides official Java 25 support and pulls in compatible Spring Framework 6.2.x, Hibernate 6.6.x via its BOM. Performed while still on JDK 17 to isolate Spring upgrade issues from JDK issues.
  - **Changes to Make**:
    - [ ] Update `spring-boot-starter-parent` version from `3.2.0` to `3.5.0` (or latest 3.5.x patch) in `pom.xml`
    - [ ] Fix any compilation errors introduced by Spring Boot 3.5.x API changes
  - **Verification**:
    - Command: `JAVA_HOME=/Library/Java/JavaVirtualMachines/temurin-17.jdk/Contents/Home mvn clean test-compile -q`
    - JDK: `/Library/Java/JavaVirtualMachines/temurin-17.jdk/Contents/Home`
    - Expected: Compilation SUCCESS with Java 17 and Spring Boot 3.5.x (test failures acceptable in this step)

---

- **Step 4: Upgrade Java Target to 25 and Lombok to 1.18.36**
  - **Rationale**: Update the compiler target to Java 25 and simultaneously upgrade Lombok to 1.18.36+, the minimum version supporting Java 25 annotation processing. These changes are tightly coupled — the compiler cannot successfully process Lombok annotations on Java 25 without the Lombok upgrade.
  - **Changes to Make**:
    - [ ] Update `<java.version>`, `<maven.compiler.source>`, `<maven.compiler.target>` from `17` to `25` in `pom.xml` `<properties>`
    - [ ] Update `<release>17</release>` to `<release>25</release>` in `maven-compiler-plugin` `<configuration>`
    - [ ] Update `<lombok.version>1.18.30</lombok.version>` to `1.18.36` (or latest stable) in `pom.xml` `<properties>`
    - [ ] Fix any compilation errors from Java 25 language or API changes
  - **Verification**:
    - Command: `JAVA_HOME=/opt/homebrew/Cellar/openjdk/25.0.2/libexec/openjdk.jdk/Contents/Home mvn clean test-compile -q`
    - JDK: `/opt/homebrew/Cellar/openjdk/25.0.2/libexec/openjdk.jdk/Contents/Home`
    - Expected: Compilation SUCCESS (both main and test sources) with JDK 25

---

- **Step 5: Final Validation**
  - **Rationale**: Verify all upgrade goals are met, the project compiles cleanly on JDK 25, and all tests pass at or above the baseline (79/79).
  - **Changes to Make**:
    - [ ] Verify `java.version`, `maven.compiler.source`, `maven.compiler.target`, and `<release>` are all `25` in `pom.xml`
    - [ ] Verify `spring-boot-starter-parent` is `3.5.x`
    - [ ] Verify `lombok.version` is `1.18.36` or newer
    - [ ] Resolve ALL TODOs and temporary workarounds from previous steps
    - [ ] Run full test suite; fix ALL failures (iterative fix loop until 100% pass)
  - **Verification**:
    - Command: `JAVA_HOME=/opt/homebrew/Cellar/openjdk/25.0.2/libexec/openjdk.jdk/Contents/Home mvn clean test`
    - JDK: `/opt/homebrew/Cellar/openjdk/25.0.2/libexec/openjdk.jdk/Contents/Home`
    - Expected: Compilation SUCCESS + 79/79 tests pass (≥ baseline)

## Key Challenges

- **Lombok Java 25 Annotation Processing**
  - **Challenge**: Lombok 1.18.30 uses javac internal APIs that changed in Java 25. Without upgrading Lombok, the annotation processor will fail to generate getters/setters/constructors for model classes (`Customer`, `Product`, `Order`, `OrderItem`), breaking compilation entirely.
  - **Strategy**: Upgrade Lombok to 1.18.36 in Step 4 (same step as Java target change). Verify that all `@Data`, `@Builder`, `@Getter`, `@Setter` annotated classes compile correctly after the upgrade.

- **Spring Boot 3.2 to 3.5 Minor Breaking Changes**
  - **Challenge**: Spring Boot 3.3–3.5 introduced deprecation removals and behavior changes (e.g., auto-configuration restructuring, property binding changes). The project is a basic CRUD application with minimal advanced Spring usage, so risk is low.
  - **Strategy**: Compile with JDK 17 after the Spring Boot upgrade (Step 3) to isolate any API-level errors before introducing JDK 25. Fix compile errors before proceeding to Step 4.

- **Maven 3.9.14 with Java 25**
  - **Challenge**: Strict compatibility guidelines recommend Maven 4.0+ for Java 25. Maven 3.9.14 is already confirmed running on Java 25.0.2 on this machine, but specific Maven plugins may exercise JDK internal APIs removed in Java 25.
  - **Strategy**: Proceed with Maven 3.9.14. If a Maven plugin fails during execution, identify and upgrade the specific plugin, or upgrade Maven to 4.0+ as a corrective action within the affected step.

## Plan Review

- All upgrade goals (Java 17 → 25, Spring Boot 3.2.0 → 3.5.x) are addressed with 5 ordered, incremental steps.
- No intermediate JDK is required — JDK 25 is available on this machine; JDK 17 is used only for baseline (Step 2) and Spring Boot upgrade validation (Step 3).
- No javax → jakarta migration is needed (project is already on Spring Boot 3.x / Jakarta EE 9+).
- The project is a straightforward Spring Boot CRUD application (17 source files, 79 tests across 6 test classes) with no complex third-party integrations. Upgrade risk is low.
- Lombok version upgrade is the highest-risk dependency change and is addressed explicitly in Step 4.
- All required JDKs are present; Step 1 requires no installs.
