package cn.breadnicecat.reciperenderer.render;

import cn.breadnicecat.reciperenderer.utils.ExportLogger;
import cn.breadnicecat.reciperenderer.utils.PoseOffset;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;

/**
 * Created in 2024/7/25 上午2:10
 * Project: reciperenderer
 *
 * @author <a href="https://github.com/Bread-Nicecat">Bread_NiceCat</a>
 * <p>
 *
 * <p>
 **/
public class IconWrapper {
	private static final AtomicInteger ids = new AtomicInteger();
	public final String wrapId = "0x" + Integer.toString(ids.incrementAndGet(), 16);
	private IconCache icon;
	private boolean cache;
	
	public IconWrapper(Function<PoseOffset, IIcon> icon) {
		this.icon = IconCache.of(icon);
	}
	
	public void render(PoseOffset pose) {
		icon.apply(pose);
	}
	
	public void render() {
		icon.apply(PoseOffset.NONE);
	}
	
	/**
	 * @return 当渲染出错时返回null
	 */
	public @Nullable IIcon getIconBlocking() {
		IIcon icon1 = icon.getLastBlocking();
		if (!cache) icon.clear();
		return icon1;
	}
	
	public byte[] getBytesBlocking() throws IOException {
		IIcon icon = getIconBlocking();
		return icon == null ? null : icon.getImage().asByteArray();
	}
	
	public byte[] getBytesBlocking(ExportLogger logger) {
		try {
			return getBytesBlocking();
		} catch (IOException e) {
			logger.error("转换图片时出现异常, wrapId=" + wrapId, e);
			return null;
		}
	}
	
	public void disableCache() {
		cache = false;
	}
}
