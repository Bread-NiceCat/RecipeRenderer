package cn.breadnicecat.reciperenderer;

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
	
	RecipeRenderer.Platform getPlatform();
	
	@Nullable String getVersion(String modid);
	
	default boolean isLoaded(String modid) {
		return getVersion(modid) != null;
	}
	
	String getLoaderVersion();
	
	default String getLoaderName() {
		return getPlatform().getName();
	}
}
