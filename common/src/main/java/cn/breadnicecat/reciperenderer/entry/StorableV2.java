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
public interface StorableV2 {
	/**
	 * @param extraWriter <将要写入的path，数据byte[]> 如果写入成功则返回完整的引用路径，否则为null
	 */
	int store(ExistHelper existHelper, BiFunction<String, byte @Nullable [], String> extraWriter, JsonObject object, ExportLogger logger);
	
}
