package cn.breadnicecat.reciperenderer;

import com.mojang.brigadier.CommandDispatcher;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.HashMap;
import java.util.concurrent.LinkedBlockingDeque;

import static net.minecraft.commands.Commands.literal;

/**
 * Created in 2024/7/8 下午5:11
 * Project: reciperenderer
 *
 * @author <a href="https://github.com/Bread-Nicecat">Bread_NiceCat</a>
 * <p>
 *
 * <p>
 **/
public class RecipeRenderer {
	public static final String MOD_ID = "reciperenderer";
	public static final String MOD_NAME = "Recipe Renderer";
	public static final Logger LOGGER = LogManager.getLogger(MOD_NAME);
	//	public static final ExportFrame exportFrame;
	public static RPlatform platform;
	public static String modVersion = null;
	/**
	 * modid : modVersion
	 */
	public static HashMap<String, String> loadedMods = new HashMap<>();
	
	static {
		LOGGER.info("开始加载 {}!", MOD_NAME);
//		LOGGER.info("设置Headless=false");
//		System.setProperty("java.awt.headless", "false");
//		exportFrame = new ExportFrame();
		
	}
	
	@Environment(EnvType.CLIENT)
	public static void init(RPlatform rr) {
		RecipeRenderer.platform = rr;
		try {
			platform.listMods().forEach(i -> loadedMods.put(i, platform.getVersion(i)));
			modVersion = loadedMods.get(MOD_ID);
		} catch (Exception e) {
			LOGGER.error("获取Mod实例时出现异常", e);
		}
	}
	
	public static void _onRegisterCMD(CommandDispatcher<CommandSourceStack> dispatcher) {
		var builder = literal("export");
		for (String modid : loadedMods.keySet()) {
			builder.then(literal(modid).executes(c -> {
				try {
					export(modid);
				} catch (Exception e) {
					c.getSource().sendFailure(Component.literal(e.getMessage()));
					LOGGER.error("致命错误", e);
					return -1;
				}
				return 1;
			}));
		}
		dispatcher.register(literal("reciperenderer").then(builder));
		dispatcher.register(literal("rr").then(builder));
	}
	
	private static final LinkedBlockingDeque<Runnable> clientTasks = new LinkedBlockingDeque<>();
	
	@Environment(EnvType.CLIENT)
	public static void _onClientTick() {
		clientTasks.removeIf(i -> {
			i.run();
			return true;
		});
	}
	
	public static void hookRenderer(Runnable run) {
		clientTasks.add(run);
	}
	
	
	public enum Platform {
		FORGE, FABRIC
	}
	
	public static void export(String modid) {
		new Exporter(modid).runAsync();
	}
	
	
}
