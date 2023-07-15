package discardoretaino.patches;

import basemod.ReflectionHacks;
import com.evacipated.cardcrawl.modthespire.lib.SpirePatch;
import com.evacipated.cardcrawl.modthespire.lib.SpirePostfixPatch;
import com.megacrit.cardcrawl.cards.CardGroup;
import com.megacrit.cardcrawl.screens.select.HandCardSelectScreen;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;

public class AutoDiscarder {
    public static String modID; //Edit your pom.xml to change this
    public static final Logger logger = LogManager.getLogger(modID); //Used to output to the console.

    @SpirePatch(
            clz=HandCardSelectScreen.class,
            method="open",
            paramtypez={
                    String.class, int.class, boolean.class
            }
    )
    public static class CustomDiscardPatch {
        @SpirePostfixPatch()
        public static void Postfix(HandCardSelectScreen __instance, String msg, int amount, boolean anyNumber, CardGroup ___hand) {
            if (___hand != null) {
                for (int cardIndex = 0; cardIndex < ___hand.group.size(); cardIndex++) {
                    logger.info("Card at index " + cardIndex + ":" + ___hand.group.get(cardIndex));
                }

                ArrayList<Integer> cardIndexesToDiscard = CustomDiscardPatch.getCardsToDiscard(___hand);
                if (!cardIndexesToDiscard.isEmpty()) {
                    __instance.hoveredCard = ___hand.group.get(cardIndexesToDiscard.get(0));
                    ReflectionHacks.privateMethod(HandCardSelectScreen.class, "selectHoveredCard").invoke(__instance);
                }
            }
        }

        private static ArrayList<Integer> getCardsToDiscard(CardGroup hand) {
            /* Determine which cards should be auto-selected for discard.

               This function returns an array of numbers, each corresponding to an index
               in the list of cards in the hand.

               Logic for auto-discarding:
               - If all cards are the same, we can choose any card(s).
               - ...TODO
             */
            ArrayList<Integer> discardIndexes = new ArrayList<Integer>();

            if (hand.group.isEmpty()) {
                return discardIndexes;
            }

            if (isHandAllSameCard(hand)) {
                discardIndexes.add(0);
                return discardIndexes;
            }

            return discardIndexes;
        }

        private static boolean isHandAllSameCard(CardGroup hand) {
            /* Check if every card in the hand has the same name. */
            if (hand.group.isEmpty()) {
                return true;
            }
            return hand.getCardNames().stream().distinct().count() == 1;
        }
    }

    // TODO: Custom patch for retain
}

