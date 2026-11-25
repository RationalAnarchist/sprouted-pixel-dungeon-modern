package com.shatteredpixel.shatteredpixeldungeon.items.spells;

import com.shatteredpixel.shatteredpixeldungeon.Assets;
import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.Actor;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.effects.CellEmitter;
import com.shatteredpixel.shatteredpixeldungeon.effects.Speck;
import com.shatteredpixel.shatteredpixeldungeon.effects.particles.BlastParticle;
import com.shatteredpixel.shatteredpixeldungeon.items.Heap;
import com.shatteredpixel.shatteredpixeldungeon.items.Item;
import com.shatteredpixel.shatteredpixeldungeon.items.artifacts.SkeletonKey;
import com.shatteredpixel.shatteredpixeldungeon.items.bombs.Bomb;
import com.shatteredpixel.shatteredpixeldungeon.levels.Level;
import com.shatteredpixel.shatteredpixeldungeon.levels.Terrain;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.scenes.CellSelector;
import com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene;
import com.shatteredpixel.shatteredpixeldungeon.scenes.PixelScene;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSpriteSheet;
import com.shatteredpixel.shatteredpixeldungeon.utils.GLog;
import com.watabou.noosa.audio.Sample;
import com.watabou.utils.PathFinder;

import java.util.ArrayList;

public class BreachingPowder extends Item {

	public static final String AC_BREACH = "BREACH";

	{
		image = ItemSpriteSheet.ARCANE_BOMB;
		stackable = true;
		defaultAction = AC_BREACH;
	}

	@Override
	public ArrayList<String> actions(Hero hero) {
		ArrayList<String> actions = super.actions(hero);
		actions.add(AC_BREACH);
		return actions;
	}

	@Override
	public void execute(Hero hero, String action) {
		if (action.equals(AC_BREACH)) {
			curUser = hero;
			curItem = this;
			GameScene.selectCell(breacher);
		} else {
			super.execute(hero, action);
		}
	}

	@Override
	public int value() {
		return 20 * quantity;
	}

	@Override
	public boolean isIdentified() {
		return true;
	}

	protected static CellSelector.Listener breacher = new CellSelector.Listener() {
		@Override
		public void onSelect(Integer target) {
			if (target == null) return;

			if (Dungeon.bossLevel()) {
				GLog.w(Messages.get(BreachingPowder.class, "boss_level"));
				return;
			}

			int x = target % Dungeon.level.width();
			int y = target / Dungeon.level.width();
			if (x <= 0 || x >= Dungeon.level.width() - 1 || y <= 0 || y >= Dungeon.level.height() - 1) {
				GLog.w(Messages.get(BreachingPowder.class, "boundary"));
				return;
			}

			if (Dungeon.level.distance(curUser.pos, target) > 6) {
				GLog.w(Messages.get(BreachingPowder.class, "too_far"));
				return;
			}

			Heap heap = Dungeon.level.heaps.get(target);
			boolean isChest = heap != null && (heap.type == Heap.Type.LOCKED_CHEST || heap.type == Heap.Type.CRYSTAL_CHEST || heap.type == Heap.Type.CHEST);
			int terr = Dungeon.level.map[target];
			boolean isBreachableTerrain = terr == Terrain.WALL || terr == Terrain.WALL_DECO || terr == Terrain.BOOKSHELF
					|| terr == Terrain.BARRICADE || terr == Terrain.DOOR || terr == Terrain.LOCKED_DOOR
					|| terr == Terrain.CRYSTAL_DOOR || terr == Terrain.SECRET_DOOR || terr == Terrain.HERO_LKD_DR;

			if (!isChest && !isBreachableTerrain) {
				GLog.w(Messages.get(BreachingPowder.class, "invalid_target"));
				return;
			}

			CellEmitter.center(target).burst(BlastParticle.FACTORY, 12);
			CellEmitter.get(target).burst(Speck.factory(Speck.ROCK), 6);
			Sample.INSTANCE.play(Assets.Sounds.BLAST);
			PixelScene.shake(1.5f, 0.4f);

			if (isChest) {
				if (heap.type == Heap.Type.LOCKED_CHEST) {
					Buff.affect(curUser, SkeletonKey.KeyReplacementTracker.class).processGoldLockOpened();
				} else if (heap.type == Heap.Type.CRYSTAL_CHEST) {
					Buff.affect(curUser, SkeletonKey.KeyReplacementTracker.class).processCrystalLockOpened();
				}
				Sample.INSTANCE.play(Assets.Sounds.UNLOCK);
				heap.open(curUser);
			} else {
				if (terr == Terrain.LOCKED_DOOR || terr == Terrain.HERO_LKD_DR) {
					Buff.affect(curUser, SkeletonKey.KeyReplacementTracker.class).processIronLockOpened();
				} else if (terr == Terrain.CRYSTAL_DOOR) {
					Buff.affect(curUser, SkeletonKey.KeyReplacementTracker.class).processCrystalLockOpened();
				}
				Level.set(target, Terrain.EMPTY_DECO);
				GameScene.updateMap(target);
				for (int n : PathFinder.NEIGHBOURS8) {
					GameScene.updateMap(target + n);
				}
				Dungeon.level.cleanWalls();
				Dungeon.observe();
			}

			GLog.p(Messages.get(BreachingPowder.class, "breached"));
			curItem.detach(curUser.belongings.backpack);
			curUser.spendAndNext(Actor.TICK);
		}

		@Override
		public String prompt() {
			return Messages.get(BreachingPowder.class, "prompt");
		}
	};

	public static class Recipe extends com.shatteredpixel.shatteredpixeldungeon.items.Recipe.SimpleRecipe {
		{
			inputs = new Class[]{Bomb.class};
			inQuantity = new int[]{1};
			cost = 4;
			output = BreachingPowder.class;
			outQuantity = 3;
		}

		@Override
		public boolean testIngredients(ArrayList<Item> ingredients) {
			return ingredients.size() == 1 && ingredients.get(0) instanceof Bomb && ingredients.get(0).quantity() >= 1;
		}

		@Override
		public Item brew(ArrayList<Item> ingredients) {
			if (!testIngredients(ingredients)) return null;
			ingredients.get(0).quantity(ingredients.get(0).quantity() - 1);
			return sampleOutput(ingredients);
		}
	}
}
