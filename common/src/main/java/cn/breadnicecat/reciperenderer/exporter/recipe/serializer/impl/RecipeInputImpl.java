package cn.breadnicecat.reciperenderer.exporter.recipe.serializer.impl;

import cn.breadnicecat.reciperenderer.api.dumper.IRecipeInputs;
import cn.breadnicecat.reciperenderer.utils.RRUtils;
import com.google.gson.JsonObject;
import com.mojang.datafixers.util.Pair;
import it.unimi.dsi.fastutil.ints.Int2ObjectAVLTreeMap;
import net.minecraft.world.item.crafting.Ingredient;

/**
 * @author youyihj
 * @author Bread_NiceCat
 */
public class RecipeInputImpl implements IRecipeInputs {
	Int2ObjectAVLTreeMap<Pair<Ingredient, Integer>> inputs = new Int2ObjectAVLTreeMap<>();
	
	@Override
	public void addInput(int slot, Ingredient ingredient, int count) {
		if (!ingredient.isEmpty()) {
			inputs.put(slot, Pair.of(ingredient, count));
		}
	}
	
	@Override
	public JsonObject serialize() {
		JsonObject object = new JsonObject();
		inputs.forEach((i, value) -> {
			Ingredient ingredient = value.getFirst();
			Integer count = value.getSecond();
			JsonObject ingredientJson = RRUtils.serializeIngredient(ingredient);
			ingredientJson.addProperty("count", count);
			object.add(String.valueOf(i), ingredientJson);
		});
		return object;
	}
}
