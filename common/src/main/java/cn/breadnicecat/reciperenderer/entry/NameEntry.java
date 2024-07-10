package cn.breadnicecat.reciperenderer.entry;

import com.google.gson.JsonObject;

/**
 * Created in 2024/7/10 下午3:23
 * Project: reciperenderer
 *
 * @author <a href="https://github.com/Bread-Nicecat">Bread_NiceCat</a>
 * <p>
 *
 * <p>
 **/
public class NameEntry implements Storable {
	
	private final String id;
	private final String name;
	
	public NameEntry(String id, String name) {
		this.id = id;
		this.name = name;
	}
	
	@Override
	public void store(JsonObject object) {
		object.addProperty("id", id);
		object.addProperty("name", name);
	}
}
