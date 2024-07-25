package cn.breadnicecat.reciperenderer.gui.screens;

import cn.breadnicecat.reciperenderer.RecipeRenderer;
import cn.breadnicecat.reciperenderer.entry.EntityEntry;
import cn.breadnicecat.reciperenderer.utils.PoseOffset;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.List;
import java.util.Objects;

import static com.mojang.text2speech.Narrator.LOGGER;

/**
 * Created in 2024/7/25 上午5:33
 * Project: reciperenderer
 *
 * @author <a href="https://github.com/Bread-Nicecat">Bread_NiceCat</a>
 * <p>
 *
 * <p>
 **/
public class EntityAdjustScreen extends Screen {
	static Image coord256;
	private static final Dimension SIZE = new Dimension(300, 600);
	
	
	static {
		try {
			coord256 = ImageIO.read(Objects.requireNonNull(EntityAdjustScreen.class.getResourceAsStream("/gui/coord256.png")));
		} catch (Exception e) {
			LOGGER.error("无法加载图片", e);
		}
	}
	
	public EntityAdjustScreen(List<EntityEntry> entity) {
		EntityEntry entityEntry = entity.get(0);
		RecipeRenderer.hookRenderer(() -> entityEntry.ico.render(PoseOffset.NONE));
		JLabel coord = new JLabel(new ImageIcon(coord256)) {
			@Override
			public void print(Graphics g) {
				super.print(g);
				try {
					ImageIO.read(new ByteArrayInputStream(entityEntry.ico.getBytesBlocking()));
				} catch (IOException e) {
					throw new RuntimeException(e);
				}
				
			}
		};
		add(coord);
	}
	
	@Override
	public void print(Graphics g) {
		super.print(g);
	}
	
	
	@Override
	public Dimension getScreenSize() {
		return SIZE;
	}
}
