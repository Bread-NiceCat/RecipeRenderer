package cn.breadnicecat.reciperenderer.utils;

import cn.breadnicecat.reciperenderer.RecipeRenderer;
import cn.breadnicecat.reciperenderer.platform.InvHooks;
import cn.breadnicecat.reciperenderer.platform.RPlatform;
import com.google.gson.*;
import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.pipeline.TextureTarget;
import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.VertexSorting;
import com.mojang.serialization.JsonOps;
import net.minecraft.ChatFormatting;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.resources.language.LanguageManager;
import net.minecraft.core.RegistryAccess;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.Ingredient;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4f;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.IntFunction;
import java.util.function.Supplier;

import static cn.breadnicecat.reciperenderer.RecipeRenderer.MC_VERSION;
import static cn.breadnicecat.reciperenderer.RecipeRenderer.MOD_ID;

/**
 * Created in 2024/12/1 02:53
 * Project: reciperenderer
 *
 * @author <a href="https://github.com/Bread-Nicecat">Bread_NiceCat</a>
 * <p>
 *
 * <p>
 **/
public class RRUtils {
	
	public static final boolean DEV;
	
	static {
		//检查是否处于Dev环境
		boolean inDev = false;
		try {
			inDev = new File(new File("").getAbsoluteFile().getParentFile(), "src").exists();
		} catch (Exception ignored) {
		} finally {
			DEV = inDev;
		}
	}
	
	public static final Gson GSON = new GsonBuilder().disableHtmlEscaping().create();
	public static final Gson PRETTY = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();
	public static final Base64.Encoder BASE64 = Base64.getEncoder();
	
	
	/**
	 * 不要用这个方法,这个方法是仅供RR内部使用
	 */
	public static @NotNull ResourceLocation modPrefix(String path) {
		return ResourceLocation.fromNamespaceAndPath(RecipeRenderer.MOD_ID, path);
	}
	
	public static void sleep(long ms) {
		try {
			Thread.sleep(ms);
		} catch (InterruptedException ignored) {
		}
	}
	
	public static <T> T make(Supplier<T> t) {
		return t.get();
	}
	
	public static <T> ArrayList<T> createArray(int size, IntFunction<? extends T> content) {
		ArrayList<T> array = new ArrayList<>(size);
		for (int i = 0; i < size; i++) {
			array.add(content.apply(i));
		}
		return array;
	}
	
	public static <T> T apply(T t, Consumer<T> con) {
		con.accept(t);
		return t;
	}
	
	public static void open(File file) {
		file.mkdirs();
		Util.getPlatform().openFile(file);
	}
	
	public static void hookClientTick(Consumer<Minecraft> consumer) {
		if (RenderSystem.isOnRenderThread()) {
			consumer.accept(Minecraft.getInstance());
		} else {
			InvHooks.hookClientTick(() -> consumer.accept(Minecraft.getInstance()));
		}
	}
	
	public static <T> CompletableFuture<T> hookClientTickSupplier(Function<Minecraft, T> function) {
		CompletableFuture<T> future = new CompletableFuture<>();
		hookClientTick((mc) -> {
			try {
				T value = function.apply(mc);
				future.complete(value);
			} catch (Throwable e) {
				future.completeExceptionally(e);
			}
		});
		return future;
	}
	
	public static CompletableFuture<byte[]> render(int width, int height, @NotNull Consumer<GuiGraphics> render, @Nullable Consumer<NativeImage> modifier) {
		return hookClientTickSupplier((mc) -> {
			Matrix4f oldProjMatrix = RenderSystem.getProjectionMatrix();
			VertexSorting oldVertexSorting = RenderSystem.getVertexSorting();
			try {
				Matrix4f projMatrix = new Matrix4f().setOrtho(0, width, height, 0, -1000, 1000);
				RenderSystem.setProjectionMatrix(projMatrix, VertexSorting.ORTHOGRAPHIC_Z);
				
				RenderTarget target = new TextureTarget(width, height, true, Minecraft.ON_OSX);
				target.bindWrite(true);
				
				GuiGraphics graphics = new GuiGraphics(mc, mc.renderBuffers().bufferSource());
				render.accept(graphics);
				graphics.flush();
				try (NativeImage image = new NativeImage(target.width, target.height, false)) {
					target.bindRead();
					image.downloadTexture(0, false);
					if (modifier != null) modifier.accept(image);
					return image.asByteArray();
				} catch (IOException e) {
					throw new IllegalStateException("处理图片时遇到致命异常", e);
				} finally {
					target.destroyBuffers();
				}
			} finally {
				RenderSystem.setProjectionMatrix(oldProjMatrix, oldVertexSorting);
			}
		});
	}
	
	public static RegistryAccess registryAccessor() {
		return Objects.requireNonNull(Minecraft.getInstance().getConnection(), "未找到Connection").registryAccess();
	}
	
	public static JsonObject createSerializedRecipe(ResourceLocation id, String serializer, String type, JsonObject data) {
		JsonObject object = new JsonObject();
		object.addProperty("id", id.toString());
		object.addProperty("type", serializer + "/" + type);
		object.add("data", data);
		return object;
	}
	
	public static MutableComponent createMayOpenMessage(String text, File toOpen) {
		return Component.literal(text).withStyle(s -> s
				.withClickEvent(new ClickEvent(ClickEvent.Action.OPEN_FILE, toOpen.toString()))
				.withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, Component.literal("单击打开" + toOpen.getName())))
				.withColor(ChatFormatting.LIGHT_PURPLE)
		);
	}
	
	public static MutableComponent createFinishedMessage(long beginTime, int successCnt, int warnCnt, int errorCnt) {
		MutableComponent c = Component.literal("共耗时:")
				.append(Component.literal(String.valueOf((System.currentTimeMillis() - beginTime)))
						.withStyle(ChatFormatting.UNDERLINE))
				.append(Component.literal("毫秒"))
				.withStyle(ChatFormatting.AQUA);
		
		if (successCnt != 0) {
			c.append(Component.literal("成功:")
					.append(Component.literal(String.valueOf(successCnt))
							.withStyle(ChatFormatting.UNDERLINE))
					.withStyle(ChatFormatting.GREEN)
					.append(";")
			);
		}
		if (warnCnt != 0) {
			c.append(Component.literal("警告:")
					.append(Component.literal(String.valueOf(warnCnt))
							.withStyle(ChatFormatting.UNDERLINE))
					.withStyle(ChatFormatting.YELLOW)
					.append(";")
			);
		}
		if (errorCnt != 0) {
			c.append(Component.literal("错误:")
					.append(Component.literal(String.valueOf(errorCnt))
							.withStyle(ChatFormatting.UNDERLINE))
					.withStyle(ChatFormatting.RED)
					.append(";")
			);
		}
		return c;
	}
	
	private static final DateTimeFormatter DIR_PATTERN = DateTimeFormatter.ofPattern("yyyyMMdd");
	
	public static File createNewResultDir(File rootDir) {
		String dirName = LocalDate.now().format(DIR_PATTERN);
		File specDir = new File(rootDir, dirName);
		for (int i = 1; specDir.exists(); specDir = new File(rootDir, dirName + "_" + i++)) {
		}
		specDir.mkdirs();
		
		return specDir;
	}
	
	public static JsonObject serializeIngredient(Ingredient ingredient) {
		JsonElement element = Ingredient.CODEC_NONEMPTY.encodeStart(JsonOps.INSTANCE, ingredient).getOrThrow();
		if (element instanceof JsonArray array) {
			JsonObject object = new JsonObject();
			object.add("item", array);
			return object;
		} else {
			return element.getAsJsonObject();
		}
	}
	
	/**
	 * @param baseName 不要带后缀
	 * @return 返回导出的文件夹
	 */
	public static File writeJsonResults(File rootDir, String baseName, String command, List<JsonObject> out, boolean createFreshDir) throws IOException {
		File specDir = createFreshDir ? createNewResultDir(rootDir) : rootDir;
		
		RPlatform platform = RecipeRenderer.getPlatform();
		long timestamp = System.currentTimeMillis();
		try (PrintWriter writer = new PrintWriter(new FileWriter(new File(specDir, baseName + ".json"), StandardCharsets.UTF_8))) {
			writer.println("#mc=" + MC_VERSION);
			writer.println("#loader=" + platform.getLoaderName() + "@" + platform.getLoaderVersion());
			writer.println("#core=" + MOD_ID + "@" + platform.getRRVersion());
			writer.println("#export_cmd=" + command);
			writer.println("#task_stamp=" + timestamp);
			for (JsonObject object : out) {
				String line = GSON.toJson(object);
				writer.println(line);
			}
		}
		return specDir;
		
	}
	
	public static String base64(byte[] data) {
		return BASE64.encodeToString(data);
	}
	
	public static final String CHINESE_SIMPLIFIED = "zh_cn";
	
	public static void forceChinese() {
		Minecraft instance = Minecraft.getInstance();
		LanguageManager languageManager = instance.getLanguageManager();
		if (Objects.equals(languageManager.getSelected(), CHINESE_SIMPLIFIED)) return;
		languageManager.setSelected(CHINESE_SIMPLIFIED);
		instance.options.languageCode = CHINESE_SIMPLIFIED;
		instance.reloadResourcePacks();

//		ClientLanguage zh = ClientLanguage.loadFrom(instance.getResourceManager(), List.of("en_us", "zh_cn"), false);
//		ClientLanguage en = ClientLanguage.loadFrom(instance.getResourceManager(), List.of("en_us"), false);
//		Language rawl = Language.getInstance();
//		Language.inject(rawl);
	}
}
