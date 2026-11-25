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

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.HeroClass;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Talent;
import com.shatteredpixel.shatteredpixeldungeon.items.Item;
import com.shatteredpixel.shatteredpixeldungeon.items.KindofMisc;
import com.shatteredpixel.shatteredpixeldungeon.items.scrolls.Scroll;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSprite;
import com.shatteredpixel.shatteredpixeldungeon.utils.GLog;
import com.shatteredpixel.shatteredpixeldungeon.windows.WndOptions;
import com.watabou.utils.Bundle;

import java.util.ArrayList;

public abstract class Gadget extends KindofMisc {

	public static final String AC_USE = "USE";

	protected Buff passiveBuff;
	public int cooldown = 0;

	{
		stackable = false;
		bones = false;
	}

	@Override
	public boolean isUpgradable() {
		return true;
	}

	@Override
	public boolean isIdentified() {
		return true;
	}

	@Override
	public ArrayList<String> actions(Hero hero) {
		ArrayList<String> actions = super.actions(hero);
		if (isEquipped(hero) && hasActiveAbility()) {
			actions.add(AC_USE);
		}
		return actions;
	}

	public boolean hasActiveAbility() {
		return false;
	}

	@Override
	public void execute(Hero hero, String action) {
		super.execute(hero, action);
		if (action.equals(AC_USE) && isEquipped(hero)) {
			activateGadget(hero);
		}
	}

	public void activateGadget(Hero hero) {
		// Override in active gadgets
	}

	public abstract Class<? extends Scroll> matchingScroll();

	@Override
	public boolean doEquip(final Hero hero) {
		int maxGadgets = (hero.heroClass == HeroClass.GUNMAN) ? 2 : 1;
		int currentGadgets = 0;
		if (hero.belongings.misc instanceof Gadget) currentGadgets++;
		if (hero.belongings.ring instanceof Gadget) currentGadgets++;

		if (currentGadgets >= maxGadgets) {
			final ArrayList<KindofMisc> options = new ArrayList<>();
			if (hero.belongings.misc instanceof Gadget) options.add(hero.belongings.misc);
			if (hero.belongings.ring instanceof Gadget) options.add(hero.belongings.ring);

			if (options.isEmpty()) {
				hero.belongings.misc = this;
			} else {
				String[] titles = new String[options.size()];
				for (int i = 0; i < options.size(); i++) {
					titles[i] = Messages.titleCase(options.get(i).title());
				}

				GameScene.show(new WndOptions(new ItemSprite(this),
						Messages.get(KindofMisc.class, "unequip_title"),
						Messages.get(KindofMisc.class, "unequip_message"),
						titles) {
					@Override
					protected void onSelect(int index) {
						KindofMisc equipped = options.get(index);
						hero.belongings.backpack.items.remove(Gadget.this);
						if (equipped.doUnequip(hero, true, false)) {
							hero.belongings.backpack.items.add(Gadget.this);
							doEquip(hero);
						} else {
							hero.belongings.backpack.items.add(Gadget.this);
						}
					}
				});
				return false;
			}
		} else {
			if (hero.belongings.misc == null) {
				hero.belongings.misc = this;
			} else if (hero.belongings.ring == null) {
				hero.belongings.ring = this;
			} else {
				// Swap with one of the slots
				final KindofMisc[] slots = new KindofMisc[]{hero.belongings.misc, hero.belongings.ring};
				GameScene.show(new WndOptions(new ItemSprite(this),
						Messages.get(KindofMisc.class, "unequip_title"),
						Messages.get(KindofMisc.class, "unequip_message"),
						Messages.titleCase(slots[0].title()),
						Messages.titleCase(slots[1].title())) {
					@Override
					protected void onSelect(int index) {
						KindofMisc equipped = slots[index];
						hero.belongings.backpack.items.remove(Gadget.this);
						if (equipped.doUnequip(hero, true, false)) {
							hero.belongings.backpack.items.add(Gadget.this);
							doEquip(hero);
						} else {
							hero.belongings.backpack.items.add(Gadget.this);
						}
					}
				});
				return false;
			}
		}

		detach(hero.belongings.backpack);
		Talent.onItemEquipped(hero, this);
		activate(hero);
		return true;
	}

	@Override
	public boolean doUnequip(Hero hero, boolean collect, boolean single) {
		if (super.doUnequip(hero, collect, single)) {
			if (hero.belongings.misc == this) hero.belongings.misc = null;
			if (hero.belongings.ring == this) hero.belongings.ring = null;

			if (passiveBuff != null) {
				passiveBuff.detach();
				passiveBuff = null;
			}
			return true;
		}
		return false;
	}

	@Override
	public boolean isEquipped(Hero hero) {
		return hero.belongings.misc == this || hero.belongings.ring == this;
	}

	@Override
	public void activate(Char ch) {
		if (passiveBuff != null) {
			passiveBuff.detach();
			passiveBuff = null;
		}
		passiveBuff = passiveBuff();
		if (passiveBuff != null) {
			passiveBuff.attachTo(ch);
		}
	}

	protected Buff passiveBuff() {
		return null;
	}

	private static final String COOLDOWN = "cooldown";

	@Override
	public void storeInBundle(Bundle bundle) {
		super.storeInBundle(bundle);
		bundle.put(COOLDOWN, cooldown);
	}

	@Override
	public void restoreFromBundle(Bundle bundle) {
		super.restoreFromBundle(bundle);
		cooldown = bundle.getInt(COOLDOWN);
	}

	@Override
	public int value() {
		return 50 + 25 * level();
	}
}
