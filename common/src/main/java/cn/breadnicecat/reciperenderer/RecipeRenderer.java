package cn.breadnicecat.reciperenderer;

import cn.breadnicecat.reciperenderer.api.IExporter;
import cn.breadnicecat.reciperenderer.exporter.jei.JEIExporter;
import cn.breadnicecat.reciperenderer.exporter.recipe.SimpleRecipeExporter;
import cn.breadnicecat.reciperenderer.internal.InternalImpl;
import cn.breadnicecat.reciperenderer.utils.RRUtils;
import cn.breadnicecat.reciperenderer.utils.platform.InvHooks;
import cn.breadnicecat.reciperenderer.utils.platform.RPlatform;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.DetectedVersion;
import net.minecraft.client.Minecraft;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.Comparator;
import java.util.TreeSet;
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
	private static final TreeSet<IExporter> exporters = new TreeSet<>(Comparator.comparing(IExporter::getExporterName));
	
	
	public static RPlatform getPlatform() {
		return platform;
	}
	
	
	public static void registerExporter(IExporter exporter) {
		exporters.add(exporter);
	}
	
	public static Stream<IExporter> getAllExporters() {
		return exporters.stream();
	}
	//==================================================//
	
	/**
	 * 其他mod不应该调用这个方法
	 */
	public static void init(@NotNull RPlatform platform) {
		if (RecipeRenderer.platform != null) {
			throw new IllegalStateException("重复初始化...");
		}
		if (!platform.isClient()) {
			throw new IllegalStateException("尝试在服务器上加载" + MOD_ID + "/Try loading " + MOD_ID + " on the server.");
		}
		
		logger.info("开始初始化...");
		RecipeRenderer.platform = platform;
		logger.info("当前版本:{},mc版本:{},mod加载器:{}@{}", platform.getRRVersion(), MC_VERSION, platform.getLoaderName(), platform.getLoaderVersion());
		logger.info("加载内置实现...");
		new InternalImpl();
		registerExporter(new SimpleRecipeExporter());
		if (platform.isLoaded("jei")) {
			registerExporter(new JEIExporter());
		}
//		registerExporter(new WorldExporter());
		
		InvHooks.hookClientTick(() -> {
			logger.info("正在切换到简体中文");
			RRUtils.setChinese();
		});
		logger.info("初始化完成!");
	}
}
