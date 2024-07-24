package cn.breadnicecat.reciperenderer;

import cn.breadnicecat.reciperenderer.cmd.ICmdFeedback;
import cn.breadnicecat.reciperenderer.datafix.DataStorer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

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
	public static String modVersion = "undefined";
	public static final Logger LOGGER = LogManager.getLogger(MOD_NAME);
	public static RPlatform RR;
	
	static {
		LOGGER.info("Loading {} !", MOD_NAME);
		LOGGER.info("设置Headless=false");
		try {
			System.setProperty("java.awt.headless", "false");
		} catch (Exception e) {
			LOGGER.error("取消Headless失败", e);
		}
	}
	
	@Environment(EnvType.CLIENT)
	public static void init(RPlatform rr) {
		RecipeRenderer.RR = rr;
		try {
			modVersion = RR.getVersion(MOD_ID);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	
	public enum Platform {
		FORGE, FABRIC
	}
	
	
	public static int exportAll(ICmdFeedback callback, DataStorer storer) {
		return RR.listMods().map(i -> export(i, callback, storer) == 1).reduce(true, (a, b) -> a & b)
				? 1 : -1;
	}
	
	public static int export(String modid, ICmdFeedback callback, DataStorer storer) {
		try {
			new Exporter(modid).run(callback, storer);
		} catch (Exception e) {
			return -1;
		}
		return 1;
	}
	
	
}
