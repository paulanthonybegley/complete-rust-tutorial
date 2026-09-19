package com.example.euind.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.example.euind.model.CouncilMember;
import com.example.euind.model.Partner;
import com.example.euind.model.PartnerKind;

/**
 * The /partners explorer: associate-member candidates, trade agreements and
 * observing "Euro-curious" countries, plus the pool from which the Security
 * Council builder in /lab draws.
 */
@Service
public class PartnerService {

	private final List<Partner> partners = List.of(
			new Partner("Canada", "🇨🇦", PartnerKind.ASSOCIATE, "Invited to explore",
					"First proposed associate member; already participates in the €150bn SAFE defence "
						+ "instrument. Covers defence, minerals, energy, AI, quantum, cyber and the Arctic."),
			new Partner("Ukraine", "🇺🇦", PartnerKind.ASSOCIATE, "Original pitch, now defence-integrated",
					"Associate membership was first pitched to bring Ukraine closer in lieu of membership; "
						+ "now placed inside EU defence-industrial planning."),
			new Partner("India", "🇮🇳", PartnerKind.TRADE, "Deal signed",
					"Part of the flurry of trade deals to diversify supply chains."),
			new Partner("Mercosur", "🇧🇷", PartnerKind.TRADE, "Deal signed",
					"Latin America block deal — more diversified trade."),
			new Partner("Mexico", "🇲🇽", PartnerKind.TRADE, "Deal advanced",
					"One of several diversification agreements."),
			new Partner("Australia", "🇦🇺", PartnerKind.TRADE, "Door opened",
					"The Parliament president opened the door to Australia."),
			new Partner("New Zealand", "🇳🇿", PartnerKind.TRADE, "Door opened",
					"The Parliament president opened the door to New Zealand."),
			new Partner("Norway", "🇳🇴", PartnerKind.OBSERVER, "Euro-curious observer",
					"One of the countries the video says may observe with interest."),
			new Partner("Iceland", "🇮🇸", PartnerKind.OBSERVER, "Euro-curious observer",
					"Alongside Norway, watching the associate-membership process."),
			new Partner("UK", "🇬🇧", PartnerKind.OBSERVER, "Euro-curious observer",
					"Named by the video alongside Norway and Iceland."));

	private final List<CouncilMember> councilMembers = List.of(
			m("fr", "France", "🇫🇷", 68_000_000, CouncilMember.Region.EUROPE_WEST, true,
					"Permanent member mentioned by the video"),
			m("de", "Germany", "🇩🇪", 84_000_000, CouncilMember.Region.EUROPE_WEST, true,
					"Permanent member mentioned by the video"),
			m("it", "Italy", "🇮🇹", 59_000_000, CouncilMember.Region.EUROPE_SOUTH, true,
					"Permanent member mentioned by the video"),
			m("es", "Spain", "🇪🇸", 48_000_000, CouncilMember.Region.EUROPE_SOUTH, true,
					"Permanent member mentioned by the video"),
			m("pl", "Poland", "🇵🇱", 38_000_000, CouncilMember.Region.EUROPE_EAST, true,
					"Permanent member mentioned by the video"),
			m("nl", "Netherlands", "🇳🇱", 18_000_000, CouncilMember.Region.EUROPE_WEST, false, "Rotating pool"),
			m("be", "Belgium", "🇧🇪", 12_000_000, CouncilMember.Region.EUROPE_WEST, false, "Rotating pool"),
			m("lu", "Luxembourg", "🇱🇺", 700_000, CouncilMember.Region.EUROPE_WEST, false, "Small state"),
			m("ie", "Ireland", "🇮🇪", 5_000_000, CouncilMember.Region.EUROPE_WEST, false, "Small state"),
			m("at", "Austria", "🇦🇹", 9_000_000, CouncilMember.Region.EUROPE_CENTRAL, false, "Central"),
			m("si", "Slovenia", "🇸🇮", 2_100_000, CouncilMember.Region.EUROPE_CENTRAL, false, "Small state"),
			m("cz", "Czechia", "🇨🇿", 11_000_000, CouncilMember.Region.EUROPE_EAST, false, "Eastern"),
			m("sk", "Slovakia", "🇸🇰", 5_400_000, CouncilMember.Region.EUROPE_EAST, false, "Eastern small state"),
			m("hu", "Hungary", "🇭🇺", 9_600_000, CouncilMember.Region.EUROPE_EAST, false, "Eastern"),
			m("ro", "Romania", "🇷🇴", 19_000_000, CouncilMember.Region.EUROPE_EAST, false, "Eastern"),
			m("bg", "Bulgaria", "🇧🇬", 6_400_000, CouncilMember.Region.EUROPE_EAST, false, "Eastern small state"),
			m("lv", "Latvia", "🇱🇻", 1_900_000, CouncilMember.Region.EUROPE_EAST, false, "Eastern small state"),
			m("lt", "Lithuania", "🇱🇹", 2_800_000, CouncilMember.Region.EUROPE_EAST, false, "Eastern small state"),
			m("ee", "Estonia", "🇪🇪", 1_300_000, CouncilMember.Region.EUROPE_EAST, false, "Eastern small state"),
			m("gr", "Greece", "🇬🇷", 10_000_000, CouncilMember.Region.EUROPE_SOUTH, false, "Southern"),
			m("pt", "Portugal", "🇵🇹", 10_000_000, CouncilMember.Region.EUROPE_SOUTH, false, "Southern"),
			m("cy", "Cyprus", "🇨🇾", 1_000_000, CouncilMember.Region.EUROPE_SOUTH, false, "Small island state"),
			m("mt", "Malta", "🇲🇹", 500_000, CouncilMember.Region.EUROPE_SOUTH, false, "Small island state"),
			m("hr", "Croatia", "🇭🇷", 3_800_000, CouncilMember.Region.EUROPE_SOUTH, false, "Southern"),
			m("se", "Sweden", "🇸🇪", 10_000_000, CouncilMember.Region.EUROPE_NORTH, false, "Nordic"),
			m("fi", "Finland", "🇫🇮", 6_000_000, CouncilMember.Region.EUROPE_NORTH, false, "Nordic"),
			m("dk", "Denmark", "🇩🇰", 6_000_000, CouncilMember.Region.EUROPE_NORTH, false, "Nordic"));

	public List<Partner> partners() {
		return partners;
	}

	public List<Partner> byKind(PartnerKind kind) {
		return partners.stream().filter(p -> p.kind() == kind).toList();
	}

	public List<CouncilMember> councilMembers() {
		return councilMembers;
	}

	public List<CouncilMember> permanentMembers() {
		return councilMembers.stream().filter(CouncilMember::permanent).toList();
	}

	public List<CouncilMember> rotatingMembers() {
		return councilMembers.stream().filter(c -> !c.permanent()).toList();
	}

	private static CouncilMember m(String id, String country, String flag, long pop, CouncilMember.Region region,
			boolean permanent, String rationale) {
		return new CouncilMember(id, country, flag, pop, region, permanent, rationale);
	}
}