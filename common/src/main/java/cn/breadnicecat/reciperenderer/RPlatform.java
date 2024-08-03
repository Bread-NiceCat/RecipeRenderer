package cn.breadnicecat.reciperenderer;

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
	
	boolean isLoaded(String modid);
	
	RecipeRenderer.Platform getPlatform();
	
	String getVersion(String modid);
	
	String getLoaderVersion();
	
	String getName();
}
