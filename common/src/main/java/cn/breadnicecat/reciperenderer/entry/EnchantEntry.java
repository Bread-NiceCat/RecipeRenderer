package cn.breadnicecat.reciperenderer.entry;

import com.google.gson.JsonObject;
import net.minecraft.network.chat.Component;
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
	
	public String id;
	public int min;
	public int max;
	public String zh;
	public String en;
	
	public EnchantEntry(String id, Enchantment value) {
		this.id = id;
		this.value = value;
		min = value.getMinLevel();
		max = value.getMaxLevel();
		
	}
	
	@Override
	public Component getName() {
		return Component.translatable(value.getDescriptionId());
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
	public void store(JsonObject object) {
		object.addProperty("id", id);
		object.addProperty("en", en);
		object.addProperty("zh", zh);
		object.addProperty("min", min);
		object.addProperty("max", max);
	}
}
