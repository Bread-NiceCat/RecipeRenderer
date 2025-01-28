package cn.breadnicecat.reciperenderer.utils.image;

import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.pipeline.TextureTarget;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.MultiBufferSource;

/**
 * Created in 2025/1/18 00:17
 * Project: reciperenderer
 *
 * @author <a href="https://github.com/Bread-Nicecat">Bread_NiceCat</a>
 * <p>
 * 作为标记
 * <p>
 **/
public abstract class RGuiGraphics extends GuiGraphics implements AutoCloseable {
	protected final int width;
	protected final int height;
	protected final RenderTarget target;
	
	public RGuiGraphics(Minecraft minecraft, MultiBufferSource.BufferSource bufferSource, int width, int height) {
		super(minecraft, bufferSource);
		this.width = width;
		this.height = height;
		target = new TextureTarget(width, height, true, Minecraft.ON_OSX);
		target.bindWrite(true);
	}
	
	
	public RGuiGraphics(int width, int height) {
		this(Minecraft.getInstance(), Minecraft.getInstance().renderBuffers().bufferSource(), width, height);
	}
	
	@Override
	public int guiHeight() {
		return height;
	}
	
	@Override
	public int guiWidth() {
		return width;
	}
	
	@Override
	public void close() {
		target.destroyBuffers();
	}
}
