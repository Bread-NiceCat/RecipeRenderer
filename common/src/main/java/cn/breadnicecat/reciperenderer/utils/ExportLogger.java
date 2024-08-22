package cn.breadnicecat.reciperenderer.utils;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.Logger;

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
public class ExportLogger {
	//	private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm:ss:SSS");
//	private final ExecutorService executor = Exporter.executor;
	/**
	 * 防止这个日志崩，挂一个mc日志
	 */
	private final Logger logger;
	
	public ExportLogger(Logger logger) {
		this.logger = logger;
	}
	
	public void warnSilent(String message) {
		logger.warn(message);
	}
	
	public void infoSilent(String message) {
		logger.info(message);
	}
	
	public void info(String message) {
		logger.info(message);
		logChat(INFO, message);
	}
	
	public void warn(String message) {
		logger.warn(message);
		logChat(WARN, message);
	}
	
	public void error(String message) {
		logger.error(message);
		logChat(ERROR, message);
	}
	
	public void warn(String message, Throwable throwable) {
		warn(throwable.toString());
		logger.warn(message, throwable);
	}
	
	public void error(String message, Throwable throwable) {
		error(throwable.toString());
		logger.error(message, throwable);
	}
	
	
	int greenOrd = 0;
	
	void logChat(Level level, String message) {
		MutableComponent msg = Component.literal(message).withStyle(switch (level.getStandardLevel()) {
			case INFO -> greenOrd++ % 2 == 0 ? ChatFormatting.DARK_GREEN : ChatFormatting.DARK_AQUA;
			case WARN -> ChatFormatting.YELLOW;
			case ERROR -> ChatFormatting.RED;
			default -> ChatFormatting.WHITE;
		});
		Minecraft.getInstance().gui.getChat().addMessage(msg);
	}
}
