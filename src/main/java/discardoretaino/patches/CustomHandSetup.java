package discardoretaino.patches;

import com.evacipated.cardcrawl.modthespire.lib.SpirePatch;
import com.evacipated.cardcrawl.modthespire.lib.SpirePostfixPatch;
import com.megacrit.cardcrawl.cards.AbstractCard;
import com.megacrit.cardcrawl.cards.colorless.Purity;
import com.megacrit.cardcrawl.cards.green.*;
import com.megacrit.cardcrawl.cards.status.Burn;
import com.megacrit.cardcrawl.cards.status.Dazed;
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
            if (testingOn) {
                ArrayList<AbstractCard> cardsToAdd = new ArrayList<AbstractCard>();

                // Special purity that allows us to discard whatever we want
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

                // Other cards for testing
                cardsToAdd.add(new WellLaidPlans());
                cardsToAdd.add(new Strike_Green());
                cardsToAdd.add(upgraded_strike);
                cardsToAdd.add(upgraded_strike_2);
                cardsToAdd.add(new Dazed());
                cardsToAdd.add(new Reflex());
                cardsToAdd.add(new Survivor());


                for (int cardIndex = 0; cardIndex < cardsToAdd.size(); cardIndex++) {
                    ___instance.hand.addToHand(cardsToAdd.get(cardIndex));
                }
            }
        }
    }
}
