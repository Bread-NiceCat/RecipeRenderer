package cn.breadnicecat.reciperenderer.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.GuiGraphics;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.gen.Accessor;

/**
 * Created in 2025/1/27 01:19
 * Project: reciperenderer
 *
 * @author <a href="https://github.com/Bread-Nicecat">Bread_NiceCat</a>
 * <p>
 *
 * <p>
 **/
@Mixin(GuiGraphics.class)
public interface MixinGuiGraphics {
	
	@Mutable
	@Accessor("pose")
	void setPose(PoseStack pose);
	
}
