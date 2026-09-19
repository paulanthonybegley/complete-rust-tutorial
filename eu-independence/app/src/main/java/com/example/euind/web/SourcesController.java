package com.example.euind.web;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * The media-literacy feature (Lesson 9 / Sources screen): the same Canada
 * announcement framed from opposite corners of the political spectrum, plus
 * the "verify before you share" checklist the video's own segment motivates.
 */
@Controller
public class SourcesController {

	private record Frame(String corner, String headline, String body) {
	}

	private static final java.util.List<Frame> FRAMES = java.util.List.of(
			new Frame("left", "EU and Canada: a warm partnership is born",
					"Seen from the left-leaning press, the announcement is about Europe"
						+ " deepening its bond with a like-minded democracy at exactly the moment"
						+ " Washington turns inward. The emphasis lands on shared values, trade"
						+ " and the return of Europe as a leader of the world's middle powers."),
			new Frame("right", "Uncertainty over an undefined status",
					"Seen from the right-leaning press, the announcement is a textbook case of"
						+ " pre-announcement: a brand-new category that 'does not formally exist',"
						+ " with no details on rights, obligations or ratification. The emphasis"
						+ " lands on the gap between the headline and the legal reality."),
			new Frame("facts", "The shared facts underneath",
					"Both frames report the same two facts: (1) the Commission invited Canada"
						+ " to explore becoming the EU's first associate member; (2) no such status"
						+ " currently exists in EU law. The difference is entirely in which fact is"
						+ " foregrounded and which is downplayed."));

	@GetMapping("/sources")
	public String sources(Model model) {
		model.addAttribute("frames", FRAMES);
		model.addAttribute("sourceCount", 157);
		return "sources";
	}
}