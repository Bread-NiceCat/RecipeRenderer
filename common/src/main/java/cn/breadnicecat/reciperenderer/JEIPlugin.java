package cn.breadnicecat.reciperenderer;

import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.runtime.IJeiRuntime;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

/**
 * Created in 2024/11/24 01:38
 * Project: reciperenderer
 *
 * @author <a href="https://github.com/Bread-Nicecat">Bread_NiceCat</a>
 * <p>
 *
 * <p>
 **/
@JeiPlugin
public class JEIPlugin implements IModPlugin {
	public static JEIPlugin INSTANCE;
	public final static @NotNull ResourceLocation UID = RecipeRenderer.prefix("jei");
	private IJeiRuntime runtime;
	
	public JEIPlugin() {
		INSTANCE = this;
	}
	
	@Override
	public @NotNull ResourceLocation getPluginUid() {
		return UID;
	}
	
	@Override
	public void onRuntimeAvailable(IJeiRuntime runtime) {
		RecipeRenderer.LOGGER.info("JEI插件已经就绪");
		this.runtime = runtime;
	}
	
}
