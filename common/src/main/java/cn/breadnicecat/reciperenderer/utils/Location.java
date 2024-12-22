package cn.breadnicecat.reciperenderer.utils;

import org.jetbrains.annotations.Nullable;

/**
 * Created in 2024/12/22 04:38
 * Project: reciperenderer
 *
 * @author <a href="https://github.com/Bread-Nicecat">Bread_NiceCat</a>
 * <p>
 *
 * <p>
 **/
public record Location<T>(LocationType type, @Nullable T arg) {
	public static <T> Location<T> first() {
		return new Location<>(LocationType.FIRST, null);
	}
	
	public static <T> Location<T> after(T arg) {
		return new Location<>(LocationType.AFTER, arg);
	}
	
	public static <T> Location<T> before(T arg) {
		return new Location<>(LocationType.BEFORE, arg);
	}
	
	public static <T> Location<T> last() {
		return new Location<>(LocationType.LAST, null);
	}
	
	public enum LocationType {
		FIRST, BEFORE, AFTER, LAST;
	}
}
