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

package com.shatteredpixel.shatteredpixeldungeon.actors.mobs.npcs;

import com.shatteredpixel.shatteredpixeldungeon.Assets;
import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.effects.Speck;
import com.shatteredpixel.shatteredpixeldungeon.items.DewVial;
import com.shatteredpixel.shatteredpixeldungeon.items.quest.Toadstool;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSprite;
import com.shatteredpixel.shatteredpixeldungeon.sprites.TinkererSprite;
import com.shatteredpixel.shatteredpixeldungeon.utils.GLog;
import com.shatteredpixel.shatteredpixeldungeon.windows.WndMessage;
import com.shatteredpixel.shatteredpixeldungeon.windows.WndOptions;
import com.watabou.noosa.Game;
import com.watabou.noosa.audio.Sample;
import com.watabou.utils.Bundle;
import com.watabou.utils.Callback;

public class Tinkerer extends NPC {

	{
		spriteClass = TinkererSprite.class;
		properties.add(Property.IMMOVABLE);
	}

	public boolean completed = false;

	private static final String COMPLETED = "tinkerer_completed";

	@Override
	public void storeInBundle( Bundle bundle ) {
		super.storeInBundle( bundle );
		bundle.put( COMPLETED, completed );
	}

	@Override
	public void restoreFromBundle( Bundle bundle ) {
		super.restoreFromBundle( bundle );
		completed = bundle.getBoolean( COMPLETED );
	}

	@Override
	public int defenseSkill( Char enemy ) {
		return INFINITE_EVASION;
	}

	@Override
	public void damage( int dmg, Object src ) {
		// Immune
	}

	@Override
	public boolean add( Buff buff ) {
		return false;
	}

	@Override
	public boolean reset() {
		return true;
	}

	@Override
	public boolean interact(Char c) {
		sprite.turnTo( pos, c.pos );
		if (c != Dungeon.hero) {
			return true;
		}

		final DewVial vial = Dungeon.hero.belongings.getItem( DewVial.class );
		final Toadstool shroom = Dungeon.hero.belongings.getItem( Toadstool.class );

		if (completed || (vial != null && vial.mode != DewVial.Mode.NONE)) {

			Game.runOnRenderThread(new Callback() {
				@Override
				public void call() {
					GameScene.show( new WndMessage( Messages.get(Tinkerer.class, "done") ) );
				}
			});

		} else if (vial != null && shroom != null) {

			Game.runOnRenderThread(new Callback() {
				@Override
				public void call() {
					GameScene.show( new WndOptions(
							new ItemSprite(vial),
							Messages.get(Tinkerer.class, "choice_title"),
							Messages.get(Tinkerer.class, "choice_prompt"),
							Messages.get(Tinkerer.class, "opt_draw"),
							Messages.get(Tinkerer.class, "opt_water")
					){
						@Override
						public void onSelect(int index) {
							if (index == 0) {
								vial.mode = DewVial.Mode.DRAW_OUT;
								GLog.p( Messages.get(Tinkerer.class, "chosen_draw") );
							} else if (index == 1) {
								vial.mode = DewVial.Mode.WATER;
								GLog.p( Messages.get(Tinkerer.class, "chosen_water") );
							}

							shroom.detach( Dungeon.hero.belongings.backpack );
							completed = true;

							Sample.INSTANCE.play( Assets.Sounds.MASTERY );
							sprite.emitter().burst( Speck.factory( Speck.LIGHT ), 10 );
							vial.updateQuickslot();
						}
					});
				}
			});

		} else {

			Game.runOnRenderThread(new Callback() {
				@Override
				public void call() {
					GameScene.show( new WndMessage( Messages.get(Tinkerer.class, "intro") ) );
				}
			});

		}

		return true;
	}
}
