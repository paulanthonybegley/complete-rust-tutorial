package com.example.codesmells.domain;

import java.util.ArrayList;
import java.util.List;

/**
 * A tiny line-based diff (Longest Common Subsequence, O(n*m)) used to show
 * exactly which lines the refactoring touched. Good enough for snippets that
 * are a few hundred lines long.
 */
public final class Diff {

    public enum Kind { SAME, REMOVED, ADDED }

    public record Line(Kind kind, int beforeLine, int afterLine, String text) {}

    private Diff() {}

    public static List<Line> lcs(String before, String after) {
        String[] a = before.split("\\R", -1);
        String[] b = after.split("\\R", -1);
        int n = a.length, m = b.length;

        int[][] dp = new int[n + 1][m + 1];
        for (int i = n - 1; i >= 0; i--) {
            for (int j = m - 1; j >= 0; j--) {
                dp[i][j] = a[i].equals(b[j])
                        ? dp[i + 1][j + 1] + 1
                        : Math.max(dp[i + 1][j], dp[i][j + 1]);
            }
        }

        List<Line> out = new ArrayList<>();
        int i = 0, j = 0, bi = 0, ai = 0;
        while (i < n && j < m) {
            if (a[i].equals(b[j])) {
                out.add(new Line(Kind.SAME, ++bi, ++ai, a[i]));
                i++;
                j++;
            } else if (dp[i + 1][j] >= dp[i][j + 1]) {
                out.add(new Line(Kind.REMOVED, ++bi, -1, a[i++]));
            } else {
                out.add(new Line(Kind.ADDED, -1, ++ai, b[j++]));
            }
        }
        while (i < n) {
            out.add(new Line(Kind.REMOVED, ++bi, -1, a[i++]));
        }
        while (j < m) {
            out.add(new Line(Kind.ADDED, -1, ++ai, b[j++]));
        }
        return out;
    }

    public static int changedLines(List<Line> lines) {
        return (int) lines.stream().filter(l -> l.kind() != Kind.SAME).count();
    }
}