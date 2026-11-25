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
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Cripple;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.effects.CellEmitter;
import com.shatteredpixel.shatteredpixeldungeon.effects.particles.BlastParticle;
import com.shatteredpixel.shatteredpixeldungeon.effects.particles.SparkParticle;
import com.shatteredpixel.shatteredpixeldungeon.items.scrolls.Scroll;
import com.shatteredpixel.shatteredpixeldungeon.items.scrolls.ScrollOfTerror;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSpriteSheet;
import com.shatteredpixel.shatteredpixeldungeon.utils.GLog;
import com.watabou.noosa.audio.Sample;
import com.watabou.utils.PathFinder;

public class PhosphorFlashPack extends Gadget {

	{
		image = ItemSpriteSheet.GADGET_FLASH_PACK;
	}

	@Override
	public boolean hasActiveAbility() {
		return true;
	}

	@Override
	public Class<? extends Scroll> matchingScroll() {
		return ScrollOfTerror.class;
	}

	@Override
	public void activateGadget(Hero hero) {
		if (cooldown > 0) {
			GLog.w(Messages.get(PhosphorFlashPack.class, "cooldown", cooldown));
			return;
		}

		cooldown = 50;
		Sample.INSTANCE.play(Assets.Sounds.BLAST, 1.2f, 1.1f);
		CellEmitter.center(hero.pos).burst(BlastParticle.FACTORY, 8);
		CellEmitter.center(hero.pos).burst(SparkParticle.FACTORY, 12);
		GameScene.flash(0xFFFFFF);

		float duration = 5f + 2f * level();
		for (Char ch : Actor.chars()) {
			if (ch != hero && ch.isAlive() && Dungeon.level.distance(hero.pos, ch.pos) <= 4) {
				Buff.affect(ch, Blindness.class, duration);
				Buff.affect(ch, Cripple.class, duration);
				CellEmitter.center(ch.pos).burst(SparkParticle.FACTORY, 4);
			}
		}

		GLog.p(Messages.get(PhosphorFlashPack.class, "detonated"));
		hero.spendAndNext(Actor.TICK);
	}

	@Override
	protected Buff passiveBuff() {
		return new FlashTracker();
	}

	public class FlashTracker extends Buff {
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
			inputs = new Class[]{ScrollOfTerror.class};
			inQuantity = new int[]{1};
			cost = 4;
			output = PhosphorFlashPack.class;
			outQuantity = 1;
		}
	}
}
