package cn.breadnicecat.reciperenderer.gui;

import cn.breadnicecat.reciperenderer.gui.screens.Screen;

import javax.swing.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

/**
 * Created in 2024/7/25 上午3:15
 * Project: reciperenderer
 *
 * @author <a href="https://github.com/Bread-Nicecat">Bread_NiceCat</a>
 * <p>
 *
 * <p>
 **/
@Deprecated(forRemoval = true)
public class WindowTestMain {
	public static void main(String[] args) {
		ExportFrame exportFrame = new ExportFrame();
		exportFrame.setAlwaysOnTop(true);
		exportFrame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
		
		JFrame cont = new JFrame();
		JButton next = new JButton("next[0]");
		next.addMouseListener(new MouseAdapter() {
			Screen[] sc = new Screen[]{exportFrame.defaultScreen};
			int i = 1;
			
			@Override
			public void mouseReleased(MouseEvent e) {
				if (i == sc.length) i = 0;
				next.setText("next[" + i + "]");
				exportFrame.setScreen(sc[i++]);
				next.repaint();
			}
		});
		cont.setSize(200, 100);
		new Timer(20, (e) -> cont.setLocation(exportFrame.getX() + exportFrame.getWidth(), exportFrame.getY())).start();
		cont.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
		cont.add(next);
		cont.setAlwaysOnTop(true);
		cont.setVisible(true);
	}
}
