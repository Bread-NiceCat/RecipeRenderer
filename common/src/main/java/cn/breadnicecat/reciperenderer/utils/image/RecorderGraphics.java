package cn.breadnicecat.reciperenderer.utils.image;

import com.mojang.blaze3d.platform.NativeImage;
import org.jetbrains.annotations.Nullable;

import java.util.function.Consumer;

/**
 * Created in 2025/1/26 14:22
 * Project: reciperenderer
 *
 * @author <a href="https://github.com/Bread-Nicecat">Bread_NiceCat</a>
 * <p>
 *
 * <p>
 **/
public class RecorderGraphics extends RGuiGraphics {
	protected @Nullable Consumer<NativeImage> modifier;
	
	public RecorderGraphics(int width, int height, @Nullable Consumer<NativeImage> modifier) {
		super(width, height);
		this.modifier = modifier;
	}
	
	public NativeImage downloadRaw() {
		target.bindRead();
		NativeImage image = new NativeImage(target.width, target.height, false);
		image.downloadTexture(0, false);
		if (modifier != null) {
			modifier.accept(image);
		}
		return image;
	}
	
	public byte[] download() {
		try (NativeImage image = downloadRaw()) {
			return image.asByteArray();
		} catch (Exception e) {
			throw new IllegalStateException("处理图像时遇到致命异常" + e, e);
		}
	}
	
}
