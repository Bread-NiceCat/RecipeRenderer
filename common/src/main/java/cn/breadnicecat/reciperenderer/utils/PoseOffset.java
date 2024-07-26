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
public record PoseOffset(float x, float y, float z,
                         float xRot, float yRot, float zRot,
                         float xScale, float yScale, float zScale) {
	public static final PoseOffset NONE = new PoseOffset(0f, 0f, 0f, 0f, 0f, 0f, 1f, 1f, 1f);
	
	public void apply(PoseStack ps) {
		if (this == NONE) return;
		ps.translate(x, y, z);
		if (xRot != 0f) ps.mulPose(Axis.XP.rotationDegrees(xRot));
		if (yRot != 0f) ps.mulPose(Axis.YP.rotationDegrees(yRot));
		if (zRot != 0f) ps.mulPose(Axis.ZP.rotationDegrees(zRot));
		ps.scale(xScale, yScale, zScale);
	}
	
	public PoseOffset translate(float deltaX, float deltaY, float deltaZ) {
		if (deltaX == deltaY && deltaY == deltaZ && deltaZ == 0f) {
			return this;
		} else {
			return setTranslate(x + deltaX, y + deltaY, z + deltaZ);
		}
	}
	
	public PoseOffset setTranslate(float x, float y, float z) {
		return new PoseOffset(x, y, z, xRot, yRot, zRot, xScale, yScale, zScale);
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
		return new PoseOffset(x, y, z, xRot, yRot, zRot, xScale, yScale, zScale);
	}
	
	public PoseOffset scale(float deltaScale) {
		return scale(deltaScale, deltaScale, deltaScale);
	}
	
	public PoseOffset scale(float deltaXScale, float deltaYScale, float deltaZScale) {
		if (deltaXScale == deltaYScale && deltaYScale == deltaZScale && deltaZScale == 0f) {
			return this;
		} else {
			return setScale(xScale + deltaXScale, yScale + deltaYScale, zScale + deltaZScale);
		}
	}
	
	public PoseOffset setScale(float xScale, float yScale, float zScale) {
		return new PoseOffset(x, y, z, xRot, yRot, zRot, xScale, yScale, zScale);
	}
}
