package cn.breadnicecat.reciperenderer;

import cn.breadnicecat.reciperenderer.gui.screens.EntityViewScreen;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.arguments.CompoundTagArgument;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.commands.arguments.ResourceArgument;
import net.minecraft.commands.arguments.item.ItemArgument;
import net.minecraft.commands.arguments.selector.EntitySelector;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
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
		LiteralArgumentBuilder<CommandSourceStack> test = null;
		if (exportFrame != null) {
			test = literal("test")
					.then(literal("entity").then(argument("target", EntityArgument.entity()).executes(c -> {
						LivingEntity target = (LivingEntity) c.getArgument("target", EntitySelector.class).findSingleEntity(c.getSource());
						if (target instanceof ServerPlayer) target = Minecraft.getInstance().player;
						exportFrame.setScreen(EntityViewScreen.SIMPLE.setTarget(target));
						return 1;
					})).then(argument("entity_type", ResourceArgument.resource(context, Registries.ENTITY_TYPE)).suggests((con, bu) -> {
						BuiltInRegistries.ENTITY_TYPE.entrySet().stream()
								.filter(k -> k.getValue().isEnabled(con.getSource().enabledFeatures()))
								.map(k -> k.getKey().location().toString())
								.forEach(bu::suggest);
						return bu.buildFuture();
					}).executes(c -> {
						if (ResourceArgument.getSummonableEntityType(c, "entity_type").value().create(Minecraft.getInstance().level) instanceof LivingEntity le) {
							exportFrame.setScreen(EntityViewScreen.SIMPLE.setTarget(le));
							return 1;
						} else {
							c.getSource().sendFailure(Component.literal("该实体不是一个LivingEntity类型实体"));
							return -1;
						}
					}).then(argument("addition_nbt", CompoundTagArgument.compoundTag()).executes(c -> {
						if (ResourceArgument.getSummonableEntityType(c, "entity_type").value().create(Minecraft.getInstance().level) instanceof LivingEntity le) {
							le.readAdditionalSaveData(CompoundTagArgument.getCompoundTag(c, "addition_nbt"));
							exportFrame.setScreen(EntityViewScreen.SIMPLE.setTarget(le));
							return 1;
						} else {
							c.getSource().sendFailure(Component.literal("该实体不是一个LivingEntity类型实体"));
							return -1;
						}
					}))))
					.then(literal("item")
							.then(literal("hand").executes(c -> {
								ItemStack target = Minecraft.getInstance().player.getItemInHand(InteractionHand.MAIN_HAND);
								exportFrame.setScreen(EntityViewScreen.SIMPLE.setTarget(target));
								return 1;
							}))
							.then(argument("itemId", ItemArgument.item(context))
									.executes(c -> {
										ItemStack target = ItemArgument.getItem(c, "itemId").createItemStack(1, false);
										exportFrame.setScreen(EntityViewScreen.SIMPLE.setTarget(target));
										return 1;
									}))
					);
		}
		
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
					c.getSource().sendFailure(Component.literal("当前版本不是已发布的最新版本(" + modVersion + ", 最新版:" + latestVer + "), 导出的数据可能与会与最新版有分歧"));
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
		if (test != null) {
			reciperenderer.then(test);
			rr.then(test);
		}
		dispatcher.register(reciperenderer.then(builder).then(outdate).then(open));
		dispatcher.register(rr.then(builder).then(open));
	}
	
}
