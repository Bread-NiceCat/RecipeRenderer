package cn.breadnicecat.reciperenderer.neoforge;

import cn.breadnicecat.reciperenderer.RecipeRenderer;
import cn.breadnicecat.reciperenderer.platform.PlatformInvHooks;
import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.RegisterCommandsEvent;

import static cn.breadnicecat.reciperenderer.RecipeRenderer.MOD_ID;

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
		IEventBus eventBus = NeoForge.EVENT_BUS;
		eventBus.addListener(this::onRegisterCommands);
	}
	
	public void onRegisterCommands(RegisterCommandsEvent event) {
		CommandDispatcher<CommandSourceStack> dispatcher = event.getDispatcher();
		CommandBuildContext context = event.getBuildContext();
		Commands.CommandSelection selection = event.getCommandSelection();
		PlatformInvHooks.registerCommands(dispatcher, context, selection);
	}
}
