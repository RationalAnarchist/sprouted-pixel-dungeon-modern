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
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Paralysis;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Talent;
import com.shatteredpixel.shatteredpixeldungeon.effects.CellEmitter;
import com.shatteredpixel.shatteredpixeldungeon.effects.Splash;
import com.shatteredpixel.shatteredpixeldungeon.effects.particles.BlastParticle;
import com.shatteredpixel.shatteredpixeldungeon.effects.particles.SmokeParticle;
import com.shatteredpixel.shatteredpixeldungeon.effects.particles.SparkParticle;
import com.shatteredpixel.shatteredpixeldungeon.items.Item;
import com.shatteredpixel.shatteredpixeldungeon.items.rings.RingOfSharpshooting;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.missiles.MissileWeapon;
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
		image = ItemSpriteSheet.CROSSBOW;

		defaultAction = AC_SHOOT;
		usesTargeting = true;

		unique = true;
		bones = false;
	}

	@Override
	public ArrayList<String> actions(Hero hero) {
		ArrayList<String> actions = super.actions(hero);
		actions.remove(AC_EQUIP);
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

		info += "\n\n" + Messages.get(MissileWeapon.class, "distance");

		return info;
	}

	@Override
	public int damageRoll(Char owner) {
		int damage = augment.damageFactor(super.damageRoll(owner));

		if (owner instanceof Hero) {
			Hero hero = (Hero) owner;
			// Point blank blast bonus
			if (hero.hasTalent(Talent.POINT_BLANK_BLAST) && targetPos != -1) {
				if (Dungeon.level.adjacent(owner.pos, targetPos)) {
					damage = Math.round(damage * (1f + 0.15f * hero.pointsInTalent(Talent.POINT_BLANK_BLAST)));
				}
			}

			// Sharpshooter headshot bonus at long range
			if (hero.hasTalent(Talent.HEADSHOT) && targetPos != -1) {
				int dist = Dungeon.level.distance(owner.pos, targetPos);
				if (dist >= 4) {
					int chance = 15 * hero.pointsInTalent(Talent.HEADSHOT);
					if (Random.Int(100) < chance) {
						damage = Math.round(damage * 1.5f);
					}
				}
			}

			// Close Quarters (Gunslinger)
			if (hero.hasTalent(Talent.CLOSE_QUARTERS) && targetPos != -1) {
				int dist = Dungeon.level.distance(owner.pos, targetPos);
				if (dist <= 2) {
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

	public static class FanTheHammerTracker extends Buff {
		{ type = buffType.POSITIVE; }
		@Override
		public boolean act() {
			detach();
			return true;
		}
	}

	public Bullet knockBullet() {
		return new Bullet();
	}

	private int targetPos = -1;

	public class Bullet extends MissileWeapon {

		{
			image = ItemSpriteSheet.DART;
			hitSound = Assets.Sounds.BLAST;
			setID = 0;
		}

		@Override
		public ArrayList<String> actions(Hero hero) {
			return new ArrayList<>();
		}

		@Override
		public String defaultAction() {
			return null;
		}

		@Override
		public int defaultQuantity() {
			return 1;
		}

		@Override
		public int damageRoll(Char owner) {
			return Gun.this.damageRoll(owner);
		}

		@Override
		public float delayFactor(Char user) {
			return Gun.this.delayFactor(user);
		}

		@Override
		public float speedMultiplier(Char owner) {
			return Gun.this.speedMultiplier(owner);
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
		public int STRReq(int lvl) {
			return Gun.this.STRReq();
		}

		@Override
		public void throwSound() {
			Sample.INSTANCE.play(Assets.Sounds.BLAST, 1f, Random.Float(0.95f, 1.05f));
		}

		@Override
		public Item split(int amount) {
			return null;
		}

		@Override
		protected void onThrow(int cell) {
			Char enemy = Actor.findChar(cell);
			if (enemy == null || enemy == curUser) {
				parent = null;
				CellEmitter.center(cell).burst(SparkParticle.FACTORY, 4);
				CellEmitter.center(cell).burst(SmokeParticle.FACTORY, 2);
			} else {
				CellEmitter.center(cell).burst(SparkParticle.FACTORY, 6);
				CellEmitter.center(cell).burst(BlastParticle.FACTORY, 2);

				Hero hero = curUser;
				if (!curUser.shoot(enemy, this)) {
					Splash.at(cell, 0xCCCCCC, 1);
					// Ricochet talent on miss
					if (hero != null && hero.hasTalent(Talent.RICOCHET)) {
						triggerRicochet(hero, cell, enemy);
					}
				} else {
					// Hit effects & elemental ammo
					applyAmmoAndTalentEffects(hero, enemy, cell);
				}
				if (hero != null && hero.hasTalent(Talent.FAN_THE_HAMMER)) {
					Buff.affect(hero, FanTheHammerTracker.class);
				}
			}
		}

		private void triggerRicochet(Hero hero, int fromCell, Char originalTarget) {
			float bounceChance = 0.35f * hero.pointsInTalent(Talent.RICOCHET);
			if (Random.Float() < bounceChance) {
				for (int n : PathFinder.NEIGHBOURS8) {
					int adj = fromCell + n;
					Char other = Actor.findChar(adj);
					if (other != null && other != originalTarget && other != hero && other.isAlive()) {
						int dmg = Math.round(Gun.this.damageRoll(hero) * 0.5f);
						other.damage(dmg, Gun.this);
						CellEmitter.center(adj).burst(SparkParticle.FACTORY, 4);
						break;
					}
				}
			}
		}

		private void applyAmmoAndTalentEffects(Hero hero, Char enemy, int cell) {
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

			// Concussive Shots: chance to cripple or paralyze
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
				enemy.damage(Random.IntRange(1, pts * 3), Gun.this);
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
						int arcDmg = Math.round(Gun.this.damageRoll(hero) * (0.25f * pts));
						other.damage(arcDmg, Gun.this);
						CellEmitter.center(adj).burst(SparkParticle.FACTORY, 4);
					}
				}
			}

			// Sharpshooter Piercing Rounds
			if (hero.hasTalent(Talent.PIERCING_ROUNDS)) {
				int pts = hero.pointsInTalent(Talent.PIERCING_ROUNDS);
				int dir = cell - hero.pos;
				// check line behind enemy
				Ballistica trace = new Ballistica(hero.pos, cell, Ballistica.STOP_SOLID);
				int nextCell = cell + (cell - hero.pos > 0 ? 1 : -1);
				if (trace.path.size() > 0) {
					int idx = trace.path.indexOf(cell);
					if (idx != -1 && idx + 1 < trace.path.size()) {
						int behindCell = trace.path.get(idx + 1);
						Char behindChar = Actor.findChar(behindCell);
						if (behindChar != null && behindChar != hero && behindChar.isAlive()) {
							behindChar.damage(Math.round(Gun.this.damageRoll(hero) * (0.35f * pts)), Gun.this);
							CellEmitter.center(behindCell).burst(SparkParticle.FACTORY, 4);
						}
					}
				}
			}
		}

		@Override
		public void cast(Hero user, int dst) {
			targetPos = throwPos(user, dst);
			// Set missile sprite according to active ammo talent
			if (user.hasTalent(Talent.INCENDIARY_AMMO)) {
				image = ItemSpriteSheet.INCENDIARY_DART;
			} else if (user.hasTalent(Talent.CRYO_AMMO)) {
				image = ItemSpriteSheet.CHILLING_DART;
			} else if (user.hasTalent(Talent.SHOCK_ROUNDS)) {
				image = ItemSpriteSheet.SHOCKING_DART;
			} else {
				image = ItemSpriteSheet.DART;
			}

			super.cast(user, dst);
		}
	}

	private CellSelector.Listener shooter = new CellSelector.Listener() {
		@Override
		public void onSelect(Integer target) {
			if (target != null) {
				knockBullet().cast(curUser, target);
			}
		}

		@Override
		public String prompt() {
			return Messages.get(Gun.class, "prompt");
		}
	};
}
