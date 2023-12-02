# Discardo Retaino

Ever been playing a deck with a lot of discard, and you find
yourself discarding the same cards over and over? Ever have a hand
full of Strikes when retaining, so that it doesn't matter which
card you choose? Well, look no further for a solution!

Discardo Retaino (DR) is a quality-of-life mod that improves the discarding and retaining
experiences. DR automatically selects cards for you to discard or retain when there is a clear choice.

Sometimes DR will be wrong, and that's okay. The idea is to reduce the number of times
you're mindlessly clicking to make an "obvious" choice.

## Logic

### Discarding
When discarding, DR will ignore:
- Ethereal status cards
- Ethereal curse cards

DR will only suggest a card to discard if you *must* discard exactly one card.

DR will automatically suggest a card to discard when one of the following is true:
- Your hand contains copies of only a single card (prioritizes non-upgraded cards)
- Your hand contains Reflex or Tactician (not both) and no status/curse cards
- Your hand contains neither Reflex nor Tactician and only one type of status/curse card

### Retaining
When retaining, DR will ignore:
- Ethereal status cards
- Ethereal curse cards
- Cards with Retain

If your hand contains copies of only a single card, DR will suggest as many as possible
(prioritizing upgraded cards). Otherwise, DR will only suggest cards to retain if you can
retain your whole hand.

**TODO**: Test curses like Injury.

## Potential future features
- Logic for forced discarding multiple cards
- Logic for optional discarding of one or more cards
  - Logic for Gambler's Brew
- Should curses be retained?
  - A setting for this
- Implement exhaust features (e.g., True Grit+)
- A setting to enable auto-discarding, not just auto-selecting
- Settings for determining the rules for discarding/retaining
