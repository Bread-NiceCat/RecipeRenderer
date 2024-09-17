package cn.breadnicecat.reciperenderer.gui.screens;

import cn.breadnicecat.reciperenderer.WorldlyExporter;
import cn.breadnicecat.reciperenderer.utils.RTimer;
import cn.breadnicecat.reciperenderer.worldly.WorldlyExporterListener;
import oshi.util.tuples.Triplet;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ItemEvent;
import java.util.LinkedList;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static cn.breadnicecat.reciperenderer.gui.ExportFrame.debug;
import static cn.breadnicecat.reciperenderer.utils.CommonUtils.make;
import static java.awt.BorderLayout.*;

/**
 * Created in 2024/8/22 23:13
 * Project: reciperenderer
 *
 * @author <a href="https://github.com/Bread-Nicecat">Bread_NiceCat</a>
 * <p>
 *
 * <p>
 **/
public class WorldlyProgressScreen extends Screen implements WorldlyExporterListener {
	
	private final WorldlyExporter exporter;
	private final JLabel board;
	private final JPanel barPanel;
	private boolean ran = false;
	private final Dimension dimension = new Dimension(256, 256);
	private final Map<WorldlyExporter.ExporterEntry, Triplet<JProgressBar, JLabel, JPanel>> bars = new ConcurrentHashMap<>();
	private final Triplet<JProgressBar, JLabel, JPanel> genBar;
	private final Timer updateTimer = new Timer(100, this::tick);
	private float cps = 0;
	
	private int tickCount = 0;
	
	private void tick(ActionEvent actionEvent) {
		if (tickCount % 10 == 0) repaint();
		setBoard(exporter.rt);
	}
	
	private RTimer formater = new RTimer(true);
	private boolean memoryTipped = false;
	
	private void setBoard(RTimer rt) {
		Runtime runtime = Runtime.getRuntime();
		formater.setTime((long) (cps * (exporter.scanCount - genBar.getA().getValue())));
		board.setText("用时%s,剩余%s,内存%d/%dMB".formatted(rt.getClockString(),
				formater.getClockString(),
				(runtime.totalMemory() - runtime.freeMemory()) / 1024L / 1024L,
				runtime.totalMemory() / 1024L / 1024L));
		if (!memoryTipped && runtime.freeMemory() / 1024 / 1024 < 50) {
			JOptionPane.showMessageDialog(this, "警告：内存分配过小，将严重影响导出速度！建议的分配大小为500MB每2000区块", "警告", JOptionPane.WARNING_MESSAGE);
			memoryTipped = true;
		}
	}
	
	public WorldlyProgressScreen(WorldlyExporter exporter) {
		this.exporter = exporter;
		if (!debug) exporter.addMonitor(this);

//		setLayout(new FlowLayout());
//		setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
		setLayout(new BorderLayout());
		
		board = new JLabel("用时N/A");
		board.setHorizontalTextPosition(SwingConstants.LEFT);
		add(board, NORTH);
		
		barPanel = new JPanel();
		barPanel.setLayout(new BoxLayout(barPanel, BoxLayout.Y_AXIS));

//		barPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 0, 0));
//		barPanel.setLayout(new GridLayout(1, 5));
		genBar = createProcessBar("区块生成器,待机");
		{
			JProgressBar bar = genBar.getA();
			bar.setMaximum(debug ? 100 : exporter.scanCount);
		}
		barPanel.add(genBar.getC());
		add(barPanel, CENTER);
		
		add(make(new JPanel(), p -> {
			p.setLayout(new FlowLayout(FlowLayout.CENTER));
			JToggleButton pause = new JToggleButton("暂停");
			pause.addItemListener(c -> {
				boolean v = c.getStateChange() == ItemEvent.SELECTED;
				exporter.setPaused(v);
				if (v) {
					pause.setForeground(Color.RED);
				} else {
					pause.setForeground(Color.BLACK);
				}
			});
			JButton stop = new JButton("取消");
			stop.addActionListener(c -> {
				int i = JOptionPane.showConfirmDialog(p, "确定取消？", "警告", JOptionPane.YES_NO_OPTION);
				if (i == JOptionPane.YES_OPTION) {
					exporter.cancel();
				}
			});
			p.add(pause);
			p.add(stop);
		}), SOUTH);
		if (debug) {
			barPanel.add(createProcessBar("test").getC());
		}
	}
	
	private static LinkedList<Triplet<JProgressBar, JLabel, JPanel>> objectPool = new LinkedList<>();
	
	private Triplet<JProgressBar, JLabel, JPanel> createProcessBar(String title) {
		Triplet<JProgressBar, JLabel, JPanel> obj = objectPool.isEmpty() ? null : objectPool.removeFirst();
		if (obj == null) {
			JProgressBar bar = new JProgressBar();
			JLabel label = new JLabel(title);
			var p = new JPanel();
			p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
//		p.setLayout(new FlowLayout(FlowLayout.LEADING));
			p.add(label);
			p.add(bar);
			return new Triplet<>(bar, label, p);
		} else {
			obj.getB().setText(title);
			return obj;
		}
	}
	
	private synchronized void add(WorldlyExporter.ExporterEntry source, Triplet<JProgressBar, JLabel, JPanel> t) {
		bars.put(source, t);
		barPanel.add(t.getC());
	}
	
	private synchronized void remove(WorldlyExporter.ExporterEntry source) {
		Triplet<JProgressBar, JLabel, JPanel> triplet = bars.remove(source);
		if (triplet != null) {
			barPanel.remove(triplet.getC());
			objectPool.push(triplet);
		}
	}
	
	@Override
	public void onEnable() {
		if (!ran && !debug) {
			ran = true;
			frame.setScreenLocked(true);
			frame.setAlwaysOnTop(true);
			updateTimer.start();
			exporter.run();
		}
	}
	
	@Override
	public void onDisable() {
		updateTimer.stop();
		frame.setAlwaysOnTop(false);
	}
	
	private void free() {
		frame.setScreenLocked(false);
		frame.free();
	}
	
	@Override
	public Dimension getScreenSize() {
		return dimension;
	}
	
	private final RTimer chunkTimer = new RTimer();
	
	@Override
	public synchronized void onGenerateChunk(WorldlyExporter.ExporterEntry source) {
		chunkTimer.reset();
		JProgressBar bar = genBar.getA();
		genBar.getB().setText("区块生成器,chunkX=" + source.chunkX + ",chunkZ=" + source.chunkZ);
		bar.setString((source.ordinal + 1) + " / " + source.getParent().scanCount);
	}
	
	@Override
	public synchronized void onGenerateChunkDone(WorldlyExporter.ExporterEntry source) {
		long l = chunkTimer.get();
		float cpsP = l != 0 ? 1000f / l : 1000f;
		cps = cps * 0.4f + cpsP * 0.6f;
		JProgressBar bar = genBar.getA();
		genBar.getB().setText("区块生成器,暂停");
		bar.setValue(source.ordinal + 1);
	}
	
	@Override
	public synchronized void onScanChunkBegin(WorldlyExporter.ExporterEntry source) {
		Triplet<JProgressBar, JLabel, JPanel> triplet = createProcessBar("区块扫描,chunkX=" + source.chunkX + ",chunkZ=" + source.chunkZ);
		
		JProgressBar bar = triplet.getA();
//		bar.setMaximum(source.height());
		bar.setIndeterminate(true);
		add(source, triplet);
	}
	
	@Override
	public void onScanLayerChanged(WorldlyExporter.ExporterEntry source, int y) {
	}
	
	@Override
	public synchronized void onScanDone(WorldlyExporter.ExporterEntry source) {
		remove(source);
	}
	
	@Override
	public synchronized void onDone(WorldlyExporter exporter) {
		JOptionPane.showMessageDialog(this, "导出完成", "共扫描" + exporter.scanCount + "个区块，用时" + exporter.rt.getString(), JOptionPane.INFORMATION_MESSAGE);
		free();
	}
	
	@Override
	public synchronized void onExportExceptionally(WorldlyExporter source, Throwable t) {
		JOptionPane.showMessageDialog(this, t.toString(), "导出异常", JOptionPane.ERROR_MESSAGE);
		free();
	}
	
}
