package cn.breadnicecat.reciperenderer.platform;

import cn.breadnicecat.reciperenderer.RecipeRenderer;
import org.jetbrains.annotations.Nullable;

import java.util.stream.Stream;

/**
 * Created in 2024/7/11 上午8:45
 * Project: reciperenderer
 *
 * @author <a href="https://github.com/Bread-Nicecat">Bread_NiceCat</a>
 * <p>
 *
 * <p>
 **/
public interface RPlatform {
	Stream<String> listMods();
	
	Loader getLoader();
	
	/**
	 * @return 获取指定mod的版本, 当不存在时返回null
	 */
	@Nullable String getVersion(String modid);
	
	default String getRRVersion() {
		return getVersion(RecipeRenderer.MOD_ID);
	}
	
	boolean isLoaded(String modid);
	
	boolean isClient();
	
	/**
	 * @return 获取模组加载器的版本
	 */
	String getLoaderVersion();
	
	default String getLoaderName() {
		return getLoader().toString();
	}
	
	enum Loader {
		neoforge, fabric;
	}
	
}
