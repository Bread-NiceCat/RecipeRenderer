package cn.breadnicecat.reciperenderer.utils;

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
		tail = new Task(() -> {
		});
		cur = tail;
	}
	
	public void run() {
		while (cur.hasNext()) {
			cur = cur.next;
			cur.run();
		}
	}
	
	public void add(Runnable runnable) {
		tail = new Task(tail, runnable);
	}
	
	
	private static class Task {
		@Nullable
		Task next;
		final Runnable runnable;
		
		Task(Runnable runnable) {
			this.runnable = runnable;
		}
		
		Task(Task prev, Runnable runnable) {
			this(runnable);
			prev.next = this;
		}
		
		boolean hasNext() {
			return next != null;
		}
		
		void run() {
			runnable.run();
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
				int o = ++ord;
				tc.add(() -> System.out.println("pop " + o));
				System.out.println("push " + o);
			} else {
				tc.run();
				System.out.println("pop all");
			}
		}
	}
}
