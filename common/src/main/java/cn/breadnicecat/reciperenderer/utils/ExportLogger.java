package cn.breadnicecat.reciperenderer.utils;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import org.apache.logging.log4j.Logger;

import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created in 2024/7/25 上午12:25
 * Project: reciperenderer
 *
 * @author <a href="https://github.com/Bread-Nicecat">Bread_NiceCat</a>
 * <p>
 *
 * <p>
 **/
@Environment(EnvType.CLIENT)
public class ExportLogger extends PrintWriter {
	private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm:ss");
	private static final ExecutorService executor = Executors.newFixedThreadPool(1);
	
	/**
	 * 防止这个日志崩，挂一个mc日志
	 */
	private final Logger logger;
	
	public ExportLogger(OutputStream os, Logger logger) {
		super(new OutputStreamWriter(os, StandardCharsets.UTF_8));
		this.logger = logger;
	}
	
	
	public void info(String message) {
		logger.info(message);
		log("INFO", message);
	}
	
	public void warn(String message) {
		logger.warn(message);
		log("WARN", message);
	}
	
	public void error(String message) {
		logger.error(message);
		log("ERROR", message);
		
	}
	
	public void warn(String message, Throwable throwable) {
		logger.warn(message, throwable);
		warn(message);
		exception(throwable);
	}
	
	public void error(String message, Throwable throwable) {
		logger.error(message, throwable);
		error(message);
		exception(throwable);
	}
	
	public void exception(Throwable throwable) {
		executor.submit(() -> throwable.printStackTrace(this));
	}
	
	int greenOrd = 0;
	
	public void log(String level, String message) {
		executor.submit(() -> {
			try {
				MutableComponent msg = Component.literal(message).withStyle(switch (level) {
					case "INFO" -> greenOrd++ % 2 == 0 ? ChatFormatting.DARK_GREEN : ChatFormatting.DARK_AQUA;
					case "WARN" -> ChatFormatting.YELLOW;
					case "ERROR" -> ChatFormatting.RED;
					default -> ChatFormatting.WHITE;
				});
				Minecraft.getInstance().gui.getChat().addMessage(msg);
			} catch (Exception ignored) {
			}
			write("[" + LocalTime.now().format(formatter) + "][" + level + "]" + message + "\n");
			flush();
		});
	}
}
