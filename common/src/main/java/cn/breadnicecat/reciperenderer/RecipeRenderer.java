package cn.breadnicecat.reciperenderer;

import cn.breadnicecat.reciperenderer.cmd.ICmdFeedback;
import cn.breadnicecat.reciperenderer.utils.LogUtils;
import dev.architectury.injectables.annotations.ExpectPlatform;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import org.slf4j.Logger;

import java.util.stream.Stream;

import static cn.breadnicecat.reciperenderer.utils.CommonUtils.impossibleCode;

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
	public static final String MOD_VERSION = getVersion(MOD_ID);
	private static final Logger LOGGER = LogUtils.sign();
	
	static {
		LOGGER.info("Loading {} !", MOD_NAME);
		LOGGER.info("设置Headless=false");
		try {
			System.setProperty("java.awt.headless", "false");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	@Environment(EnvType.CLIENT)
	public static void init() {
	}
	
	@ExpectPlatform
	public static Stream<String> listMods() {
		return impossibleCode();
	}
	
	@ExpectPlatform
	public static boolean isLoaded(String modid) {
		return impossibleCode();
	}
	
	@ExpectPlatform
	public static String getVersion(String modid) {
		return impossibleCode();
	}
	
	@ExpectPlatform
	public static Platform getPlatform() {
		return impossibleCode();
	}
	
	
	public enum Platform {
		FORGE, FABRIC
	}
	
	
	public static int export(String modid, ICmdFeedback callback) {
		new Exporter().run(modid, callback);
		return 1;
	}
	
	
}
