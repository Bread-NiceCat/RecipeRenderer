package cn.breadnicecat.reciperenderer.api.dumper;

import cn.breadnicecat.reciperenderer.exporter.recipe.serializer.impl.RecipeInputImpl;
import cn.breadnicecat.reciperenderer.exporter.recipe.serializer.impl.RecipeOutputImpl;
import cn.breadnicecat.reciperenderer.utils.RRUtils;
import com.google.gson.JsonObject;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeType;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

/**
 * @author youyihj
 * @author Bread_NiceCat
 */
public interface IRecipeDumper<R extends Recipe<?>> {
	
	
	void setInputs(R recipe, IRecipeInputs inputs);
	
	default void setOutputs(@NotNull R recipe, IRecipeOutputs outputs, RegistryAccess registryAccess) {
		outputs.addOutput(1, recipe.getResultItem(registryAccess));
	}
	
	default void writeExtraInformation(R recipe, JsonObject object) {
	}
	
	default String getRecipeType(R recipe) {
		RecipeType<?> type = recipe.getType();
		return Optional.ofNullable(RRUtils.registryAccessor().registryOrThrow(Registries.RECIPE_TYPE).getKey(type))
				.map(ResourceLocation::toString)
				.orElseGet(type::toString);
	}
	
	default void serialize(R recipe, JsonObject data) {
		RecipeInputImpl rin = new RecipeInputImpl();
		RecipeOutputImpl rout = new RecipeOutputImpl();
		setInputs(recipe, rin);
		setOutputs(recipe, rout, RRUtils.registryAccessor());
		data.add("input", rin.serialize());
		data.add("output", rout.serialize());
		writeExtraInformation(recipe, data);
	}
}
