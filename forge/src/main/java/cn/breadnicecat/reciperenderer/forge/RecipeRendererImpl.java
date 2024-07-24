package cn.breadnicecat.reciperenderer.forge;

import cn.breadnicecat.reciperenderer.RecipeRenderer;
import cn.breadnicecat.reciperenderer.cmd.ICmdFeedback;
import cn.breadnicecat.reciperenderer.cmd.ModidArgumentType;
import cn.breadnicecat.reciperenderer.datafix.IconRStorer;
import cn.breadnicecat.reciperenderer.datafix.RRStorer;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.commands.CommandSourceStack;
import net.minecraftforge.client.event.RegisterClientCommandsEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import static cn.breadnicecat.reciperenderer.RecipeRenderer.*;
import static cn.breadnicecat.reciperenderer.utils.CommonUtils.accept;
import static net.minecraft.commands.Commands.argument;
import static net.minecraft.commands.Commands.literal;

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
		MinecraftForge.EVENT_BUS.addListener(this::onRegisterClientCommands);
	}
	
	private ICmdFeedback create(CommandSourceStack source) {
		return ICmdFeedback.create(source::getEntity, source::sendSystemMessage, source::sendFailure);
	}
	
	
	@SubscribeEvent
	public void onRegisterClientCommands(RegisterClientCommandsEvent event) {
		CommandDispatcher<CommandSourceStack> dispatcher = event.getDispatcher();
		
		accept(root -> {
					Command<CommandSourceStack> mod_rr = (s) -> {
						String modid = s.getArgument("modid", String.class);
						return export(modid, create(s.getSource()), RRStorer.INSTANCE);
					};
					Command<CommandSourceStack> mod_iconr = s -> {
						String modid = s.getArgument("modid", String.class);
						return export(modid, create(s.getSource()), IconRStorer.DEFAULT);
					};
					Command<CommandSourceStack> all_rr =
							s -> exportAll(create(s.getSource()), RRStorer.INSTANCE);
					Command<CommandSourceStack> all_iconr =
							s -> exportAll(create(s.getSource()), IconRStorer.DEFAULT);
					
					
					dispatcher.register(
							root.then(literal("export")
									.then(argument("modid", ModidArgumentType.INSTANCE)
											.executes(mod_rr)
											.then(literal("by")
													.then(literal("rr")
															.executes(mod_rr))
													.then(literal("iconr")
															.executes(mod_iconr)))
									).then(literal("all")
											.executes(all_rr)
											.then(literal("by")
													.then(literal("rr")
															.executes(all_rr))
													.then(literal("iconr")
															.executes(all_iconr))
											)
									)
							));
				}
				, literal(MOD_ID), literal("rr"));
	}
	
	
}
