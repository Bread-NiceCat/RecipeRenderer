package cn.breadnicecat.reciperenderer.mixin.jei;

import cn.breadnicecat.reciperenderer.utils.image.RGuiGraphics;
import mezz.jei.api.gui.drawable.IDrawableStatic;
import mezz.jei.common.gui.elements.DrawableAnimated;
import net.minecraft.client.gui.GuiGraphics;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Created in 2025/1/18 00:06
 * Project: reciperenderer
 *
 * @author <a href="https://github.com/Bread-Nicecat">Bread_NiceCat</a>
 * <p>
 *
 * <p>
 **/
@Mixin(value = DrawableAnimated.class, remap = false)
@Pseudo
public class MixinDrawableAnimated {
	@Shadow
	@Final
	private IDrawableStatic drawable;
	
	@Inject(method = "draw",
			at = @At(value = "HEAD"),
			cancellable = true
	)
	public void draw(GuiGraphics guiGraphics, int xOffset, int yOffset, CallbackInfo ci) {
		if (guiGraphics instanceof RGuiGraphics) {
			drawable.draw(guiGraphics, xOffset, yOffset, 0, 0, 0, 0);
			ci.cancel();
		}
		
	}
}
