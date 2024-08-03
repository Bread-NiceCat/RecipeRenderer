package cn.breadnicecat.reciperenderer.utils;

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
public interface Runnable_WithException<E extends Throwable> {
	
	void run() throws E;
}
