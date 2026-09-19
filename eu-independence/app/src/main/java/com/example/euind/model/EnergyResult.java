package com.example.euind.model;

/**
 * Output of the energy-to-2040 simulator (see SimService).
 */
public record EnergyResult(double targetShare, long importSavingBillion, boolean atTarget, long gridBacklogMW) {
}