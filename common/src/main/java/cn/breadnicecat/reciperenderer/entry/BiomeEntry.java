package cn.breadnicecat.reciperenderer.entry;

import com.google.gson.JsonObject;
import net.minecraft.resources.ResourceLocation;

/**
 * Created in 2024/7/25 上午3:45
 * Project: reciperenderer
 *
 * @author <a href="https://github.com/Bread-Nicecat">Bread_NiceCat</a>
 * <p>
 *
 * <p>
 **/
public class BiomeEntry implements Storable {
	public ResourceLocation id;
	public String name;
	
	public BiomeEntry(ResourceLocation id, String name) {
		this.id = id;
		this.name = name;
	}
	
	@Override
	public int store(JsonObject object) {
		object.addProperty("id", id.toString());
		object.addProperty("name", name);
		return 1;
	}
}
