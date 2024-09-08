package cn.breadnicecat.reciperenderer.worldly;

import cn.breadnicecat.reciperenderer.RecipeRenderer;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import it.unimi.dsi.fastutil.ints.Int2FloatMap;
import it.unimi.dsi.fastutil.ints.Int2IntAVLTreeMap;
import it.unimi.dsi.fastutil.ints.Int2IntMap;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.Block;

import java.util.*;
import java.util.stream.Collectors;

import static cn.breadnicecat.reciperenderer.utils.CommonUtils.make;

/**
 * Created in 2024/8/30 20:08
 * Project: reciperenderer
 *
 * @author <a href="https://github.com/Bread-Nicecat">Bread_NiceCat</a>
 * <p>
 *
 * <p>
 **/
public class WorldlyContainer {
	public static final int VERSION = 1;
	
	private List<ChunkContainer> chunks = Collections.synchronizedList(new LinkedList<>());
	private Set<WorldlyBlockEntry> merged;
	
	public final ServerLevel level;
	public final int sample;
	
	private volatile boolean finished = false;
	
	public WorldlyContainer(ServerLevel level, int scanCount) {
		this.level = level;
		this.sample = scanCount;
	}
	
	private Set<WorldlyBlockEntry> merge() {
		checkStatus();
		if (!finished) setFinished();
		Registry<Block> blocks = level.registryAccess().registryOrThrow(Registries.BLOCK);
		HashMap<Block, WorldlyBlockEntry.Builder> entries = new HashMap<>();
		
		for (ChunkContainer chunk : chunks) {
			//block->(y,count)
			for (Map.Entry<Block, Int2IntAVLTreeMap> b2count : chunk.entries.entrySet()) {
				Block block = b2count.getKey();
				WorldlyBlockEntry.Builder builder = entries.computeIfAbsent(block, b -> {
					ResourceLocation key = Objects.requireNonNull(blocks.getKey(b));
					return new WorldlyBlockEntry.Builder(b, key);
				});
				//(block:) y->count
				for (Int2IntMap.Entry b2y : b2count.getValue().int2IntEntrySet()) {
					int y = b2y.getIntKey();
					int count = b2y.getIntValue();
					int all = chunk.allBlockCount.get(y);
					builder.append(y, count, all);
				}
			}
		}
		return entries.values()
				.stream()
				.sorted(Comparator.comparing(o -> o.id))
				.map(WorldlyBlockEntry.Builder::build)
				.collect(Collectors.toSet());
	}
	
	/**
	 * <pre>{
	 *     "format_version": 1,
	 *     "minecraft_version": ...
	 *     "dimension": 路径型字符串，该维度的注册名，
	 *     "sample": 整数，采样区块数量,
	 *     "entries": [
	 *         {   //这是单个条目
	 *             "id": 路径型字符串，方块的注册名,
	 *             "where": 见下文where格式，
	 *     [wip]   "loot":{ //特别注明：这里的loot是在1000次模拟掉落得出的数据，不一定符合所有情况
	 *     !          "silk_required" : 布尔值，精准采集是否掉落原方块
	 *     !          "drop" : 路径型字符串，物品的注册名,
	 *     !          "fortune" : [3] 长度为3的浮点数数组，单位：%，分别表示在时运I、II、III附魔下掉落的数量相对无时运附魔时的百分比值
	 *     !       }
	 *         },
	 *         ...
	 *     ]
	 * }
	 *
	 * </pre>
	 */
	public JsonObject toJson() {
		if (merged == null) merged = merge();
		JsonObject object = new JsonObject();
		object.addProperty("format_version", VERSION);
		object.addProperty("minecraft_version", RecipeRenderer.mcVersion);
		object.addProperty("dimension", level.dimension().location().toString());
		object.addProperty("sample", sample);
		object.add("entries", make(new JsonArray(), a -> {
			for (WorldlyBlockEntry entry : merged) {
				a.add(make(new JsonObject(), e -> {
					e.addProperty("id", entry.id().toString());
					e.addProperty("where", make(() -> {
						StringBuilder sb = new StringBuilder();
						float last = 0f;
						for (Int2FloatMap.Entry where : entry.where().int2FloatEntrySet()) {
							float prob = 100 * where.getFloatValue();
							if (prob != last) {
								sb.append(where.getIntKey()).append(':').append(prob).append(';');
							}
							last = prob;
						}
						return sb.toString();
					}));
				}));
			}
		}));
		return object;
	}
	
	
	public ChunkContainer subChunkContainer() {
		checkStatus();
		ChunkContainer container = new ChunkContainer(this);
		chunks.add(container);
		return container;
	}
	
	public void setFinished() {
		checkStatus();
		finished = true;
	}
	
	public void checkStatus() {
		if (finished) throw new IllegalStateException("Finished");
	}
	
	
}
