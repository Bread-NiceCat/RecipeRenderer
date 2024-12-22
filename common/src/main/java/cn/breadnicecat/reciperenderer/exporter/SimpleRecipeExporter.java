package cn.breadnicecat.reciperenderer.exporter;

import cn.breadnicecat.reciperenderer.RecipeRenderer;
import cn.breadnicecat.reciperenderer.api.IExporter;
import cn.breadnicecat.reciperenderer.serializer.SerializerManager;
import cn.breadnicecat.reciperenderer.utils.RRUtils;
import com.google.gson.JsonObject;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.ArgumentBuilder;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.resources.ResourceLocation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Objects;

import static cn.breadnicecat.reciperenderer.RecipeRenderer.exportDir;
import static com.mojang.brigadier.Command.SINGLE_SUCCESS;
import static net.minecraft.commands.Commands.argument;

/**
 * Created in 2024/11/24 01:23
 * Project: reciperenderer
 *
 * @author <a href="https://github.com/Bread-Nicecat">Bread_NiceCat</a>
 * <p>
 *
 * <p>
 **/
public class SimpleRecipeExporter implements IExporter {
	private static final Logger logger = LoggerFactory.getLogger(SimpleRecipeExporter.class);
	
	public static final String ID = "simple";
	public static final File workingDir = exportDir;
	
	public SimpleRecipeExporter() {
	}
	
	@Override
	public ArgumentBuilder<CommandSourceStack, ?> buildCommand() {
		return argument("namespace", StringArgumentType.string())
				.suggests((context, builder) -> {
					context.getSource().getLevel().getRecipeManager().getRecipeIds()
							.map(ResourceLocation::getNamespace)
							.distinct()
							.forEach(builder::suggest);
					return builder.buildFuture();
				})
				.executes(context -> {
					long stt = System.currentTimeMillis();
					final int[] status = {0, 0};//success,fail
					String namespace = StringArgumentType.getString(context, "namespace");
					SerializerManager manager = RecipeRenderer.getSerializerManager();
					List<JsonObject> out = context.getSource().getLevel().getRecipeManager().getOrderedRecipes().stream()
							.filter(c -> c.id().getNamespace().equals(namespace))
							.map(r -> manager.serialize(r.id(), r.value()))
							.peek(c -> {
								if (c != null) status[0]++;
								else status[1]++;
							})
							.filter(Objects::nonNull)
							.toList();
					try {
						RRUtils.writeResult(workingDir, namespace, context.getInput(), out, false);
					} catch (IOException e) {
						throw new RuntimeException("写入文件时发生了IO异常: " + e, e);
					}
					context.getSource().sendSystemMessage(RRUtils.createFinishedMessage(stt, status[0], 0, status[1]));
					return SINGLE_SUCCESS;
				});
	}
	
	@Override
	public String getExporterName() {
		return "simple";
	}

//	@SuppressWarnings({"rawtypes", "unchecked"})
//	@Override
//	public ArgumentBuilder<CommandSourceStack, ?> buildCommand() {
//		return argument("namespace", StringArgumentType.string())
//				.suggests((context, builder) -> {
//					context.getSource().getLevel().getRecipeManager().getRecipeIds()
//							.map(ResourceLocation::getNamespace)
//							.distinct()
//							.forEach(builder::suggest);
//					return builder.buildFuture();
//				})
//				.executes(context -> {
//					long stt = System.currentTimeMillis();
//					String namespace = StringArgumentType.getString(context, "namespace");
//					CommandSourceStack source = context.getSource();
//
//					logger.info("开始导出配方 {}:*", namespace);
//					ServerLevel level = source.getLevel();
//					RecipeManager manager = level.getRecipeManager();
//
//					List<RecipeHolder<?>> list = manager.getOrderedRecipes().stream()
//							.filter(h -> h.id().getNamespace().equals(namespace))
//							.toList();
//					int size = list.size();
//					if (size == 0) {
//						source.sendFailure(Component.literal("未搜索到配方"));
//						return 0;
//					}
//					logger.info("共检索到了{}条配方", size);
//					workingDir.mkdirs();
//					File o = new File(workingDir, namespace + ".json");
//					try (var writer = Files.newBufferedWriter(o.toPath(), UTF_8)) {
//						writer.write("#" + getPlatform().getMetadata());
//						int success = 0, failed = 0;//计数
//						//导出从以下开始
//						//注:泛型无法捕获类,所以这里不使用泛型
//						for (RecipeHolder<?> holder : list) {
//							ResourceLocation id = holder.id();
//							Recipe recipe = holder.value();
//							try {
//								Encoder<Recipe> encoder = getSerializer(recipe);
//								String typeName;
//								if (encoder instanceof IRecipeTypeNameProvider np) {
//									typeName = np.getRecipeTypeName(recipe);
//								} else if (encoder instanceof RecipeSerializer<?> r) {
//									Registry<RecipeSerializer<?>> serializers = level.registryAccess().registryOrThrow(Registries.RECIPE_SERIALIZER);
//									ResourceLocation type = serializers.getKey(r);
//									if (type == null) {
//										throw new IllegalArgumentException("获取配方" + id + "对应的序列化器异常");
//									}
//									typeName = type.toString();
//								} else {
//									throw new IllegalStateException("编码器:" + encoder.getClass().getName() + "未提供typeName");
//								}
//
//								DataResult<JsonElement> dataResult = serialize(recipe, encoder);
//								JsonObject json = new JsonObject();
//								json.addProperty("id", id.toString());
//								json.addProperty("type", typeName);
//								JsonElement suc = dataResult.getOrThrow(msg -> new IllegalStateException("序列化配方" + id + "时遇到异常" + msg));
//								json.add("data", suc);
//								writer.newLine();
//								writer.append(GSON.toJson(json));
//								success++;
//							} catch (Exception e) {
//								logger.error("导出" + id + "时遇到异常:" + e, e);
//								failed++;
//							}
//						}
//						source.sendSystemMessage(Component.literal("导出完成,成功导出了%d条,失败%d条.用时%dms.".formatted(success, failed, System.currentTimeMillis() - stt))
//								.withStyle(failed == 0 ? GREEN : YELLOW));
//						RRUtils.open(workingDir);
//						return success;
//					} catch (IOException e) {
//						throw new RuntimeException("致命IO错误," + e, e);
//					}
//				});
//	}
//
//
//	@SuppressWarnings("unchecked")
//	private <T extends Recipe<?>> Encoder<T> getSerializer(T recipe) {
//		Optional<IRecipeDumper<?>> optional = dumpers.stream().filter(t -> t.isMatch(recipe)).findFirst();
//		return optional.map(dumper -> (Encoder<T>) dumper).orElseGet(() -> {
//
//			return ((RecipeSerializer<T>) recipe.getSerializer()).codec().encoder();
//		});
//	}
//
//	public <T extends Recipe<?>> DataResult<JsonElement> serialize(T recipe, Encoder<T> encoder) {
//		return encoder.encodeStart(JsonOps.INSTANCE, recipe);
//	}
}
