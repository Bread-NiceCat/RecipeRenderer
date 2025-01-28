import cn.breadnicecat.reciperenderer.utils.StageTimer;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedList;
import java.util.Random;

/**
 * Created in 2025/1/26 22:47
 * Project: reciperenderer
 *
 * @author <a href="https://github.com/Bread-Nicecat">Bread_NiceCat</a>
 * <p>
 *
 * <p>
 **/
public class STimer {
	public static void main(String[] args) throws IOException {
		StageTimer timer = new StageTimer();
		Random r = new Random(0);
		int push = 0, pop = 0;
		int depth = 0, index = 0;
		//System.out.println(i + ".push" + " index=" + ++index + " push/pop=" + ++push + "/" + pop + " depth=" + ++depth);
//System.out.println(i + ".pop" + " index=" + index + " push/pop=" + push + "/" + ++pop + " depth=" + --depth);
		LinkedList<StageTimer.Node> nodes = new LinkedList<>();
		for (int i = 0; i < 1000; i++) {
			String is = String.valueOf(i);
			if (nodes.isEmpty()) {
				nodes.add(timer.push(is));
			} else if (r.nextBoolean()) {
				nodes.add(nodes.getLast().push(is));
			} else {
				nodes.removeLast().close();
			}
		}
		timer.end();
		String s = timer.toString();
		System.out.println(s);
		Files.writeString(Path.of(".gradle", "stimer.txt"), s);
	}
}
