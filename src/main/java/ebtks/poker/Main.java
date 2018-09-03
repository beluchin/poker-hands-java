package ebtks.poker;

import com.google.common.collect.ImmutableSet;

import java.util.*;
import java.util.function.Function;
import java.util.function.Predicate;

import static com.google.common.base.Predicates.equalTo;
import static com.google.common.collect.ImmutableList.toImmutableList;
import static com.google.common.collect.ImmutableSet.toImmutableSet;
import static com.google.common.collect.Iterables.getLast;
import static com.google.common.collect.Lists.reverse;
import static com.google.common.collect.Streams.zip;
import static java.util.Arrays.stream;
import static java.util.Comparator.comparing;
import static java.util.stream.Collectors.groupingBy;

public class Main {

    // in order
    enum Rank {
        // FourOfAKind, StraightFlush, RoyalFlush,

        FullHouse{
            @Override
            public Set<Card> evaluate(Hand hand) {
                throw new UnsupportedOperationException();
            }
        },

        Flush {
            @Override
            public Set<Card> evaluate(Hand hand) {
                final Set<Card> cards = hand.cards();
                return allSameSuit(cards)? cards : ImmutableSet.of();
            }

            private boolean allSameSuit(Set<Card> cards) {
                return cards.stream()
                        .map(Card::suit)
                        .allMatch(equalTo(getFirst(cards).suit()));
            }
        },

        Straight {
            @Override
            public Set<Card> evaluate(Hand hand) {
                List<Symbol> sortedSymbols = sorted(symbols(hand));
                return getLast(sortedSymbols).ordinal() -
                        getFirst(sortedSymbols).ordinal() == sortedSymbols.size() - 1?
                        hand.cards(): ImmutableSet.of();
            }
        },

        ThreeOfAKind{
            @Override
            public Set<Card> evaluate(Hand hand) {
                return manyOfAKind(hand, 3);
            }
        },

        TwoPairs {
            @Override
            public Set<Card> evaluate(Hand hand) {
                final Map<Symbol, List<Card>> symbols = hand.cards().stream()
                        .collect(groupingBy(Card::symbol));
                Set<Card> repeatedSymbols = symbols.keySet().stream()
                        .filter(s -> symbols.get(s).size() > 1)
                        .limit(2)
                        .map(symbols::get)
                        .map(l -> l.subList(0, 2))
                        .flatMap(List::stream)
                        .collect(toImmutableSet());
                return repeatedSymbols.size() >= 4?
                        repeatedSymbols:
                        ImmutableSet.of();
            }
        },

        OnePair {
            @Override
            public Set<Card> evaluate(Hand hand) {
                return manyOfAKind(hand, 2);
            }
        },

        High {
            @Override
            public Set<Card> evaluate(Hand hand) {
                return hand.cards().stream()
                        .sorted(comparing(Card::symbol).reversed())
                        .limit(1)
                        .collect(toImmutableSet());
            }
        };

        public abstract Set<Card> evaluate(Hand hand);

        private static Set<Card> manyOfAKind(Hand hand, int n) {
            final Map<Symbol, List<Card>> symbols = hand.cards().stream()
                    .collect(groupingBy(Card::symbol));
            return symbols.keySet().stream()
                    .filter(s -> symbols.get(s).size() >= n)
                    .findFirst()
                    .map(s -> symbols.get(s).stream()
                            .limit(n)
                            .collect(toImmutableSet()))
                    .orElse(ImmutableSet.of());
        }
    }

    public enum Result {
        left, right, none
    }

    enum Suit {
        Spade('S'), Club('C'), Heart('H'), Diamond('D');

        private final char character;

        Suit(char character) {
            this.character = character;
        }

        public static Suit of(char c) {
            return stream(Suit.values())
                    .filter(matches(c))
                    .findFirst()
                    .orElseThrow(() -> new IllegalArgumentException());
        }

        private static Predicate<? super Suit> matches(char c) {
            return s -> s.character == c;
        }

    }

    // in order
    enum Symbol {
        Two('2'), Three('3'), Four('4'), Five('5'), Six('6'), Seven('7'),
        Eight('8'), Nine('9'), Ten('T'), Jack('J'), Queen('Q'), King('K'), Ace('A');

        private final char character;

        Symbol(char character) {
            this.character = character;
        }

        public static Symbol of(char c) {
            return stream(Symbol.values())
                    .filter(matches(c))
                    .findFirst()
                    .orElseThrow(() -> new IllegalArgumentException());
        }

        private static Predicate<? super Symbol> matches(char c) {
            return s -> s.character == c;
        }

    }

    static final class Card {
        private final Symbol symbol;
        private final Suit suit;

        private Card(Suit suit, Symbol symbol) {
            this.symbol = symbol;
            this.suit = suit;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            Card card = (Card) o;

            if (symbol != card.symbol) return false;
            return suit == card.suit;
        }

        @Override
        public int hashCode() {
            int result = symbol.hashCode();
            result = 31 * result + suit.hashCode();
            return result;
        }

        public static Card of(String s) {
            if (s.length() != 2) {
                throw new IllegalArgumentException();
            }
            return new Card(
                    Suit.of(s.charAt(1)),
                    Symbol.of(s.charAt(0))
            );
        }

        Suit suit() {
            return suit;
        }

        Symbol symbol() {
            return symbol;
        }
    }

    static final class Hand {
        private final Set<Card> cards; // 5 and only 5

        private Hand(Set<Card> cards) {
            this.cards = cards;
        }

        public Set<Card> cards() {
            return cards;
        }

        public static Hand of(String s) {
            String[] tokens = s.split(" ");
            if (tokens.length != 5) {
                throw new IllegalArgumentException();
            }
            return new Hand(stream(tokens)
                    .map(Card::of)
                    .collect(toImmutableSet()));
        }
    }

    private static class Pair<L, R> {
        public final L first;
        public final R second;

        private Pair(L first, R second) {
            this.first = first;
            this.second = second;
        }

        public static <L, R> Pair<L, R> of(L l, R r) {
            return new Pair<>(l, r);
        }
    }

    static class RankEvaluation extends Pair<Rank, Set<Card>> {
        private RankEvaluation(Rank first, Set<Card> second) {
            super(first, second);
        }

        public static RankEvaluation of(Rank r, Set<Card> s) {
            return new RankEvaluation(r, s);
        }
    }

    public static Result compare(Hand left, Hand right) {
        return compare(rankEvaluation(left), rankEvaluation(right))
                .orElse(compareHighestSymbol(left, right));
    }

    public static void main(String[] args) {
        System.out.println(Symbol.Ace.ordinal() > Symbol.Two.ordinal());
    }

    private static Predicate<RankEvaluation> applicableRankEvaluation() {
        return p -> !p.second.isEmpty();
    }

    private static Optional<Result> compare(
            RankEvaluation leftEval,
            RankEvaluation rightEval) {
        // different ranks
        if (leftEval.first.ordinal() < rightEval.first.ordinal()) {
            return Optional.of(Result.left);
        } else if (rightEval.first.ordinal() < leftEval.first.ordinal()) {
            return Optional.of(Result.right);
        }

        // same rank
        Symbol leftHighest = highestSymbol(leftEval.second),
                rightHighest = highestSymbol(rightEval.second);
        return Optional.ofNullable(
                leftHighest.ordinal() < rightHighest.ordinal()? Result.right:
                rightHighest.ordinal() < leftHighest.ordinal()? Result.left:
                null);
    }

    private static Result compareHighestSymbol(Hand left, Hand right) {
        List<Symbol> leftSymbols = reverse(sorted(symbols(left))),
                rightSymbols = reverse(sorted(symbols(right)));
        return zip(leftSymbols.stream(), rightSymbols.stream(), Main::compareSymbol)
                .filter(r -> r != Result.none)
                .findFirst()
                .orElse(Result.none);
    }

    private static Result compareSymbol(Symbol left, Symbol right) {
        return left.ordinal() < right.ordinal()? Result.right:
               left.ordinal() > right.ordinal()? Result.left:
               Result.none;
    }

    private static Function<Rank, RankEvaluation> evaluate(Hand hand) {
        return r -> RankEvaluation.of(r, r.evaluate(hand));
    }

    private static <T> T getFirst(Iterable<T> iter) {
        return iter.iterator().next();
    }

    private static Symbol highestSymbol(Set<Card> cards) {
        return cards.stream()
                .map(Card::symbol)
                .sorted(comparing(Symbol::ordinal).reversed())
                .findFirst()
                .get();
    }

    private static Symbol highestSymbol(Hand h) {
        return highestSymbol(h.cards());
    }

    private static RankEvaluation rankEvaluation(Hand hand) {
        return stream(Rank.values())
                .map(evaluate(hand))
                .filter(applicableRankEvaluation())
                .findFirst()
                .get();
    }

    private static List<Symbol> sorted(Collection<Symbol> symbols) {
        return symbols.stream()
                .sorted(comparing(Symbol::ordinal))
                .collect(toImmutableList());
    }

    private static List<Symbol> symbols(Hand hand) {
        return hand.cards().stream()
                .map(Card::symbol)
                .collect(toImmutableList());
    }

    private static <T> T throw_(RuntimeException ex) {
        throw ex;
    }

}
