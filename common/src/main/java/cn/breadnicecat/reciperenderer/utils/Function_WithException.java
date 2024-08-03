package cn.breadnicecat.reciperenderer.utils;

import java.util.function.Consumer;

/**
 * Created in 2024/7/10 下午3:17
 * Project: reciperenderer
 *
 * @author <a href="https://github.com/Bread-Nicecat">Bread_NiceCat</a>
 * <p>
 *
 * <p>
 **/
@FunctionalInterface
public interface Function_WithException<T, R> {
	
	R apply(T t) throws Throwable;
	
	default R apply(T t, Consumer<Throwable> e) {
		try {
			return apply(t);
		} catch (Throwable ex) {
			e.accept(ex);
			return null;
		}
	}
}
