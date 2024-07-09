package cn.breadnicecat.reciperenderer.entry;

import com.google.gson.JsonObject;
import net.minecraft.network.chat.Component;
import net.minecraft.world.effect.MobEffect;

/**
 * Created in 2024/7/9 下午11:01
 * Project: reciperenderer
 *
 * @author <a href="https://github.com/Bread-Nicecat">Bread_NiceCat</a>
 * <p>
 *
 * <p>
 **/
public class EffectEntry implements Storable, Localizable {
	final MobEffect effect;
	public byte[] icoRaw;
	public String ico;
	public String id;
	
	public String zh;
	public String en;
	
	public EffectEntry(String id, MobEffect effect) {
		this.effect = effect;
		this.id = id;
	}
	
	@Override
	public Component getName() {
		return effect.getDisplayName();
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
		object.addProperty("ico", ico);
	}
}
