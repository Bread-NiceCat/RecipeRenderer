package cn.breadnicecat.reciperenderer.gui.screens;

import cn.breadnicecat.reciperenderer.WorldlyExporter;
import cn.breadnicecat.reciperenderer.utils.RTimer;
import cn.breadnicecat.reciperenderer.worldly.WorldlyExporterListener;
import oshi.util.tuples.Triplet;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ItemEvent;
import java.util.HashMap;

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
	private Dimension dimension = new Dimension(256, 256);
	private final HashMap<WorldlyExporter.ExporterEntry, Triplet<JProgressBar, JLabel, JPanel>> bars = new HashMap<>();
	private final Triplet<JProgressBar, JLabel, JPanel> genBar;
	private final Timer updateTimer = new Timer(50, this::tick);
	
	private void tick(ActionEvent actionEvent) {
		setTime(exporter.rt);
	}
	
	private void setTime(RTimer rt) {
		Runtime runtime = Runtime.getRuntime();
		board.setText("用时：" + rt.getString() + ",内存:" + ((runtime.totalMemory() - runtime.freeMemory()) / 1024L / 1024L) + "MB/" + (runtime.totalMemory() / 1024L / 1024L) + "MB");
	}
	
	public WorldlyProgressScreen(WorldlyExporter exporter) {
		this.exporter = exporter;
		if (!debug) exporter.addMonitor(this);

//		setLayout(new FlowLayout());
//		setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
		setLayout(new BorderLayout());
		
		board = new JLabel("用时：N/A");
		board.setHorizontalTextPosition(SwingConstants.LEFT);
		add(board, NORTH);
		
		barPanel = new JPanel();
		barPanel.setLayout(new BoxLayout(barPanel, BoxLayout.Y_AXIS));

//		barPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 0, 0));
//		barPanel.setLayout(new GridLayout(1, 5));
		genBar = createProcessBar("区块生成器：待机");
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
				exporter.paused = v;
				if (v) {
					pause.setForeground(Color.RED);
				} else {
					pause.setForeground(Color.GRAY);
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
			add((WorldlyExporter.ExporterEntry) null, createProcessBar("test"));
		}
	}
	
	private Triplet<JProgressBar, JLabel, JPanel> createProcessBar(String title) {
		JProgressBar bar = new JProgressBar(0, 100);
		JLabel label = new JLabel(title);
		var p = new JPanel();
		p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
//		p.setLayout(new FlowLayout(FlowLayout.LEADING));
		p.add(label);
		p.add(bar);
		
		bar.setStringPainted(true);
		return new Triplet<>(bar, label, p);
	}
	
	private synchronized void add(WorldlyExporter.ExporterEntry source, Triplet<JProgressBar, JLabel, JPanel> t) {
		bars.put(source, t);
		barPanel.add(t.getC());
	}
	
	private synchronized void remove(WorldlyExporter.ExporterEntry source) {
		Triplet<JProgressBar, JLabel, JPanel> triplet = bars.remove(source);
		if (triplet != null) {
			barPanel.remove(triplet.getC());
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
	
	@Override
	public void onGenerateChunk(WorldlyExporter.ExporterEntry source) {
		JProgressBar bar = genBar.getA();
		bar.setIndeterminate(false);
		genBar.getB().setText("区块生成器：chunkX=" + source.chunkX + ",chunkZ=" + source.chunkZ);
		bar.setString((source.ordinal + 1) + " / " + source.getParent().scanCount);
	}
	
	@Override
	public void onGenerateChunkDone(WorldlyExporter.ExporterEntry source) {
		JProgressBar bar = genBar.getA();
		bar.setIndeterminate(true);
		bar.setValue(source.ordinal + 1);
		genBar.getB().setText("区块生成器：暂停");
	}
	
	@Override
	public void onScanChunkBegin(WorldlyExporter.ExporterEntry source) {
		Triplet<JProgressBar, JLabel, JPanel> triplet = createProcessBar("区块扫描,chunkX=" + source.chunkX + ",chunkZ=" + source.chunkZ);
		
		JProgressBar bar = triplet.getA();
		bar.setMinimum(source.minY());
		bar.setMaximum(source.maxY());
		add(source, triplet);
	}
	
	@Override
	public void onScanLayerChanged(WorldlyExporter.ExporterEntry source, int y) {
		JProgressBar bar = bars.get(source).getA();
		bar.setValue(y);
		bar.setString(y + " / " + source.maxY());
	}
	
	int i = 0;
	
	@Override
	public void onScanDone(WorldlyExporter.ExporterEntry source) {
		if (++i % 6 == 0) {
			repaint();
		}
		remove(source);
	}
	
	@Override
	public void onDone(WorldlyExporter exporter) {
		JOptionPane.showMessageDialog(this, "导出完成", "共扫描" + exporter.scanCount + "个区块，用时" + exporter.rt.getString(), JOptionPane.INFORMATION_MESSAGE);
		free();
	}
	
	@Override
	public void onExportExceptionally(WorldlyExporter source, Throwable t) {
		JOptionPane.showMessageDialog(this, t.toString(), "导出异常", JOptionPane.ERROR_MESSAGE);
		free();
	}
}
