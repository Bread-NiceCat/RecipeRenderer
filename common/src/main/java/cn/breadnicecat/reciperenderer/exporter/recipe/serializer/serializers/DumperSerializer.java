package cn.breadnicecat.reciperenderer.exporter.recipe.serializer.serializers;

import cn.breadnicecat.reciperenderer.api.IRecipeSerializer;
import cn.breadnicecat.reciperenderer.api.dumper.IRecipeDumper;
import cn.breadnicecat.reciperenderer.utils.RRUtils;
import com.google.gson.JsonObject;
import com.mojang.serialization.DataResult;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.Recipe;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;

/**
 * Created in 2024/12/22 03:40
 * Project: reciperenderer
 *
 * @author <a href="https://github.com/Bread-Nicecat">Bread_NiceCat</a>
 * <p>
 *
 * <p>
 **/
public class DumperSerializer implements IRecipeSerializer {
	//	private static ClassRelationMap<Recipe<?>, IRecipeDumper<?>> dumpers = new ClassRelationMap<>();
	private static HashMap<Class<? extends Recipe<?>>, IRecipeDumper<?>> dumpers = new HashMap<>();
	
	
	@SafeVarargs
	public static <R extends Recipe<?>> void registerRecipeDumper(IRecipeDumper<R> dumper, Class<? extends R>... targets) {
		for (Class<? extends R> ts : targets) {
			dumpers.put(ts, dumper);
		}
	}
	
	@SuppressWarnings("unchecked")
	public static <R extends Recipe<?>> @Nullable IRecipeDumper<R> getDumper(Class<? extends R> c) {
		return (IRecipeDumper<R>) dumpers.get(c);
	}
	
	@SuppressWarnings({"rawtypes", "unchecked"})
	@Override
	public @Nullable DataResult<JsonObject> serialize(ResourceLocation id, Recipe<?> recipe) {
		var dumper = (IRecipeDumper) getDumper(recipe.getClass());
		JsonObject data = new JsonObject();
		if (dumper != null) {
			dumper.serialize(recipe, data);
			String rt = dumper.getRecipeType(recipe);
			return DataResult.success(RRUtils.createSerializedRecipe(id, getSerializerName(), rt, data));
		}
		return DataResult.error(() -> "未找到对应的Dumper:" + recipe.getClass());
	}
	
	@Override
	public String getSerializerName() {
		return "dumper";
	}
}
