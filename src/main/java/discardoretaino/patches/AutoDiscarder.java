package discardoretaino.patches;

import com.evacipated.cardcrawl.modthespire.lib.SpirePatch;
import com.evacipated.cardcrawl.modthespire.lib.SpirePostfixPatch;
import com.evacipated.cardcrawl.modthespire.lib.SpirePrefixPatch;
import com.megacrit.cardcrawl.actions.common.DiscardAction;
import com.megacrit.cardcrawl.cards.CardGroup;
import com.megacrit.cardcrawl.screens.select.HandCardSelectScreen;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

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
            logger.info("\n\n\nNow in hand card select screen\n\n");
            logger.info("Message: " + msg);
            logger.info("Amount: " + amount);
            logger.info("anyNumber: " + anyNumber);
            logger.info("Hand: " + ___hand);
            if (___hand != null) {
                for (int cardIndex = 0; cardIndex < ___hand.group.size(); cardIndex++) {
                    logger.info("Card at index " + cardIndex + ":" + ___hand.group.get(cardIndex));
                }
            }
        }
    }
}

