package cn.breadnicecat.reciperenderer.gui.screens;

import cn.breadnicecat.reciperenderer.gui.ExportFrame;
import cn.breadnicecat.reciperenderer.includes.AnimatedGifEncoder.AnimatedGifEncoder;
import cn.breadnicecat.reciperenderer.render.EntityIcon;
import cn.breadnicecat.reciperenderer.render.IIcon;
import cn.breadnicecat.reciperenderer.render.IconWrapper;
import cn.breadnicecat.reciperenderer.render.ItemIcon;
import cn.breadnicecat.reciperenderer.utils.ExistHelper;
import cn.breadnicecat.reciperenderer.utils.PoseOffset;
import cn.breadnicecat.reciperenderer.utils.RTimer;
import com.google.common.io.Files;
import net.minecraft.client.Minecraft;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;

import static cn.breadnicecat.reciperenderer.RecipeRenderer.LOGGER;
import static cn.breadnicecat.reciperenderer.RecipeRenderer.hookRenderer;
import static cn.breadnicecat.reciperenderer.utils.CommonUtils.make;
import static java.awt.event.KeyEvent.VK_0;
import static java.awt.event.KeyEvent.VK_9;
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
	public static boolean showTip = false;
	
	private final ByteArrayOutputStream gifTarget = new ByteArrayOutputStream();
	private final AnimatedGifEncoder gifEncoder = make(new AnimatedGifEncoder(), e -> {
		e.setSize(128, 128);
		e.setRepeat(0);
	});
	private final RTimer frameTimer = new RTimer();
	/**
	 * true时才会在渲染的时候保存
	 * 0b_paused_running
	 */
	private volatile boolean gifLever;
	private int fpsUpper = 30;
	private IconWrapper wrapper;
	private ImgView view;
	private final JButton export = make(new JButton(" 导出 "), t -> {
		t.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				byte[] imgData = view.imgData;
				if (imgData != null) {
					xSave("图片", "png", imgData);
				} else
					JOptionPane.showMessageDialog(EntityViewScreen.this, "无法获取已渲染目标", "错误", ERROR_MESSAGE);
			}
		});
	});
	private final JToggleButton record = make(new JToggleButton(" 录制 "), t -> {
		t.addActionListener(new ActionListener() {
			boolean alwaysRenderOri;
			int r = 255, mu = -1;
			Timer colorTimer = new Timer(15, (e) -> {
				r += 5 * mu;
				if (r == 0 || r == 255) mu = -mu;
				view.setBorder(BorderFactory.createLineBorder(new Color(r, 0, 0), 2));
			});
			
			@Override
			public void actionPerformed(ActionEvent e) {
				if (t.isSelected()) {
					gifLever = true;
					frame.setScreenLocked(true);
					t.setForeground(Color.RED);
					view.setBorder(BorderFactory.createLineBorder(Color.RED, 2));
					gifFrames.setVisible(true);
					gifEncodedFrames.setVisible(true);
					gifBufferSize.setVisible(true);
					r = 255;
					mu = -1;
					colorTimer.start();
					alwaysRenderOri = renderAlways.isSelected();
					renderAlways.setSelected(true);
					renderAlways.setEnabled(false);
				} else {
					gifLever = false;
					frame.setScreenLocked(false);
					renderAlways.setSelected(alwaysRenderOri);
					renderAlways.setEnabled(true);
					view.gifEncodedCnt = view.gifFrameCnt = 0;
					colorTimer.stop();
					view.setBorder(BorderFactory.createLineBorder(Color.GRAY, 2));
					//防止finish之后addFrame还未执行完
					SwingUtilities.invokeLater(() -> {
						t.setForeground(Color.BLACK);
						gifFrames.setVisible(false);
						gifEncodedFrames.setVisible(false);
						gifBufferSize.setVisible(false);
						gifEncoder.finish();
						byte[] imgData = gifTarget.toByteArray();
						xSave("动图", "gif", imgData);
					});
				}
			}
		});
	});
	private final JTextField fpsText = make(new JTextField("30", 3), t -> {
		t.setHorizontalAlignment(JTextField.CENTER);
		//点击外面的时候光标可以自动消去
		EntityViewScreen.this.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				if (!t.getBounds().contains(e.getPoint())) {
					if (t.getText().isEmpty()) t.setText("30");
					EntityViewScreen.this.requestFocusInWindow();
				}
			}
		});
		t.addKeyListener(new KeyAdapter() {
			@Override
			public void keyTyped(KeyEvent e) {
				String text = t.getText();
				int keyChar = e.getKeyChar();
				if (text.length() > 2 || (keyChar < VK_0 || keyChar > VK_9)) {
					e.consume();
				}
			}
		});
		t.addActionListener(e -> {
			fpsUpper = Integer.parseInt(t.getText());
		});
	});
	private final JCheckBox showHitbox = make(new JCheckBox("显示碰撞箱"), t -> {
		new Timer(500, (e) -> {
			t.setSelected(Minecraft.getInstance().getEntityRenderDispatcher().shouldRenderHitBoxes());
		}).start();
		t.addItemListener((e) -> Minecraft.getInstance().getEntityRenderDispatcher().setRenderHitBoxes(e.getStateChange() == ItemEvent.SELECTED));
	});
	private JCheckBox renderAlways = make(new JCheckBox("循环渲染", true), (b) -> {
		b.addItemListener(new ItemListener() {
			@Override
			public void itemStateChanged(ItemEvent e) {
				if (e.getStateChange() == ItemEvent.SELECTED) {
					fps.setVisible(true);
					view.doUpdate();
				} else {
					fps.setVisible(false);
				}
			}
		});
	});
	private JCheckBox useOverride = new JCheckBox("使用重写", true);
	private JCheckBox gifStopWhenLoop = new JCheckBox("录制闭环", false);
	private JLabel fps = new JLabel();
	boolean mcPaused = false;
	private JLabel paused = make(new JLabel("MC已暂停"), t -> {
		t.setVisible(false);
		t.setForeground(Color.RED);
		new Timer(50, e -> {
			mcPaused = Minecraft.getInstance().isPaused();
			t.setVisible(mcPaused);
		}).start();
	});
	private JLabel gifFrames = make(new JLabel(), t -> t.setVisible(false));
	private JLabel gifEncodedFrames = make(new JLabel(), t -> t.setVisible(false));
	private JLabel gifBufferSize = make(new JLabel(), t -> t.setVisible(false));
	
	private void setFps(float fps) {
		this.fps.setText("FPS:%.1f/%d".formatted(fps, fpsUpper));
	}
	
	private void setGifFrames(int cnt) {
		gifFrames.setText("帧总数:" + cnt);
	}
	
	private void setGifEncodedFrames(int cnt) {
		gifEncodedFrames.setText("已编码:" + cnt);
		int size = gifTarget.size();
		float sizeKb = size / 1024f;
		if (sizeKb > 1024f) {//>1mb
			gifBufferSize.setText("已缓存:%.2fMB".formatted(sizeKb / 1024));
		} else {
			gifBufferSize.setText("已缓存:%.2fKB".formatted(sizeKb));
		}
	}
	
	private static ExistHelper helper = new ExistHelper(ExistHelper.absFileBase());
	
	public EntityViewScreen() {
		try {
			LOGGER.info("launch test frame");
			this.view = new ImgView();
			setLayout(new FlowLayout(FlowLayout.CENTER, 5, 5));
			add(make(new JPanel(), (p) -> {
				p.setLayout(new FlowLayout(FlowLayout.CENTER));
				p.add(view);
				p.add(make(new JPanel(), (pp) -> {
					pp.setLayout(new BoxLayout(pp, BoxLayout.Y_AXIS));
					pp.add(export);
					pp.add(record);
					pp.add(fps);
					pp.add(paused);
					pp.add(gifFrames);
					pp.add(gifEncodedFrames);
					pp.add(gifBufferSize);
				}));
			}));
			add(make(new JPanel(), (p) -> {
				p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
				p.add(make(new JPanel(), pp -> {
					pp.setLayout(new FlowLayout(FlowLayout.CENTER));
					pp.add(renderAlways);
					pp.add(showHitbox);
					pp.add(useOverride);
				}));
				p.add(make(new JPanel(), pp -> {
					pp.setLayout(new FlowLayout(FlowLayout.CENTER));
					pp.add(new JLabel("fps上限"));
					pp.add(fpsText);
					pp.add(gifStopWhenLoop);
				}));
			}));
			LOGGER.info("fine");
		} catch (Exception e) {
			LOGGER.error("test error", e);
			frame.free();
		}
	}
	
	
	public EntityViewScreen _setTarget(IconWrapper ta, boolean showHixboxEnabled, boolean overrideUsing) {
		showHitbox.setVisible(showHixboxEnabled);
		useOverride.setVisible(overrideUsing);
		if (wrapper != ta) {
			wrapper = ta;
			view.update(PoseOffset.NONE);
		}
		return this;
	}
	
	public EntityViewScreen setTarget(LivingEntity e) {
		return _setTarget(new IconWrapper(o -> new EntityIcon(o, 128, e)), true, false);
	}
	
	public EntityViewScreen setTarget(ItemStack is) {
		return _setTarget(new IconWrapper(o -> new ItemIcon(o, 128, is, getUseOverride() ? Minecraft.getInstance().player : null)), false, true);
	}
	
	public boolean getUseOverride() {
		return useOverride.isEnabled() && useOverride.isSelected();
	}
	
	boolean alwaysOnTop;
	
	@Override
	public void onEnable() {
		alwaysOnTop = frame.isAlwaysOnTop();
		frame.setAlwaysOnTop(true);
		if (showTip) {
			JOptionPane.showMessageDialog(this, "左键拖动旋转,右键拖动平移,滚轮缩放,单击滚轮重置.");
			showTip = false;
		}
	}
	
	@Override
	public void onDisable() {
		wrapper = null;
		view.imgLast = null;
		view.imgData = null;
		view.gifImgFirst = null;
		if (record.isSelected()) {
			record.doClick();
		}
		frame.setAlwaysOnTop(alwaysOnTop);
	}
	
	@Override
	public Dimension getScreenSize() {
		return SIZE;
	}
	
	public ImgView getView() {
		return view;
	}
	
	private void xSave(String chooserDesc, String chooserExtension, byte[] imgData) {
		while (true) {
			var chooser = new JFileChooser(ExportFrame.getRootPath()) {
				void write(File f, byte[] data, boolean override) {
					try {
						File path = override ? new File(helper.getModified(f.toString())) : f;
						Files.write(data, path);
						JOptionPane.showMessageDialog(EntityViewScreen.this, "导出成功", "成功", JOptionPane.INFORMATION_MESSAGE);
					} catch (Exception ex) {
						LOGGER.error("导出失败", ex);
						JOptionPane.showMessageDialog(EntityViewScreen.this, ex, "错误", ERROR_MESSAGE);
					}
				}
			};
			
			String suffix = "." + chooserExtension;
			chooser.setFileFilter(new FileNameExtensionFilter(chooserDesc + "(" + chooserExtension + ")", chooserExtension));
			int code = chooser.showOpenDialog(EntityViewScreen.this);
			if (code == JFileChooser.APPROVE_OPTION) {
				File f = chooser.getSelectedFile();
				String p = f.getPath();
				f = p.endsWith(suffix) ? f : new File(p + suffix);//强制加后缀
				if (f.exists()) {
					int v = JOptionPane.showConfirmDialog(this,
							"文件" + f.getPath() + "已存在,是否覆盖？选是覆盖,选否新建一个不重名文件。", "重复文件", JOptionPane.YES_NO_CANCEL_OPTION);
					switch (v) {
						case JOptionPane.YES_OPTION -> chooser.write(f, imgData, false);
						case JOptionPane.NO_OPTION -> chooser.write(f, imgData, true);
						case JOptionPane.CANCEL_OPTION -> {
							continue;
						}
					}
				} else chooser.write(f, imgData, true);
			}
			break;
		}
	}
	
	public class ImgView extends JLabel {
		boolean lock;
		IconWrapper curIco;
		public byte[] imgData;
		BufferedImage imgLast;
		BufferedImage gifImgFirst;
		MouseAdapter adjustor = new MouseAdapter() {
			int pX, pY;
			
			@Override
			public void mousePressed(MouseEvent e) {
				pX = e.getXOnScreen();
				pY = e.getYOnScreen();
				//中键
				if ((e.getModifiersEx() & MouseEvent.BUTTON2_DOWN_MASK) != 0) {
					view.update(PoseOffset.NONE);
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
				if ((e.getModifiersEx() & MouseEvent.BUTTON1_DOWN_MASK) != 0) {
					view.update(view.lasto.rotate(0f, ((float) deltaX / getSize().width) * 180, 0f));
				} else if ((e.getModifiersEx() & MouseEvent.BUTTON3_DOWN_MASK) != 0) {
					view.update(view.lasto.translate(deltaX, deltaY, 0));
				}
			}
			
			@Override
			public void mouseWheelMoved(MouseWheelEvent e) {
				view.update(view.lasto.scale(e.getWheelRotation() * -0.1f));
			}
		};
		PoseOffset lasto = PoseOffset.NONE;
		volatile int gifFrameCnt;
		volatile int gifEncodedCnt;
		float fpsAvg = 30f;
		int nonDuplicateFrameGap;
		
		public ImgView() {
			this.addMouseListener(adjustor);
			this.addMouseMotionListener(adjustor);
			this.addMouseWheelListener(adjustor);
			
			setBorder(BorderFactory.createLineBorder(Color.GRAY, 2));
			setOpaque(false);
		}
		
		public void doUpdate() {
			update(lasto);
		}
		
		void update(PoseOffset o) {
			this.lasto = o;
			if (wrapper == null) return;
			if (wrapper != curIco) {
				curIco = wrapper;
			}
			//防止多项任务堆积，而后面的任务会迅速覆盖前面的任务，造成无用资源浪费
			if (!lock) {
				lock = true;
				hookRenderer(() -> {
					ProfilerFiller profiler = Minecraft.getInstance().getProfiler();
					profiler.push("update_ImgView");
					try {
						int frameGap = (int) frameTimer.get();
						float fps = 1000f / frameGap;
						if (fps <= fpsUpper) {
							fpsAvg = fpsAvg == 0 ? fps : (fps + 4 * fpsAvg) / 5f;
							curIco.clear();
							IIcon icon = curIco.render(o).getOrThrow();
							if (gifLever) {
								setGifFrames(++gifFrameCnt);
							}
							setFps(fpsAvg);
							frameTimer.reset();
							
							imgData = icon.getImage().asByteArray();
							SwingUtilities.invokeLater(() -> {
								Image image = Toolkit.getDefaultToolkit().createImage(imgData);
								setIcon(new ImageIcon(image));
								if (gifLever) {
									boolean startFlag = !gifEncoder.isStarted();
									BufferedImage bufferedImage = make(new BufferedImage(128, 128, BufferedImage.TYPE_INT_RGB), (i) -> {
										Graphics2D g2d = i.createGraphics();
										g2d.setColor(Color.WHITE);
										g2d.fillRect(0, 0, 128, 128);
										g2d.drawImage(image, 0, 0, null);
									});
									if (startFlag) {
										gifImgFirst = bufferedImage;
										gifTarget.reset();
										gifEncoder.start(gifTarget);
										gifEncoder.setDelay(0);
										gifFrameCnt = 1;//上面已经计数过一次
										gifEncodedCnt = 0;
										nonDuplicateFrameGap = 0;
									} else {
										gifEncoder.setDelay(frameGap + nonDuplicateFrameGap);
									}
									boolean duplicateFlag = false;
									boolean endFlag = false;
									if (imgLast != null) {
										if (isEquals(bufferedImage, imgLast)) {
											duplicateFlag = true;
											nonDuplicateFrameGap += frameGap;
										} else {
											nonDuplicateFrameGap = 0;
										}
										if (!startFlag && gifStopWhenLoop.isSelected()) {
											if (isEquals(bufferedImage, gifImgFirst)) {
												endFlag = true;
											}
										}
									}
									if (!duplicateFlag) {
										gifEncoder.addFrame(bufferedImage);
										setGifEncodedFrames(++gifEncodedCnt);
									}
									imgLast = bufferedImage;
									if (endFlag) {
										record.doClick();
									}
								}
							});
						}
						if (renderAlways.isSelected()) hookRenderer(this::doUpdate);
					} catch (Exception e) {
						LOGGER.error("渲染错误", e);
						JOptionPane.showMessageDialog(EntityViewScreen.this, e, "渲染失败", ERROR_MESSAGE);
					} finally {
						lock = false;
					}
					profiler.pop();
				});
			}
		}
		
		static boolean isEquals(@NotNull BufferedImage image, @NotNull BufferedImage i2) {
			int height = image.getHeight();
			int width = image.getWidth();
			if (i2.getHeight() == height && i2.getWidth() == width) {
				for (int y = 0; y < image.getHeight(); y++) {
					for (int x = 0; x < image.getWidth(); x++) {
						if (i2.getRGB(x, y) != image.getRGB(x, y)) {
							return false;
						}
					}
				}
				return true;
			}
			return false;
		}
		
		@Override
		protected void paintComponent(Graphics g) {
			//4=128/32
			int w = getWidth();
			int h = getHeight();
			int gap = 32;
			for (int i = 0; i < w / gap; i++) {
				for (int j = 0; j < h / gap; j++) {
					if ((i + j) % 2 == 0) {
						g.setColor(Color.WHITE);
					} else {
						g.setColor(Color.GRAY);
					}
					g.fillRect(j * gap, i * gap, gap, gap);
				}
			}
			super.paintComponent(g);
		}
	}
}
