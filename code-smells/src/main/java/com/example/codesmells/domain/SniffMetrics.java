package com.example.codesmells.domain;

import java.util.LinkedHashSet;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * Dead-simple, deterministic line heuristics that turn a snippet into the
 * metrics shown in the lab panel. They are deliberately crude (brace counting,
 * not a parser) so learners can reason about what changed.
 *
 * @param codeLines     non-blank, non-comment lines
 * @param commentLines  lines that are comments only
 * @param methodCount   method declarations
 * @param longestMethod longest method body in lines (brace-depth walk)
 * @param fieldCount    field/assignment declarations
 * @param typeMentions  distinct type names used (built-ins excluded)
 * @param branchCount   switch/case lines, i.e. decision sprawl
 */
public record SniffMetrics(int codeLines, int commentLines, int methodCount,
                           int longestMethod, int fieldCount, int typeMentions,
                           int branchCount) {

    private static final Pattern FIELDISH =
            Pattern.compile("^[A-Za-z_][A-Za-z0-9_.]*\\s+[A-Za-z_][A-Za-z0-9_]*");

    private static final Set<String> CONTROL = Set.of(
            "if", "for", "while", "switch", "catch", "else", "do", "try", "return",
            "new", "throw", "synchronized", "case", "default", "break", "continue");

    private static final Set<String> BUILTINS = Set.of(
            "String", "Integer", "Long", "Double", "Boolean", "Character", "Object", "Void",
            "List", "Map", "Set", "Optional", "Collection", "Arrays", "Collections", "ArrayList",
            "HashMap", "HashSet", "LinkedHashSet", "Exception", "RuntimeException", "System",
            "Math", "Long.MIN_VALUE", "Long.MAX_VALUE", "BigDecimal", "LocalDate",
            "Record", "Field", "Collectors", "Predicate", "Function", "Stream");

    public static SniffMetrics of(String source) {
        String[] lines = source.split("\\R");
        int code = 0, comments = 0, methods = 0, fields = 0, branches = 0;
        boolean inBlockComment = false;
        Set<String> types = new LinkedHashSet<>();

        for (String raw : lines) {
            String stripped = raw.strip();

            boolean opensBlock = stripped.startsWith("/*");
            boolean isComment = stripped.startsWith("//")
                    || inBlockComment
                    || opensBlock
                    || stripped.startsWith("*");
            if (opensBlock) {
                inBlockComment = true;
            }
            if (stripped.contains("*/")) {
                inBlockComment = false;
            }
            if (isComment) {
                comments++;
                continue;
            }
            if (stripped.isEmpty()) {
                continue;
            }
            code++;

            String cleaned = clearComments(raw).strip();

            if (isField(cleaned)) {
                fields++;
            }
            if (isMethodDeclaration(cleaned)) {
                methods++;
            }
            if (cleaned.startsWith("case ") || cleaned.startsWith("default :")
                    || cleaned.startsWith("default:") || cleaned.startsWith("switch ")) {
                branches++;
            }
            collectTypes(cleaned, types);
        }

        return new SniffMetrics(code, comments, methods, longestMethodOf(lines), fields,
                types.size(), branches);
    }

    /** A declaration: parens, an opening brace, and not a control-flow keyword or a call. */
    private static boolean isMethodDeclaration(String line) {
        if (!line.contains("(") || !line.contains(")") || !line.endsWith("{")) {
            return false;
        }
        if (firstWord(line).isEmpty() || CONTROL.contains(firstWord(line))) {
            return false;
        }
        // method declarations are prefixed by a modifier, a return type, or both
        return line.matches(".*\\b(public|private|protected|static|final|abstract|void)\\b.*");
    }

    private static boolean isField(String line) {
        if (!line.endsWith(";") || line.endsWith(");")) {
            return false;
        }
        String first = firstWord(line);
        if (CONTROL.contains(first) || "import".equals(first) || "package".equals(first)) {
            return false;
        }
        return FIELDISH.matcher(line).find();
    }

    private static String firstWord(String line) {
        int i = 0;
        while (i < line.length() && (Character.isLetterOrDigit(line.charAt(i))
                || line.charAt(i) == '_' || line.charAt(i) == '.')) {
            i++;
        }
        return i == 0 ? "" : line.substring(0, i);
    }

    private static void collectTypes(String line, Set<String> into) {
        String withoutStrings = line.replaceAll("\"[^\"]*\"", "");
        for (String word : withoutStrings.split("[^A-Za-z0-9_.]+")) {
            if (word.length() > 1 && Character.isUpperCase(word.charAt(0)) && !BUILTINS.contains(word)) {
                into.add(word);
            }
        }
    }

    /**
     * Walks from each method declaration to its matching close brace and returns
     * the longest body. Blank and comment lines inside the body still count, which
     * is exactly the bloat a Long Method lab wants to expose.
     */
    private static int longestMethodOf(String[] lines) {
        int best = 0;
        for (int i = 0; i < lines.length; i++) {
            String cleaned = clearComments(lines[i]).strip();
            if (!isMethodDeclaration(cleaned)) {
                continue;
            }
            int depth = 0;
            int end = i;
            for (int j = i; j < lines.length; j++) {
                String body = clearComments(lines[j]);
                for (char c : body.toCharArray()) {
                    if (c == '{') {
                        depth++;
                    } else if (c == '}') {
                        depth--;
                    }
                }
                end = j;
                if (depth <= 0) {
                    break;
                }
            }
            best = Math.max(best, end - i + 1);
        }
        return best;
    }

    private static String clearComments(String line) {
        int idx = line.indexOf("//");
        return idx >= 0 ? line.substring(0, idx) : line;
    }
}