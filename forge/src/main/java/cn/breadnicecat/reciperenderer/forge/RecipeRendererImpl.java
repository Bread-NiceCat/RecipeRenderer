package cn.breadnicecat.reciperenderer.forge;

import cn.breadnicecat.reciperenderer.RecipeRenderer;
import cn.breadnicecat.reciperenderer.cmd.ICmdFeedback;
import cn.breadnicecat.reciperenderer.cmd.ModidArgumentType;
import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.SharedConstants;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraftforge.client.event.RegisterClientCommandsEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.forgespi.language.IModInfo;

import java.util.stream.Stream;

import static cn.breadnicecat.reciperenderer.RecipeRenderer.MOD_ID;
import static cn.breadnicecat.reciperenderer.RecipeRenderer.export;
import static cn.breadnicecat.reciperenderer.utils.CommonUtils.accept;
import static net.minecraft.commands.Commands.literal;

/**
 * Created in 2024/7/8 下午5:12
 * Project: reciperenderer
 *
 * @author <a href="https://github.com/Bread-Nicecat">Bread_NiceCat</a>
 * <p>
 *
 * <p>
 **/
@Mod(MOD_ID)
public class RecipeRendererImpl {
	public RecipeRendererImpl() {
		RecipeRenderer.init();
		MinecraftForge.EVENT_BUS.addListener(this::onRegisterClientCommands);
	}
	
	@SubscribeEvent
	public void onRegisterClientCommands(RegisterClientCommandsEvent event) {
		CommandDispatcher<CommandSourceStack> dispatcher = event.getDispatcher();
		accept((root) -> dispatcher.register(root
				.then(literal("export")
						.then(Commands.argument("modid", ModidArgumentType.INSTANCE)
								.executes((s) -> {
									String modid = s.getArgument("modid", String.class);
									var source = s.getSource();
									return export(modid, ICmdFeedback.create(source::sendSystemMessage, source::sendFailure));
								})
						).then(literal("all")
								.executes(s -> {
									boolean suc = listMods()
											.map(e -> {
												var source = s.getSource();
												return (export(e, ICmdFeedback.create(source::sendSystemMessage, source::sendFailure)) == 1);
											})
											.reduce(true, (a, b) -> a && b);
									return suc ? 1 : -1;
								}))
				)
		), literal(MOD_ID), literal("rr"));
	}
	
	public static Stream<String> listMods() {
		return ModList.get().getMods().stream().map(IModInfo::getModId);
	}
	
	public static boolean isLoaded(String modid) {
		return ModList.get().isLoaded(modid);
	}
	
	@SuppressWarnings("deprecation")
	public static String getVersion(String modid) {
		if ("minecraft".equals(modid)) {
			return SharedConstants.VERSION_STRING;
		}
		return ModList.get().getModFileById(modid).versionString();
	}
	
	public static RecipeRenderer.Platform getPlatform() {
		return RecipeRenderer.Platform.FORGE;
	}
}
