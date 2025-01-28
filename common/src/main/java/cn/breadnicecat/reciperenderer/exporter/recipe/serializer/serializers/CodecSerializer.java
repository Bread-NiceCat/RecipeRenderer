package cn.breadnicecat.reciperenderer.exporter.recipe.serializer.serializers;

import cn.breadnicecat.reciperenderer.api.IRecipeSerializer;
import cn.breadnicecat.reciperenderer.utils.RRUtils;
import com.google.gson.JsonObject;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.JsonOps;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Optional;
import java.util.function.Function;

/**
 * Created in 2024/12/22 04:24
 * Project: reciperenderer
 *
 * @author <a href="https://github.com/Bread-Nicecat">Bread_NiceCat</a>
 * <p>
 *
 * <p>
 **/
public class CodecSerializer implements IRecipeSerializer {
	private static HashMap<Class<? extends Recipe<?>>, Function<JsonObject, JsonObject>> modifiers = new HashMap<>();
	
	@SuppressWarnings({"rawtypes", "unchecked"})
	@Override
	public @Nullable DataResult<JsonObject> serialize(ResourceLocation id, Recipe recipe) {
		RecipeSerializer<Recipe<?>> serializer = recipe.getSerializer();
		String rt = Optional.ofNullable(RRUtils.registryAccessor().registryOrThrow(Registries.RECIPE_SERIALIZER).getKey(serializer))
				.map(ResourceLocation::toString)
				.orElseThrow(() -> new IllegalStateException("无法获取codec序列器的注册表id,也许尚未注册?"));
		try {
			DataResult<JsonObject> result = (DataResult) serializer.codec().encoder().encodeStart(JsonOps.INSTANCE, recipe);
			JsonObject data = result.getOrThrow();
			Function<JsonObject, JsonObject> modifier = modifiers.get(recipe.getClass());
			if (modifier != null) {
				try {
					data = modifier.apply(data);
				} catch (Exception e) {
					return DataResult.error(() -> "在修饰时发生了错误");
				}
			}
			
			return DataResult.success(RRUtils.createSerializedRecipe(id, getSerializerName(), rt, data));
		} catch (Exception e) {
			return DataResult.error(() -> "在序列化时发生了错误:" + e);
		}
	}
	
	@SafeVarargs
	public static <R extends Recipe<?>> void registerResultModifier(Function<JsonObject, JsonObject> modifier, Class<? extends R>... targets) {
		for (Class<? extends R> ts : targets) {
			modifiers.put(ts, modifier);
		}
	}
	
	@Override
	public String getSerializerName() {
		return "codec";
	}
}
