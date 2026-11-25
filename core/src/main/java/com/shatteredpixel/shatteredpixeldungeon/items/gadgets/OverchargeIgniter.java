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
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.effects.CellEmitter;
import com.shatteredpixel.shatteredpixeldungeon.effects.particles.BlastParticle;
import com.shatteredpixel.shatteredpixeldungeon.effects.particles.SparkParticle;
import com.shatteredpixel.shatteredpixeldungeon.items.scrolls.Scroll;
import com.shatteredpixel.shatteredpixeldungeon.items.scrolls.ScrollOfRecharging;
import com.shatteredpixel.shatteredpixeldungeon.mechanics.Ballistica;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.scenes.CellSelector;
import com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSpriteSheet;
import com.shatteredpixel.shatteredpixeldungeon.utils.GLog;
import com.watabou.noosa.audio.Sample;
import com.watabou.utils.PathFinder;
import com.watabou.utils.Random;

public class OverchargeIgniter extends Gadget {

	{
		image = ItemSpriteSheet.GADGET_IGNITER;
	}

	@Override
	public boolean hasActiveAbility() {
		return true;
	}

	@Override
	public Class<? extends Scroll> matchingScroll() {
		return ScrollOfRecharging.class;
	}

	@Override
	public void activateGadget(Hero hero) {
		if (cooldown > 0) {
			GLog.w(Messages.get(OverchargeIgniter.class, "cooldown", cooldown));
			return;
		}

		GameScene.selectCell(targeter);
	}

	private CellSelector.Listener targeter = new CellSelector.Listener() {
		@Override
		public void onSelect(Integer target) {
			if (target != null && curUser != null) {
				Ballistica shot = new Ballistica(curUser.pos, target, Ballistica.PROJECTILE);
				int hitCell = shot.collisionPos;

				cooldown = 30;
				Sample.INSTANCE.play(Assets.Sounds.BLAST, 1.2f, Random.Float(0.9f, 1.1f));

				int blastDmg = 12 + 8 * level();
				for (int n : PathFinder.NEIGHBOURS9) {
					int cell = hitCell + n;
					if (Dungeon.level.insideMap(cell)) {
						CellEmitter.center(cell).burst(BlastParticle.FACTORY, 4);
						CellEmitter.center(cell).burst(SparkParticle.FACTORY, 6);
						Char ch = Actor.findChar(cell);
						if (ch != null && ch.isAlive()) {
							ch.damage(blastDmg, OverchargeIgniter.this);
						}
					}
				}

				curUser.spendAndNext(Actor.TICK);
			}
		}

		@Override
		public String prompt() {
			return Messages.get(OverchargeIgniter.class, "prompt");
		}
	};

	@Override
	protected Buff passiveBuff() {
		return new IgniterTracker();
	}

	public class IgniterTracker extends Buff {
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
			inputs = new Class[]{ScrollOfRecharging.class};
			inQuantity = new int[]{1};
			cost = 4;
			output = OverchargeIgniter.class;
			outQuantity = 1;
		}
	}
}
