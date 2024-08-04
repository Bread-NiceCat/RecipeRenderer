package cn.breadnicecat.reciperenderer.gui.screens;

import cn.breadnicecat.reciperenderer.Exporter;
import cn.breadnicecat.reciperenderer.RecipeRenderer;
import cn.breadnicecat.reciperenderer.render.EntityIcon;
import cn.breadnicecat.reciperenderer.render.IconWrapper;
import cn.breadnicecat.reciperenderer.render.ItemIcon;
import cn.breadnicecat.reciperenderer.utils.ExistHelper;
import cn.breadnicecat.reciperenderer.utils.PoseOffset;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.include.com.google.common.io.Files;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.event.*;
import java.io.File;

import static cn.breadnicecat.reciperenderer.RecipeRenderer.LOGGER;
import static cn.breadnicecat.reciperenderer.RecipeRenderer.hookRenderer;
import static javax.swing.JOptionPane.ERROR_MESSAGE;

/**
 * Created in 2024/7/25 上午5:33
 * Project: reciperenderer
 *
 * @author <a href="https://github.com/Bread-Nicecat">Bread_NiceCat</a>
 * <p>
 *
 * <p>
 **/
public class EntityViewScreen extends Screen {
	private static final Dimension SIZE = new Dimension(300, 300);
	public static final EntityViewScreen SIMPLE = new EntityViewScreen();
	private final JButton export = new JButton("导出");
	private MouseAdapter adapter;
	private ImgView view;
	private IconWrapper wrapper;
	private WindowAdapter onCloseListener = new WindowAdapter() {
		@Override
		public void windowClosing(WindowEvent e) {
			free();
		}
	};
	private ExistHelper helper = new ExistHelper(ExistHelper.absFileBase());
	private static Robot robot;
	
	static final int ITEM = 0, ENTITY = 1, UNDEFINED = -1;
	int renderType;
	
	static {
		try {
			robot = new Robot();
		} catch (AWTException e) {
			LOGGER.error("无法创建Robot", e);
		}
	}
	
	public EntityViewScreen() {
		try {
			LOGGER.info("launch test frame");
			this.view = new ImgView();
//			setBackground(Color.GRAY);
			view.setBorder(BorderFactory.createLineBorder(Color.BLACK));
			view.setOpaque(false);
			//300-128=172, 172/2=86
			view.setLayout(new FlowLayout(FlowLayout.CENTER, 20, 80));
			add(view);
			add(export);
			
			export.addMouseListener(new MouseAdapter() {
				@Override
				public void mouseClicked(MouseEvent e) {
					byte[] imgData = view.imgData;
//					if (true)
					if (imgData != null) {
						var chooser = new JFileChooser(Exporter.ROOT_DIR)
//						var chooser = new JFileChooser(new File("."))//测试用
						{//注意这里有个内部类
							void write(File f, byte[] data, boolean override) {
								try {
									File path = override ? new File(helper.getModified(f.toString())) : f;
									Files.write(data, path);
									JOptionPane.showMessageDialog(EntityViewScreen.this, "导出成功", "成功", JOptionPane.INFORMATION_MESSAGE);
								} catch (Exception ex) {
									LOGGER.error("单点导出失败", ex);
									JOptionPane.showMessageDialog(EntityViewScreen.this, ex, "错误", ERROR_MESSAGE);
								}
							}
						};
						chooser.setFileFilter(new FileNameExtensionFilter("图片(.png)", "png"));
						int code = chooser.showOpenDialog(EntityViewScreen.this);
						if (code == JFileChooser.APPROVE_OPTION) {
							File f = chooser.getSelectedFile();
							String p = f.getPath();
							f = p.endsWith(".png") ? f : new File(p + ".png");//强制加后缀
							if (f.exists()) {
								int v = JOptionPane.showConfirmDialog(EntityViewScreen.this,
										"文件" + f.getPath() + "已存在,是否覆盖？选否新建文件。",
										"重复文件", JOptionPane.YES_NO_CANCEL_OPTION);
								switch (v) {
									case JOptionPane.YES_OPTION -> chooser.write(f, imgData, false);
									case JOptionPane.NO_OPTION -> chooser.write(f, imgData, true);
									case JOptionPane.CANCEL_OPTION -> mouseClicked(e);
								}
							} else chooser.write(f, imgData, true);
						}
					} else
						JOptionPane.showMessageDialog(EntityViewScreen.this, "无法获取已渲染目标", "错误", ERROR_MESSAGE);
				}
			});
			adapter = new MouseAdapter() {
				PoseOffset off = PoseOffset.NONE;
				int pX, pY;
				
				@Override
				public void mousePressed(MouseEvent e) {
					pX = e.getXOnScreen();
					pY = e.getYOnScreen();
					//中键
					if ((e.getModifiersEx() & MouseEvent.BUTTON2_DOWN_MASK) != 0) {
						off = PoseOffset.NONE;
						view.update(off);
					}
				}
				
				@Override
				public void mouseDragged(MouseEvent e) {
					int x = e.getXOnScreen();
					int y = e.getYOnScreen();
					int deltaX = x - pX;
					int deltaY = y - pY;
					pX = x;
					pY = y;
//					if ((e.getModifiersEx() & MouseEvent.BUTTON1_DOWN_MASK) != 0) {
//					} else
					if (renderType == ENTITY && (e.getModifiersEx() & MouseEvent.BUTTON3_DOWN_MASK) != 0) {
						off = off.translate(deltaX, deltaY, 0);
						view.update(off);
					}
				}
				
				@Override
				public void mouseWheelMoved(MouseWheelEvent e) {
					off = off.scale(e.getWheelRotation() * -0.1f);
					view.update(off);
				}
			};
			LOGGER.info("fine");
		} catch (Exception e) {
			LOGGER.error("test error", e);
			free();
		}
	}
	
	
	private EntityViewScreen _setTarget(IconWrapper ta) {
		if (wrapper != ta) {
			wrapper = ta;
			view.update(PoseOffset.NONE);
		}
		return this;
	}
	
	public EntityViewScreen setTarget(IconWrapper ta) {
		renderType = UNDEFINED;
		return _setTarget(ta);
	}
	
	public EntityViewScreen setTarget(LivingEntity e) {
		renderType = ENTITY;
		return _setTarget(new IconWrapper(o -> new EntityIcon(o, 128, e)));
	}
	
	public EntityViewScreen setTarget(ItemStack is) {
		renderType = ITEM;
		return _setTarget(new IconWrapper(o -> new ItemIcon(o, 128, is)));
	}
	
	public void free() {
		wrapper = null;
		frame.free();
	}
	
	boolean alwaysOnTop;
	
	@Override
	public void onEnable() {
		RecipeRenderer.hookRenderer(() -> {
			LOGGER.info("正在暂停MC...");
			Minecraft.getInstance().pauseGame(false);
		});
		frame.removeWindowListener(frame.exitListener);
		frame.addWindowListener(onCloseListener);
		frame.addMouseListener(adapter);
		frame.addMouseMotionListener(adapter);
		frame.addMouseWheelListener(adapter);
		alwaysOnTop = frame.isAlwaysOnTop();
		frame.requestFocusInWindow();
		if (robot != null) {//移动鼠标
			robot.mouseMove(frame.getX() + frame.getWidth() / 2, frame.getY() + frame.getHeight() / 2);
		}
		frame.setAlwaysOnTop(true);
		JOptionPane.showMessageDialog(this, "右键拖动平移(物品不支持),滚轮缩放,单击滚轮重置.");
	}
	
	@Override
	public void onDisable() {
		frame.removeWindowListener(onCloseListener);
		SwingUtilities.invokeLater(() -> frame.addWindowListener(frame.exitListener));
		frame.removeMouseListener(adapter);
		frame.removeMouseMotionListener(adapter);
		frame.removeMouseWheelListener(adapter);
		wrapper = null;
		frame.setAlwaysOnTop(alwaysOnTop);
	}
	
	@Override
	public Dimension getScreenSize() {
		return SIZE;
	}
	
	public ImgView getView() {
		return view;
	}
	
	public class ImgView extends JLabel {
		PoseOffset last = null;
		boolean lock;
		IconWrapper zwr;
		public byte[] imgData;
		
		void update(PoseOffset o) {
			if (wrapper == null) return;
			if (wrapper != zwr) {
				zwr = wrapper;
				last = null;
			}
			
			if (lock) return;
			lock = true;
			if (last == o) {
				LOGGER.info("skip {}", o);
				lock = false;
			} else hookRenderer(() -> {
				LOGGER.info("update {}", o);
				zwr.clear();
				try {
					setIcon(new ImageIcon(imgData = zwr.render(o).getImage().asByteArray()));
				} catch (Exception e) {
					JOptionPane.showMessageDialog(EntityViewScreen.this, e, "渲染失败", ERROR_MESSAGE);
				}
				lock = false;
			});
		}
		
		@Override
		protected void paintComponent(Graphics g) {
			//4=128/32
			for (int i = 0; i < 4; i++) {
				for (int j = 0; j < 4; j++) {
					if ((i + j) % 2 == 0) {
						g.setColor(Color.WHITE);
					} else {
						g.setColor(Color.GRAY);
					}
					g.fillRect(j * 32, i * 32, 32, 32);
				}
			}
			super.paintComponent(g);
		}
	}
}
