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
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.effects.CellEmitter;
import com.shatteredpixel.shatteredpixeldungeon.effects.particles.SparkParticle;
import com.shatteredpixel.shatteredpixeldungeon.items.EquipableItem;
import com.shatteredpixel.shatteredpixeldungeon.items.Item;
import com.shatteredpixel.shatteredpixeldungeon.items.scrolls.Scroll;
import com.shatteredpixel.shatteredpixeldungeon.items.scrolls.ScrollOfRetribution;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSpriteSheet;
import com.shatteredpixel.shatteredpixeldungeon.utils.GLog;
import com.shatteredpixel.shatteredpixeldungeon.windows.WndBag;
import com.watabou.noosa.audio.Sample;

public class MaliceOverclocker extends Gadget {

	{
		image = ItemSpriteSheet.GADGET_OVERCLOCKER;
	}

	@Override
	public boolean hasActiveAbility() {
		return true;
	}

	@Override
	public Class<? extends Scroll> matchingScroll() {
		return ScrollOfRetribution.class;
	}

	@Override
	public void activateGadget(final Hero hero) {
		if (cooldown > 0) {
			GLog.w(Messages.get(MaliceOverclocker.class, "cooldown", cooldown));
			return;
		}

		GameScene.selectItem(new WndBag.ItemSelector() {
			@Override
			public String textPrompt() {
				return Messages.get(MaliceOverclocker.class, "select_item");
			}

			@Override
			public boolean itemSelectable(Item item) {
				return item instanceof EquipableItem && item.isUpgradable();
			}

			@Override
			public void onSelect(Item item) {
				if (item != null && item instanceof EquipableItem && item.isUpgradable()) {
					int bonus = level() >= 3 ? 2 : 1;
					item.upgrade(bonus);
					item.cursed = true;
					item.cursedKnown = true;

					CellEmitter.center(hero.pos).burst(SparkParticle.FACTORY, 8);
					Sample.INSTANCE.play(Assets.Sounds.CURSED);
					GLog.p(Messages.get(MaliceOverclocker.class, "overclocked", item.name(), bonus));

					cooldown = 60;
					hero.spendAndNext(Actor.TICK);
				}
			}
		});
	}

	@Override
	protected Buff passiveBuff() {
		return new MaliceTracker();
	}

	public class MaliceTracker extends Buff {
		@Override
		public boolean act() {
			if (cooldown > 0) {
				cooldown--;
			}
			spend(Actor.TICK);
			return true;
		}
	}

	@Override
	public String status() {
		return cooldown > 0 ? (cooldown + "t") : null;
	}

	public static class Recipe extends com.shatteredpixel.shatteredpixeldungeon.items.Recipe.SimpleRecipe {
		{
			inputs = new Class[]{ScrollOfRetribution.class};
			inQuantity = new int[]{1};
			cost = 4;
			output = MaliceOverclocker.class;
			outQuantity = 1;
		}
	}
}
