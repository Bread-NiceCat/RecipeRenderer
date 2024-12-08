package cn.breadnicecat.reciperenderer.utils;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.minecraft.Util;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.Base64;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.function.Supplier;

import static cn.breadnicecat.reciperenderer.RecipeRenderer.*;

/**
 * Created in 2024/12/1 02:53
 * Project: reciperenderer
 *
 * @author <a href="https://github.com/Bread-Nicecat">Bread_NiceCat</a>
 * <p>
 *
 * <p>
 **/
public class RRUtils {
	
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
	
	public static final Gson GSON = new GsonBuilder().disableHtmlEscaping().create();
	public static final Gson PRETTY = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();
	public static final Base64.Encoder BASE64 = Base64.getEncoder();
	
	
	public static @NotNull ResourceLocation prefix(String path) {
		return ResourceLocation.fromNamespaceAndPath(MOD_ID, path);
	}
	
	/**
	 * 可解析 "*:*", "n:*", "n:p"
	 */
	public static Predicate<ResourceLocation> expression2Predicate(String pred) {
		if (pred.matches(".+?:[*]")) {
			String modid = pred.split(":", 2)[0];
			if (modid.equals("*")) return id -> true;
			return id -> id.getNamespace().equals(modid);
		} else {
			ResourceLocation id = ResourceLocation.parse(pred);
			return id::equals;
		}
	}
	
	public static void sleep(long ms) {
		try {
			Thread.sleep(ms);
		} catch (InterruptedException ignored) {
		}
	}
	
	public static <T> T make(Supplier<T> t) {
		return t.get();
	}
	
	public static <T> T apply(T t, Consumer<T> con) {
		con.accept(t);
		return t;
	}
	
	public static void open(File file) {
		file.mkdirs();
		Util.getPlatform().openFile(file);
	}
	
	public static File createFileDistinct(File root, String fileName) {
		return new File(root, new ExistHelper(ExistHelper.fileBase(root)).getModified(fileName));
	}
	
	/**
	 * @return mc=1.21.1@Fabric:x&rr=3.0.0
	 */
	public static String getMetadata() {
		return "mc=%s@%s:%s&rr=%s".formatted(MC_VERSION, getPlatform().getLoaderName(),
				getPlatform().getLoaderVersion(), getPlatform().getRRVersion());
	}
}
