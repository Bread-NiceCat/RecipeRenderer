package cn.breadnicecat.reciperenderer;

import cn.breadnicecat.reciperenderer.gui.screens.EntityViewScreen;
import cn.breadnicecat.reciperenderer.render.IconWrapper;
import cn.breadnicecat.reciperenderer.render.ItemIcon;
import cn.breadnicecat.reciperenderer.utils.RTimer;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.arguments.CompoundTagArgument;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.commands.arguments.ResourceArgument;
import net.minecraft.commands.arguments.item.ItemArgument;
import net.minecraft.commands.arguments.selector.EntitySelector;
import net.minecraft.commands.synchronization.SuggestionProviders;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;

import java.io.File;

import static cn.breadnicecat.reciperenderer.RecipeRenderer.*;
import static net.minecraft.commands.Commands.argument;
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
	public static void init(CommandBuildContext context, CommandDispatcher<CommandSourceStack> dispatcher) {
		EntityViewScreen scn = EntityViewScreen.SIMPLE;
		Minecraft instance = Minecraft.getInstance();
		var worldly = literal("worldly")
				.then(
						(literal("fixed")
								.then(argument("scan_count", IntegerArgumentType.integer(1)).executes(c -> {
									ServerLevel level = c.getSource().getPlayerOrException().serverLevel();
									RecipeRenderer.worldlyExportFixed(level, IntegerArgumentType.getInteger(c, "scan_count"));
									return 1;
								}))
								.executes(c -> {
									if (!checkVersion(c.getSource())) return 0;
									ServerLevel level = c.getSource().getPlayerOrException().serverLevel();
									RecipeRenderer.worldlyExportFixed(level, 10000);
									return 1;
								}))
				);
		//=================================
		var test = literal("test")
				.then(literal("entity").then(argument("target", EntityArgument.entity()).executes(c -> {
							LivingEntity target = (LivingEntity) c.getArgument("target", EntitySelector.class).findSingleEntity(c.getSource());
							if (target instanceof ServerPlayer) target = instance.player;
							getWindow().setScreen(scn.setTarget(target));
							return 1;
						}))
				).then(literal("entity_type").then(argument("entity_type", ResourceArgument.resource(context, Registries.ENTITY_TYPE)).suggests(SuggestionProviders.SUMMONABLE_ENTITIES).executes(c -> {
							if (ResourceArgument.getSummonableEntityType(c, "entity_type").value().create(instance.level) instanceof LivingEntity le) {
								getWindow().setScreen(scn.setTarget(le));
								return 1;
							} else {
								c.getSource().sendFailure(Component.literal("该实体不是一个LivingEntity类型实体"));
								return -1;
							}
						}).then(argument("addition_nbt", CompoundTagArgument.compoundTag()).executes(c -> {
							if (ResourceArgument.getSummonableEntityType(c, "entity_type").value().create(instance.level) instanceof LivingEntity le) {
								le.readAdditionalSaveData(CompoundTagArgument.getCompoundTag(c, "addition_nbt"));
								getWindow().setScreen(scn.setTarget(le));
								return 1;
							} else {
								c.getSource().sendFailure(Component.literal("该实体不是一个LivingEntity类型实体"));
								return -1;
							}
						})))
				).then(literal("item")
						.then(argument("instance", ItemArgument.item(context))
								.executes(c -> {
									ItemStack target = ItemArgument.getItem(c, "instance").createItemStack(1, false);
									getWindow().setScreen(scn.setTarget(target));
									return 1;
								}))
				).then(literal("hand").executes(c -> {
							getWindow().setScreen(scn._setTarget(new IconWrapper((o) -> new ItemIcon(o, 128, instance.player.getItemInHand(InteractionHand.MAIN_HAND), scn.getUseOverride() ? instance.player : null)), false, true));
							return 1;
						})
				);
		
		//=================================
		var outdate = literal("ignoreOutdated").executes(c -> {
			if (outdated) {
				outdated = false;
				c.getSource().sendSystemMessage(Component.literal("好的,但我们仍然建议使用您最新版本去导出。").withStyle(ChatFormatting.YELLOW));
			} else {
				c.getSource().sendSystemMessage(Component.literal("当前无需处理。").withStyle(ChatFormatting.RED));
			}
			return 1;
		});
		//=================================
		var gc = literal("gc").executes(c -> {
			c.getSource().sendSystemMessage(Component.literal("已唤醒内存垃圾回收器"));
			RTimer t = new RTimer();
			long now = Runtime.getRuntime().freeMemory();
			System.gc();
			long cleaned = Runtime.getRuntime().freeMemory() - now;
			c.getSource().sendSystemMessage(Component.literal("清理完成,清理了" + (cleaned / 1024L / 1024L) + "MB,用时" + t));
			return 1;
		});
		//=================================
		var builder = literal("export");
		for (String modid : allMods.keySet()) {
			builder.then(literal(modid).executes(c -> {
				if (!checkVersion(c.getSource())) return 0;
				try {
					export(modid);
				} catch (RuntimeException e) {
					c.getSource().sendFailure(Component.literal(e.getMessage()));
					throw e;
				}
				return 1;
			}));
		}
		//=================================
		var open = literal("open")
				.then(literal("worldly").executes((c) -> open(WorldlyExporter.OUTPUT_DIR)))
				.executes((c) -> open(Exporter.ROOT_DIR));
		for (String modid : allMods.keySet()) {
			open.then(literal(modid).executes(c -> open(new File(Exporter.ROOT_DIR, modid))));
		}
		//=================================
		var reciperenderer = literal("reciperenderer");
		var rr = literal("rr");
		if (test != null) {
			reciperenderer.then(test);
			rr.then(test);
		}
		dispatcher.register(reciperenderer.then(builder).then(worldly).then(outdate).then(gc).then(open));
		dispatcher.register(rr.then(builder).then(worldly).then(open).then(gc));
	}
	
	/**
	 * @return true最新版本
	 */
	static boolean checkVersion(CommandSourceStack source) {
		if (outdated) {
			source.sendFailure(Component.literal("当前版本不是已发布的最新版本(" + modVersion + ", 最新版:" + latestVer + "), 导出的数据可能与会与最新版有分歧"));
			source.sendSystemMessage(Component.literal("输入\"/reciperenderer ignoreOutdated\"忽略此问题").withStyle(ChatFormatting.YELLOW));
		}
		return !outdated;
	}
}
