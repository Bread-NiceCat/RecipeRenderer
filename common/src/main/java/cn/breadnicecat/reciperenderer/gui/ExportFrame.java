package cn.breadnicecat.reciperenderer.gui;

import cn.breadnicecat.reciperenderer.Exporter;
import cn.breadnicecat.reciperenderer.RecipeRenderer;
import cn.breadnicecat.reciperenderer.gui.screens.DefaultScreen;
import cn.breadnicecat.reciperenderer.gui.screens.Screen;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;

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
	public @Nullable Timer freeTimer;
	private boolean screenLocked = false;
	public static boolean debug = false;
	
	public ExportFrame(int timeout) {
		setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
		addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent e) {
				free();
			}
		});
		if (timeout > 0) freeTimer = new Timer(timeout, (e) -> {
			((Timer) e.getSource()).stop();
			free();
		});
		setResizable(false);
		setTitle(RecipeRenderer.MOD_NAME);
		setScreen(defaultScreen);
	}
	
	public ExportFrame() {
		this(-1);
	}
	
	final static File D_ROOT_DIR = new File(".");
	
	public static File getRootPath() {
		return debug ? D_ROOT_DIR : Exporter.ROOT_DIR;
	}
	
	public void setScreen(Screen screen) {
		if (screenLocked || this.screen == screen) return;
		if (this.screen != null) {
			this.screen.onDisable();
			screen.frame = null;
			remove(this.screen);
		}
		this.setVisible(true);
		this.screen = screen;
		
		if (isBusy()) {
			if (freeTimer != null) freeTimer.stop();
		} else {
			if (freeTimer != null) freeTimer.start();
		}
		
		add(screen);
		screen.frame = this;
		setSize(screen.getScreenSize());
		screen.onEnable();
	}
	
	public Screen getScreen() {
		return screen;
	}
	
	public boolean isBusy() {
		return screen != defaultScreen;
	}
	
	public void free() {
		if (screenLocked) {
			return;
		}
		if (!isBusy()) {
			setVisible(false);
			if (freeTimer != null && freeTimer.isRunning()) freeTimer.stop();
		} else {
			setScreen(defaultScreen);
		}
	}
	
	public boolean isScreenLocked() {
		return screenLocked;
	}
	
	public void setScreenLocked(boolean screenLocked) {
		this.screenLocked = screenLocked;
	}
}
