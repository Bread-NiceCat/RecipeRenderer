package cn.breadnicecat.reciperenderer.platform;

import cn.breadnicecat.reciperenderer.RRExtension;
import cn.breadnicecat.reciperenderer.RecipeRenderer;
import cn.breadnicecat.reciperenderer.utils.RRUtils;
import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static net.minecraft.commands.Commands.literal;

/**
 * Created in 2024/12/8 00:07
 * Project: reciperenderer
 *
 * @author <a href="https://github.com/Bread-Nicecat">Bread_NiceCat</a>
 * <p>
 * 反向钩子
 * <p>
 **/
public class PlatformInvHooks {
	
	private static final Logger logger = LoggerFactory.getLogger(PlatformInvHooks.class);
	
	public static void registerCommands(CommandDispatcher<CommandSourceStack> dispatcher, CommandBuildContext context, Commands.CommandSelection selection) {
		logger.info("正在为{}注册指令", selection);
		var open = literal("open").executes(c -> {
			RRUtils.open(RecipeRenderer.exportDir);
			return 0;
		});
		var export = RRUtils.apply(literal("export"), (c) -> {
			RecipeRenderer.getAllExtensions().forEach((es) -> {
				String id = es.getKey();
				RRExtension ex = es.getValue();
				c.then(literal(id).then(ex.buildCommand()));
			});
		});
		dispatcher.register(literal(RecipeRenderer.MOD_ID).then(open).then(export));
		dispatcher.register(literal("rr").then(open).then(export));
	}
}
