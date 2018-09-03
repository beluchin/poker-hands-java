package ebtks.poker;

import ebtks.poker.Main.Hand;
import ebtks.poker.Main.Result;
import org.junit.Test;

import static ebtks.poker.Main.Result.none;
import static ebtks.poker.Main.compare;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.IsEqual.equalTo;

public class EndToEndTest {
    @Test
    public void _1_differentRanks() {
        Hand left = Hand.of("2S 3S 2H 4S 5S");  // one pair
        Hand right = Hand.of("AH QH 9H 7H 5H"); // high
        assertThat(compare(left, right), equalTo(Result.left));
    }

    @Test
    public void _2_sameRankedHands_Rank_WithHighestValueWins() {
        Hand left = Hand.of("2S 2H 3S 4S 5S");  // pair of 2's
        Hand right = Hand.of("3S 3H 4S 5S 6S"); // pair of 3's

        assertThat(compare(left, right), equalTo(Result.right));
    }

    @Test
    public void _3_sameRankedHands_Hand_WithHighestValueWins() {
        Hand left = Hand.of("2S 2H 3S 4S 5S");  // pair of 2's
        Hand right = Hand.of("2S 2H 3S 4S 6S");  // pair of 2's - highest value 6S
        assertThat(compare(left, right), equalTo(Result.right));
    }

    @Test
    public void _4_sameRankedHands_HighestValueTie_NextHighestValueWins() {
        Hand left = Hand.of("2S 2H 3S 4S 7S");  // pair of 2's
        Hand right = Hand.of("2S 2H 3S 5S 7S");  // pair of 2's - highest value 5S after 7S
        assertThat(compare(left, right), equalTo(Result.right));
    }

    @Test
    public void _5_tie() {
        Hand left = Hand.of("5D 7H TC JH JS");
        Hand right = Hand.of("5S 7S TS JD JC");
        assertThat(compare(left, right), equalTo(none));
    }

    @Test
    public void test1() {
        assertThat(compare("JH 5D 7H TC JS JD JC TS 5S 7S"), equalTo(none));
    }

    private Main.Result compare(Hand left, Hand right) {
        return Main.compare(left, right);
    }

    private Main.Result compare(String s) {
        String left = s.substring(0, 14),
                right = s.substring(15, s.length());
        return Main.compare(Hand.of(left), Hand.of(right));
    }
}
