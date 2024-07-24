package cn.breadnicecat.reciperenderer.forge;

import cn.breadnicecat.reciperenderer.RPlatform;
import cn.breadnicecat.reciperenderer.RecipeRenderer;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.forgespi.language.IModInfo;

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
	public boolean isLoaded(String modid) {
		return ModList.get().isLoaded(modid);
	}
	
	@Override
	public String getVersion(String modid) {
		return ModList.get().getModFileById(modid).versionString();
	}
	
	@Override
	public RecipeRenderer.Platform getPlatform() {
		return RecipeRenderer.Platform.FORGE;
	}
}
