package cn.breadnicecat.reciperenderer.utils;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.Logger;

import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.apache.logging.log4j.Level.*;

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
	private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm:ss:SSS");
	private final ExecutorService executor = Executors.newFixedThreadPool(1);
	/**
	 * 防止这个日志崩，挂一个mc日志
	 */
	private final Logger logger;
	
	public ExportLogger(OutputStream os, Logger logger) {
		super(new OutputStreamWriter(os, StandardCharsets.UTF_8));
		this.logger = logger;
	}
	
	public void warnSilent(String message) {
		logger.warn(message);
		logSilent(WARN, message);
	}
	
	public void infoSilent(String message) {
		logger.info(message);
		logSilent(INFO, message);
	}
	
	public void info(String message) {
		logger.info(message);
		log(INFO, message);
	}
	
	public void warn(String message) {
		logger.warn(message);
		log(WARN, message);
	}
	
	public void error(String message) {
		logger.error(message);
		log(ERROR, message);
		
	}
	
	public void warn(String message, Throwable throwable) {
		logger.warn(message, throwable);
		warn(throwable.toString());
		exception(throwable);
	}
	
	public void error(String message, Throwable throwable) {
		logger.error(message, throwable);
		error(throwable.toString());
		exception(throwable);
	}
	
	public void exception(Throwable throwable) {
		executor.submit(() -> throwable.printStackTrace(this));
	}
	
	int greenOrd = 0;
	int ord = 0;
	
	public void log(Level level, String message) {
		LocalTime now = LocalTime.now();
		String name = Thread.currentThread().getName();
		executor.submit(() -> {
			try {
				MutableComponent msg = Component.literal(message).withStyle(switch (level.getStandardLevel()) {
					case INFO -> greenOrd++ % 2 == 0 ? ChatFormatting.DARK_GREEN : ChatFormatting.DARK_AQUA;
					case WARN -> ChatFormatting.YELLOW;
					case ERROR -> ChatFormatting.RED;
					default -> ChatFormatting.WHITE;
				});
				Minecraft.getInstance().gui.getChat().addMessage(msg);
				write("[" + now.format(formatter) + "][" + name + "/" + level + "]" + message + "\n");
				if (ord % 5 == 0) flush();
			} catch (Exception ignored) {
			}
		});
	}
	
	public void logSilent(Level level, String message) {
		LocalTime now = LocalTime.now();
		String name = Thread.currentThread().getName();
		executor.submit(() -> {
			write("[" + now.format(formatter) + "][" + name + "/" + level + "]" + message + "\n");
			if (ord % 5 == 0) flush();
		});
	}
	
	@Override
	public void close() {
		executor.submit(super::close);
	}
	
	@Override
	public void flush() {
		executor.submit(super::flush);
	}
}
