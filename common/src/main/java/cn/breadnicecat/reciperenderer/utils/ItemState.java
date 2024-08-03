package cn.breadnicecat.reciperenderer.utils;

import net.minecraft.world.item.ItemStack;

import java.util.Objects;

/**
 * Created in 2024/8/3 上午10:10
 * Project: reciperenderer
 *
 * @author <a href="https://github.com/Bread-Nicecat">Bread_NiceCat</a>
 * <p>
 *
 * <p>
 **/
public class ItemState {
	public final ItemStack stack;
	
	public ItemState(ItemStack stack) {
		this.stack = stack;
	}
	
	@Override
	public final boolean equals(Object o) {
		if (this == o) return true;
		if (!(o instanceof ItemState itemState)) return false;
		return ItemStack.isSameItemSameTags(stack, itemState.stack);
	}
	
	@Override
	public int hashCode() {
		if (stack.isEmpty()) return 0;
		if (!stack.hasTag()) return stack.getItem().hashCode();
		return Objects.hash(stack.getItem(), stack.getOrCreateTag());
	}
}
