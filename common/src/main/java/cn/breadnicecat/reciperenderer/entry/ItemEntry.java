package cn.breadnicecat.reciperenderer.entry;

import cn.breadnicecat.reciperenderer.render.Icon;
import cn.breadnicecat.reciperenderer.render.IconWrapper;
import com.google.gson.JsonObject;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
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
public class ItemEntry implements Localizable, Storable {
	public final ItemStack stack;
	
	public int stackSize;
	public int durability;
	public ResourceLocation id;
	public String[] tags;
	public String en;
	public String zh;
	
	public IconWrapper ico32;
	public IconWrapper ico128;
	
	public ItemEntry(ResourceLocation id, ItemStack stack) {
		this.stack = stack;
		this.id = id;
		ico32 = new IconWrapper((pose) -> new Icon(pose, 32, stack));
		ico128 = new IconWrapper((pose) -> new Icon(pose, 128, stack));
		stackSize = stack.getMaxStackSize();
		durability = stack.getMaxDamage();
		tags = stack.getTags().map(i -> i.location().toString()).toArray(String[]::new);
	}
	
	
	@Override
	public Component getName() {
		return stack.getHoverName();
	}
	
	@Override
	public void setZh(String zh) {
		this.zh = zh;
	}
	
	@Override
	public void setEn(String en) {
		this.en = en;
	}
	
	@Override
	public int store(BiFunction<String, byte @Nullable [], String> writer, JsonObject object) {
		object.addProperty("id", id.toString());
		object.addProperty("en", en);
		object.addProperty("zh", zh);
		object.addProperty("tags", Arrays.toString(tags));
		object.addProperty("stackSize", stackSize);
		object.addProperty("durability", durability);
		object.addProperty("ico32", writer.apply("attachment/ico32/" + id.getPath() + ".png", ico32.getBytesBlocking()));
		object.addProperty("ico128", writer.apply("attachment/ico128/" + id.getPath() + ".png", ico128.getBytesBlocking()));
		return 1;
	}
}
