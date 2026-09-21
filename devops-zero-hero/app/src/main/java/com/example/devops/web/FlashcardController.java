package com.example.devops.web;

import java.security.SecureRandom;
import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.example.devops.model.Category;
import com.example.devops.model.Flashcard;
import com.example.devops.service.FlashcardService;

/**
 * Flashcard decks: the page renders every card and a spotlight card; htmx
 * re-filters the grid by theme and shuffles a fresh spotlight card.
 */
@Controller
public class FlashcardController {

	private static final SecureRandom RANDOM = new SecureRandom();

	private final FlashcardService cards;

	public FlashcardController(FlashcardService cards) {
		this.cards = cards;
	}

	@GetMapping("/flashcards")
	public String page(@RequestParam(required = false) Category cat, Model model) {
		grid(cat, model);
		model.addAttribute("card", pick(cards.all()));
		return "flashcards";
	}

	@GetMapping("/flashcards/grid")
	public String grid(@RequestParam(required = false) Category cat, Model model) {
		List<Flashcard> deck = cat == null ? cards.all() : cards.byCategory(cat);
		model.addAttribute("deck", deck);
		model.addAttribute("selected", cat);
		return "_fragments :: flashcardGrid";
	}

	@GetMapping("/flashcards/next")
	public String next(@RequestParam(required = false) Category cat, Model model) {
		List<Flashcard> deck = cat == null ? cards.all() : cards.byCategory(cat);
		model.addAttribute("card", pick(deck));
		return "_fragments :: flashcardCard";
	}

	private Flashcard pick(List<Flashcard> deck) {
		return deck.get(RANDOM.nextInt(deck.size()));
	}
}