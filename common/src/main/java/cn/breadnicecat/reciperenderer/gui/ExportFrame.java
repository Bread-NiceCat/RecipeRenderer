package cn.breadnicecat.reciperenderer.gui;

import cn.breadnicecat.reciperenderer.RecipeRenderer;
import cn.breadnicecat.reciperenderer.gui.screens.DefaultScreen;
import cn.breadnicecat.reciperenderer.gui.screens.Screen;

import javax.swing.*;

/**
 * Created in 2024/7/25 上午3:13
 * Project: reciperenderer
 *
 * @author <a href="https://github.com/Bread-Nicecat">Bread_NiceCat</a>
 * <p>
 *
 * <p>
 **/
public class ExportFrame extends JFrame {
	public final DefaultScreen defaultScreen = new DefaultScreen();
	private Screen screen;
	private boolean busy;
	
	public ExportFrame() {
		setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
		setResizable(false);
		setScreen(defaultScreen);
		setTitle(RecipeRenderer.MOD_NAME);
		setVisible(true);
	}
	
	public void setScreen(Screen screen) {
		if (this.screen != null) {
			this.screen.onDisable();
			remove(this.screen);
		}
		if (screen != defaultScreen) busy = true;
		this.screen = screen;
		add(screen);
		setSize(screen.getScreenSize());
		screen.onEnable();
		repaint();
	}
	
	public Screen getScreen() {
		return screen;
	}
	
	public boolean isBusy() {
		return busy;
	}
	
	public void free() {
		setScreen(defaultScreen);
		this.busy = true;
	}
	
	
}
