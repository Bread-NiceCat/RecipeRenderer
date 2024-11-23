package cn.breadnicecat.reciperenderer;

import cn.breadnicecat.reciperenderer.utils.CommonUtils;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.RecipeManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.event.Level;

import java.io.File;
import java.util.function.Predicate;
import java.util.stream.Stream;

import static cn.breadnicecat.reciperenderer.RecipeRenderer.LOGGER;
import static net.minecraft.ChatFormatting.*;

/**
 * Created in 2024/11/24 01:23
 * Project: reciperenderer
 *
 * @author <a href="https://github.com/Bread-Nicecat">Bread_NiceCat</a>
 * <p>
 *
 * <p>
 **/
public class RecipeExporter {
	public static final File workingDir = RecipeRenderer.exportDir;
	private static final Logger log = LoggerFactory.getLogger(RecipeExporter.class);
	
	private final Predicate<ResourceLocation> predicate;
	private final RecipeManager manager = Minecraft.getInstance().level.getRecipeManager();
	
	public RecipeExporter(String recipeId) {
		if (recipeId.matches(".+?:[*]")) {
			String modid = recipeId.split(":", 1)[0];
			predicate = id -> id.getNamespace().equals(modid);
		} else {
			ResourceLocation id = ResourceLocation.parse(recipeId);
			predicate = id::equals;
		}
	}
	
	public void run() {
		long l = System.currentTimeMillis();
		manager.getRecipeIds().filter(predicate).forEach(this::doExport);
		log(Level.INFO, "导出成功,用时" + (System.currentTimeMillis() - l) + "ms");
		CommonUtils.open(RecipeRenderer.exportDir);
	}
	
	private void doExport(ResourceLocation recipeId) {
		File dir = new File(workingDir, recipeId.getNamespace() + File.separator + recipeId.getPath());
		dir.mkdirs();
		
	}
	
	int logCount = 0;
	
	private void log(Level level, String msg) {
		CommonUtils.log(LOGGER, level, msg);
		final boolean cnt = logCount % 2 == 0;
		ChatFormatting format = switch (level) {
			case ERROR -> cnt ? RED : DARK_RED;
			case WARN -> cnt ? YELLOW : GOLD;
			case INFO -> cnt ? GREEN : DARK_GREEN;
			case DEBUG, TRACE -> cnt ? GRAY : DARK_GRAY;
		};
		Minecraft.getInstance().gui.getChat().addMessage(Component.literal(msg).withStyle(format));
	}
	
	public static Stream<ResourceLocation> getAllRecipeTypes() {
		return Minecraft.getInstance().level.registryAccess().registryOrThrow(Registries.RECIPE_TYPE).keySet().stream();
	}
}
