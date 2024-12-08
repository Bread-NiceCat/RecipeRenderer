package cn.breadnicecat.reciperenderer;

import cn.breadnicecat.reciperenderer.exporter.VanillaRecipeExporter;
import cn.breadnicecat.reciperenderer.exporter.jei.JEIExporter;
import cn.breadnicecat.reciperenderer.exporter.jei.JEIPlugin;
import cn.breadnicecat.reciperenderer.platform.RPlatform;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.DetectedVersion;
import net.minecraft.client.Minecraft;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Stream;

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
	
	private static final Logger logger = LoggerFactory.getLogger(RecipeRenderer.class);
	
	public static final File exportDir = new File(Minecraft.getInstance().gameDirectory, "rr_export");
	public static final String MC_VERSION = DetectedVersion.BUILT_IN.getName();
	private static RPlatform platform;
	private static TreeMap<String, RRExtension> extensions = new TreeMap<>();
	
	public static void init(@NotNull RPlatform platform) {
		if (RecipeRenderer.platform != null) {
			throw new IllegalStateException("initialized");
		}
		logger.info("初始化...");
		RecipeRenderer.platform = platform;
		logger.info("当前版本:{},mc版本:{},mod加载器:{}@{}", platform.getRRVersion(), MC_VERSION, platform.getLoaderName(), platform.getLoaderVersion());
		registerExtension(VanillaRecipeExporter.ID, new VanillaRecipeExporter());
		if (platform.isLoaded("jei")) {
			new JEIPlugin();//在forge环境下注册jei插件
			registerExtension("jei", new JEIExporter());
		}
		logger.info("初始化完成!");
	}
	
	public static RPlatform getPlatform() {
		return platform;
	}
	
	/**
	 * 注册插件.允许在任意时刻进行注册(线程不安全),不允许重复注册.
	 */
	public static void registerExtension(String id, RRExtension extension) {
		RRExtension dup = extensions.put(id, extension);
		if (dup != null) {
			throw new IllegalArgumentException("重复注册插件:%s (class1=%s,class2=%s)".formatted(id, extension.getClass().descriptorString(), dup.getClass().descriptorString()));
		}
		logger.info("成功注册插件:{} (class={})", id, extension.getClass().descriptorString());
	}
	
	public static @NotNull Stream<Map.Entry<String, RRExtension>> getAllExtensions() {
		return extensions.entrySet().stream();
	}
	
}
