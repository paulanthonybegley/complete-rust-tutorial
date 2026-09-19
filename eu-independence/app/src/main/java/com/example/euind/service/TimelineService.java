package com.example.euind.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.example.euind.model.Category;
import com.example.euind.model.TimelineEvent;

/**
 * The "last year → the speech → what's next" story the video tells, as
 * clickable/filterable events.
 */
@Service
public class TimelineService {

	private final List<TimelineEvent> events = List.of(
			new TimelineEvent("Sep 2025", "SOTEU 2025: defence & competitiveness", Category.SPEECH,
					"Last year's speech set the two big themes. This year the EU reports back on both."),
			new TimelineEvent("During 2026", "€1.5bn Defence Industry Programme launched", Category.DEFENCE,
					"A European defence industry programme to build scale and common procurement."),
			new TimelineEvent("During 2026", "€325m for five joint defence projects", Category.DEFENCE,
					"Five joint projects earmarked, plus mechanisms for common procurement and planning."),
			new TimelineEvent("During 2026", "Frozen Russian assets deal blocked", Category.DEFENCE,
					"The EU could not agree on using frozen Russian assets to fund Ukraine support."),
			new TimelineEvent("During 2026", "€90bn of joint debt plugs the gap", Category.DEFENCE,
					"Joint borrowing kept support flowing after the assets plan stalled."),
			new TimelineEvent("During 2026", "Single market overhaul crawling", Category.ECONOMY,
					"The overhaul moves slowly; EU Inc meets union resistance; Savings & Investments Union is "
						+ "preliminary."),
			new TimelineEvent("During 2026", "80 GW of renewables added", Category.ENERGY,
					"Europe added 80 GW of renewables — about six times that is still waiting for a grid "
						+ "connection."),
			new TimelineEvent("16 Sep 2026", "SOTEU 2026: the independence thesis", Category.SPEECH,
					"Von der Leyen's sixth annual speech: 'strongest yet precarious', organised around "
						+ "independence from Russia, China and the US."),
			new TimelineEvent("16 Sep 2026", "Canada invited as first associate member", Category.PARTNERSHIPS,
					"A role first pitched for Ukraine; no such formal status exists yet."),
			new TimelineEvent("16 Sep 2026", "Emergency Security Protocol announced", Category.DEFENCE,
					"An EU counterpart to NATO Article 4 plus a counter-hybrid playbook."),
			new TimelineEvent("16 Sep 2026", "Strategic Enablers instrument flagged", Category.DEFENCE,
					"A new instrument for missile defence, strategic transport, refuelling, space and cyber."),
			new TimelineEvent("16 Sep 2026", "Climate resilience & AI pushes announced", Category.CLIMATE,
					"A Climate Resilience Framework (100 territories), insurance alliance, and industrial AI in "
						+ "five sectors due from November."),
			new TimelineEvent("Next month", "European security strategy expected", Category.DEFENCE,
					"The security architecture developed from the speech, including the new European Security "
						+ "Council ideas."),
			new TimelineEvent("By 2027", "Single market overhaul complete", Category.ECONOMY,
					"The stated target for finishing the one-Europe-one-market overhaul."),
			new TimelineEvent("By 2030", "Middle Corridor flows tripled", Category.PARTNERSHIPS,
					"€12bn Global Gateway investment targeting triple flows and shorter transit to Europe."),
			new TimelineEvent("By 2040", "Double electricity's share", Category.ENERGY,
					"The 2040 target that could cut the fossil import bill by ~€260bn a year."));

	public List<TimelineEvent> all() {
		return events;
	}

	public List<TimelineEvent> byCategory(Category category) {
		return events.stream().filter(e -> e.category() == category).toList();
	}
}