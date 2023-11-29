package discardoretaino.patches;

import basemod.ReflectionHacks;
import com.evacipated.cardcrawl.modthespire.lib.SpirePatch;
import com.evacipated.cardcrawl.modthespire.lib.SpirePostfixPatch;
import com.megacrit.cardcrawl.cards.AbstractCard;
import com.megacrit.cardcrawl.cards.CardGroup;
import com.megacrit.cardcrawl.cards.green.Reflex;
import com.megacrit.cardcrawl.cards.green.Tactician;
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
                ArrayList<Integer> cardIndexesToRetain = CustomRetainPatch.getCardsToRetain(___hand);

                // If there are cards to suggest, suggest them
                if (!cardIndexesToRetain.isEmpty()) {  // This check seems necessary for whatever reason
                    // TODO: Handle the case of multiple retains here
                    __instance.hoveredCard = ___hand.group.get(cardIndexesToRetain.get(0));
                    ReflectionHacks.privateMethod(HandCardSelectScreen.class, "selectHoveredCard").invoke(__instance);
                }
            }
        }

        private static ArrayList<Integer> getCardsToRetain(CardGroup hand) {
            /* Determine which cards should be auto-selected for retain.

               This method returns an array of numbers, each corresponding to an index
               in the list of cards in the hand.

               The logic here ignores ethereal status/curse cards.
             */
            ArrayList<Integer> retainIndexes = new ArrayList<Integer>();

            // 1. Hand is empty
            if (hand.group.isEmpty()) {
                return retainIndexes;
            }

            // 2. Hand has all copies of the same card (upgraded or otherwise)
            if (isHandAllSameCard(hand)) {
                // Find upgraded and non-upgraded card indexes
                int bestRetainIndex = getBestRetainIndexFromUniformHand(hand);

                if (bestRetainIndex != -1) {
                    retainIndexes.add(bestRetainIndex);
                }

                return retainIndexes;
            }

            return retainIndexes;
        }

        private static boolean isHandAllSameCard(CardGroup hand) {
            /* Check if every card in the hand has the same name (ignoring upgrades).

               Ignores ethereal status/curse cards.
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

                cardIDs.add(card.cardID);
            }

            return cardIDs.size() == 1;
        }

        private static int getBestRetainIndexFromUniformHand(CardGroup hand) {
            /* Return the index of the best card to retain given a hand of all the same cards.

               This ignores ethereal status/curse cards.

               This method will return the following:
                   - The index of the first upgraded card
                   - The index of the first non-upgraded card
                   - -1, to indicate that no card should be retained

               For example:
                   [Strike+, Strike+, Strike ] -> 0
                   [Strike,  Strike+, Strike+] -> 1
                   [Dazed,   Dazed,   Strike ] -> 2
             */
            int unupgradedIndex = -1;
            for (int cardIndex = 0; cardIndex < hand.group.size(); cardIndex++) {
                AbstractCard card = hand.group.get(cardIndex);

                // Ignore this card if it's an ethereal status/curse
                if (isCardEtherealStatusOrCurse(card)) {
                    continue;
                }

                // Return the first upgraded index we see
                if (card.upgraded) {
                    return cardIndex;
                }

                // Keep track of the first upgraded card in the hand
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

