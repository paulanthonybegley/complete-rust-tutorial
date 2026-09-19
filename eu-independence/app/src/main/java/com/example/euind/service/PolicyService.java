package com.example.euind.service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import org.springframework.stereotype.Service;

import com.example.euind.model.Category;
import com.example.euind.model.Figure;
import com.example.euind.model.Policy;
import com.example.euind.model.Status;

/**
 * The complete catalogue of features raised in the video. Every entry is
 * sourced from the transcript or the press coverage linked on the /sources
 * page. Figures are passed as {@link Figure} objects in the varargs; the rest
 * are prose bullets under "in the speech".
 */
@Service
public class PolicyService {

	private final List<Policy> all = Stream
			.of(policiesSpeech(), policiesEconomy(), policiesMaterials(), policiesDefence(), policiesEnergy(),
					policiesClimate(), policiesTech(), policiesPartners(), policiesMedia())
			.flatMap(List::stream).toList();

	public List<Policy> all() {
		return all;
	}

	public List<Policy> byCategory(Category category) {
		return all.stream().filter(p -> p.category() == category).toList();
	}

	public Policy byId(String id) {
		return all.stream().filter(p -> p.id().equals(id)).findFirst().orElse(null);
	}

	public List<Category> categoriesPresent() {
		return java.util.Arrays.stream(Category.values()).toList();
	}

	/* The speech — the frame the video puts around everything else. */
	private static List<Policy> policiesSpeech() {
		return List.of(
				policy("speech-thesis", "The independence thesis", "strongest, yet precarious", Category.SPEECH,
						Status.DELIVERED,
						"The speech opens on a paradox: the state of the union is the strongest it has ever been — and "
							+ "as precarious as it has ever been. The organising idea is independence: decoupling "
							+ "from volatile powers like Russia, China and, more recently, the US.",
						"Two truths at once — record strength, record precariousness.",
						"Decoupling targets: Russia (energy), China (materials & trade), US (security & tech).",
						"The video asks: what could an independent Europe look like?",
						fig("6th", "annual State of the Union", "von der Leyen"),
						fig("3", "volatile powers named", "Russia, China, US"),
						fig("1", "organising idea", "an independent Europe")),

				policy("speech-delivery", "From reform to delivery", "year three of the mandate", Category.SPEECH,
						Status.IN_PROGRESS,
						"This is the third year of a five-year term, so the speech shifts from bombastic new reforms "
							+ "to delivery: proving the EU can act on what was already promised.",
						"Defence made tangible progress since last year; competitiveness lagged.",
						"The single-market overhaul is crawling; EU Inc met union resistance.",
						"The Savings & Investments Union is in its preliminary stages.",
						fig("year 3", "of 5", "delivery over new reform")));
	}

	/* Economy & speed — most new proposals revolve around efficiency. */
	private static List<Policy> policiesEconomy() {
		return List.of(
				policy("econ-singmarket", "Single market overhaul by 2027", "one Europe, one market",
						Category.ECONOMY, Status.ANNOUNCED,
						"The Commission wants the single-market overhaul finished by the end of 2027 so capital, "
							+ "goods and people stop stalling at national borders.",
						"Only ~16% of corporate lending in the euro area crosses a border.",
						"Cash still cannot flow easily across borders — the fragmentation story.",
						fig("16%", "of corporate lending is cross-border", "the fragmentation story"),
						fig("2027", "target for the overhaul", "one Europe, one market")),

				policy("econ-permitting", "Speed: faster permitting", "the name of the game is speed",
						Category.ECONOMY, Status.ANNOUNCED,
						"Dramatically accelerate authorisation and permitting of businesses and infrastructure. The "
							+ "Commission calls kickstarting the economy its number one priority.",
						"Permit-chasing log-jams businesses, grids, renewables and defence projects.",
						"Faster processing is framed as crucial to unlocking growth.",
						fig("#1", "priority", "kickstarting Europe's economy")),

				policy("econ-banking", "Banking Package", "simpler, less fragmented banks", Category.ECONOMY,
						Status.PROPOSED,
						"A new banking package to cut fragmentation and raise banks' capacity and appetite for risk, "
							+ "so European savings finance European growth.",
						"Cross-border corporate lending sits at ~16% today.",
						"Sits alongside the long-term Savings & Investments Union.",
						fig("16%", "cross-border lending today", "the gap the package attacks")),

				policy("econ-omnibus", "12 omnibus packages", "€17bn of red tape gone a year", Category.ECONOMY,
						Status.ANNOUNCED,
						"Twelve omnibus simplification packages aim to cut annual administrative costs by about €17 "
							+ "billion, plus a pact against gold-plating by member states.",
						"Gold-plating = national governments layering extra rules onto EU law.",
						fig("12", "omnibus packages", "regulatory simplification"),
						fig("€17bn", "saved a year", "administrative costs")),

				policy("econ-savings", "Savings & Investments Union", "European savings fund European companies",
						Category.ECONOMY, Status.PROPOSED,
						"The long-term vehicle for deeper capital markets: route Europe's savings into European "
							+ "companies, scale-ups and deep-tech instead of leaving them parked nationally.",
						"Still in preliminary stages — slow progress noted in the video.",
						"Pairs with scale-up and deep-tech financing for European champions.",
						fig("early", "stage", "preliminary, long-term strategy")),

				policy("econ-euinc", "The 28th regime (EU Inc)", "a company law for the single market",
						Category.ECONOMY, Status.BLOCKED,
						"A common company-law regime to make it easier to scale up across borders, more commonly "
							+ "known as EU Inc — it has met resistance from unions and remains in early talks.",
						"Pitched as one option for cross-border scaling.",
						"The video: 'has met resistance from unions.'",
						fig("28th", "regime", "EU Inc — contested")));
	}

	/* Critical raw materials — the 90% problem and the response. */
	private static List<Policy> policiesMaterials() {
		return List.of(
				policy("raw-china", "The 90% problem", "rare earths run through Beijing", Category.RAW_MATERIALS,
						Status.IN_PROGRESS,
						"China supplies roughly 90% of the EU's critical raw-material imports and has shown "
							+ "willingness to weaponise its near-monopoly on rare earths. The EU is especially "
							+ "vulnerable here.",
						"Rare earths power wind turbines, EV motors, smartphones and defence electronics.",
						"The EU runs a €1bn-a-day trade deficit with China.",
						fig("90%", "of EU critical-material imports", "some rare earths even higher"),
						fig("€1bn", "China deficit per day", "≈ €365bn a year")),

				policy("raw-corp", "Joint procurement & stockpiles", "a European body coordinates crunch reserves",
						Category.RAW_MATERIALS, Status.PROPOSED,
						"A joint procurement and stockpile programme for critical materials, coordinated by a "
							+ "European-level body, for use in crises or supply-chain disruption.",
						"A new European Corporation on Critical Raw Materials would procure and stockpile strategic "
							+ "inputs (EVs, semiconductors, batteries, clean tech, defence).",
						"Stockpiles buy time while substitute sources and routes come online.",
						fig("1", "coordinator", "a European-level body")));
	}

	/* Defence & security — the progress made and the architecture proposed. */
	private static List<Policy> policiesDefence() {
		return List.of(
				policy("def-programme", "Defence Industry Programme", "€1.5bn to build scale", Category.DEFENCE,
						Status.DELIVERED,
						"The EU launched a €1.5bn European Defence Industry Programme, earmarked €325m for five "
							+ "joint defence projects and set up mechanisms for common procurement and planning.",
						"Common procurement gives buying power and interoperability.",
						"European defence spending has risen almost 80% in five years.",
						fig("€1.5bn", "defence industry programme", "launched"),
						fig("€325m", "five joint projects", "earmarked")),

				policy("def-debt", "€90bn of joint debt", "plugging the Ukraine gap", Category.DEFENCE,
						Status.DELIVERED,
						"The EU could not agree on using frozen Russian assets to finance Ukraine support, so it "
							+ "plugged the gap with €90bn of joint debt.",
						"Frozen Russian assets remained politically blocked.",
						"Joint borrowing kept support flowing.",
						fig("€90bn", "joint debt", "Ukraine support gap")),

				policy("def-article4", "EU's Article 4", "now: the Emergency Security Protocol", Category.DEFENCE,
						Status.ANNOUNCED,
						"NATO's Article 4 lets any member call a formal consultation when it feels threatened. Von "
							+ "der Leyen proposed an EU equivalent: an Emergency Security Protocol, flanking NATO, "
							+ "that one member can trigger to convene all members against hybrid threats.",
						"Critics call it a step, but 'not nearly enough' on its own.",
						"It sits below the armed-aggression threshold of NATO Article 5.",
						fig("1", "triggering member", "convenes the whole union"),
						fig("hybrid", "below-armed-aggression threats", "sabotage, drones, cyber")),

				policy("def-playbook", "Counter-hybrid playbook", "who responds to what", Category.DEFENCE,
						Status.ANNOUNCED,
						"A counter-hybrid playbook would define what counts as a hybrid attack, which institutions "
							+ "own responsibility, and how responses are coordinated to deter further threats.",
						"Hybrid incidents: sabotage, arson, drone incursions, disinformation.",
						"The playbook builds consensus before an incident happens.",
						fig("1", "playbook", "one agreed rulebook")),

				policy("def-council", "European Security Council", "security summit without Washington",
						Category.DEFENCE, Status.PROPOSED,
						"A UNSC-style leaders' format with a core group of permanent members (e.g. France, Germany, "
							+ "Italy, Spain, Poland) and a rotating periphery, bringing in partners like the UK, "
							+ "Norway, Canada and Ukraine.",
						"Modelled loosely on the UN Security Council.",
						"No defined powers or financing yet — 'at the very start'.",
						fig("5+", "permanent members", "FR, DE, IT, ES, PL"),
						fig("rotating", "non-permanent periphery", "balance by region")),

				policy("def-enablers", "Strategic Enablers instrument", "the systems only Europe can share",
						Category.DEFENCE, Status.PROPOSED,
						"A European Instrument for Strategic Enablers to channel investment in the systems "
							+ "individual states struggle to scale: air & missile defence, strategic transport, "
							+ "air-to-air refuelling, space and cyber.",
						"Aligned with NATO — 'only together does Europe have the scale'.",
						"The war in Ukraine showed their importance first-hand.",
						fig("5", "enabler domains", "missiles, transport, refuelling, space, cyber")),

				policy("def-spend", "Defence spending +80%", "record investment in five years", Category.DEFENCE,
						Status.DELIVERED,
						"EU defence spending has risen almost 80% over the past five years, generating record "
							+ "investment in forces, larger European orders and more joint projects.",
						"Driven by the war in Ukraine and hybrid activity from Russia.",
						fig("+80%", "defence spending in 5 years", "record investment")));
	}

	/* Energy — the independence story in electrons. */
	private static List<Policy> policiesEnergy() {
		return List.of(
				policy("energy-2040", "Double electricity by 2040", "cutting the import bill by €260bn/yr",
						Category.ENERGY, Status.IN_PROGRESS,
						"The Commission's goal is to double electricity's share of total energy consumption by 2040, "
							+ "which could cut the EU fossil-fuel import bill by about €260bn a year.",
						"Imported fossil energy has already cost Europe an extra €90bn since the latest Middle East "
							+ "conflict began.",
						"Electrification links energy, defence and climate in one policy.",
						fig("2×", "electricity share by 2040", "from today's level"),
						fig("€260bn", "import bill cut per year", "at the target")),

				policy("energy-grid", "The grid bottleneck", "80 GW added, 6× waiting for a socket",
						Category.ENERGY, Status.IN_PROGRESS,
						"Europe added more than 80 GW of renewables last year, but about six times that amount is "
							+ "waiting for a grid connection. The speech promises more grid connections between "
							+ "member states, storage and faster hook-ups.",
						"A larger, more secure network is a security asset, not just climate policy.",
						"Less bureaucratic burden on grid projects is part of the speed agenda.",
						fig("80 GW", "renewables added last year", "new capacity"),
						fig("6×", "that amount waiting for the grid", "the grid backlog")),

				policy("energy-mix", "Tech-neutral mix", "renewables, nuclear, biomethane", Category.ENERGY,
						Status.ANNOUNCED,
						"The prescription is deliberately technology-neutral: renewables, nuclear and biomethane "
							+ "all count as part of a more self-reliant, secure electricity system.",
						"Self-determination on energy = resilience against price shocks and weaponised supply.",
						fig("3+", "technologies in the mix", "neutral, not one-size-fits-all")));
	}

	/* Climate — future-proofing a continent warming twice as fast. */
	private static List<Policy> policiesClimate() {
		return List.of(
				policy("climate-fleet", "Climate Resilience Framework", "100 vulnerable territories mapped",
						Category.CLIMATE, Status.ANNOUNCED,
						"Europe is heating up about twice as fast as the rest of the world. The Commission will "
							+ "present a resilience framework identifying 100 of Europe's most vulnerable "
							+ "territories and the risks they face.",
						"A climate risk insurance alliance — only ~25% of catastrophe losses in Europe are privately "
							+ "insured.",
						"Includes a European Heatwave Plan and early-warning health systems.",
						fig("2×", "faster warming than average", "the grim warning"),
						fig("100", "territories in the framework", "vulnerability mapping")),

				policy("climate-water", "Heatwave, drought & water", "one litre in four is lost", Category.CLIMATE,
						Status.ANNOUNCED,
						"Heatwave plans, drought strategies and a new European water initiative: European networks "
							+ "already lose almost one litre in four through leakage.",
						"A heatwave plan and drought strategies were both announced.",
						fig("1 in 4", "litres lost to leakage", "the water initiative")),

				policy("climate-fleet2", "European Firefighting Fleet", "a possible shared wildfire response",
						Category.CLIMATE, Status.PROPOSED,
						"The Commission floated creating a European firefighting fleet as one of the resilience "
							+ "responses to more frequent wildfires.",
						"Still exploratory — 'possible creation'.",
						fig("1", "fleet", "shared firefighting capability")));
	}

	/* AI & technology sovereignty — compute, capital and coordination. */
	private static List<Policy> policiesTech() {
		return List.of(
				policy("ai-compute", "More European compute", "gigafactories and fresh capital", Category.AI,
						Status.ANNOUNCED,
						"Massively increase European computing capacity using new combinations of public and "
							+ "private capital, funding European AI companies and AI gigafactories.",
						"Technology sovereignty is framed as both an economic and security goal.",
						fig("more", "compute + capital", "public-private combinations")),

				policy("ai-safety", "Frontier-model safety", "evaluate before you deploy", Category.AI,
						Status.ANNOUNCED,
						"Work with Canada, the UK and other partners on model evaluation, verification, early "
							+ "warning and AI security, and bring frontier laboratories together to discuss "
							+ "'pacing the frontier'.",
						"Addresses hacking capabilities, adversarial use and self-improving systems.",
						fig("3+", "partner countries", "Canada, UK and others")),

				policy("ai-sectors", "Industrial AI in five sectors",
						"health, transport, agri-food, manufacturing, defence & space", Category.AI,
						Status.ANNOUNCED,
						"Concentrate industrial AI deployment in five sectors where Europe has data and industrial "
							+ "strength: health, transport, agri-food, advanced manufacturing, and defence & space.",
						"New initiatives for those sectors announced in November.",
						fig("5", "priority sectors", "industrial AI push")));
	}

	/* Partnerships & trade — diversify to decouple. */
	private static List<Policy> policiesPartners() {
		return List.of(
				policy("part-canada", "Canada: first associate member", "a status that doesn't exist yet",
						Category.PARTNERSHIPS, Status.ANNOUNCED,
						"Canada has been invited to explore becoming the EU's first associate member — a role "
							+ "first pitched to bring Ukraine closer. The category does not formally exist and no "
							+ "details were given beyond exploring it.",
						"Part of the grander strategy to wean Europe off US and China dependence.",
						"Covers defence industries, critical minerals, energy, AI, quantum, cyber and the Arctic.",
						fig("1st", "associate member", "Canada — status TBD"),
						fig("start", "of negotiations", "nothing formal yet")),

				policy("part-trade", "Trade diversification", "India to Mercosur and beyond",
						Category.PARTNERSHIPS, Status.DELIVERED,
						"A flurry of EU trade agreements over the past year — from India to Mercosur — to "
							+ "diversify trade and supply chains; the European Parliament's president opened the "
							+ "door to New Zealand and Australia too.",
						"Euro-curious outsiders watching closely: Norway, Iceland, the UK.",
						fig("2+", "major deals in a year", "India, Mercosur"),
						fig("2", "doors opening", "New Zealand, Australia")),

				policy("part-middle", "Middle Corridor", "€12bn to dodge the bottlenecks",
						Category.PARTNERSHIPS, Status.ANNOUNCED,
						"Up to €12bn of Global Gateway investment for the Middle Corridor linking Central Asia "
							+ "and the South Caucasus to the European market — to triple flows and cut transit "
							+ "times by 2030.",
						"A land route that diversifies away from single choke-points.",
						fig("€12bn", "Global Gateway investment", "Middle Corridor"),
						fig("2030", "triple-flows target", "shorter transit")),

				policy("part-china", "The €1bn-a-day deficit", "trade instruments as leverage",
						Category.PARTNERSHIPS, Status.IN_PROGRESS,
						"The EU's trade deficit with China runs to about €1 billion a day, which the speech "
							+ "called unsustainable — with a willingness to use trade instruments to rebalance it.",
						"Combined with critical raw materials, China is the single biggest dependency.",
						fig("€1bn", "per day", "trade deficit with China")));
	}

	/* Media & sources — the video's own media-literacy segment. */
	private static List<Policy> policiesMedia() {
		return List.of(
				policy("media-bias", "157 sources, one story", "framing is everything", Category.MEDIA,
						Status.ANNOUNCED,
						"When the video was made, 157 outlets were already reporting on the speech. Left-leaning "
							+ "sources emphasised the warm EU–Canada relationship; right-leaning sources "
							+ "highlighted the uncertainty of the undefined associate-member status.",
						"Same facts, different frames — which are emphasised and which are left out?",
						fig("157", "sources at time of filming", "the count itself is a fact"),
						fig("2", "sample frames", "warmth vs uncertainty")),

				policy("media-verify", "Verify before you share", "a five-step checklist", Category.MEDIA,
						Status.ANNOUNCED,
						"Before re-sharing a claim about the speech: check the source and date, find the primary "
							+ "text, read one left- and one right-leaning account, ask who owns the outlet, and "
							+ "confirm the figure is quoted, not invented.",
						"Bias-aware reporting surfaces the range of honest readings.",
						fig("5", "checks", "source, date, primary text, framing, ownership")));
	}

	private static Policy policy(String id, String title, String subtitle, Category category, Status status,
			String summary, Object... content) {
		List<String> detail = new ArrayList<>();
		List<Figure> figures = new ArrayList<>();
		for (Object item : content) {
			if (item instanceof Figure f) {
				figures.add(f);
			} else {
				detail.add(item.toString());
			}
		}
		return new Policy(id, title, subtitle, category, status, summary, detail, figures);
	}

	private static Figure fig(String value, String label, String note) {
		return new Figure(label, value, note);
	}
}