package cn.breadnicecat.reciperenderer.exporter.recipe;

import cn.breadnicecat.reciperenderer.api.IExporter;
import cn.breadnicecat.reciperenderer.exporter.recipe.serializer.SerializerManager.SerializeStatus;
import cn.breadnicecat.reciperenderer.utils.CallbackMessageBuilder;
import cn.breadnicecat.reciperenderer.utils.RRUtils;
import cn.breadnicecat.reciperenderer.utils.StageTimer;
import com.google.gson.JsonObject;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.DataResult;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.RecipeHolder;

import java.io.File;
import java.io.IOException;
import java.util.EnumMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import static cn.breadnicecat.reciperenderer.RecipeRenderer.exportDir;
import static cn.breadnicecat.reciperenderer.exporter.recipe.serializer.SerializerManager.serialize;
import static com.mojang.brigadier.Command.SINGLE_SUCCESS;
import static net.minecraft.ChatFormatting.*;
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
	
	public static final String ID = "recipe";
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
					StageTimer timer = new StageTimer();
					String namespace = StringArgumentType.getString(context, "namespace");
					Map<SerializeStatus, LinkedList<RecipeHolder<?>>> receptions = new EnumMap<>(SerializeStatus.class) {
						@Override
						public LinkedList<RecipeHolder<?>> get(Object key) {
							var v = super.get(key);
							if (v == null) {
								v = new LinkedList<>();
								put((SerializeStatus) key, v);
							}
							return v;
						}
					};
					List<JsonObject> out = context.getSource().getLevel().getRecipeManager().getOrderedRecipes()
							.stream()
							.filter(holder -> holder.id().getNamespace().equals(namespace))
							.map(holder -> {
								Pair<SerializeStatus, DataResult<JsonObject>> result = serialize(holder.id(), holder.value());
								receptions.get(result.getFirst()).add(holder);
								return result;
							})
							.map(Pair::getSecond)
							.filter(DataResult::isSuccess)
							.map(DataResult::getOrThrow)
							.toList();
					
					//写入文件
					File dir;
					try {
						dir = RRUtils.writeJsonResults(workingDir, namespace, context.getInput(), out, true).getParentFile();
					} catch (IOException e) {
						throw new RuntimeException("写入文件时发生了IO异常: " + e, e);
					}
					CallbackMessageBuilder.message()
							.appendCommandSource(this, context.getInput(), AQUA)
							.append(namespace, "", YELLOW, UNDERLINE)
							.append("导出完成", GREEN)
							.appendTimeUsed(timer)
							.appendResultCount("成功", "使用专用序列化器导出", receptions.get(SerializeStatus.BY_DUMPER), ChatFormatting.GREEN)
							.appendResultCount("兼容", "使用默认序列化器导出", receptions.get(SerializeStatus.BY_CODEC), ChatFormatting.AQUA)
							.appendResultCount("丢弃", "被标记为丢弃的配方，未导出", receptions.get(SerializeStatus.DISCARDED), YELLOW)
							.appendResultCount("失败", "导出失败，详情请查看日志", receptions.get(SerializeStatus.ERROR), ChatFormatting.RED)
							.appendMayOpenDir(dir)
							.buildAndSend();
					return SINGLE_SUCCESS;
				});
	}
	
	@Override
	public String getExporterName() {
		return ID;
	}
}
