package cn.breadnicecat.reciperenderer.fabric;

import cn.breadnicecat.reciperenderer.RecipeRenderer;
import cn.breadnicecat.reciperenderer.cmd.ICmdFeedback;
import cn.breadnicecat.reciperenderer.cmd.ModidArgumentType;
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
		ClientCommandRegistrationCallback.EVENT.register((dispatcher, context) ->
				accept((root) -> dispatcher.register(root
								.then(literal("export")
										.then(argument("modid", ModidArgumentType.INSTANCE)
												.executes((s) -> {
													String modid = s.getArgument("modid", String.class);
													FabricClientCommandSource source = s.getSource();
													return export(modid, ICmdFeedback.create(source::sendFeedback, source::sendError));
												})
										).then(literal("all")
												.executes(s -> {
													boolean suc = listMods()
															.map(e -> {
																FabricClientCommandSource source = s.getSource();
																return (export(e, ICmdFeedback.create(source::sendFeedback, source::sendError)) == 1);
															})
															.reduce(true, (a, b) -> a && b);
													return suc ? 1 : -1;
												}))
								))
						, literal(MOD_ID), literal("rr")
				));
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