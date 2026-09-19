package com.example.euind.model;

/**
 * Output of the critical-materials exposure simulator (see SimService).
 */
public record MaterialsResult(double chinaSharePct, ExposureGrade grade, int stockpileDays, double conditionalImpact) {
}