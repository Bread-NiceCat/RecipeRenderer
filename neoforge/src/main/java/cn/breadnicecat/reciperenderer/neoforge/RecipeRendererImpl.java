package cn.breadnicecat.reciperenderer.neoforge;

import cn.breadnicecat.reciperenderer.RecipeRenderer;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.loading.FMLLoader;
import net.neoforged.neoforge.client.event.RenderFrameEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.data.loading.DatagenModLoader;
import net.neoforged.neoforge.event.RegisterCommandsEvent;

import static cn.breadnicecat.reciperenderer.RecipeRenderer.*;

/**
 * Created in 2024/7/8 下午5:12
 * Project: reciperenderer
 *
 * @author <a href="https://github.com/Bread-Nicecat">Bread_NiceCat</a>
 * <p>
 *
 * <p>
 **/
@Mod(MOD_ID)
public class RecipeRendererImpl {
	public RecipeRendererImpl() {
		if (FMLLoader.getDist() == Dist.DEDICATED_SERVER || DatagenModLoader.isRunningDataGen()) {
			System.out.println("处于服务器或Datagen环境下,跳过加载!");
			return;
		}
		
		RecipeRenderer.init(new ForgeRPlatform());
		IEventBus eventBus = NeoForge.EVENT_BUS;
		eventBus.addListener(this::onRegisterCommands);
		eventBus.addListener(this::onFrameUpdate);
	}
	
	public void onRegisterCommands(RegisterCommandsEvent event) {
		_onRegisterCMD(event.getBuildContext(), event.getDispatcher());
	}
	
	public void onFrameUpdate(RenderFrameEvent.Post event) {
		_onFrameUpdate();
	}
}
