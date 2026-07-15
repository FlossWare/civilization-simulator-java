package org.flossware.civilization.engine;

public record SimulationSnapshot(
    int year,
    long population,
    double wealth,
    double gdp,
    int techs,
    double stability,
    double literacy
) {}
