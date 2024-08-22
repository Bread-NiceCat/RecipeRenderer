package cn.breadnicecat.reciperenderer.gui.screens;

import cn.breadnicecat.reciperenderer.gui.ExportFrame;

import javax.swing.*;
import java.awt.*;

/**
 * Created in 2024/7/25 上午3:13
 * Project: reciperenderer
 *
 * @author <a href="https://github.com/Bread-Nicecat">Bread_NiceCat</a>
 * <p>
 *
 * <p>
 **/
public abstract class Screen extends JPanel {
	public ExportFrame frame;
	
	
	public abstract Dimension getScreenSize();
	
	public void onEnable() {
	}
	
	public void onDisable() {
	}
}
