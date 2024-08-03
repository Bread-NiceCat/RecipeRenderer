package cn.breadnicecat.reciperenderer.utils;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;

/**
 * Created in 2024/7/25 下午12:52
 * Project: reciperenderer
 *
 * @author <a href="https://github.com/Bread-Nicecat">Bread_NiceCat</a>
 * <p>
 *
 * <p>
 **/
public record PoseOffset(int x, int y, int z,
                         float xRot, float yRot, float zRot,
                         float scale) {
	public static final PoseOffset NONE = new PoseOffset(0, 0, 0, 0f, 0f, 0f, 1f);
	
	public void apply(PoseStack ps) {
		if (this == NONE) return;
		ps.translate(x, y, z);
		if (xRot != 0f) ps.mulPose(Axis.XP.rotationDegrees(xRot));
		if (yRot != 0f) ps.mulPose(Axis.YP.rotationDegrees(yRot));
		if (zRot != 0f) ps.mulPose(Axis.ZP.rotationDegrees(zRot));
		ps.scale(scale, scale, scale);
	}
	
	public PoseOffset translate(int deltaX, int deltaY, int deltaZ) {
		if (deltaX == deltaY && deltaY == deltaZ && deltaZ == 0f) {
			return this;
		} else {
			return setTranslate(x + deltaX, y + deltaY, z + deltaZ);
		}
	}
	
	public PoseOffset setTranslate(int x, int y, int z) {
		return new PoseOffset(x, y, z, xRot, yRot, zRot, scale);
	}
	
	/**
	 * 单位deg
	 */
	public PoseOffset rotate(float deltaXRot, float deltaYRot, float deltaZRot) {
		if (deltaXRot == deltaYRot && deltaYRot == deltaZRot && deltaZRot == 0f) {
			return this;
		} else {
			return setRotate(xRot + deltaXRot, yRot + deltaYRot, zRot + deltaZRot);
		}
	}
	
	/**
	 * 单位deg
	 */
	public PoseOffset setRotate(float xRot, float yRot, float zRot) {
		return new PoseOffset(x, y, z, xRot, yRot, zRot, scale);
	}
	
	public PoseOffset scale(float deltaScale) {
		if (deltaScale == 0f) {
			return this;
		} else {
			return setScale(scale + deltaScale);
		}
	}
	
	public PoseOffset setScale(float scale) {
		return new PoseOffset(x, y, z, xRot, yRot, zRot, scale);
	}
}
