package cn.breadnicecat.reciperenderer.worldly;

import cn.breadnicecat.reciperenderer.RecipeRenderer;
import cn.breadnicecat.reciperenderer.WorldlyExporter;

/**
 * Created in 2024/8/30 20:08
 * Project: reciperenderer
 *
 * @author <a href="https://github.com/Bread-Nicecat">Bread_NiceCat</a>
 * <p>
 *
 * <p>
 **/
public interface WorldlyExporterListener {
	
	/**
	 * 开始生成
	 */
	void onGenerateChunk(WorldlyExporter.ExporterEntry source);
	
	/**
	 * 生成完成
	 */
	void onGenerateChunkDone(WorldlyExporter.ExporterEntry source);
	
	/**
	 * 开始扫描
	 */
	void onScanChunkBegin(WorldlyExporter.ExporterEntry source);
	
	/**
	 * y变了
	 */
	void onScanLayerChanged(WorldlyExporter.ExporterEntry source, int y);
	
	/**
	 * 扫描完成
	 */
	void onScanDone(WorldlyExporter.ExporterEntry source);
	
	/**
	 * 导出完成
	 */
	void onDone(WorldlyExporter exporter);
	
	/**
	 * 导出异常
	 */
	void onExportExceptionally(WorldlyExporter source, Throwable t);
	
	default void remove(WorldlyExporterListener o) {
	}
	
	static WorldlyExporterListener merge(WorldlyExporterListener o1, WorldlyExporterListener o2) {
		if (o1 == o2) {
			throw new IllegalStateException("o1==o2");
		}
		if (o1 == NULL) {
			return o2;
		} else if (o2 == NULL) {
			return o1;
		}
		return new Union(o1, o2);
	}
	
	WorldlyExporterListener LOGGER = new WorldlyExporterListener() {
		@Override
		public void onGenerateChunk(WorldlyExporter.ExporterEntry source) {
			RecipeRenderer.LOGGER.debug("开始生成区块:chunkX={}, chunkZ={}", source.chunkX, source.chunkZ);
		}
		
		@Override
		public void onGenerateChunkDone(WorldlyExporter.ExporterEntry source) {
			RecipeRenderer.LOGGER.debug("区块生成完成:chunkX={}, chunkZ={}", source.chunkX, source.chunkZ);
		}
		
		@Override
		public void onScanChunkBegin(WorldlyExporter.ExporterEntry source) {
			RecipeRenderer.LOGGER.debug("开始扫描区块:chunkX={}, chunkZ={}", source.chunkX, source.chunkZ);
		}
		
		@Override
		public void onScanLayerChanged(WorldlyExporter.ExporterEntry source, int y) {
//			RecipeRenderer.LOGGER.info("正在扫描区块:chunkX={}, chunkZ={},y={}", source.chunkX, source.chunkZ, y);
		}
		
		@Override
		public void onScanDone(WorldlyExporter.ExporterEntry source) {
			RecipeRenderer.LOGGER.debug("区块扫描完成:chunkX={}, chunkZ={}", source.chunkX, source.chunkZ);
		}
		
		@Override
		public void onDone(WorldlyExporter exporter) {
			RecipeRenderer.LOGGER.info("导出完成，用时{}", exporter.rt);
		}
		
		@Override
		public void onExportExceptionally(WorldlyExporter source, Throwable t) {
			RecipeRenderer.LOGGER.error("Worldly导出错误", t);
		}
	};
	WorldlyExporterListener RUNTIME = new WorldlyExporterListener() {
		@Override
		public void onGenerateChunk(WorldlyExporter.ExporterEntry source) {
			source.getParent().checkStatus();
		}
		
		@Override
		public void onGenerateChunkDone(WorldlyExporter.ExporterEntry source) {
			source.getParent().checkStatus();
		}
		
		@Override
		public void onScanChunkBegin(WorldlyExporter.ExporterEntry source) {
			source.getParent().checkStatus();
		}
		
		@Override
		public void onScanLayerChanged(WorldlyExporter.ExporterEntry source, int y) {
			source.getParent().checkStatus();
		}
		
		@Override
		public void onScanDone(WorldlyExporter.ExporterEntry source) {
			source.getParent().checkStatus();
		}
		
		@Override
		public void onDone(WorldlyExporter exporter) {
		}
		
		@Override
		public void onExportExceptionally(WorldlyExporter source, Throwable t) {
		}
	};
	WorldlyExporterListener NULL = new WorldlyExporterListener() {
		@Override
		public void onGenerateChunk(WorldlyExporter.ExporterEntry source) {
		
		}
		
		@Override
		public void onGenerateChunkDone(WorldlyExporter.ExporterEntry source) {
		
		}
		
		@Override
		public void onScanChunkBegin(WorldlyExporter.ExporterEntry source) {
		
		}
		
		@Override
		public void onScanLayerChanged(WorldlyExporter.ExporterEntry source, int y) {
		
		}
		
		@Override
		public void onScanDone(WorldlyExporter.ExporterEntry source) {
		
		}
		
		@Override
		public void onDone(WorldlyExporter exporter) {
		
		}
		
		@Override
		public void onExportExceptionally(WorldlyExporter source, Throwable t) {
		}
	};
	
	
	static WorldlyExporterListener getDefault() {
		return RecipeRenderer.DEV ? merge(RUNTIME, LOGGER) : RUNTIME;
	}
	
	final class Union implements WorldlyExporterListener {
		private WorldlyExporterListener o1;
		private WorldlyExporterListener o2;
		
		public Union(WorldlyExporterListener o1, WorldlyExporterListener o2) {
			this.o1 = o1;
			this.o2 = o2;
		}
		
		@Override
		public void onGenerateChunk(WorldlyExporter.ExporterEntry source) {
			o1.onGenerateChunk(source);
			o2.onGenerateChunk(source);
		}
		
		@Override
		public void onGenerateChunkDone(WorldlyExporter.ExporterEntry source) {
			o1.onGenerateChunkDone(source);
			o2.onGenerateChunkDone(source);
		}
		
		@Override
		public void onScanChunkBegin(WorldlyExporter.ExporterEntry source) {
			o1.onScanChunkBegin(source);
			o2.onScanChunkBegin(source);
		}
		
		@Override
		public void onScanLayerChanged(WorldlyExporter.ExporterEntry source, int y) {
			o1.onScanLayerChanged(source, y);
			o2.onScanLayerChanged(source, y);
		}
		
		@Override
		public void onScanDone(WorldlyExporter.ExporterEntry source) {
			o1.onScanDone(source);
			o2.onScanDone(source);
		}
		
		@Override
		public void onDone(WorldlyExporter source) {
			o1.onDone(source);
			o2.onDone(source);
		}
		
		@Override
		public void onExportExceptionally(WorldlyExporter source, Throwable t) {
			o1.onExportExceptionally(source, t);
			o2.onExportExceptionally(source, t);
		}
		
		@Override
		public void remove(WorldlyExporterListener o) {
			if (o1 == o) {
				o1 = NULL;
			} else if (o2 == o) {
				o2 = NULL;
			}
			o1.remove(o);
			o2.remove(o);
		}
	}
}
