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

package com.shatteredpixel.shatteredpixeldungeon.items.weapon;

import com.shatteredpixel.shatteredpixeldungeon.Assets;
import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.Actor;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Bleeding;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Burning;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Chill;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Cripple;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Frost;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Talent;
import com.shatteredpixel.shatteredpixeldungeon.effects.CellEmitter;
import com.shatteredpixel.shatteredpixeldungeon.effects.Splash;
import com.shatteredpixel.shatteredpixeldungeon.effects.particles.BlastParticle;
import com.shatteredpixel.shatteredpixeldungeon.effects.particles.SmokeParticle;
import com.shatteredpixel.shatteredpixeldungeon.effects.particles.SparkParticle;
import com.shatteredpixel.shatteredpixeldungeon.items.rings.RingOfSharpshooting;
import com.shatteredpixel.shatteredpixeldungeon.mechanics.Ballistica;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.scenes.CellSelector;
import com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSpriteSheet;
import com.watabou.noosa.audio.Sample;
import com.watabou.utils.PathFinder;
import com.watabou.utils.Random;

import java.util.ArrayList;

public class Gun extends Weapon {

	public static final String AC_SHOOT = "SHOOT";

	{
		image = ItemSpriteSheet.REVOLVER;
		hitSound = Assets.Sounds.BLAST;

		defaultAction = AC_SHOOT;
		usesTargeting = true;

		unique = true;
		bones = false;
	}

	@Override
	public ArrayList<String> actions(Hero hero) {
		ArrayList<String> actions = super.actions(hero);
		actions.add(AC_SHOOT);
		return actions;
	}

	@Override
	public void execute(Hero hero, String action) {
		super.execute(hero, action);

		if (action.equals(AC_SHOOT)) {
			curUser = hero;
			curItem = this;
			GameScene.selectCell(shooter);
		}
	}

	public int effectiveTier() {
		if (Dungeon.hero == null) return 1;
		return Math.min(5, 1 + Dungeon.hero.lvl / 5);
	}

	@Override
	public int STRReq(int lvl) {
		return 10;
	}

	@Override
	public int min(int lvl) {
		int t = effectiveTier();
		int dmg = 2 * t + lvl;
		if (Dungeon.hero != null && Dungeon.hero.hasTalent(Talent.HIGH_CALIBER)) {
			dmg += Dungeon.hero.pointsInTalent(Talent.HIGH_CALIBER) * t;
		}
		if (Dungeon.hero != null) {
			dmg += RingOfSharpshooting.levelDamageBonus(Dungeon.hero);
		}
		return Math.max(1, dmg);
	}

	@Override
	public int max(int lvl) {
		int t = effectiveTier();
		int dmg = 8 * t + (2 * t * lvl);
		if (Dungeon.hero != null && Dungeon.hero.hasTalent(Talent.HIGH_CALIBER)) {
			dmg += Dungeon.hero.pointsInTalent(Talent.HIGH_CALIBER) * 2 * t;
		}
		if (Dungeon.hero != null) {
			dmg += 2 * RingOfSharpshooting.levelDamageBonus(Dungeon.hero);
		}
		return Math.max(1, dmg);
	}

	@Override
	public boolean isUpgradable() {
		return true;
	}

	@Override
	public boolean canReach(Char owner, int target) {
		if (owner == null) return false;
		if (Dungeon.level.adjacent(owner.pos, target)) return true;
		Ballistica shot = new Ballistica(owner.pos, target, Ballistica.PROJECTILE);
		return shot.collisionPos == target;
	}

	@Override
	public float accuracyFactor(Char owner, Char target) {
		float acc = super.accuracyFactor(owner, target);
		if (owner instanceof Hero) {
			Hero hero = (Hero) owner;
			if (hero.hasTalent(Talent.CALIBRATED_SIGHTS)) {
				acc *= (1f + 0.20f * hero.pointsInTalent(Talent.CALIBRATED_SIGHTS));
			}
		}
		return acc;
	}

	@Override
	public String info() {
		String info = super.info();

		info += "\n\n" + Messages.get(Gun.class, "stats",
				Math.round(augment.damageFactor(min())),
				Math.round(augment.damageFactor(max())),
				effectiveTier());

		switch (augment) {
			case SPEED:
				info += "\n\n" + Messages.get(Weapon.class, "faster");
				break;
			case DAMAGE:
				info += "\n\n" + Messages.get(Weapon.class, "stronger");
				break;
			case NONE:
		}

		return info;
	}

	@Override
	public int damageRoll(Char owner) {
		int damage = augment.damageFactor(super.damageRoll(owner));

		if (owner instanceof Hero) {
			Hero hero = (Hero) owner;
			Char target = hero.attackTarget();
			if (target != null) {
				int dist = Dungeon.level.distance(owner.pos, target.pos);
				if (hero.hasTalent(Talent.POINT_BLANK_BLAST) && dist <= 1) {
					damage = Math.round(damage * (1f + 0.15f * hero.pointsInTalent(Talent.POINT_BLANK_BLAST)));
				}
				if (hero.hasTalent(Talent.HEADSHOT) && dist >= 4) {
					int chance = 15 * hero.pointsInTalent(Talent.HEADSHOT);
					if (Random.Int(100) < chance) {
						damage = Math.round(damage * 1.5f);
					}
				}
				if (hero.hasTalent(Talent.CLOSE_QUARTERS) && dist <= 2) {
					damage = Math.round(damage * (1f + 0.15f * hero.pointsInTalent(Talent.CLOSE_QUARTERS)));
				}
			}
		}

		return damage;
	}

	@Override
	protected float speedMultiplier(Char owner) {
		float speed = super.speedMultiplier(owner);
		if (owner instanceof Hero) {
			Hero hero = (Hero) owner;
			if (hero.hasTalent(Talent.HAIR_TRIGGER)) {
				speed += 0.10f * hero.pointsInTalent(Talent.HAIR_TRIGGER);
			}
		}
		return speed;
	}

	@Override
	public float delayFactor(Char user) {
		float factor = super.delayFactor(user);
		if (user instanceof Hero) {
			Hero hero = (Hero) user;
			if (hero.hasTalent(Talent.QUICK_RELOAD)) {
				factor *= (1f - 0.15f * hero.pointsInTalent(Talent.QUICK_RELOAD));
			}
			if (hero.hasTalent(Talent.FAN_THE_HAMMER) && hero.buff(FanTheHammerTracker.class) != null) {
				factor *= (1f - 0.18f * hero.pointsInTalent(Talent.FAN_THE_HAMMER));
			}
		}
		return factor;
	}

	@Override
	public int proc(Char attacker, Char defender, int damage) {
		// Hitscan visual effects
		if (attacker != null) {
			CellEmitter.center(attacker.pos).burst(SparkParticle.FACTORY, 4);
			CellEmitter.center(attacker.pos).burst(SmokeParticle.FACTORY, 2);
		}
		Sample.INSTANCE.play(Assets.Sounds.BLAST, 1f, Random.Float(0.95f, 1.05f));

		if (defender != null) {
			CellEmitter.center(defender.pos).burst(SparkParticle.FACTORY, 5);
			CellEmitter.center(defender.pos).burst(BlastParticle.FACTORY, 2);
			Splash.at(defender.pos, 0x999999, 2);

			if (attacker instanceof Hero) {
				Hero hero = (Hero) attacker;
				applyTalentEffects(hero, defender, defender.pos);
				if (hero.hasTalent(Talent.FAN_THE_HAMMER)) {
					Buff.affect(hero, FanTheHammerTracker.class);
				}
			}
		}

		return super.proc(attacker, defender, damage);
	}

	private void applyTalentEffects(Hero hero, Char enemy, int cell) {
		if (hero == null || !enemy.isAlive()) return;

		// Point Blank knockback
		if (hero.hasTalent(Talent.POINT_BLANK_BLAST) && Dungeon.level.adjacent(hero.pos, enemy.pos)) {
			int dir = enemy.pos - hero.pos;
			int knockPos = enemy.pos + dir;
			if (Actor.findChar(knockPos) == null && (Dungeon.level.passable[knockPos] || Dungeon.level.avoid[knockPos])) {
				Actor.addDelayed(new Actor() {
					@Override
					protected boolean act() {
						if (enemy.isAlive()) {
							enemy.move(knockPos);
							enemy.sprite.move(enemy.pos, knockPos);
						}
						Actor.remove(this);
						return true;
					}
				}, 0.1f);
			}
		}

		// Concussive Shots: chance to cripple
		if (hero.hasTalent(Talent.CONCUSSIVE_SHOTS)) {
			int pts = hero.pointsInTalent(Talent.CONCUSSIVE_SHOTS);
			if (Random.Int(100) < 15 * pts) {
				Buff.affect(enemy, Cripple.class, 3f * pts);
			}
		}

		// Incendiary Ammo
		if (hero.hasTalent(Talent.INCENDIARY_AMMO)) {
			int pts = hero.pointsInTalent(Talent.INCENDIARY_AMMO);
			Buff.affect(enemy, Burning.class).reignite(enemy);
			enemy.damage(Random.IntRange(1, pts * 3), this);
		}

		// Cryo Ammo
		if (hero.hasTalent(Talent.CRYO_AMMO)) {
			int pts = hero.pointsInTalent(Talent.CRYO_AMMO);
			Buff.prolong(enemy, Chill.class, 3f * pts);
			if (pts >= 3 && Random.Int(100) < 30) {
				Buff.affect(enemy, Frost.class, 2f);
			}
		}

		// Shock Rounds: arcs lightning to nearby enemies
		if (hero.hasTalent(Talent.SHOCK_ROUNDS)) {
			int pts = hero.pointsInTalent(Talent.SHOCK_ROUNDS);
			for (int n : PathFinder.NEIGHBOURS8) {
				int adj = cell + n;
				Char other = Actor.findChar(adj);
				if (other != null && other != enemy && other != hero && other.isAlive()) {
					int arcDmg = Math.round(damageRoll(hero) * (0.25f * pts));
					other.damage(arcDmg, this);
					CellEmitter.center(adj).burst(SparkParticle.FACTORY, 4);
				}
			}
		}

		// Sharpshooter Piercing Rounds
		if (hero.hasTalent(Talent.PIERCING_ROUNDS)) {
			int pts = hero.pointsInTalent(Talent.PIERCING_ROUNDS);
			Ballistica trace = new Ballistica(hero.pos, cell, Ballistica.STOP_SOLID);
			if (trace.path.size() > 0) {
				int idx = trace.path.indexOf(cell);
				if (idx != -1 && idx + 1 < trace.path.size()) {
					int behindCell = trace.path.get(idx + 1);
					Char behindChar = Actor.findChar(behindCell);
					if (behindChar != null && behindChar != hero && behindChar.isAlive()) {
						behindChar.damage(Math.round(damageRoll(hero) * (0.35f * pts)), this);
						CellEmitter.center(behindCell).burst(SparkParticle.FACTORY, 4);
					}
				}
			}
		}
	}

	public static class FanTheHammerTracker extends Buff {
		{ type = buffType.POSITIVE; }
		@Override
		public boolean act() {
			detach();
			return true;
		}
	}

	private CellSelector.Listener shooter = new CellSelector.Listener() {
		@Override
		public void onSelect(Integer target) {
			if (target != null && curUser != null) {
				Ballistica shot = new Ballistica(curUser.pos, target, Ballistica.PROJECTILE);
				int hitCell = shot.collisionPos;
				Char enemy = Actor.findChar(hitCell);

				// Visual & Audio hitscan effects
				CellEmitter.center(curUser.pos).burst(SparkParticle.FACTORY, 4);
				CellEmitter.center(curUser.pos).burst(SmokeParticle.FACTORY, 2);
				Sample.INSTANCE.play(Assets.Sounds.BLAST, 1f, Random.Float(0.95f, 1.05f));

				if (enemy != null && enemy != curUser) {
					curUser.attack(enemy);
					curUser.spendAndNext(delayFactor(curUser));
				} else {
					CellEmitter.center(hitCell).burst(SparkParticle.FACTORY, 4);
					CellEmitter.center(hitCell).burst(SmokeParticle.FACTORY, 2);
					Dungeon.level.pressCell(hitCell);
					curUser.spendAndNext(delayFactor(curUser));
				}
			}
		}

		@Override
		public String prompt() {
			return Messages.get(Gun.class, "prompt");
		}
	};
}
