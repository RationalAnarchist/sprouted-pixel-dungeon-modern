package com.shatteredpixel.shatteredpixeldungeon.items.bags;

import com.shatteredpixel.shatteredpixeldungeon.Assets;
import com.shatteredpixel.shatteredpixeldungeon.actors.Actor;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.effects.FloatingText;
import com.shatteredpixel.shatteredpixeldungeon.items.Item;
import com.shatteredpixel.shatteredpixeldungeon.items.scrolls.ScrollOfTeleportation;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.sprites.CharSprite;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSpriteSheet;
import com.shatteredpixel.shatteredpixeldungeon.utils.GLog;
import com.watabou.noosa.audio.Sample;

import java.util.ArrayList;

public class BackpackExpander extends Item {

	public static final String AC_APPLY = "APPLY";

	{
		image = ItemSpriteSheet.POUCH;
		stackable = false;
		defaultAction = AC_APPLY;
	}

	@Override
	public ArrayList<String> actions(Hero hero) {
		ArrayList<String> actions = super.actions(hero);
		actions.add(AC_APPLY);
		return actions;
	}

	@Override
	public void execute(Hero hero, String action) {
		if (action.equals(AC_APPLY)) {
			hero.belongings.extraBagCapacity += 4;
			detach(hero.belongings.backpack);
			hero.sprite.operate(hero.pos);
			Sample.INSTANCE.play(Assets.Sounds.ITEM);
			hero.sprite.showStatusWithIcon(CharSprite.POSITIVE, "+4 SLOTS", FloatingText.ENERGY);
			GLog.p(Messages.get(BackpackExpander.class, "expanded"));
			hero.spendAndNext(Actor.TICK);
		} else {
			super.execute(hero, action);
		}
	}

	@Override
	public int value() {
		return 40;
	}

	@Override
	public boolean isIdentified() {
		return true;
	}

	public static class Recipe extends com.shatteredpixel.shatteredpixeldungeon.items.Recipe.SimpleRecipe {
		{
			inputs = new Class[]{ScrollOfTeleportation.class};
			inQuantity = new int[]{1};
			cost = 6;
			output = BackpackExpander.class;
			outQuantity = 1;
		}

		@Override
		public boolean testIngredients(ArrayList<Item> ingredients) {
			return ingredients.size() == 1 && ingredients.get(0) instanceof ScrollOfTeleportation && ingredients.get(0).quantity() >= 1;
		}

		@Override
		public Item brew(ArrayList<Item> ingredients) {
			if (!testIngredients(ingredients)) return null;
			ingredients.get(0).quantity(ingredients.get(0).quantity() - 1);
			return sampleOutput(ingredients);
		}
	}
}
