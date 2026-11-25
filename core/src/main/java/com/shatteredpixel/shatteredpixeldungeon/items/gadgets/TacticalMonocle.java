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

import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.items.scrolls.Scroll;
import com.shatteredpixel.shatteredpixeldungeon.items.scrolls.ScrollOfIdentify;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSpriteSheet;
import com.watabou.utils.Random;

public class TacticalMonocle extends Gadget {

	{
		image = ItemSpriteSheet.GADGET_MONOCLE;
	}

	@Override
	public Class<? extends Scroll> matchingScroll() {
		return ScrollOfIdentify.class;
	}

	public static int armorPenetration(Char attacker) {
		if (attacker instanceof Hero) {
			Hero hero = (Hero) attacker;
			TacticalMonocle monocle = findEquipped(hero);
			if (monocle != null) {
				return 2 + 2 * monocle.level();
			}
		}
		return 0;
	}

	public static boolean checkCrit(Char attacker, Char defender) {
		if (attacker instanceof Hero) {
			Hero hero = (Hero) attacker;
			TacticalMonocle monocle = findEquipped(hero);
			if (monocle != null) {
				int chance = 10 + 5 * monocle.level();
				return Random.Int(100) < chance;
			}
		}
		return false;
	}

	private static TacticalMonocle findEquipped(Hero hero) {
		if (hero.belongings.misc instanceof TacticalMonocle) return (TacticalMonocle) hero.belongings.misc;
		if (hero.belongings.ring instanceof TacticalMonocle) return (TacticalMonocle) hero.belongings.ring;
		return null;
	}

	public static class Recipe extends com.shatteredpixel.shatteredpixeldungeon.items.Recipe.SimpleRecipe {
		{
			inputs = new Class[]{ScrollOfIdentify.class};
			inQuantity = new int[]{1};
			cost = 4;
			output = TacticalMonocle.class;
			outQuantity = 1;
		}
	}
}
