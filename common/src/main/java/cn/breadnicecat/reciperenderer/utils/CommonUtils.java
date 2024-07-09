package cn.breadnicecat.reciperenderer.utils;

import org.apache.logging.log4j.util.StackLocatorUtil;
import org.jetbrains.annotations.Contract;

import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * @author <a href="https://gitee.com/Bread_NiceCat">Bread_NiceCat</a>
 * @date 2022/12/30 15:32
 * 非mc特有
 */
public class CommonUtils {
	
	
	public static Class<?> getCaller() {
		return StackLocatorUtil.getCallerClass(3);//因为要再调用getCaller(int)所以要+1
	}
	
	/**
	 * @param depth 从深度1(此方法)开始,2调用这个方法的方法,3调用调用这个方法的方法...
	 */
	public static Class<?> getCaller(int depth) {
		return StackLocatorUtil.getCallerClass(depth);
	}
	
	/**
	 * 依次让所有guest拜访house
	 */
	@SafeVarargs
	public static <I> void accept(Consumer<I> house, I... guests) {
		for (I guest : guests) {
			house.accept(guest);
		}
	}
	
	@Contract()//把->fail顶掉
	public static <T> T impossibleCode() {
		throw new AssertionError("Impossible code invoked. It's a bug, please report it to us");
	}
	
	public static <T> T orElse(T value, T defaultValue) {
		return value == null ? defaultValue : value;
	}
	
	public static <T> T orElse(T value, Supplier<T> defaultValue) {
		return value == null ? defaultValue.get() : value;
	}
}
