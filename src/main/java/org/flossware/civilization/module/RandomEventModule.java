package org.flossware.civilization.module;

import org.flossware.civilization.engine.TickContext;
import org.flossware.civilization.model.*;
import org.flossware.civilization.util.SeedManager;

import java.util.ArrayList;
import java.util.List;
import java.util.SplittableRandom;

public final class RandomEventModule implements SimulationModule {

    private static final int COOLDOWN_YEARS = 25;

    private static final double METEOR_PROB = 0.002;
    private static final double VOLCANIC_PROB = 0.005;
    private static final double PANDEMIC_PROB = 0.008;
    private static final double FAMINE_PROB = 0.015;
    private static final double FLOOD_PROB = 0.01;
    private static final double INVASION_PROB = 0.012;
    private static final double CIVIL_WAR_PROB = 0.02;
    private static final double GOLDEN_AGE_PROB = 0.01;
    private static final double RENAISSANCE_PROB = 0.008;
    private static final double MARRIAGE_PROB = 0.01;

    @Override
    public String moduleName() {
        return "random_event";
    }

    @Override
    public ModuleResult<?> tick(TickContext context) {
        var random = SeedManager.getModuleRandom(context.yearSeed(), moduleName());
        return tick(context.state(), context.scenario().worldConstraints().randomEventFrequency(), random);
    }

    public static ModuleResult<CivilizationState> tick(
            CivilizationState state, double frequency, SplittableRandom random) {

        if (frequency <= 0.0) {
            return new ModuleResult<>(state, List.of());
        }

        int currentYear = state.year();
        if ((long) currentYear - (long) state.randomEvent().lastEventYear() < COOLDOWN_YEARS) {
            return new ModuleResult<>(state, List.of());
        }

        double roll = random.nextDouble();
        double cumulative = 0.0;
        List<Event> events = new ArrayList<>();

        // Meteor Strike - no preconditions
        cumulative += METEOR_PROB * frequency;
        if (roll < cumulative) {
            return applyMeteorStrike(state, currentYear);
        }

        // Volcanic Eruption - no preconditions
        cumulative += VOLCANIC_PROB * frequency;
        if (roll < cumulative) {
            return applyVolcanicEruption(state, currentYear);
        }

        // Great Pandemic - requires pop > 1M and no active plague
        if (state.population().population() > 1_000_000 && !state.population().plagueActive()) {
            cumulative += PANDEMIC_PROB * frequency;
            if (roll < cumulative) {
                return applyGreatPandemic(state, currentYear);
            }
        }

        // Great Famine - requires high drought
        if (state.climate().droughtIndex() > 0.6) {
            cumulative += FAMINE_PROB * frequency;
            if (roll < cumulative) {
                return applyGreatFamine(state, currentYear);
            }
        }

        // Great Flood - requires high storm frequency
        if (state.climate().stormFrequency() > 1.5) {
            cumulative += FLOOD_PROB * frequency;
            if (roll < cumulative) {
                return applyGreatFlood(state, currentYear);
            }
        }

        // Foreign Invasion - weak military or low stability
        if (state.military().armySize() < state.population().population() * 0.01
                || state.politics().stability() < 0.3) {
            cumulative += INVASION_PROB * frequency;
            if (roll < cumulative) {
                return applyForeignInvasion(state, currentYear);
            }
        }

        // Civil War - low stability AND active rebellion
        if (state.politics().stability() < 0.3 && state.politics().inRebellion()) {
            cumulative += CIVIL_WAR_PROB * frequency;
            if (roll < cumulative) {
                return applyCivilWar(state, currentYear);
            }
        }

        // Golden Age - high stability, at peace, no rebellion
        if (state.politics().stability() > 0.6
                && !state.military().atWar()
                && !state.politics().inRebellion()) {
            cumulative += GOLDEN_AGE_PROB * frequency;
            if (roll < cumulative) {
                return applyGoldenAge(state, currentYear);
            }
        }

        // Renaissance - literate, has universities, wealthy
        if (state.technology().literacyRate() > 0.1
                && state.technology().universities() > 0
                && state.economy().wealth() > 5_000_000) {
            cumulative += RENAISSANCE_PROB * frequency;
            if (roll < cumulative) {
                return applyRenaissance(state, currentYear);
            }
        }

        // Diplomatic Marriage - at peace, moderate stability
        if (!state.military().atWar() && state.politics().stability() > 0.4) {
            cumulative += MARRIAGE_PROB * frequency;
            if (roll < cumulative) {
                return applyDiplomaticMarriage(state, currentYear);
            }
        }

        return new ModuleResult<>(state, List.of());
    }

    private static ModuleResult<CivilizationState> applyMeteorStrike(CivilizationState state, int year) {
        PopulationState pop = state.population();
        long newPop = Math.max(1000, (long) (pop.population() * 0.85));
        PopulationState newPopState = new PopulationState(
            newPop, pop.birthRate(), pop.deathRate(), pop.carryingCapacity(), pop.plagueActive());

        EconomyState eco = state.economy();
        double newWealth = Math.max(0, eco.wealth() * 0.75);
        EconomyState newEcoState = new EconomyState(
            newWealth, eco.production(), eco.consumption(), eco.workers(),
            eco.tradeSurplus(), eco.gdp(), eco.tradeRoutes());

        ClimateState clim = state.climate();
        ClimateState newClimState = new ClimateState(
            Math.min(10.0, clim.temperatureAnomaly() + 2.0),
            Math.min(1.0, clim.droughtIndex() + 0.3),
            clim.stormFrequency() + 1.0,
            clim.seaLevelRise_mm());

        CivilizationState newState = state
            .withPopulation(newPopState)
            .withEconomy(newEcoState)
            .withClimate(newClimState)
            .withRandomEvent(new RandomEventState(year));

        Event event = new Event(0, "", Event.EventType.METEOR_STRIKE, Event.EventSeverity.CRITICAL,
            "A devastating meteor strike causes widespread destruction", null);

        return new ModuleResult<>(newState, List.of(event));
    }

    private static ModuleResult<CivilizationState> applyVolcanicEruption(CivilizationState state, int year) {
        PopulationState pop = state.population();
        long newPop = Math.max(1000, (long) (pop.population() * 0.95));
        PopulationState newPopState = new PopulationState(
            newPop, pop.birthRate(), pop.deathRate(), pop.carryingCapacity(), pop.plagueActive());

        EconomyState eco = state.economy();
        double newWealth = Math.max(0, eco.wealth() * 0.90);
        EconomyState newEcoState = new EconomyState(
            newWealth, eco.production(), eco.consumption(), eco.workers(),
            eco.tradeSurplus(), eco.gdp(), eco.tradeRoutes());

        ClimateState clim = state.climate();
        ClimateState newClimState = new ClimateState(
            Math.max(-10.0, clim.temperatureAnomaly() - 1.5),
            Math.min(1.0, clim.droughtIndex() + 0.2),
            clim.stormFrequency(),
            clim.seaLevelRise_mm());

        CivilizationState newState = state
            .withPopulation(newPopState)
            .withEconomy(newEcoState)
            .withClimate(newClimState)
            .withRandomEvent(new RandomEventState(year));

        Event event = new Event(0, "", Event.EventType.VOLCANIC_ERUPTION, Event.EventSeverity.CRITICAL,
            "A massive volcanic eruption triggers a volcanic winter", null);

        return new ModuleResult<>(newState, List.of(event));
    }

    private static ModuleResult<CivilizationState> applyGreatPandemic(CivilizationState state, int year) {
        PopulationState pop = state.population();
        long newPop = Math.max(1000, (long) (pop.population() * 0.70));
        PopulationState newPopState = new PopulationState(
            newPop, pop.birthRate(), pop.deathRate(), pop.carryingCapacity(), true);

        EconomyState eco = state.economy();
        double newWealth = Math.max(0, eco.wealth() * 0.85);
        EconomyState newEcoState = new EconomyState(
            newWealth, eco.production(), eco.consumption(), eco.workers(),
            eco.tradeSurplus(), eco.gdp(), eco.tradeRoutes());

        ReligionState rel = state.religion();
        double newUnity = Math.max(0, rel.religiousUnity() - 0.1);
        ReligionState newRelState = new ReligionState(
            rel.religionShares(), newUnity, rel.stabilityBonus(), rel.spreadRate());

        CivilizationState newState = state
            .withPopulation(newPopState)
            .withEconomy(newEcoState)
            .withReligion(newRelState)
            .withRandomEvent(new RandomEventState(year));

        Event event = new Event(0, "", Event.EventType.GREAT_PANDEMIC, Event.EventSeverity.CRITICAL,
            "A great pandemic sweeps through the civilization, killing millions", null);

        return new ModuleResult<>(newState, List.of(event));
    }

    private static ModuleResult<CivilizationState> applyGreatFamine(CivilizationState state, int year) {
        PopulationState pop = state.population();
        long newPop = Math.max(1000, (long) (pop.population() * 0.80));
        PopulationState newPopState = new PopulationState(
            newPop, pop.birthRate(), pop.deathRate(), pop.carryingCapacity(), pop.plagueActive());

        EconomyState eco = state.economy();
        double newWealth = Math.max(0, eco.wealth() * 0.85);
        EconomyState newEcoState = new EconomyState(
            newWealth, eco.production(), eco.consumption(), eco.workers(),
            eco.tradeSurplus(), eco.gdp(), eco.tradeRoutes());

        PoliticsState pol = state.politics();
        double newStability = Math.max(0, pol.stability() - 0.2);
        PoliticsState newPolState = new PoliticsState(
            newStability, pol.government(), pol.rulerAge(),
            pol.warExhaustion(), pol.inRebellion(), pol.inSuccessionCrisis());

        CivilizationState newState = state
            .withPopulation(newPopState)
            .withEconomy(newEcoState)
            .withPolitics(newPolState)
            .withRandomEvent(new RandomEventState(year));

        Event event = new Event(0, "", Event.EventType.GREAT_FAMINE, Event.EventSeverity.CRITICAL,
            "Widespread famine devastates the population", null);

        return new ModuleResult<>(newState, List.of(event));
    }

    private static ModuleResult<CivilizationState> applyGreatFlood(CivilizationState state, int year) {
        PopulationState pop = state.population();
        long newPop = Math.max(1000, (long) (pop.population() * 0.92));
        PopulationState newPopState = new PopulationState(
            newPop, pop.birthRate(), pop.deathRate(), pop.carryingCapacity(), pop.plagueActive());

        EconomyState eco = state.economy();
        double newWealth = Math.max(0, eco.wealth() * 0.85);
        EconomyState newEcoState = new EconomyState(
            newWealth, eco.production(), eco.consumption(), eco.workers(),
            eco.tradeSurplus(), eco.gdp(), eco.tradeRoutes());

        ClimateState clim = state.climate();
        ClimateState newClimState = new ClimateState(
            clim.temperatureAnomaly(),
            clim.droughtIndex(),
            clim.stormFrequency() + 0.5,
            clim.seaLevelRise_mm());

        CivilizationState newState = state
            .withPopulation(newPopState)
            .withEconomy(newEcoState)
            .withClimate(newClimState)
            .withRandomEvent(new RandomEventState(year));

        Event event = new Event(0, "", Event.EventType.GREAT_FLOOD, Event.EventSeverity.MAJOR,
            "Catastrophic flooding destroys crops and settlements", null);

        return new ModuleResult<>(newState, List.of(event));
    }

    private static ModuleResult<CivilizationState> applyForeignInvasion(CivilizationState state, int year) {
        PopulationState pop = state.population();
        long newPop = Math.max(1000, (long) (pop.population() * 0.90));
        PopulationState newPopState = new PopulationState(
            newPop, pop.birthRate(), pop.deathRate(), pop.carryingCapacity(), pop.plagueActive());

        EconomyState eco = state.economy();
        double newWealth = Math.max(0, eco.wealth() * 0.80);
        EconomyState newEcoState = new EconomyState(
            newWealth, eco.production(), eco.consumption(), eco.workers(),
            eco.tradeSurplus(), eco.gdp(), eco.tradeRoutes());

        MilitaryState mil = state.military();
        long newArmy = Math.max(100, (long) (mil.armySize() * 0.60));
        MilitaryState newMilState = new MilitaryState(
            newArmy, mil.navySize(), mil.techAdvantage(), mil.logisticsScore(),
            true, "Foreign Horde");

        CivilizationState newState = state
            .withPopulation(newPopState)
            .withEconomy(newEcoState)
            .withMilitary(newMilState)
            .withRandomEvent(new RandomEventState(year));

        Event event = new Event(0, "", Event.EventType.FOREIGN_INVASION, Event.EventSeverity.CRITICAL,
            "A massive foreign invasion threatens the heartland", null);

        return new ModuleResult<>(newState, List.of(event));
    }

    private static ModuleResult<CivilizationState> applyCivilWar(CivilizationState state, int year) {
        PopulationState pop = state.population();
        long newPop = Math.max(1000, (long) (pop.population() * 0.88));
        PopulationState newPopState = new PopulationState(
            newPop, pop.birthRate(), pop.deathRate(), pop.carryingCapacity(), pop.plagueActive());

        MilitaryState mil = state.military();
        long newArmy = Math.max(100, (long) (mil.armySize() * 0.70));
        MilitaryState newMilState = new MilitaryState(
            newArmy, mil.navySize(), mil.techAdvantage(), mil.logisticsScore(),
            mil.atWar(), mil.warOpponent());

        PoliticsState pol = state.politics();
        double newStability = Math.max(0, pol.stability() - 0.25);
        double newExhaustion = Math.min(1.0, pol.warExhaustion() + 0.3);
        PoliticsState newPolState = new PoliticsState(
            newStability, pol.government(), pol.rulerAge(),
            newExhaustion, pol.inRebellion(), pol.inSuccessionCrisis());

        CivilizationState newState = state
            .withPopulation(newPopState)
            .withMilitary(newMilState)
            .withPolitics(newPolState)
            .withRandomEvent(new RandomEventState(year));

        Event event = new Event(0, "", Event.EventType.CIVIL_WAR, Event.EventSeverity.CRITICAL,
            "Civil war erupts as factions battle for control", null);

        return new ModuleResult<>(newState, List.of(event));
    }

    private static ModuleResult<CivilizationState> applyGoldenAge(CivilizationState state, int year) {
        EconomyState eco = state.economy();
        double newWealth = eco.wealth() * 1.30;
        EconomyState newEcoState = new EconomyState(
            newWealth, eco.production(), eco.consumption(), eco.workers(),
            eco.tradeSurplus(), eco.gdp(), eco.tradeRoutes());

        TechnologyState tech = state.technology();
        double newLiteracy = Math.min(1.0, tech.literacyRate() + 0.1);
        TechnologyState newTechState = new TechnologyState(
            tech.unlockedTechs(), tech.researchProgress(), newLiteracy, tech.universities());

        PoliticsState pol = state.politics();
        double newStability = Math.min(1.0, pol.stability() + 0.15);
        PoliticsState newPolState = new PoliticsState(
            newStability, pol.government(), pol.rulerAge(),
            pol.warExhaustion(), pol.inRebellion(), pol.inSuccessionCrisis());

        CivilizationState newState = state
            .withEconomy(newEcoState)
            .withTechnology(newTechState)
            .withPolitics(newPolState)
            .withRandomEvent(new RandomEventState(year));

        Event event = new Event(0, "", Event.EventType.GOLDEN_AGE, Event.EventSeverity.MAJOR,
            "A golden age of prosperity and cultural achievement begins", null);

        return new ModuleResult<>(newState, List.of(event));
    }

    private static ModuleResult<CivilizationState> applyRenaissance(CivilizationState state, int year) {
        TechnologyState tech = state.technology();
        double newLiteracy = Math.min(1.0, tech.literacyRate() + 0.15);
        TechnologyState newTechState = new TechnologyState(
            tech.unlockedTechs(), tech.researchProgress(), newLiteracy, tech.universities() + 1);

        EconomyState eco = state.economy();
        double newProduction = eco.production() * 1.20;
        EconomyState newEcoState = new EconomyState(
            eco.wealth(), newProduction, eco.consumption(), eco.workers(),
            eco.tradeSurplus(), eco.gdp(), eco.tradeRoutes());

        CivilizationState newState = state
            .withTechnology(newTechState)
            .withEconomy(newEcoState)
            .withRandomEvent(new RandomEventState(year));

        Event event = new Event(0, "", Event.EventType.RENAISSANCE, Event.EventSeverity.MAJOR,
            "A renaissance of learning and innovation transforms society", null);

        return new ModuleResult<>(newState, List.of(event));
    }

    private static ModuleResult<CivilizationState> applyDiplomaticMarriage(CivilizationState state, int year) {
        PoliticsState pol = state.politics();
        double newStability = Math.min(1.0, pol.stability() + 0.15);
        double newExhaustion = Math.max(0, pol.warExhaustion() - 0.1);
        PoliticsState newPolState = new PoliticsState(
            newStability, pol.government(), pol.rulerAge(),
            newExhaustion, pol.inRebellion(), pol.inSuccessionCrisis());

        EconomyState eco = state.economy();
        double newWealth = eco.wealth() * 1.10;
        EconomyState newEcoState = new EconomyState(
            newWealth, eco.production(), eco.consumption(), eco.workers(),
            eco.tradeSurplus(), eco.gdp(), eco.tradeRoutes());

        MilitaryState mil = state.military();
        long newArmy = (long) (mil.armySize() * 1.10);
        MilitaryState newMilState = new MilitaryState(
            newArmy, mil.navySize(), mil.techAdvantage(), mil.logisticsScore(),
            mil.atWar(), mil.warOpponent());

        CivilizationState newState = state
            .withPolitics(newPolState)
            .withEconomy(newEcoState)
            .withMilitary(newMilState)
            .withRandomEvent(new RandomEventState(year));

        Event event = new Event(0, "", Event.EventType.DIPLOMATIC_MARRIAGE, Event.EventSeverity.MAJOR,
            "A diplomatic marriage forges a powerful alliance", null);

        return new ModuleResult<>(newState, List.of(event));
    }

    @Override
    @SuppressWarnings("unchecked")
    public CivilizationState applyResult(CivilizationState state, ModuleResult<?> result) {
        return ((ModuleResult<CivilizationState>) result).state();
    }
}
