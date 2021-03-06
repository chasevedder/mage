/*
 *  Copyright 2010 BetaSteward_at_googlemail.com. All rights reserved.
 *
 *  Redistribution and use in source and binary forms, with or without modification, are
 *  permitted provided that the following conditions are met:
 *
 *     1. Redistributions of source code must retain the above copyright notice, this list of
 *        conditions and the following disclaimer.
 *
 *     2. Redistributions in binary form must reproduce the above copyright notice, this list
 *        of conditions and the following disclaimer in the documentation and/or other materials
 *        provided with the distribution.
 *
 *  THIS SOFTWARE IS PROVIDED BY BetaSteward_at_googlemail.com ``AS IS'' AND ANY EXPRESS OR IMPLIED
 *  WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
 *  FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL BetaSteward_at_googlemail.com OR
 *  CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 *  CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 *  SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 *  ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 *  NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF
 *  ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 *  The views and conclusions contained in the software and documentation are those of the
 *  authors and should not be interpreted as representing official policies, either expressed
 *  or implied, of BetaSteward_at_googlemail.com.
 */
package mage.cards.t;

import java.util.UUID;
import mage.MageInt;
import mage.abilities.Ability;
import mage.abilities.TriggeredAbilityImpl;
import mage.abilities.common.delayed.AtTheBeginOfNextEndStepDelayedTriggeredAbility;
import mage.abilities.effects.Effect;
import mage.abilities.effects.OneShotEffect;
import mage.abilities.effects.common.ReturnFromGraveyardToBattlefieldTargetEffect;
import mage.abilities.effects.common.ReturnFromGraveyardToHandTargetEffect;
import mage.abilities.keyword.FlyingAbility;
import mage.abilities.keyword.FirstStrikeAbility;
import mage.cards.Card;
import mage.cards.CardImpl;
import mage.cards.CardSetInfo;
import mage.constants.CardType;
import mage.constants.Outcome;
import mage.constants.SubType;
import mage.constants.SuperType;
import mage.constants.TargetController;
import mage.constants.Zone;
import mage.game.Game;
import mage.game.events.GameEvent;
import mage.game.events.GameEvent.EventType;
import mage.game.events.ZoneChangeEvent;
import mage.game.permanent.Permanent;
import mage.target.targetpointer.FixedTarget;

/**
 *
 * @author spjspj
 */
public class TianaShipsCaretaker extends CardImpl {

    public TianaShipsCaretaker(UUID ownerId, CardSetInfo setInfo) {
        super(ownerId, setInfo, new CardType[]{CardType.CREATURE}, "{3}{R}{W}");

        addSuperType(SuperType.LEGENDARY);
        this.subtype.add(SubType.ANGEL);
        this.subtype.add(SubType.ARTIFICER);
        this.power = new MageInt(3);
        this.toughness = new MageInt(3);

        // Flying
        this.addAbility(FlyingAbility.getInstance());

        // First strike
        this.addAbility(FirstStrikeAbility.getInstance());

        // Whenever an Aura or Equipment you control is put into a graveyard from the battlefield, you may return that card to its owner's hand at the beginning of the next end step.
        this.addAbility(new TianaShipsCaretakerTriggeredAbility());
    }

    public TianaShipsCaretaker(final TianaShipsCaretaker card) {
        super(card);
    }

    @Override
    public TianaShipsCaretaker copy() {
        return new TianaShipsCaretaker(this);
    }
}

class TianaShipsCaretakerTriggeredAbility extends TriggeredAbilityImpl {

    TianaShipsCaretakerTriggeredAbility() {
        super(Zone.BATTLEFIELD, new TianaShipsCaretakerEffect(), true);
    }

    TianaShipsCaretakerTriggeredAbility(final TianaShipsCaretakerTriggeredAbility ability) {
        super(ability);
    }

    @Override
    public TianaShipsCaretakerTriggeredAbility copy() {
        return new TianaShipsCaretakerTriggeredAbility(this);
    }

    @Override
    public boolean checkEventType(GameEvent event, Game game) {
        return event.getType() == EventType.ZONE_CHANGE;
    }

    @Override
    public boolean checkTrigger(GameEvent event, Game game) {
        ZoneChangeEvent zEvent = (ZoneChangeEvent) event;
        if (zEvent.getTarget() == null) {
            return false;
        }
        Permanent permanent = game.getPermanentOrLKIBattlefield(zEvent.getTarget().getId());

        if (permanent != null
                && zEvent.getToZone() == Zone.GRAVEYARD
                && zEvent.getFromZone() == Zone.BATTLEFIELD
                && (permanent.isArtifact() && permanent.hasSubtype(SubType.EQUIPMENT, game)
                    || permanent.isEnchantment() && permanent.hasSubtype(SubType.AURA, game))
                && permanent.getControllerId().equals(this.controllerId)) {
            this.getEffects().setTargetPointer(new FixedTarget(zEvent.getTargetId()));
            return true;
        }
        return false;
    }

    @Override
    public String getRule() {
        return "Whenever an Aura or Equipment you control is put into a graveyard from the battlefield, " + super.getRule();
    }
}

class TianaShipsCaretakerEffect extends OneShotEffect {

    TianaShipsCaretakerEffect() {
        super(Outcome.PutCardInPlay);
        this.staticText = "you may return that card to its owner's hand at the beginning of the next end step";
    }

    TianaShipsCaretakerEffect(final TianaShipsCaretakerEffect effect) {
        super(effect);
    }

    @Override
    public TianaShipsCaretakerEffect copy() {
        return new TianaShipsCaretakerEffect(this);
    }

    @Override
    public boolean apply(Game game, Ability source) {
        Card card = game.getCard(getTargetPointer().getFirst(game, source));
        if (card != null && game.getState().getZone(card.getId()) == Zone.GRAVEYARD) {
            Effect effect = new ReturnFromGraveyardToHandTargetEffect();
            effect.setTargetPointer(new FixedTarget(card.getId(), card.getZoneChangeCounter(game)));
            effect.setText("return that card to your hand at the beginning of the next end step");
            game.addDelayedTriggeredAbility(new AtTheBeginOfNextEndStepDelayedTriggeredAbility(effect), source);
            return true;
        }
        return false;
    }
}
