package cn.breadnicecat.reciperenderer;

import cn.breadnicecat.reciperenderer.utils.CommonUtils;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.DetectedVersion;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.Base64;
import java.util.Objects;

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
	public static final boolean DEV;
	
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
	
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_NAME);
	public static final File exportDir = new File(Minecraft.getInstance().gameDirectory, "rr_export");
	
	public static final Gson GSON = new GsonBuilder().disableHtmlEscaping().create();
	public static final Gson PRETTY = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();
	public static final Base64.Encoder BASE64 = Base64.getEncoder();
	
	public static RPlatform platform;
	
	public static final String MC_VERSION = DetectedVersion.BUILT_IN.getName();
	private static String modVersion = null;
	private static String jeiVersion = null;
	
	public static String getModVersion() {
		return Objects.requireNonNull(modVersion, "not initialized!");
	}
	
	public static void init(@NotNull RPlatform platform) {
		LOGGER.info("init");
		RecipeRenderer.platform = platform;
		if ((jeiVersion = platform.getVersion("jei")) != null) {
			new JEIPlugin();
		}
		modVersion = platform.getVersion(MOD_ID);
	}
	
	public static void open() {
		CommonUtils.open(exportDir);
	}
	
	public static void export(String predicate) {
		new RecipeExporter(predicate).run();
	}
	
	public enum Platform {
		NEOFORGE, FABRIC;
		
		public String getName() {
			return name().toLowerCase();
		}
		
	}
	
	public static @NotNull ResourceLocation prefix(String path) {
		return ResourceLocation.fromNamespaceAndPath(MOD_ID, path);
	}
	
	public static boolean isJeiEnabled() {
		return jeiVersion != null;
	}
}
