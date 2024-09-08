package cn.breadnicecat.reciperenderer.render;

import cn.breadnicecat.reciperenderer.utils.ExportLogger;
import cn.breadnicecat.reciperenderer.utils.PoseOffset;
import com.mojang.serialization.DataResult;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;

import static cn.breadnicecat.reciperenderer.RecipeRenderer.LOGGER;

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
	private Function<PoseOffset, IIcon> factory;
	private volatile DataResult<IIcon> last;
	
	public IconWrapper(Function<PoseOffset, IIcon> factory) {
		this.factory = factory;
	}
	
	@Environment(EnvType.CLIENT)
	public DataResult<IIcon> render(PoseOffset pose) {
		try {
			IIcon apply = factory.apply(pose);
			if (apply != null) {
				last = DataResult.success(apply);
			} else {
				last = DataResult.error(() -> "未知渲染错误,wrapId" + wrapId);
			}
		} catch (Exception e) {
			last = DataResult.error(() -> e + ",wrapId=" + wrapId);
		}
		return last;
	}
	
	@Environment(EnvType.CLIENT)
	public DataResult<IIcon> render() {
		return render(PoseOffset.NONE);
	}
	
	public void clear() {
		if (last != null) {
			last.result().ifPresent(IIcon::close);
			last = null;
		}
	}
	
	/**
	 * 一定要调用render，不然会线程死锁
	 *
	 * @return 当渲染出错时返回null
	 */
	public @Nullable IIcon getBlocking() {
		while (last == null) {
//			Thread.yield();
			try {
				Thread.sleep(100);
				LOGGER.warn("等待客户端响应...");
			} catch (InterruptedException ignored) {
			}
		}
		return last.getOrThrow();
	}
	
	public byte[] getBytesBlocking() throws IOException {
		try (IIcon icon = getBlocking()) {
			if (icon == null) return null;
			return icon.getImage().asByteArray();
		}
	}
	
	public byte[] getBytesBlocking(ExportLogger logger) {
		try {
			return getBytesBlocking();
		} catch (IOException e) {
			logger.error("转换图片时出现异常, wrapId=" + wrapId, e);
			return null;
		}
	}
	
}
