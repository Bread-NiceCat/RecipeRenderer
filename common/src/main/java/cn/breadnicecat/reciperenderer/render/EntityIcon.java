package cn.breadnicecat.reciperenderer.render;

import cn.breadnicecat.reciperenderer.utils.PoseOffset;
import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.pipeline.TextureTarget;
import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.AABB;
import org.jetbrains.annotations.NotNull;

import static com.mojang.math.Axis.*;

/**
 * Created in 2024/7/27 下午12:03
 * Project: reciperenderer
 *
 * @author <a href="https://github.com/Bread-Nicecat">Bread_NiceCat</a>
 * <p>
 *
 * <p>
 **/
public class EntityIcon implements IIcon {
	private final Minecraft instance = Minecraft.getInstance();
	NativeImage image;
	
	public EntityIcon(PoseOffset offset, int size, LivingEntity entity) {
		//开始
//		RenderTarget target = new TextureTarget(size, size, true, Minecraft.ON_OSX);
//		target.bindWrite(true);
		entity.setPos(instance.player.position());
		entity.noCulling = true;
//      ========================================
		//Iconr的思路
//      RenderSystem.backupProjectionMatrix();
//		Matrix4f p = new Matrix4f().setOrtho(0, 16, 16, 0, -150, 150);
//		RenderSystem.setProjectionMatrix(p, VertexSorting.ORTHOGRAPHIC_Z);
//
//		PoseStack stack = RenderSystem.getModelViewStack();
//		//渲染
//		stack.pushPose();
//		{
//			stack.setIdentity();
//			offset.apply(stack);
//			stack.mulPose(Axis.XP.rotationDegrees(112.5f));
//			stack.scale(2.5f, -2.5f, -2.5f);
//			stack.translate(0.75f, 1f, 1f);
//			stack.mulPose(Axis.ZP.rotationDegrees(45f));
//			stack.translate(-0.75f, 0, 0);
//			stack.mulPose(Axis.YP.rotationDegrees(22.5f));
//			stack.mulPose(Axis.ZN.rotationDegrees(22.5f));
//			stack.translate(0.75f, 0, 0);
//
//			MultiBufferSource.BufferSource immediate = instance.renderBuffers().bufferSource();
//			instance.getEntityRenderDispatcher().render(entity, 0, 0, 0, 0, instance.getFrameTime(), stack, immediate, 15728880);
//			immediate.endBatch();
//		}
//		stack.popPose();
//		RenderSystem.restoreProjectionMatrix();
//      ========================================
		//AnimR 的思路
		var hitbox = HitBoxHelper.getFromEntity(entity);
		GuiGraphics graphics = new GuiGraphics(instance, instance.renderBuffers().bufferSource());
		int scaledWidth = instance.getWindow().getGuiScaledWidth();
		int scaledHeight = instance.getWindow().getGuiScaledHeight();
		int sz = Math.min(scaledHeight, scaledWidth);
		RenderTarget target = new TextureTarget(scaledWidth, scaledHeight, true, Minecraft.ON_OSX);
		target.bindWrite(true);
		
		final float rotateX = -22.5f + offset.xRot(),
				rotateY = -45f + offset.yRot(),
				rotateZ = 22.5f + offset.zRot(),
				scale = offset.scale();
		final int itemDeltaX = offset.x(),
				itemDeltaY = offset.y();
		
		PoseStack stack = graphics.pose();
		stack.pushPose();
		{
			//预处理
			RenderSystem.clearColor(1, 1, 1, 0);
			RenderSystem.disableBlend();
			stack.setIdentity();
			//旋转模型;
			stack.translate(0, 0, 500);
			stack.mulPose(YP.rotationDegrees(rotateY));
			stack.mulPose(XP.rotationDegrees(rotateX));
			stack.mulPose(ZP.rotationDegrees(rotateZ));
			//渲染并保存
			int entitySz = (int) (80 * scale / Math.max(hitbox.width, hitbox.height));
			
			InventoryScreen.renderEntityInInventoryFollowsMouse(graphics,
					51, 75, 30, (float) (51.0f - instance.mouseHandler.xpos()), (float) (25.0f - instance.mouseHandler.ypos()), instance.player);
			
			InventoryScreen.renderEntityInInventoryFollowsMouse(
					new GuiGraphics(instance, instance.renderBuffers().bufferSource()),
					sz + itemDeltaX - 27,
					sz / 2 + itemDeltaY - 14,
					entitySz,
					0, 0,
					entity);
		}
		stack.popPose();
//      ========================================
		//JER的思路
//		GuiGraphics graphics = new GuiGraphics(instance, instance.renderBuffers().bufferSource());
//		PoseStack modelViewStack = RenderSystem.getModelViewStack();
//		modelViewStack.pushPose();
//		modelViewStack.mulPoseMatrix(graphics.pose().last().pose());
//		int x = 2, y = 2, scale = 1;
//		modelViewStack.translate(x, y, 50.0F);
//		modelViewStack.scale((float) -scale, (float) scale, (float) scale);
//		PoseStack mobPoseStack = new PoseStack();
//		mobPoseStack.mulPose(Axis.ZP.rotationDegrees(180.0F));
//		LivingEntity livingEntity = entity;
//		IMobRenderHook.RenderInfo renderInfo = MobRegistryImpl.applyRenderHooks(livingEntity, new IMobRenderHook.RenderInfo(x, y, scale, yaw, pitch));
//		x = renderInfo.x;
//		y = renderInfo.y;
//		scale = renderInfo.scale;
//		yaw = renderInfo.yaw;
//		pitch = renderInfo.pitch;
//		mobPoseStack.mulPose(Axis.XN.rotationDegrees(((float) Math.atan((pitch / 40.0F))) * 20.0F));
//		livingEntity.yo = (float) Math.atan(yaw / 40.0F) * 20.0F;
//		float yRot = (float) Math.atan(yaw / 40.0F) * 40.0F;
//		float xRot = -((float) Math.atan(pitch / 40.0F)) * 20.0F;
//		livingEntity.setYRot(yRot);
//		livingEntity.setYRot(yRot);
//		livingEntity.setXRot(xRot);
//		livingEntity.yHeadRot = yRot;
//		livingEntity.yHeadRotO = yRot;
//		mobPoseStack.translate(0.0F, livingEntity.getY(), 0.0F);
//		RenderSystem.applyModelViewMatrix();
//		EntityRenderDispatcher entityRenderDispatcher = Minecraft.getInstance().getEntityRenderDispatcher();
//		entityRenderDispatcher.setRenderShadow(false);
//		MultiBufferSource.BufferSource bufferSource = Minecraft.getInstance().renderBuffers().bufferSource();
//		RenderSystem.runAsFancy(() -> {
//			entityRenderDispatcher.render(livingEntity, 0.0D, 0.0D, 0.0D, 0.0F, 1.0F, mobPoseStack, bufferSource, 15728880);
//		});
//		bufferSource.endBatch();
//		entityRenderDispatcher.setRenderShadow(true);
//		modelViewStack.popPose();
//		RenderSystem.applyModelViewMatrix();
//
		//收尾
		target.unbindWrite();
		
		image = new NativeImage(target.width, target.height, false);
		RenderSystem.bindTexture(target.getColorTextureId());
		image.downloadTexture(0, false);
//		image.flipY();
	}
	
	
	@Override
	public NativeImage getImage() {
		return image;
	}
	
	
	/**
	 * <a href="https://github.com/GregTaoo/AnimationRecorder/blob/Fabric/Fabric-1.19/src/main/java/top/gregtao/animt/HitBoxHelper.java">Origin</a>
	 */
	public static class HitBoxHelper {
		public double x, y, z, theta, m, n;
		public double k, width, height;
		
		public HitBoxHelper(double x, double y, double z, double theta, double m, double n) {
			this.x = x;
			this.y = y;
			this.z = z;
			this.theta = theta;
			this.m = m;
			this.n = n;
			this.k = z / (Math.sqrt(m * m + n * n) * Math.tan(theta) - z / 2);
			this.width = this.width();
			this.height = this.height();
		}
		
		public static HitBoxHelper getFromEntity(@NotNull Entity entity) {
			AABB box = entity.getBoundingBox();
			return new HitBoxHelper(box.getXsize(), box.getYsize(), box.getZsize(), Math.PI / 8, 0, 0);
		}
		
		public double width() {
			return Math.sqrt(this.x * this.x + this.y * this.y) * (this.k + 1);
		}
		
		public double height() {
			double a = this.m * this.k + this.x * (this.k + 1) / 2;
			double b = this.n * this.k + this.y * (this.k + 1) / 2;
			return Math.sqrt(a * a + b * b);
		}
		
	}
}