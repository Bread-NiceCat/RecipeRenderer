package cn.breadnicecat.reciperenderer.neoforge;

import cn.breadnicecat.reciperenderer.RPlatform;
import cn.breadnicecat.reciperenderer.RecipeRenderer;
import net.neoforged.fml.ModList;
import net.neoforged.neoforge.internal.versions.neoforge.NeoForgeVersion;
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
		return ModList.get().getModFileById(modid).versionString();
	}
	
	@Override
	public String getLoaderVersion() {
		return NeoForgeVersion.getVersion();
	}
	
	@Override
	public RecipeRenderer.Platform getPlatform() {
		return RecipeRenderer.Platform.NEOFORGE;
	}
}
