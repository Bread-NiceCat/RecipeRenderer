package cn.breadnicecat.reciperenderer.worldly;

import it.unimi.dsi.fastutil.ints.Int2IntAVLTreeMap;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

import java.util.HashMap;

/**
 * Created in 2024/9/8 01:12
 * Project: reciperenderer
 *
 * @author <a href="https://github.com/Bread-Nicecat">Bread_NiceCat</a>
 * <p>
 * 作为中间产物，只在扫描时使用
 * <p>
 **/
public class ChunkContainer {
	public final HashMap<Block, Int2IntAVLTreeMap> entries = new HashMap<>();
	public final Int2IntAVLTreeMap allBlockCount = new Int2IntAVLTreeMap();
	public final WorldlyContainer parent;
	
	public ChunkContainer(WorldlyContainer parent) {
		this.parent = parent;
	}
	
	/**
	 * 任何方块，包括空气都需要调用
	 */
	public void collect(int y, BlockState state) {
		parent.checkStatus();
		allBlockCount.addTo(y, 1);
		if (!state.isAir()) {
			entries.computeIfAbsent(state.getBlock(), block -> new Int2IntAVLTreeMap())
					.addTo(y, 1);
		}
	}
}
