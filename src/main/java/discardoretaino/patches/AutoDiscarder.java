package discardoretaino.patches;

import com.evacipated.cardcrawl.modthespire.lib.SpirePatch;
import com.evacipated.cardcrawl.modthespire.lib.SpirePrefixPatch;
import com.megacrit.cardcrawl.actions.common.DiscardAction;
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
        @SpirePrefixPatch()
        public static void Prefix(HandCardSelectScreen __instance, String msg, int amount, boolean anyNumber) {
            logger.info("\n\n\nNow in hand card select screen\n\n");
            logger.info("Message: " + msg);
            logger.info("Amount: " + amount);
            logger.info("anyNumber: " + anyNumber);
        }
    }
}

