/**
 * monte-carlo.js - Monte Carlo analysis logic for the Monte Carlo page.
 *
 * Depends on: main.js (CivSim namespace), Chart.js (loaded via CDN in monte-carlo.html).
 *
 * Runs N simulations with different seeds, collects statistics, and renders:
 *   - Summary statistics (avg population, avg wealth, avg techs, survival rate)
 *   - Population distribution histogram
 *   - Technology distribution histogram
 *   - Wealth comparison bar chart
 *   - Runs detail table
 */

/* global CivSim, Chart, document, window */

'use strict';

(function () {

    // ---------------------------------------------------------------
    // DOM references
    // ---------------------------------------------------------------

    var runBtn          = document.getElementById('mcRunBtn');
    var numRunsInput    = document.getElementById('numRuns');
    var baseSeedInput   = document.getElementById('baseSeed');
    var loadingEl       = document.getElementById('mcLoading');
    var resultsEl       = document.getElementById('mcResults');
    var progressFill    = document.getElementById('progressFill');
    var progressText    = document.getElementById('progressText');

    var avgPopEl        = document.getElementById('avgPop');
    var avgWealthEl     = document.getElementById('avgWealth');
    var avgTechsEl      = document.getElementById('avgTechs');
    var survivalRateEl  = document.getElementById('survivalRate');
    var totalDurationEl = document.getElementById('totalDuration');

    var popHistCanvas   = document.getElementById('popHistChart');
    var techHistCanvas  = document.getElementById('techHistChart');
    var wealthBarCanvas = document.getElementById('wealthBarChart');
    var runsTableBody   = document.getElementById('runsTableBody');

    // Chart instances
    var popHistChart    = null;
    var techHistChart   = null;
    var wealthBarChart  = null;

    // ---------------------------------------------------------------
    // Event handling
    // ---------------------------------------------------------------

    runBtn.addEventListener('click', function () {
        runBtn.disabled = true;
        loadingEl.style.display = 'block';
        resultsEl.style.display = 'none';
        progressFill.style.width = '0%';
        progressText.textContent = 'Starting...';

        // Use setTimeout to let the UI update before blocking the main thread
        setTimeout(function () {
            try {
                executeMonteCarloAnalysis();
            } finally {
                runBtn.disabled = false;
            }
        }, 50);
    });

    // ---------------------------------------------------------------
    // Monte Carlo execution
    // ---------------------------------------------------------------

    function executeMonteCarloAnalysis() {
        var scenario = CivSim.createRomeScenario();
        var numRuns = parseInt(numRunsInput.value, 10) || 50;
        numRuns = Math.max(2, Math.min(200, numRuns));
        numRunsInput.value = numRuns;

        var baseSeedValue = baseSeedInput.value.trim();
        var baseSeed = baseSeedValue ? parseInt(baseSeedValue, 10) : 12345;
        if (!baseSeedValue) { baseSeedInput.value = baseSeed; }

        var results = [];
        var overallStart = performance.now();

        for (var i = 0; i < numRuns; i++) {
            // Each run uses a different seed derived from the base seed + run index
            var runSeed = baseSeed + i * 7919; // Use a prime multiplier for spread
            var result = CivSim.runSimulation(scenario, runSeed);
            results.push({
                runIndex: i,
                seed: runSeed,
                finalState: result.finalState,
                events: result.events,
                snapshots: result.snapshots,
                durationMs: result.durationMs
            });

            // Update progress
            var pct = ((i + 1) / numRuns * 100);
            progressFill.style.width = pct + '%';
            progressText.textContent = 'Run ' + (i + 1) + ' / ' + numRuns + ' completed';
        }

        var totalDurationMs = performance.now() - overallStart;

        // Analyze
        var analysis = analyzeResults(results);

        // Render
        renderSummary(analysis, totalDurationMs);
        renderPopulationHistogram(results);
        renderTechHistogram(results);
        renderWealthBarChart(results);
        renderRunsTable(results);

        loadingEl.style.display = 'none';
        resultsEl.style.display = 'block';
    }

    // ---------------------------------------------------------------
    // Analysis  (mirrors MonteCarloRunner.analyze)
    // ---------------------------------------------------------------

    function analyzeResults(results) {
        var populations = results.map(function (r) { return r.finalState.population.population; });
        var wealths     = results.map(function (r) { return r.finalState.economy.wealth; });
        var techs       = results.map(function (r) { return r.finalState.technology.unlockedTechs.length; });

        var avgPop    = populations.reduce(function (a, b) { return a + b; }, 0) / populations.length;
        var avgWealth = wealths.reduce(function (a, b) { return a + b; }, 0) / wealths.length;
        var avgTechs  = techs.reduce(function (a, b) { return a + b; }, 0) / techs.length;

        var survivedCount = populations.filter(function (p) { return p > 10000; }).length;
        var survivalRate  = survivedCount / populations.length;

        return {
            totalRuns: results.length,
            avgPopulation: avgPop,
            avgWealth: avgWealth,
            avgTechs: avgTechs,
            survivalRate: survivalRate,
            populations: populations,
            wealths: wealths,
            techs: techs
        };
    }

    // ---------------------------------------------------------------
    // Renderers
    // ---------------------------------------------------------------

    function renderSummary(analysis, totalDurationMs) {
        avgPopEl.textContent      = CivSim.formatNumber(Math.floor(analysis.avgPopulation));
        avgWealthEl.textContent   = CivSim.formatNumber(Math.floor(analysis.avgWealth));
        avgTechsEl.textContent    = analysis.avgTechs.toFixed(1) + ' / ' + CivSim.TECH_TREE.length;
        survivalRateEl.textContent = CivSim.formatPercent(analysis.survivalRate);
        totalDurationEl.textContent = totalDurationMs.toFixed(0) + ' ms (' + analysis.totalRuns + ' runs)';
    }

    function renderPopulationHistogram(results) {
        if (popHistChart) { popHistChart.destroy(); }

        var populations = results.map(function (r) { return r.finalState.population.population; });
        var histogram = buildHistogram(populations, 10);

        popHistChart = new Chart(popHistCanvas.getContext('2d'), {
            type: 'bar',
            data: {
                labels: histogram.labels,
                datasets: [{
                    label: 'Runs',
                    data: histogram.counts,
                    backgroundColor: 'rgba(108,138,255,0.6)',
                    borderColor: '#6c8aff',
                    borderWidth: 1
                }]
            },
            options: {
                responsive: true,
                plugins: {
                    legend: { display: false },
                    title: { display: false }
                },
                scales: {
                    x: {
                        ticks: { color: '#8b8fa8', maxRotation: 45 },
                        grid:  { color: 'rgba(46,51,71,0.5)' }
                    },
                    y: {
                        ticks: { color: '#8b8fa8', stepSize: 1 },
                        grid:  { color: 'rgba(46,51,71,0.5)' },
                        title: { display: true, text: 'Number of Runs', color: '#8b8fa8' }
                    }
                }
            }
        });
    }

    function renderTechHistogram(results) {
        if (techHistChart) { techHistChart.destroy(); }

        var techs = results.map(function (r) { return r.finalState.technology.unlockedTechs.length; });
        var histogram = buildHistogram(techs, Math.min(10, CivSim.TECH_TREE.length));

        techHistChart = new Chart(techHistCanvas.getContext('2d'), {
            type: 'bar',
            data: {
                labels: histogram.labels,
                datasets: [{
                    label: 'Runs',
                    data: histogram.counts,
                    backgroundColor: 'rgba(76,175,80,0.6)',
                    borderColor: '#4caf50',
                    borderWidth: 1
                }]
            },
            options: {
                responsive: true,
                plugins: {
                    legend: { display: false }
                },
                scales: {
                    x: {
                        ticks: { color: '#8b8fa8' },
                        grid:  { color: 'rgba(46,51,71,0.5)' }
                    },
                    y: {
                        ticks: { color: '#8b8fa8', stepSize: 1 },
                        grid:  { color: 'rgba(46,51,71,0.5)' },
                        title: { display: true, text: 'Number of Runs', color: '#8b8fa8' }
                    }
                }
            }
        });
    }

    function renderWealthBarChart(results) {
        if (wealthBarChart) { wealthBarChart.destroy(); }

        // Sort by wealth descending and show top 20
        var sorted = results.slice().sort(function (a, b) {
            return b.finalState.economy.wealth - a.finalState.economy.wealth;
        });
        var top = sorted.slice(0, Math.min(20, sorted.length));

        wealthBarChart = new Chart(wealthBarCanvas.getContext('2d'), {
            type: 'bar',
            data: {
                labels: top.map(function (r) { return 'Run #' + r.runIndex; }),
                datasets: [{
                    label: 'Final Wealth',
                    data: top.map(function (r) { return r.finalState.economy.wealth; }),
                    backgroundColor: 'rgba(255,213,79,0.6)',
                    borderColor: '#ffd54f',
                    borderWidth: 1
                }]
            },
            options: {
                responsive: true,
                plugins: {
                    legend: { display: false }
                },
                scales: {
                    x: {
                        ticks: { color: '#8b8fa8' },
                        grid:  { color: 'rgba(46,51,71,0.5)' }
                    },
                    y: {
                        ticks: {
                            color: '#8b8fa8',
                            callback: function (value) {
                                if (value >= 1e9) return (value / 1e9).toFixed(1) + 'B';
                                if (value >= 1e6) return (value / 1e6).toFixed(1) + 'M';
                                if (value >= 1e3) return (value / 1e3).toFixed(0) + 'K';
                                return value;
                            }
                        },
                        grid: { color: 'rgba(46,51,71,0.5)' }
                    }
                }
            }
        });
    }

    function renderRunsTable(results) {
        runsTableBody.innerHTML = '';

        results.forEach(function (r) {
            var state = r.finalState;
            var tr = document.createElement('tr');
            tr.innerHTML =
                '<td>#' + r.runIndex + '</td>' +
                '<td>' + r.seed + '</td>' +
                '<td>' + CivSim.formatNumber(state.population.population) + '</td>' +
                '<td>' + CivSim.formatNumber(Math.floor(state.economy.wealth)) + '</td>' +
                '<td>' + state.technology.unlockedTechs.length + '</td>' +
                '<td>' + CivSim.formatPercent(state.politics.stability) + '</td>' +
                '<td>' + r.events.length + '</td>' +
                '<td>' + r.durationMs.toFixed(1) + ' ms</td>';
            runsTableBody.appendChild(tr);
        });
    }

    // ---------------------------------------------------------------
    // Histogram utility
    // ---------------------------------------------------------------

    function buildHistogram(values, numBins) {
        if (values.length === 0) { return { labels: [], counts: [] }; }

        var min = Math.min.apply(null, values);
        var max = Math.max.apply(null, values);

        // Handle case where all values are the same
        if (min === max) {
            return { labels: [formatBinLabel(min, min)], counts: [values.length] };
        }

        var binWidth = (max - min) / numBins;
        var counts = new Array(numBins).fill(0);
        var labels = [];

        for (var i = 0; i < numBins; i++) {
            var lo = min + i * binWidth;
            var hi = lo + binWidth;
            labels.push(formatBinLabel(lo, hi));
        }

        values.forEach(function (v) {
            var bin = Math.min(numBins - 1, Math.floor((v - min) / binWidth));
            counts[bin]++;
        });

        return { labels: labels, counts: counts };
    }

    function formatBinLabel(lo, hi) {
        function shortNum(n) {
            if (n >= 1e9) return (n / 1e9).toFixed(1) + 'B';
            if (n >= 1e6) return (n / 1e6).toFixed(1) + 'M';
            if (n >= 1e3) return (n / 1e3).toFixed(0) + 'K';
            return n.toFixed(0);
        }
        return shortNum(lo) + '-' + shortNum(hi);
    }

})();
