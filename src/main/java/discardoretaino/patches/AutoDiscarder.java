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

import java.util.*;

public class AutoDiscarder {
    public static String modID; //Edit your pom.xml to change this
    public static final Logger logger = LogManager.getLogger(modID); //Used to output to the console.

    @SpirePatch(
            clz = HandCardSelectScreen.class,
            method = "open",
            paramtypez = {
                    String.class, int.class, boolean.class
            }
    )
    public static class CustomDiscardPatch {
        @SpirePostfixPatch()
        public static void Postfix(HandCardSelectScreen __instance, String msg,
                                   int amount, boolean anyNumber, CardGroup ___hand,
                                   String ___message) {
            // This check is to make sure that we're discarding here, not
            // putting on top of our draw pile (for example)
            String[] split_message = ___message.split(" ");
            if (split_message[split_message.length - 1].equals("Discard") && ___hand != null) {
                // Get indexes of cards to discard
                ArrayList<Integer> cardIndexesToDiscard = CustomDiscardPatch.getCardsToDiscard(___hand);

                // If there are cards to suggest, suggest them
                if (!cardIndexesToDiscard.isEmpty()) {  // This check seems necessary for whatever reason
                    // TODO: Handle the case of multiple discards here
                    __instance.hoveredCard = ___hand.group.get(cardIndexesToDiscard.get(0));
                    ReflectionHacks.privateMethod(HandCardSelectScreen.class, "selectHoveredCard").invoke(__instance);
                }
            }
        }

        private static ArrayList<Integer> getCardsToDiscard(CardGroup hand) {
            /* Determine which cards should be auto-selected for discard.

               This method returns an array of numbers, each corresponding to an index
               in the list of cards in the hand.

               The logic here ignores ethereal status/curse cards.
             */
            ArrayList<Integer> discardIndexes = new ArrayList<Integer>();

            // 1. Hand is empty
            if (hand.group.isEmpty()) {
                return discardIndexes;
            }

            // Used in (3) and (4)
            int numDistinctStatusCurseCards = getHandDistinctStatusCurseCards(hand);

            ArrayList<String> goodDiscardCards = new ArrayList<>();
            goodDiscardCards.add(Reflex.ID);
            goodDiscardCards.add(Tactician.ID);
            int handGoodDiscardCardsCount = getUniqueCountOfGivenCardsInHand(goodDiscardCards, hand);

            // 2. Hand has all copies of the same card (upgraded or otherwise)
            if (isHandAllSameCard(hand)) {
                // Find upgraded and non-upgraded card indexes
                int bestDiscardIndex = getBestDiscardIndexFromUniformHand(hand);

                if (bestDiscardIndex != -1) {
                    discardIndexes.add(bestDiscardIndex);
                }

                return discardIndexes;
            }

            // 3. Hand has (no status/curse cards && (Reflex xor Tactician))
            else if (numDistinctStatusCurseCards == 0 && handGoodDiscardCardsCount == 1) {
                discardIndexes.add(getHandReflexTacticianIndex(hand));
            }

            // 4. Hand has (only one type of status/curse card && !(Reflex or Tactician))
            else if (numDistinctStatusCurseCards == 1 && handGoodDiscardCardsCount == 0) {
                discardIndexes.add(getHandStatusCurseCardIndex(hand));
            }

            return discardIndexes;
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

        private static int getBestDiscardIndexFromUniformHand(CardGroup hand) {
            /* Return the index of the best card to discard given a hand of all the same cards.

               This ignores ethereal status/curse cards.

               This method will return the following:
                   - The index of the first non-upgraded card
                   - The index of the first upgraded card
                   - -1, to indicate that no card should be discarded

               For example:
                   [Strike+, Strike+, Strike ] -> 2
                   [Strike+, Strike+, Strike+] -> 0
                   [Dazed,   Strike,  Strike ] -> 1
             */
            int upgradedIndex = -1;
            for (int cardIndex = 0; cardIndex < hand.group.size(); cardIndex++) {
                AbstractCard card = hand.group.get(cardIndex);

                // Ignore this card if it's an ethereal status/curse
                if (isCardEtherealStatusOrCurse(card)) {
                    continue;
                }

                // Return the first non-upgraded index we see
                if (!card.upgraded) {
                    return cardIndex;
                }

                // Keep track of the first upgraded card in the hand
                if (upgradedIndex == -1 && card.upgraded) {
                    upgradedIndex = cardIndex;
                }
            }

            // If we saw an upgraded card, return its index
            if (upgradedIndex != -1) { return upgradedIndex; }
            return -1;
        }

        private static int getHandDistinctStatusCurseCards(CardGroup hand) {
            /* Return the number of status/curse cards in the hand.

               Ignores ethereal cards.
             */
            Set statusCurseNames = new HashSet<String>();
            for (int cardIndex = 0; cardIndex < hand.group.size(); cardIndex++) {
                AbstractCard card = hand.group.get(cardIndex);

                // Only count this card if it's a status/curse and it's NOT ethereal
                if (isCardStatusOrCurse(card) && !card.isEthereal) {
                    statusCurseNames.add(card.name);
                }
            }

            return statusCurseNames.size();
        }

        private static int getUniqueCountOfGivenCardsInHand(ArrayList<String> cards,
                                                            CardGroup hand) {
            /* Return the number of given card ID's that appear in hand, not counting duplicates. */
            // Create map from card ID -> false
            Map<String, Boolean> seen = new HashMap<String, Boolean>();
            for (int cardIndex = 0; cardIndex < cards.size(); cardIndex++) {
                seen.put(cards.get(cardIndex), false);
            }

            // Mark each card in hand as seen if it is in the list of given cards
            for (int cardIndex = 0; cardIndex < hand.group.size(); cardIndex++) {
                String cardID = hand.group.get(cardIndex).cardID;
                if (seen.containsKey(cardID)) {
                    seen.put(cardID, true);
                }
            }

            // Find the count of seen cards
            int count = 0;
            for (Map.Entry<String, Boolean> entry : seen.entrySet()) {
                if (entry.getValue() == true) {
                    count++;
                }
            }

            return count;
        }

        private static int getHandStatusCurseCardIndex(CardGroup hand) {
            /* Return the index of the first status/curse card in the hand, -1 if none exist.

               Ignores ethereal cards.
             */
            for (int cardIndex = 0; cardIndex < hand.group.size(); cardIndex++) {
                AbstractCard card = hand.group.get(cardIndex);
                if (isCardStatusOrCurse(card) && !card.isEthereal) {
                    return cardIndex;
                }
            }

            return -1;
        }

        private static int getHandReflexTacticianIndex(CardGroup hand) {
            /* Return the index of the first Reflex/Tactician card in the hand, -1 if none exist. */
            for (int cardIndex = 0; cardIndex < hand.group.size(); cardIndex++) {
                String cardID = hand.group.get(cardIndex).cardID;
                if (cardID == Reflex.ID || cardID == Tactician.ID) {
                    return cardIndex;
                }
            }

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

