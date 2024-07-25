package cn.breadnicecat.reciperenderer.fabric;

import cn.breadnicecat.reciperenderer.RecipeRenderer;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;

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
	
	@Override
	public void onInitialize() {
		RecipeRenderer.init(new FabricRPlatform());
		CommandRegistrationCallback.EVENT.register((dispatcher, a, b) -> RecipeRenderer._onRegisterCMD(dispatcher));
		ClientTickEvents.START_CLIENT_TICK.register((e) -> RecipeRenderer._onClientTick());
	}
	
	
}