package cn.breadnicecat.reciperenderer.forge;

import cn.breadnicecat.reciperenderer.RecipeRenderer;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.fml.Bindings;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

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
		FMLJavaModLoadingContext.get().getModEventBus().addListener(this::onRegisterCommands);
		Bindings.getForgeBus().get().addListener(this::onClientTick);
	}
	
	public void onRegisterCommands(RegisterCommandsEvent event) {
		_onRegisterCMD(event.getDispatcher());
	}
	
	public void onClientTick(TickEvent.ClientTickEvent event) {
		_onClientTick();
	}
}
