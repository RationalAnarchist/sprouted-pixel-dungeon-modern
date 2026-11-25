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
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Hunger;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.items.scrolls.Scroll;
import com.shatteredpixel.shatteredpixeldungeon.items.scrolls.ScrollOfLullaby;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSpriteSheet;

public class BiometricResonator extends Gadget {

	{
		image = ItemSpriteSheet.GADGET_RESONATOR;
	}

	@Override
	public Class<? extends Scroll> matchingScroll() {
		return ScrollOfLullaby.class;
	}

	public static float HTMultiplier(Hero hero) {
		BiometricResonator resonator = findEquipped(hero);
		if (resonator != null) {
			return 1.10f + 0.05f * resonator.level();
		}
		return 1f;
	}

	private static BiometricResonator findEquipped(Hero hero) {
		if (hero.belongings.misc instanceof BiometricResonator) return (BiometricResonator) hero.belongings.misc;
		if (hero.belongings.ring instanceof BiometricResonator) return (BiometricResonator) hero.belongings.ring;
		return null;
	}

	@Override
	public boolean doEquip(Hero hero) {
		boolean res = super.doEquip(hero);
		if (res) hero.updateHT(true);
		return res;
	}

	@Override
	public boolean doUnequip(Hero hero, boolean collect, boolean single) {
		boolean res = super.doUnequip(hero, collect, single);
		if (res) hero.updateHT(false);
		return res;
	}

	@Override
	protected Buff passiveBuff() {
		return new ResonatorRegenBuff();
	}

	public class ResonatorRegenBuff extends Buff {
		@Override
		public boolean act() {
			if (target instanceof Hero) {
				Hero hero = (Hero) target;
				Hunger hunger = hero.buff(Hunger.class);
				if (hunger != null && !hunger.isStarving() && hero.HP < hero.HT) {
					hero.HP = Math.min(hero.HT, hero.HP + 1);
				}
			}
			spend(Actor.TICK * 8);
			return true;
		}
	}

	public static class Recipe extends com.shatteredpixel.shatteredpixeldungeon.items.Recipe.SimpleRecipe {
		{
			inputs = new Class[]{ScrollOfLullaby.class};
			inQuantity = new int[]{1};
			cost = 4;
			output = BiometricResonator.class;
			outQuantity = 1;
		}
	}
}
