package cn.breadnicecat.reciperenderer.api.dumper;

import com.google.gson.JsonObject;
import net.minecraft.world.item.crafting.Ingredient;

/**
 * @author youyihj
 * @author Bread_NiceCat
 */
public interface IRecipeInputs {
	void addInput(int slot, Ingredient ingredient, int count);
	
	default void addInput(int slot, Ingredient ingredient) {
		addInput(slot, ingredient, 1);
	}
	
	JsonObject serialize();
}
