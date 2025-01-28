package cn.breadnicecat.reciperenderer.exporter.world;

import cn.breadnicecat.reciperenderer.api.IExporter;
import com.mojang.brigadier.builder.ArgumentBuilder;
import net.minecraft.commands.CommandSourceStack;

/**
 * Created in 2025/1/27 22:10
 * Project: reciperenderer
 *
 * @author <a href="https://github.com/Bread-Nicecat">Bread_NiceCat</a>
 * <p>
 *
 * <p>
 **/
public class WorldExporter implements IExporter {
	@Override
	public ArgumentBuilder<CommandSourceStack, ?> buildCommand() {
		return null;
	}
	
	@Override
	public String getExporterName() {
		return "";
	}
}
