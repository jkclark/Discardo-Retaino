# Discardo Retaino

Ever been playing a deck with a lot of discard, and you find
yourself discarding the same cards over and over? Ever have a hand
full of Strikes when retaining, so that it doesn't matter which
card you choose? Well, look no further for a solution!

Discardo Retaino (DR) is a quality-of-life mod that improves the discarding and retaining
experiences. DR automatically selects a card for you to discard or retain when there is a clear choice.

Sometimes DR will be wrong, and that's okay. The idea is to reduce the number of times
you're mindlessly clicking to make an "obvious" choice.

### Logic
DR will ignore ethereal status/curse cards.

##### Discarding
DR will automatically suggest a card to discard when your hand contains:
- Copies of only a single card (prioritizes non-upgraded cards)
- Reflex or Tactician (not both) and no status/curse cards
- Neither Reflex nor Tactician and only one type of status/curse card

TODO: Figure out how to handle discarding multiple cards. I think this can be done
by first "discarding" one card, and then re-assessing the resulting hand.

##### Retaining
DR will automatically suggest a card to retain when your hand contains:
- Copies of only a single card (prioritizes upgraded cards)
- TODO: Prioritize Perseverance, Sands of Time, and Windmill Strike
- TODO: Investigate how cards which already have "Retain" work when retaining

TODO: Figure out how to handle retaining multiple cards. I think this can be done
by first "retaining" one card, and then re-assessing the resulting hand.

### Future features
- Implement TODO logic (above)
- A setting to enable auto-discarding, not just auto-selecting (?)
- A setting to enable/disable each condition above (?)
- When retaining a card from a one-card hand, should we retain curses?
    - A setting for this (?)
