package cn.breadnicecat.reciperenderer.api;

import com.mojang.brigadier.builder.ArgumentBuilder;
import net.minecraft.commands.CommandSourceStack;

/**
 * Created in 2024/12/1 02:20
 * Project: reciperenderer
 *
 * @author <a href="https://github.com/Bread-Nicecat">Bread_NiceCat</a>
 * <p>
 *
 * <p>
 **/

public interface IExporter {
	ArgumentBuilder<CommandSourceStack, ?> buildCommand();
	
	String getExporterName();
}
