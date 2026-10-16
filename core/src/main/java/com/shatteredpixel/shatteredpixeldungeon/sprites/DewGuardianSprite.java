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

package com.shatteredpixel.shatteredpixeldungeon.sprites;

public class DewGuardianSprite extends EarthGuardianSprite {

	public DewGuardianSprite() {
		super();
		resetColor();
	}

	@Override
	public void resetColor() {
		super.resetColor();
		// Verdant emerald / dew tint
		tint(0x22EE66, 0.45f);
	}

	@Override
	public int blood() {
		// Dewdrop glowing emerald fluid
		return 0xFF22CC66;
	}
}
