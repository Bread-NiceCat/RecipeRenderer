package cn.breadnicecat.reciperenderer.utils;

import java.util.Scanner;

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
	private long start;
	private long pausedTime;
	
	public RTimer() {
		reset();
	}
	
	public RTimer(boolean paused) {
		start = -1;
	}
	
	public void reset() {
		this.pausedTime = 0;
		this.start = current();
	}
	
	public long get() {
		if (isPaused()) {
			return pausedTime;
		} else {
			return pausedTime + (current() - start);
		}
	}
	
	public void setTime(long time) {
		pausedTime = time;
	}
	
	public void pause() {
		if (isPaused()) return;
		pausedTime = get();
		start = -1;
	}
	
	public void resume() {
		if (!isPaused()) return;
		start = current();
	}
	
	public boolean isPaused() {
		return start < 0;
	}
	
	/**
	 * 1ms
	 */
	public String getStringMs() {
		return get() + "ms";
	}
	
	/**
	 * 1s
	 */
	public String getStringSecond() {
		return get() / 1000 + "s";
	}
	
	/**
	 * 1min1s
	 */
	public String getStringMinuteSecond() {
		long l = get() / 1000;
		return l / 60 + "min" + l % 60 + "s";
	}
	
	/**
	 * 1ms/1s/1min1s
	 */
	public String getString() {
		if (get() < 1000) return getStringMs();
		if (get() < 60 * 1000) return getStringSecond();
		else return getStringMinuteSecond();
	}
	
	/**
	 * 11:11(min:second)
	 */
	public String getClockString() {
		long l = get() / 1000;
		return l / 60 + ":" + l % 60;
	}
	
	private long current() {
		return System.currentTimeMillis();
	}
	
	@Override
	public String toString() {
		return getString();
	}
	
	@Deprecated(forRemoval = true)
	public static void main(String[] args) {
		Scanner scanner = new Scanner(System.in);
		RTimer rt = new RTimer();
		while (true) {
			System.out.print("> ");
			String v = scanner.nextLine();
			switch (v) {
				case "p" -> {
					if (!rt.isPaused()) {
						rt.pause();
						System.out.println("paused");
					} else {
						rt.resume();
						System.out.println("resumed");
					}
				}
				case "r" -> rt.reset();
				default -> System.out.println(rt.getString());
			}
		}
	}
	
	public void setPaused(boolean paused) {
		if (paused) pause();
		else resume();
	}
}
