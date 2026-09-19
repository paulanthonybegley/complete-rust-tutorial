# Exercises: European Independence (SOTEU 2026)

> All exercises reference the Spring Boot 4 + Thymeleaf + HTMX application in
> [`app/`](./app) and the video
> <https://www.youtube.com/watch?v=ec_4Un6JrAY>.
> Answers live in [`solutions.md`](./solutions.md).

---

## Lesson 1 — The speech & the "independent Europe" thesis

**1.1** From the video's narration, fill in the three powers von der Leyen
wants Europe to *decouple* from, and for each one name the dependency Europe
currently has on it (energy, tech, security, materials...).

| Power | The dependency Europe has on it |
|---|---|
| Russia | |
| China | |
| US | |

**1.2** The narrator says the state of the union is "the strongest it has
ever been" *and* "precarious". Write the speech's thesis in your own words
(2–4 sentences).

**1.3** Open the app dashboard `/`. For each theme tile, name the single
statistic it presents.

---

## Lesson 2 — Economic resilience & the speed agenda

**2.1** Match each proposal to the friction it removes:

| Proposal | Friction removed |
|---|---|
| Faster permitting & authorisation | |
| Banking Package / simpler fragmentation | |
| Complete single market overhaul by 2027 | |
| 12 omnibus simplification packages (€17bn/yr) | |
| "Pact against gold-plating" | |
| Savings & Investments Union / deep capital markets | |

**2.2** Cross-border corporate lending in the euro area is only about **16%**
of total corporate lending. Write one paragraph: what does this mean for a
company in Portugal wanting a loan from a German bank, and why does the
Commission call the single market "crawling"?

**2.3** Open `/policies` and filter by **Economy**. List the first three cards
you see and, for each, quote the key figure it shows.

---

## Lesson 3 — Critical raw materials

**3.1** Fill in the blanks from the video/app: China supplies about
**\___%** of the EU's imports of critical raw materials, rising to **\___%**
for some rare earths.

**3.2** Build a "dependency → shock → response" chain for rare earths:
1. Which products depend on them?
2. What would a trade cut-off do? (the shock)
3. What is the Commission proposing? (the response)

**3.3** Open `/lab` (materials simulator). Set the China-dependency slider to
your country's exposure scenario and note the exposure grade and stockpile
clock. Then re-run at 70% and at 95% and write what changed in the grade and
why.

---

## Lesson 4: Defence & security architecture

**4.1** Label each instrument with *the threat it answers* and *EU or NATO or both*:

| Instrument | Threat answered | EU / NATO / both |
|---|---|---|
| €1.5bn European Defence Industry Programme | | |
| €90bn of joint debt (Ukraine support gap) | | |
| Emergency Security Protocol (EU's Article 4) | | |
| Counter-hybrid playbook | | |
| European Security Council | | |
| European Instrument for Strategic Enablers | | |

**4.2** In `/lab` build a Security Council. Record your chosen permanent
members and rotating members, then read the score the app computes. Now replace
two rotating members with two different ones — what happens to the score and
which factor changed it?

**4.3** Name one critique of the video's Article 4 suggestion raised by the
narrator or the sources.

---

## Lesson 5: Energy independence

**5.1** Fill in the numbers from the video/app:
- Electricity's share of energy use is set to **_______ by 2040**.
- This could cut the fossil import bill by **€\___ billion a year**.
- Europe added **\___ GW** of renewables last year; about **\___ × that**
  amount is waiting for a grid connection.

**5.2** Open `/lab` (energy simulator). Run it at the 2040 target and at a
lower target. Write down the projected import-bill saving in both runs and
explain the relationship you observe (is it linear? why?).

**5.3** Why does the video call a bigger, more connected grid a *security*
story rather than only a *climate* story? Answer in two sentences.

---

## Lesson 6: Climate preparedness

**6.1** Match the climate instrument to the hazard it answers:

| Instrument | Hazard |
|---|---|
| Climate Risk Insurance Alliance | |
| European Heatwave Plan | |
| Drought strategies | |
| European Firefighting Fleet | |
| Water initiative | |

**6.2** From `/policies` (Climate), quote the statistic about (a) how much
faster Europe warms than the global average, (b) what share of European
catastrophe losses are privately insured, (c) how much water is lost to
leakage.

**6.3** Propose one "resilience metric" you would track for your region
(e.g. % of homes with heatwave cooling) with a target and a source.

---

## Lesson 7: AI & technology sovereignty

**7.1** Classify each AI move as **Competitiveness**, **Security**, or **Both**:

| AI move | Classification |
|---|---|
| Massively increase European computing capacity | |
| Finance and fund European AI scale-ups and deep-tech | |
| AI gigafactories & technology sovereignty | |
| Cooperate with Canada/UK on frontier-model evaluation & safety | |
| Industrial AI pushed into 5 priority sectors | |
| "Pacing the frontier" conversations with frontier labs | |

**7.2** Name the five priority sectors for industrial AI deployment.

**7.3** Open `/policies` (AI & Tech) and list one figure the card uses and one
policy instrument named in the video's coverage.

---

## Lesson 8: Partnerships & trade diversification

**8.1** Rank these four partners by *readiness for associate membership* and
justify your order: Canada, Ukraine, New Zealand, Norway.

| Rank | Country | Status in the video | Why this rank |
|---|---|---|---|
| 1 | | | |
| 2 | | | |
| 3 | | | |
| 4 | | | |

**8.2** Write a 2-sentence definition of "associate membership" based on what
the video and `/partners` say, including the fact that the category "doesn't
formally exist".

**8.3** The video mentions trade agreements with India and Mercosur and doors
opening for New Zealand and Australia. In `/partners` find the Middle Corridor
figure (Global Gateway investment) and the China trade deficit per day, and
write both numbers down.

---

## Lesson 9: Media literacy

**9.1** In `/sources`, read the left-frame and right-frame blurbs about the
Canada announcement. Underline the shared facts; circle the words that carry
the different frames.

**9.2** Rewrite this neutral blurb twice — once with a positive frame, once with
a sceptical frame — without changing any facts:

> "The Commission has invited Canada to explore becoming the first associate
> member of the EU. No such status currently exists in EU law."

**9.3** Name two things you would verify before re-sharing a claim about the
speech (e.g. source, date, check the primary text).

---

## Integration Project — Independence Scorecard Rubric

Use this rubric when scoring a peer's scorecard:

| Criterion | What to check | Points |
|---|---|---|
| Cover every theme | At least 8 themes scored (economy, raw materials, defence, energy, climate, AI, partnerships, media) | /10 |
| Evidence quality | Each score backed by a stat visible in the app/sources; no unsourced claims | /20 |
| Gap list | One named dependency per theme, not generalities | /15 |
| 3-year lookahead | Scores tied to specific proposals ("if delivered, X moves score from Y to Z") | /15 |
| "Who decides" | Per-theme decision-maker row (Commission / member states / NATO / markets) | /10 |
| Method note | Scoring rules defined before applying them | /10 |
| Defence in review | Student holds a score against a sceptical reviewer using evidence | /20 |
| **Total** | | **/100** |