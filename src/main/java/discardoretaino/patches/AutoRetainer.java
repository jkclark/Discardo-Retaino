package discardoretaino.patches;

import basemod.ReflectionHacks;
import com.evacipated.cardcrawl.modthespire.lib.SpirePatch;
import com.evacipated.cardcrawl.modthespire.lib.SpirePostfixPatch;
import com.megacrit.cardcrawl.cards.AbstractCard;
import com.megacrit.cardcrawl.cards.CardGroup;
import com.megacrit.cardcrawl.cards.green.Reflex;
import com.megacrit.cardcrawl.cards.green.Tactician;
import com.megacrit.cardcrawl.cards.purple.Perseverance;
import com.megacrit.cardcrawl.cards.purple.SandsOfTime;
import com.megacrit.cardcrawl.cards.purple.WindmillStrike;
import com.megacrit.cardcrawl.screens.select.HandCardSelectScreen;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

public class AutoRetainer {
    public static String modID; //Edit your pom.xml to change this
    public static final Logger logger = LogManager.getLogger(modID); //Used to output to the console.

    @SpirePatch(
            clz = HandCardSelectScreen.class,
            method = "open",
            paramtypez = {
                    String.class, int.class, boolean.class, boolean.class, boolean.class,
                    boolean.class, boolean.class
            }
    )
    public static class CustomRetainPatch {
        @SpirePostfixPatch()
        public static void Postfix(HandCardSelectScreen __instance, String msg,
                                   int amount, boolean anyNumber,
                                   boolean canPickZero, boolean forTransform,
                                   boolean forUpgrade, boolean upTo, CardGroup ___hand,
                                   String ___message) {
            // This check is to make sure that we're retaining here, not
            // putting on top of our draw pile (for example)
            String[] split_message = ___message.split(" ");
            if (split_message[split_message.length - 1].equals("Retain") && ___hand != null) {
                // Get indexes of cards to retain
                ArrayList<Integer> cardIndexesToRetain = getCardIndexesToRetain(___hand, amount);

                // If there are cards to suggest, suggest them
                if (!cardIndexesToRetain.isEmpty()) {  // This check seems necessary for whatever reason
                    // Click on the card at each index
                    for (int retainIndex = 0; retainIndex < cardIndexesToRetain.size(); retainIndex++) {
                        // Select this card for retaining
                        __instance.hoveredCard =
                                ___hand.group.get(cardIndexesToRetain.get(retainIndex));
                        ReflectionHacks.privateMethod(HandCardSelectScreen.class, "selectHoveredCard").invoke(__instance);
                    }
                }
            }
        }

        private static ArrayList<Integer> getCardIndexesToRetain(CardGroup hand, int numToRetain) {
            /* Return an array of indexes of cards to retain.
             *
             * The returned array is returned in decreasing order so that we don't have to alter
             * the indexes as we remove them. If we removed from left to right, later indexes
             * would be affected by removing earlier indexes.
             */
            ArrayList<Integer> indexesToRetain = new ArrayList<>();

            // Copy hand so that we can remove cards when doing multiple retains
            CardGroup fakeHand = new CardGroup(hand, hand.type);

            for (int iteration = 0; iteration < numToRetain; iteration++) {
                // Find index of card to retain
                int indexToRetain = getCardIndexToRetain(fakeHand);

                // If there's no card to retain, continue
                if (indexToRetain < 0) {
                    continue;
                }

                // Add index to output list
                indexesToRetain.add(indexToRetain);

                // Remove card from fake hand
                fakeHand.removeCard(fakeHand.group.get(indexToRetain));
            }

            return indexesToRetain;
        }

        private static int getCardIndexToRetain(CardGroup hand) {
            /* Determine the index of the card that should be auto-selected for retain.
             *
             * Returns -1 if no card should be retained.
             * Ignores ethereal status/curses cards and cards with Retain.
             * Favors cards to the right over cards to the left. This is done to make retaining
             * multiple cards easier.
             */

            // 1. Hand is empty
            if (hand.group.isEmpty()) {
                return -1;
            }

            // 2. Hand has all copies of the same card (upgraded or otherwise)
            if (isHandAllSameCard(hand)) {
                return getBestRetainIndexFromUniformHand(hand);
            }

            return -1;
        }

        private static boolean isHandAllSameCard(CardGroup hand) {
            /* Check if every card in the hand has the same name (ignoring upgrades).

               Ignores ethereal status/curse cards and cards with Retain.
            */
            if (hand.group.isEmpty()) {
                return true;
            }

            Set cardIDs = new HashSet<String>();
            for (int cardIndex = 0; cardIndex < hand.group.size(); cardIndex++) {
                AbstractCard card = hand.group.get(cardIndex);

                // Ignore this card if it's an ethereal status/curse
                if (isCardEtherealStatusOrCurse(card)) {
                    continue;
                }

                // Ignore this card if it has Retain
                if (card.selfRetain) {
                    continue;
                }

                cardIDs.add(card.cardID);
            }

            return cardIDs.size() == 1;
        }

        private static int getBestRetainIndexFromUniformHand(CardGroup hand) {
            /* Return the index of the best card to retain given a hand of all the same cards.

               This ignores ethereal status/curse cards and cards with Retain.

               This function will favor a card with a higher index because it makes
               handling multiple retains easier.

               This method will return the following:
                   - The index of the rightmost upgraded card
                   - The index of the rightmost non-upgraded card
                   - -1, to indicate that no card should be retained

               For example:
                   [Strike+, Strike+, Strike ] -> 1
                   [Strike,  Strike+, Strike+] -> 2
                   [Strike,  Dazed,   Dazed  ] -> 0
                   [Windmill Strike, Dazed   ] -> -1
             */
            int unupgradedIndex = -1;
            for (int cardIndex = hand.group.size() - 1; cardIndex >= 0; cardIndex--) {
                AbstractCard card = hand.group.get(cardIndex);

                // Ignore this card if it's an ethereal status/curse
                if (isCardEtherealStatusOrCurse(card)) {
                    continue;
                }

                if (card.selfRetain) {
                    continue;
                }

                // Return the first upgraded index we see (iterating backwards)
                if (card.upgraded) {
                    return cardIndex;
                }

                // Keep track of the first upgraded card in the hand (iterating backwards)
                if (unupgradedIndex == -1 && !card.upgraded) {
                    unupgradedIndex = cardIndex;
                }
            }

            // If we saw an unupgraded card, return its index
            if (unupgradedIndex != -1) { return unupgradedIndex; }
            return -1;
        }

        private static boolean isCardEtherealStatusOrCurse(AbstractCard card) {
            /* Return true if card is ethereal and a status/curse, false otherwise. */
            return card.isEthereal && isCardStatusOrCurse(card);
        }

        private static boolean isCardStatusOrCurse(AbstractCard card) {
            return card.type == AbstractCard.CardType.STATUS || card.type == AbstractCard.CardType.CURSE;
        }
    }
}

