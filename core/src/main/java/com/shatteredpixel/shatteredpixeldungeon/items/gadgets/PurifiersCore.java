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

import com.shatteredpixel.shatteredpixeldungeon.actors.Actor;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Corrosion;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Ooze;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.effects.CellEmitter;
import com.shatteredpixel.shatteredpixeldungeon.effects.particles.SparkParticle;
import com.shatteredpixel.shatteredpixeldungeon.items.scrolls.Scroll;
import com.shatteredpixel.shatteredpixeldungeon.items.scrolls.ScrollOfRemoveCurse;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSpriteSheet;

public class PurifiersCore extends Gadget {

	{
		image = ItemSpriteSheet.GADGET_PURIFIER;
	}

	@Override
	public Class<? extends Scroll> matchingScroll() {
		return ScrollOfRemoveCurse.class;
	}

	public static float damageBonus(Char attacker, Char defender) {
		if (attacker instanceof Hero && defender != null) {
			Hero hero = (Hero) attacker;
			PurifiersCore core = findEquipped(hero);
			if (core != null) {
				if (defender.properties().contains(Char.Property.UNDEAD)
						|| defender.properties().contains(Char.Property.DEMONIC)) {
					CellEmitter.center(defender.pos).burst(SparkParticle.FACTORY, 4);
					return 1.30f + 0.10f * core.level();
				}
			}
		}
		return 1f;
	}

	private static PurifiersCore findEquipped(Hero hero) {
		if (hero.belongings.misc instanceof PurifiersCore) return (PurifiersCore) hero.belongings.misc;
		if (hero.belongings.ring instanceof PurifiersCore) return (PurifiersCore) hero.belongings.ring;
		return null;
	}

	@Override
	protected Buff passiveBuff() {
		return new PurifierBuff();
	}

	public class PurifierBuff extends Buff {
		@Override
		public boolean act() {
			if (target != null) {
				Ooze ooze = target.buff(Ooze.class);
				if (ooze != null) ooze.detach();

				Corrosion corr = target.buff(Corrosion.class);
				if (corr != null) corr.detach();
			}
			spend(Actor.TICK);
			return true;
		}
	}

	public static class Recipe extends com.shatteredpixel.shatteredpixeldungeon.items.Recipe.SimpleRecipe {
		{
			inputs = new Class[]{ScrollOfRemoveCurse.class};
			inQuantity = new int[]{1};
			cost = 4;
			output = PurifiersCore.class;
			outQuantity = 1;
		}
	}
}
