# Civilization Simulator

A deterministic alternate-history civilization simulator built in Java 21.

What if Rome never fell? Given a seed and a scenario, it simulates 2,000+ years of civilization across seven interconnected domains — population, economy, technology, climate, politics, military, and religion. Every run with the same seed produces identical results, making it suitable for Monte Carlo analysis and reproducible experimentation.

![Home Page](docs/screenshots/home.png)

## Quick Start

```bash
# Build
mvn clean package -q

# Run a single simulation
java -jar target/civilization-simulator-java-1.10.jar single --seed 42

# Run Monte Carlo analysis (50 parallel runs)
java -jar target/civilization-simulator-java-1.10.jar monte --runs 50 --seed 12345

# Start the web UI
java -jar target/civilization-simulator-java-1.10.jar server --port 8080
# Open http://localhost:8080
```

Requires Java 21+.

## Web UI

The built-in web server serves a Chart.js dashboard for interactive simulation and Monte Carlo analysis.

### Simulation View

Run a single simulation and visualize population, economy, and technology over 2,053 years:

![Simulation Results](docs/screenshots/simulation-results.png)

### Monte Carlo Analysis

Run 2-200 simulations with statistical analysis across all runs:

![Monte Carlo](docs/screenshots/monte-carlo.png)

## Architecture

Pure-function simulation: `(state, seed) → (newState, events)`. No mutation, no side effects.

```
src/main/java/org/flossware/civilization/
├── model/              # Immutable state records (Java records)
│   ├── CivilizationState   # Top-level state aggregate
│   ├── PopulationState     # Births, deaths, growth rate
│   ├── EconomyState        # Wealth, GDP, trade routes
│   ├── TechnologyState     # Unlocked techs, literacy
│   ├── ClimateState        # Temperature anomaly, resources
│   ├── PoliticsState       # Stability, rebellion, succession
│   ├── MilitaryState       # Army, wars, defense
│   ├── ReligionState       # Religion shares, unity
│   └── TechGraph           # DAG with cycle detection
├── module/             # Stateless tick functions
│   ├── PopulationModule    # Growth, plague, carrying capacity
│   ├── EconomyModule       # Production, trade, taxation
│   ├── TechnologyModule    # Research, diffusion, unlocks
│   ├── ClimateModule       # Temperature, disasters, resources
│   ├── PoliticsModule      # Mean-reverting stability, war exhaustion
│   ├── MilitaryModule      # War probability, army scaling
│   └── ReligionModule      # Spread, conversion, schisms
├── engine/             # Simulation execution
│   ├── SimulationEngine    # Variable-tick loop (1-10 year ticks)
│   ├── MonteCarloRunner    # Parallel execution with ExecutorService
│   ├── TickType            # Adaptive time steps based on volatility
│   └── SeedManager         # Hierarchical deterministic seeding
├── scenarios/
│   └── RomeEnduresScenario # "What if Rome never fell?" (27 BCE → 2026 CE)
└── web/
    └── WebServer           # REST API + static file server
```

### Deterministic Seeding

Every random decision derives from a hierarchical seed chain:

```
baseSeed → runSeed(runIndex) → yearSeed(year, tickType) → moduleSeed(module)
```

Same seed = identical results across runs, threads, and platforms.

### Variable Tick Rate

The engine adapts its time step based on current conditions:

| Condition | Tick Size | When |
|-----------|-----------|------|
| Crisis | 1 year | Stability < 0.3 or climate volatility > 0.7 |
| Normal | 5 years | Default |
| Stable | 10 years | Stability > 0.8 and low volatility |

## REST API

```
POST /api/simulate      {"seed": 42}                    → simulation results + snapshots
POST /api/monte-carlo   {"numRuns": 50, "baseSeed": 42} → statistical analysis + per-run data
GET  /api/health                                        → {"status": "ok"}
```

## Default Scenario: Rome Endures

The built-in scenario starts at 27 BCE with the Roman Empire and simulates through 2026 CE:

- **Population**: 56M initial, logistic growth with plague and famine
- **Economy**: 1B denarii starting wealth, trade route expansion
- **Technology**: 20-node tech tree (Agriculture → Spaceflight) with prerequisite DAG
- **Climate**: Volcanic eruptions, temperature anomalies, resource impacts
- **Politics**: Mean-reverting stability, rebellion at <20%, succession crises
- **Military**: War probability tied to economy and politics
- **Religion**: Roman Polytheism start, conversion mechanics, schisms

## Tests

```bash
mvn test
```

87 tests covering all simulation modules, tech tree validation, reproducibility, and performance.

## Tech Stack

- Java 21 (records, sealed classes)
- Jackson 2.18 (JSON serialization)
- `com.sun.net.httpserver` (zero-dependency web server)
- Chart.js 4.4 (frontend visualization)
- JUnit 5 (testing)
- Maven (build)

## License

GPL-3.0
