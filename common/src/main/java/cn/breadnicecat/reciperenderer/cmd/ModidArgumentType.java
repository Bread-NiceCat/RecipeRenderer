package cn.breadnicecat.reciperenderer.cmd;

import cn.breadnicecat.reciperenderer.RecipeRenderer;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import net.minecraft.network.chat.Component;

import java.util.concurrent.CompletableFuture;

/**
 * Created in 2024/7/8 下午6:37
 * Project: reciperenderer
 *
 * @author <a href="https://github.com/Bread-Nicecat">Bread_NiceCat</a>
 * <p>
 *
 * <p>
 **/
public class ModidArgumentType implements ArgumentType<String> {
	public static final ModidArgumentType INSTANCE = new ModidArgumentType();
	
	protected ModidArgumentType() {
	}
	
	@Override
	public String parse(StringReader reader) throws CommandSyntaxException {
		String s = reader.readString();
		if (!RecipeRenderer.isLoaded(s)) {
			throw new SimpleCommandExceptionType(Component.literal("未安装mod: " + s)).create();
		}
		return s;
	}
	
	@Override
	public <S> CompletableFuture<Suggestions> listSuggestions(CommandContext<S> context, SuggestionsBuilder builder) {
		RecipeRenderer.listMods().forEach(builder::suggest);
		return builder.buildFuture();
	}
}
