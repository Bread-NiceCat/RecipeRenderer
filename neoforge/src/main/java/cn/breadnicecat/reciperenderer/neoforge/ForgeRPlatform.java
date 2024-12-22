package cn.breadnicecat.reciperenderer.neoforge;

import cn.breadnicecat.reciperenderer.platform.RPlatform;
import net.neoforged.fml.ModList;
import net.neoforged.fml.loading.FMLLoader;
import net.neoforged.neoforge.internal.versions.neoforge.NeoForgeVersion;
import net.neoforged.neoforgespi.language.IModFileInfo;
import net.neoforged.neoforgespi.language.IModInfo;

import java.util.stream.Stream;

/**
 * Created in 2024/7/11 上午8:50
 * Project: reciperenderer
 *
 * @author <a href="https://github.com/Bread-Nicecat">Bread_NiceCat</a>
 * <p>
 *
 * <p>
 **/
public class ForgeRPlatform implements RPlatform {
	@Override
	public Stream<String> listMods() {
		return ModList.get().getMods().stream().map(IModInfo::getModId);
	}
	
	@Override
	public String getVersion(String modid) {
		IModFileInfo file = ModList.get().getModFileById(modid);
		return file != null ? file.versionString() : null;
	}
	
	@Override
	public boolean isLoaded(String modid) {
		return ModList.get().isLoaded(modid);
	}
	
	@Override
	public boolean isClient() {
		return FMLLoader.getDist().isClient();
	}
	
	@Override
	public String getLoaderVersion() {
		return NeoForgeVersion.getVersion();
	}
	
	
	@Override
	public Loader getLoader() {
		return Loader.neoforge;
	}
}
