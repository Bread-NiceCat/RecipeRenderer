package cn.breadnicecat.reciperenderer.utils;

import net.minecraft.util.profiling.InactiveProfiler;
import net.minecraft.util.profiling.ProfilerFiller;
import org.jetbrains.annotations.Nullable;

import java.util.Scanner;

/**
 * Created in 2024/8/4 下午5:29
 * Project: reciperenderer
 *
 * @author <a href="https://github.com/Bread-Nicecat">Bread_NiceCat</a>
 * <p>
 *
 * <p>
 **/
public class TaskChain {
	
	Task cur, tail;
	
	public TaskChain() {
		cur = tail = new Break();
	}
	
	public void run() {
		run(InactiveProfiler.INSTANCE);
	}
	
	public void run(ProfilerFiller profiler) {
		profiler.push("run_TaskChain");
		if (cur.hasNext()) {
			profiler.push("wait_synchronized");
			synchronized (this) {
				profiler.pop();
				tail = tail.bind(new Break());
			}
			while (cur.hasNext()) {
				cur = cur.next;
				if (cur instanceof Break) {
					break;
				}
				cur.run();
			}
		}
		profiler.pop();
	}
	
	public boolean hasNext() {
		return cur.hasNext();
	}
	
	
	private void add(Task task) {
		synchronized (this) {
			tail = tail.bind(task);
		}
	}
	
	public void add(Runnable runnable) {
		add(new Task(runnable));
	}
	
	public void endFrame() {
		add(new Break());
	}
	
	static class Task {
		@Nullable
		Task next;
		final Runnable runnable;
		
		Task(Runnable runnable) {
			this.runnable = runnable;
		}
		
		Task bind(Task next) {
			this.next = next;
			return next;
		}
		
		boolean hasNext() {
			return next != null;
		}
		
		void run() {
			runnable.run();
		}
	}
	
	static class Break extends Task {
		Break() {
			super(null);
		}
		
		@Override
		void run() {
		}
	}
	
	@Deprecated(forRemoval = true)
	public static void main(String[] args) {
		TaskChain tc = new TaskChain();
		Scanner sc = new Scanner(System.in);
		int ord = 0;
		//empty to pop
		//else to push
		while (true) {
			System.out.print("> ");
			String s = sc.nextLine();
			if (!s.isEmpty()) {
				if (s.equals("br")) {
					tc.endFrame();
				}
				int o = ++ord;
				tc.add(() -> System.out.println("pop " + o));
				System.out.println("push " + o);
			} else {
				System.out.println("pop all");
				while (tc.hasNext()) {
					tc.run();
					System.out.println("break;");
				}
			}
		}
	}
}
