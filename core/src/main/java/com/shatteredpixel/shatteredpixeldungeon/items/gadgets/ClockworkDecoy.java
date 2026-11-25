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
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Blindness;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Mob;
import com.shatteredpixel.shatteredpixeldungeon.effects.CellEmitter;
import com.shatteredpixel.shatteredpixeldungeon.effects.particles.SmokeParticle;
import com.shatteredpixel.shatteredpixeldungeon.effects.particles.SparkParticle;
import com.shatteredpixel.shatteredpixeldungeon.items.scrolls.Scroll;
import com.shatteredpixel.shatteredpixeldungeon.items.scrolls.ScrollOfMirrorImage;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSpriteSheet;
import com.shatteredpixel.shatteredpixeldungeon.sprites.MobSprite;
import com.shatteredpixel.shatteredpixeldungeon.utils.GLog;
import com.watabou.noosa.audio.Sample;
import com.watabou.utils.PathFinder;
import com.watabou.utils.Random;

import java.util.ArrayList;

public class ClockworkDecoy extends Gadget {

	{
		image = ItemSpriteSheet.GADGET_DECOY;
	}

	@Override
	public boolean hasActiveAbility() {
		return true;
	}

	@Override
	public Class<? extends Scroll> matchingScroll() {
		return ScrollOfMirrorImage.class;
	}

	@Override
	public void activateGadget(Hero hero) {
		if (cooldown > 0) {
			GLog.w(Messages.get(ClockworkDecoy.class, "cooldown", cooldown));
			return;
		}

		ArrayList<Integer> spawnPoints = new ArrayList<>();
		for (int n : PathFinder.NEIGHBOURS8) {
			int cell = hero.pos + n;
			if (Dungeon.level.passable[cell] && Actor.findChar(cell) == null) {
				spawnPoints.add(cell);
			}
		}

		if (spawnPoints.isEmpty()) {
			GLog.w(Messages.get(ClockworkDecoy.class, "no_space"));
			return;
		}

		int targetCell = Random.element(spawnPoints);
		DecoyDummy dummy = new DecoyDummy();
		dummy.HP = dummy.HT = 15 + 10 * level();
		dummy.pos = targetCell;
		GameScene.add(dummy);
		CellEmitter.center(targetCell).burst(SparkParticle.FACTORY, 6);
		Sample.INSTANCE.play(Assets.Sounds.STEP);
		GLog.p(Messages.get(ClockworkDecoy.class, "deployed"));

		cooldown = 60;
		hero.spendAndNext(Actor.TICK);
	}

	@Override
	protected Buff passiveBuff() {
		return new DecoyRechargeBuff();
	}

	public class DecoyRechargeBuff extends Buff {
		@Override
		public boolean act() {
			if (cooldown > 0) {
				cooldown--;
			}
			spend(Actor.TICK);
			return true;
		}
	}

	public static class DecoyDummy extends Mob {
		{
			spriteClass = DecoySprite.class;
			flying = false;
			alignment = Alignment.ALLY;
			state = PASSIVE;
		}

		@Override
		public int defenseProc(Char enemy, int damage) {
			return damage;
		}

		@Override
		public void die(Object cause) {
			super.die(cause);
			CellEmitter.center(pos).burst(SmokeParticle.FACTORY, 10);
			CellEmitter.center(pos).burst(SparkParticle.FACTORY, 6);
			Sample.INSTANCE.play(Assets.Sounds.BLAST);
			for (int n : PathFinder.NEIGHBOURS8) {
				Char ch = Actor.findChar(pos + n);
				if (ch != null && ch.alignment == Alignment.ENEMY) {
					Buff.affect(ch, Blindness.class, 3f);
				}
			}
		}

		@Override
		public boolean act() {
			for (Mob mob : Dungeon.level.mobs) {
				if (mob.alignment == Alignment.ENEMY && Dungeon.level.distance(pos, mob.pos) <= 6) {
					mob.aggro(this);
				}
			}
			spend(Actor.TICK);
			return true;
		}
	}

	public static class DecoySprite extends MobSprite {
		public DecoySprite() {
			super();
			texture(Assets.Sprites.WARRIOR);
			hardlight(0.8f, 0.7f, 0.4f);
		}
	}

	@Override
	public String status() {
		return cooldown > 0 ? (cooldown + "t") : null;
	}

	public static class Recipe extends com.shatteredpixel.shatteredpixeldungeon.items.Recipe.SimpleRecipe {
		{
			inputs = new Class[]{ScrollOfMirrorImage.class};
			inQuantity = new int[]{1};
			cost = 4;
			output = ClockworkDecoy.class;
			outQuantity = 1;
		}
	}
}
