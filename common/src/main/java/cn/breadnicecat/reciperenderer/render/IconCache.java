package cn.breadnicecat.reciperenderer.render;

import cn.breadnicecat.reciperenderer.utils.PoseOffset;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

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
	
	
	public synchronized boolean isPresent(PoseOffset t) {
		return cache.containsKey(t);
	}
	
	@Override
	public synchronized Icon apply(PoseOffset t) {
		return last = cache.computeIfAbsent(t, factory);
	}
	
	public synchronized void clear() {
		cache.clear();
		last = null;
	}
	
	public synchronized boolean isLastPresent() {
		return last != null;
	}
	
	public synchronized @Nullable Icon last() {
		return last;
	}
	
	@SuppressWarnings("BusyWait")
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
}
