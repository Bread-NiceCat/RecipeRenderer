package cn.breadnicecat.reciperenderer.api;

import com.google.gson.JsonObject;
import com.mojang.serialization.DataResult;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.Recipe;

/**
 * Created in 2024/12/22 03:41
 * Project: reciperenderer
 *
 * @author <a href="https://github.com/Bread-Nicecat">Bread_NiceCat</a>
 * <p>
 *
 * <p>
 **/
public interface IRecipeSerializer {
	DataResult<JsonObject> serialize(ResourceLocation id, Recipe<?> recipe);
	
	String getSerializerName();
}
