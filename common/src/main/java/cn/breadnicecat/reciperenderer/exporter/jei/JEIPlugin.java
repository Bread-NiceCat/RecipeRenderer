package cn.breadnicecat.reciperenderer.exporter.jei;

import cn.breadnicecat.reciperenderer.utils.RRUtils;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.runtime.IJeiRuntime;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
	private static final Logger logger = LoggerFactory.getLogger(JEIPlugin.class);
	
	public static JEIPlugin INSTANCE;
	public final static @NotNull ResourceLocation UID = RRUtils.modPrefix("jei");
	public IJeiRuntime runtime;
	
	@Override
	public @NotNull ResourceLocation getPluginUid() {
		return UID;
	}
	
	@Override
	public void onRuntimeAvailable(IJeiRuntime runtime) {
		this.runtime = runtime;
		INSTANCE = this;
		logger.info("JEI插件已经就绪");
	}
	
}
