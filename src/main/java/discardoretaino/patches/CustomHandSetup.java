package discardoretaino.patches;

import com.evacipated.cardcrawl.modthespire.lib.SpirePatch;
import com.evacipated.cardcrawl.modthespire.lib.SpirePostfixPatch;
import com.megacrit.cardcrawl.cards.AbstractCard;
import com.megacrit.cardcrawl.cards.colorless.Purity;
import com.megacrit.cardcrawl.cards.green.*;
import com.megacrit.cardcrawl.cards.purple.WindmillStrike;
import com.megacrit.cardcrawl.characters.AbstractPlayer;

import java.util.ArrayList;

public class CustomHandSetup {
    // Testing switch
    public static boolean testingOn = false;
    @SpirePatch(
            clz = AbstractPlayer.class,
            method = "applyStartOfTurnPostDrawRelics"
    )
    public static class PostDraw {
        @SpirePostfixPatch()
        public static void Postfix(AbstractPlayer ___instance) {
            /* This method adds cards to your hand for debugging. */
            if (testingOn) {
                ArrayList<AbstractCard> cardsToAdd = new ArrayList<AbstractCard>();

                // Special purity that allows us to exhaust whatever we want
                Purity purity = new Purity();
                purity.baseMagicNumber = 10;
                purity.magicNumber = 10;
                cardsToAdd.add(purity);

                // Upgraded strike
                Strike_Green upgraded_strike = new Strike_Green();
                upgraded_strike.upgrade();

                // Upgraded strike 2
                Strike_Green upgraded_strike_2 = new Strike_Green();
                upgraded_strike_2.upgrade();

                // Basic cards
                cardsToAdd.add(new Strike_Green());
                cardsToAdd.add(new Strike_Green());
                cardsToAdd.add(upgraded_strike);
                cardsToAdd.add(upgraded_strike_2);
                cardsToAdd.add(new Defend_Green());

                // Discard
//                cardsToAdd.add(new Survivor());
//                cardsToAdd.add(new Reflex());
//                cardsToAdd.add(new Tactician());

                // Retain
                cardsToAdd.add(new WellLaidPlans());
                cardsToAdd.add(new WindmillStrike());

                // Status/Curse
//                cardsToAdd.add(new Dazed());
//                cardsToAdd.add(new Doubt());

                for (int cardIndex = 0; cardIndex < cardsToAdd.size(); cardIndex++) {
                    ___instance.hand.addToHand(cardsToAdd.get(cardIndex));
                }
            }
        }
    }
}
