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
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.entity.LivingEntity;
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
	private final LivingEntity holder;
	RenderTarget target;
	final int size;
	ItemStack item;
	PoseOffset offset;
	NativeImage image;
	
	public ItemIcon(PoseOffset pose, int size, ItemStack item) {
		this(pose, size, item, null);
	}
	
	public ItemIcon(PoseOffset pose, int size, ItemStack item, LivingEntity holder) {
		ProfilerFiller profiler = instance.getProfiler();
		profiler.push("render_ItemIcon");
		offset = pose;
		this.size = size;
		this.item = item;
		this.holder = holder;
		//开始
		target = new TextureTarget(size, size, true, Minecraft.ON_OSX);
		RenderSystem.backupProjectionMatrix();
		Matrix4f p = new Matrix4f().setOrtho(0, 16, 16, 0, -1000, 1000);
		RenderSystem.setProjectionMatrix(p, VertexSorting.ORTHOGRAPHIC_Z);
		target.bindWrite(true);
		//渲染
		render();
		//收尾
		RenderSystem.restoreProjectionMatrix();
		target.bindRead();
		image = new NativeImage(target.width, target.height, false);
		image.downloadTexture(0, false);
		target.destroyBuffers();
		image.flipY();
		instance.getMainRenderTarget().bindWrite(true);
		profiler.pop();
	}
	
	@Override
	public NativeImage getImage() {
		return image;
	}
	
	protected void render() {
		ItemRenderer renderer = instance.getItemRenderer();
		this.render(item, renderer.getModel(item, holder == null ? null : holder.level(), holder, 0), renderer);
	}
	
	protected void render(ItemStack stack, BakedModel model, ItemRenderer renderer) {
		RenderSystem.setShaderTexture(0, InventoryMenu.BLOCK_ATLAS);
		RenderSystem.enableBlend();
		RenderSystem.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
		RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
		PoseStack poseStack = RenderSystem.getModelViewStack();
		poseStack.pushPose();
		{
			poseStack.setIdentity();
			float mu = 16f / size;
			poseStack.translate(8.0 + offset.x() * mu, 8.0 + offset.y() * mu, (model.isGui3d() ? 150 : 0) + offset.z() * mu);
			poseStack.scale(16.0F * offset.scale(), -16.0F * offset.scale(), 16.0F * offset.scale());
			RenderSystem.applyModelViewMatrix();
			MultiBufferSource.BufferSource immediate = instance.renderBuffers().bufferSource();
			Lighting.setupForFlatItems();
			renderer.render(stack, ItemDisplayContext.GUI, false, new PoseStack(), immediate, 0xF000F0, OverlayTexture.NO_OVERLAY, model);
			immediate.endBatch();
			RenderSystem.enableDepthTest();
			Lighting.setupFor3DItems();
		}
		poseStack.popPose();
	}
	
}