# Structured Lesson Plans — European Independence (SOTEU 2026)

> Applied from the YouTube video: **"Von der Leyen's New Plan for European
> Independence Explained"** by TLDR News EU
> <https://www.youtube.com/watch?v=ec_4Un6JrAY> (8:42)
>
> Core tagline from `OLD.jpg`: **"A lesson plan is a system, not a prompt."**

Every lesson below is built with the 9 design inputs extracted from
[`OLD.txt`](./OLD.txt):
**Learning Goal**, **Lesson Sequence**, **Assessment Evidence**, **Learner
Profile**, **Prior Knowledge**, **Learning Activities**, **Output
Requirements**, **Accessibility & Supports**, and **Teacher Decisions**.

The companion application lives in [`app/`](./app) — a Spring Boot 4 +
Thymeleaf + HTMX learning lab whose pages are the "worked example" for each
lesson. Every lesson lists the exact app screens to open.

| # | Feature (from the video) | App screens to open |
|---|--------------------------|----------------------|
| 1 | The speech & the "independent Europe" thesis | `/` (dashboard) |
| 2 | Economic resilience & the speed agenda | `/policies` (Economy), `/policies/econ-singmarket` |
| 3 | Critical raw materials | `/lab` (materials simulator), `/policies/rawmaterials` |
| 4 | European defence & security architecture | `/policies` (Defence), `/lab` (Security Council builder) |
| 5 | Energy independence | `/lab` (energy simulator), `/policies/energy` |
| 6 | Climate preparedness | `/policies` (Climate) |
| 7 | AI & technology sovereignty | `/policies` (AI & Tech) |
| 8 | Partnerships & trade diversification | `/partners`, `/policies` (Partnerships) |
| 9 | Media literacy & sources | `/sources` |
| + | Integration project: the Independence Scorecard | all screens |

---

## Lesson 1: The Speech — Understanding the "Independent Europe" Thesis

**Learning Goal:** Learners can explain what a State of the Union speech is,
why von der Leyen frames 2026 around *independence*, and what "decoupling from
Russia, China and the US" means in practice.

**Lesson Sequence:**
1. Warm-up: what do you already know about NATO, the EU, and "strategic
   autonomy"? Quick word-cloud.
2. Watch the video's first two minutes (context + last year's progress).
3. Open the app dashboard `/` and read the "one stat per theme" tiles.
4. Discuss: the speech says the state of the union is "the strongest it has
   ever been" *and* "precarious" — how can both be true?
5. Hands-on: complete Exercise 1.

**Assessment Evidence:** Given a list of 6 foreign powers and dependencies, the
learner classifies each as "energy", "materials", "security" or "finance"
dependence and writes a two-sentence "thesis" of the speech in their own words.
- Assessment answer key: `solutions.md` → Lesson 1.

**Learner Profile:** 16–19-year-olds or adult learners new to EU politics; no
prior EU knowledge assumed; comfortable reading a dashboard.

**Prior Knowledge:** What the EU is (a union of member states); what a "speech
by a leader" is for. None technical required.

**Learning Activities:** keyword spray, dashboard read-aloud, paired
"strongest/precarious" debate, classification grid.

**Output Requirements:** A "thesis card" (2–4 sentences) plus a 6-row
classification table; no code required.

**Accessibility & Supports:** Provide the glossary (EU, Commission, State of
the Union, decouple, hybrid threat, tariff); allow verbal answers; transcripts
of the video available in the Sources lesson.

**Teacher Decisions:** Emphasize that the preamble ("strongest but precarious")
is the emotional frame for *every* later proposal — it explains why the speech
is about resilience, not celebration.

---

## Lesson 2: Economic Resilience — The Speed Agenda

**Learning Goal:** Learners understand why the Commission calls speed its
"number one priority" and can name the concrete economic proposals and the
frictions they target.

**Lesson Sequence:**
1. Recall the video's "last year, slow progress" scene (single market crawling,
   EU Inc resistance, Savings & Investments Union in its early stage).
2. Open `/policies` and filter by **Economy**. Read each card.
3. Focus activity: cross-border corporate lending is only **16%** of lending —
   what does that say about capital flowing between member states?
4. Map each proposal to the friction it removes (permitting, banking,
   regulation, gold-plating).
5. Hands-on: complete Exercise 2.

**Assessment Evidence:** Learner matches 6 proposals to 6 frictions and
explains one of them to a peer in a "60-second consultancy pitch".
- Assessment answer key: `solutions.md` → Lesson 2.

**Learner Profile / Prior Knowledge:** Lesson 1 done; comfortable with the
ideas of a market, a bank and a business permit.

**Learning Activities:** friction-matching card game, "why is 16% a problem"
group story, re-scoring the EU's "speed score".

**Output Requirements:** A two-column table (proposal → friction removed) with
at least 6 rows and a one-line verdict on whether "speed" is the right frame.

**Accessibility & Supports:** Pre-printed table template; picture cards for
each friction; allow a voice recording instead of a written verdict.

**Teacher Decisions:** Spend a moment on the 16% statistic — it is the video's
single most concrete number and makes the abstract "fragmentation" tangible.

---

## Lesson 3: Critical Raw Materials — China Dependency

**Learning Goal:** Learners can explain why rare earths and critical minerals
are a strategic weakness and what joint procurement + stockpiling is meant to
achieve.

**Lesson Sequence:**
1. Hook: list 5 objects in the room that run on rare earths or critical metals
   (smartphones, EVs, wind turbines...).
2. Read the video's "China accounts for 90% of EU imports" scene.
3. Open the app's materials simulator in `/lab`: drag the China-dependency
   slider and watch the exposure grade and stockpile clock change.
4. Read `/policies/rawmaterials` for the proposed fix (European Corporation,
   joint procurement, stockpiles).
5. Discussion: is a stockpile a "plan" or a "reaction"? (Defence economics.)
6. Hands-on: complete Exercise 3.

**Assessment Evidence:** Learner builds a "dependency → shock → response" chain
for one material and recommends whether stockpiling or substitution deserves
the first euro.
- Assessment answer key: `solutions.md` → Lesson 3.

**Learner Profile / Prior Knowledge:** Lessons 1–2 done; enough chemistry to
know rare earths power electronics and magnets.

**Learning Activities:** "what's in your phone" scavenger, simulator play,
materials bingo, mini-Debate (stockpile vs subsidise mining vs substitute).

**Output Requirements:** One dependency chain diagram (text or drawn) + a
one-paragraph prioritisation argument.

**Accessibility & Supports:** Provide a completed example chain to imitate;
large slider controls in the app; allow drawing instead of writing.

**Teacher Decisions:** Keep the focus on *China ~90%* and *crits as leverage* —
the video names both, and both are what the policy responds to.

---

## Lesson 4: A European Defence & Security Architecture

**Learning Goal:** Learners can map the video's defence proposals onto the
threats they answer (war, hybrid attacks, sabotage, capability gaps).

**Lesson Sequence:**
1. Quiz (from `/quiz`): what is NATO Article 4? Do an instant card-sort of
   "NATO / EU / national" for 8 responses.
2. Replay the defence scenes: €1.5bn programme, €325m joint projects, €90bn
   joint debt, spending up ~80% in five years.
3. Read the Defence cards in `/policies` (Emergency Security Protocol,
   counter-hybrid playbook, European Security Council, Strategic Enablers).
4. Go to `/lab` and build a Security Council: pick permanent members
   (France, Germany, Italy, Spain, Poland) and rotating ones; watch the score.
5. Discussion: why does the video call the Article 4 equivalent "not nearly
   enough" per its critics?
6. Hands-on: complete Exercise 4.

**Assessment Evidence:** Learner labels the 6 defence instruments with (a) the
threat answered and (b) NATO/EU boundary, and states one critique of each.
- Assessment answer key: `solutions.md` → Lesson 4.

**Learner Profile / Prior Knowledge:** Lessons 1–3 done; knows war exists and
that NATO is a security alliance.

**Learning Activities:** threat→instrument matching, Security Council builder,
role-play: "trigger the protocol", two-column NATO vs EU sorting.

**Output Requirements:** A 6-row instrument table (instrument, threat, gap it
fills) + one "critic's objection" written from a sceptic's point of view.

**Accessibility & Supports:** Pre-printed instrument cards; allow grouped
answers ("defence vs diplomacy"); speech-to-text for the critic paragraph.

**Teacher Decisions:** The star moment is *the video's own caveat* — even the
proposals acknowledge hybrid protection under Article 4 isn't a full solution.
Keep that nuance alive.

---

## Lesson 5: Energy Independence

**Learning Goal:** Learners can explain the link between energy, security and
climate in the speech, and estimate the value of the 2040 electricity target.

**Lesson Sequence:**
1. Warm-up: where does our electricity come from? (national mix guessing game.)
2. Watch the energy scene: grids, 80 GW added, "six times that is waiting for
   a grid connection", double electricity share by 2040, cut imports €260bn/yr.
3. Open `/lab` energy simulator: move the target share and read the projected
   import-bill saving and grid catch-up.
4. Read `/policies/energy` — tech-neutral mix (renewables, nuclear, biomethane).
5. Hands-on: complete Exercise 5.

**Assessment Evidence:** Learner computes the "grid gap" logic for their own
country example and explains in two sentences why cheap electrons are a
security asset, not just a climate one.
- Assessment answer key: `solutions.md` → Lesson 5.

**Learner Profile / Prior Knowledge:** Lessons 1–4 done; basic ratio thinking;
knows energy prices affect bills and industry.

**Learning Activities:** simulator experimentation, "who loses the most on the
grid" mapping, import-bill arithmetic, debate: nuclear in or out of the mix.

**Output Requirements:** Screenshot/annotation of one simulator run plus a
2-sentence "why energy independence is security" claim.

**Accessibility & Supports:** Compare simulator outputs side-by-side as audio
descriptions; provide the formula cheat-sheet; allow numeric-only answers.

**Teacher Decisions:** Anchor the lesson on the number **€260 billion/yr** and
the phrase **"six times the added capacity waiting for a grid connection"** —
both make the policy feel concrete.

---

## Lesson 6: Climate Preparedness

**Learning Goal:** Learners can list the climate proposals and explain the
"twice as fast" warming claim and why insurance/infrastructure matter.

**Lesson Sequence:**
1. Warm-up: climate threats in your town (heat, flood, drought, fire).
2. Read the Climate cards in `/policies` and the `/timeline` entries.
3. Focus on the numbers: Europe warms 2× faster; only ~25% of catastrophe
   losses are privately insured in Europe; 1 in 4 litres of water lost to
   leakage; ~100 vulnerable territories to be identified.
4. Discussion: why announce an insurance alliance and a firefighting fleet in
   the *same* speech as energy independence? (Future-proofing thread.)
5. Hands-on: complete Exercise 6.

**Assessment Evidence:** Learner matches 5 climate instruments to the hazard
they answer and drafts one "resilience metric" they would track.
- Assessment answer key: `solutions.md` → Lesson 6.

**Learner Profile / Prior Knowledge:** Lessons 1–5 done; general awareness of
climate change.

**Learning Activities:** hazard→plan card sort, map-spot your country,
resilience-metre design, insurance gap role-play (insurer vs household).

**Output Requirements:** A 5-row hazard→response table + one proposed
resilience metric with a target.

**Accessibility & Supports:** Provide photo cards per hazard; allow oral
responses; keep the table template printed.

**Teacher Decisions:** Link climate to the video's bigger move — resilience
isn't a side theme, it is *why* independence ("future-proofing") is the speech's
organising idea.

---

## Lesson 7: AI & Technology Sovereignty

**Learning Goal:** Learners can describe the dual nature of AI in the speech
(competitiveness + security) and name the five priority sectors for industrial
AI.

**Lesson Sequence:**
1. Brainstorm: what can Europe do in AI today, and where does it depend on US
   and Chinese platforms?
2. Read `/policies` (AI & Tech): European compute, funding scale-ups, model
   safety cooperation with Canada/UK, AI gigafactories, "pacing the frontier".
3. Sector scramble: which five sectors get industrial AI first? (health,
   transport, agri-food, advanced manufacturing, defence & space).
4. Discussion: "pacing the frontier" — who should decide how fast frontier
   models develop?
5. Hands-on: complete Exercise 7.

**Assessment Evidence:** Learner classifies 6 AI moves as "competitiveness" or
"security" (some both) and argues one prioritisation choice.
- Assessment answer key: `solutions.md` → Lesson 7.

**Learner Profile / Prior Knowledge:** Lessons 1–6 done; uses AI tools; knows
compute/gigafactory roughly.

**Learning Activities:** sector bingo, "EU vs US vs CN" dependency grid,
frontier-pacing debate, compute-vs-data balance scales.

**Output Requirements:** A two-column classification table + a 4-sentence
"Europe's AI bet" summary.

**Accessibility & Supports:** Sector cards with images; allow a two-minute
audio recording instead of the summary; sentence starters provided.

**Teacher Decisions:** Frame AI as the *new* resource lesson: where rare earths
were physical leverage, chips and compute are digital leverage.

---

## Lesson 8: Partnerships & Trade Diversification

**Learning Goal:** Learners can explain "associate membership", why Canada is
the first candidate, and how trade deals (India, Mercosur, NZ, Australia)
reduce dependence on the US and China.

**Lesson Sequence:**
1. Warm-up: what does a "country joining a club" normally look like (membership,
   CETA, association)?
2. Open `/partners`: read the associate-member card, the trade-deal list and
   the observers (Norway, Iceland, UK, plus NZ/Australia doors opening).
3. Read `/policies` (Partnerships) and the Middle Corridor (€12bn via Global
   Gateway) and China deficit (€1bn/day) cards.
4. Discussion: the video says associate membership "doesn't formally exist" —
   is announcing a proposal before the rules exist strong or reckless?
5. Hands-on: complete Exercise 8.

**Assessment Evidence:** Learner ranks 4 candidate partners (Canada, Ukraine,
NZ, Norway) by readiness and explains the "Euro-curious" list logic.
- Assessment answer key: `solutions.md` → Lesson 8.

**Learner Profile / Prior Knowledge:** Lessons 1–7 done; comfortable with trade
and tariffs vocabulary from the news.

**Learning Activities:** partnership ranking, "build a deal" pairing activity,
door-opening timeline, deficit arithmetic (€1bn/day → yearly number).

**Output Requirements:** A ranking table (partner, status, why, risk) + a
2-sentence definition of associate membership in the learner's own words.

**Accessibility & Supports:** Team-based ranking; flag cards for countries;
allow a holding "I don't know yet" place for open questions.

**Teacher Decisions:** Note the video's own caution — Canada's invite was
"just the very start of negotiations" — and let learners smell the difference
between an announcement and a treaty.

---

## Lesson 9: Media Literacy — Following the Story

**Learning Goal:** Learners can compare how different outlets frame the same
speech and identify why "157 sources" is not the same as "one consensus".

**Lesson Sequence:**
1. Watch the closing sponsor scene (Ground News): left outlets highlight the
   warm EU–Canada relationship; right outlets highlight associate-status
   uncertainty.
2. Open `/sources` and read the left/right framing cards.
3. Activity: given an excerpt, underline the fact and circle the frame.
4. Discussion: what checks do we have before trusting a headline about the EU?
5. Hands-on: complete Exercise 9.

**Assessment Evidence:** Learner rewrites a given blurb in two deliberately
different frames without changing any facts, and names two things they would
verify.
- Assessment answer key: `solutions.md` → Lesson 9.

**Learner Profile / Prior Knowledge:** Any of lessons 1–8; uses news and social
media daily.

**Learning Activities:** frame-the-sentence lab, bias bingo, headline rewrite,
"verify this" checklist building.

**Output Requirements:** A before/after two-frame rewrite plus a 5-item
"before you share" checklist.

**Accessibility & Supports:** Sentence starters for the rewrites; recorded oral
versions accepted; pair work encouraged.

**Teacher Decisions:** Treat media literacy as an *applied* skill, not a
lecture — the video's sponsor segment is itself a teachable example, and this
lesson debriefs it openly.

---

## Integration Project — The Independence Scorecard

**Learning Goal:** Build one coherent, assessable picture of Europe's
independence across the video's themes and defend the scoring.

**Lesson Sequence:** (project sprint over 2–3 sessions)

**Project brief:** Design an **Independence Scorecard** for Europe covering the
video's themes: Economy, Raw Materials, Defence, Energy, Climate, AI,
Partnerships, and Media.

**Output Requirements (every video feature must appear):**
- A 0–10 score per theme with the evidence (stat from the speech/news) behind
  it — at least 8 stats that appear in the app.
- A "gap list": for each theme, the single biggest dependency that remains.
- A 3-year lookahead: which proposals, if delivered, move which scores most.
- A "who decides" row: Commission / member states / NATO / markets — per theme.
- A one-page method note explaining the scoring rules (how scores are defined).

**Assessment Evidence:** Peer review where each student defends one score against
a sceptical reviewer ("why 7 not 4 for Energy?") using the app's data; the
reviewer role rotates.

**Learner Profile / Teacher Decisions:** Work in pairs across lessons 1–9;
reuse the app's `/sources` page to justify the evidence; grading rubric in
`exercises.md` → Integration Project.