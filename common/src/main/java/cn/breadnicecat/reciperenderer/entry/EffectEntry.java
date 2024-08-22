package cn.breadnicecat.reciperenderer.entry;

import cn.breadnicecat.reciperenderer.utils.ExistHelper;
import cn.breadnicecat.reciperenderer.utils.ExportLogger;
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
public class EffectEntry implements StorableV2, Localizable {
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
	
	@Override
	public int store(ExistHelper existHelper, BiFunction<String, byte @Nullable [], String> writer, JsonObject object, ExportLogger logger) {
		object.addProperty("id", id.toString());
		object.addProperty("en", en);
		object.addProperty("zh", zh);
		object.addProperty("ico", writer.apply(existHelper.getModified("attachment/effect/" + id.getPath() + ".png"), ico));
		return 1;
	}
	
}
