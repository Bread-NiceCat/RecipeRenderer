package cn.breadnicecat.reciperenderer.utils;

import net.minecraft.Util;
import org.jetbrains.annotations.Contract;
import org.slf4j.Logger;
import org.slf4j.event.Level;

import java.io.File;
import java.util.function.Supplier;

/**
 * @author <a href="https://gitee.com/Bread_NiceCat">Bread_NiceCat</a>
 * @date 2022/12/30 15:32
 */
public class CommonUtils {
	
	public static void sleep(long ms) {
		try {
			Thread.sleep(ms);
		} catch (InterruptedException ignored) {
		}
	}
	
	public static <T> T make(Supplier<T> t) {
		return t.get();
	}
	
	public static void open(File file) {
		file.mkdirs();
		Util.getPlatform().openFile(file);
	}
	
	public static void log(Logger logger, Level level, String msg) {
		switch (level) {
			case ERROR -> logger.error(msg);
			case WARN -> logger.warn(msg);
			case INFO -> logger.info(msg);
			case DEBUG -> logger.debug(msg);
			case TRACE -> logger.trace(msg);
		}
	}
	
	/**
	 * 从注册名中获取名称
	 *
	 * @return 将id中的下划线替换为空格，并且每个空格后第一个字母大写
	 * <p>
	 * 例如{@code 输入this_is_a_example 返回 This Is A Example}
	 */
	
	public static String byId(String id) {
		StringBuilder sb = new StringBuilder();
		String[] s = id.split("_");
		for (String s1 : s) {
			sb.append(s1.substring(0, 1).toUpperCase()).append(s1.substring(1)).append(" ");
		}
		return sb.substring(0, sb.length() - 1);
	}
	
	@Contract()//把->fail顶掉
	public static <T> T impossibleCode() {
		throw new AssertionError("Impossible code invoked. It's a bug, please report it to us");
	}
	
}
