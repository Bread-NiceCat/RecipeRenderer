package cn.breadnicecat.reciperenderer.entry;

import cn.breadnicecat.reciperenderer.render.IconWrapper;
import cn.breadnicecat.reciperenderer.render.ItemIcon;
import cn.breadnicecat.reciperenderer.utils.ExistHelper;
import cn.breadnicecat.reciperenderer.utils.ExportLogger;
import cn.breadnicecat.reciperenderer.utils.ItemState;
import com.google.gson.JsonObject;
import com.mojang.serialization.JsonOps;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.io.Closeable;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.function.BiFunction;

/**
 * Created in 2024/7/8 下午10:53
 * Project: reciperenderer
 *
 * @author <a href="https://github.com/Bread-Nicecat">Bread_NiceCat</a>
 * <p>
 *
 * <p>
 **/
public class ItemEntry implements LocalizableV2, StorableV2, Closeable {
	public final ItemStack stack;
	
	public int stackSize;
	public int durability;
	public ResourceLocation id;
	public String[] tags;
	public String en;
	public String zh;
	public CompoundTag nbt;
	public IconWrapper ico32;
	public IconWrapper ico128;
	//
	public int nutrition;
	public float saturation;
	//
	public LinkedList<CreativeModeTab> tabs = new LinkedList<>();
	public LinkedList<String> tabNames = new LinkedList<>();
	
	public ItemEntry(ResourceLocation id, ItemState state) {
		this.stack = state.stack;
		this.id = id;
		ico32 = new IconWrapper((pose) -> new ItemIcon(pose, 32, stack));
		ico128 = new IconWrapper((pose) -> new ItemIcon(pose, 128, stack));
		stackSize = stack.getMaxStackSize();
		durability = stack.getMaxDamage();
		tags = stack.getTags().map(i -> i.location().toString()).toArray(String[]::new);
		nbt = stack.hasTag() ? state.stack.getTag() : new CompoundTag();
		FoodProperties properties = stack.getItem().getFoodProperties();
		if (properties != null) {
			//nutrition 饱食度
			//saturation 饱和度 # 饱和度=2*饱食度*饱和度修饰符,这里已经进行转化
			nutrition = properties.getNutrition();
			saturation = 2 * nutrition * properties.getSaturationModifier();
		}
	}
	
	@Override
	public void localizeZh() {
		zh = stack.getHoverName().getString();
		tabs.forEach(i -> tabNames.add(i.getDisplayName().getString()));
	}
	
	@Override
	public void localizeEn() {
		en = stack.getHoverName().getString();
	}
	
	@Override
	public int store(ExistHelper existHelper, BiFunction<String, byte @Nullable [], String> writer, JsonObject object, ExportLogger logger) {
		object.addProperty("id", id.toString());
		object.addProperty("en", en);
		object.addProperty("zh", zh);
		object.addProperty("tabs", tabNames.toString());
		object.addProperty("tags", Arrays.toString(tags));
		object.addProperty("stackSize", stackSize);
		object.addProperty("durability", durability);
		object.addProperty("nbt", CompoundTag.CODEC.encodeStart(JsonOps.INSTANCE, nbt).get().orThrow().toString());
		object.addProperty("nut", nutrition);
		object.addProperty("sat", saturation);
		object.addProperty("ico32", writer.apply(existHelper.getModified("attachment/ico32/" + id.getPath() + ".png"), ico32.getBytesBlocking(logger)));
		object.addProperty("ico128", writer.apply(existHelper.getModified("attachment/ico128/" + id.getPath() + ".png"), ico128.getBytesBlocking(logger)));
		return 3;
	}
	
	@Override
	public void close() {
		ico32.clear();
		ico128.clear();
	}
}
