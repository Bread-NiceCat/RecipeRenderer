package cn.breadnicecat.reciperenderer.entry;

import com.google.gson.JsonObject;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffect;
import org.jetbrains.annotations.Nullable;

import java.util.function.BiFunction;

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
	
	public byte[] ico;
	public ResourceLocation id;
	public String zh;
	public String en;
	
	public EffectEntry(ResourceLocation id, MobEffect effect) {
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
	
	public void setIco(byte[] ico) {
		this.ico = ico;
	}
	
	@Override
	public int store(BiFunction<String, byte @Nullable [], String> writer, JsonObject object) {
		object.addProperty("id", id.toString());
		object.addProperty("en", en);
		object.addProperty("zh", zh);
		object.addProperty("ico", writer.apply("attachment/effect/" + id.getPath() + ".png", ico));
		return 1;
	}
	
}
