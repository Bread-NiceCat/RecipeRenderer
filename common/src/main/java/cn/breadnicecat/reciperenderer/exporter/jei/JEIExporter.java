package cn.breadnicecat.reciperenderer.exporter.jei;


import cn.breadnicecat.reciperenderer.api.IExporter;
import cn.breadnicecat.reciperenderer.utils.RRUtils;
import com.google.gson.JsonObject;
import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.ArgumentBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.helpers.IJeiHelpers;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.category.IRecipeCategory;
import mezz.jei.api.runtime.IJeiRuntime;
import mezz.jei.common.Internal;
import mezz.jei.common.gui.elements.DrawableNineSliceTexture;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.resources.ResourceLocation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ExecutionException;

import static cn.breadnicecat.reciperenderer.RecipeRenderer.exportDir;
import static net.minecraft.commands.Commands.argument;

/**
 * Created in 2024/11/24 09:49
 * Project: reciperenderer
 *
 * @author <a href="https://github.com/Bread-Nicecat">Bread_NiceCat</a>
 * <p>
 *
 * <p>
 **/
public class JEIExporter implements IExporter {
	
	private static final Logger logger = LoggerFactory.getLogger(JEIExporter.class);
	
	public JEIExporter() {
	}
	
	@Override
	public ArgumentBuilder<CommandSourceStack, ?> buildCommand() {
		return argument("namespace", StringArgumentType.string())
				.suggests(((context, builder) -> {
					getJEIRuntime().ifPresent(runtime -> {
						runtime.getJeiHelpers().getAllRecipeTypes()
								.map(RecipeType::getUid)
								.map(ResourceLocation::getNamespace)
								.distinct()
								.forEach(builder::suggest);
					});
					return builder.buildFuture();
				}))
				.then(argument("path", StringArgumentType.string())
						.suggests(((context, builder) -> {
							getJEIRuntime().ifPresent(runtime -> {
								String namespace = StringArgumentType.getString(context, "namespace");
								runtime.getJeiHelpers().getAllRecipeTypes()
										.filter(r -> r.getUid().getNamespace().equals(namespace))
										.forEach(r -> {
											ResourceLocation uid = r.getUid();
											String path = uid.getPath();
											try {
												IRecipeCategory<?> category = runtime.getRecipeManager().getRecipeCategory(r);
												builder.suggest(path, category.getTitle());
											} catch (Throwable t) {
												logger.error("获取JEI分栏标题时遇到异常 uid=" + r.getUid(), t);
												builder.suggest(path);
											}
										});
							});
							return builder.buildFuture();
						}))
						.executes(context -> {
							long stt = System.currentTimeMillis();
							
							File workingDir = RRUtils.createNewResultDir(exportDir);
							
							String namespace = StringArgumentType.getString(context, "namespace");
							String path = StringArgumentType.getString(context, "path");
							
							IJeiRuntime runtime = getJEIRuntimeOrThrow();
							ResourceLocation uid = ResourceLocation.fromNamespaceAndPath(namespace, path);
							IJeiHelpers helpers = runtime.getJeiHelpers();
							
							File resDir = new File(workingDir, uid.getNamespace() + "/" + uid.getPath());
							resDir.mkdirs();
							JsonObject object = export(resDir, runtime, helpers.getRecipeType(uid).orElseThrow(() -> new IllegalArgumentException("无效的配方类型")));
							try {
								RRUtils.writeJsonResults(workingDir, "result", context.getInput(), List.of(object), false);
							} catch (IOException e) {
								throw new RuntimeException("写入result错误:" + e, e);
							}
							RRUtils.hookClientTick((mc) -> {
								mc.gui.getChat().addMessage(RRUtils.createFinishedMessage(stt, 1, 0, 0));
								mc.gui.getChat().addMessage(RRUtils.createMayOpenMessage("[打开目录]", workingDir));
							});
							
							return 1;
						}))
				.executes(context -> {
					long stt = System.currentTimeMillis();
					File workingDir = RRUtils.createNewResultDir(exportDir);
					
					String namespace = StringArgumentType.getString(context, "namespace");
					IJeiRuntime runtime = getJEIRuntimeOrThrow();
					int[] status = {0, 0, 0};
					List<JsonObject> results = runtime.getJeiHelpers().getAllRecipeTypes()
							.filter(r -> r.getUid().getNamespace().equals(namespace))
							.map(r -> {
								ResourceLocation uid = r.getUid();
								try {
									File resDir = new File(workingDir, uid.getNamespace() + "/" + uid.getPath());
									resDir.mkdirs();
									return export(resDir, runtime, r);
								} catch (Exception e) {
									logger.error("JEI专栏导出错误: uid=" + uid, e);
									return null;
								}
							})
							.peek(o -> {
								if (o == null) status[2]++;
								else status[0]++;
							})
							.filter(Objects::nonNull)
							.toList();
					try {
						RRUtils.writeJsonResults(workingDir, "result", context.getInput(), results, false);
					} catch (IOException e) {
						throw new RuntimeException("写入result错误:" + e, e);
					}
					RRUtils.hookClientTick((mc) -> {
						mc.gui.getChat().addMessage(RRUtils.createFinishedMessage(stt, status[0], status[1], status[2]));
						mc.gui.getChat().addMessage(RRUtils.createMayOpenMessage("[打开目录]", workingDir));
					});
					return 1;
				});
	}
	
	@Override
	public String getExporterName() {
		return "jei";
	}
	
	/**
	 * @see mezz.jei.library.gui.recipes.RecipeLayout#DEFAULT_RECIPE_BORDER_PADDING
	 */
	public static final int DEFAULT_RECIPE_BORDER_PADDING = 4;
	
	
	private JsonObject export(File workingDir, IJeiRuntime runtime, RecipeType<?> recipeType) {
		IRecipeCategory<?> category = runtime.getRecipeManager().getRecipeCategory(recipeType);
		//绑定贴图
		byte[] bg, bg_raw, ico;
		try {
			bg_raw = RRUtils.render(category.getWidth(), category.getHeight(), graphics -> {
				IDrawable background = category.getBackground();
				background.draw(graphics);
			}, NativeImage::flipY).get();
			
			int border = DEFAULT_RECIPE_BORDER_PADDING;
			int width = category.getWidth() + 2 * border;
			int height = category.getHeight() + 2 * border;
			bg = RRUtils.render(width, height, graphics -> {
				DrawableNineSliceTexture recipeBackground = Internal.getTextures().getRecipeBackground();
				IDrawable background = category.getBackground();
				recipeBackground.draw(graphics, new Rect2i(0, 0, width, height));
				background.draw(graphics, border, border);
			}, NativeImage::flipY).get();
			
			IDrawable iconDrawable = category.getIcon();
			ico = iconDrawable != null
					? RRUtils.render(iconDrawable.getWidth(), iconDrawable.getHeight(), iconDrawable::draw, NativeImage::flipY).get()
					: null;
			
		} catch (InterruptedException | ExecutionException e) {
			throw new RuntimeException("渲染异常: " + e, e);
		}
		try {
			Files.write(new File(workingDir, "bg_raw.png").toPath(), bg_raw);
			Files.write(new File(workingDir, "bg.png").toPath(), bg);
			if (ico != null) {
				Files.write(new File(workingDir, "ico.png").toPath(), ico);
			}
			
			JsonObject object = new JsonObject();
			object.addProperty("uid", category.getRecipeType().getUid().toString());
			object.addProperty("title", category.getTitle().getString());
			object.addProperty("bg", RRUtils.base64(bg));
//			object.addProperty("bg_raw", RRUtils.base64(bg_raw));
			object.addProperty("ico", ico == null ? "" : RRUtils.base64(ico));
			return object;
		} catch (IOException e) {
			throw new RuntimeException("输出结果时发生了异常: " + e, e);
		}
	}
	
	private static IJeiRuntime getJEIRuntimeOrThrow() {
		return getJEIRuntime().orElseThrow(() -> new IllegalStateException("无法访问JEI插件"));
	}
	
	private static Optional<IJeiRuntime> getJEIRuntime() {
		return Optional.ofNullable(JEIPlugin.INSTANCE)
				.map(i -> i.runtime);
	}
	
}