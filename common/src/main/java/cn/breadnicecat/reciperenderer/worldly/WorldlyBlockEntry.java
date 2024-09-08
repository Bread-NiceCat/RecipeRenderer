package cn.breadnicecat.reciperenderer.worldly;

import cn.breadnicecat.reciperenderer.utils.ModifiableIntIntPair;
import it.unimi.dsi.fastutil.ints.Int2FloatAVLTreeMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectAVLTreeMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;

import java.util.Comparator;
import java.util.Objects;

/**
 * Created in 2024/8/30 20:26
 * Project: reciperenderer
 *
 * @author <a href="https://github.com/Bread-Nicecat">Bread_NiceCat</a>
 * <p>
 *
 * <p>
 **/
public record WorldlyBlockEntry(Block block, ResourceLocation id, Int2FloatAVLTreeMap where) {
	
	public static class Builder {
		private final Block block;
		public final ResourceLocation id;
		/**
		 * y->(count,allInY)
		 */
		private Int2ObjectAVLTreeMap<ModifiableIntIntPair> allCounts = new Int2ObjectAVLTreeMap<>(Comparator.reverseOrder());
		boolean finished = false;
		
		public Builder(Block block, ResourceLocation id) {
			this.block = block;
			this.id = id;
		}
		
		
		public void append(int y, int count, int allInY) {
			checkStatus();
			ModifiableIntIntPair ca = allCounts.computeIfAbsent(y, i -> new ModifiableIntIntPair());
			ca.addLeft(count);
			ca.addRight(allInY);
		}
		
		private void checkStatus() {
			if (finished) throw new IllegalStateException("Finished");
		}
		
		private void setFinished() {
			finished = true;
		}
		
		public WorldlyBlockEntry build() {
			setFinished();
			Int2FloatAVLTreeMap where = new Int2FloatAVLTreeMap();
			for (Int2ObjectMap.Entry<ModifiableIntIntPair> entry : allCounts.int2ObjectEntrySet()) {
				ModifiableIntIntPair value = entry.getValue();
				where.put(entry.getIntKey(), 1f * value.getLeft() / value.getRight());
			}
			return new WorldlyBlockEntry(block, id, where);
		}
		
		@Override
		public final boolean equals(Object o) {
			if (this == o) return true;
			if (!(o instanceof Builder builder)) return false;
			
			return Objects.equals(id, builder.id);
		}
		
		@Override
		public int hashCode() {
			return Objects.hashCode(id);
		}
	}
	
}
