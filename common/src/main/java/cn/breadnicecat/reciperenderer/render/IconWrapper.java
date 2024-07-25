package cn.breadnicecat.reciperenderer.render;

import cn.breadnicecat.reciperenderer.utils.PoseOffset;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
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
	private IconCache icon;
	
	public IconWrapper(Function<PoseOffset, Icon> icon) {
		this.icon = IconCache.ofConcurrent(icon);
	}
	
	public void render(PoseOffset pose) {
		icon.apply(pose);
	}
	
	public void render() {
		try {
			icon.apply(PoseOffset.NONE);
		} catch (Exception e) {
			LOGGER.error("渲染错误", e);
		}
	}
	
	public @NotNull Icon getIconBlocking() {
		return icon.getLastBlocking();
	}
	
	public byte[] getBytesBlocking() {
		try {
			Icon icon1 = getIconBlocking();
			return icon1.getImage().asByteArray();
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
	
}
