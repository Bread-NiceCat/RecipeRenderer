package cn.breadnicecat.reciperenderer.entry;

import com.google.gson.JsonObject;

/**
 * Created in 2024/7/9 上午12:43
 * Project: reciperenderer
 *
 * @author <a href="https://github.com/Bread-Nicecat">Bread_NiceCat</a>
 * <p>
 *
 * <p>
 **/
public interface Storable {
	void store(JsonObject object);
}
