package cn.breadnicecat.reciperenderer.fabric;

import cn.breadnicecat.reciperenderer.RecipeRenderer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.impl.datagen.FabricDataGenHelper;
import net.fabricmc.loader.api.FabricLoader;

/**
 * Created in 2024/7/8 下午5:09
 * Project: reciperenderer
 *
 * @author <a href="https://github.com/Bread-Nicecat">Bread_NiceCat</a>
 * <p>
 *
 * <p>
 **/
public class RecipeRendererImpl implements ModInitializer {
	
	@SuppressWarnings("UnstableApiUsage")
	@Override
	public void onInitialize() {
		if (FabricLoader.getInstance().getEnvironmentType() == EnvType.SERVER || FabricDataGenHelper.ENABLED) {
			System.out.println("处于服务器或Datagen环境下,跳过加载!");
			return;
		}
		RecipeRenderer.init(new FabricRPlatform());
		WorldRenderEvents.END.register((p) -> {
			RecipeRenderer._onFrameUpdate();
		});
		CommandRegistrationCallback.EVENT.register((dispatcher, context, b) -> RecipeRenderer._onRegisterCMD(context, dispatcher));
	}
	
}