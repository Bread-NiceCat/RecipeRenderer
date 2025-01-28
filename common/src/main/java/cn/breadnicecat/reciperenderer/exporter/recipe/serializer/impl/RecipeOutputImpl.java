package cn.breadnicecat.reciperenderer.exporter.recipe.serializer.impl;

import cn.breadnicecat.reciperenderer.api.dumper.IRecipeOutputs;
import com.google.gson.JsonObject;
import it.unimi.dsi.fastutil.ints.Int2ObjectAVLTreeMap;
import net.minecraft.world.item.ItemStack;

/**
 * @author youyihj
 * @author Bread_NiceCat
 */
public class RecipeOutputImpl implements IRecipeOutputs {
	Int2ObjectAVLTreeMap<ItemStack> outputs = new Int2ObjectAVLTreeMap<>();
	
	@Override
	public void addOutput(int slot, ItemStack stack) {
		outputs.put(slot, stack);
	}
	
	@Override
	public JsonObject serialize() {
		JsonObject json = new JsonObject();
		outputs.forEach((i, stack) -> {
			JsonObject object = new JsonObject();
			object.addProperty("item", stack.getItem().toString());
			object.addProperty("count", stack.getCount());
			//站长说百科不支持NBT
//				if (stack.hasTag()) {
//					object.addProperty("nbt", stack.getTag().toString());
//				}
			json.add(String.valueOf(i), object);
		});
		return json;
	}
}
