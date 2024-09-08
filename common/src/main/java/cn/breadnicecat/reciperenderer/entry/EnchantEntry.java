package cn.breadnicecat.reciperenderer.entry;

import cn.breadnicecat.reciperenderer.utils.ExportLogger;
import com.google.gson.JsonObject;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.enchantment.Enchantment;

/**
 * Created in 2024/7/10 下午3:02
 * Project: reciperenderer
 *
 * @author <a href="https://github.com/Bread-Nicecat">Bread_NiceCat</a>
 * <p>
 *
 * <p>
 **/
public class EnchantEntry implements Localizable, Storable {
	final Enchantment value;
	
	public ResourceLocation id;
	public int min;
	public int max;
	public String zh;
	public String en;
	
	public EnchantEntry(ResourceLocation id, Enchantment value) {
		this.id = id;
		this.value = value;
		min = value.getMinLevel();
		max = value.getMaxLevel();
	}
	
	@Override
	public Component getName() {
		return value.description();
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
	public int store(JsonObject object, ExportLogger logger) {
		object.addProperty("id", id.toString());
		object.addProperty("en", en);
		object.addProperty("zh", zh);
		object.addProperty("min", min);
		object.addProperty("max", max);
		return 2;
	}
}
