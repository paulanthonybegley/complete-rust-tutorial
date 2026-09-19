package com.example.euind.service;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.springframework.stereotype.Service;

import com.example.euind.model.CouncilMember;
import com.example.euind.model.CouncilResult;
import com.example.euind.model.EnergyResult;
import com.example.euind.model.ExposureGrade;
import com.example.euind.model.MaterialsResult;

/**
 * The three in-app simulators behind /lab. The models are deliberately simple
 * and transparent so learners can reason about *why* a number changes.
 */
@Service
public class SimService {

	private static final double TODAY_ELECTRICITY_SHARE = 22.0;
	private static final double TARGET_ELECTRICITY_SHARE = 44.0;
	private static final long MAX_SAVING_BILLION = 260L;
	private static final long TOTAL_GRID_BACKLOG_MW = 480_000L;
	private static final long EU_POPULATION = 447_000_000L;

	private final PartnerService partners;

	public SimService(PartnerService partners) {
		this.partners = partners;
	}

	/* ------------------------------------------------------------------ */
	/* Energy: drag the 2040 target and watch the import bill + grid.      */
	/* ------------------------------------------------------------------ */
	public EnergyResult energy(double targetShare) {
		double clamped = Math.min(70.0, Math.max(15.0, targetShare));
		double progress = (clamped - TODAY_ELECTRICITY_SHARE) / (TARGET_ELECTRICITY_SHARE - TODAY_ELECTRICITY_SHARE);
		long saving = Math.round(MAX_SAVING_BILLION * Math.max(0, progress));
		long backlog = Math.round(TOTAL_GRID_BACKLOG_MW * Math.max(0, 1 - progress));
		return new EnergyResult(clamped, saving, clamped >= TARGET_ELECTRICITY_SHARE, backlog);
	}

	/* ------------------------------------------------------------------ */
	/* Materials: drag China dependency and watch exposure + stockpile.    */
	/* ------------------------------------------------------------------ */
	public MaterialsResult materials(double chinaSharePct) {
		double share = Math.min(95.0, Math.max(40.0, chinaSharePct));
		ExposureGrade grade;
		if (share >= 90) {
			grade = ExposureGrade.CRITICAL;
		} else if (share >= 80) {
			grade = ExposureGrade.SEVERE;
		} else if (share >= 65) {
			grade = ExposureGrade.ELEVATED;
		} else {
			grade = ExposureGrade.MANAGED;
		}
		int stockpileDays = Math.min(365, (int) Math.round((100 - share) * 8));
		long impact = Math.round(share * 0.9);
		return new MaterialsResult(share, grade, stockpileDays, impact);
	}

	/* ------------------------------------------------------------------ */
	/* Council: pick permanent + rotating members, partners optional.      */
	/* ------------------------------------------------------------------ */
	public CouncilResult council(Set<String> selectedRotating, Set<String> selectedPartners) {
		Set<String> rotating = selectedRotating == null ? Set.of() : new HashSet<>(selectedRotating);
		Set<String> ext = selectedPartners == null ? Set.of() : new HashSet<>(selectedPartners);

		long coveredPopulation = 0;
		boolean hasEast = false;
		boolean hasNorth = false;
		int smallStates = 0;

		for (CouncilMember m : partners.councilMembers()) {
			boolean keep = m.permanent() || rotating.contains(m.id());
			if (!keep) {
				continue;
			}
			coveredPopulation += m.population();
			hasEast |= m.region() == CouncilMember.Region.EUROPE_EAST;
			hasNorth |= m.region() == CouncilMember.Region.EUROPE_NORTH;
			if (m.isSmall()) {
				smallStates++;
			}
		}

		int coveragePct = (int) Math.round(100.0 * coveredPopulation / EU_POPULATION);

		int bonus = 0;
		if (hasEast) {
			bonus += 10;
		}
		if (hasNorth) {
			bonus += 5;
		}
		bonus += Math.min(10, smallStates * 5);
		bonus += Math.min(10, ext.size() * 5);

		int total = Math.min(100, coveragePct + bonus);
		String verdict;
		if (total >= 85) {
			verdict = "Fully legitimate Council — every region is represented.";
		} else if (total >= 70) {
			verdict = "Balanced Council — credible and representative.";
		} else if (total >= 50) {
			verdict = "Working Council — coverage, but gaps remain.";
		} else {
			verdict = "Minimal Council — you represent only a minority of Europeans.";
		}
		return new CouncilResult(coveragePct, bonus, total, verdict);
	}

	/* Optional non-EU partners for the council builder. */
	public List<CouncilMember> externalPartners() {
		return partners.partners().stream()
				.filter(p -> List.of("Canada", "Ukraine", "Norway", "UK").contains(p.country()))
				.map(p -> new CouncilMember(p.country().toLowerCase(), p.country(), p.flag(), 0,
						CouncilMember.Region.EUROPE_WEST, false, p.status()))
				.toList();
	}
}