package cn.breadnicecat.reciperenderer.mixin;

import cn.breadnicecat.reciperenderer.RecipeRenderer;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Created in 2024/8/3 下午3:48
 * Project: reciperenderer
 *
 * @author <a href="https://github.com/Bread-Nicecat">Bread_NiceCat</a>
 * <p>
 *
 * <p>
 **/
@Mixin(Entity.class)
public abstract class MixinEntity {
	@Inject(method = "shouldRender", at = @At("HEAD"), cancellable = true)
	void shouldRenderer(double x, double y, double z, CallbackInfoReturnable<Boolean> cir) {
		if (RecipeRenderer._onRendering) {
			cir.setReturnValue(true);
		}
	}
}
