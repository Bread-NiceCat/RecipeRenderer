package cn.breadnicecat.reciperenderer;

import cn.breadnicecat.reciperenderer.gui.ExportFrame;
import cn.breadnicecat.reciperenderer.render.Icon;
import cn.breadnicecat.reciperenderer.render.IconWrapper;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.mojang.brigadier.CommandDispatcher;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.ChatFormatting;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.monster.Zombie;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.swing.*;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.Arrays;
import java.util.Base64;
import java.util.HashMap;
import java.util.concurrent.LinkedBlockingDeque;

import static net.minecraft.commands.Commands.literal;

/**
 * Created in 2024/7/8 下午5:11
 * Project: reciperenderer
 *
 * @author <a href="https://github.com/Bread-Nicecat">Bread_NiceCat</a>
 * <p>
 *
 * <p>
 **/
public class RecipeRenderer {
	public static final String MOD_ID = "reciperenderer";
	public static final String MOD_NAME = "Recipe Renderer";
	public static final Logger LOGGER = LogManager.getLogger(MOD_NAME);
	public static final Gson GSON = new GsonBuilder().disableHtmlEscaping().create();
	public static final Base64.Encoder BASE64 = Base64.getEncoder();
	
	public static final ExportFrame exportFrame;
	public static RPlatform platform;
	public static String modVersion = null;
	static boolean outdated = false;
	private static String latestVer;
	/**
	 * modid : modVersion
	 */
	public static HashMap<String, String> loadedMods = new HashMap<>();
	
	static {
		LOGGER.info("开始加载 {}!", MOD_NAME);
		LOGGER.info("设置Headless=false");
		System.setProperty("java.awt.headless", "false");
		exportFrame = new ExportFrame();
		
	}
	
	@Environment(EnvType.CLIENT)
	public static void init(RPlatform rr) {
		RecipeRenderer.platform = rr;
		try {
			platform.listMods().forEach(i -> loadedMods.put(i, platform.getVersion(i)));
			modVersion = loadedMods.get(MOD_ID);
		} catch (Exception e) {
			LOGGER.error("获取Mod实例时出现异常", e);
		}
		Util.backgroundExecutor().submit(() -> {
			try {
				URLConnection connection = new URL("https://gitee.com/Bread-NiceCat/RecipeRenderer/raw/master/versions.json").openConnection();
				InputStreamReader reader = new InputStreamReader(connection.getInputStream());
				JsonObject json = GSON.fromJson(reader, JsonObject.class);
				latestVer = json.get("latest").getAsString();
				int[] latest = Arrays.stream(latestVer.split("[.]", 3)).mapToInt(Integer::parseInt).toArray();
				int[] current = Arrays.stream(modVersion.split("[.]", 3)).mapToInt(Integer::parseInt).toArray();
				if (latest[0] > current[0] || latest[1] > current[1] || latest[2] > current[2]) {
					RecipeRenderer.outdated = true;
				}
			} catch (IOException e) {
				LOGGER.warn("无法检查更新", e);
			}
		});
	}
	
	public static void _onRegisterCMD(CommandDispatcher<CommandSourceStack> dispatcher) {
		var test = literal("test").executes(c -> {
			try {
				LOGGER.info("launch test frame");
				JFrame frame = new JFrame();
				frame.setSize(256, 256);
				frame.setVisible(true);
				
				IconWrapper zo = new IconWrapper(p -> {
					Zombie entity = EntityType.ZOMBIE.create(Minecraft.getInstance().level);
					return new Icon(p, 128, entity);
				});
				var ico1 = new JLabel() {
					void update() {
						hookRenderer(zo::render);
						setIcon(new ImageIcon(zo.getBytesBlocking()));
					}
				};
				ico1.setBorder(new LineBorder(Color.BLACK, 1));
				frame.add(ico1);
				ico1.update();
				frame.addMouseListener(new MouseAdapter() {
					@Override
					public void mouseClicked(MouseEvent e) {
						ico1.update();
					}
					
				});
				c.getSource().sendSystemMessage(Component.literal("测试窗口已打开").withStyle(ChatFormatting.GREEN));
				LOGGER.info("fine");
			} catch (Exception e) {
				LOGGER.error("test error", e);
				throw new RuntimeException(e);
			}
			return 1;
		});
		//=================================
		var outdate = literal("ignoreOutdated").executes(c -> {
			RecipeRenderer.outdated = false;
			c.getSource().sendSystemMessage(Component.literal("好的,但我们仍然建议使用您最新版本的RR去导出mod").withStyle(ChatFormatting.GREEN));
			return 1;
		});
		//=================================
		var builder = literal("export");
		for (String modid : loadedMods.keySet()) {
			builder.then(literal(modid).executes(c -> {
				if (outdated) {
					c.getSource().sendFailure(Component.literal("当前版本过低(" + modVersion + ", 最新版:" + latestVer + "), 导出的数据可能与会与最新版有分歧"));
					c.getSource().sendSystemMessage(Component.literal("输入\"/reciperenderer ignoreOutdated\"忽略此问题").withStyle(ChatFormatting.YELLOW));
					return 0;
				}
				try {
					export(modid);
				} catch (Exception e) {
					c.getSource().sendFailure(Component.literal(e.getMessage()));
					LOGGER.error("致命错误", e);
					return -1;
				}
				return 1;
			}));
		}
		var reciperenderer = literal("reciperenderer");
		var rr = literal("rr");
		dispatcher.register(reciperenderer.then(builder).then(test));
		dispatcher.register(rr.then(builder));
		if (outdated) {
			reciperenderer.then(outdate);
		}
	}
	
	private static final LinkedBlockingDeque<Runnable> clientTasks = new LinkedBlockingDeque<>();
	
	@Environment(EnvType.CLIENT)
	public static void _onClientTick() {
		clientTasks.removeIf(i -> {
			i.run();
			return true;
		});
	}
	
	public static void hookRenderer(Runnable run) {
		clientTasks.add(run);
	}
	
	
	public enum Platform {
		FORGE, FABRIC
	}
	
	public static void export(String modid) {
		new Exporter(modid).runAsync();
	}
	
	
}
