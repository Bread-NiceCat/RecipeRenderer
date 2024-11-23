package cn.breadnicecat.reciperenderer.fabric;

import cn.breadnicecat.reciperenderer.RecipeRenderer;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.resources.ResourceLocation;

import static cn.breadnicecat.reciperenderer.RecipeExporter.getAllRecipeTypes;
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
		RecipeRenderer.init(new FabricRPlatform());
		ClientCommandRegistrationCallback.EVENT.register(this::onRegisterCommand);
	}
	
	private void onRegisterCommand(CommandDispatcher<FabricClientCommandSource> dispatcher, CommandBuildContext context) {
		dispatcher.register(literal(RecipeRenderer.MOD_ID)
				.then(literal("open").executes(c -> {
					RecipeRenderer.open();
					return 0;
				}))
				.then(literal("export")
						.then(argument("recipe_type", StringArgumentType.greedyString())
								.suggests((c, builder) -> {
									getAllRecipeTypes().map(ResourceLocation::getNamespace).distinct().forEach(m -> builder.suggest(m + ":*"));
									getAllRecipeTypes().map(ResourceLocation::toString).forEach(builder::suggest);
									return builder.buildFuture();
								})
								.executes(c -> {
									RecipeRenderer.export(StringArgumentType.getString(c, "recipe_type"));
									return 0;
								})
						)
				)
		);
	}
	
}