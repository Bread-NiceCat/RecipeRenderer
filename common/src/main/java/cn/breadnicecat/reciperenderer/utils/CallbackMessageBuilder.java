package cn.breadnicecat.reciperenderer.utils;

import cn.breadnicecat.reciperenderer.api.IExporter;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.*;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.util.List;
import java.util.function.UnaryOperator;
import java.util.stream.Collectors;

import static net.minecraft.network.chat.Component.empty;
import static net.minecraft.network.chat.Component.literal;

/**
 * Created in 2025/1/26 10:40
 * Project: reciperenderer
 *
 * @author <a href="https://github.com/Bread-Nicecat">Bread_NiceCat</a>
 * <p>
 *
 * <p>
 **/
public class CallbackMessageBuilder {
	private final String DEFAULT_TRAILING = ",";
	private MutableComponent component;
	private String lastTrailing = null;
	
	private CallbackMessageBuilder(MutableComponent base) {
		component = base;
	}
	
	public static CallbackMessageBuilder message(ChatFormatting... cf) {
		return new CallbackMessageBuilder(empty().withStyle(cf));
	}
	
	public static CallbackMessageBuilder message() {
		return new CallbackMessageBuilder(empty());
	}
	
	public static CallbackMessageBuilder message(Component base) {
		return new CallbackMessageBuilder(base.copy());
	}
	
	public CallbackMessageBuilder appendTimeUsed(StageTimer timer) {
		if (!timer.isEnd()) {
			timer.end();
		}
		String tu = timer.toString();
		MutableComponent c = literal("共耗时:")
				.append(literal(String.valueOf(timer.getTimeUsed()))
						.withStyle(ChatFormatting.UNDERLINE))
				.append(literal("毫秒"))
				.withStyle(ChatFormatting.AQUA)
				.withStyle(showAndCopy(tu));
		return append(c);
	}
	
	public CallbackMessageBuilder appendMayOpen(String text, File toOpen) {
		return append(literal(text).withStyle(s -> s
				.withClickEvent(new ClickEvent(ClickEvent.Action.OPEN_FILE, toOpen.toString()))
				.withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, literal("单击打开" + toOpen.getName())))
				.withColor(ChatFormatting.LIGHT_PURPLE)
		));
	}
	
	public CallbackMessageBuilder appendMayOpenDir(File toOpen) {
		toOpen = toOpen.getAbsoluteFile();
		return appendMayOpen("[打开目录]", toOpen.isDirectory() ? toOpen : toOpen.getParentFile());
	}
	
	public CallbackMessageBuilder appendResultCount(String text, @Nullable String desc, List<?> details, ChatFormatting... formatting) {
		MutableComponent base = empty().withStyle(formatting);
		
		MutableComponent tx = literal(text + ":");
		if (desc != null) {
			tx.withStyle(s -> s.withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, literal(desc))));
		}
		base.append(tx);
		
		base.append(literal(String.valueOf(details.size())).withStyle(ChatFormatting.UNDERLINE));
		String dt = details.stream().map(Object::toString).collect(Collectors.joining("\n"));
		String hover;
		//只显示10行
		int size = details.size();
		if (size > 8) {
			hover = details.subList(0, 10).stream().map(Object::toString).collect(Collectors.joining("\n"))
					+ "\n...还有" + (size - 10) + "行" + ",单击以复制";
		} else hover = dt;
		return append(base.withStyle(showAndCopy(hover, dt))
		);
	}
	
	public CallbackMessageBuilder appendCommandSource(IExporter name, String cmd, ChatFormatting... cf) {
		return appendCommandSource(name.getExporterName(), cmd, cf);
	}
	
	public CallbackMessageBuilder appendCommandSource(String name, String cmd, ChatFormatting... cf) {
		return append(literal("[" + name + "]").withStyle(cf).withStyle(showAndCopy(cmd)), "");
	}
	
	public CallbackMessageBuilder appendIf(boolean flag, String msg, String trailing, ChatFormatting... format) {
		return flag ? append(msg, trailing, format) : this;
	}
	
	
	public CallbackMessageBuilder append(String msg, String trailing, ChatFormatting... format) {
		return append(literal(msg).withStyle(format), trailing);
	}
	
	public CallbackMessageBuilder append(String msg, ChatFormatting... format) {
		return append(msg, DEFAULT_TRAILING, format);
	}
	
	public CallbackMessageBuilder append(Component part) {
		return append(part, DEFAULT_TRAILING);
	}
	
	public CallbackMessageBuilder append(Component part, String trailing) {
		if (lastTrailing != null && !lastTrailing.isEmpty()) component.append(lastTrailing);
		component.append(part);
		lastTrailing = trailing;
		return this;
	}
	
	public Component build() {
		return component;
	}
	
	public Component buildAndSend() {
		Component art = build();
		RRUtils.sendChat(art);
		return art;
	}
	
	private static UnaryOperator<Style> showAndCopy(String txt) {
		return showAndCopy(txt, txt);
	}
	
	private static UnaryOperator<Style> showAndCopy(String show, String copy) {
		return s -> s.withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, literal(show)))
				.withClickEvent(new ClickEvent(ClickEvent.Action.COPY_TO_CLIPBOARD, copy));
	}
}
