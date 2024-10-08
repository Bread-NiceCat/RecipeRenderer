package cn.breadnicecat.reciperenderer.fabric;

import cn.breadnicecat.reciperenderer.RPlatform;
import cn.breadnicecat.reciperenderer.RecipeRenderer;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.impl.FabricLoaderImpl;

import java.util.stream.Stream;

/**
 * Created in 2024/7/11 上午8:47
 * Project: reciperenderer
 *
 * @author <a href="https://github.com/Bread-Nicecat">Bread_NiceCat</a>
 * <p>
 *
 * <p>
 **/
public class FabricRPlatform implements RPlatform {
	@Override
	public Stream<String> listMods() {
		return FabricLoader.getInstance().getAllMods().stream().map(i -> i.getMetadata().getId());
	}
	
	@Override
	public String getVersion(String modid) {
		return FabricLoader.getInstance().getModContainer(modid).orElseThrow().getMetadata().getVersion().getFriendlyString();
	}
	
	@Override
	public String getLoaderVersion() {
		return getVersion(FabricLoaderImpl.MOD_ID);
	}
	
	@Override
	public RecipeRenderer.Platform getPlatform() {
		return RecipeRenderer.Platform.FABRIC;
	}
}
