package cn.breadnicecat.reciperenderer;

import cn.breadnicecat.reciperenderer.utils.ExistHelper;
import cn.breadnicecat.reciperenderer.utils.RTimer;
import cn.breadnicecat.reciperenderer.worldly.ChunkContainer;
import cn.breadnicecat.reciperenderer.worldly.WorldlyContainer;
import cn.breadnicecat.reciperenderer.worldly.WorldlyExporterListener;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.status.ChunkStatus;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.concurrent.CancellationException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import static cn.breadnicecat.reciperenderer.RecipeRenderer.*;
import static cn.breadnicecat.reciperenderer.utils.CommonUtils.sleep;
import static java.lang.Math.sqrt;

/**
 * Created in 2024/8/22 22:40
 * Project: reciperenderer
 *
 * @author <a href="https://github.com/Bread-Nicecat">Bread_NiceCat</a>
 * <p>
 * 多线程
 * <p>
 **/
public class WorldlyExporter {
	public static File OUTPUT_DIR = Exporter.WORLDLY;
	private static ExistHelper NAME_HELPER = new ExistHelper(ExistHelper.fileBase(OUTPUT_DIR));
	public static final int SCAN_LIMIT_MAX = 3;
	static final Minecraft instance = Minecraft.getInstance();
	
	private WorldlyExporterListener monitor = WorldlyExporterListener.getDefault();
	/**
	 * true在运行
	 */
	private final AtomicBoolean state = new AtomicBoolean(false);
	public final int scanCount;
	public final WorldlyContainer container;
	private final ServerLevel level;
	public boolean paused;
	public RTimer rt;
	private final AtomicInteger workerCount = new AtomicInteger();
	
	
	public WorldlyExporter(ServerLevel level, int scanCount) {
		this.level = level;
		this.scanCount = scanCount;
		container = new WorldlyContainer(level, scanCount);
	}
	
	public void run() {
		if (state.get()) throw new IllegalStateException("该Exporter已经执行");
		state.set(true);
		rt = new RTimer();
		File file = new File(OUTPUT_DIR, NAME_HELPER.getModified("worldly-export-" + level.dimension().location().toString().replace(':', '_') + ".json"));
		LOGGER.info("开始Worldly扫描,采样数:{},输出到文件：{}", scanCount, file);
		file.getParentFile().mkdirs();
		try (var writer = Files.newBufferedWriter(file.toPath(), StandardCharsets.UTF_8)) {
			//阻塞渲染线程让效率更高
			RecipeRenderer.hookRenderer(() -> {
				instance.pauseGame(false);
				while (state.get()) {
					sleep(5000);
				}
			});
			//区块x的最大边界
//			int rectLen = 1000;
			int rectLen = (int) sqrt(scanCount);
			CompletableFuture<Void> future = CompletableFuture.completedFuture(null);
			//区块的加载必须由服务器线程进行，不然会死锁
			//开始扫描区块
			//i只用于计数,j用于记录区块的扫描值
			for (int i = 0, j = 0; state.get() && i < scanCount; i++) {
				if (i % 500 == 0) System.gc();
				//如果扫的数量超过预设值，就暂停
				//虽然收集的速度远远快于生成的速度，但是不排除服务器线程抢占时间片的情况
				while (paused || workerCount.get() > SCAN_LIMIT_MAX) {
					sleep(1000);
				}
				//由j确定需要扫描的区块的坐标
				int ix = j / rectLen;
				int iz = j % rectLen;
				//要求必须是没加载的
				while (level.hasChunk(ix, iz)) {
					j++;
					ix = j / rectLen;
					iz = j % rectLen;
				}
				
				final int chunkX = ix;
				final int chunkZ = iz;
				
				ExporterEntry chunkEntry = new ExporterEntry(i, chunkX, chunkZ);
				monitor.onGenerateChunk(chunkEntry);
				ChunkAccess chunk = level.getChunk(chunkX, chunkZ, ChunkStatus.FULL, true);
				monitor.onGenerateChunkDone(chunkEntry);
				chunkEntry.setAccess(chunk);
				workerCount.incrementAndGet();
				//委托给worker线程去收集
				CompletableFuture<Void> newFuture = CompletableFuture.runAsync(() -> {
					monitor.onScanChunkBegin(chunkEntry);
					for (int y = chunkEntry.minY(); y <= chunkEntry.maxY(); y++) {
						monitor.onScanLayerChanged(chunkEntry, y);
						int xst = chunkX << 4;
						int zst = chunkZ << 4;
						int sz = 16;//=区块尺寸(0b1111)+1
						//相当于
//					    	for (int x = 0; x < sz; x++) {
						for (int x = xst; x < xst + sz; x++) {
							for (int z = zst; z < zst + sz; z++) {
								chunkEntry.getChunkContainer().collect(y, chunk.getBlockState(new BlockPos(x, y, z)));
							}
						}
					}
					workerCount.decrementAndGet();
					monitor.onScanDone(chunkEntry);
					
				}, EXECUTOR).exceptionally(t -> {
					state.set(false);
					LOGGER.error("Worldly导出异常", t);
					monitor.onExportExceptionally(this, t);
					return null;
				});
				future = CompletableFuture.allOf(future, newFuture);
				j++;
			}
			
			future.get();
			PRETTY.toJson(container.toJson(), writer);
			PLAYER_LOGGER.info("导出完成，共扫描" + scanCount + "个区块，用时" + rt.getString());
			monitor.onDone(this);
			RecipeRenderer.open(OUTPUT_DIR);
		} catch (IOException e) {
			String msg = "写入文件异常: " + e;
			PLAYER_LOGGER.error(msg);
			RuntimeException exception = new RuntimeException(msg, e);
			monitor.onExportExceptionally(this, exception);
			throw exception;
		} catch (ExecutionException | RuntimeException e) {
			PLAYER_LOGGER.error("导出时遇到异常: " + e);
			throw new RuntimeException(e);
		} catch (InterruptedException e) {
			LOGGER.error("被迫中断 ", e);
		} finally {
			state.set(false);
		}
	}
	
	public void checkStatus() {
		if (!state.get()) throw new IllegalStateException("导出状态错误：false");
	}
	
	/**
	 * 留给可视化的监听器API
	 */
	public void addMonitor(WorldlyExporterListener monitor) {
		this.monitor = WorldlyExporterListener.merge(this.monitor, monitor);
	}
	
	public void cancel() {
		PLAYER_LOGGER.warn("用户取消");
		state.set(false);
		monitor.onExportExceptionally(this, new CancellationException("用户取消"));
	}
	
	public class ExporterEntry {
		public final int ordinal;
		public final int chunkX;
		public final int chunkZ;
		private ChunkAccess access;
		//该区块的收集器
		private final ChunkContainer chunkContainer;
		
		public ExporterEntry(int ordinal, int chunkX, int chunkZ) {
			this.ordinal = ordinal;
			this.chunkX = chunkX;
			this.chunkZ = chunkZ;
			this.chunkContainer = container.subChunkContainer();
		}
		
		public ChunkContainer getChunkContainer() {
			container.checkStatus();
			return chunkContainer;
		}
		
		public WorldlyExporter getParent() {
			return WorldlyExporter.this;
		}
		
		public int maxY() {
			return access.getMaxBuildHeight();
		}
		
		public int minY() {
			return access.getMinBuildHeight();
		}
		
		public int height() {
			return access.getHeight();
		}
		
		public ChunkAccess getAccess() {
			return access;
		}
		
		private void setAccess(ChunkAccess access) {
			this.access = access;
		}
	}
	
}
