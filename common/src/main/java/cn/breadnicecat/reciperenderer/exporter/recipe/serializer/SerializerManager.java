package cn.breadnicecat.reciperenderer.exporter.recipe.serializer;

import cn.breadnicecat.reciperenderer.api.IRecipeSerializer;
import cn.breadnicecat.reciperenderer.exporter.recipe.serializer.serializers.CodecSerializer;
import cn.breadnicecat.reciperenderer.exporter.recipe.serializer.serializers.DumperSerializer;
import com.google.gson.JsonObject;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.DataResult;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.Recipe;
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
	/**
	 * 需要被舍弃的配方
	 */
	private static HashSet<Class<? extends Recipe<?>>> discard = new HashSet<>();
	
	private static final DumperSerializer DUMPER = new DumperSerializer();
	private static final CodecSerializer CODEC = new CodecSerializer();
	private static final LinkedList<IRecipeSerializer> serializerSet = new LinkedList<>();
	
	static {
		serializerSet.addFirst(DUMPER);
		serializerSet.addLast(CODEC);
	}
	
	public enum SerializeStatus {
		BY_DUMPER, BY_CODEC, DISCARDED, ERROR
		
	}
	
	public static Pair<SerializeStatus, DataResult<JsonObject>> serialize(ResourceLocation id, Recipe<?> recipe) {
		logger.info("开始序列化配方:{}", id);
		var c = recipe.getClass();
		if (discard.contains(c)) {
			logger.warn("被放弃的配方,跳过导出");
			return Pair.of(SerializeStatus.DISCARDED, DataResult.error(() -> "被放弃的配方类型类:" + c));
		}
		
		for (IRecipeSerializer serializer : serializerSet) {
			DataResult<JsonObject> result;
			
			try {
				result = serializer.serialize(id, recipe);
			} catch (Throwable e) {
				logger.error("导出过程中遇到异常", e);
				result = DataResult.error(e::toString);
				//这里等统一处理
			}
			
			if (result instanceof DataResult.Error<JsonObject> error) {
				logger.error("{}序列化失败,原因:{}", serializer.getSerializerName(), error.message());
				//此序列器不可用,继续使用下一个
			} else if (result instanceof DataResult.Success<JsonObject> success) {
				SerializeStatus status;
				if (serializer == CODEC) {
					status = SerializeStatus.BY_CODEC;
				} else {
					status = SerializeStatus.BY_DUMPER;
				}
				return Pair.of(status, success);
			}
		}
		
		return Pair.of(SerializeStatus.ERROR, null);
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
	
	@SafeVarargs
	public static <R extends Recipe<?>> void rescuedDiscardedRecipeType(Class<? extends R>... targets) {
		Arrays.asList(targets).forEach(discard::remove);
	}

//	public static void registerSerializer(@MagicConstant(intValues = {HIGH, MIDDLE, LOW})
//										  int priority, IRecipeSerializer serializer) {
//		serializerSets.get(priority).add(serializer);
//	}
	
}
