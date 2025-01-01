package cn.breadnicecat.reciperenderer.utils.DEBUG;

import com.ibm.icu.impl.Pair;
import groovy.lang.Binding;
import groovy.util.GroovyScriptEngine;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

import static cn.breadnicecat.reciperenderer.utils.RRUtils.DEV;

/**
 * Created in 2023/7/29 15:23
 * Project: candycraftce
 *
 * @author <a href="https://github.com/Bread-Nicecat">Bread_NiceCat</a>
 * <p>
 * ANY memeber in this class SHOULD NOT be used when running release!
 * It's for DEBUG only
 * <p>
 * <p>
 * Warning: an exception will be thrown when invoke any memeber of this class
 * if you're not in a development environment
 */
public class DEBUGS {
	
	public static final File PROJECT_ROOT = new File("").getAbsoluteFile().getParentFile().getParentFile();
	public static final File COMMON_ROOT = new File(PROJECT_ROOT, "common");
	public static final File COMMON_SRC_DIR = new File(COMMON_ROOT, "src");
	/**
	 * 给idea的调试器eval存储数据的地方
	 */
	public static final HashMap<?, ?> EVAL = new HashMap<>();
	private static final Logger logger = LoggerFactory.getLogger(DEBUGS.class);
	
	static {
		logger.warn("=".repeat(40));
		logger.warn("DEBUGS ON!");
		logger.warn("=".repeat(40));
		if (!DEV) throw new IllegalStateException("Not in DEV");
	}
	
	public static <T> Consumer<T> generateConsumer(List<T> con) {
		return con::add;
	}
	
	public static List<?> extractPair(List<Pair> con, int or) {
		return con.stream().map(i -> or < 0 ? i.second : i.first).toList();
	}
	
	
	public static @Nullable Object runGroovyScript(File path, Map<String, Object> args) throws Exception {
		GroovyScriptEngine engine = new GroovyScriptEngine(path.getAbsoluteFile().getParent(), Thread.currentThread().getContextClassLoader());
		return engine.run(path.getName(), new Binding(args));
	}
}
