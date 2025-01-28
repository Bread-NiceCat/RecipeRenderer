package cn.breadnicecat.reciperenderer.utils.image;

import cn.breadnicecat.reciperenderer.mixin.MixinGuiGraphics;
import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.util.LinkedList;
import java.util.function.Consumer;

/**
 * Created in 2025/1/18 00:14
 * Project: reciperenderer
 *
 * @author <a href="https://github.com/Bread-Nicecat">Bread_NiceCat</a>
 * <p>
 *
 * <p>
 **/
public class LayeredRecorderGraphics extends RecorderGraphics {
	final LinkedList<byte[]> images = new LinkedList<>();
	
	public LayeredRecorderGraphics(int width, int height, @Nullable Consumer<NativeImage> modifier) {
		super(width, height, modifier);
		((MixinGuiGraphics) this).setPose(createPose());
	}
	
	void clear() {
		target.setClearColor(1, 1, 1, 0);
		target.clear(Minecraft.ON_OSX);
		target.bindWrite(true);
	}
	
	void update() {
		try (NativeImage image = downloadRaw()) {
			if (checkOpaque(image)) {
				try {
					images.add(image.asByteArray());
				} catch (IOException e) {
					throw new RuntimeException(e);
				}
			}
		}
		clear();
	}
	
	protected boolean checkOpaque(NativeImage image) {
		for (int x = 0; x < image.getWidth(); x++) {
			for (int y = 0; y < image.getHeight(); y++) {
				if (image.getLuminanceOrAlpha(x, y) != 0) {
					return true;
				}
			}
		}
		return false;
	}
	
	public byte[][] getLayered() {
		return images.toArray(byte[][]::new);
	}
	
	@Override
	public void drawCenteredString(Font font, String text, int x, int y, int color) {
		super.drawCenteredString(font, text, x, y, color);
	}
	
	@Override
	public void flush() {
		super.flush();
		update();
	}
	
	
	PoseStack createPose() {
		return new PoseStack() {
			@Override
			public void pushPose() {
				super.pushPose();
				update();
			}
			
			@Override
			public void popPose() {
				super.popPose();
				update();
			}
		};
	}
}
