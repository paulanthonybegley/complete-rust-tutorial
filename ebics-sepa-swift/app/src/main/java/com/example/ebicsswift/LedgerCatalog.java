package com.example.ebicsswift;

import java.util.List;

public final class LedgerCatalog {
	public static final List<LedgerConcept> CONCEPTS = List.of(
		new LedgerConcept("ebics", "EBICS — the encrypted channel", "EBICS (Electronic Banking Internet Communication Standard)",
			"postgres:5549", "open a MAC'd, authenticated channel before one transmission moves",
			"postgres live on 5549 — compose service 'ledger-postgres'",
			"GET /api/ebics \u2192 the channel, verified"),
		new LedgerConcept("sepa", "SEPA — one euro, one area", "SEPA (Single Euro Payments Area)",
			"postgres:5549", "move \u20AC between any two accounts under one rulebook",
			"postgres live on 5549 — compose service 'ledger-postgres'",
			"GET /api/sepa \u2192 the area, verified"),
		new LedgerConcept("swift", "SWIFT — 11,000 institutions, one message", "SWIFT (Society for Worldwide Interbank Financial Telecommunication)",
			"postgres:5549", "route one canonical message across the backbone",
			"postgres live on 5549 — compose service 'ledger-postgres'",
			"GET /api/swift \u2192 the backbone, verified"),
		new LedgerConcept("pain", "PAIN — the transfer order", "Payment Initiation (pain.001)",
			"postgres:5549", "the debtor specifies the target of a transfer order",
			"postgres live on 5549 — compose service 'ledger-postgres'",
			"GET /api/pain \u2192 the order, verified"),
		new LedgerConcept("pacs", "PACS — the settlement trail", "Payment Clearing + Settlement (pacs.002 / pacs.008)",
			"postgres:5549", "the clearing house's acceptance or rejection of the transfer",
			"postgres live on 5549 — compose service 'ledger-postgres'",
			"GET /api/pacs \u2192 the trail, verified"),
		new LedgerConcept("camt", "CAMT — the statement proof", "Cash Management (camt.053)",
			"postgres:5549", "the account statement that proves arrival",
			"postgres live on 5549 — compose service 'ledger-postgres'",
			"GET /api/camt \u2192 the proof, verified"));
}
