package com.example.ebicsswift;

import java.util.List;

public final class LedgerCatalog {
	public static final List<LedgerConcept> CONCEPTS = List.of(
		new LedgerConcept("ebics", "EBICS — the encrypted channel", "EBICS (Electronic Banking Internet Communication Standard)",
			"postgres:5433", "open a MAC'd, authenticated pipe before one transmission moves",
			"get a real channel: compose 'postgres' + 'localstack' on 5433/4566",
			"GET /api/ebics \u2192 the channel, verified"),
		new LedgerConcept("sepa", "SEPA — one euro, one area", "SEPA (Single Euro Payments Area)",
			"postgres:5433", "move \u20AC between any two accounts in 36 countries under one rulebook",
			"live-compose when the postgres statement answers on 5433",
			"GET /api/sepa \u2192 the area, verified"),
		new LedgerConcept("swift", "SWIFT — the message backbone", "SWIFT (Society for Worldwide Interbank Financial Telecommunication)",
			"postgres:5433", "route one canonical message across 11,000 institutions",
			"live-compose when the stateless journal answers",
			"GET /api/swift \u2192 the backbone, verified"),
		new LedgerConcept("pain", "PAIN — the transfer order", "Payment Initiation (pain.001)",
			"postgres:5433", "the debtor says who pays whom, how many cents, through which layer",
			"live-compose when the transfer order persists",
			"GET /api/pain \u2192 the order, verified"),
		new LedgerConcept("pacs", "PACS — the settlement", "Payment Clearing + Settlement (pacs.008)",
			"postgres:5433", "the clearing house answers: accepted, rejected, or settled",
			"live-compose when the clearing result persists",
			"GET /api/pacs \u2192 the result, verified"),
		new LedgerConcept("camt", "CAMT — the statement", "Cash Management (camt.053)",
			"postgres:5433", "the account statement that proves the money arrived",
			"live-compose when the statement persists",
			"GET /api/camt \u2192 the statement, verified"),
		new LedgerConcept("fin", "FIN — the classic SWIFT message", "SWIFT FIN (MT103)",
			"postgres:5433", "the world's most repeated cross-border payment message",
			"live-compose when the FIN log persists",
			"GET /api/fin \u2192 the message, verified"),
		new LedgerConcept("mx", "MX — ISO 20022", "ISO 20022 (XML messages)",
			"postgres:5433", "one canonical schema every bank must speak by 2025",
			"live-compose when the MX envelope persists",
			"GET /api/mx \u2192 the envelope, verified"));

	private LedgerCatalog() {}
}
