package cn.breadnicecat.reciperenderer.utils;

/**
 * Created in 2024/7/9 下午12:10
 * Project: reciperenderer
 *
 * @author <a href="https://github.com/Bread-Nicecat">Bread_NiceCat</a>
 * <p>
 *
 * <p>
 **/
public class Timer {
	public long start;
	
	public Timer() {
		reset();
	}
	
	public void reset() {
		this.start = ms();
	}
	
	public long get() {
		return (ms() - start);
	}
	
	public String getString() {
		return get() + "ms";
	}
	
	public String getStringAndReset() {
		String s = get() + "ms";
		reset();
		return s;
	}
	
	private long ms() {
		return System.currentTimeMillis();
	}
	
}
