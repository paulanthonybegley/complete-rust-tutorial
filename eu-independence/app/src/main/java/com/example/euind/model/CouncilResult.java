package com.example.euind.model;

/**
 * Output of the European Security Council builder (see SimService).
 */
public record CouncilResult(int coveragePct, int balanceBonus, int total, String verdict) {
}