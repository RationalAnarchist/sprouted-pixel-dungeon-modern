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
import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.Actor;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.effects.CellEmitter;
import com.shatteredpixel.shatteredpixeldungeon.effects.particles.SparkParticle;
import com.shatteredpixel.shatteredpixeldungeon.items.Item;
import com.shatteredpixel.shatteredpixeldungeon.items.Recipe;
import com.shatteredpixel.shatteredpixeldungeon.items.scrolls.Scroll;
import com.shatteredpixel.shatteredpixeldungeon.items.scrolls.ScrollOfMagicMapping;
import com.shatteredpixel.shatteredpixeldungeon.levels.Level;
import com.shatteredpixel.shatteredpixeldungeon.levels.Terrain;
import com.shatteredpixel.shatteredpixeldungeon.levels.traps.Trap;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSpriteSheet;
import com.shatteredpixel.shatteredpixeldungeon.utils.GLog;
import com.watabou.noosa.audio.Sample;
import com.watabou.utils.Bundle;
import com.watabou.utils.PathFinder;

import java.util.ArrayList;

public class SappersSensor extends Gadget {

	{
		image = ItemSpriteSheet.GADGET_SENSOR;
	}

	public int charges = 1;
	public int steps = 0;

	public int maxCharges() {
		return 1 + level() / 2;
	}

	public int stepsPerCharge() {
		return Math.max(20, 60 - 8 * level());
	}

	@Override
	public Class<? extends Scroll> matchingScroll() {
		return ScrollOfMagicMapping.class;
	}

	@Override
	protected Buff passiveBuff() {
		return new SensorBuff();
	}

	public static boolean deflectTrap(Hero hero, Trap trap, int cell) {
		SappersSensor sensor = null;
		if (hero.belongings.misc instanceof SappersSensor) sensor = (SappersSensor) hero.belongings.misc;
		else if (hero.belongings.ring instanceof SappersSensor) sensor = (SappersSensor) hero.belongings.ring;

		if (sensor != null && sensor.charges > 0) {
			sensor.charges--;
			Level.set(cell, Terrain.INACTIVE_TRAP);
			GameScene.updateMap(cell);
			CellEmitter.center(cell).burst(SparkParticle.FACTORY, 6);
			Sample.INSTANCE.play(Assets.Sounds.DEGRADE);
			GLog.p(Messages.get(SappersSensor.class, "deflected"));
			return true;
		}
		return false;
	}

	public class SensorBuff extends Buff {
		@Override
		public boolean act() {
			if (target instanceof Hero) {
				Hero hero = (Hero) target;
				// Detect traps in 2 tiles
				for (int n : PathFinder.NEIGHBOURS8) {
					int cell = hero.pos + n;
					if (Dungeon.level.insideMap(cell) && Dungeon.level.map[cell] == Terrain.SECRET_TRAP) {
						Dungeon.level.discover(cell);
					}
				}

				// Recharge charges
				steps++;
				if (steps >= stepsPerCharge()) {
					steps = 0;
					if (charges < maxCharges()) {
						charges++;
					}
				}
			}
			spend(Actor.TICK);
			return true;
		}
	}

	private static final String CHARGES = "charges";
	private static final String STEPS = "steps";

	@Override
	public void storeInBundle(Bundle bundle) {
		super.storeInBundle(bundle);
		bundle.put(CHARGES, charges);
		bundle.put(STEPS, steps);
	}

	@Override
	public void restoreFromBundle(Bundle bundle) {
		super.restoreFromBundle(bundle);
		charges = bundle.getInt(CHARGES);
		steps = bundle.getInt(STEPS);
	}

	@Override
	public String status() {
		return charges + "/" + maxCharges();
	}

	public static class Recipe extends com.shatteredpixel.shatteredpixeldungeon.items.Recipe.SimpleRecipe {
		{
			inputs = new Class[]{ScrollOfMagicMapping.class};
			inQuantity = new int[]{1};
			cost = 4;
			output = SappersSensor.class;
			outQuantity = 1;
		}
	}
}
