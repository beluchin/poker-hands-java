package ebtks.poker;

import com.google.common.collect.ImmutableSet;
import ebtks.poker.Main.Card;
import ebtks.poker.Main.Hand;
import org.hamcrest.Matcher;
import org.junit.Test;

import java.util.Set;

import static com.google.common.collect.ImmutableSet.toImmutableSet;
import static ebtks.poker.Main.Rank.*;
import static java.util.Arrays.stream;
import static org.hamcrest.MatcherAssert.assertThat;

public class RankTest {
    @Test
    public void _1_high() {
        assertThat(evaluate(High, "6D 7H AH 7S QC"), equalTo("AH"));
    }

    @Test
    public void _2_onePair() {
        assertThat(evaluate(OnePair, "2S 3H 2H 4D 5C"), equalTo("2S 2H"));
    }

    @Test
    public void _3_twoPairs() {
        assertThat(evaluate(TwoPairs, "2S 2H 3S 3H 4S"), equalTo("2S 2H 3S 3H"));
    }

    @Test
    public void _4_threeOfAKind() {
        assertThat(
                evaluate(ThreeOfAKind, "2S 3H 2H 4H 2D"),
                equalTo("2S 2H 2D"));
    }

    @Test
    public void _5_straight() {
        assertThat(
                evaluate(Straight, "2D 4H 3S 6C 5D"),
                equalTo("2D 4H 3S 6C 5D"));
    }

    @Test
    public void _6_flush() {
        assertThat(
                evaluate(Flush, "2D TD 6D 8D 4D"),
                equalTo("2D TD 6D 8D 4D"));
    }

    @Test
    public void _7_fullHouse() {
        assertThat(
                evaluate(FullHouse, "2D 3H 2S 3D 3C"),
                equalTo("2D 3H 2S 3D 3C"));
    }

    private static ImmutableSet<Card> cards(String... ss) {
        return stream(ss)
                .map(Card::of)
                .collect(toImmutableSet());
    }

    private static Matcher<Set<Card>> equalTo(String s) {
        return org.hamcrest.CoreMatchers.equalTo(cards(s.split(" ")));
    }

    private static Set<Card> evaluate(Main.Rank rank, String cards) {
        return rank.evaluate(Hand.of(cards));
    }
}
