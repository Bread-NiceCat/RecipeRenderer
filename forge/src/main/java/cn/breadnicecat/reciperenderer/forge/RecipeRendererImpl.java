package cn.breadnicecat.reciperenderer.forge;

import cn.breadnicecat.reciperenderer.RecipeRenderer;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;

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
		RecipeRenderer.init(new ForgeRPlatform());
		IEventBus eventBus = MinecraftForge.EVENT_BUS;
		eventBus.addListener(this::onRegisterCommands);
		eventBus.addListener(this::onClientTick);
	}
	
	public void onRegisterCommands(RegisterCommandsEvent event) {
		_onRegisterCMD(event.getBuildContext(), event.getDispatcher());
	}
	
	public void onClientTick(TickEvent.ClientTickEvent event) {
		_onClientTick();
	}
}
