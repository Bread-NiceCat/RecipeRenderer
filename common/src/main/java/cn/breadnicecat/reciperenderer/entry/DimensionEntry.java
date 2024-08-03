package cn.breadnicecat.reciperenderer.entry;

import cn.breadnicecat.reciperenderer.utils.ExportLogger;
import com.google.gson.JsonObject;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.dimension.DimensionType;

/**
 * Created in 2024/7/27 上午9:38
 * Project: reciperenderer
 *
 * @author <a href="https://github.com/Bread-Nicecat">Bread_NiceCat</a>
 * <p>
 *
 * <p>
 **/
public class DimensionEntry implements Storable {
	final DimensionType value;
	
	public ResourceLocation id;
	public String name;
	
	public DimensionEntry(ResourceLocation id, String name, DimensionType value) {
		this.id = id;
		this.name = name;
		this.value = value;
	}
	
	@Override
	public int store(JsonObject object, ExportLogger logger) {
		object.addProperty("id", id.toString());
		object.addProperty("name", name);
		return 1;
	}
}
