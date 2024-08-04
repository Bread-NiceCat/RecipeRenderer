package cn.breadnicecat.reciperenderer.utils;

import java.io.File;
import java.util.HashSet;
import java.util.Scanner;
import java.util.function.Predicate;

/**
 * Created in 2024/8/3 上午9:28
 * Project: reciperenderer
 *
 * @author <a href="https://github.com/Bread-Nicecat">Bread_NiceCat</a>
 * <p>
 *
 * <p>
 **/
public class ExistHelper {
	/**
	 * true为不存在
	 */
	public final Predicate<String> recorder;
	
	public ExistHelper() {
		this(simple());
	}
	
	public ExistHelper(Predicate<String> recorder) {
		this.recorder = recorder;
	}
	
	public static Predicate<String> simple() {
		HashSet<String> set = new HashSet<>();
		return e -> !set.add(e);
	}
	
	public static Predicate<String> fileBase(File root) {
		return s -> new File(root, s).exists();
	}
	
	public static Predicate<String> absFileBase() {
		return s -> new File(s).exists();
	}
	
	/**
	 * @return 如果有重复则在后面加 _${i}
	 */
	public String getModified(String path) {
		if (record(path)) return path;
		int last = path.lastIndexOf('.');
		//pre点前, post点后(带点)
		String pre, post;
		if (last != -1) {
			pre = path.substring(0, last);
			post = path.substring(last);
		} else {
			pre = path;
			post = "";
		}
		String st;
		for (int i = 1; !record(st = pre + "_" + i + post); i++) {
		}
		return st;
	}
	
	/**
	 * @return true记录成功，false重复失败
	 */
	public boolean record(String path) {
		return !recorder.test(path);
	}
	
	@Deprecated(forRemoval = true)
	public static void main(String[] args) {
		Scanner scanner = new Scanner(System.in);
		ExistHelper helper = new ExistHelper();

//		File root = new File(".");
//		for (String s : root.list()) {
//			System.out.println(s);
//		}
//		ExistHelper helper = new ExistHelper(fileBase(root));
		
		while (true) {
			System.out.print("> ");
			System.out.println(helper.getModified(scanner.nextLine()));
		}
	}
}
