package cn.breadnicecat.reciperenderer.exporter.jei;


import cn.breadnicecat.reciperenderer.api.IExporter;
import cn.breadnicecat.reciperenderer.utils.RRUtils;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.ArgumentBuilder;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.category.IRecipeCategory;
import mezz.jei.api.runtime.IJeiRuntime;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.resources.ResourceLocation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
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
	public static final String ID = "jei";
	public static final File workingDir = exportDir;
	
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
												logger.error("获取JEI分栏标题时遇到异常" + r.getUid(), t);
												builder.suggest(path);
											}
										});
							});
							return builder.buildFuture();
						}))
						.executes(context -> {
							String namespace = StringArgumentType.getString(context, "namespace");
							String path = StringArgumentType.getString(context, "path");
							IJeiRuntime runtime = getJEIRuntimeOrThrow();
							ResourceLocation uid = ResourceLocation.fromNamespaceAndPath(namespace, path);
							export(context.getSource(), runtime, runtime.getJeiHelpers().getRecipeType(uid).orElseThrow(() -> new IllegalArgumentException("无效的配方类型")));
							return 1;
						}))
				.executes(context -> {
					CommandSourceStack source = context.getSource();
					String namespace = StringArgumentType.getString(context, "namespace");
					IJeiRuntime runtime = getJEIRuntimeOrThrow();
					runtime.getJeiHelpers().getAllRecipeTypes()
							.filter(r -> r.getUid().getNamespace().equals(namespace))
							.forEach(r -> {
								export(source, runtime, r);
							});
					return 1;
				});
	}
	
	@Override
	public String getExporterName() {
		return "jei";
	}
	
	private void export(CommandSourceStack source, IJeiRuntime runtime, RecipeType<?> recipeType) {
		IRecipeCategory<?> category = runtime.getRecipeManager().getRecipeCategory(recipeType);
		System.out.println(category.getClass());
		//绑定贴图
		try {
			RRUtils.render(category.getWidth(), category.getHeight(), graphics -> {
			
			}).get();
		} catch (InterruptedException | ExecutionException e) {
			throw new RuntimeException(e.toString(), e);
		}
	}
	
	private static IJeiRuntime getJEIRuntimeOrThrow() {
		return getJEIRuntime().orElseThrow(() -> new IllegalStateException("无法访问JEI插件"));
	}
	
	private static Optional<IJeiRuntime> getJEIRuntime() {
		return Optional.ofNullable(JEIPlugin.INSTANCE)
				.map(i -> i.runtime);
	}
/// /		IJeiRuntime runtime = INSTANCE.runtime;
/// /		IRecipeCategory<?> category = runtime.getRecipeManager().getRecipeCategory(recipeType);
/// /		//绑定贴图
/// /		RenderTarget target = new TextureTarget(category.getWidth(), category.getHeight(), false, Minecraft.ON_OSX);
/// /		target.bindWrite(true);
/// /		target.bindRead();
/// /		GuiGraphics graphics = new GuiGraphics(Minecraft.getInstance(), Minecraft.getInstance().renderBuffers().bufferSource());
/// /		//JEI渲染部分
/// /		{
/// /			IJeiHelpers helpers = runtime.getJeiHelpers();
/// /			IFocusFactory focusFactory = helpers.getFocusFactory();
/// /			IDrawable background = category.getBackground();
/// ///			runtime.getRecipeManager().getRecipeCategoryDecorators()
/// /			DrawableNineSliceTexture recipeBackground = Internal.getTextures().getRecipeBackground();
/// /			ImmutableRect2i area = new ImmutableRect2i(
/// /					0,
/// /					0,
/// /					category.getWidth(),
/// /					category.getHeight()
/// /			);
/// /			//DEFAULT_RECIPE_BORDER_PADDING
/// ///			create(category, )
/// /		}
/// /		try (NativeImage image = new NativeImage(target.width, target.height, false)) {
/// /			image.downloadTexture(0, false);
/// /			target.destroyBuffers();
/// /			byte[] data;
/// /			data = image.asByteArray();
/// /		} catch (IOException e) {
/// /			throw new RuntimeException("在处理图片时出现了错误", e);
/// /		}
/// /	}
/// /
/// /	public static <T> IRecipeLayoutDrawable<T> create(
/// /			IRecipeCategory<T> recipeCategory,
/// /			Collection<IRecipeCategoryDecorator<T>> decorators,
/// ///			T recipe,
/// /			IFocusGroup focuses,
/// /			IIngredientManager ingredientManager,
/// /			IScalableDrawable recipeBackground,
/// /			int recipeBorderPadding
/// /	) {
/// /		RecipeLayoutBuilder<T> builder = new RecipeLayoutBuilder<>(recipeCategory, null, ingredientManager);
/// ///		recipeCategory.setRecipe(builder, recipe, focuses);
/// ///		recipeCategory.createRecipeExtras(builder, recipe, focuses);
/// /		return builder.buildRecipeLayout(
/// /				focuses,
/// /				decorators,
/// /				recipeBackground,
/// /				recipeBorderPadding
/// /		);
/// /	}

}
