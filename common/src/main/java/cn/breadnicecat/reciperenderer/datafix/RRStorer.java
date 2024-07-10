package cn.breadnicecat.reciperenderer.datafix;

import cn.breadnicecat.reciperenderer.entry.Storable;
import com.google.gson.JsonObject;
import com.mojang.datafixers.util.Pair;

import static cn.breadnicecat.reciperenderer.RecipeRenderer.MOD_ID;

/**
 * Created in 2024/7/10 上午9:05
 * Project: reciperenderer
 *
 * @author <a href="https://github.com/Bread-Nicecat">Bread_NiceCat</a>
 * <p>
 *
 * <p>
 **/
public class RRStorer implements DataStorer {
	public static RRStorer INSTANCE = new RRStorer();
	
	private RRStorer() {
	}
	
	@Override
	public Pair<JsonObject, String> store(Storable storable) {
		JsonObject object = new JsonObject();
		storable.store(object);
		return Pair.of(object, MOD_ID);
	}
}
