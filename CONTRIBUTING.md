# Contributing to Spring Boot Course Deal Server

First off, thank you for considering contributing to the Spring Boot Course Deal Server! It's people like you that make this project possible.

## How Can I Contribute?

### Reporting Bugs

If you find a bug, please report it by opening an issue on GitHub. Please include as much detail as possible, including steps to reproduce the issue, your environment, and any relevant logs or screenshots.

### Suggesting Enhancements

If you have an idea for an enhancement or new feature, please open an issue on GitHub. Describe your idea in detail, including any benefits it would bring and any potential drawbacks.

### Submitting Pull Requests

1. **Fork the Repository**: Click the "Fork" button at the top right of the repository page.

2. **Clone Your Fork**: Clone your forked repository to your local machine.

```shell
    git clone https://github.com/huythanh0x/course-deal-server.git
    cd course-deal-server
```

3. **Create a Branch**: Create a new branch for your feature or bugfix.

```shell
    git checkout -b feature/your-feature-name
```

4. **Make Your Changes**: Make your changes to the codebase.

5. **Commit Your Changes**: Commit your changes with a clear and descriptive commit message.

```shell
    git add .
    git commit -m "Add feature: your feature name"
```

6. **Push to Your Fork**: Push your changes to your forked repository.

```shell
    git push origin feature/your-feature-name
```

7. **Open a Pull Request**: Go to the original repository and open a pull request. Provide a clear and descriptive title and description for your pull request.

### Code Style

This project uses [ktlint](https://github.com/pinterest/ktlint) for formatting and [detekt](https://detekt.dev/) for static analysis, running on every module. Before submitting a PR:

```shell
./gradlew ktlintCheck detekt   # check for violations
./gradlew ktlintFormat         # auto-fix formatting issues
```

Pre-existing violations are snapshotted per-module in `ktlint-baseline.xml`/`detekt-baseline.xml`, so only newly introduced issues fail the build. Dependency and plugin versions are centralized in [`gradle/libs.versions.toml`](gradle/libs.versions.toml) - add new dependencies there rather than hardcoding versions in a module's `build.gradle.kts`.

### Running Tests

Before submitting your pull request, make sure all tests pass:

```shell
./gradlew test
```

CI runs `ktlintCheck`, `detekt`, and the full test suite on every push and pull request - a PR won't merge cleanly if any of these fail.