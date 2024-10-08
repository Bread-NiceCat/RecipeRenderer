package cn.breadnicecat.reciperenderer.entry;

import cn.breadnicecat.reciperenderer.utils.ExistHelper;
import cn.breadnicecat.reciperenderer.utils.ExportLogger;
import com.google.gson.JsonObject;
import org.jetbrains.annotations.Nullable;

import java.util.function.BiFunction;

/**
 * Created in 2024/7/9 上午12:43
 * Project: reciperenderer
 *
 * @author <a href="https://github.com/Bread-Nicecat">Bread_NiceCat</a>
 * <p>
 * 任何Entry都应该实现此接口,导出了不序列化有啥用？
 * <p>
 **/
public interface Storable extends StorableV2 {
	/**
	 * 返回当前数据版本，任何改动都应该向站长报备并且+1
	 */
	int store(JsonObject object, ExportLogger logger);
	
	@Override
	default int store(ExistHelper existHelper, BiFunction<String, byte @Nullable [], String> writer, JsonObject object, ExportLogger logger) {
		return store(object, logger);
	}
}
