package cn.breadnicecat.reciperenderer.internal;

import cn.breadnicecat.reciperenderer.internal.dumpers.CookingRecipeDumper;
import cn.breadnicecat.reciperenderer.internal.dumpers.CraftingRecipeDumper;
import cn.breadnicecat.reciperenderer.internal.dumpers.SmithingRecipeDumper;
import cn.breadnicecat.reciperenderer.internal.dumpers.StoneCuttingRecipeDumper;
import net.minecraft.world.item.crafting.*;

import static cn.breadnicecat.reciperenderer.exporter.recipe.serializer.SerializerManager.discardRecipeType;
import static cn.breadnicecat.reciperenderer.exporter.recipe.serializer.serializers.DumperSerializer.registerRecipeDumper;

/**
 * Created in 2025/1/17 22:15
 * Project: reciperenderer
 *
 * @author <a href="https://github.com/Bread-Nicecat">Bread_NiceCat</a>
 * <p>
 *
 * <p>
 **/
public class InternalImpl {
	public InternalImpl() {
		//SerializerManager
		registerRecipeDumper(new CookingRecipeDumper(), BlastingRecipe.class, CampfireCookingRecipe.class, SmeltingRecipe.class, SmokingRecipe.class);
		registerRecipeDumper(new CraftingRecipeDumper.Shaped(), ShapedRecipe.class, MapExtendingRecipe.class);
		registerRecipeDumper(new CraftingRecipeDumper.Shapeless(), ShapelessRecipe.class);
		registerRecipeDumper(new SmithingRecipeDumper.Transform(), SmithingTransformRecipe.class);
		registerRecipeDumper(new StoneCuttingRecipeDumper(), StonecutterRecipe.class);
		//DumperSerializer
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
}
