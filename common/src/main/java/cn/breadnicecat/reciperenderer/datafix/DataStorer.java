package cn.breadnicecat.reciperenderer.datafix;

import cn.breadnicecat.reciperenderer.entry.Storable;
import com.google.gson.JsonObject;
import com.mojang.datafixers.util.Pair;

/**
 * Created in 2024/7/10 上午9:02
 * Project: reciperenderer
 *
 * @author <a href="https://github.com/Bread-Nicecat">Bread_NiceCat</a>
 * <p>
 * 子类优先
 * <p>
 **/
public interface DataStorer {
	/**
	 * @return <数据/类型戳>
	 */
	Pair<JsonObject, String> store(Storable storable);
}
