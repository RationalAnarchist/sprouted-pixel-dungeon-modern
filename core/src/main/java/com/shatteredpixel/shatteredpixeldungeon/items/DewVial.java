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

package com.shatteredpixel.shatteredpixeldungeon.items;

import com.shatteredpixel.shatteredpixeldungeon.Assets;
import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Barrier;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Haste;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Invisibility;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Talent;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.HeroClass;
import com.shatteredpixel.shatteredpixeldungeon.effects.CellEmitter;
import com.shatteredpixel.shatteredpixeldungeon.effects.Speck;
import com.shatteredpixel.shatteredpixeldungeon.effects.particles.LeafParticle;
import com.shatteredpixel.shatteredpixeldungeon.items.armor.Armor;
import com.shatteredpixel.shatteredpixeldungeon.items.rings.Ring;
import com.shatteredpixel.shatteredpixeldungeon.items.trinkets.VialOfBlood;
import com.shatteredpixel.shatteredpixeldungeon.items.wands.Wand;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.Weapon;
import com.shatteredpixel.shatteredpixeldungeon.journal.Catalog;
import com.shatteredpixel.shatteredpixeldungeon.levels.Level;
import com.shatteredpixel.shatteredpixeldungeon.levels.Terrain;
import com.shatteredpixel.shatteredpixeldungeon.levels.features.HighGrass;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSpriteSheet;
import com.shatteredpixel.shatteredpixeldungeon.utils.GLog;
import com.shatteredpixel.shatteredpixeldungeon.windows.WndBag;
import com.watabou.noosa.audio.Sample;
import com.watabou.utils.Bundle;
import com.watabou.utils.GameMath;
import com.watabou.utils.PathFinder;
import com.watabou.utils.Random;

import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.DewGuardian;

import java.util.ArrayList;

public class DewVial extends Waterskin {

	public static final int MAX_VOLUME = 100;

	public enum Mode {
		NONE,
		DRAW_OUT,
		WATER
	}

	public Mode mode = Mode.NONE;
	public int trialCount = 0;

	public static final String AC_DRINK  = "DRINK";
	public static final String AC_SIP    = "SIP";
	public static final String AC_SPLASH = "SPLASH";
	public static final String AC_BLESS  = "BLESS";
	public static final String AC_WATER  = "WATER";

	private static final float TIME_TO_DRINK  = 1f;
	private static final float TIME_TO_SPLASH = 1f;
	private static final float TIME_TO_WATER  = 1f;

	private static final String MODE = "vial_mode";
	private static final String TRIAL_COUNT = "trial_count";

	{
		image = ItemSpriteSheet.VIAL;
		defaultAction = AC_DRINK;
		unique = true;
	}

	@Override
	public void storeInBundle( Bundle bundle ) {
		super.storeInBundle( bundle );
		bundle.put( MODE, mode.name() );
		bundle.put( TRIAL_COUNT, trialCount );
	}

	@Override
	public void restoreFromBundle( Bundle bundle ) {
		super.restoreFromBundle( bundle );
		if (bundle.contains( MODE )){
			try {
				mode = Mode.valueOf( bundle.getString( MODE ) );
			} catch (Exception e) {
				mode = Mode.NONE;
			}
		}
		trialCount = bundle.getInt( TRIAL_COUNT );
	}

	@Override
	public ArrayList<String> actions( Hero hero ) {
		ArrayList<String> actions = new ArrayList<>();
		int vol = getVolume();

		if (vol > 0) {
			actions.add( AC_DRINK );
			actions.add( AC_SIP );
		}

		if (vol >= 20) {
			actions.add( AC_SPLASH );
		}

		if (canBless()) {
			actions.add( AC_BLESS );
		}

		if (canWater()) {
			actions.add( AC_WATER );
		}

		return actions;
	}

	public boolean canBless() {
		int vol = getVolume();
		switch (mode) {
			case DRAW_OUT:
				return vol >= 90;
			case WATER:
				return vol >= 50;
			case NONE:
			default:
				return vol >= 100;
		}
	}

	public boolean canWater() {
		return mode == Mode.WATER && getVolume() >= 2;
	}

	public int getVolume() {
		// Use internal volume from Waterskin bundle or reflection-safe getter
		return volume;
	}

	public void setVolume( int val ) {
		volume = (int)GameMath.gate(0, val, MAX_VOLUME);
		updateQuickslot();
	}

	@Override
	public boolean isFull() {
		return volume >= MAX_VOLUME;
	}

	@Override
	public void collectDew( Dewdrop dew ) {
		GLog.i( Messages.get(DewVial.class, "collected", dew.quantity) );
		volume += dew.quantity;
		if (volume >= MAX_VOLUME) {
			volume = MAX_VOLUME;
			GLog.p( Messages.get(DewVial.class, "full") );
		}
		updateQuickslot();
	}

	@Override
	public void fill() {
		volume = MAX_VOLUME;
		updateQuickslot();
	}

	@Override
	public String status() {
		return Messages.format( "%d/%d", volume, MAX_VOLUME );
	}

	public static int upgradeCap() {
		int cap = 5 + (Dungeon.depth / 3);
		if (Dungeon.hero != null && Dungeon.hero.heroClass == HeroClass.MAGE) {
			cap++;
		}
		return cap;
	}

	@Override
	public void execute( final Hero hero, String action ) {

		if (action.equals( AC_DRINK )) {

			if (volume <= 0) {
				GLog.w( Messages.get(DewVial.class, "empty") );
				return;
			}

			// Full heal consumes up to 10 drops
			int dropsToConsume = Math.min(volume, 10);
			int missingHP = hero.HT - hero.HP;

			if (missingHP <= 0 && !hero.hasTalent(Talent.SHIELDING_DEW)) {
				GLog.i( Messages.get(Dewdrop.class, "already_full") );
				return;
			}

			hero.HP = Math.min(hero.HT, hero.HP + (int)Math.ceil(hero.HT * (dropsToConsume / 10f)));
			volume -= dropsToConsume;

			hero.spend(TIME_TO_DRINK);
			hero.busy();

			Sample.INSTANCE.play(Assets.Sounds.DRINK);
			hero.sprite.operate(hero.pos);
			hero.sprite.emitter().burst(Speck.factory(Speck.HEALING), 5);
			GLog.p( Messages.get(DewVial.class, "drink_msg") );

			updateQuickslot();

		} else if (action.equals( AC_SIP )) {

			if (volume <= 0) {
				GLog.w( Messages.get(DewVial.class, "empty") );
				return;
			}

			if (Dewdrop.consumeDew(1, hero, false)) {
				volume -= 1;
				hero.spend(TIME_TO_DRINK * 0.5f);
				hero.busy();
				Sample.INSTANCE.play(Assets.Sounds.DRINK);
				hero.sprite.operate(hero.pos);
				updateQuickslot();
			} else {
				GLog.i( Messages.get(Dewdrop.class, "already_full") );
			}

		} else if (action.equals( AC_SPLASH )) {

			if (volume < 20) {
				GLog.w( Messages.get(DewVial.class, "not_enough_dew") );
				return;
			}

			volume -= 20;
			hero.spend(TIME_TO_SPLASH);
			hero.busy();
			hero.sprite.operate(hero.pos);

			Sample.INSTANCE.play(Assets.Sounds.WATER);
			CellEmitter.get(hero.pos).burst(LeafParticle.LEVEL_SPECIFIC, 10);

			// Grants Haste (20 turns) and Invisibility (25 turns)
			Buff.affect(hero, Haste.class, 20f);
			Buff.affect(hero, Invisibility.class, 25f);

			GLog.p( Messages.get(DewVial.class, "splash_msg") );
			updateQuickslot();

		} else if (action.equals( AC_WATER )) {

			if (volume < 2) {
				GLog.w( Messages.get(DewVial.class, "not_enough_dew") );
				return;
			}

			volume -= 2;
			hero.spend(TIME_TO_WATER);
			hero.busy();
			hero.sprite.operate(hero.pos);

			Sample.INSTANCE.play(Assets.Sounds.WATER);
			int wateredCount = 0;

			// Regrow 3x3 surrounding tiles
			for (int n : PathFinder.NEIGHBOURS9) {
				int target = hero.pos + n;
				if (target >= 0 && target < Dungeon.level.length()) {
					int terrain = Dungeon.level.map[target];
					if (terrain == Terrain.GRASS || terrain == Terrain.EMPTY || terrain == Terrain.EMPTY_DECO) {
						Level.set(target, Terrain.HIGH_GRASS);
						HighGrass.markWateredTile(target);
						CellEmitter.get(target).burst(LeafParticle.LEVEL_SPECIFIC, 4);
						wateredCount++;
					}
				}
			}

			GameScene.updateMap();
			Dungeon.observe();

			if (wateredCount > 0) {
				GLog.p( Messages.get(DewVial.class, "water_success", wateredCount) );
			} else {
				GLog.i( Messages.get(DewVial.class, "water_none") );
			}

			updateQuickslot();

		} else if (action.equals( AC_BLESS )) {

			if (!canBless()) {
				GLog.w( Messages.get(DewVial.class, "not_enough_dew") );
				return;
			}

			if (mode == Mode.WATER) {
				// Water with Dew: Cleanses curses from entire pack and rolls upgrade chance
				volume -= 50;
				hero.spend(1f);
				hero.busy();
				hero.sprite.operate(hero.pos);
				blessEntirePack(hero);

				// Summon escalating Dew Guardian trial
				trialCount++;
				DewGuardian.spawnAt(hero.pos, trialCount);
				GLog.n( Messages.get(DewVial.class, "trial_summon") );

				updateQuickslot();
			} else {
				// Draw Out Dew (or pre-choice): targeted single-item upgrade and uncurse
				GameScene.selectItem(new WndBag.ItemSelector() {
					@Override
					public String textPrompt() {
						return Messages.get(DewVial.class, "prompt_bless");
					}

					@Override
					public boolean itemSelectable(Item item) {
						if (!item.isUpgradable()) return false;
						return (item instanceof Weapon || item instanceof Armor || item instanceof Wand || item instanceof Ring);
					}

					@Override
					public void onSelect(Item item) {
						if (item != null) {
							int cost = (mode == Mode.DRAW_OUT) ? 90 : 100;
							volume -= cost;
							hero.spend(1f);
							hero.busy();
							hero.sprite.operate(hero.pos);
							blessSingleItem(hero, item);
							updateQuickslot();
						}
					}
				});
			}

		} else {
			super.execute( hero, action );
		}
	}

	private void blessSingleItem(Hero hero, Item item) {
		int cap = upgradeCap();
		boolean upgraded = false;

		// 1. Cleanse curses
		if (item.cursed) {
			item.cursed = false;
			item.cursedKnown = true;
			GLog.p( Messages.get(DewVial.class, "uncurse_item", item.name()) );
		}

		// 2. Upgrade if under cap
		if (item.level() < cap) {
			item.upgrade();
			upgraded = true;
			GLog.p( Messages.get(DewVial.class, "bless_success", item.name(), item.level()) );
		} else {
			GLog.w( Messages.get(DewVial.class, "cap_reached", item.name(), cap) );
		}

		Sample.INSTANCE.play(Assets.Sounds.MASTERY);
		hero.sprite.emitter().burst(Speck.factory(Speck.LIGHT), 8);
		Catalog.countUse(Dewdrop.class);
	}

	private void blessEntirePack(Hero hero) {
		int cap = upgradeCap();
		int upgradedCount = 0;

		// Cleanse all items and roll upgrade chances across all belongings (equipped + backpack)
		for (Item item : hero.belongings) {
			if (item == null) continue;

			if (item.cursed) {
				item.cursed = false;
				item.cursedKnown = true;
			}

			if (item.isUpgradable() && (item instanceof Weapon || item instanceof Armor || item instanceof Wand || item instanceof Ring)) {
				if (item.level() < cap && Random.Float() < 0.45f) {
					item.upgrade();
					upgradedCount++;
				}
			}
		}

		Sample.INSTANCE.play(Assets.Sounds.MASTERY);
		hero.sprite.emitter().burst(Speck.factory(Speck.LIGHT), 12);
		GLog.p( Messages.get(DewVial.class, "bless_pack", upgradedCount) );
	}

	@Override
	public String info() {
		StringBuilder sb = new StringBuilder();
		sb.append(Messages.get(DewVial.class, "desc"));
		sb.append("\n\n");
		sb.append(Messages.get(DewVial.class, "desc_cap", upgradeCap()));

		if (mode == Mode.WATER) {
			sb.append("\n\n").append(Messages.get(DewVial.class, "desc_water_mode"));
		} else if (mode == Mode.DRAW_OUT) {
			sb.append("\n\n").append(Messages.get(DewVial.class, "desc_draw_mode"));
		} else {
			sb.append("\n\n").append(Messages.get(DewVial.class, "desc_no_mode"));
		}

		return sb.toString();
	}
}
