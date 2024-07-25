package cn.breadnicecat.reciperenderer.utils;

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
}
