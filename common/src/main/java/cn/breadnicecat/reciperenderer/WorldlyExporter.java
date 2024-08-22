package cn.breadnicecat.reciperenderer;

import net.minecraft.client.Minecraft;
import net.minecraft.world.level.ChunkPos;

import java.io.File;

/**
 * Created in 2024/8/22 22:40
 * Project: reciperenderer
 *
 * @author <a href="https://github.com/Bread-Nicecat">Bread_NiceCat</a>
 * <p>
 *
 * <p>
 **/
public class WorldlyExporter {
	
	public ExporterListener monitor;
	public static File OUTPUT_DIR = Exporter.WORLDLY;
	public static Minecraft instance = Minecraft.getInstance();
	
	
	public final int scanCount;
	
	public WorldlyExporter(int scanCount) {
		this.scanCount = scanCount;
	}
	
	public static int getChunkX(long chunkPos) {
		return ChunkPos.getX(chunkPos);
	}
	
	public static int getChunkY(long chunkPos) {
		return ChunkPos.getZ(chunkPos);
	}
	
	public class ExporterEntry {
		private int chunkX;
		private int chunkZ;
		private int y;
		
		public WorldlyExporter getParent() {
			return WorldlyExporter.this;
		}
		
		public int getChunkX() {
			return chunkX;
		}
		
		public int getChunkZ() {
			return chunkZ;
		}
		
		public int getY() {
			return y;
		}
	}
	
	public interface ExporterListener {
		
		void onGenerateChunk(ExporterEntry source);
		
		void onScanChunkChanged(ExporterEntry source);
		
		void onScanLayerChanged(ExporterEntry source);
		
		void onScanCompleted(ExporterEntry source);
	}
}
