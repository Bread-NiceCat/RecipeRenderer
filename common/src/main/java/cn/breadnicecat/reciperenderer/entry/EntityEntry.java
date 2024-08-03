package cn.breadnicecat.reciperenderer.entry;

import cn.breadnicecat.reciperenderer.render.EntityIcon;
import cn.breadnicecat.reciperenderer.render.IconWrapper;
import cn.breadnicecat.reciperenderer.utils.ExistHelper;
import cn.breadnicecat.reciperenderer.utils.ExportLogger;
import com.google.gson.JsonObject;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import org.jetbrains.annotations.Nullable;

import java.util.function.BiFunction;

/**
 * Created in 2024/7/9 上午12:26
 * Project: reciperenderer
 *
 * @author <a href="https://github.com/Bread-Nicecat">Bread_NiceCat</a>
 * <p>
 *
 * <p>
 **/
public class EntityEntry implements Localizable, Storable {
	final EntityType<?> entityType;
	final Entity entity;
	
	public IconWrapper ico128;
	public ResourceLocation id;
	public String zh;
	public String en;
	
	public EntityEntry(ResourceLocation id, LivingEntity entity) {
		this.entityType = entity.getType();
		this.id = id;
		ico128 = new IconWrapper((pose) -> new EntityIcon(pose, 128, entity));
		this.entity = entity;
	}
	
	@Override
	public int store(ExistHelper existHelper, BiFunction<String, byte @Nullable [], String> writer, JsonObject object, ExportLogger logger) {
		object.addProperty("id", id.toString());
		object.addProperty("zh", zh);
		object.addProperty("en", en);
		object.addProperty("ico", writer.apply(existHelper.getModified("attachment/entity/" + id.getPath() + ".png"), ico128.getBytesBlocking(logger)));
		return 1;
	}
	
	
	@Override
	public Component getName() {
		return entityType.getDescription();
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
