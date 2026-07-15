import Foundation
import shared

@MainActor
class SimulationViewModel: ObservableObject {
    @Published var isRunning = false
    @Published var selectedScenarioId: String = "rome"
    @Published var singleResult: SimulationResult? = nil
    @Published var analysis: MonteCarloRunner.MonteCarloAnalysis? = nil
    @Published var durationMs: Int64 = 0
    @Published var errorMessage: String? = nil

    var availableScenarios: [ScenarioRegistry.ScenarioInfo] {
        ScenarioRegistry.shared.listAll() as! [ScenarioRegistry.ScenarioInfo]
    }

    var selectedScenarioInfo: ScenarioRegistry.ScenarioInfo? {
        availableScenarios.first { $0.id == selectedScenarioId }
    }

    func selectScenario(_ id: String) {
        selectedScenarioId = id
        singleResult = nil
        analysis = nil
    }

    func runSingle() {
        isRunning = true
        errorMessage = nil
        Task {
            do {
                let scenario = ScenarioRegistry.shared.get(id: selectedScenarioId)
                let start = DispatchTime.now()
                let engine = SimulationEngine(scenario: scenario, baseSeed: scenario.simulationRules.baseRandomSeed)
                let result = engine.run(runIndex: 0)
                let end = DispatchTime.now()
                let nanos = end.uptimeNanoseconds - start.uptimeNanoseconds
                self.durationMs = Int64(nanos / 1_000_000)
                self.singleResult = result
                self.isRunning = false
            } catch {
                self.errorMessage = error.localizedDescription
                self.isRunning = false
            }
        }
    }

    func runMonteCarlo() {
        isRunning = true
        errorMessage = nil
        Task {
            do {
                let scenario = ScenarioRegistry.shared.get(id: selectedScenarioId)
                let start = DispatchTime.now()
                let runner = MonteCarloRunner(scenario: scenario)
                let results = try await runner.runAll()
                let end = DispatchTime.now()
                let nanos = end.uptimeNanoseconds - start.uptimeNanoseconds
                self.durationMs = Int64(nanos / 1_000_000)
                self.analysis = MonteCarloRunner.companion.analyze(results: results)
                self.isRunning = false
            } catch {
                self.errorMessage = error.localizedDescription
                self.isRunning = false
            }
        }
    }
}
