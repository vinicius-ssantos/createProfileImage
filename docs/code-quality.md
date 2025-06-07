# Code Quality Guidelines

This document outlines the code quality tools and practices used in the create_ia_profiles project.

## Automated Code Quality Checks

The project uses several automated code quality tools that run as part of the build process. These tools help maintain code quality, identify potential bugs, and ensure consistent coding standards.

### Running Code Quality Checks

All code quality checks are integrated into the Maven build lifecycle and will run automatically during the `verify` phase:

```bash
mvn verify
```

To run individual checks:

```bash
# Run just SpotBugs
mvn spotbugs:check

# Run just PMD
mvn pmd:check

# Run just Checkstyle
mvn checkstyle:check

# Run just Spotless
mvn spotless:check

# Run just JaCoCo coverage report
mvn jacoco:report

# Run SonarQube analysis
mvn verify sonar:sonar -Psonar
```

To automatically fix formatting issues with Spotless:

```bash
mvn spotless:apply
```

## Code Quality Tools

### 1. SpotBugs

[SpotBugs](https://spotbugs.github.io/) is a static analysis tool that looks for potential bugs in Java code.

- **Configuration**: `spotbugs-maven-plugin` in pom.xml
- **Exclusion file**: `spotbugs-exclude.xml` in the project root
- **Reports**: Generated in `target/spotbugsXml.xml`

Common issues detected:
- Null pointer dereferences
- Infinite recursive loops
- Bad uses of the Java libraries
- Deadlocks and other threading issues

### 2. PMD

[PMD](https://pmd.github.io/) is a source code analyzer that finds common programming flaws.

- **Configuration**: `maven-pmd-plugin` in pom.xml
- **Rulesets**: Best practices and error-prone rules
- **Reports**: Generated in `target/pmd.xml`

Common issues detected:
- Unused variables, parameters, and private methods
- Empty catch blocks
- Unnecessary object creation
- Complex code that needs refactoring

### 3. Checkstyle

[Checkstyle](https://checkstyle.org/) is a development tool to help programmers write Java code that adheres to a coding standard.

- **Configuration**: `maven-checkstyle-plugin` in pom.xml using a custom configuration file (`checkstyle.xml`)
- **Reports**: Generated in `target/checkstyle-result.xml`

Common issues detected:
- Code formatting issues
- Naming conventions
- Javadoc problems
- Code structure issues

The custom Checkstyle configuration is based on Google's Java Style Guide but with some modifications:
- 4-space indentation (instead of 2)
- 120 character line length (instead of 100)
- Custom Javadoc requirements

### 4. JaCoCo

[JaCoCo](https://www.jacoco.org/jacoco/) is a code coverage library for Java.

- **Configuration**: `jacoco-maven-plugin` in pom.xml
- **Reports**: Generated in `target/site/jacoco/index.html`
- **Minimum coverage**: 50% instruction coverage

### 5. Maven Enforcer

The [Maven Enforcer Plugin](https://maven.apache.org/enforcer/maven-enforcer-plugin/) provides a way to enforce certain conditions in your Maven projects.

- **Configuration**: `maven-enforcer-plugin` in pom.xml
- **Rules enforced**:
  - Maven version >= 3.8.0
  - Java version matching project's java.version property
  - No duplicate dependencies
  - Dependency convergence (consistent versions)

### 6. Spotless

[Spotless](https://github.com/diffplug/spotless) is a code formatter that helps maintain consistent code style.

- **Configuration**: `spotless-maven-plugin` in pom.xml
- **Features**:
  - Formats Java code using Google Java Format
  - Organizes imports
  - Removes unused imports
  - Formats XML files

To check for formatting issues:
```bash
mvn spotless:check
```

To automatically fix formatting issues:
```bash
mvn spotless:apply
```

### 7. SonarQube

[SonarQube](https://www.sonarqube.org/) is a platform for continuous inspection of code quality.

- **Configuration**: SonarQube profile in pom.xml
- **Integration**: SonarCloud for cloud-based analysis
- **Features**:
  - Code quality analysis
  - Security vulnerability detection
  - Technical debt tracking
  - Code coverage visualization
  - Duplication detection

To run a SonarQube analysis:
```bash
mvn verify sonar:sonar -Psonar
```

This requires the following environment variables:
- `GITHUB_TOKEN` - GitHub access token
- `SONAR_TOKEN` - SonarCloud access token

## Interpreting Results

### Understanding Error Messages

Each tool produces different types of error messages:

- **SpotBugs**: Reports include bug patterns (e.g., NP_NULL_ON_SOME_PATH) with descriptions of the potential issue.
- **PMD**: Reports include rule violations with references to the specific rule being violated.
- **Checkstyle**: Reports include style violations with line and column numbers.
- **JaCoCo**: Reports include coverage percentages for different coverage metrics (instructions, branches, etc.).

### Fixing Issues

1. **Review the error message** to understand what's being reported
2. **Locate the affected code** using the file and line information
3. **Understand the rule** that's being violated (consult tool documentation if needed)
4. **Fix the issue** according to the recommended practice
5. **Re-run the check** to verify the fix

### Suppressing False Positives

Sometimes tools may report issues that aren't actually problems in your specific context:

- **SpotBugs**: Add patterns to `spotbugs-exclude.xml`
- **PMD**: Use `@SuppressWarnings("PMD.RuleName")` annotations
- **Checkstyle**: Use `// CHECKSTYLE:OFF` and `// CHECKSTYLE:ON` comments

## Best Practices

1. **Run checks early and often** during development
2. **Fix issues as they arise** rather than letting them accumulate
3. **Understand why** a rule exists before suppressing it
4. **Maintain high test coverage** to prevent regressions
5. **Review code quality reports** as part of code reviews

## Continuous Integration

These code quality checks are integrated into the CI/CD pipeline and will cause the build to fail if quality standards are not met. This ensures that code quality issues are addressed before code is merged.
