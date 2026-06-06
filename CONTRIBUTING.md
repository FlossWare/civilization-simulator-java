# Contributing to Civilization Simulator

Thank you for your interest in contributing to the FlossWare Civilization Simulator!

## Development Setup

### Prerequisites

- Java 21 or higher
- Maven 3.9 or higher
- Git

### Building

```bash
git clone https://github.com/FlossWare/civilization-simulator-java.git
cd civilization-simulator-java
mvn clean package
```

### Running Tests

```bash
mvn test
```

All tests must pass before submitting a PR.

## Contribution Guidelines

### Code Style

- Follow existing code conventions
- Use meaningful variable and method names
- Keep methods focused and under 30 lines when possible
- Add Javadoc comments for all public classes and methods
- Maintain immutability - use records and defensive copying

### Design Principles

This project follows **pure functional design**:

1. **Pure Functions**: Every module must be `(state, params, seed) → (newState, events)`
2. **Immutability**: All state objects are immutable (Java records)
3. **Reproducibility**: Same seed must always produce same results
4. **No Side Effects**: No I/O, mutation, or hidden state in simulation logic

### Adding a New Module

1. Create a new `*Module.java` file in `org.flossware.civilization.module`
2. Implement the pure function pattern:
   ```java
   public static ModuleResult<YourStateType> tick(
       YourStateType current,
       // ... parameters
       SplittableRandom random
   ) {
       // Your logic
       return new ModuleResult<>(newState, events);
   }
   ```
3. Add corresponding state record in `org.flossware.civilization.model`
4. Update `CivilizationState` with `with*()` method
5. Add module to `SimulationEngine.executeTick()` in correct execution order
6. Write comprehensive tests

### Testing Requirements

- Unit tests for all new modules
- Reproducibility tests for any randomized behavior
- Performance benchmarks if adding expensive operations
- All tests must pass: `mvn test`

### Pull Request Process

1. Fork the repository
2. Create a feature branch: `git checkout -b feature/your-feature-name`
3. Make your changes
4. Add tests for your changes
5. Ensure all tests pass: `mvn test`
6. Commit with clear messages following conventional commits format:
   - `feat: Add new climate event type`
   - `fix: Correct sea level calculation bug`
   - `docs: Update API usage examples`
   - `test: Add edge cases for population module`
7. Push to your fork: `git push origin feature/your-feature-name`
8. Open a Pull Request against `main` branch

### PR Review Criteria

Your PR will be reviewed for:

- ✅ Code quality and style consistency
- ✅ Test coverage (aim for >80% on new code)
- ✅ Reproducibility (same seed → same results)
- ✅ Performance (no regressions)
- ✅ Documentation (Javadoc + README updates if needed)
- ✅ Pure functional design adherence

## Reporting Bugs

Use GitHub Issues with the following information:

- **Description**: Clear description of the bug
- **Steps to Reproduce**: Minimal code example
- **Expected Behavior**: What should happen
- **Actual Behavior**: What actually happens
- **Environment**: Java version, OS, Maven version
- **Seed**: If reproducibility issue, provide the seed value

## Feature Requests

We welcome feature requests! Please:

1. Check existing issues first to avoid duplicates
2. Describe the use case and motivation
3. Provide example scenarios or code sketches
4. Consider implementation complexity vs. benefit

## Questions?

- Open a GitHub Discussion for questions
- Tag issues with `question` label
- Check existing documentation in README.md and QUICKSTART.md

## License

By contributing, you agree that your contributions will be licensed under the MIT License.

---

**Thank you for helping make this project better!**
