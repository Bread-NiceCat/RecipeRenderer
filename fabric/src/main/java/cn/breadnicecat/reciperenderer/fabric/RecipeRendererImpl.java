package cn.breadnicecat.reciperenderer.fabric;

import cn.breadnicecat.reciperenderer.RecipeRenderer;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.impl.datagen.FabricDataGenHelper;

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
		if (FabricDataGenHelper.ENABLED) {
			return;
		}
		RecipeRenderer.init(new FabricRPlatform());
		CommandRegistrationCallback.EVENT.register((dispatcher, context, b) -> RecipeRenderer._onRegisterCMD(context, dispatcher));
//		ClientTickEvents.END_CLIENT_TICK.register((e) -> RecipeRenderer._onClientTick());
		WorldRenderEvents.END.register((e) -> RecipeRenderer._onClientTick());
	}
	
}