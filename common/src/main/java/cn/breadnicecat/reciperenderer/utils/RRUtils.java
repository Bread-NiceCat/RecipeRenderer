package cn.breadnicecat.reciperenderer.utils;

import cn.breadnicecat.reciperenderer.RecipeRenderer;
import cn.breadnicecat.reciperenderer.platform.InvHooks;
import cn.breadnicecat.reciperenderer.platform.RPlatform;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.pipeline.TextureTarget;
import com.mojang.blaze3d.platform.NativeImage;
import net.minecraft.ChatFormatting;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.core.RegistryAccess;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Base64;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;
import java.util.function.Predicate;
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
	
	
	public static @NotNull ResourceLocation prefix(String path) {
		return ResourceLocation.fromNamespaceAndPath(MOD_ID, path);
	}
	
	/**
	 * 可解析 "*:*", "n:*", "n:p"
	 */
	public static Predicate<ResourceLocation> expression2Predicate(String pred) {
		if (pred.matches(".+?:[*]")) {
			String modid = pred.split(":", 2)[0];
			if (modid.equals("*")) return id -> true;
			return id -> id.getNamespace().equals(modid);
		} else {
			ResourceLocation id = ResourceLocation.parse(pred);
			return id::equals;
		}
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
	
	public static <T> T apply(T t, Consumer<T> con) {
		con.accept(t);
		return t;
	}
	
	public static void open(File file) {
		file.mkdirs();
		Util.getPlatform().openFile(file);
	}
	
	public static File createFileDistinct(File root, String fileName) {
		return new File(root, new ExistHelper(ExistHelper.fileBase(root)).getModified(fileName));
	}
	
	
	public static void hookClientTick(Runnable runnable) {
		InvHooks.hookClientTick(runnable);
	}
	
	public static <T> CompletableFuture<T> hookClientTickSupplier(Supplier<T> supplier) {
		CompletableFuture<T> future = new CompletableFuture<>();
		hookClientTick(() -> {
			try {
				T value = supplier.get();
				future.complete(value);
			} catch (Throwable e) {
				future.completeExceptionally(e);
			}
		});
		return future;
	}
	
	public static CompletableFuture<byte[]> render(int width, int height, Consumer<GuiGraphics> render) {
		return hookClientTickSupplier(() -> {
			RenderTarget target = new TextureTarget(width, height, false, Minecraft.ON_OSX);
			target.bindWrite(true);
			target.bindRead();
			Minecraft mc = Minecraft.getInstance();
			render.accept(new GuiGraphics(mc, mc.renderBuffers().bufferSource()));
			try (NativeImage image = new NativeImage(target.width, target.height, false)) {
				image.downloadTexture(0, false);
				return image.asByteArray();
			} catch (IOException e) {
				throw new IllegalStateException("处理图片时遇到致命异常", e);
			} finally {
				target.destroyBuffers();
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
	
	public static final int MCMOD_MAX_CAPACITY_BYTE = 1024 * 1024;
	private static final DateTimeFormatter DIR_PATTERN = DateTimeFormatter.ofPattern("yyyyMMdd");
	
	/**
	 * @param paged    是否分页导出,<s>方便白嫖百科资源(bushi)</s>
	 * @param baseName 不要带后缀
	 * @return 返回导出的文件夹
	 */
	public static File writeResult(File rootDir, String baseName, String command, List<JsonObject> out, boolean paged) throws IOException {
		String dirName = LocalDate.now().format(DIR_PATTERN);
		File specDir = new File(rootDir, dirName);
		for (int i = 1; specDir.exists(); specDir = new File(rootDir, dirName + "_" + i++)) {
		}
		specDir.mkdirs();
		RPlatform platform = RecipeRenderer.getPlatform();
		long timestamp = System.currentTimeMillis();
		
		if (!paged) {
			PrintWriter writer = new PrintWriter(new FileWriter(new File(specDir, baseName + ".json"), StandardCharsets.UTF_8));
			writer.println("#mc=" + MC_VERSION);
			writer.println("#loader=" + platform.getLoaderName() + "@" + platform.getLoaderVersion());
			writer.println("#core=" + MOD_ID + "@" + platform.getRRVersion());
			writer.println("#export_cmd=" + command);
			writer.println("#timestamp=" + timestamp);
			for (JsonObject object : out) {
				String line = GSON.toJson(object);
				writer.println(line);
			}
		} else {
			pg:
			for (int page = 1; ; page++) {
				ByteArrayOutputStream buffer = new ByteArrayOutputStream(MCMOD_MAX_CAPACITY_BYTE);
				PrintWriter writer = new PrintWriter(new OutputStreamWriter(buffer, StandardCharsets.UTF_8));
				if (page == 1) {
					writer.println("#mc=" + MC_VERSION);
					writer.println("#loader=" + platform.getLoaderName() + "@" + platform.getLoaderVersion());
					writer.println("#core=" + MOD_ID + "@" + platform.getRRVersion());
					writer.println("#export_cmd=" + command);
				}
				writer.println("#timestamp=" + timestamp);
				writer.println("#page=" + page);
				
				for (JsonObject object : out) {
					String line = GSON.toJson(object);
					
					continue pg;
				}
				break;
			}
		}
		return specDir;
		
	}
}
