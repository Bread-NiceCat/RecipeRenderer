package cn.breadnicecat.reciperenderer.entry;

import net.minecraft.network.chat.Component;

/**
 * Created in 2024/7/9 上午12:10
 * Project: reciperenderer
 *
 * @author <a href="https://github.com/Bread-Nicecat">Bread_NiceCat</a>
 * <p>
 *
 * <p>
 **/
public interface Localizable extends LocalizableV2 {
	@Override
	default void localizeZh() {
		setZh(getName().getString());
	}
	
	@Override
	default void localizeEn() {
		setEn(getName().getString());
	}
	
	Component getName();
	
	void setZh(String zh);
	
	void setEn(String en);
}
