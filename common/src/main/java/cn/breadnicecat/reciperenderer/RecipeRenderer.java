package cn.breadnicecat.reciperenderer;

import cn.breadnicecat.reciperenderer.gui.ExportFrame;
import cn.breadnicecat.reciperenderer.utils.TaskChain;
import cn.breadnicecat.reciperenderer.utils.Timer;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.mojang.brigadier.CommandDispatcher;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.DetectedVersion;
import net.minecraft.Util;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.net.URL;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

import static cn.breadnicecat.reciperenderer.utils.VersionControl.getLatestVersion;

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
	public static final String UPDATE_URL = "https://gitee.com/Bread-NiceCat/RecipeRenderer/raw/master/gradle.properties";
	public static final Logger LOGGER = LogManager.getLogger(MOD_NAME);
	public static final Gson GSON = new GsonBuilder().disableHtmlEscaping().create();
	public static final Base64.Encoder BASE64 = Base64.getEncoder();
	
	public static ExportFrame exportFrame;
	public static RPlatform platform;
	public static String modVersion = null;
	public static String mcVersion = DetectedVersion.BUILT_IN.getName();
	static boolean outdated = false;
	public static String latestVer;
	/**
	 * modid : modVersion
	 */
	public static final Map<String, String> allMods = new HashMap<>();
	
	static {
		LOGGER.info("开始加载 {}!", MOD_NAME);
		LOGGER.info("设置headless=false");
		System.setProperty("java.awt.headless", "false");
		Exporter.executor.submit(() -> {
			Timer t = new Timer();
			LOGGER.info("正在启动窗口");
			exportFrame = new ExportFrame();
			LOGGER.info("窗口启动成功,用时{}", t);
		});
	}
	
	public static String getVersion(String modid) {
		return allMods.get(modid);
	}
	
	@Environment(EnvType.CLIENT)
	public static void init(RPlatform rr) {
		RecipeRenderer.platform = rr;
		try {
			platform.listMods().forEach(i -> allMods.put(i, platform.getVersion(i)));
			modVersion = allMods.get(MOD_ID);
		} catch (Exception e) {
			LOGGER.error("获取Mod实例时出现异常", e);
		}
		Util.backgroundExecutor().submit(() -> {
			try {
				latestVer = getLatestVersion(new URL(UPDATE_URL));
				LOGGER.info("当前版本: {}", modVersion);
				LOGGER.info("获取到最新版本: {}", latestVer);
				if (!modVersion.equals(latestVer)) {
					RecipeRenderer.outdated = true;
				}
			} catch (Exception e) {
				LOGGER.warn("无法检查更新", e);
			}
		});
	}
	
	
	public static void _onRegisterCMD(CommandBuildContext context, CommandDispatcher<CommandSourceStack> dispatcher) {
		RCommand.init(context, dispatcher);
	}
	
	//head -> tail
	private static TaskChain tasks = new TaskChain();
	
	@Environment(EnvType.CLIENT)
	public static void _onClientTick() {
		tasks.run();
	}
	
	public static void hookRenderer(Runnable run) {
		tasks.add(run);
	}
	
	public static void export(String modid) {
		new Exporter(modid).runAsync();
	}
	
	public static int open(File file) {
		try {
			file.mkdirs();
			Util.getPlatform().openFile(file);
			return 1;
		} catch (Exception e) {
			throw new RuntimeException(e.getMessage(), e);
		}
	}
	
	public enum Platform {
		FORGE, FABRIC
	}
}
