package cn.breadnicecat.reciperenderer.render;

import cn.breadnicecat.reciperenderer.utils.Function_WithException;
import cn.breadnicecat.reciperenderer.utils.PoseOffset;
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
public class IconCache implements Function<PoseOffset, IIcon> {
	private Map<PoseOffset, IIcon> cache = new HashMap<>();
	private IIcon last;
	private final Function_WithException<PoseOffset, IIcon> factory;
	private boolean valid = true;
	
	private IconCache(Function<PoseOffset, IIcon> factory) {
		this.factory = factory::apply;
	}
	
	public static IconCache of(Function<PoseOffset, IIcon> sup) {
		return new IconCache(sup);
	}
	
	
	public synchronized boolean isPresent(PoseOffset t) {
		return valid && cache.containsKey(t);
	}
	
	@Override
	public synchronized IIcon apply(PoseOffset t) {
		return valid ? last = cache.computeIfAbsent(t, (of) -> factory.apply(of, (e) -> invalidate())) : null;
	}
	
	public synchronized void clear() {
		cache.clear();
		last = null;
	}
	
	public synchronized boolean isLastPresent() {
		return valid && last != null;
	}
	
	public synchronized @Nullable IIcon last() {
		return valid ? last : null;
	}
	
	public synchronized void invalidate() {
		valid = false;
		clear();
	}
	
	public @Nullable IIcon getLastBlocking() {
		while (valid && !isLastPresent()) {
			Thread.yield();
		}
		return last();
	}
	
}
