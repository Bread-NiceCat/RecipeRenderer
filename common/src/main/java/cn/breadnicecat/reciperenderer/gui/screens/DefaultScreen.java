package cn.breadnicecat.reciperenderer.gui.screens;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Objects;

import static cn.breadnicecat.reciperenderer.utils.CommonUtils.visit;
import static com.mojang.text2speech.Narrator.LOGGER;

/**
 * Created in 2024/7/25 上午3:14
 * Project: reciperenderer
 *
 * @author <a href="https://github.com/Bread-Nicecat">Bread_NiceCat</a>
 * <p>
 *
 * <p>
 **/
public class DefaultScreen extends Screen {
	private static final Dimension SIZE = new Dimension(200, 200);
	
	public static Image waitingImage, blackImage, whatImage, emmmImage, imageNow;
	final Timer timer = new Timer(50, this::tick);
	/**
	 * 黑化时长
	 */
	int blackLeft;
	//短时间内改变次数
	int change;
	int red = 255, green, blue;
	
	static {
		try {
			waitingImage = ImageIO.read(Objects.requireNonNull(DefaultScreen.class.getResourceAsStream("/gui/waiting.png")));
			whatImage = ImageIO.read(Objects.requireNonNull(DefaultScreen.class.getResourceAsStream("/gui/what.png")));
			blackImage = ImageIO.read(Objects.requireNonNull(DefaultScreen.class.getResourceAsStream("/gui/black.png")));
			emmmImage = ImageIO.read(Objects.requireNonNull(DefaultScreen.class.getResourceAsStream("/gui/emmm.png")));
			imageNow = waitingImage;
		} catch (Exception e) {
			LOGGER.error("无法加载图片", e);
		}
	}
	
	private final JLabel label;
	
	public DefaultScreen() {
		this.label = new JLabel("/rr export modid");
		label.setVerticalTextPosition(SwingConstants.CENTER);
		visit(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				int x = e.getX(), y = e.getY();
				var ori = imageNow;
				if (mouseInPic(x, y)) {
					blackLeft = 1000;
					imageNow = emmmImage;
				}
				if (ori != imageNow) repaint();
			}
			
			@Override
			public void mouseMoved(MouseEvent e) {
				if (blackLeft > 0) return;
				int x = e.getX(), y = e.getY();
				var ori = imageNow;
				if (mouseInPic(x, y)) {
					imageNow = whatImage;
				} else imageNow = waitingImage;
				if (ori != imageNow) {
					if ((change += 2) > 5) {
						blackLeft = 1000;
						imageNow = blackImage;
					}
					repaint();
				}
			}
		}, this::addMouseListener, this::addMouseMotionListener);
		add(label);
	}
	
	private boolean mouseInPic(int x, int y) {
		int pw = getWidth() / 2 - 64, ph = getHeight() / 2 - 64;
		return x > pw && x < pw + 128 && y > ph && y < ph + 128;
	}
	
	private void tick(ActionEvent e) {
		if (blackLeft > 0) {
			if ((blackLeft -= 50) <= 0) {
				Point point = getMousePosition();
				imageNow = point != null && mouseInPic(point.x, point.y) ? whatImage : waitingImage;
				repaint();
			}
		}
		if (change > 0) change--;
		// 更新RGB值
		int delta = 15;
		if (red > 0 && blue == 0) {
			red -= delta;
			green += delta;
		}
		if (green > 0 && red == 0) {
			green -= delta;
			blue += delta;
		}
		if (blue > 0 && green == 0) {
			blue -= delta;
			red += delta;
		}
		// 设置新的背景颜色
		label.setForeground(new Color(red, green, blue));
	}
	
	@Override
	public void onEnable() {
		timer.start();
	}
	
	@Override
	public void onDisable() {
		timer.stop();
	}
	
	
	@Override
	public void paint(Graphics g) {
		super.paint(g);
		if (imageNow != null) g.drawImage(imageNow, getWidth() / 2 - 64, getHeight() / 2 - 64, 128, 128, this);
	}
	
	@Override
	public Dimension getScreenSize() {
		return SIZE;
	}
}
