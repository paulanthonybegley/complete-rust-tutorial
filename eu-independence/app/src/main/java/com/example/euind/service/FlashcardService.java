package com.example.euind.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.example.euind.model.Category;
import com.example.euind.model.Flashcard;

/**
 * Flip-card definitions covering the vocabulary of the speech.
 */
@Service
public class FlashcardService {

	private final List<Flashcard> cards = List.of(
			f("Decoupling", "Reducing reliance on volatile external powers — Russia (energy), China (materials), "
					+ "the US (security & tech).", Category.SPEECH),
			f("Strongest, yet precarious", "The speech's opening paradox: record strength and record "
					+ "precariousness at the same time.", Category.SPEECH),
			f("The 28th regime (EU Inc)", "A proposed common company-law regime to help firms scale across "
					+ "borders; met union resistance.", Category.ECONOMY),
			f("Gold-plating", "National governments adding extra requirements on top of EU law — the 'pact "
					+ "against gold-plating' pushes back.", Category.ECONOMY),
			f("16%", "The share of euro-area corporate lending that crosses a border — the fragmentation "
					+ "stat.", Category.ECONOMY),
			f("Omnibus simplification", "12 Commission packages cutting ~€17bn a year in administrative "
					+ "costs.", Category.ECONOMY),
			f("Critical raw materials", "Rare earths and strategic minerals that power EVs, wind turbines, "
					+ "smartphones and defence — ~90% imported from China.", Category.RAW_MATERIALS),
			f("Strategic stockpiles", "Crunch reserves of critical materials coordinated by a European "
					+ "body, for crisis use.", Category.RAW_MATERIALS),
			f("Article 4 (NATO)", "Lets any member call a formal consultation when its security is "
					+ "threatened — threats, not attacks.", Category.DEFENCE),
			f("Emergency Security Protocol", "The EU's proposed Article 4 counterpart: one member triggers a "
					+ "union-wide coordination against hybrid threats.", Category.DEFENCE),
			f("Counter-hybrid playbook", "An agreed rulebook defining hybrid attacks and who responds, to "
					+ "deter further sabotage.", Category.DEFENCE),
			f("European Security Council", "A proposed leaders' format (FR, DE, IT, ES, PL as permanent) "
					+ "with partners like the UK, Norway, Canada and Ukraine.", Category.DEFENCE),
			f("Strategic enablers", "The expensive systems states can't scale alone: missile defence, "
					+ "airlift, refuelling, space, cyber.", Category.DEFENCE),
			f("2× electricity by 2040", "Doubling electricity's energy share, cutting the fossil import "
					+ "bill by ~€260bn/yr.", Category.ENERGY),
			f("6× the 80 GW", "Six times the renewables Europe added last year is still waiting for a grid "
					+ "connection.", Category.ENERGY),
			f("Associate membership", "A proposed halfway house between a trade deal and full membership; "
					+ "Canada is the first candidate — the category doesn't exist yet.", Category.PARTNERSHIPS),
			f("Middle Corridor", "€12bn Global Gateway investment to triple flows on the Central Asia → "
					+ "Europe trade route by 2030.", Category.PARTNERSHIPS),
			f("€1bn a day", "The EU's trade deficit with China — combined with rare earths, the biggest "
					+ "single dependency.", Category.PARTNERSHIPS),
			f("Pacing the frontier", "Frontier-lab conversations about coordinating how fast AI models "
					+ "develop.", Category.AI),
			f("157 sources", "How many outlets were already reporting on the speech when the video was "
					+ "made — with left/right frames diverging.", Category.MEDIA));

	public List<Flashcard> all() {
		return cards;
	}

	public List<Flashcard> byCategory(Category category) {
		return cards.stream().filter(c -> c.category() == category).toList();
	}

	private static Flashcard f(String front, String back, Category category) {
		return new Flashcard(front, back, category);
	}
}