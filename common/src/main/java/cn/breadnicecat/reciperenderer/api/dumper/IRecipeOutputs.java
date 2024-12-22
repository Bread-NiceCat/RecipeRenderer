package cn.breadnicecat.reciperenderer.api.dumper;

import com.google.gson.JsonObject;
import net.minecraft.world.item.ItemStack;

/**
 * @author youyihj
 * @author Bread_NiceCat
 */
public interface IRecipeOutputs {
	void addOutput(int slot, ItemStack stack);
	
	JsonObject serialize();
}
