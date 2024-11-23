package cn.breadnicecat.reciperenderer.neoforge;

import cn.breadnicecat.reciperenderer.RecipeRenderer;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.client.event.RegisterClientCommandsEvent;
import net.neoforged.neoforge.common.NeoForge;

import static cn.breadnicecat.reciperenderer.RecipeExporter.getAllRecipeTypes;
import static cn.breadnicecat.reciperenderer.RecipeRenderer.MOD_ID;
import static com.mojang.brigadier.arguments.StringArgumentType.getString;
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
		IEventBus eventBus = NeoForge.EVENT_BUS;
		eventBus.addListener(this::onRegisterCommands);
	}
	
	public void onRegisterCommands(RegisterClientCommandsEvent event) {
		CommandDispatcher<CommandSourceStack> dispatcher = event.getDispatcher();
		CommandBuildContext context = event.getBuildContext();
		
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
									RecipeRenderer.export(getString(c, "recipe_type"));
									return 0;
								})
						)
				)
		);
	}
}
