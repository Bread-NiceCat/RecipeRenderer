package cn.breadnicecat.reciperenderer;

/**
 * Created in 2024/7/10 下午8:52
 * Project: reciperenderer
 *
 * @author <a href="https://github.com/Bread-Nicecat">Bread_NiceCat</a>
 * <p>
 *
 * <p>
 **/
public class ExportCancelledException extends RuntimeException {
	public ExportCancelledException() {
		super("已取消");
	}
}
