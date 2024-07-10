package cn.breadnicecat.reciperenderer.fabric;

import cn.breadnicecat.reciperenderer.RecipeRenderer;
import cn.breadnicecat.reciperenderer.cmd.ICmdFeedback;
import cn.breadnicecat.reciperenderer.cmd.ModidArgumentType;
import cn.breadnicecat.reciperenderer.datafix.IconrStorer;
import cn.breadnicecat.reciperenderer.datafix.RRStorer;
import com.mojang.brigadier.Command;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.fabricmc.loader.api.FabricLoader;

import java.util.stream.Stream;

import static cn.breadnicecat.reciperenderer.RecipeRenderer.MOD_ID;
import static cn.breadnicecat.reciperenderer.RecipeRenderer.export;
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
	@Override
	public void onInitialize() {
		RecipeRenderer.init();
		ClientCommandRegistrationCallback.EVENT.register((dispatcher, context) -> {
			
			Command<FabricClientCommandSource> mod_rr = (s) -> {
				String modid = s.getArgument("modid", String.class);
				FabricClientCommandSource source = s.getSource();
				return export(modid, ICmdFeedback.create(source::getEntity, source::sendFeedback, source::sendError), RRStorer.INSTANCE);
			};
			Command<FabricClientCommandSource> mod_iconr = s -> {
				String modid = s.getArgument("modid", String.class);
				FabricClientCommandSource source = s.getSource();
				return export(modid, ICmdFeedback.create(source::getEntity, source::sendFeedback, source::sendError), new IconrStorer(RRStorer.INSTANCE));
			};
			Command<FabricClientCommandSource> all_rr = s -> {
				boolean suc = listMods()
						.map(e -> {
							FabricClientCommandSource source = s.getSource();
							return (export(e, ICmdFeedback.create(source::getEntity, source::sendFeedback, source::sendError), RRStorer.INSTANCE) == 1);
						})
						.reduce(true, (a, b) -> a && b);
				return suc ? 1 : -1;
			};
			Command<FabricClientCommandSource> all_iconr = s -> {
				boolean suc = listMods()
						.map(e -> {
							FabricClientCommandSource source = s.getSource();
							return (export(e, ICmdFeedback.create(source::getEntity, source::sendFeedback, source::sendError), new IconrStorer(RRStorer.INSTANCE)) == 1);
						})
						.reduce(true, (a, b) -> a && b);
				return suc ? 1 : -1;
			};
			
			
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
	
	public static Stream<String> listMods() {
		return FabricLoader.getInstance().getAllMods().stream().map(i -> i.getMetadata().getId());
	}
	
	public static boolean isLoaded(String modid) {
		return FabricLoader.getInstance().isModLoaded(modid);
	}
	
	public static String getVersion(String modid) {
		return FabricLoader.getInstance().getModContainer(modid).orElseThrow().getMetadata().getVersion().getFriendlyString();
	}
	
	public static RecipeRenderer.Platform getPlatform() {
		return RecipeRenderer.Platform.FABRIC;
	}
}