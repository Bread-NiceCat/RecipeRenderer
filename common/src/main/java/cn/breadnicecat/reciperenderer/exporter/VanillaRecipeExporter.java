package cn.breadnicecat.reciperenderer.exporter;

import cn.breadnicecat.reciperenderer.RRExtension;
import cn.breadnicecat.reciperenderer.RecipeRenderer;
import cn.breadnicecat.reciperenderer.utils.RRUtils;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.JsonOps;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.item.crafting.RecipeSerializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.HashSet;
import java.util.List;

import static cn.breadnicecat.reciperenderer.utils.RRUtils.GSON;
import static java.nio.charset.StandardCharsets.UTF_8;
import static net.minecraft.ChatFormatting.GREEN;
import static net.minecraft.ChatFormatting.YELLOW;
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
public class VanillaRecipeExporter implements RRExtension {
	private static final Logger logger = LoggerFactory.getLogger(VanillaRecipeExporter.class);
	
	public static final String ID = "vanilla";
	public static final File workingDir = new File(RecipeRenderer.exportDir, ID);
	
	public VanillaRecipeExporter() {
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
					
					String namespace = StringArgumentType.getString(context, "namespace");
					logger.info("开始导出配方 {}:*", namespace);
					CommandSourceStack source = context.getSource();
					ServerLevel level = source.getLevel();
					RecipeManager manager = level.getRecipeManager();
					
					List<RecipeHolder<?>> list = manager.getOrderedRecipes().stream()
							.filter(h -> h.id().getNamespace().equals(namespace))
							.toList();
					int size = list.size();
					if (size == 0) {
						source.sendFailure(Component.literal("未搜索到配方"));
						return 0;
					}
					logger.info("共搜索到了{}条配方", size);
					workingDir.mkdirs();
					//由于文件系统不允许带'*'的文件
					File o = new File(workingDir, namespace + ".json");
					try (var writer = Files.newBufferedWriter(o.toPath(), UTF_8)) {
						writer.write("#" + RRUtils.getMetadata());
						//导出从以下开始
						HashSet<RecipeSerializer<?>> unknownSerializers = new HashSet<>();//防止同一个警告重复发送
						int success = 0, failed = 0;//计数
						for (RecipeHolder<?> holder : list) {
							ResourceLocation id = holder.id();
							Registry<RecipeSerializer<?>> serializers = level.registryAccess().registryOrThrow(Registries.RECIPE_SERIALIZER);
							RecipeSerializer<?> serializer = holder.value().getSerializer();
							ResourceLocation type = serializers.getKey(serializer);
							if (type == null) {
								if (unknownSerializers.add(serializer)) {
									logger.error("获取序列化器异常, 配方id={}", id);
									source.sendFailure(Component.literal("错误:未知的序列化器'%s'.将忽略所有该序列化器对应的配方!".formatted(serializer)));
								}
								failed++;
								continue;
							}
							DataResult<JsonElement> dataResult = serialize(holder.value());
							
							JsonObject json = new JsonObject();
							json.addProperty("type", type.toString());
							json.addProperty("id", id.toString());
							if (dataResult instanceof DataResult.Success<JsonElement> suc) {
								json.add("data", suc.value());
							}
							if (dataResult instanceof DataResult.Error<JsonElement> error) {
								logger.error("序列化配方异常:{}, id={}", error.message(), id);
								failed++;
								continue;
							}
							writer.newLine();
							writer.append(GSON.toJson(json));
							success++;
						}
						source.sendSystemMessage(Component.literal("导出完成,成功导出了%d条,失败%d条.用时%dms.".formatted(success, failed, System.currentTimeMillis() - stt))
								.withStyle(failed == 0 ? GREEN : YELLOW));
						RRUtils.open(workingDir);
					} catch (IOException e) {
						throw new RuntimeException("致命IO错误," + e, e);
					}
					return 1;
				});
	}
	
	@SuppressWarnings("unchecked")
	private <T extends Recipe<?>> DataResult<JsonElement> serialize(T recipe) {
		RecipeSerializer<T> serializer = (RecipeSerializer<T>) recipe.getSerializer();
		return serializer.codec().encoder().encodeStart(JsonOps.INSTANCE, recipe);
	}
}
