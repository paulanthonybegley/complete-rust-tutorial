package com.example.devops;

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
class DevopsZeroHeroApplicationTests {

	@Autowired
	private MockMvc mockMvc;

	/* ---------------------------------------------------------- pages */

	@Test
	void dashboardShowsThesisAndMeter() throws Exception {
		mockMvc.perform(get("/"))
				.andExpect(status().isOk())
				.andExpect(content().string(containsString("something you do")))
				.andExpect(content().string(containsString("Ship meter")));
	}

	@Test
	void conceptExplorerListsCards() throws Exception {
		mockMvc.perform(get("/concepts"))
				.andExpect(status().isOk())
				.andExpect(content().string(containsString("Concept explorer")))
				.andExpect(content().string(containsString("What DevOps is")))
				.andExpect(content().string(containsString("Ship the production API")));
	}

	@Test
	void conceptGridFiltersByTheme() throws Exception {
		mockMvc.perform(get("/concepts/grid").param("cat", "CONTAINERS"))
				.andExpect(status().isOk())
				.andExpect(content().string(containsString("Docker")))
				.andExpect(content().string(not(containsString("Infrastructure as Code"))));
	}

	@Test
	void conceptDetailDrawsTheVideoBullets() throws Exception {
		mockMvc.perform(get("/concepts/detail").param("id", "docker"))
				.andExpect(status().isOk())
				.andExpect(content().string(containsString("class=\"drawer open")))
				.andExpect(content().string(containsString("portable unit")))
				.andExpect(content().string(containsString("docker build -t")));
	}

	@Test
	void conceptDrawerOpensForHtmx() throws Exception {
		mockMvc.perform(get("/concepts/detail").param("id", "kubernetes"))
				.andExpect(status().isOk())
				.andExpect(content().string(containsString("class=\"drawer open")))
				.andExpect(content().string(containsString("scrollIntoView")));
	}

	@Test
	void unknownConceptReturnsNotice() throws Exception {
		mockMvc.perform(get("/concepts/detail").param("id", "nope"))
				.andExpect(status().isOk())
				.andExpect(content().string(containsString("No concept card")));
	}

	@Test
	void pipelineRunsGreenByDefault() throws Exception {
		mockMvc.perform(get("/pipeline/run"))
				.andExpect(status().isOk())
				.andExpect(content().string(containsString("GREEN")))
				.andExpect(content().string(containsString("8 / 8")));
	}

	@Test
	void pipelineStopsAtFirstFailure() throws Exception {
		mockMvc.perform(get("/pipeline/run").param("failAt", "tests"))
				.andExpect(status().isOk())
				.andExpect(content().string(containsString("RED")))
				.andExpect(content().string(containsString("✗ failed")))
				.andExpect(content().string(containsString("never reached")))
				.andExpect(content().string(containsString("first failure (“tests”)")))
				.andExpect(content().string(not(containsString("%s"))));
	}

	@Test
	void dockerLayersCacheByDefault() throws Exception {
		mockMvc.perform(get("/docker/build").param("change", "src"))
				.andExpect(status().isOk())
				.andExpect(content().string(containsString("eclipse-temurin:17-jre")))
				.andExpect(content().string(containsString("CACHED")))
				.andExpect(content().string(containsString("REBUILT")));
	}

	@Test
	void dockerBaseChangeRebuildsEverything() throws Exception {
		mockMvc.perform(get("/docker/build").param("change", "base"))
				.andExpect(status().isOk())
				.andExpect(content().string(containsString("every layer below it rebuilds")))
				.andExpect(content().string(containsString("107s")))
				.andExpect(content().string(not(containsString("CACHED"))));
	}

	@Test
	void dockerPomChangeKeepsTheBaseImagesCached() throws Exception {
		mockMvc.perform(get("/docker/build").param("change", "pom"))
				.andExpect(status().isOk())
				.andExpect(content().string(containsString("43s")))
				.andExpect(content().string(containsString("the two base images stay cached")))
				.andExpect(content().string(containsString("2 layers reused")));
	}

	@Test
	void aCheckedHiddenTwinCheckboxWins() throws Exception {
		mockMvc.perform(get("/docker/build").param("change", "src").param("noCache", "true", "false"))
				.andExpect(status().isOk())
				.andExpect(content().string(containsString("forced a cold build")));
	}

	@Test
	void dockerPageRendersTheWholeContainerArc() throws Exception {
		mockMvc.perform(get("/docker"))
				.andExpect(status().isOk())
				.andExpect(content().string(containsString("The pipeline ships it")))
				.andExpect(content().string(containsString("The build packs it")))
				.andExpect(content().string(containsString("The cluster runs it")))
				.andExpect(content().string(containsString("GREEN")))
				.andExpect(content().string(containsString("8 / 8")))
				.andExpect(content().string(containsString("eclipse-temurin:17-jre")))
				.andExpect(content().string(containsString("ready replicas")));
	}

	@Test
	void dockerPageHonoursParamsAcrossAllPanels() throws Exception {
		mockMvc.perform(get("/docker").param("failAt", "tests").param("change", "base")
				.param("killPod", "2"))
				.andExpect(status().isOk())
				.andExpect(content().string(containsString("RED")))
				.andExpect(content().string(containsString("never reached")))
				.andExpect(content().string(containsString("every layer below it rebuilds")))
				.andExpect(content().string(containsString("Replaced")));
	}

	@Test
	void navPointsToTheSingleContainerLab() throws Exception {
		mockMvc.perform(get("/"))
				.andExpect(status().isOk())
				.andExpect(content().string(containsString("href=\"/docker\"")))
				.andExpect(content().string(not(containsString("href=\"/pipeline\""))))
				.andExpect(content().string(not(containsString("href=\"/k8s\""))));
	}

	@Test
	void k8sSteadyStateIsAllRunning() throws Exception {
		mockMvc.perform(get("/k8s/reconcile").param("replicas", "3"))
				.andExpect(status().isOk())
				.andExpect(content().string(containsString("3 / 3")))
				.andExpect(content().string(containsString("Steady state")));
	}

	@Test
	void k8sSelfHealsAKilledPod() throws Exception {
		mockMvc.perform(get("/k8s/reconcile").param("replicas", "3").param("killPod", "2"))
				.andExpect(status().isOk())
				.andExpect(content().string(containsString("Replaced")))
				.andExpect(content().string(containsString("Starting")))
				.andExpect(content().string(containsString("Self-heal")))
				.andExpect(content().string(containsString("Pod 2 died")))
				.andExpect(content().string(containsString("noticed 2/3 ready")))
				.andExpect(content().string(containsString("Desired state stays at 3 replicas.")));
	}

	@Test
	void k8sRollingUpdateHasNoDowntime() throws Exception {
		mockMvc.perform(get("/k8s/reconcile").param("replicas", "3").param("rolling", "true"))
				.andExpect(status().isOk())
				.andExpect(content().string(containsString("Rolling update")))
				.andExpect(content().string(containsString("no downtime, 3/3 ready")))
				.andExpect(content().string(not(containsString("%s"))));
	}

	@Test
	void k8sRollingCheckboxOrderingLetsTrueWin() throws Exception {
		mockMvc.perform(get("/k8s/reconcile").param("replicas", "3").param("rolling", "true", "false"))
				.andExpect(status().isOk())
				.andExpect(content().string(containsString("Rolling update")));
	}

	@Test
	void monitorAutoHealCheckboxOrderingLetsTrueWin() throws Exception {
		mockMvc.perform(get("/monitor/sample").param("unhealthy", "true").param("autoHeal", "true", "false"))
				.andExpect(status().isOk())
				.andExpect(content().string(containsString("quiet")))
				.andExpect(content().string(containsString("auto-heal")));
	}

	@Test
	void securityHasKeyCheckboxOrderingLetsTrueWin() throws Exception {
		mockMvc.perform(get("/security/check").param("kind", "browser").param("botLike", "5").param("hasKey", "true", "false"))
				.andExpect(status().isOk())
				.andExpect(content().string(containsString("ALLOWED")));
	}

	@Test
	void iacPlanIsANoOpWhenNothingChanged() throws Exception {
		mockMvc.perform(get("/iac/run"))
				.andExpect(status().isOk())
				.andExpect(content().string(containsString("no-op")))
				.andExpect(content().string(containsString("7 modules unchanged")))
				.andExpect(content().string(not(containsString("%d"))));
	}

	@Test
	void iacApplyNoOpReportsApplyNotPlan() throws Exception {
		mockMvc.perform(get("/iac/run").param("mode", "apply"))
				.andExpect(status().isOk())
				.andExpect(content().string(containsString("no-op")))
				.andExpect(content().string(containsString(">apply<")));
	}

	@Test
	void iacPlanDiffsAnAddedDatabase() throws Exception {
		mockMvc.perform(get("/iac/run").param("mode", "plan").param("change", "db"))
				.andExpect(status().isOk())
				.andExpect(content().string(containsString("aws_rds_cluster")))
				.andExpect(content().string(containsString("tf-add")))
				.andExpect(content().string(containsString("7 unchanged, 1 to add")));
	}

	@Test
	void iacScaleKeepsTheOtherSixInSync() throws Exception {
		mockMvc.perform(get("/iac/run").param("mode", "plan").param("change", "scale"))
				.andExpect(status().isOk())
				.andExpect(content().string(containsString("6 unchanged, 1 to update")))
				.andExpect(content().string(containsString(">6<")));
	}

	@Test
	void monitorSteadyStateIsQuiet() throws Exception {
		mockMvc.perform(get("/monitor/sample"))
				.andExpect(status().isOk())
				.andExpect(content().string(containsString("quiet")))
				.andExpect(content().string(containsString("99.95%")));
	}

	@Test
	void monitorUnhealthyWithoutAutoHealPages() throws Exception {
		mockMvc.perform(get("/monitor/sample").param("unhealthy", "true").param("autoHeal", "false"))
				.andExpect(status().isOk())
				.andExpect(content().string(containsString("PAGED")))
				.andExpect(content().string(containsString("3 a.m. page")));
	}

	@Test
	void monitorAutoHealAvoidsThePage() throws Exception {
		mockMvc.perform(get("/monitor/sample").param("unhealthy", "true").param("autoHeal", "true"))
				.andExpect(status().isOk())
				.andExpect(content().string(containsString("quiet")))
				.andExpect(content().string(containsString("auto-heal")));
	}

	@Test
	void securityAllowsAHumanWithAKey() throws Exception {
		mockMvc.perform(get("/security/check").param("kind", "browser").param("botLike", "5").param("hasKey", "true"))
				.andExpect(status().isOk())
				.andExpect(content().string(containsString("ALLOWED")))
				.andExpect(content().string(containsString("under the 62 threshold")))
				.andExpect(content().string(containsString("risk score")))
				.andExpect(content().string(not(containsString("%d"))));
	}

	@Test
	void securityBlocksKeylessTraffic() throws Exception {
		mockMvc.perform(get("/security/check").param("kind", "browser").param("botLike", "5").param("hasKey", "false"))
				.andExpect(status().isOk())
				.andExpect(content().string(containsString("BLOCKED")))
				.andExpect(content().string(containsString("no API key")));
	}

	@Test
	void securityBlocksSpam() throws Exception {
		mockMvc.perform(get("/security/check").param("kind", "spammer").param("botLike", "90").param("hasKey", "true"))
				.andExpect(status().isOk())
				.andExpect(content().string(containsString("BLOCKED")))
				.andExpect(content().string(containsString("risk score")));
	}

	@Test
	void gitShowsMainFirst() throws Exception {
		mockMvc.perform(get("/git/graph"))
				.andExpect(status().isOk())
				.andExpect(content().string(containsString("tag v1.0.0")))
				.andExpect(content().string(not(containsString("JWT auth + role-based access"))));
	}

	@Test
	void gitShowsTheFeatureBranchDivergence() throws Exception {
		mockMvc.perform(get("/git/graph").param("branch", "feature/login"))
				.andExpect(status().isOk())
				.andExpect(content().string(containsString("JWT auth + role-based access")));
	}

	@Test
	void shipPageMapsTheArcToApiRules() throws Exception {
		mockMvc.perform(get("/ship"))
				.andExpect(status().isOk())
				.andExpect(content().string(containsString("The shipping loop")))
				.andExpect(content().string(containsString("api-rules")));
	}

	@Test
	void sourcesPageMapsTheChapters() throws Exception {
		mockMvc.perform(get("/sources"))
				.andExpect(status().isOk())
				.andExpect(content().string(containsString("Framing test")))
				.andExpect(content().string(containsString("00:25:42")));
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
		mockMvc.perform(post("/quiz/answer").param("q", "q6")
				.param("choice", "Images are templates and containers are the running instances"))
				.andExpect(status().isOk())
				.andExpect(content().string(containsString("Correct")))
				.andExpect(content().string(containsString("an image (template)")));
	}

	@Test
	void quizExplainsWrongAnswer() throws Exception {
		mockMvc.perform(post("/quiz/answer").param("q", "q9").param("choice", "Replace Kubernetes entirely"))
				.andExpect(status().isOk())
				.andExpect(content().string(containsString("Not quite")))
				.andExpect(content().string(containsString("You picked: Replace Kubernetes entirely")))
				.andExpect(content().string(containsString("Declarative config")));
	}

	@Test
	void quizQuestionAgainRendersTheCard() throws Exception {
		mockMvc.perform(get("/quiz/question").param("q", "q11"))
				.andExpect(status().isOk())
				.andExpect(content().string(containsString("Hard-coding secrets")))
				.andExpect(content().string(containsString("Check answer")));
	}

	@Test
	void flashcardsServeDeckAndSpotlight() throws Exception {
		mockMvc.perform(get("/flashcards"))
				.andExpect(status().isOk())
				.andExpect(content().string(containsString("Flip until it sticks")))
				.andExpect(content().string(containsString("flip-toggle")));
	}

	@Test
	void flashcardsGridFiltersByTheme() throws Exception {
		mockMvc.perform(get("/flashcards/grid").param("cat", "SECURITY"))
				.andExpect(status().isOk())
				.andExpect(content().string(containsString("RBAC")))
				.andExpect(content().string(not(containsString("Multi-stage build"))));
	}
}