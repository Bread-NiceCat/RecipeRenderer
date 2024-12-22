package cn.breadnicecat.reciperenderer.serializer;

import cn.breadnicecat.reciperenderer.api.IRecipeSerializer;
import cn.breadnicecat.reciperenderer.serializer.serializers.CodecSerializer;
import cn.breadnicecat.reciperenderer.serializer.serializers.DumperSerializer;
import cn.breadnicecat.reciperenderer.utils.Location;
import com.google.gson.JsonObject;
import com.mojang.serialization.DataResult;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.Recipe;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedList;

/**
 * Created in 2024/12/22 01:10
 * Project: reciperenderer
 *
 * @author <a href="https://github.com/Bread-Nicecat">Bread_NiceCat</a>
 * <p>
 *
 * <p>
 **/
public class SerializerManager {
	
	private static final Logger logger = LoggerFactory.getLogger(SerializerManager.class);
	private LinkedList<IRecipeSerializer> serializerSet = new LinkedList<>();
	/**
	 * 需要被舍弃的配方
	 */
	private HashSet<Class<? extends Recipe<?>>> desperate = new HashSet<>();
	
	{
		registerSerializer(new DumperSerializer());
		registerSerializer(new CodecSerializer());
	}
	
	public @Nullable JsonObject serialize(ResourceLocation id, Recipe<?> recipe) {
		logger.info("开始序列化配方:{}", id);
		var c = recipe.getClass();
		if (desperate.contains(c)) return null;
		for (IRecipeSerializer serializer : serializerSet) {
			DataResult<JsonObject> result = serializer.serialize(id, recipe);
			if (result instanceof DataResult.Error<JsonObject> error) {
				logger.error("{}序列化失败,原因:{}", serializer.getSerializerName(), error.message());
			} else if (result instanceof DataResult.Success<JsonObject> success) {
				return success.value();
			}
		}
		return null;
	}
	
	public final void registerSerializer(IRecipeSerializer serializer) {
		registerSerializer(serializer, Location.last());
	}
	
	public final void registerSerializer(IRecipeSerializer serializer, Location<Class<? extends IRecipeSerializer>> location) {
		Location.LocationType type = location.type();
		switch (type) {
			case FIRST -> serializerSet.addFirst(serializer);
			case LAST -> serializerSet.addLast(serializer);
			case AFTER, BEFORE -> {
				for (int i = 0; i < serializerSet.size(); i++) {
					Class<? extends IRecipeSerializer> s1 = serializerSet.get(i).getClass();
					if (s1.equals(location.arg())) {
						if (type == Location.LocationType.AFTER) {
							serializerSet.add(i + 1, serializer);
						} else {
							serializerSet.add(i, serializer);
						}
					}
				}
			}
		}
	}
	
	/**
	 * 被remove的配方类不会被序列化
	 */
	@SafeVarargs
	public final <R extends Recipe<?>> void registerDesperateRecipe(Class<? extends R>... targets) {
		desperate.addAll(Arrays.asList(targets));
	}
}
