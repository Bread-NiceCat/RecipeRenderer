package cn.breadnicecat.reciperenderer.fabric;

import cn.breadnicecat.reciperenderer.utils.platform.RPlatform;
import net.fabricmc.api.EnvType;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.ModContainer;
import net.fabricmc.loader.impl.FabricLoaderImpl;

import java.util.Optional;
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
		Optional<ModContainer> container = FabricLoader.getInstance().getModContainer(modid);
		return container.isPresent() ? container.orElseThrow().getMetadata().getVersion().getFriendlyString() : null;
	}
	
	@Override
	public boolean isLoaded(String modid) {
		return FabricLoader.getInstance().isModLoaded(modid);
	}
	
	@Override
	public boolean isClient() {
		return FabricLoader.getInstance().getEnvironmentType() == EnvType.CLIENT;
	}
	
	@Override
	public String getLoaderVersion() {
		return getVersion(FabricLoaderImpl.MOD_ID);
	}
	
	@Override
	public Loader getLoader() {
		return Loader.fabric;
	}
	
}
