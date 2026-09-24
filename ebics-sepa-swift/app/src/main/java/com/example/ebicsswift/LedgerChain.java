package com.example.ebicsswift;

import java.security.MessageDigest;
import java.security.SecureRandom;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import org.springframework.stereotype.Service;

@Service
public class LedgerChain {
	public record Block(int index, String slug, String role, String prevHash, String hash) {}
	private final List<Block> chain = new ArrayList<>();
	private final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();
	private static final SecureRandom RNG = new SecureRandom();

	public LedgerChain() {
		chain.add(new Block(0, "genesis", "conservation — every euro in is a euro out", "0".repeat(64),
			sha("0|genesis|conservation|" + "0".repeat(64))));
		append("ebics", "the authenticated channel: nobody enters unauthenticated");
		append("sepa", "the rulebook: one euro moves by one set of rules");
		append("swift", "the backbone: one canonical message crosses 11,000 institutions");
		append("pain", "the order: the debtor specifies the transfer");
		append("pacs", "the settlement: the clearing house accepts or rejects");
		append("camt", "the proof: the account statement shows arrival");
	}

	private String sha(String s) {
		try {
			MessageDigest d = MessageDigest.getInstance("SHA-256");
			StringBuilder sb = new StringBuilder(64);
			for (byte b : d.digest(s.getBytes(StandardCharsets.UTF_8))) sb.append(String.format("%02x", b));
			return sb.toString();
		} catch (Exception e) { throw new IllegalStateException(e); }
	}

	private void append(String slug, String role) {
		String prev = chain.get(chain.size() - 1).hash();
		int i = chain.size();
		chain.add(new Block(i, slug, role, prev, sha(i + "|" + slug + "|" + role + "|" + prev)));
	}

	public Map<String, Object> verify() {
		lock.readLock().lock();
		try {
			Map<String, Object> out = new LinkedHashMap<>();
			List<Map<String, Object>> issues = new ArrayList<>();
			String prev = chain.get(0).hash();
			boolean ok = true;
			for (int i = 1; i < chain.size(); i++) {
				Block b = chain.get(i);
				String expect = sha(i + "|" + b.slug() + "|" + b.role() + "|" + prev);
				if (!b.prevHash().equals(prev) || !b.hash().equals(expect)) {
					ok = false;
					Map<String, Object> m = new LinkedHashMap<>();
					m.put("at", i); m.put("slug", b.slug()); m.put("role", b.role());
					m.put("expectedPrev", prev.substring(0, 16)); m.put("actualPrev", b.prevHash().substring(0, 16));
					m.put("expectedHash", expect.substring(0, 16)); m.put("actualHash", b.hash().substring(0, 16));
					issues.add(m);
					break; // Hamilton: show the FIRST break (the priority), not the whole pile
				}
				prev = b.hash();
			}
			out.put("ok", ok);
			out.put("blocks", chain.size());
			out.put("firstBreakAt", issues.isEmpty() ? null : issues.get(0).get("at"));
			out.put("issues", issues);
			return out;
		} finally { lock.readLock().unlock(); }
	}

	public String corruptRandomly() {
		lock.writeLock().lock();
		try {
			if (chain.size() < 3) return "not enough blocks to corrupt";
			int p = 1 + RNG.nextInt(chain.size() - 1);
			Block b = chain.get(p);
			String bad = sha(p + "|" + b.slug() + "|" + b.role() + "//tamper|" + b.prevHash());
			chain.set(p, new Block(p, b.slug(), b.role() + "//tamper", b.prevHash(), bad));
			return "corrupted block " + p;
		} finally { lock.writeLock().unlock(); }
	}

	public Map<String, Object> blocks() {
		List<Map<String, Object>> out = new ArrayList<>();
		for (Block b : chain) {
			Map<String, Object> m = new LinkedHashMap<>();
			m.put("index", b.index()); m.put("slug", b.slug()); m.put("role", b.role());
			m.put("prevHash", b.prevHash().substring(0, 16)); m.put("hash", b.hash().substring(0, 16));
			out.add(m);
		}
		return Map.of("blocks", out, "count", out.size());
	}
}
