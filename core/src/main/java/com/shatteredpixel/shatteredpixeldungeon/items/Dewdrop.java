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

package com.shatteredpixel.shatteredpixeldungeon.items;

import com.shatteredpixel.shatteredpixeldungeon.Assets;
import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.Statistics;
import com.shatteredpixel.shatteredpixeldungeon.actors.Actor;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Barrier;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Healing;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Talent;
import com.shatteredpixel.shatteredpixeldungeon.effects.FloatingText;
import com.shatteredpixel.shatteredpixeldungeon.items.trinkets.VialOfBlood;
import com.shatteredpixel.shatteredpixeldungeon.journal.Catalog;
import com.shatteredpixel.shatteredpixeldungeon.levels.Terrain;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene;
import com.shatteredpixel.shatteredpixeldungeon.sprites.CharSprite;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSpriteSheet;
import com.shatteredpixel.shatteredpixeldungeon.utils.GLog;
import com.watabou.noosa.audio.Sample;

public class Dewdrop extends Item {
	
	{
		image = ItemSpriteSheet.DEWDROP;
		
		stackable = true;
		dropsDownHeap = true;
	}
	
	@Override
	public boolean doPickUp(Hero hero, int pos) {
		if (this.quantity <= 0) {
			hero.spendAndNext( pickupDelay() );
			return true;
		}
		
		Waterskin flask = hero.belongings.getItem( Waterskin.class );
		Catalog.setSeen(getClass());
		Statistics.itemTypesDiscovered.add(getClass());
		
		int collectedToFlask = 0;
		if (flask != null && !flask.isFull()){
			int beforeQty = this.quantity;
			flask.collectDew( this );
			collectedToFlask = beforeQty - this.quantity;
		}

		int consumedForHealing = 0;
		if (this.quantity > 0) {
			int needed = dropsNeeded(hero);
			if (needed > 0) {
				int toHeal = Math.min(needed, this.quantity);
				if (consumeDew(toHeal, hero, true)) {
					consumedForHealing = toHeal;
					this.quantity -= toHeal;
					Catalog.countUses(getClass(), toHeal);
				}
			}
		}

		if (collectedToFlask == 0 && consumedForHealing == 0) {
			int terr = Dungeon.level.map[pos];
			boolean onStairs = terr == Terrain.ENTRANCE || terr == Terrain.ENTRANCE_SP
					|| terr == Terrain.EXIT || terr == Terrain.UNLOCKED_EXIT;
			if (onStairs && this.quantity > 0) {
				this.quantity--;
				consumedForHealing = 1;
				consumeDew(1, hero, true);
				Catalog.countUse(getClass());
			} else {
				GLog.i( Messages.get(Dewdrop.class, "already_full") );
				return false;
			}
		}

		if (this.quantity > 0) {
			Dewdrop remainder = new Dewdrop();
			remainder.quantity = this.quantity;
			this.quantity = 0;
			Actor.add(new Actor() {
				{ actPriority = VFX_PRIO; }
				@Override
				protected boolean act() {
					Actor.remove(this);
					Dungeon.level.drop(remainder, pos).sprite.drop(pos);
					return true;
				}
			});
		}

		if (collectedToFlask > 0) {
			GameScene.pickUp( this, pos );
		}
		Sample.INSTANCE.play( Assets.Sounds.DEWDROP );
		hero.spendAndNext( pickupDelay() );
		
		return true;
	}

	public static int dropsNeeded(Hero hero) {
		float missingHealthPercent = 1f - (hero.HP / (float)hero.HT);
		float dropsNeeded = missingHealthPercent / 0.05f;
		if (dropsNeeded > 1.01f && VialOfBlood.delayBurstHealing()){
			dropsNeeded /= VialOfBlood.totalHealMultiplier();
		}
		if (hero.hasTalent(Talent.SHIELDING_DEW)){
			int curShield = 0;
			if (hero.buff(Barrier.class) != null) curShield = hero.buff(Barrier.class).shielding();
			int maxShield = Math.round(hero.HT * 0.2f * hero.pointsInTalent(Talent.SHIELDING_DEW));
			if (maxShield > curShield) {
				float missingShieldPercent = 1f - (curShield / (float)maxShield);
				missingShieldPercent *= 0.2f * hero.pointsInTalent(Talent.SHIELDING_DEW);
				if (missingShieldPercent > 0){
					dropsNeeded += missingShieldPercent / 0.05f;
				}
			}
		}
		return Math.max(0, (int)Math.ceil(dropsNeeded - 0.01f));
	}

	public static boolean consumeDew(int quantity, Hero hero, boolean force){
		//20 drops for a full heal
		int effect = Math.round( hero.HT * 0.05f * quantity );

		int heal = Math.min( hero.HT - hero.HP, effect );

		int shield = 0;
		if (hero.hasTalent(Talent.SHIELDING_DEW)){

			//When vial is present, this allocates exactly as much of the effect as is needed
			// to get to 100% HP, and the rest is then given as shielding (without the vial boost)
			if (quantity > 1 && heal < effect && VialOfBlood.delayBurstHealing()){
				heal = Math.round(heal/VialOfBlood.totalHealMultiplier());
			}

			shield = effect - heal;

			int maxShield = Math.round(hero.HT *0.2f*hero.pointsInTalent(Talent.SHIELDING_DEW));
			int curShield = 0;
			if (hero.buff(Barrier.class) != null) curShield = hero.buff(Barrier.class).shielding();
			shield = Math.min(shield, maxShield-curShield);
		}

		if (heal > 0 || shield > 0) {

			if (heal > 0 && quantity > 1 && VialOfBlood.delayBurstHealing()){
				Healing healing = Buff.affect(hero, Healing.class);
				healing.setHeal(heal, 0, VialOfBlood.maxHealPerTurn(), true);
			} else {
				hero.HP += heal;
				if (heal > 0){
					hero.sprite.showStatusWithIcon( CharSprite.POSITIVE, Integer.toString(heal), FloatingText.HEALING);
				}
			}

			if (shield > 0) {
				Buff.affect(hero, Barrier.class).incShield(shield);
				hero.sprite.showStatusWithIcon( CharSprite.POSITIVE, Integer.toString(shield), FloatingText.SHIELDING );
			}

		} else if (!force) {
			GLog.i( Messages.get(Dewdrop.class, "already_full") );
			return false;
		}

		return true;
	}

	@Override
	public boolean isUpgradable() {
		return false;
	}

	@Override
	public boolean isIdentified() {
		return true;
	}

}
