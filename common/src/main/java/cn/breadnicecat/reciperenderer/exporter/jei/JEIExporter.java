package cn.breadnicecat.reciperenderer.exporter.jei;


import cn.breadnicecat.reciperenderer.RRExtension;
import com.mojang.brigadier.builder.ArgumentBuilder;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created in 2024/11/24 09:49
 * Project: reciperenderer
 *
 * @author <a href="https://github.com/Bread-Nicecat">Bread_NiceCat</a>
 * <p>
 *
 * <p>
 **/
public class JEIExporter implements RRExtension {
	
	private static final Logger logger = LoggerFactory.getLogger(JEIExporter.class);
	
	@Override
	public ArgumentBuilder<CommandSourceStack, ?> buildCommand() {
		return Commands.literal("run").executes(c -> {
			c.getSource().sendFailure(Component.literal("未完成的特性,仍在测试"));
			return 1;
		});
	}

//	{
//		IJeiRuntime runtime = JEIPlugin.INSTANCE.runtime;
//	}

//	private void doExport(RecipeType<?> recipeType) {
//		logger.info("开始JEI导出:{}", recipeType);
//		try {
//			DEBUGS.runGroovyScript(new File(DEBUGS.TEST_SRC_CODE, "jei.groovy"), Map.of("type", recipeType, "runtime", INSTANCE.runtime));
//		} catch (Exception e) {
//			throw new RuntimeException(e);
//		}
//	}
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
