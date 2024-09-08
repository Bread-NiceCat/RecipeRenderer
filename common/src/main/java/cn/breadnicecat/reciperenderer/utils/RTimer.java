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
public class RTimer {
	public long start;
	
	public RTimer() {
		reset();
	}
	
	public void reset() {
		this.start = ms();
	}
	
	public long get() {
		return (ms() - start);
	}
	
	public String getStringMs() {
		return get() + "ms";
	}
	
	public String getStringSecond() {
		return get() / 1000 + "s";
	}
	
	private String getStringMinuteSecond() {
		long l = get() / 1000;
		return l / 60 + "min" + l % 60 + "s";
	}
	
	public String getString() {
		return get() < 1000 ? getStringMs() : (get() > 60 * 1000 ? getStringMinuteSecond() : getStringSecond());
	}
	
	private long ms() {
		return System.currentTimeMillis();
	}
	
	@Override
	public String toString() {
		return getStringMs();
	}
}
