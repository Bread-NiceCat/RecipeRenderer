package cn.breadnicecat.reciperenderer.fabric;

import cn.breadnicecat.reciperenderer.RecipeRenderer;
import cn.breadnicecat.reciperenderer.cmd.ICmdFeedback;
import cn.breadnicecat.reciperenderer.cmd.ModidArgumentType;
import cn.breadnicecat.reciperenderer.datafix.IconRStorer;
import cn.breadnicecat.reciperenderer.datafix.RRStorer;
import com.mojang.brigadier.Command;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;

import static cn.breadnicecat.reciperenderer.RecipeRenderer.*;
import static cn.breadnicecat.reciperenderer.utils.CommonUtils.accept;
import static net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.argument;
import static net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.literal;

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
	private ICmdFeedback create(FabricClientCommandSource source) {
		return ICmdFeedback.create(source::getEntity, source::sendFeedback, source::sendError);
	}
	
	@Override
	public void onInitialize() {
		RecipeRenderer.init(new FabricRPlatform());
		ClientCommandRegistrationCallback.EVENT.register((dispatcher, context) -> {
			
			Command<FabricClientCommandSource> mod_rr = (s) -> {
				String modid = s.getArgument("modid", String.class);
				return export(modid, create(s.getSource()), RRStorer.INSTANCE);
			};
			Command<FabricClientCommandSource> mod_iconr = s -> {
				String modid = s.getArgument("modid", String.class);
				return export(modid, create(s.getSource()), IconRStorer.DEFAULT);
			};
			Command<FabricClientCommandSource> all_rr =
					s -> exportAll(create(s.getSource()), RRStorer.INSTANCE);
			Command<FabricClientCommandSource> all_iconr =
					s -> exportAll(create(s.getSource()), IconRStorer.DEFAULT);
			
			
			accept((root) -> dispatcher.register(
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
							))
					, literal(MOD_ID), literal("rr")
			);
		});
	}
	
	
}