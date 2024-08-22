package cn.breadnicecat.reciperenderer.gui.screens;

import cn.breadnicecat.reciperenderer.WorldlyExporter;

import java.awt.*;

/**
 * Created in 2024/8/22 23:13
 * Project: reciperenderer
 *
 * @author <a href="https://github.com/Bread-Nicecat">Bread_NiceCat</a>
 * <p>
 *
 * <p>
 **/
public class WorldlyProgressScreen extends Screen {
	
	private final WorldlyExporter exporter;
	private Dimension dimension = new Dimension(256, 384);
	
	public WorldlyProgressScreen(WorldlyExporter exporter) {
		this.exporter = exporter;
		
	}
	
	@Override
	public Dimension getScreenSize() {
		return dimension;
	}
}
