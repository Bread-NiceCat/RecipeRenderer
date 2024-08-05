package cn.breadnicecat.reciperenderer.render;

import cn.breadnicecat.reciperenderer.utils.PoseOffset;
import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.pipeline.TextureTarget;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.platform.Lighting;
import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexSorting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import org.joml.Matrix4f;


/**
 * Created in 2024/7/8 下午9:35
 * Project: reciperenderer
 *
 * @author <a href="https://github.com/Bread-Nicecat">Bread_NiceCat</a>
 * <p>
 * 参考 <a href="https://github.com/Nova-Committee/IconR/blob/master/src/main/java/top/gregtao/iconrenderer/utils/ImageHelper.java">IconR</a>,
 * <p>
 * 参考{@link net.minecraft.client.gui.GuiGraphics#renderItem(net.minecraft.world.entity.LivingEntity, net.minecraft.world.level.Level, net.minecraft.world.item.ItemStack, int, int, int, int)}
 * <p>
 **/
public class ItemIcon implements IIcon {
	private final Minecraft instance = Minecraft.getInstance();
	RenderTarget target;
	ItemStack item;
	PoseOffset offset;
	NativeImage image;
	
	public ItemIcon(PoseOffset pose, int size, ItemStack item) {
		offset = pose;
		this.item = item;
		//开始
		target = new TextureTarget(size, size, true, Minecraft.ON_OSX);
		RenderSystem.backupProjectionMatrix();
		Matrix4f p = new Matrix4f().setOrtho(0, 16, 16, 0, -150, 150);
		RenderSystem.setProjectionMatrix(p, VertexSorting.ORTHOGRAPHIC_Z);
		target.bindWrite(true);
		//渲染
		render();
		//收尾
		RenderSystem.restoreProjectionMatrix();
		image = new NativeImage(target.width, target.height, false);
		RenderSystem.bindTexture(target.getColorTextureId());
		image.downloadTexture(0, false);
		target.destroyBuffers();
		image.flipY();
	}
	
	@Override
	public NativeImage getImage() {
		return image;
	}
	
	protected void render() {
		ItemRenderer client = instance.getItemRenderer();
		this.render(item, client.getModel(item, null, null, 0), client);
	}
	
	protected void render(ItemStack stack, BakedModel model, ItemRenderer renderer) {
		RenderSystem.setShaderTexture(0, InventoryMenu.BLOCK_ATLAS);
		RenderSystem.enableBlend();
		RenderSystem.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
		RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
		PoseStack modelView = RenderSystem.getModelViewStack();
		modelView.pushPose();
		{
			modelView.setIdentity();
//			offset.apply(modelView);
			modelView.scale(offset.scale(), offset.scale(), offset.scale());
			
			modelView.translate(0, 0, 50.0F);
			modelView.translate(8.0D, 8.0D, 0.0D);
			modelView.scale(1.0F, -1.0F, 1.0F);
			modelView.scale(16.0F, 16.0F, 16.0F);
			
			RenderSystem.applyModelViewMatrix();
			MultiBufferSource.BufferSource immediate = instance.renderBuffers().bufferSource();
			boolean bl = !model.usesBlockLight();
			if (bl) {
				Lighting.setupForFlatItems();
			}
			renderer.render(stack, ItemDisplayContext.GUI, false, new PoseStack(), immediate, 15728880, OverlayTexture.NO_OVERLAY, model);
			immediate.endBatch();
			RenderSystem.enableDepthTest();
			if (bl) {
				Lighting.setupFor3DItems();
			}
		}
		modelView.popPose();
		RenderSystem.applyModelViewMatrix();
	}
	
}