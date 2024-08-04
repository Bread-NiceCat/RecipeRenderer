package cn.breadnicecat.reciperenderer.utils;

import cn.breadnicecat.reciperenderer.RecipeRenderer;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;
import java.util.Properties;

import static cn.breadnicecat.reciperenderer.RecipeRenderer.mcVersion;

/**
 * Created in 2024/8/4 下午9:05
 * Project: reciperenderer
 *
 * @author <a href="https://github.com/Bread-Nicecat">Bread_NiceCat</a>
 * <p>
 *
 * <p>
 **/
public class VersionControl {
	
	@NotNull
	public static String getLatestVersion(URL url) throws IOException {
		RecipeRenderer.LOGGER.info("开始获取版本:{}", url);
		URLConnection connection = url.openConnection();
		connection.setConnectTimeout(5000);
		Properties properties = new Properties();
		properties.load(connection.getInputStream());
		
		String defaultVer = properties.getProperty("mod_version");
		
		var ver = properties.getProperty("mod_version_" + mcVersion);
		if (ver == null) {
			String sub1 = mcVersion.substring(0, mcVersion.lastIndexOf('.'));
			ver = properties.getProperty("mod_version_" + sub1);
			if (ver == null) {
				String sub2 = sub1.substring(0, sub1.lastIndexOf('.'));
				ver = properties.getProperty("mod_version_" + sub2);
			}
		}
		while (ver != null && ver.startsWith("$")) {
			ver = properties.getProperty(ver.substring(1));
		}
		if (ver == null) {
			return defaultVer;
		} else {
			return ver;
		}
	}
	
	@Deprecated(forRemoval = true)
	public static void main(String[] args) throws IOException {
		System.out.println(getLatestVersion(new File("gradle.properties").toURI().toURL()));
	}
}
