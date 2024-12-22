package cn.breadnicecat.reciperenderer.serializer.impl;

import cn.breadnicecat.reciperenderer.api.dumper.IRecipeInputs;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.JsonOps;
import it.unimi.dsi.fastutil.ints.Int2ObjectAVLTreeMap;
import net.minecraft.world.item.crafting.Ingredient;

/**
 * @author youyihj
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
			JsonElement ingredientJson = Ingredient.CODEC.encodeStart(JsonOps.INSTANCE, ingredient).getOrThrow();
			if (ingredientJson instanceof JsonArray array) {
				//物品组
				JsonObject o1 = new JsonObject();
				o1.add("items", array);
				o1.addProperty("count", count);
				ingredientJson = o1;
			} else {
				ingredientJson.getAsJsonObject().addProperty("count", count);
			}
			object.add(String.valueOf(i), ingredientJson);
		});
		return object;
	}
}
