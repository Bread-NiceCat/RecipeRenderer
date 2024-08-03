package cn.breadnicecat.reciperenderer.utils;

import java.util.HashSet;
import java.util.Scanner;

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
	public final HashSet<String> exists = new HashSet<>();
	
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
		return exists.add(path);
	}
	
	@Deprecated(forRemoval = true)
	public static void main(String[] args) {
		Scanner scanner = new Scanner(System.in);
		ExistHelper helper = new ExistHelper();
		while (true) {
			System.out.print("> ");
			System.out.println(helper.getModified(scanner.nextLine()));
		}
	}
}
