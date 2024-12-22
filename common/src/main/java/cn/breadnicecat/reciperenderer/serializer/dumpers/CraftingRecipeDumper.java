package cn.breadnicecat.reciperenderer.serializer.dumpers;

import cn.breadnicecat.reciperenderer.api.dumper.IRecipeDumper;
import cn.breadnicecat.reciperenderer.api.dumper.IRecipeInputs;
import net.minecraft.core.NonNullList;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.ShapedRecipe;
import net.minecraft.world.item.crafting.ShapelessRecipe;

/**
 * Created in 2024/12/22 03:26
 * Project: reciperenderer
 *
 * @author <a href="https://github.com/Bread-Nicecat">Bread_NiceCat</a>
 */
public class CraftingRecipeDumper {
	/**
	 * @author youyihj
	 */
	//@IRecipeDumper.For(ShapedRecipe.class)
	public static class Shaped implements IRecipeDumper<ShapedRecipe> {
		
		@Override
		public void setInputs(ShapedRecipe recipe, IRecipeInputs inputs) {
			int width = recipe.getWidth();
			NonNullList<Ingredient> ingredients = recipe.getIngredients();
			for (int i = 0; i < ingredients.size(); i++) {
				Ingredient ingredient = ingredients.get(i);
				int x = i % width;
				int y = i / width;
				inputs.addInput(y * 3 + x + 1, ingredient);
			}
		}
		
		@Override
		public String getRecipeType(ShapedRecipe recipe) {
			return "shaped";
		}
	}
	
	/**
	 * @author youyihj
	 */
	//@IRecipeDumper.For(ShapelessRecipe.class)
	public static class Shapeless implements IRecipeDumper<ShapelessRecipe> {
		
		@Override
		public void setInputs(ShapelessRecipe recipe, IRecipeInputs inputs) {
			NonNullList<Ingredient> ingredients = recipe.getIngredients();
			for (int i = 0; i < ingredients.size(); i++) {
				inputs.addInput(i + 1, ingredients.get(i));
			}
		}
		
		@Override
		public String getRecipeType(ShapelessRecipe recipe) {
			return "shapeless";
		}
	}
}
