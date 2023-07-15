package discardoretaino.patches;

import com.evacipated.cardcrawl.modthespire.lib.SpirePatch;
import com.evacipated.cardcrawl.modthespire.lib.SpirePrefixPatch;
import com.megacrit.cardcrawl.actions.common.DiscardAction;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class Discarder {
    public static String modID; //Edit your pom.xml to change this
    public static final Logger logger = LogManager.getLogger(modID); //Used to output to the console.

    @SpirePatch(clz= DiscardAction.class, method="update")
    public static class CustomDiscardPatch {
        @SpirePrefixPatch()
        public static void Prefix(DiscardAction __instance) {
            logger.info("\n\n\nDISCARD\n\n");
        }
    }
}

