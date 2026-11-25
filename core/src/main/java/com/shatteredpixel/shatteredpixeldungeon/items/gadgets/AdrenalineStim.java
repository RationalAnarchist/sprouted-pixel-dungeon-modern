/*
 * Pixel Dungeon
 * Copyright (C) 2012-2015 Oleg Dolya
 *
 * Shattered Pixel Dungeon
 * Copyright (C) 2014-2026 Evan Debenham
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>
 */

package com.shatteredpixel.shatteredpixeldungeon.items.gadgets;

import com.shatteredpixel.shatteredpixeldungeon.Assets;
import com.shatteredpixel.shatteredpixeldungeon.actors.Actor;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Cripple;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.FlavourBuff;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Haste;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.effects.CellEmitter;
import com.shatteredpixel.shatteredpixeldungeon.effects.particles.SparkParticle;
import com.shatteredpixel.shatteredpixeldungeon.items.scrolls.Scroll;
import com.shatteredpixel.shatteredpixeldungeon.items.scrolls.ScrollOfRage;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.sprites.CharSprite;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSpriteSheet;
import com.shatteredpixel.shatteredpixeldungeon.utils.GLog;
import com.watabou.noosa.audio.Sample;

public class AdrenalineStim extends Gadget {

	{
		image = ItemSpriteSheet.GADGET_STIM;
	}

	@Override
	public boolean hasActiveAbility() {
		return true;
	}

	@Override
	public Class<? extends Scroll> matchingScroll() {
		return ScrollOfRage.class;
	}

	@Override
	public void activateGadget(Hero hero) {
		if (cooldown > 0) {
			GLog.w(Messages.get(AdrenalineStim.class, "cooldown", cooldown));
			return;
		}

		inject(hero);
		hero.spendAndNext(Actor.TICK);
	}

	public void inject(Hero hero) {
		cooldown = 80;
		float dur = 6f + 2f * level();
		Buff.affect(hero, Haste.class, dur);
		Buff.affect(hero, AdrenalineBuff.class, dur);
		CellEmitter.center(hero.pos).burst(SparkParticle.FACTORY, 6);
		Sample.INSTANCE.play(Assets.Sounds.DRINK);
		hero.sprite.showStatus(CharSprite.POSITIVE, "STIMPACK");
		GLog.p(Messages.get(AdrenalineStim.class, "injected"));
	}

	@Override
	protected Buff passiveBuff() {
		return new StimTracker();
	}

	public class StimTracker extends Buff {
		@Override
		public boolean act() {
			if (cooldown > 0) {
				cooldown--;
			} else if (target instanceof Hero) {
				Hero hero = (Hero) target;
				// Auto-trigger if HP <= 35%
				if (hero.HP <= hero.HT * 0.35f && hero.buff(AdrenalineBuff.class) == null) {
					inject(hero);
				}
			}
			spend(Actor.TICK);
			return true;
		}
	}

	public static class AdrenalineBuff extends FlavourBuff {
		{
			type = buffType.POSITIVE;
		}

		@Override
		public void detach() {
			super.detach();
			if (target instanceof Hero) {
				Buff.affect(target, Cripple.class, 3f);
				GLog.w(Messages.get(AdrenalineStim.class, "fatigue"));
			}
		}
	}

	@Override
	public String status() {
		return cooldown > 0 ? (cooldown + "t") : null;
	}

	public static class Recipe extends com.shatteredpixel.shatteredpixeldungeon.items.Recipe.SimpleRecipe {
		{
			inputs = new Class[]{ScrollOfRage.class};
			inQuantity = new int[]{1};
			cost = 4;
			output = AdrenalineStim.class;
			outQuantity = 1;
		}
	}
}
