package com.example.codesmells.domain;

import org.junit.jupiter.api.Test;
import org.springframework.core.io.ClassPathResource;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * The metrics are the app's evidence, so they must move in the direction the
 * lesson claims. These tests pin that direction per lab.
 */
class SniffMetricsTest {

    private static String snippet(String slug, boolean after) throws IOException {
        return new ClassPathResource("/snippets/%s/%s.java".formatted(slug, after ? "After" : "Before"))
                .getContentAsString(StandardCharsets.UTF_8);
    }

    private static SniffMetrics before(String slug) throws IOException {
        return SniffMetrics.of(snippet(slug, false));
    }

    private static SniffMetrics after(String slug) throws IOException {
        return SniffMetrics.of(snippet(slug, true));
    }

    @Test
    void longMethod_shrinks_the_longest_method() throws IOException {
        assertThat(after("long-method").longestMethod())
                .isLessThan(before("long-method").longestMethod());
    }

    @Test
    void godClass_spreads_its_fields_over_several_types() throws IOException {
        assertThat(after("god-class").fieldCount())
                .isLessThan(before("god-class").fieldCount());
    }

    @Test
    void dataClass_behaviour_moves_onto_the_data() throws IOException {
        // the bag of getters becomes a class that carries rules: more methods,
        // and the service that used to do the thinking is gone
        assertThat(after("data-class").methodCount())
                .isGreaterThan(before("data-class").methodCount());
        assertThat(after("data-class").typeMentions())
                .isLessThan(before("data-class").typeMentions());
    }

    @Test
    void primitiveObsession_introduces_types() throws IOException {
        assertThat(after("primitive-obsession").typeMentions())
                .isGreaterThan(before("primitive-obsession").typeMentions());
    }

    @Test
    void switchStatements_behaviour_leaves_the_branches() throws IOException {
        assertThat(after("switch-statements").branchCount())
                .isLessThan(before("switch-statements").branchCount());
    }

    @Test
    void lazyClass_and_middleMan_delete_code() throws IOException {
        assertThat(after("lazy-class").codeLines()).isLessThan(before("lazy-class").codeLines());
        assertThat(after("middle-man").codeLines()).isLessThan(before("middle-man").codeLines());
    }

    @Test
    void comments_lose_their_comment_lines() throws IOException {
        assertThat(after("comments").commentLines())
                .isLessThan(before("comments").commentLines());
    }

    @Test
    void classObsession_flattens_the_hierarchy() throws IOException {
        // eight types collapse into four: the empty floors are gone
        assertThat(after("class-obsession").typeMentions())
                .isLessThan(before("class-obsession").typeMentions());
    }

    @Test
    void inappropriateIntimacy_cuts_the_long_reach() throws IOException {
        assertThat(after("inappropriate-intimacy").longestMethod())
                .isLessThan(before("inappropriate-intimacy").longestMethod());
    }

    @Test
    void temporaryField_loses_a_field_and_its_null_guards() throws IOException {
        assertThat(after("temporary-field").fieldCount())
                .isLessThan(before("temporary-field").fieldCount());
    }

    @Test
    void featureEnvy_shrinks_without_growing_a_method() throws IOException {
        // the method moves house rather than getting longer
        assertThat(after("feature-envy").codeLines())
                .isLessThan(before("feature-envy").codeLines());
    }

    @Test
    void speculativeGenerality_shrinks_hard() throws IOException {
        assertThat(after("speculative-generality").codeLines())
                .isLessThan(before("speculative-generality").codeLines() / 2);
    }

    @Test
    void hiddenBugs_gets_wider_types_and_louder_bounds() throws IOException {
        // the refactor trades a couple of lines for explicit validation
        assertThat(after("hidden-bugs").codeLines())
                .isGreaterThanOrEqualTo(before("hidden-bugs").codeLines());
    }
}