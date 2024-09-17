package cn.breadnicecat.reciperenderer;

import cn.breadnicecat.reciperenderer.gui.ExportFrame;
import cn.breadnicecat.reciperenderer.gui.screens.WorldlyProgressScreen;
import cn.breadnicecat.reciperenderer.utils.ExportLogger;
import cn.breadnicecat.reciperenderer.utils.RTimer;
import cn.breadnicecat.reciperenderer.utils.TaskChain;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.mojang.brigadier.CommandDispatcher;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.DetectedVersion;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.profiling.ProfilerFiller;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

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
@Environment(EnvType.CLIENT)
public class RecipeRenderer {
	public static final String MOD_ID = "reciperenderer";
	public static final String MOD_NAME = "Recipe Renderer";
	public static final String UPDATE_URL = "https://gitee.com/Bread-NiceCat/RecipeRenderer/raw/master/gradle.properties";
	
	public static final boolean DEV;
	public static boolean skipUpdateCheck;
	
	static {
		//检查是否处于Dev环境
		boolean inDev = false;
		try {
			inDev = new File(new File("").getAbsoluteFile().getParentFile(), "src").exists();
		} catch (Exception ignored) {
		} finally {
			DEV = inDev;
		}
	}
	
	public static final Logger LOGGER = LogManager.getLogger(MOD_NAME);
	public static final ExportLogger PLAYER_LOGGER = new ExportLogger(LOGGER);
	
	public static final Gson GSON = new GsonBuilder().disableHtmlEscaping().create();
	public static final Gson PRETTY = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();
	
	public static final ExecutorService EXECUTOR = Executors.newWorkStealingPool();
	
	private static ExportFrame exportFrame;
	
	public static RPlatform platform;
	
	public static String modVersion = null;
	public static String mcVersion = DetectedVersion.BUILT_IN.getName();
	/**
	 * modid : modVersion
	 */
	public static final Map<String, String> allMods = new HashMap<>();
	
	static boolean outdated = false;
	public static String latestVer;
	
	public static String getVersion(String modid) {
		return allMods.get(modid);
	}
	
	private static ExportFrame launchWindow() {
		RTimer t = new RTimer();
		LOGGER.info("正在启动窗口");
		exportFrame = new ExportFrame();
		LOGGER.info("窗口启动成功,用时{}", t);
		return exportFrame;
	}
	
	public static ExportFrame getWindow() {
		return exportFrame != null ? exportFrame : launchWindow();
	}
	
	public static void init(RPlatform rr) {
		RecipeRenderer.platform = rr;
		LOGGER.info("开始加载 {}!", MOD_NAME);
		
		try {
			platform.listMods().forEach(i -> allMods.put(i, platform.getVersion(i)));
			modVersion = allMods.get(MOD_ID);
		} catch (Exception e) {
			LOGGER.error("获取Mod实例时出现异常", e);
		}
		
		EXECUTOR.submit(() -> {
			LOGGER.info("设置headless=false");
			System.setProperty("java.awt.headless", "false");
			//先初始化
			launchWindow().free();
		});
		
		if (!(skipUpdateCheck | DEV)) {
			EXECUTOR.submit(() -> {
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
		} else {
			LOGGER.info("跳过检查更新");
		}
	}
	
	
	public static void _onRegisterCMD(CommandBuildContext context, CommandDispatcher<CommandSourceStack> dispatcher) {
		RCommand.init(context, dispatcher);
	}
	
	//head -> tail
	private static TaskChain tasks = new TaskChain();
	
	public static void _onFrameUpdate() {
		ProfilerFiller profiler = Minecraft.getInstance().getProfiler();
		profiler.push("rr_onClientTick");
		tasks.run(profiler);
		profiler.pop();
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
	
	public static void worldlyExportFixed(ServerLevel level, int scanCount) {
		ExportFrame window = getWindow();
		if (window.isScreenLocked()) throw new RuntimeException("当前导出屏幕已锁定");
		window.setScreen(new WorldlyProgressScreen(new WorldlyExporter(level, scanCount)));
	}
	
	public enum Platform {
		NEOFORGE("NeoForge"), FABRIC("Fabric");
		
		private final String name;
		
		Platform(String name) {
			this.name = name;
		}
		
		public String getName() {
			return name;
		}
	}
}
