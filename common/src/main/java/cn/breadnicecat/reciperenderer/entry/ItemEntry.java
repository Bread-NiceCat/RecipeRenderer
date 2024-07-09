package cn.breadnicecat.reciperenderer.entry;

import cn.breadnicecat.reciperenderer.Icon;
import com.google.gson.JsonObject;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

import java.util.Arrays;

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
	public Icon ico32;
	public Icon ico128;
	public int durability;
	public String id;
	public String[] tags;
	public String en;
	public String zh;
	
	public ItemEntry(ResourceLocation id, ItemStack stack) {
		this.stack = stack;
		this.id = id.toString();
		ico32 = new Icon(32, stack);
		ico128 = new Icon(128, stack);
		stackSize = stack.getMaxStackSize();
		durability = stack.getMaxDamage();
		tags = stack.getTags().map(i -> i.location().toString()).toArray(String[]::new);
	}
	
	@Override
	public void store(JsonObject object) {
		object.addProperty("id", id);
		object.addProperty("en", en);
		object.addProperty("zh", zh);
		object.addProperty("tags", Arrays.toString(tags));
		object.addProperty("stackSize", stackSize);
		object.addProperty("durability", durability);
		object.addProperty("ico32", ico32.getBase64());
		object.addProperty("ico128", ico128.getBase64());
		
	}

//	void storeAsIconR(JsonObject object) {
//		object.addProperty("name", zh);
//		object.addProperty("englishName", en);
//		object.addProperty("registerName", id);
//		object.addProperty("CreativeTabName", (tabIn));
//		object.addProperty("Oredictlist", Arrays.toString(tags));
//		object.addProperty("maxstacksSize", stackSize);
//		object.addProperty("maxDurability", durability);
//		object.addProperty("smallIcon", ico32.getBase64());
//		object.addProperty("largeIcon", ico128.getBase64());
//	}
	
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
}
