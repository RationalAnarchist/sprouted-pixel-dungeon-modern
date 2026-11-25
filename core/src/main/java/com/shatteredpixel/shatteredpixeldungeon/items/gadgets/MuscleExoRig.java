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

import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.items.scrolls.Scroll;
import com.shatteredpixel.shatteredpixeldungeon.items.scrolls.ScrollOfTransmutation;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSpriteSheet;

public class MuscleExoRig extends Gadget {

	{
		image = ItemSpriteSheet.GADGET_EXO_RIG;
	}

	@Override
	public Class<? extends Scroll> matchingScroll() {
		return ScrollOfTransmutation.class;
	}

	public static int strengthBonus(Hero hero) {
		MuscleExoRig rig = findEquipped(hero);
		if (rig != null) {
			return Math.min(3, 1 + rig.level() / 2);
		}
		return 0;
	}

	private static MuscleExoRig findEquipped(Hero hero) {
		if (hero.belongings.misc instanceof MuscleExoRig) return (MuscleExoRig) hero.belongings.misc;
		if (hero.belongings.ring instanceof MuscleExoRig) return (MuscleExoRig) hero.belongings.ring;
		return null;
	}

	public static class Recipe extends com.shatteredpixel.shatteredpixeldungeon.items.Recipe.SimpleRecipe {
		{
			inputs = new Class[]{ScrollOfTransmutation.class};
			inQuantity = new int[]{1};
			cost = 4;
			output = MuscleExoRig.class;
			outQuantity = 1;
		}
	}
}
