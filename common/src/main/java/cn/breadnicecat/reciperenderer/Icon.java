package cn.breadnicecat.reciperenderer;

import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.pipeline.TextureTarget;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.platform.Lighting;
import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexSorting;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import org.joml.Matrix4f;

import java.io.IOException;

import static cn.breadnicecat.reciperenderer.Exporter.BASE64;


/**
 * Created in 2024/7/8 下午9:35
 * Project: reciperenderer
 *
 * @author <a href="https://github.com/Bread-Nicecat">Bread_NiceCat</a>
 * <p>
 * 参考 <a href="https://github.com/Nova-Committee/IconR/blob/master/src/main/java/top/gregtao/iconrenderer/utils/ImageHelper.java">IconR</a>,
 * 有修改
 * <p>
 **/
public class Icon {
	private final RenderTarget target;
	private PoseStack pose;
	NativeImage image;
	final boolean flipY;
	
	public Icon(int size, Entity entity) {
		this.target = new TextureTarget(size, size, true, Minecraft.ON_OSX);
		flipY = false;
		this.start();
		this.renderEntity(entity);
		this.end();
		image = get();
	}
	
	public Icon(int size, ItemStack itemStack) {
		target = new TextureTarget(size, size, true, Minecraft.ON_OSX);
		flipY = true;
		start();
		render(itemStack, Minecraft.getInstance().getItemRenderer());
		end();
		image = get();
	}
	
	private NativeImage get() {
		NativeImage img = new NativeImage(target.width, target.height, false);
		RenderSystem.bindTexture(target.getColorTextureId());
		img.downloadTexture(0, false);
		if (flipY) img.flipY();
		return img;
	}
	
	public NativeImage getImage() {
		return image;
	}
	
	public String getBase64() {
		try {
			return BASE64.encodeToString(image.asByteArray());
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
	
	protected void start() {
		this.pose = RenderSystem.getModelViewStack();
		this.pose.pushPose();
		this.pose.setIdentity();
		
		RenderSystem.backupProjectionMatrix();
		Matrix4f p = new Matrix4f().setOrtho(0, 16, 16, 0, -150, 150);
		RenderSystem.setProjectionMatrix(p, VertexSorting.ORTHOGRAPHIC_Z);
		
		this.target.bindWrite(true);
		this.target.bindRead();
	}
	
	protected void end() {
		RenderSystem.restoreProjectionMatrix();
		this.pose.popPose();
		
		this.target.unbindWrite();
		this.target.unbindRead();
	}
	
	protected void render(ItemStack stack, ItemRenderer renderer) {
		this.render(stack, renderer.getModel(stack, null, null, 0), renderer);
	}
	
	protected void render(ItemStack stack, BakedModel model, ItemRenderer renderer) {
		RenderSystem.setShaderTexture(0, InventoryMenu.BLOCK_ATLAS);
		RenderSystem.enableBlend();
		RenderSystem.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
		RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
		PoseStack matrixStack = RenderSystem.getModelViewStack();
		matrixStack.pushPose();
		{
			matrixStack.translate(0, 0, 100.0F);
			matrixStack.translate(8.0D, 8.0D, 0.0D);
			matrixStack.scale(1.0F, -1.0F, 1.0F);
			matrixStack.scale(16.0F, 16.0F, 16.0F);
			RenderSystem.applyModelViewMatrix();
			PoseStack matrixStack2 = new PoseStack();
			MultiBufferSource.BufferSource immediate = Minecraft.getInstance().renderBuffers().bufferSource();
			boolean bl = !model.usesBlockLight();
			if (bl) {
				Lighting.setupForFlatItems();
			}
			renderer.render(stack, ItemDisplayContext.GUI, false, matrixStack2, immediate, 15728880, OverlayTexture.NO_OVERLAY, model);
			immediate.endBatch();
			RenderSystem.enableDepthTest();
			if (bl) {
				Lighting.setupFor3DItems();
			}
		}
		matrixStack.popPose();
		RenderSystem.applyModelViewMatrix();
	}
	
	public void renderEntity(Entity spawnEntity) {
		Minecraft client = Minecraft.getInstance();
		MultiBufferSource.BufferSource immediate = client.renderBuffers().bufferSource();
		
		this.pose = RenderSystem.getModelViewStack();
		this.pose.pushPose();
		{
			this.pose.setIdentity();
			this.pose.mulPose(Axis.XP.rotationDegrees(112.5f));
			this.pose.scale(2.5f, -2.5f, -2.5f);
			this.pose.translate(0.75f, 1f, 1f);
			this.pose.mulPose(Axis.ZP.rotationDegrees(45f));
			this.pose.translate(-0.75f, 0, 0);
			this.pose.mulPose(Axis.YP.rotationDegrees(22.5f));
			this.pose.mulPose(Axis.ZN.rotationDegrees(22.5f));
			this.pose.translate(0.75f, 0, 0);
			if (!(client.player == null)) {
				spawnEntity.setPosRaw(client.player.getX(), client.player.getY(), client.player.getZ());
			}
			client.getEntityRenderDispatcher().render(spawnEntity, 0, 0, 0, 0, client.getFrameTime(), this.pose, immediate, 15728880);
		}
		pose.popPose();
		
	}
	
}