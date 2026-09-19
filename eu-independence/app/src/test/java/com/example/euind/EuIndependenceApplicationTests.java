package com.example.euind;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;

/**
 * The course must actually deliver every claimed feature to the browser.
 * These tests treat the rendered HTML as the contract: pages, htmx
 * fragments, the sim models and the self-grading quiz.
 */
@SpringBootTest
@AutoConfigureMockMvc
class EuIndependenceApplicationTests {

	@Autowired
	private MockMvc mockMvc;

	/* ---------------------------------------------------------- pages */

	@Test
	void dashboardShowsThesisAndMeter() throws Exception {
		mockMvc.perform(get("/"))
				.andExpect(status().isOk())
				.andExpect(content().string(containsString("Strongest")))
				.andExpect(content().string(containsString("precarious")))
				.andExpect(content().string(containsString("Independence meter")));
	}

	@Test
	void policyExplorerListsCards() throws Exception {
		mockMvc.perform(get("/policies"))
				.andExpect(status().isOk())
				.andExpect(content().string(containsString("Policy explorer")))
				.andExpect(content().string(containsString("EU&#39;s Article 4")));
	}

	@Test
	void policyGridFiltersByTheme() throws Exception {
		mockMvc.perform(get("/policies/grid").param("cat", "ENERGY"))
				.andExpect(status().isOk())
				.andExpect(content().string(containsString("Double electricity")))
				.andExpect(content().string(not(containsString("The 90% problem"))));
	}

	@Test
	void policyDetailDrawsFigures() throws Exception {
		mockMvc.perform(get("/policies/detail").param("id", "raw-china"))
				.andExpect(status().isOk())
				.andExpect(content().string(containsString("The 90% problem")))
				.andExpect(content().string(containsString("90%")));
	}

	@Test
	void policyDetailOpensTheDrawerForHtmx() throws Exception {
		mockMvc.perform(get("/policies/detail").param("id", "def-council"))
				.andExpect(status().isOk())
				.andExpect(content().string(containsString("class=\"drawer open\"")));
	}

	@Test
	void unknownPolicyDetailReturnsMissedCard() throws Exception {
		mockMvc.perform(get("/policies/detail").param("id", "nope"))
				.andExpect(status().isOk())
				.andExpect(content().string(containsString("No policy card")));
	}

	@Test
	void timelineGridFiltersByTheme() throws Exception {
		mockMvc.perform(get("/timeline/grid").param("cat", "DEFENCE"))
				.andExpect(status().isOk())
				.andExpect(content().string(containsString("€90bn")))
				.andExpect(content().string(not(containsString("Double electricity"))));
	}

	@Test
	void sourcesPageCarriesTheFramingTest() throws Exception {
		mockMvc.perform(get("/sources"))
				.andExpect(status().isOk())
				.andExpect(content().string(containsString("framing test")))
				.andExpect(content().string(containsString("157")));
	}

	@Test
	void partnersPublishGroups() throws Exception {
		mockMvc.perform(get("/partners"))
				.andExpect(status().isOk())
				.andExpect(content().string(containsString("Canada")))
				.andExpect(content().string(containsString("🇨🇦")));
	}

	@Test
	void partnersFilterByKind() throws Exception {
		mockMvc.perform(get("/partners/group").param("kind", "OBSERVER"))
				.andExpect(status().isOk())
				.andExpect(content().string(containsString("Norway")))
				.andExpect(content().string(not(containsString("Invited to explore"))));
	}

	@Test
	void flashcardsServeDeckAndSpotlight() throws Exception {
		mockMvc.perform(get("/flashcards"))
				.andExpect(status().isOk())
				.andExpect(content().string(containsString("Flashcard decks")))
				.andExpect(content().string(containsString("flip-toggle")));
	}

	@Test
	void staticAssetsLoad() throws Exception {
		mockMvc.perform(get("/js/htmx.min.js"))
				.andExpect(status().isOk())
				.andExpect(content().string(containsString("htmx")));
		mockMvc.perform(get("/css/style.css"))
				.andExpect(status().isOk())
				.andExpect(content().string(containsString("meter-fill")));
	}

	/* ------------------------------------------------------------ lab */

	@Test
	void energySimHitsTheQuotedSavingAtTarget() throws Exception {
		mockMvc.perform(get("/lab/energy").param("share", "44"))
				.andExpect(status().isOk())
				.andExpect(content().string(containsString("€260")))
				.andExpect(content().string(containsString("44.0%")))
				.andExpect(content().string(containsString("the €260bn/yr saving")));
	}

	@Test
	void energySimDropsToZeroBelowBaseline() throws Exception {
		mockMvc.perform(get("/lab/energy").param("share", "22"))
				.andExpect(status().isOk())
				.andExpect(content().string(containsString("€0bn")));
	}

	@Test
	void materialsSimGradesNinetyAsCritical() throws Exception {
		mockMvc.perform(get("/lab/materials").param("share", "90"))
				.andExpect(status().isOk())
				.andExpect(content().string(containsString("CRITICAL")))
				.andExpect(content().string(containsString("80 days")));
	}

	@Test
	void materialsSimGradesLowExposureAsManaged() throws Exception {
		mockMvc.perform(get("/lab/materials").param("share", "40"))
				.andExpect(status().isOk())
				.andExpect(content().string(containsString("MANAGED")));
	}

	@Test
	void councilSimDefaultsToPermanentOnly() throws Exception {
		mockMvc.perform(get("/lab/council"))
				.andExpect(status().isOk())
				.andExpect(content().string(containsString("66%")))
				.andExpect(content().string(containsString("Balanced")));
	}

	@Test
	void councilSimAddsBonusForEasternAndNorthernSeats() throws Exception {
		mockMvc.perform(get("/lab/council").param("members", "ro").param("members", "se"))
				.andExpect(status().isOk())
				.andExpect(content().string(containsString("+15")));
	}

	/* ------------------------------------------------------------ quiz */

	@Test
	void quizGivesAllTwelveQuestions() throws Exception {
		mockMvc.perform(get("/quiz"))
				.andExpect(status().isOk())
				.andExpect(content().string(containsString("Check your understanding")))
				.andExpect(content().string(containsString("q1")))
				.andExpect(content().string(containsString("q12")));
	}

	@Test
	void quizMarksRightAnswerCorrect() throws Exception {
		mockMvc.perform(post("/quiz/answer").param("q", "q1").param("choice", "~90%"))
				.andExpect(status().isOk())
				.andExpect(content().string(containsString("Correct")))
				.andExpect(content().string(containsString("China accounts for about 90%")));
	}

	@Test
	void quizExplainsWrongAnswer() throws Exception {
		mockMvc.perform(post("/quiz/answer").param("q", "q4").param("choice", "€90bn a year"))
				.andExpect(status().isOk())
				.andExpect(content().string(containsString("Not quite")))
				.andExpect(content().string(containsString("You picked: €90bn a year")))
				.andExpect(content().string(containsString("€260 billion")));
	}

	@Test
	void quizQuestionAgainRendersTheCard() throws Exception {
		mockMvc.perform(get("/quiz/question").param("q", "q5"))
				.andExpect(status().isOk())
				.andExpect(content().string(containsString("associate member")))
				.andExpect(content().string(containsString("Check answer")));
	}
}