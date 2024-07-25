package cn.breadnicecat.reciperenderer.render;

import cn.breadnicecat.reciperenderer.utils.PoseOffset;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

/**
 * Created in 2024/7/25 上午2:12
 * Project: reciperenderer
 *
 * @author <a href="https://github.com/Bread-Nicecat">Bread_NiceCat</a>
 * <p>
 * 缓存
 * <p>
 **/
public class IconCache implements Function<PoseOffset, Icon> {
	private Map<PoseOffset, Icon> cache = new HashMap<>();
	private Icon last;
	private final Function<PoseOffset, Icon> factory;
	
	private IconCache(Function<PoseOffset, Icon> factory) {
		this.factory = factory;
	}
	
	public static IconCache of(Function<PoseOffset, Icon> sup) {
		return new IconCache(sup);
	}
	
	public static IconCache ofConcurrent(Function<PoseOffset, Icon> sup) {
		return new Concurrent(sup);
	}
	
	public boolean isPresent(PoseOffset t) {
		return cache.containsKey(t);
	}
	
	@Override
	public Icon apply(PoseOffset t) {
		return last = cache.computeIfAbsent(t, factory);
	}
	
	public void clear() {
		cache.clear();
		clearLast();
	}
	
	public boolean isLastPresent() {
		return last != null;
	}
	
	public @Nullable Icon last() {
		return last;
	}
	
	public @NotNull Icon getLastBlocking() {
		while (!isLastPresent()) {
			try {
				Thread.sleep(50);
			} catch (InterruptedException e) {
				throw new RuntimeException(e);
			}
		}
		return last;
	}
	
	public void clearLast() {
		last = null;
	}
	
	static class Concurrent extends IconCache {
		public Concurrent(Function<PoseOffset, Icon> sup) {
			super(sup);
			super.cache = Collections.synchronizedMap(super.cache);
		}
	}
}
