package cn.breadnicecat.reciperenderer.mixin;

import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.CreativeModeTabs;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import static cn.breadnicecat.reciperenderer.utils.CommonUtils.impossibleCode;

/**
 * Created in 2024/8/30 02:54
 * Project: reciperenderer
 *
 * @author <a href="https://github.com/Bread-Nicecat">Bread_NiceCat</a>
 * <p>
 *
 * <p>
 **/
@Mixin(CreativeModeTabs.class)
public interface MixinCreativeModeTabs {
	@Accessor
	static ResourceKey<CreativeModeTab> getHOTBAR() {
		return impossibleCode();
	}
	
	@Accessor
	static ResourceKey<CreativeModeTab> getSEARCH() {
		return impossibleCode();
	}
	
	@Accessor
	static ResourceKey<CreativeModeTab> getINVENTORY() {
		return impossibleCode();
	}
}
