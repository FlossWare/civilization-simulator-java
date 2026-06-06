/**
 * main.js - Common utilities and initialization for the Civilization Simulator Web UI.
 *
 * Provides:
 *   - Number formatting helpers
 *   - Year display formatting (BCE/CE)
 *   - The Rome Endures scenario definition (mirroring RomeEnuresScenario.java)
 *   - A simplified client-side simulation engine that mirrors the Java engine
 *   - Shared DOM utility functions
 */

/* global window, document */

'use strict';

var CivSim = (function () {

    // ---------------------------------------------------------------
    // Formatting utilities
    // ---------------------------------------------------------------

    /**
     * Formats a number with locale-appropriate thousands separators.
     * @param {number} n
     * @returns {string}
     */
    function formatNumber(n) {
        if (n == null) { return '---'; }
        return Number(n).toLocaleString('en-US', { maximumFractionDigits: 0 });
    }

    /**
     * Formats a number with a fixed number of decimal places.
     * @param {number} n
     * @param {number} decimals
     * @returns {string}
     */
    function formatDecimal(n, decimals) {
        if (n == null) { return '---'; }
        return Number(n).toLocaleString('en-US', {
            minimumFractionDigits: decimals,
            maximumFractionDigits: decimals
        });
    }

    /**
     * Formats a year as BCE/CE.
     * @param {number} year  negative = BCE, positive = CE
     * @returns {string}
     */
    function formatYear(year) {
        if (year < 0) {
            return Math.abs(year) + ' BCE';
        }
        return year + ' CE';
    }

    /**
     * Formats a percentage from a 0-1 fraction.
     * @param {number} fraction
     * @returns {string}
     */
    function formatPercent(fraction) {
        if (fraction == null) { return '---'; }
        return (fraction * 100).toFixed(1) + '%';
    }

    // ---------------------------------------------------------------
    // Seeded PRNG  (SplittableRandom-style, for reproducibility)
    // Uses a simple xoshiro128** to keep things deterministic in JS.
    // ---------------------------------------------------------------

    /**
     * Creates a seeded PRNG.
     * @param {number} seed
     * @returns {{ nextDouble: function(): number, nextInt: function(number): number }}
     */
    function createRandom(seed) {
        // Use a simple mulberry32 generator.
        var s = seed | 0;
        function next() {
            s = (s + 0x6D2B79F5) | 0;
            var t = Math.imul(s ^ (s >>> 15), 1 | s);
            t = (t + Math.imul(t ^ (t >>> 7), 61 | t)) ^ t;
            return ((t ^ (t >>> 14)) >>> 0) / 4294967296;
        }
        return {
            nextDouble: next,
            nextInt: function (bound) {
                return Math.floor(next() * bound);
            },
            /** Derive a child RNG from a string tag. */
            split: function (tag) {
                var h = 0;
                for (var i = 0; i < tag.length; i++) {
                    h = ((h << 5) - h + tag.charCodeAt(i)) | 0;
                }
                return createRandom(s ^ h);
            }
        };
    }

    // ---------------------------------------------------------------
    // Scenario: Rome Endures  (mirrors RomeEnuresScenario.java)
    // ---------------------------------------------------------------

    var TECH_TREE = [
        { id: 'agriculture',         era: 'neolithic',  prerequisites: [],                                 researchCost: 50,  diffusionRate: 0.1  },
        { id: 'mining',              era: 'neolithic',  prerequisites: [],                                 researchCost: 50,  diffusionRate: 0.1  },
        { id: 'ironWorking',         era: 'classical',  prerequisites: ['mining'],                         researchCost: 100, diffusionRate: 0.08 },
        { id: 'metallurgy_advanced', era: 'classical',  prerequisites: ['ironWorking'],                    researchCost: 150, diffusionRate: 0.06 },
        { id: 'magnetism',           era: 'classical',  prerequisites: [],                                 researchCost: 60,  diffusionRate: 0.1  },
        { id: 'copperSmelting',      era: 'classical',  prerequisites: ['mining'],                         researchCost: 80,  diffusionRate: 0.09 },
        { id: 'coalMining',          era: 'medieval',   prerequisites: ['mining'],                         researchCost: 120, diffusionRate: 0.07 },
        { id: 'chemistry_basic',     era: 'medieval',   prerequisites: [],                                 researchCost: 80,  diffusionRate: 0.09 },
        { id: 'steamEngine',         era: 'industrial', prerequisites: ['metallurgy_advanced', 'coalMining'], researchCost: 300, diffusionRate: 0.05 },
        { id: 'combustion',          era: 'industrial', prerequisites: ['steamEngine', 'chemistry_basic'], researchCost: 250, diffusionRate: 0.05 },
        { id: 'electricity',         era: 'industrial', prerequisites: ['magnetism', 'copperSmelting'],    researchCost: 350, diffusionRate: 0.04 },
        { id: 'materials_science',   era: 'modern',     prerequisites: ['metallurgy_advanced'],            researchCost: 400, diffusionRate: 0.03 },
        { id: 'semiconductor',       era: 'modern',     prerequisites: ['electricity', 'materials_science'], researchCost: 500, diffusionRate: 0.02 },
        { id: 'writing',             era: 'neolithic',  prerequisites: [],                                 researchCost: 40,  diffusionRate: 0.12 },
        { id: 'mathematics',         era: 'classical',  prerequisites: ['writing'],                        researchCost: 90,  diffusionRate: 0.08 },
        { id: 'optics',              era: 'medieval',   prerequisites: ['mathematics'],                    researchCost: 110, diffusionRate: 0.07 },
        { id: 'telescope',           era: 'industrial', prerequisites: ['optics'],                         researchCost: 200, diffusionRate: 0.06 },
        { id: 'radio',               era: 'industrial', prerequisites: ['electricity'],                    researchCost: 280, diffusionRate: 0.05 },
        { id: 'computing',           era: 'modern',     prerequisites: ['electricity', 'mathematics'],     researchCost: 450, diffusionRate: 0.03 },
        { id: 'internet',            era: 'modern',     prerequisites: ['computing', 'radio'],             researchCost: 550, diffusionRate: 0.02 }
    ];

    /** Build a lookup map from the tech tree. */
    var TECH_MAP = {};
    TECH_TREE.forEach(function (t) { TECH_MAP[t.id] = t; });

    function createRomeScenario() {
        return {
            scenarioId: 'rome-endures-2026',
            name: 'Rome Survives to Modern Era',
            description: 'What if the Western Roman Empire endured through history?',
            startYear: -27,
            endYear: 2026,
            techTree: TECH_TREE,
            worldConstraints: {
                politicalStability: 0.6,
                warFrequency: 0.45,
                climateVolatility: 0.25,
                plagueProbability: 0.015,
                resourceAbundance: 0.65
            },
            simulationRules: {
                timeStepMode: 'adaptive',
                deterministic: true,
                baseRandomSeed: 12345,
                monteCarloRuns: 50,
                parallelThreads: 8
            },
            initialState: {
                id: 'rome',
                name: 'Roman Empire',
                coreRegions: ['Italy', 'Gaul', 'Hispania', 'Britannia', 'Greece', 'Egypt'],
                capital: 'Rome',
                year: -27,
                population: { population: 5000000, birthRate: 0.03, deathRate: 0.02, carryingCapacity: 10000000, plagueActive: false },
                economy:    { wealth: 50000000, production: 100000, consumption: 80000, workers: 500000, tradeSurplus: 0.1, gdp: 100000, tradeRoutes: [
                    { from: 'Rome', to: 'Egypt', goods: ['grain', 'papyrus'], volume: 1000, tariff: 0.1 },
                    { from: 'Rome', to: 'Gaul',  goods: ['wine', 'pottery'],  volume: 500,  tariff: 0.15 }
                ]},
                technology: { unlockedTechs: ['agriculture', 'mining', 'ironWorking', 'metallurgy_advanced'], researchProgress: {}, literacyRate: 0.15, universities: 3 },
                politics:   { stability: 0.6, government: 'Empire', rulerAge: 35, warExhaustion: 0.0, inRebellion: false, inSuccessionCrisis: false },
                military:   { armySize: 250000, navySize: 50000, techAdvantage: 1.0, logisticsScore: 0.7, atWar: false, warOpponent: null },
                climate:    { temperatureAnomaly: 0.0, droughtIndex: 0.5, stormFrequency: 0.3, seaLevelRise_mm: 0.0 },
                religion:   { religionShares: { 'Roman Polytheism': 0.85, 'Christianity': 0.05, 'Judaism': 0.10 }, religiousUnity: 0.85, stabilityBonus: 0.17, spreadRate: 0.05 }
            }
        };
    }

    // ---------------------------------------------------------------
    // Simplified client-side simulation engine
    // Mirrors the Java SimulationEngine tick loop at a high level.
    // ---------------------------------------------------------------

    /**
     * Deep-clone a state object (JSON-safe).
     */
    function cloneState(s) {
        return JSON.parse(JSON.stringify(s));
    }

    /**
     * Clamp a value to [min, max].
     */
    function clamp(val, min, max) {
        return Math.max(min, Math.min(max, val));
    }

    /**
     * Calculate resource abundance from climate state (mirrors ClimateState.getResourceAbundance).
     */
    function getResourceAbundance(climate) {
        var droughtPenalty = Math.abs(climate.droughtIndex - 0.5) * 2;
        var tempPenalty = Math.abs(climate.temperatureAnomaly) * 0.1;
        return Math.max(0.1, 1.0 - droughtPenalty * 0.3 - tempPenalty);
    }

    /**
     * Calculate trade connectivity from economy state.
     */
    function getTradeConnectivity(economy) {
        return Math.min(1.0, economy.tradeRoutes.length * 0.2);
    }

    /**
     * Run a single simulation using the given scenario and seed.
     * Returns { finalState, events, snapshots, durationMs }.
     * snapshots is an array of { year, population, wealth, techs } sampled every ~50 years for charting.
     */
    function runSimulation(scenario, seed) {
        var startTime = performance.now();
        var rng = createRandom(seed);
        var state = cloneState(scenario.initialState);
        var events = [];
        var snapshots = [];
        var currentYear = scenario.startYear;
        var snapshotInterval = 50;
        var nextSnapshot = currentYear;

        while (currentYear <= scenario.endYear) {
            // Determine tick step (simplified adaptive: 1-5 years based on stability)
            var step = state.politics.stability > 0.5 ? 3 : 1;

            var yearRng = rng.split('year' + currentYear);

            // --- Climate tick ---
            var climateRng = yearRng.split('climate');
            state.climate.temperatureAnomaly = clamp(
                state.climate.temperatureAnomaly + (climateRng.nextDouble() - 0.5) * scenario.worldConstraints.climateVolatility * 0.5,
                -5, 5
            );
            state.climate.droughtIndex = clamp(
                state.climate.droughtIndex + (climateRng.nextDouble() - 0.5) * 0.1,
                0, 1
            );
            state.climate.stormFrequency = clamp(
                state.climate.stormFrequency + (climateRng.nextDouble() - 0.5) * 0.05,
                0, 1
            );
            if (climateRng.nextDouble() < 0.005) {
                events.push({ year: currentYear, type: 'CLIMATE_DISASTER', severity: 'CRITICAL', description: 'Major natural disaster strikes ' + state.capital });
                state.population.population = Math.max(1000, Math.floor(state.population.population * 0.92));
                state.economy.wealth = Math.max(0, state.economy.wealth * 0.85);
            }

            // --- Population tick ---
            var popRng = yearRng.split('population');
            var resourceAbundance = getResourceAbundance(state.climate);
            var growthRate = (state.population.birthRate - state.population.deathRate) * resourceAbundance;
            var capacityRatio = state.population.population / state.population.carryingCapacity;
            if (capacityRatio > 0.8) {
                growthRate *= Math.max(0.1, 1.0 - (capacityRatio - 0.8) * 5);
            }
            var delta = state.population.population * growthRate * step;
            state.population.population = Math.max(1000, Math.floor(state.population.population + delta));

            // Plague check
            if (!state.population.plagueActive && popRng.nextDouble() < scenario.worldConstraints.plagueProbability * step) {
                state.population.plagueActive = true;
                var loss = 0.1 + popRng.nextDouble() * 0.25;
                state.population.population = Math.max(1000, Math.floor(state.population.population * (1 - loss)));
                events.push({ year: currentYear, type: 'PLAGUE', severity: 'CRITICAL', description: 'Plague outbreak kills ' + (loss * 100).toFixed(0) + '% of population' });
            } else if (state.population.plagueActive && popRng.nextDouble() < 0.3) {
                state.population.plagueActive = false;
            }

            // Carrying capacity grows with tech
            state.population.carryingCapacity = Math.max(
                state.population.carryingCapacity,
                state.population.carryingCapacity * (1 + state.technology.unlockedTechs.length * 0.001 * step)
            );

            // Population milestone
            var popMilestones = [10000000, 50000000, 100000000, 500000000, 1000000000];
            for (var mi = 0; mi < popMilestones.length; mi++) {
                if (state.population.population >= popMilestones[mi] && (state.population.population - delta) < popMilestones[mi]) {
                    events.push({ year: currentYear, type: 'POPULATION_MILESTONE', severity: 'MAJOR', description: 'Population reaches ' + formatNumber(popMilestones[mi]) });
                }
            }

            // --- Economy tick ---
            var econRng = yearRng.split('economy');
            state.economy.workers = Math.floor(state.population.population * 0.15);
            state.economy.production = state.economy.workers * (1 + state.technology.unlockedTechs.length * 0.05) * resourceAbundance;
            state.economy.consumption = state.economy.workers * 0.8;
            state.economy.gdp = state.economy.production - state.economy.consumption;
            state.economy.wealth = Math.max(0, state.economy.wealth + state.economy.gdp * step);

            // Possible trade route
            if (econRng.nextDouble() < 0.01 * step) {
                var destinations = ['Persia', 'India', 'China', 'Arabia', 'Britannia', 'Scandinavia', 'Africa', 'Americas'];
                var dest = destinations[econRng.nextInt(destinations.length)];
                var alreadyExists = state.economy.tradeRoutes.some(function (r) { return r.to === dest; });
                if (!alreadyExists) {
                    state.economy.tradeRoutes.push({ from: state.capital, to: dest, goods: ['mixed'], volume: 200 + econRng.nextInt(800), tariff: 0.05 + econRng.nextDouble() * 0.15 });
                    events.push({ year: currentYear, type: 'TRADE_ROUTE_ESTABLISHED', severity: 'MINOR', description: 'New trade route established to ' + dest });
                }
            }

            // Economic boom/collapse
            if (econRng.nextDouble() < 0.02) {
                if (econRng.nextDouble() < 0.6) {
                    state.economy.wealth *= 1.15;
                    events.push({ year: currentYear, type: 'ECONOMIC_BOOM', severity: 'MAJOR', description: 'Economic boom increases wealth by 15%' });
                } else {
                    state.economy.wealth *= 0.75;
                    events.push({ year: currentYear, type: 'ECONOMIC_COLLAPSE', severity: 'CRITICAL', description: 'Economic collapse reduces wealth by 25%' });
                }
            }

            // --- Technology tick ---
            var techRng = yearRng.split('technology');
            var tradeConnectivity = getTradeConnectivity(state.economy);
            TECH_TREE.forEach(function (tech) {
                if (state.technology.unlockedTechs.indexOf(tech.id) >= 0) { return; }
                var prereqsMet = tech.prerequisites.every(function (p) {
                    return state.technology.unlockedTechs.indexOf(p) >= 0;
                });
                if (!prereqsMet) { return; }
                var progress = state.technology.researchProgress[tech.id] || 0;
                var researchRate = (state.technology.literacyRate * 0.5 + state.technology.universities * 0.05 + tradeConnectivity * 0.2) * step;
                progress += researchRate * tech.diffusionRate * 100;
                if (progress >= tech.researchCost) {
                    state.technology.unlockedTechs.push(tech.id);
                    delete state.technology.researchProgress[tech.id];
                    events.push({ year: currentYear, type: 'TECHNOLOGY_UNLOCKED', severity: 'MAJOR', description: 'Technology unlocked: ' + tech.id + ' (' + tech.era + ' era)' });
                } else {
                    state.technology.researchProgress[tech.id] = progress;
                }
            });

            // Literacy improves over time
            state.technology.literacyRate = clamp(state.technology.literacyRate + 0.0005 * step, 0, 1);
            // Universities grow with population
            if (state.population.population > 1000000 * (state.technology.universities + 1) && techRng.nextDouble() < 0.05) {
                state.technology.universities += 1;
            }

            // --- Religion tick ---
            var relRng = yearRng.split('religion');
            var shares = state.religion.religionShares;
            var shareKeys = Object.keys(shares);
            // Slight random drift
            shareKeys.forEach(function (key) {
                shares[key] = Math.max(0.01, shares[key] + (relRng.nextDouble() - 0.5) * 0.02);
            });
            // Normalize
            var totalShares = 0;
            shareKeys.forEach(function (key) { totalShares += shares[key]; });
            shareKeys.forEach(function (key) { shares[key] /= totalShares; });
            state.religion.religiousUnity = Math.max.apply(null, shareKeys.map(function (k) { return shares[k]; }));
            state.religion.stabilityBonus = state.religion.religiousUnity * 0.2;

            // Schism
            if (relRng.nextDouble() < 0.005 * step) {
                events.push({ year: currentYear, type: 'RELIGIOUS_SCHISM', severity: 'MAJOR', description: 'Religious schism fractures the dominant faith' });
                state.religion.religiousUnity = Math.max(0.3, state.religion.religiousUnity - 0.15);
            }

            // --- Politics tick ---
            var polRng = yearRng.split('politics');
            var economicHealth = Math.min(1.0, (state.economy.wealth / Math.max(1, state.population.population)) / 1000.0);
            var stabilityDelta = (economicHealth * 0.1 + state.religion.stabilityBonus * 0.1 - (state.military.atWar ? 0.15 : 0)) * step;
            state.politics.stability = clamp(state.politics.stability + stabilityDelta + (polRng.nextDouble() - 0.5) * 0.05, 0, 1);
            state.politics.rulerAge += step;

            // Succession crisis
            if (state.politics.rulerAge > 60 && polRng.nextDouble() < 0.1) {
                state.politics.inSuccessionCrisis = true;
                state.politics.rulerAge = 20 + polRng.nextInt(20);
                state.politics.stability = Math.max(0.1, state.politics.stability - 0.2);
                events.push({ year: currentYear, type: 'SUCCESSION_CRISIS', severity: 'MAJOR', description: 'Succession crisis destabilizes the government' });
            } else if (state.politics.inSuccessionCrisis && polRng.nextDouble() < 0.4) {
                state.politics.inSuccessionCrisis = false;
            }

            // Rebellion
            if (state.politics.stability < 0.25 && polRng.nextDouble() < 0.15) {
                state.politics.inRebellion = true;
                events.push({ year: currentYear, type: 'REBELLION', severity: 'CRITICAL', description: 'Rebellion erupts against the ruling government' });
            } else if (state.politics.inRebellion && polRng.nextDouble() < 0.3) {
                state.politics.inRebellion = false;
                events.push({ year: currentYear, type: 'GOVERNMENT_CHANGE', severity: 'MAJOR', description: 'Rebellion quelled, government stabilizes' });
                state.politics.stability = clamp(state.politics.stability + 0.2, 0, 1);
            }

            // --- Military tick ---
            var milRng = yearRng.split('military');
            state.military.armySize = Math.max(1000, Math.floor(state.population.population * 0.02));
            state.military.navySize = Math.max(100, Math.floor(state.military.armySize * 0.2));
            state.military.techAdvantage = 1.0 + state.technology.unlockedTechs.length * 0.1;

            // War
            if (!state.military.atWar && milRng.nextDouble() < scenario.worldConstraints.warFrequency * 0.03 * step) {
                var opponents = ['Parthia', 'Germania', 'Persia', 'Mongols', 'Ottomans', 'Huns', 'Vandals'];
                state.military.atWar = true;
                state.military.warOpponent = opponents[milRng.nextInt(opponents.length)];
                events.push({ year: currentYear, type: 'WAR_DECLARED', severity: 'CRITICAL', description: 'War declared against ' + state.military.warOpponent });
                state.politics.warExhaustion = clamp(state.politics.warExhaustion + 0.2, 0, 1);
            } else if (state.military.atWar && milRng.nextDouble() < 0.15) {
                events.push({ year: currentYear, type: 'WAR_ENDED', severity: 'MAJOR', description: 'War with ' + state.military.warOpponent + ' concluded' });
                state.military.atWar = false;
                state.military.warOpponent = null;
                state.politics.warExhaustion = Math.max(0, state.politics.warExhaustion - 0.3);
            }

            // Update year in state
            state.year = currentYear;

            // Snapshot for charts
            if (currentYear >= nextSnapshot) {
                snapshots.push({
                    year: currentYear,
                    population: state.population.population,
                    wealth: state.economy.wealth,
                    gdp: state.economy.gdp,
                    techs: state.technology.unlockedTechs.length,
                    stability: state.politics.stability,
                    literacy: state.technology.literacyRate
                });
                nextSnapshot += snapshotInterval;
            }

            currentYear += step;
        }

        // Final snapshot
        snapshots.push({
            year: state.year,
            population: state.population.population,
            wealth: state.economy.wealth,
            gdp: state.economy.gdp,
            techs: state.technology.unlockedTechs.length,
            stability: state.politics.stability,
            literacy: state.technology.literacyRate
        });

        var durationMs = performance.now() - startTime;

        return {
            finalState: state,
            events: events,
            snapshots: snapshots,
            durationMs: durationMs
        };
    }

    // ---------------------------------------------------------------
    // Public API
    // ---------------------------------------------------------------

    return {
        formatNumber: formatNumber,
        formatDecimal: formatDecimal,
        formatYear: formatYear,
        formatPercent: formatPercent,
        createRandom: createRandom,
        createRomeScenario: createRomeScenario,
        runSimulation: runSimulation,
        cloneState: cloneState,
        TECH_TREE: TECH_TREE,
        TECH_MAP: TECH_MAP
    };

})();
