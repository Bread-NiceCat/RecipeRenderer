package cn.breadnicecat.reciperenderer.internal.dumpers;

import cn.breadnicecat.reciperenderer.api.dumper.IRecipeDumper;
import cn.breadnicecat.reciperenderer.api.dumper.IRecipeInputs;
import com.google.gson.JsonObject;
import net.minecraft.world.item.crafting.AbstractCookingRecipe;

/**
 * @author youyihj
 */
//@IRecipeDumper.For(BlastingRecipe.class)
//@IRecipeDumper.For(CampfireCookingRecipe.class)
//@IRecipeDumper.For(FurnaceRecipe.class)
//@IRecipeDumper.For(SmokingRecipe.class)
public class CookingRecipeDumper implements IRecipeDumper<AbstractCookingRecipe> {
	
	@Override
	public void setInputs(AbstractCookingRecipe recipe, IRecipeInputs inputs) {
		inputs.addInput(1, recipe.getIngredients().getFirst());
	}
	
	@Override
	public void writeExtraInformation(AbstractCookingRecipe recipe, JsonObject jsonObject) {
		jsonObject.addProperty("experience", recipe.getExperience());
		jsonObject.addProperty("cookTime", recipe.getCookingTime());
	}
}
