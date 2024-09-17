package cn.breadnicecat.reciperenderer.entry;

import com.google.gson.JsonObject;
import net.minecraft.resources.ResourceLocation;

/**
 * Created in 2024/9/16 22:38
 * Project: reciperenderer
 *
 * @author <a href="https://github.com/Bread-Nicecat">Bread_NiceCat</a>
 * <p>
 *
 * <p>
 **/
public class RecipeEntry {
	public final JsonObject object;
	public final ResourceLocation type;
	
	public RecipeEntry(ResourceLocation type, JsonObject object) {
		this.object = object;
		this.type = type;
	}
	
	public JsonObject format() {
		//TODO CUSTOM OUTPUT FORMAT
		JsonObject formatted = new JsonObject();
		formatted.addProperty("type", type.toString());
		object.asMap().forEach(formatted::add);
		return formatted;
	}
}
