package cn.breadnicecat.reciperenderer.exporter.jei;


import cn.breadnicecat.reciperenderer.api.IExporter;
import cn.breadnicecat.reciperenderer.utils.CallbackMessageBuilder;
import cn.breadnicecat.reciperenderer.utils.RRUtils;
import cn.breadnicecat.reciperenderer.utils.StageTimer;
import com.google.gson.JsonObject;
import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import it.unimi.dsi.fastutil.objects.ObjectBooleanImmutablePair;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.helpers.IJeiHelpers;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.IRecipeLookup;
import mezz.jei.api.recipe.IRecipeManager;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.category.IRecipeCategory;
import mezz.jei.api.runtime.IJeiRuntime;
import mezz.jei.common.Internal;
import mezz.jei.common.gui.elements.DrawableBlank;
import mezz.jei.common.gui.elements.DrawableNineSliceTexture;
import mezz.jei.common.util.ImmutableRect2i;
import mezz.jei.common.util.StringUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.resources.ResourceLocation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ExecutionException;
import java.util.stream.Stream;

import static cn.breadnicecat.reciperenderer.RecipeRenderer.exportDir;
import static net.minecraft.ChatFormatting.*;
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
							String namespace = StringArgumentType.getString(context, "namespace");
							getJEIRuntime().ifPresent(runtime -> {
								builder.suggest("_all_", Component.literal("所有"));
								
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
							File workingDir = RRUtils.createNewResultDir(exportDir);
							String namespace = StringArgumentType.getString(context, "namespace");
							String path = StringArgumentType.getString(context, "path");
							IJeiRuntime runtime = getJEIRuntimeOrThrow();
							IJeiHelpers helpers = runtime.getJeiHelpers();
							Stream<RecipeType<?>> stream;
							boolean batch = Objects.equals(path, "_all_");
							if (batch) {
								stream = runtime.getJeiHelpers().getAllRecipeTypes()
										.filter(r -> r.getUid().getNamespace().equals(namespace));
							} else {
								ResourceLocation uid = ResourceLocation.fromNamespaceAndPath(namespace, path);
								RecipeType<?> rt = helpers.getRecipeType(uid).orElseThrow(() -> new IllegalArgumentException("无效的配方类型:" + uid));
								stream = Stream.of(rt);
							}
							LinkedList<ResourceLocation> suc = new LinkedList<>();
							LinkedList<ResourceLocation> fail = new LinkedList<>();
							stream.map(r -> {
								ResourceLocation uid = r.getUid();
								try {
									File resDir = new File(workingDir, uid.getNamespace() + "/" + uid.getPath());
									resDir.mkdirs();
									export(context, resDir, runtime, r);
									return ObjectBooleanImmutablePair.of(uid, true);
								} catch (Exception e) {
									logger.error("JEI专栏导出错误: uid=" + uid, e);
									return ObjectBooleanImmutablePair.of(uid, false);
								}
							}).forEach(f -> {
								(f.rightBoolean() ? suc : fail).add(f.left());
							});
							CallbackMessageBuilder.message()
									.appendCommandSource(this, context.getInput(), AQUA)
									.append("导出完成")
									.appendResultCount("成功", null, suc, GREEN)
									.appendResultCount("失败", null, fail, RED)
									.appendMayOpenDir(workingDir)
									.buildAndSend();
							return 1;
						}));
	}
	
	/**
	 * @see mezz.jei.library.gui.recipes.RecipeLayout#DEFAULT_RECIPE_BORDER_PADDING
	 */
	public static final int DEFAULT_RECIPE_BORDER_PADDING = 4;
	
	/**
	 * @see mezz.jei.gui.recipes.RecipesGui#smallButtonHeight
	 */
	public static final int DEFAULT_FONT_H = 13;
	
	private <T> void export(CommandContext<CommandSourceStack> context, File workingDir, IJeiRuntime runtime, RecipeType<T> recipeType) {
		final StageTimer timer = new StageTimer();
		IRecipeManager recipeManager = runtime.getRecipeManager();
		IRecipeCategory<T> category = recipeManager.getRecipeCategory(recipeType);
		Component title = category.getTitle();
		//绑定贴图
		byte[] bg;
		byte[] ico;
		
		try (var n_im = timer.push("image")) {
			try (var n_ico = n_im.push("ico")) {
				IDrawable iconDrawable = category.getIcon();
				final int icoScale = 4;
				ico = iconDrawable == null ? null : RRUtils.render(iconDrawable.getWidth() * icoScale, iconDrawable.getHeight() * icoScale, graphics -> {
					try (var n_ren = n_ico.push("render")) {
						PoseStack stack = graphics.pose();
						stack.pushPose();
						stack.scale(icoScale, icoScale, 1);
						iconDrawable.draw(graphics);
						stack.popPose();
					}
				}, NativeImage::flipY).get();
			}
			try (var n_bg = n_im.push("bg")) {
				//NOTE:JEI后续getBackground方法被弃用
				IRecipeLookup<T> lookup = recipeManager.createRecipeLookup(recipeType);
				final int scale = 4;
				Font font = Minecraft.getInstance().font;
				int border = DEFAULT_RECIPE_BORDER_PADDING;
				int fontH = DEFAULT_FONT_H;
				int w = category.getWidth() + 2 * border;
				int h = category.getHeight() + 2 * border + fontH;
				bg = RRUtils.renderLayered(w * scale, h * scale, graphics -> {
							try (var n_ren = n_bg.push("render")) {
								PoseStack pose = graphics.pose();
								pose.pushPose();
								{
									pose.scale(scale, scale, 1);
									T recipe = lookup.get().findFirst().orElseThrow(() -> new IllegalStateException("未找到符合改配方类型的配方实例,uid=" + category.getRecipeType().getUid()));
									IFocusGroup focusGroup = runtime.getJeiHelpers().getFocusFactory().getEmptyFocusGroup();
									
									DrawableNineSliceTexture background = Internal.getTextures().getRecipeBackground();
									background.draw(graphics, 0, 0, w, h);
									graphics.flush();
									
									drawCenteredStringWithShadow(graphics, font, title, new ImmutableRect2i(0, 0, w, fontH));
									graphics.flush();
									pose.translate(border, border, 0);
									
									pose.translate(0, fontH, 0);
									recipeManager.createRecipeLayoutDrawable(category, recipe, focusGroup, new DrawableBlank(0, 0), border)
											.orElseThrow()
											.drawRecipe(graphics, -1, -1);
								}
								pose.popPose();
							}
						}, NativeImage::flipY)
						.thenApplyAsync(seq -> {
							try (var n_tiff = n_bg.push("tiff")) {
								return RRUtils.tiff(List.of(seq));
							}
						}).get();
			}
		} catch (InterruptedException | ExecutionException e) {
			throw new RuntimeException("渲染异常: " + e, e);
		}
		try (var n_io = timer.push("io")) {
			Path wp = workingDir.toPath();
			if (ico != null) {
				try (var n_ico = n_io.push("ico.png")) {
					Files.write(new File(workingDir, "ico.png").toPath(), ico);
				}
			}
			try (var n_bg = n_io.push("bg.tif")) {
				Files.write(wp.resolve("bg.tif"), bg);
			}
			try (var n_meta = n_io.push("metadata.json")) {
				JsonObject object = new JsonObject();
				object.addProperty("uid", recipeType.getUid().toString());
				object.addProperty("title", title.getString());
				object.addProperty("recipe_type", recipeType.toString());
				RRUtils.writeJsonResults(workingDir, "metadata", context.getInput(), List.of(object), false);
			}
			
		} catch (IOException e) {
			throw new RuntimeException("输出结果时发生了异常: uid+" + recipeType.getUid() + ", " + e, e);
		}
		CallbackMessageBuilder.message()
				.appendCommandSource(this, context.getInput(), AQUA)
				.append(title.copy()
								.withStyle(UNDERLINE, YELLOW)
								.withStyle(s -> s.withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, Component.literal(recipeType.toString())))),
						"")
				.append("导出完成", GREEN)
				.appendTimeUsed(timer)
				.appendMayOpenDir(workingDir)
				.buildAndSend();
	}
	
	/**
	 * 后续JEI版本中该方法被移除
	 *
	 * @see StringUtil#drawCenteredStringWithShadow(GuiGraphics, Font, Component, ImmutableRect2i)
	 */
	private void drawCenteredStringWithShadow(GuiGraphics graphics, Font font, Component text, ImmutableRect2i area) {
		int width = font.width(text);
		int height = font.lineHeight;
		int x = area.getX() + Math.round((area.getWidth() - width) / 2F);
		int y = area.getY() + Math.round((area.getHeight() - height) / 2F);
		graphics.drawString(font, text, x, y, 0xFFFFFFFF);
	}
	
	@Override
	public String getExporterName() {
		return "jei";
	}
	
	private static IJeiRuntime getJEIRuntimeOrThrow() {
		return getJEIRuntime().orElseThrow(() -> new IllegalStateException("无法访问JEI插件"));
	}
	
	private static Optional<IJeiRuntime> getJEIRuntime() {
		return Optional.ofNullable(JEIPlugin.INSTANCE)
				.map(i -> i.runtime);
	}
	
}