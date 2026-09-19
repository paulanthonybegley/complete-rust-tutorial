package com.example.euind.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.example.euind.model.Category;
import com.example.euind.model.QuizQuestion;

/**
 * Self-grading quiz questions covering every theme in the speech.
 */
@Service
public class QuizService {

	private final List<QuizQuestion> questions = List.of(
			q("q1", "What share of the EU's critical raw-material imports come from China?",
					List.of("~30%", "~90%", "~55%", "~12%"), 1, Category.RAW_MATERIALS,
					"The video: China accounts for about 90% of the EU's critical-material imports — rising to "
						+ "90%+ for some rare earths."),
			q("q2", "Cross-border corporate lending in the euro area stands at about...",
					List.of("16%", "48%", "72%", "90%"), 0, Category.ECONOMY,
					"Only ~16% of corporate lending crosses a border — the fragmentation the single-market "
						+ "overhaul targets."),
			q("q3", "NATO's Article 4 lets a member...",
					List.of("Demand military action", "Call a formal consultation when it feels threatened",
							"Automatically trigger collective defence", "Veto a NATO decision"),
					1, Category.DEFENCE,
					"Article 4 allows a formal meeting — it is about threats, not attacks (that is Article 5). "
						+ "The EU is building its own counterpart."),
			q("q4", "Doubling electricity's share of energy use by 2040 could cut the fossil import bill by...",
					List.of("€26bn a year", "€90bn a year", "€260bn a year", "€2.6bn a year"), 2, Category.ENERGY,
					"The Commission estimates ~€260 billion a year at the target."),
			q("q5", "Which country was invited to explore becoming the EU's first associate member?",
					List.of("Ukraine", "Canada", "Australia", "Norway"), 1, Category.PARTNERSHIPS,
					"Canada — a status first pitched for Ukraine, that does not formally exist yet."),
			q("q6", "Europe is heating up how much faster than the global average?",
					List.of("About the same", "Twice as fast", "Five times as fast", "Half as fast"), 1,
					Category.CLIMATE,
					"Europe is warming about twice as fast as the rest of the world — the speech's 'grim "
						+ "warning'."),
			q("q7", "The €90bn of joint debt was used to...",
					List.of("Build the Middle Corridor", "Fund Ukraine support after the frozen-Russia-assets plan "
							+ "stalled",
							"Build AI gigafactories", "Buy grid storage"), 1, Category.DEFENCE,
					"It plugged the Ukraine-support gap when the frozen-assets mechanism was blocked."),
			q("q8", "Industrial AI deployment is concentrated in how many priority sectors?",
					List.of("Two", "Three", "Five", "Ten"), 2, Category.AI,
					"Five sectors: health, transport, agri-food, advanced manufacturing, defence & space."),
			q("q9", "The EU's trade deficit with China runs to about...",
					List.of("€1 million a day", "€1 trillion a year", "€1 billion a day", "€10 billion a week"), 2,
					Category.PARTNERSHIPS,
					"About €1 billion a day (≈ €365 billion a year)."),
			q("q10", "Which of these is NOT part of the critical raw materials response?",
					List.of("Joint procurement", "Strategic stockpiles", "A European Corporation on Critical Raw "
							+ "Materials", "Tariffs on all Chinese goods"), 3, Category.RAW_MATERIALS,
					"The response is procurement, stockpiles and a European corporation — blanket tariffs were "
						+ "not proposed."),
			q("q11", "The counter-hybrid playbook would define...",
					List.of("NATO's collective defence trigger", "What counts as a hybrid attack and who responds",
							"EU electricity targets", "Associate membership rules"),
					1, Category.DEFENCE,
					"It defines hybrid attacks, institutional responsibilities, and coordinated deterrence."),
			q("q12", "How many omnibus simplification packages did the Commission announce?",
					List.of("4", "8", "12", "20"), 2, Category.ECONOMY,
					"Twelve omnibus packages, saving about €17 billion a year in administrative costs."));

	public List<QuizQuestion> all() {
		return questions;
	}

	public QuizQuestion byId(String id) {
		return questions.stream().filter(q -> q.id().equals(id)).findFirst().orElse(null);
	}

	public boolean isCorrect(QuizQuestion question, String choice) {
		if (question == null || choice == null) {
			return false;
		}
		return choice.equals(question.options().get(question.answerIndex()));
	}

	private static QuizQuestion q(String id, String prompt, List<String> options, int answer, Category category,
			String explanation) {
		return new QuizQuestion(id, prompt, options, answer, category, explanation);
	}
}