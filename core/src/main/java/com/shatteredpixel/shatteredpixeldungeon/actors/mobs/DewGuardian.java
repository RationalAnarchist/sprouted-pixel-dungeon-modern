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

package com.shatteredpixel.shatteredpixeldungeon.actors.mobs;

import com.shatteredpixel.shatteredpixeldungeon.Assets;
import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.Actor;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.ChampionEnemy;
import com.shatteredpixel.shatteredpixeldungeon.effects.CellEmitter;
import com.shatteredpixel.shatteredpixeldungeon.effects.particles.LeafParticle;
import com.shatteredpixel.shatteredpixeldungeon.items.DewVial;
import com.shatteredpixel.shatteredpixeldungeon.items.Dewdrop;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene;
import com.shatteredpixel.shatteredpixeldungeon.sprites.DewGuardianSprite;
import com.shatteredpixel.shatteredpixeldungeon.utils.GLog;
import com.watabou.noosa.audio.Sample;
import com.watabou.noosa.tweeners.AlphaTweener;
import com.watabou.utils.Bundle;
import com.watabou.utils.PathFinder;
import com.watabou.utils.Random;

import java.util.ArrayList;

public class DewGuardian extends Mob {

	private static final String TRIAL_TIER = "trial_tier";

	public int trialTier = 1;

	{
		spriteClass = DewGuardianSprite.class;

		state = HUNTING;

		properties.add(Property.MINIBOSS);
		properties.add(Property.INORGANIC);

		maxLvl = 50;
	}

	public DewGuardian() {
		super();
		initStats(1);
	}

	public DewGuardian(int tier) {
		super();
		initStats(tier);
	}

	public void initStats(int tier) {
		this.trialTier = Math.max(1, tier);

		int depth = Dungeon.scalingDepth();
		int t = Math.max(0, trialTier - 1);

		// Starts gentle at Marsupial Rat level (trial 1: 8-10 HP) and scales gently with each trial (~+3 HP) & depth
		HT = 8 + (t * 3) + depth;
		HP = HT;

		// Defense skill starts at 2 (same as Marsupial Rat)
		defenseSkill = 2 + (t / 2) + (depth / 4);

		EXP = 1 + (t / 2) + (depth / 2);

		// Champion buffs are disabled for the first 5 trials so early un-upgraded runs aren't spiked by RNG
		if (trialTier > 5 && buff(ChampionEnemy.class) == null) {
			float champChance = (trialTier >= 20) ? 0.8f : (trialTier >= 15 ? 0.6f : (trialTier >= 10 ? 0.4f : 0.2f));
			if (Random.Float() < champChance) {
				Class<? extends ChampionEnemy> buffCls;
				switch (Random.Int(6)) {
					case 0: default: buffCls = ChampionEnemy.Blazing.class; break;
					case 1: buffCls = ChampionEnemy.Projecting.class; break;
					case 2: buffCls = ChampionEnemy.AntiMagic.class; break;
					case 3: buffCls = ChampionEnemy.Giant.class; break;
					case 4: buffCls = ChampionEnemy.Blessed.class; break;
					case 5: buffCls = ChampionEnemy.Growing.class; break;
				}
				Buff.affect(this, buffCls);
			}
		}
	}

	@Override
	public int attackSkill(Char target) {
		int depth = Dungeon.scalingDepth();
		int t = Math.max(0, trialTier - 1);
		// Starts at 8 (same as Marsupial Rat) and increases smoothly with tier & depth
		return 8 + (t * 2 / 3) + (depth / 3);
	}

	@Override
	public int damageRoll() {
		int depth = Dungeon.scalingDepth();
		int t = Math.max(0, trialTier - 1);
		// Starts at 1-4 (same as Marsupial Rat) and scales gradually across 20+ trials
		int min = 1 + (t / 5) + (depth / 6);
		int max = 4 + (t / 2) + (depth / 4);
		return Random.NormalIntRange(min, max);
	}

	@Override
	public int drRoll() {
		int depth = Dungeon.scalingDepth();
		int t = Math.max(0, trialTier - 1);
		int minDR = Math.max(0, (t - 4) / 4);
		int maxDR = 1 + (t / 3) + (depth / 6);
		return super.drRoll() + Random.NormalIntRange(minDR, maxDR);
	}

	@Override
	public float speed() {
		float s = super.speed();
		if (trialTier >= 20) {
			return s * 1.2f;
		} else if (trialTier >= 12) {
			return s * 1.1f;
		}
		return s;
	}

	@Override
	public int attackProc(Char enemy, int damage) {
		if (Dungeon.level != null && Dungeon.level.heroFOV[enemy.pos]) {
			CellEmitter.bottom(enemy.pos).burst(LeafParticle.LEVEL_SPECIFIC, 4);
			Sample.INSTANCE.play(Assets.Sounds.WATER);
		}
		return super.attackProc(enemy, damage);
	}

	@Override
	public void die(Object cause) {
		// Guaranteed refund of the 50 dewdrops spent on the trial directly into the Dew Vial
		DewVial vial = Dungeon.hero != null ? Dungeon.hero.belongings.getItem(DewVial.class) : null;
		if (vial != null) {
			int space = DewVial.MAX_VOLUME - vial.getVolume();
			int refund = Math.min(50, space);
			vial.setVolume(vial.getVolume() + refund);
			GLog.p( Messages.get(DewGuardian.class, "defeat_refund", refund) );
			int excess = 50 - refund;
			if (excess > 0) {
				Dungeon.level.drop(new Dewdrop().quantity(excess), pos).sprite.drop();
			}
		} else {
			Dungeon.level.drop(new Dewdrop().quantity(50), pos).sprite.drop();
		}

		Sample.INSTANCE.play(Assets.Sounds.WATER);
		if (Dungeon.level != null && Dungeon.level.heroFOV[pos]) {
			CellEmitter.center(pos).burst(LeafParticle.LEVEL_SPECIFIC, 15);
		}

		super.die(cause);
	}

	@Override
	public String description() {
		String desc = Messages.get(this, "desc");
		desc += "\n\n" + Messages.format( "Trial Rank: %d", trialTier );
		return desc;
	}

	@Override
	public void storeInBundle(Bundle bundle) {
		super.storeInBundle(bundle);
		bundle.put(TRIAL_TIER, trialTier);
	}

	@Override
	public void restoreFromBundle(Bundle bundle) {
		super.restoreFromBundle(bundle);
		trialTier = bundle.getInt(TRIAL_TIER);
		int depth = Dungeon.scalingDepth();
		int t = Math.max(0, trialTier - 1);
		defenseSkill = 2 + (t / 2) + (depth / 4);
		EXP = 1 + (t / 2) + (depth / 2);
	}

	public static DewGuardian spawnAt(int pos, int trialTier) {
		ArrayList<Integer> candidates = new ArrayList<>();

		for (int n : PathFinder.NEIGHBOURS8) {
			int p = pos + n;
			if (p >= 0 && p < Dungeon.level.length()
					&& !Dungeon.level.solid[p]
					&& Dungeon.level.passable[p]
					&& Actor.findChar(p) == null) {
				candidates.add(p);
			}
		}

		if (candidates.isEmpty()) {
			for (int n : PathFinder.NEIGHBOURS8) {
				int p = pos + n;
				if (p >= 0 && p < Dungeon.level.length()
						&& !Dungeon.level.solid[p]
						&& Actor.findChar(p) == null) {
					candidates.add(p);
				}
			}
		}

		if (candidates.isEmpty()) {
			for (int dy = -2; dy <= 2; dy++) {
				for (int dx = -2; dx <= 2; dx++) {
					int p = pos + dx + dy * Dungeon.level.width();
					if (p >= 0 && p < Dungeon.level.length()
							&& !Dungeon.level.solid[p]
							&& Dungeon.level.passable[p]
							&& Actor.findChar(p) == null) {
						candidates.add(p);
					}
				}
			}
		}

		if (candidates.isEmpty()) {
			return null;
		}

		int targetCell = Random.element(candidates);

		DewGuardian g = new DewGuardian(trialTier);
		g.pos = targetCell;
		g.state = g.HUNTING;
		g.enemy = Dungeon.hero;
		g.target = Dungeon.hero.pos;
		g.enemySeen = true;

		GameScene.add(g, 1f);
		Dungeon.level.occupyCell(g);

		if (g.sprite != null && g.sprite.parent != null) {
			g.sprite.alpha(0f);
			g.sprite.parent.add(new AlphaTweener(g.sprite, 1f, 0.4f));
		}

		CellEmitter.get(targetCell).burst(LeafParticle.LEVEL_SPECIFIC, 10);
		Sample.INSTANCE.play(Assets.Sounds.WATER);
		Sample.INSTANCE.play(Assets.Sounds.CHALLENGE);

		return g;
	}
}
