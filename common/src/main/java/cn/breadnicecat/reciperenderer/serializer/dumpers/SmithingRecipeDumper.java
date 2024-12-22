package cn.breadnicecat.reciperenderer.serializer.dumpers;

import cn.breadnicecat.reciperenderer.api.dumper.IRecipeDumper;
import cn.breadnicecat.reciperenderer.api.dumper.IRecipeInputs;
import net.minecraft.world.item.crafting.SmithingTransformRecipe;

/**
 * @author youyihj
 */
//@IRecipeDumper.For(SmithingTransformRecipe.class)
public class SmithingRecipeDumper {
	public static class Transform implements IRecipeDumper<SmithingTransformRecipe> {
		
		@Override
		public void setInputs(SmithingTransformRecipe recipe, IRecipeInputs inputs) {
			inputs.addInput(1, recipe.base);
			inputs.addInput(2, recipe.addition);
			inputs.addInput(3, recipe.template);
		}
	}
//都没有正常的输出
//	public static class Trim implements IRecipeDumper<SmithingTrimRecipe> {
//		@Override
//		public void setOutputs(SmithingTrimRecipe recipe, IRecipeOutputs outputs, RegistryAccess registryAccess) {
//		}
//
//		@Override
//		public void setInputs(SmithingTrimRecipe recipe, IRecipeInputs inputs) {
//			inputs.addInput(1, recipe.base);
//			inputs.addInput(2, recipe.addition);
//			inputs.addInput(3, recipe.template);
//		}
//	}
}
