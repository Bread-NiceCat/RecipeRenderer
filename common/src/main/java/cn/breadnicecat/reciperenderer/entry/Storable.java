package cn.breadnicecat.reciperenderer.entry;

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
public interface Storable {
	/**
	 * 返回当前数据版本，任何改动都应该向站长报备并且+1
	 */
	default int store(JsonObject object) {
		return -1;
	}
	
	/**
	 * @param writer <将要写入的path(需要后缀名)，数据byte[]> 如果写入成功则返回完整的引用路径，否则为null
	 */
	default int store(BiFunction<String, byte @Nullable [], String> writer, JsonObject object) {
		return store(object);
	}
}
