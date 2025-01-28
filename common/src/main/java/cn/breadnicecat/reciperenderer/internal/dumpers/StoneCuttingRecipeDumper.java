package cn.breadnicecat.reciperenderer.internal.dumpers;

import cn.breadnicecat.reciperenderer.api.dumper.IRecipeDumper;
import cn.breadnicecat.reciperenderer.api.dumper.IRecipeInputs;
import net.minecraft.world.item.crafting.StonecutterRecipe;

/**
 * @author youyihj
 */
//@IRecipeDumper.For(StonecutterRecipe.class)
public class StoneCuttingRecipeDumper implements IRecipeDumper<StonecutterRecipe> {
	@Override
	public void setInputs(StonecutterRecipe recipe, IRecipeInputs inputs) {
		inputs.addInput(1, recipe.getIngredients().getFirst());
	}
}
