package org.flossware.civilization;

import org.flossware.civilization.engine.MonteCarloRunner;
import org.flossware.civilization.engine.SimulationEngine;
import org.flossware.civilization.engine.SimulationResult;
import org.flossware.civilization.model.Scenario;
import org.flossware.civilization.scenarios.RomeEnduresScenario;
import org.flossware.civilization.web.WebServer;

import java.util.List;

/**
 * Main entry point for the Alternate History Civilization Simulator.
 *
 * Usage:
 *   java org.flossware.civilization.CivilizationSimulator [mode]
 *
 * Modes:
 *   single  - Run one simulation (fast)
 *   monte   - Run full Monte Carlo analysis (default)
 */
public class CivilizationSimulator {

    public static void main(String[] args) throws Exception {
        // Handle help and validation
        if (args.length == 0 || "help".equals(args[0]) || "--help".equals(args[0]) || "-h".equals(args[0])) {
            printUsage();
            System.exit(0);
        }

        String mode = args[0];

        // Validate command
        if (!"single".equals(mode) && !"monte".equals(mode) && !"montecarlo".equals(mode) && !"server".equals(mode)) {
            System.err.println("Error: Unknown command '" + mode + "'");
            System.err.println();
            printUsage();
            System.exit(1);
        }

        if ("server".equals(mode)) {
            startWebServer(args);
            return;
        }

        System.out.println("=".repeat(80));
        System.out.println("Alternate History Civilization Simulator v1.1");
        System.out.println("=".repeat(80));
        System.out.println();

        Scenario scenario = RomeEnduresScenario.create();
        System.out.println("Scenario: " + scenario.name());
        System.out.println("Description: " + scenario.description());
        System.out.println("Time range: " + scenario.startYear() + " to " + scenario.endYear());
        System.out.println("Duration: " + (scenario.endYear() - scenario.startYear()) + " years");
        System.out.println();

        if ("single".equals(mode)) {
            runSingleSimulation(scenario);
        } else {
            runMonteCarloAnalysis(scenario);
        }
    }

    private static void printUsage() {
        System.out.println("Usage: java -jar civilization-simulator-java-1.9.jar <command>");
        System.out.println();
        System.out.println("Commands:");
        System.out.println("  single      Run a single simulation");
        System.out.println("  monte       Run Monte Carlo analysis (50 runs)");
        System.out.println("  montecarlo  Alias for 'monte'");
        System.out.println("  server      Start the web UI server (port 8080 by default)");
        System.out.println("  help        Show this help message");
        System.out.println();
        System.out.println("Examples:");
        System.out.println("  java -jar civilization-simulator-java-1.9.jar single");
        System.out.println("  java -jar civilization-simulator-java-1.9.jar monte");
        System.out.println("  java -jar civilization-simulator-java-1.9.jar server");
        System.out.println("  java -jar civilization-simulator-java-1.9.jar server --port 9090 --static-dir web-ui/static");
    }

    private static void startWebServer(String[] args) throws Exception {
        int port = 8080;
        String staticDir = "web-ui/static";

        for (int i = 1; i < args.length - 1; i++) {
            if ("--port".equals(args[i])) {
                port = Integer.parseInt(args[i + 1]);
                i++;
            } else if ("--static-dir".equals(args[i])) {
                staticDir = args[i + 1];
                i++;
            }
        }

        WebServer server = new WebServer(port, staticDir);
        server.start();
    }

    private static void runSingleSimulation(Scenario scenario) {
        System.out.println("Running single simulation...");
        long startTime = System.currentTimeMillis();

        SimulationEngine engine = new SimulationEngine(scenario, scenario.simulationRules().baseRandomSeed());
        SimulationResult result = engine.run(0);

        long duration = System.currentTimeMillis() - startTime;

        System.out.println("\n" + "=".repeat(80));
        System.out.println("SIMULATION COMPLETE");
        System.out.println("=".repeat(80));
        System.out.println("Duration: " + duration + " ms");
        System.out.println();

        printFinalState(result);
        printEventSummary(result);

        double yearsPerMs = (double)(scenario.endYear() - scenario.startYear()) / duration;
        System.out.println(String.format("Performance: %.1f years/ms", yearsPerMs));
    }

    private static void runMonteCarloAnalysis(Scenario scenario) throws Exception {
        System.out.println("Running Monte Carlo analysis (" + scenario.simulationRules().monteCarloRuns() + " runs)...");
        System.out.println("Using " + scenario.simulationRules().parallelThreads() + " parallel threads");
        System.out.println();

        long startTime = System.currentTimeMillis();

        MonteCarloRunner runner = new MonteCarloRunner(scenario);
        List<SimulationResult> results = runner.runAll();

        long duration = System.currentTimeMillis() - startTime;

        System.out.println("\n" + "=".repeat(80));
        System.out.println("MONTE CARLO ANALYSIS COMPLETE");
        System.out.println("=".repeat(80));
        System.out.println("Total duration: " + duration + " ms");
        System.out.println("Average per run: " + (duration / results.size()) + " ms");
        System.out.println();

        MonteCarloRunner.MonteCarloAnalysis analysis = MonteCarloRunner.analyze(results);
        System.out.println(analysis);

        // Show best and worst outcomes
        System.out.println("=".repeat(80));
        System.out.println("OUTCOME RANGE");
        System.out.println("=".repeat(80));

        var bestPopulation = results.stream()
            .max((a, b) -> Long.compare(
                a.finalState().population().population(),
                b.finalState().population().population()
            ));

        var worstPopulation = results.stream()
            .min((a, b) -> Long.compare(
                a.finalState().population().population(),
                b.finalState().population().population()
            ));

        if (bestPopulation.isPresent() && worstPopulation.isPresent()) {
            System.out.println("Best outcome population: " +
                bestPopulation.get().finalState().population().population());
            System.out.println("Worst outcome population: " +
                worstPopulation.get().finalState().population().population());
        } else {
            System.out.println("No valid results available to compare outcomes.");
        }

        double yearsPerMs = (double)(scenario.endYear() - scenario.startYear()) * results.size() / duration;
        System.out.println(String.format("\nAggregate performance: %.1f years/ms across all runs", yearsPerMs));
    }

    private static void printFinalState(SimulationResult result) {
        var state = result.finalState();

        System.out.println("FINAL STATE (" + state.year() + ")");
        System.out.println("-".repeat(80));
        System.out.println("Civilization: " + state.name());
        System.out.println("Population: " + state.population().population());
        System.out.println("Wealth: " + String.format("%.0f", state.economy().wealth()));
        System.out.println("GDP: " + String.format("%.0f", state.economy().gdp()));
        System.out.println("Technologies unlocked: " + state.technology().unlockedTechs().size());
        System.out.println("Army size: " + state.military().armySize());
        System.out.println("Political stability: " + String.format("%.2f", state.politics().stability()));
        System.out.println("Religious unity: " + String.format("%.2f", state.religion().religiousUnity()));
        System.out.println();
    }

    private static void printEventSummary(SimulationResult result) {
        System.out.println("EVENT SUMMARY");
        System.out.println("-".repeat(80));
        System.out.println("Total events: " + result.events().size());

        var eventsByType = result.events().stream()
            .collect(java.util.stream.Collectors.groupingBy(
                e -> e.type(),
                java.util.stream.Collectors.counting()
            ));

        eventsByType.forEach((type, count) ->
            System.out.println("  " + type + ": " + count)
        );
        System.out.println();

        // Show some major events
        System.out.println("Major Events:");
        result.events().stream()
            .filter(e -> e.severity() == org.flossware.civilization.model.Event.EventSeverity.MAJOR ||
                        e.severity() == org.flossware.civilization.model.Event.EventSeverity.CRITICAL)
            .limit(10)
            .forEach(e -> System.out.println("  Year " + e.year() + ": " + e.description()));
        System.out.println();
    }
}
