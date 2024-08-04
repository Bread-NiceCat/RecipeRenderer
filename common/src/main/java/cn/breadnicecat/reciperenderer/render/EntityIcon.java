package cn.breadnicecat.reciperenderer.render;

import cn.breadnicecat.reciperenderer.utils.PoseOffset;
import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.pipeline.TextureTarget;
import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexSorting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.AABB;
import org.joml.Matrix4f;
import org.joml.Vector3f;

import static com.mojang.math.Axis.*;
import static java.lang.Math.max;
import static java.lang.Math.min;

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
		GuiGraphics graphics = new GuiGraphics(instance, instance.renderBuffers().bufferSource());
		
		RenderTarget target = new TextureTarget(size, size, true, Minecraft.ON_OSX);
		target.bindWrite(true);
		
		final float rotateX = -22.5f + offset.xRot(),
				rotateY = -45f + offset.yRot(),
				rotateZ = 22.5f + offset.zRot(),
				scale = offset.scale() * 0.9f;
		final int itemDeltaX = offset.x(),
				itemDeltaY = offset.y();
		
		PoseStack stack = graphics.pose();
		stack.pushPose();
		{
			//预处理
			RenderSystem.backupProjectionMatrix();
//			Matrix4f p = new Matrix4f().setOrtho(0, 16, 16, 0, -150, 150);
			Matrix4f p = new Matrix4f().setOrtho(0, size, size, 0, -1000, 1000);
			RenderSystem.setProjectionMatrix(p, VertexSorting.ORTHOGRAPHIC_Z);
			RenderSystem.clearColor(1, 1, 1, 0);
			RenderSystem.disableBlend();
			stack.setIdentity();
			//旋转模型;
			stack.mulPose(YP.rotationDegrees(rotateY));
			stack.mulPose(XP.rotationDegrees(rotateX));
			stack.mulPose(ZP.rotationDegrees(rotateZ));
			var hitbox = new HitBox2D(stack.last().pose(), entity);
//			int entitySz = (int) (size * scale / Math.max(hitbox.width, hitbox.height));
			int szW = (int) (size * scale / hitbox.width);
			int szH = (int) (size * scale / hitbox.height);
			int entitySz = min(szW, szH);
			InventoryScreen.renderEntityInInventoryFollowsMouse(
					graphics,
					size + itemDeltaX,
					size / 2 + itemDeltaY,
					entitySz,
					0, 0,
					entity);
		}
		stack.popPose();
		
		//收尾
		RenderSystem.restoreProjectionMatrix();
		image = new NativeImage(target.width, target.height, false);
		RenderSystem.bindTexture(target.getColorTextureId());
		image.downloadTexture(0, false);
		image.flipY();
		target.destroyBuffers();
	}
	
	
	@Override
	public NativeImage getImage() {
		return image;
	}
	
	/**
	 * Created in 2024/8/4 上午1:52
	 * Project: reciperenderer
	 *
	 * @author <a href="https://github.com/Bread-Nicecat">Bread_NiceCat</a>
	 * @author mouse0w0
	 * <p>
	 *
	 * <p>
	 **/
	public static class HitBox2D {
		public final float width, height;
		
		public HitBox2D(Matrix4f pose, Entity entity) {
			AABB box = entity.getType().getAABB(0, 0, 0);
			
			float minX = 0, maxX = 0, minY = 0, maxY = 0;
			Vector3f v = new Vector3f();
			
			for (int i = 0; i < 8; i++) {
				v.set((i & 1) != 0 ? box.maxX : box.minX,
						(i & 2) != 0 ? box.maxY : box.minY,
						(i & 4) != 0 ? box.maxZ : box.minZ);
				pose.transformPosition(v, v);
				minX = min(minX, v.x);
				maxX = max(maxX, v.x);
				minY = min(minY, v.y);
				maxY = max(maxY, v.y);
			}
			
			width = maxX - minX;
			height = maxY - minY;
		}
	}
}