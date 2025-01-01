package cn.breadnicecat.reciperenderer.serializer;

import cn.breadnicecat.reciperenderer.api.IRecipeSerializer;
import cn.breadnicecat.reciperenderer.serializer.serializers.CodecSerializer;
import cn.breadnicecat.reciperenderer.serializer.serializers.DumperSerializer;
import cn.breadnicecat.reciperenderer.utils.RRUtils;
import com.google.gson.JsonObject;
import com.mojang.serialization.DataResult;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.*;
import org.intellij.lang.annotations.MagicConstant;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
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
	/**
	 * 需要被舍弃的配方
	 */
	private static HashSet<Class<? extends Recipe<?>>> discard = new HashSet<>();
	private static final ArrayList<LinkedList<IRecipeSerializer>> serializerSets = RRUtils.createArray(3, i -> new LinkedList<>());
	public static final int HIGH = 0, MIDDLE = 1, LOW = 2;
	
	static {
		registerSerializer(MIDDLE, new DumperSerializer());
		registerSerializer(LOW, new CodecSerializer());
		discardRecipeType(SmithingTrimRecipe.class,
				SuspiciousStewRecipe.class,
				FireworkStarRecipe.class,
				BookCloningRecipe.class,
				BannerDuplicateRecipe.class,
				FireworkStarFadeRecipe.class,
				FireworkRocketRecipe.class,
				ArmorDyeRecipe.class,
				MapCloningRecipe.class,
				RepairItemRecipe.class,
				ShulkerBoxColoring.class,
				DecoratedPotRecipe.class,
				ShieldDecorationRecipe.class,
				TippedArrowRecipe.class
		);
	}
	
	public static @Nullable JsonObject serialize(ResourceLocation id, Recipe<?> recipe) {
		logger.info("开始序列化配方:{}", id);
		var c = recipe.getClass();
		if (discard.contains(c)) {
			logger.warn("被放弃的配方,跳过导出");
			return null;
		}
		for (LinkedList<IRecipeSerializer> serializerSet : serializerSets) {
			for (IRecipeSerializer serializer : serializerSet) {
				DataResult<JsonObject> result;
				try {
					result = serializer.serialize(id, recipe);
				} catch (Exception e) {
					logger.error("导出时遇到异常", e);
					result = DataResult.error(e::toString);
				}
				if (result instanceof DataResult.Error<JsonObject> error) {
					logger.error("{}序列化失败,原因:{}", serializer.getSerializerName(), error.message());
				} else if (result instanceof DataResult.Success<JsonObject> success) {
					return success.value();
				}
			}
		}
		logger.error("导出失败");
		return null;
	}
	
	public static boolean isDiscarded(Recipe<?> recipe) {
		return discard.contains(recipe.getClass());
	}
	
	/**
	 * 被discard的配方类不会被序列化
	 */
	@SafeVarargs
	public static <R extends Recipe<?>> void discardRecipeType(Class<? extends R>... targets) {
		discard.addAll(Arrays.asList(targets));
	}
	
	public static void registerSerializer(@MagicConstant(intValues = {HIGH, MIDDLE, LOW})
										  int priority, IRecipeSerializer serializer) {
		serializerSets.get(priority).add(serializer);
	}
	
}
