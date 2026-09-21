package com.example.devops.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.example.devops.model.Category;
import com.example.devops.model.QuizQuestion;

/**
 * Self-grading quiz covering every theme of the course.
 */
@Service
public class QuizService {

	private final List<QuizQuestion> questions = List.of(
			q("q1", "DevOps is best described as…",
					List.of("A specific deployment tool", "A job title with one fixed toolchain",
							"A culture shift that unites devs and ops with automation", "A cloud provider"),
					2, Category.CULTURE,
					"DevOps is a culture of collaboration, a set of practices and an automation toolbox — not a "
							+ "single tool."),
			q("q2", "The DevOps infinity loop runs…",
					List.of("Plan → Code → Build → Test → Release → Deploy → Operate → Monitor",
							"Code → Build → Sell → Support", "Plan → Deploy → Plan", "Design → Sketch → Screenshot"),
					0, Category.CULTURE,
					"The loop is how an idea becomes software in users' hands and then improves with feedback."),
			q("q3", "Which git operation lets you rewind a released change safely?",
					List.of("git push --force", "git revert <sha>", "git stash pop", "git cherry-pick --all"),
					1, Category.VERSION_CONTROL,
					"git revert adds a new commit that undoes the target change — safe on shared lines."),
			q("q4", "Continuous Integration means…",
					List.of("Deploying only once a quarter", "Merging and testing every change into the shared repo "
							+ "constantly",
							"Writing all code in one file", "Skipping unit tests for speed"),
					1, Category.PIPELINES,
					"CI integrates everyone's pushes constantly so conflicts surface daily, not weekly."),
			q("q5", "Which phrase captures what the build stage does?",
					List.of("Serving raw dough to customers", "Baking source code into a ready-to-run artifact",
							"Writing more source code", "Turning on the servers"),
					1, Category.PIPELINES,
					"The build compiles, installs dependencies, bundles, packages and scans—dough becomes bread."),
			q("q6", "The main difference between a Docker image and a container is…",
					List.of("Containers are templates and images run them", "Images are templates and containers are "
							+ "the running instances",
							"There is no difference", "Images need Kubernetes to run"),
					1, Category.CONTAINERS,
					"`docker build` creates an image (template); `docker run` starts a container (instance)."),
			q("q7", "What does a multi-stage Dockerfile buy you?",
					List.of("A smaller runtime image without the build toolchain", "Guaranteed zero vulnerabilities",
							"Automatic load balancing", "Free PostgreSQL in every container"),
					0, Category.CONTAINERS,
					"Build with the fat toolchain image, then copy only the artifact into a lean runtime image."),
			q("q8", "If you 'kill a pod' in Kubernetes, what happens?",
					List.of("Nothing until it's Sunday", "The Deployment controller schedules a replacement",
							"The whole cluster stops", "Kubernetes deletes the Deployment"),
					1, Category.ORCHESTRATION,
					"Self-healing: the controller reconciles reality to the declared replica count."),
			q("q9", "Infrastructure as Code (Terraform) lets you…",
					List.of("Manage infrastructure as reviewable, versioned config", "Avoid using any cloud",
							"Replace Kubernetes entirely", "Run containers without Docker"),
					0, Category.IAC,
					"Declarative config gets the same review and versioning as application code."),
			q("q10", "A readiness/liveness health endpoint exists so that…",
					List.of("The developer gets a 3 a.m. page on purpose", "Orchestrators and load balancers know "
							+ "whether the app can serve traffic",
							"Users can read the source", "It can replace the database"),
					1, Category.OBSERVABILITY,
					"Probes decide when to start sending traffic and when to restart a sick pod."),
			q("q11", "Which is NOT a way the video protects the API?",
					List.of("Input validation", "JWT authentication with role-based access",
							"Bot/spam/abuse detection at the edge", "Hard-coding secrets in the source"),
					3, Category.SECURITY,
					"Secrets are injected from the environment—committing them is the anti-pattern."),
			q("q12", "The final arc of the video ships the API by…",
					List.of("Copying files with FTP", "Containerizing it, running it through CI/CD, deploying it and "
							+ "monitoring it",
							"Deleting the repository", "Emailing the jar to customers"),
					1, Category.SHIP,
					"Containerize → pipeline → registry → orchestrator → health and monitoring: zero to hero."));

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