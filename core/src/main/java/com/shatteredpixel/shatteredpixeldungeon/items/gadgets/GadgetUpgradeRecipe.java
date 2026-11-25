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

import com.shatteredpixel.shatteredpixeldungeon.items.Item;
import com.shatteredpixel.shatteredpixeldungeon.items.Recipe;
import com.shatteredpixel.shatteredpixeldungeon.items.scrolls.Scroll;

import java.util.ArrayList;

public class GadgetUpgradeRecipe extends Recipe {

	@Override
	public int cost(ArrayList<Item> ingredients) {
		return 2;
	}

	@Override
	public boolean testIngredients(ArrayList<Item> ingredients) {
		if (ingredients.size() != 2) return false;
		Gadget gadget = null;
		Scroll scroll = null;
		for (Item item : ingredients) {
			if (item instanceof Gadget && gadget == null) gadget = (Gadget) item;
			else if (item instanceof Scroll && scroll == null) scroll = (Scroll) item;
		}
		if (gadget != null && scroll != null) {
			return scroll.getClass() == gadget.matchingScroll();
		}
		return false;
	}

	@Override
	public Item brew(ArrayList<Item> ingredients) {
		Gadget gadget = null;
		for (Item item : ingredients) {
			if (item instanceof Gadget) {
				gadget = (Gadget) item;
				break;
			}
		}
		if (gadget != null) {
			gadget.upgrade(1);
			return gadget;
		}
		return null;
	}

	@Override
	public Item sampleOutput(ArrayList<Item> ingredients) {
		for (Item item : ingredients) {
			if (item instanceof Gadget) {
				return item;
			}
		}
		return null;
	}
}
