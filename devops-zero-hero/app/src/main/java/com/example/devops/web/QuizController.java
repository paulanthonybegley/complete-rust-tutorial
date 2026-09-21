package com.example.devops.web;

import java.util.HashMap;
import java.util.Map;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.example.devops.model.QuizQuestion;
import com.example.devops.service.QuizService;

/**
 * Self-grading quiz. Each question is its own htmx target: answering posts to
 * {@code /quiz/answer} and swaps in a styled result with the explanation.
 */
@Controller
public class QuizController {

	private final QuizService quiz;

	public QuizController(QuizService quiz) {
		this.quiz = quiz;
	}

	@GetMapping("/quiz")
	public String page(Model model) {
		model.addAttribute("questions", quiz.all());
		return "quiz";
	}

	@GetMapping("/quiz/question")
	public String question(@RequestParam String q, Model model) {
		QuizQuestion question = quiz.byId(q);
		if (question == null) {
			model.addAttribute("questions", quiz.all());
			return "_fragments :: quizGrid";
		}
		model.addAttribute("q", question);
		return "_fragments :: quizQuestion";
	}

	@PostMapping("/quiz/answer")
	public String answer(@RequestParam String q, @RequestParam String choice, Model model) {
		QuizQuestion question = quiz.byId(q);
		if (question == null) {
			model.addAttribute("questions", quiz.all());
			return "_fragments :: quizGrid";
		}
		Map<String, Object> result = new HashMap<>();
		result.put("question", question);
		result.put("correct", quiz.isCorrect(question, choice));
		result.put("chosen", choice);
		result.put("expected", question.options().get(question.answerIndex()));
		model.addAttribute("result", result);
		return "_fragments :: quizAnswered";
	}
}