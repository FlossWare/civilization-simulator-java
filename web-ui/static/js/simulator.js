/**
 * simulator.js - Single simulation runner logic for the Simulator page.
 *
 * Depends on: main.js (CivSim namespace), Chart.js (loaded via CDN in simulator.html).
 *
 * Wires up the "Run Simulation" button, calls the backend /api/simulate
 * endpoint, and renders results including:
 *   - Performance statistics
 *   - Final civilization state metrics
 *   - Population over time chart
 *   - Economy & technology chart
 *   - Event timeline
 */

/* global CivSim, Chart, document, window */

'use strict';

(function () {

    // ---------------------------------------------------------------
    // DOM references
    // ---------------------------------------------------------------

    var runBtn         = document.getElementById('runBtn');
    var seedInput      = document.getElementById('seed');
    var loadingEl      = document.getElementById('loading');
    var resultsEl      = document.getElementById('results');
    var durationEl     = document.getElementById('duration');
    var yearsPerMsEl   = document.getElementById('yearsPerMs');
    var eventCountEl   = document.getElementById('eventCount');
    var finalStateEl   = document.getElementById('finalState');
    var timelineEl     = document.getElementById('eventTimeline');
    var popCanvasEl    = document.getElementById('populationChart');
    var econCanvasEl   = document.getElementById('economyChart');

    // Chart instances (so we can destroy before re-creating)
    var popChart  = null;
    var econChart = null;

    // ---------------------------------------------------------------
    // Event handling
    // ---------------------------------------------------------------

    runBtn.addEventListener('click', function () {
        runBtn.disabled = true;
        loadingEl.style.display = 'block';
        resultsEl.style.display = 'none';
        executeSimulation();
    });

    // ---------------------------------------------------------------
    // Simulation execution (calls backend API)
    // ---------------------------------------------------------------

    async function executeSimulation() {
        try {
            var seedValue = seedInput.value.trim();
            var body = {};
            if (seedValue) {
                body.seed = parseInt(seedValue, 10);
            }

            var scenarioSelect = document.getElementById('scenarioSelect');
            if (scenarioSelect && scenarioSelect.value) {
                body.scenario = scenarioSelect.value;
            }

            var response = await fetch('/api/simulate', {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify(body)
            });

            if (!response.ok) {
                throw new Error('Simulation failed: ' + response.statusText);
            }

            var result = await response.json();

            // If user didn't enter a seed, show what backend used
            if (!seedValue && result.seed != null) {
                seedInput.value = result.seed;
            }

            var totalYears = result.snapshots.length > 1
                ? result.snapshots[result.snapshots.length - 1].year - result.snapshots[0].year
                : 2053;

            renderPerformance(result.durationMs, totalYears, result.events.length);
            renderFinalState(result.finalState, result.techTreeSize);
            renderPopulationChart(result.snapshots);
            renderEconomyChart(result.snapshots);
            renderTimeline(result.events);

            resultsEl.style.display = 'block';
        } catch (error) {
            alert('Error: ' + error.message);
        } finally {
            loadingEl.style.display = 'none';
            runBtn.disabled = false;
        }
    }

    // ---------------------------------------------------------------
    // Renderers
    // ---------------------------------------------------------------

    function renderPerformance(durationMs, totalYears, eventCount) {
        durationEl.textContent = durationMs.toFixed(1) + ' ms';
        yearsPerMsEl.textContent = (totalYears / durationMs).toFixed(0) + ' years/ms';
        eventCountEl.textContent = eventCount;
    }

    function renderFinalState(state, techTreeSize) {
        var metrics = [
            { label: 'Population',     value: CivSim.formatNumber(state.population.population),        detail: 'Carrying capacity: ' + CivSim.formatNumber(Math.floor(state.population.carryingCapacity)) },
            { label: 'Wealth',         value: CivSim.formatNumber(Math.floor(state.economy.wealth)),   detail: 'GDP: ' + CivSim.formatNumber(Math.floor(state.economy.gdp)) },
            { label: 'Technologies',   value: state.technology.unlockedTechs.length + ' / ' + techTreeSize, detail: 'Literacy: ' + CivSim.formatPercent(state.technology.literacyRate) },
            { label: 'Stability',      value: CivSim.formatPercent(state.politics.stability),          detail: 'Government: ' + state.politics.government },
            { label: 'Army Size',      value: CivSim.formatNumber(state.military.armySize),            detail: 'Navy: ' + CivSim.formatNumber(state.military.navySize) },
            { label: 'Trade Routes',   value: state.economy.tradeRoutes.length,                        detail: 'Trade surplus: ' + CivSim.formatDecimal(state.economy.tradeSurplus, 2) },
            { label: 'Climate',        value: (state.climate.temperatureAnomaly >= 0 ? '+' : '') + state.climate.temperatureAnomaly.toFixed(2) + ' C', detail: 'Drought index: ' + CivSim.formatDecimal(state.climate.droughtIndex, 2) },
            { label: 'Religion',       value: getDominantReligion(state.religion),                     detail: 'Unity: ' + CivSim.formatPercent(state.religion.religiousUnity) }
        ];

        finalStateEl.innerHTML = '';
        metrics.forEach(function (m) {
            var div = document.createElement('div');
            div.className = 'state-metric';
            div.innerHTML =
                '<span class="metric-label">' + m.label + '</span>' +
                '<span class="metric-value">' + m.value + '</span>' +
                '<span class="metric-detail">' + m.detail + '</span>';
            finalStateEl.appendChild(div);
        });
    }

    function getDominantReligion(religion) {
        var shares = religion.religionShares;
        var best = '';
        var bestVal = 0;
        for (var key in shares) {
            if (shares.hasOwnProperty(key) && shares[key] > bestVal) {
                bestVal = shares[key];
                best = key;
            }
        }
        return best + ' (' + CivSim.formatPercent(bestVal) + ')';
    }

    function renderPopulationChart(snapshots) {
        if (popChart) { popChart.destroy(); }

        var labels = snapshots.map(function (s) { return CivSim.formatYear(s.year); });
        var data   = snapshots.map(function (s) { return s.population; });

        popChart = new Chart(popCanvasEl.getContext('2d'), {
            type: 'line',
            data: {
                labels: labels,
                datasets: [{
                    label: 'Population',
                    data: data,
                    borderColor: '#6c8aff',
                    backgroundColor: 'rgba(108,138,255,0.1)',
                    fill: true,
                    tension: 0.3,
                    pointRadius: 0
                }]
            },
            options: {
                responsive: true,
                plugins: {
                    legend: { labels: { color: '#e0e0e8' } }
                },
                scales: {
                    x: {
                        ticks: { color: '#8b8fa8', maxTicksLimit: 10 },
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

    function renderEconomyChart(snapshots) {
        if (econChart) { econChart.destroy(); }

        var labels = snapshots.map(function (s) { return CivSim.formatYear(s.year); });

        econChart = new Chart(econCanvasEl.getContext('2d'), {
            type: 'line',
            data: {
                labels: labels,
                datasets: [
                    {
                        label: 'Wealth',
                        data: snapshots.map(function (s) { return s.wealth; }),
                        borderColor: '#ffd54f',
                        backgroundColor: 'rgba(255,213,79,0.08)',
                        fill: false,
                        tension: 0.3,
                        pointRadius: 0,
                        yAxisID: 'y'
                    },
                    {
                        label: 'Technologies',
                        data: snapshots.map(function (s) { return s.techs; }),
                        borderColor: '#4caf50',
                        fill: false,
                        tension: 0.3,
                        pointRadius: 0,
                        yAxisID: 'y1'
                    }
                ]
            },
            options: {
                responsive: true,
                plugins: {
                    legend: { labels: { color: '#e0e0e8' } }
                },
                scales: {
                    x: {
                        ticks: { color: '#8b8fa8', maxTicksLimit: 10 },
                        grid:  { color: 'rgba(46,51,71,0.5)' }
                    },
                    y: {
                        position: 'left',
                        ticks: {
                            color: '#ffd54f',
                            callback: function (value) {
                                if (value >= 1e9) return (value / 1e9).toFixed(1) + 'B';
                                if (value >= 1e6) return (value / 1e6).toFixed(1) + 'M';
                                if (value >= 1e3) return (value / 1e3).toFixed(0) + 'K';
                                return value;
                            }
                        },
                        grid: { color: 'rgba(46,51,71,0.5)' }
                    },
                    y1: {
                        position: 'right',
                        ticks: { color: '#4caf50' },
                        grid:  { drawOnChartArea: false }
                    }
                }
            }
        });
    }

    function renderTimeline(events) {
        // Show only MAJOR and CRITICAL events to avoid overwhelming the timeline
        var filtered = events.filter(function (e) {
            return e.severity === 'MAJOR' || e.severity === 'CRITICAL';
        });

        // Cap at 200 entries
        if (filtered.length > 200) {
            filtered = filtered.slice(filtered.length - 200);
        }

        timelineEl.innerHTML = '';
        filtered.forEach(function (e) {
            var div = document.createElement('div');
            div.className = 'event-entry event-severity-' + e.severity;
            div.innerHTML =
                '<span class="event-year">' + CivSim.formatYear(e.year) + '</span>' +
                '<div>' +
                    '<span class="event-description">' + escapeHtml(e.description) + '</span>' +
                    '<span class="event-type">' + e.type.replace(/_/g, ' ') + '</span>' +
                '</div>';
            timelineEl.appendChild(div);
        });
    }

    function escapeHtml(text) {
        var div = document.createElement('div');
        div.appendChild(document.createTextNode(text));
        return div.innerHTML;
    }

    // ---------------------------------------------------------------
    // Initialise scenario dropdown
    // ---------------------------------------------------------------

    CivSim.initScenarioDropdown('scenarioSelect');

})();
