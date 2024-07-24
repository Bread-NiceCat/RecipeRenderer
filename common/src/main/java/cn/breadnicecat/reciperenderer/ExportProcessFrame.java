package cn.breadnicecat.reciperenderer;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;

/**
 * Created in 2024/7/9 上午9:53
 * Project: reciperenderer
 *
 * @author <a href="https://github.com/Bread-Nicecat">Bread_NiceCat</a>
 * <p>
 *
 * <p>
 **/
public class ExportProcessFrame {
	
	public final JFrame frame;
	public final JProgressBar progressBar;
	public final JButton button;
	public final JLabel label0;
	public final JLabel label;
	private final ActionListener CANCEL;
	protected int process = 0;
	protected boolean cancel = false;
	private static final int MAX = 10000;
	private Thread daemon = null;
	
	public ExportProcessFrame(String modid) {
		// 创建窗口
		frame = new JFrame("导出 " + modid);
		frame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		frame.setAlwaysOnTop(true);
		int widthBase = 350;
		frame.setSize(new Dimension(widthBase + 15, 260));
		
		JPanel panel = new JPanel(new FlowLayout());
		panel.setPreferredSize(new Dimension(widthBase + 15, 200));
		// 创建文本标签
		label0 = new JLabel("导出 " + modid);
		label = new JLabel("正在导出");
		label0.setPreferredSize(new Dimension(widthBase, 50));
		label.setPreferredSize(new Dimension(widthBase, 30));
		panel.add(label0);
		panel.add(label);
		
		// 创建进度条
		progressBar = new JProgressBar(0, MAX);
		progressBar.setPreferredSize(new Dimension(widthBase + 15, 50));
		panel.add(progressBar);
		progressBar.setStringPainted(true);
		setProcess(process);
		
		// 创建取消按钮
		button = new JButton("取消");
		panel.add(button);
		CANCEL = e -> cancel();
		button.setPreferredSize(new Dimension(widthBase, 50));
		button.addActionListener(CANCEL);
		
		frame.add(panel);
		// 获取屏幕尺寸
		Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
		// 获取窗口尺寸
		Dimension frameSize = frame.getSize();
		// 计算窗口居中的位置
		int x = (screenSize.width - frameSize.width) / 2;
		int y = (screenSize.height - frameSize.height) / 2;
		// 设置窗口位置
		frame.setLocation(x, y);
	}
	
	public void show() {
		frame.setVisible(true);
	}
	
	public void setDaemon(Thread toDaemon) {
		daemon = toDaemon;
	}
	
	protected ExportProcessFrame() {
		this.frame = null;
		this.progressBar = null;
		this.button = null;
		this.label = null;
		this.label0 = null;
		CANCEL = null;
	}
	
	public static ExportProcessFrame empty() {
		return new Empty();
	}
	
	static final float SPLIT = MAX / 100f;
	
	protected void setProcess(int process) {
		checkCancel();
		this.process = process;
		progressBar.setValue(process);
		//max 分为 100 份 每一份是 0.1%
		progressBar.setString((process / SPLIT) + " %");
	}
	
	public void setProcess(float processPercent) {
		setProcess((int) processPercent * MAX);
	}
	
	public void addProcess(float delta) {
		addProcess(delta, 1f);
	}
	
	public void addProcess(float delta, float ceilPercent) {
		addProcess(delta, 0f, 1f);
	}
	
	public void addProcess(float deltaPercent, float floorPercent, float ceilPercent) {
		checkCancel();
		int de = (int) (MAX * deltaPercent);
		if (de <= 0) {
			de = 1;
		}
		int i = process + de;
		if (i < floorPercent * MAX) {
			setProcess((int) (floorPercent * MAX));
		} else if (i <= ceilPercent * MAX) {
			setProcess(i);
		}
	}
	
	public void setTextMajor(String text) {
		checkCancel();
		label0.setText(text);
	}
	
	public void setTextBar(String text) {
		checkCancel();
		progressBar.setString(text);
	}
	
	public void setTextButton(String text) {
		checkCancel();
		button.setText(text);
	}
	
	public void setText(String text) {
		checkCancel();
		label.setText(text);
	}
	
	public float getProcessPercent() {
		return 1f * process / MAX;
	}
	
	public boolean isCancel() {
		return cancel;
	}
	
	public void hide() {
		frame.setVisible(false);
	}
	
	public void finished(File toOpen) {
		checkCancel();
		if (!toOpen.exists()) toOpen.mkdirs();
		if (!toOpen.isDirectory()) {
			finished(toOpen.getAbsoluteFile().getParentFile());
			return;
		}
		setProcess(MAX);
		label0.setHorizontalAlignment(SwingConstants.CENTER);
		label.setText("导出完成");
		button.setText("打开文件夹");
		frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		button.removeActionListener(CANCEL);
		button.addActionListener((s) -> {
			try {
				Desktop.getDesktop().open(toOpen);
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		});
	}
	
	public void invalidate() {
		button.setText("关闭");
		progressBar.setForeground(Color.RED);
		cancel = true;
		
		frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		button.removeActionListener(CANCEL);
		button.addActionListener((s) -> frame.dispose());
	}
	
	protected void cancel() {
		label.setText("已取消");
		invalidate();
	}
	
	protected void checkCancel() {
		var daemon = this.daemon;
		if (daemon != null) {
			if (!daemon.isAlive()) {
				cancel();
				label.setText("主线程死亡,详看日志");
				throw new RuntimeException("主线程死亡");
			}
		}
		if (cancel) {
			throw new ExportCancelledException();
		}
	}
	
	public void revalidate() {
		cancel = false;
	}
	
	public void close() {
		frame.setVisible(false);
		frame.dispose();
	}
	
	static class Empty extends ExportProcessFrame {
		
		public Empty() {
			super();
		}
		
		@Override
		public void setProcess(int process) {
		}
		
		@Override
		public void addProcess(float delta, float floorPercent, float ceilPercent) {
		}
		
		@Override
		public void addProcess(float delta) {
		}
		
		@Override
		public void setText(String text) {
		}
		
		@Override
		public void finished(File toOpen) {
		}
		
		@Override
		protected void cancel() {
		}
		
		@Override
		public void setProcess(float processPercent) {
		}
		
		@Override
		public void setTextMajor(String text) {
		}
		
		@Override
		public void setTextBar(String text) {
		}
		
		@Override
		public void invalidate() {
		}
		
		@Override
		public void close() {
		}
		
		@Override
		public void revalidate() {
		}
		
		@Override
		protected void checkCancel() {
		}
		
		@Override
		public boolean isCancel() {
			return false;
		}
		
		@Override
		public float getProcessPercent() {
			return 0f;
		}
		
		@Override
		public void setTextButton(String text) {
		}
		
		@Override
		public void addProcess(float delta, float ceilPercent) {
		}
		
		public Empty(String modid) {
			super(modid);
		}
		
		@Override
		public void show() {
			super.show();
		}
		
		@Override
		public void hide() {
			super.hide();
		}
	}
	
	public static void main(String[] args) {
		ExportProcessFrame frame1 = new ExportProcessFrame("minecraft");
		frame1.setText("<html><body><p align=\"center\">TEST1 test1<br/>v11.45.14</p></body></html>");
		frame1.show();
		var ref = new Object() {
			Timer timer;
		};
		ref.timer = new Timer(100, (e) -> {
			frame1.addProcess(0.1f);
			if (frame1.getProcessPercent() == 1f) {
				ref.timer.stop();
				frame1.finished(new File("."));
			}
		});
		ref.timer.start();
	}
	
}
