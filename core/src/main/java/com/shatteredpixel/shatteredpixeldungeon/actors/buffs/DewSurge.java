/*
 * Pixel Dungeon
 * Copyright (C) 2012-2015 Oleg Dolya
 *
 * Shattered Pixel Dungeon
 * Copyright (C) 2014-2026 Evan Debenham
 *
 * Verdant Pixel Dungeon
 * Copyright (C) 2026 RationalAnarchist
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 */

package com.shatteredpixel.shatteredpixeldungeon.actors.buffs;

import com.shatteredpixel.shatteredpixeldungeon.Assets;
import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Mob;
import com.shatteredpixel.shatteredpixeldungeon.effects.CellEmitter;
import com.shatteredpixel.shatteredpixeldungeon.effects.Speck;
import com.shatteredpixel.shatteredpixeldungeon.effects.particles.LeafParticle;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.ui.BuffIndicator;
import com.shatteredpixel.shatteredpixeldungeon.utils.GLog;
import com.watabou.noosa.Image;
import com.watabou.noosa.audio.Sample;
import com.watabou.utils.Bundle;

public class DewSurge extends Buff {

	public static final float DURATION = 120f;
	private float left = DURATION;

	private static final String LEFT = "left";

	{
		type = buffType.NEGATIVE;
		announced = true;
	}

	@Override
	public void storeInBundle( Bundle bundle ) {
		super.storeInBundle( bundle );
		bundle.put( LEFT, left );
	}

	@Override
	public void restoreFromBundle( Bundle bundle ) {
		super.restoreFromBundle( bundle );
		left = bundle.getFloat( LEFT );
	}

	@Override
	public boolean act() {
		if (target.isAlive() && Dungeon.level != null) {
			spend( TICK );
			left -= TICK;

			// Periodically beckon all enemies on the level towards the hero
			if (Math.round(left) % 6 == 0) {
				beckonEnemies();
			}

			if (left <= 0) {
				detach();
				GLog.i( Messages.get(this, "expired") );
			}
		} else {
			detach();
		}
		return true;
	}

	public void triggerEvent() {
		left = DURATION;
		beckonEnemies();
		if (target != null && target.sprite != null) {
			target.sprite.centerEmitter().start( Speck.factory( Speck.SCREAM ), 0.3f, 3 );
			CellEmitter.center(target.pos).burst(LeafParticle.LEVEL_SPECIFIC, 10);
		}
		Sample.INSTANCE.play( Assets.Sounds.CHALLENGE );
		Sample.INSTANCE.play( Assets.Sounds.WATER );
		GLog.n( Messages.get(this, "alert") );
	}

	public void beckonEnemies() {
		if (Dungeon.level == null || target == null) return;
		for (Mob m : Dungeon.level.mobs) {
			if (m.alignment == Char.Alignment.ENEMY) {
				m.beckon( target.pos );
			}
		}
	}

	@Override
	public int icon() {
		return BuffIndicator.CHALLENGE;
	}

	@Override
	public void tintIcon(Image icon) {
		icon.hardlight(0.2f, 0.9f, 0.3f);
	}

	@Override
	public float iconFadePercent() {
		return Math.max(0, (DURATION - left) / DURATION);
	}

	@Override
	public String iconTextDisplay() {
		return Integer.toString(Math.max(0, (int)left));
	}

	@Override
	public String toString() {
		return Messages.get(this, "name");
	}

	@Override
	public String desc() {
		return Messages.get(this, "desc", dispTurns(left));
	}
}
