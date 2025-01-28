package cn.breadnicecat.reciperenderer.utils.platform;

import cn.breadnicecat.reciperenderer.RecipeRenderer;
import cn.breadnicecat.reciperenderer.utils.RRUtils;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.brigadier.CommandDispatcher;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ConcurrentLinkedQueue;

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
public class InvHooks {
	
	private static final Logger logger = LoggerFactory.getLogger(InvHooks.class);
	
	public static void registerCommands(CommandDispatcher<CommandSourceStack> dispatcher, CommandBuildContext context, Commands.CommandSelection selection) {
		logger.info("正在注册指令");
		
		var open = literal("open").executes(c -> {
			RRUtils.open(RecipeRenderer.exportDir);
			return 0;
		});
		var export = RRUtils.apply(literal("export"), c -> {
			RecipeRenderer.getAllExporters().forEach(es -> {
				String name = es.getExporterName();
				c.then(literal(name).then(es.buildCommand()));
			});
		});
		dispatcher.register(literal(RecipeRenderer.MOD_ID).then(open).then(export));
		dispatcher.register(literal("rr").then(open).then(export));
	}
	
	private static final ConcurrentLinkedQueue<Runnable> tickQueue = new ConcurrentLinkedQueue<>();
	
	public static void hookClientTick(Runnable runnable) {
		tickQueue.add(runnable);
	}
	
	@Environment(EnvType.CLIENT)
	public static void onClientTick() {
		RenderSystem.assertOnRenderThread();
		long time = System.currentTimeMillis();
		Runnable r;
		while (System.currentTimeMillis() - time < 50 && (r = tickQueue.poll()) != null) {
			r.run();
		}
	}
}
