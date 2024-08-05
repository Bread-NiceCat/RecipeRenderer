package cn.breadnicecat.reciperenderer.gui;

import cn.breadnicecat.reciperenderer.RecipeRenderer;
import cn.breadnicecat.reciperenderer.gui.screens.DefaultScreen;
import cn.breadnicecat.reciperenderer.gui.screens.Screen;
import net.minecraft.client.Minecraft;

import javax.swing.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;

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
	public WindowListener exitListener = new WindowAdapter() {
		@Override
		public void windowClosing(WindowEvent e) {
//			RecipeRenderer.hookRenderer(() -> System.exit(0));
			RecipeRenderer.hookRenderer(Minecraft.getInstance()::close);
		}
	};
	
	public ExportFrame() {
		setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
		setResizable(false);
		setTitle(RecipeRenderer.MOD_NAME);
		setScreen(defaultScreen);
		setVisible(true);
		addWindowListener(exitListener);
	}
	
	public void setScreen(Screen screen) {
		if(this.screen == screen) return;
		if (this.screen != null) {
			this.screen.onDisable();
			screen.frame = null;
			remove(this.screen);
		}
		if (screen != defaultScreen) busy = true;
		this.screen = screen;
		add(screen);
		setSize(screen.getScreenSize());
		screen.frame = this;
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
		this.busy = false;
	}
	
	
}
