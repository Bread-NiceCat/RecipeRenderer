package cn.breadnicecat.reciperenderer;

import cn.breadnicecat.reciperenderer.render.EntityIcon;
import cn.breadnicecat.reciperenderer.render.IconWrapper;
import cn.breadnicecat.reciperenderer.utils.PoseOffset;
import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.monster.Zombie;

import javax.swing.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.io.File;
import java.io.IOException;

import static cn.breadnicecat.reciperenderer.RecipeRenderer.*;
import static com.mojang.text2speech.Narrator.LOGGER;
import static net.minecraft.commands.Commands.literal;

/**
 * Created in 2024/8/3 下午3:10
 * Project: reciperenderer
 *
 * @author <a href="https://github.com/Bread-Nicecat">Bread_NiceCat</a>
 * <p>
 *
 * <p>
 **/
public class RCommand {
	public static void init(CommandDispatcher<CommandSourceStack> dispatcher) {
		var test = literal("test").executes(c -> {
			try {
				LOGGER.info("launch test frame");
				JFrame frame = new JFrame();
				frame.setSize(256, 256);
				IconWrapper zo = new IconWrapper(p -> {
					Zombie entity = EntityType.ZOMBIE.create(Minecraft.getInstance().level);
					return new EntityIcon(p, 128, entity);
				});
				zo.disableCache();
				var ico1 = new JLabel() {
					PoseOffset last = null;
					boolean lock;
					
					void update(PoseOffset o) {
						if (lock) return;
						lock = true;
						if (last == o) {
							LOGGER.info("skip {}", o);
							lock = false;
						} else hookRenderer(() -> {
							LOGGER.info("update {}", o);
							zo.render(last = o);
							try {
								setIcon(new ImageIcon(zo.getBytesBlocking()));
							} catch (IOException e) {
								throw new RuntimeException(e);
							}
							lock = false;
						});
					}
				};
				frame.add(ico1);
				ico1.update(PoseOffset.NONE);
				MouseAdapter adapter = new MouseAdapter() {
					PoseOffset off = PoseOffset.NONE;
					int pX, pY;
					
					int rotType;
					
					@Override
					public void mousePressed(MouseEvent e) {
						pX = e.getXOnScreen();
						pY = e.getYOnScreen();
						if ((e.getModifiersEx() & MouseEvent.BUTTON2_DOWN_MASK) != 0) {
							off = PoseOffset.NONE;
							ico1.update(off);
						}
					}
					
					@Override
					public void mouseReleased(MouseEvent e) {
						rotType = 0;
					}
					
					@Override
					public void mouseDragged(MouseEvent e) {
						int x = e.getXOnScreen();
						int y = e.getYOnScreen();
						int deltaX = x - pX;
						int deltaY = y - pY;
						pX = x;
						pY = y;
						if ((e.getModifiersEx() & MouseEvent.BUTTON1_DOWN_MASK) != 0) {
							if (rotType == 0) {
								if (deltaY > deltaX) {
									rotType = 2;//Y
								} else {
									rotType = 1;//X
								}
							}
							switch (rotType) {
								case 1 -> off = off.rotate(0, 0, deltaY);
								case 2 -> off = off.rotate(deltaX, 0, 0);
							}
						} else if ((e.getModifiersEx() & MouseEvent.BUTTON3_DOWN_MASK) != 0) {
							off = off.translate(deltaX, deltaY, 0);
						}
						ico1.update(off);
					}
					
					@Override
					public void mouseWheelMoved(MouseWheelEvent e) {
						off = off.scale(e.getWheelRotation() * -0.1f);
						ico1.update(off);
					}
				};
				frame.addMouseListener(adapter);
				frame.addMouseMotionListener(adapter);
				frame.addMouseWheelListener(adapter);
				frame.setVisible(true);
				frame.setAlwaysOnTop(true);
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
			if (outdated) {
				outdated = false;
				c.getSource().sendSystemMessage(Component.literal("好的,但我们仍然建议使用您最新版本的RR去导出。").withStyle(ChatFormatting.YELLOW));
			} else {
				c.getSource().sendSystemMessage(Component.literal("当前无需处理。").withStyle(ChatFormatting.RED));
			}
			return 1;
		});
		//=================================
		var builder = literal("export");
		for (String modid : allMods.keySet()) {
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
					throw new RuntimeException(e);
				}
				return 1;
			}));
		}
		//=================================
		var open = literal("open").executes((c) -> open(Exporter.ROOT_DIR));
		for (String modid : allMods.keySet()) {
			open.then(literal(modid).executes(c -> open(new File(Exporter.ROOT_DIR, modid))));
		}
		//=================================
		var reciperenderer = literal("reciperenderer");
		var rr = literal("rr");
		dispatcher.register(reciperenderer.then(builder).then(test).then(outdate).then(open));
		dispatcher.register(rr.then(builder).then(open));
	}
	
}
