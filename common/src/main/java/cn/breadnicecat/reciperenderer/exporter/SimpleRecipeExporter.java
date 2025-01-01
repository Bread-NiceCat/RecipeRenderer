package cn.breadnicecat.reciperenderer.exporter;

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
					final int[] status = {0, 0, 0};//success,warn,fail
					String namespace = StringArgumentType.getString(context, "namespace");
					List<JsonObject> out = context.getSource().getLevel().getRecipeManager().getOrderedRecipes().stream()
							.filter(c -> c.id().getNamespace().equals(namespace))
							.filter(c -> !SerializerManager.isDiscarded(c.value()))
							.map(r -> SerializerManager.serialize(r.id(), r.value()))
							.peek(c -> {
								if (c != null) status[0]++;
								else status[2]++;
							})
							.filter(Objects::nonNull)
							.toList();
					File dir;
					try {
						dir = RRUtils.writeJsonResults(workingDir, namespace, context.getInput(), out, true);
					} catch (IOException e) {
						throw new RuntimeException("写入文件时发生了IO异常: " + e, e);
					}
					RRUtils.hookClientTick((mc) -> {
						mc.gui.getChat().addMessage(RRUtils.createFinishedMessage(stt, status[0], status[1], status[2]));
						mc.gui.getChat().addMessage(RRUtils.createMayOpenMessage("[打开目录]", dir));
					});
					return SINGLE_SUCCESS;
				});
	}
	
	@Override
	public String getExporterName() {
		return ID;
	}
}
