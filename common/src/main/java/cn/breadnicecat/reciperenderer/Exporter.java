package cn.breadnicecat.reciperenderer;

import cn.breadnicecat.reciperenderer.entry.*;
import cn.breadnicecat.reciperenderer.render.IconWrapper;
import cn.breadnicecat.reciperenderer.utils.ExportLogger;
import cn.breadnicecat.reciperenderer.utils.FernFlowerUtils;
import cn.breadnicecat.reciperenderer.utils.Runnable_WithException;
import cn.breadnicecat.reciperenderer.utils.Timer;
import com.google.gson.JsonObject;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.language.ClientLanguage;
import net.minecraft.client.server.IntegratedServer;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.locale.Language;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.item.Item;
import org.jetbrains.annotations.Nullable;

import java.io.*;
import java.rmi.RemoteException;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.BiFunction;
import java.util.zip.GZIPOutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import static cn.breadnicecat.reciperenderer.RecipeRenderer.*;
import static cn.breadnicecat.reciperenderer.utils.CommonUtils.byId;

/**
 * Created in 2024/7/9 上午11:17
 * Project: reciperenderer
 *
 * @author <a href="https://github.com/Bread-Nicecat">Bread_NiceCat</a>
 * <p>
 *
 * <p>
 **/
@Environment(EnvType.CLIENT)
public class Exporter {
	public static final AtomicBoolean LOCK = new AtomicBoolean(false);
	
	private static final ExecutorService executor = Executors.newFixedThreadPool(1);
	static Minecraft instance = Minecraft.getInstance();
	static IntegratedServer server = instance.getSingleplayerServer();
	
	final String modid;
	
	Timer startTime;
	
	/**
	 * .minecraft/rr_export/modid
	 */
	final File root;
	/**
	 * $root/output.zip
	 */
	final File outputfile;
	
	/**
	 * $root/logout.zip
	 */
	final File logoutfile;
	
	private ExportLogger logger;
	
	List<ItemEntry> items = new LinkedList<>();
	List<EntityEntry> entities = new LinkedList<>();
	List<EnchantEntry> enchantments = new LinkedList<>();
	List<EffectEntry> effects = new LinkedList<>();
	List<BiomeEntry> biomes = new LinkedList<>();
	List<JsonObject> recipes = new LinkedList<>();
	Map<String, Class<?>> recipeTypes = new TreeMap<>();
	List<List<? extends Localizable>> localizable = List.of(items, entities, enchantments, effects);
	
	public Exporter(String modid) {
		this.modid = modid;
		this.root = new File(instance.gameDirectory, "rr_export/" + modid);
		this.outputfile = new File(root, "output.zip");
		this.logoutfile = new File(root, "logout.log.gz");
	}
	
	public void runAsync() {
		executor.submit(this::run);
		LOGGER.info("异步导出任务创建完成");
	}
	
	private void run() {
		if (startTime != null) {
			throw new RuntimeException("重复使用的Exporter");
		}
		this.startTime = new Timer();
		LOGGER.info("开始初始化");
		root.mkdirs();
		try (var logger = this.logger = new ExportLogger(new GZIPOutputStream(new FileOutputStream(logoutfile)), LOGGER);
		     var outputs_z = new ZipOutputStream(new FileOutputStream(outputfile))) {
			outputs_z.setLevel(9);
			outputs_z.setComment("Exported by " + MOD_NAME + "@v" + modVersion);
			logger.info("开始导出" + modid);
			
			logger.info("开始收集条目");
			collects();
			
			logger.info("开始渲染图片");
			render();
			logger.info("开始写入数据");
			if (tryRun("写入数据时发生致命错误", () -> write(outputs_z), null)) return;
			logger.info("导出完成,共耗时" + startTime.getString());
			Util.getPlatform().openUri(root.toURI());
		} catch (Throwable e) {
			reportError("遭遇致命错误", e);
			throw new RuntimeException(e.getMessage(), e);
		}
	}
	
	//========================================
	
	public void collects() {
		tryRun("导出物品时遇到致命错误", this::collectItem, items::clear);
		tryRun("导出实体时遇到致命错误", this::collectEntity, entities::clear);
		tryRun("导出配方时遇到致命错误", this::collectRecipe, () -> {
			recipes.clear();
			recipeTypes.clear();
		});
		tryRun("导出物品时遇到致命错误", this::collectBiome, biomes::clear);
		tryRun("导出实体时遇到致命错误", this::collectEffect, effects::clear);
		tryRun("导出配方时遇到致命错误", this::collectEnchant, enchantments::clear);
		
		collectLang();
	}
	
	public void render() {
		items.forEach(i -> {
			scheduleRender(i.id.getPath() + "_ico32", i.ico32);
			scheduleRender(i.id.getPath() + "_ico128", i.ico128);
		});
		entities.forEach(i -> scheduleRender(i.id.getPath() + ".ico128", i.ico));
	}
	
	
	private void write(ZipOutputStream output) throws IOException {
		StringJoiner joiner = new StringJoiner(", ", "共导出", ".");
		if (!items.isEmpty()) {
			joiner.add(items.size() + "个物品");
			logger.info("写入物品");
			write("item", output, "item.jsons", items);
		}
		if (!entities.isEmpty()) {
			joiner.add(effects.size() + "个实体");
			logger.info("写入实体");
			write("entity", output, "entity.jsons", entities);
		}
		if (!effects.isEmpty()) {
			joiner.add(effects.size() + "个药水效果");
			logger.info("写入药水效果");
			write("effect", output, "effect.jsons", effects);
		}
		if (!enchantments.isEmpty()) {
			joiner.add(enchantments.size() + "个附魔");
			logger.info("写入附魔");
			write("enchantment", output, "enchantment.jsons", enchantments);
		}
		if (!biomes.isEmpty()) {
			joiner.add(biomes.size() + "个群系");
			logger.info("写入生物群系");
			write("biome", output, "biome.jsons", biomes);
		}
		if (!recipes.isEmpty()) {
			joiner.add(recipes.size() + "个配方(涉及" + recipeTypes.size() + "种类型)");
			logger.info("写入配方");
			writeJson(output, "recipe.jsons", recipes, null);
			logger.info("反编译配方序列化类");
			HashMap<Class<?>, byte[]> cache = new HashMap<>();
			for (Map.Entry<String, Class<?>> entry : recipeTypes.entrySet()) {
				String k = entry.getKey();
				output.putNextEntry(newZipEntry("recipe_types/" + k + ".java"));
				try {
					Class<?> clz = entry.getValue();
					byte[] bytes = cache.get(clz);
					logger.infoSilent("decompile " + clz.getName());
					if (bytes == null) {
						logger.infoSilent("decompiling...");
						bytes = FernFlowerUtils.decompile(clz).getBytes();
						cache.put(clz, bytes);
					} else {
						logger.infoSilent("decompile SKIPPED");
					}
					output.write(bytes);
				} catch (Exception e) {
					logger.error("反编译" + k + "失败", e);
				} finally {
					output.closeEntry();
				}
			}
			
		}
		logger.info(joiner.toString());
	}
	
	
	//========================================
	private void scheduleRender(@Nullable String name, IconWrapper ico) {
		RecipeRenderer.hookRenderer(() -> {
			if (name != null) logger.infoSilent("render " + name);
			ico.render();
		});
	}
	
	private void collectItem() {
		logger.info("开始收集物品");
		BuiltInRegistries.ITEM.entrySet().stream()
				.filter(i -> i.getKey().location().getNamespace().equals(modid))
				.forEach(i -> {
					ResourceLocation location = i.getKey().location();
					logger.infoSilent(location.toString());
					Item value = i.getValue();
					ItemEntry e = new ItemEntry(location, value.getDefaultInstance());
					items.add(e);
				});
	}
	
	private void collectEntity() {
		logger.info("开始获取实体");
		BuiltInRegistries.ENTITY_TYPE.entrySet().stream()
				.filter(i -> i.getKey().location().getNamespace().equals(modid))
				.forEach(i -> {
					ResourceLocation location = i.getKey().location();
					logger.infoSilent(location.toString());
					Entity entity = i.getValue().create(instance.level);
					if (entity instanceof Mob) {
						EntityEntry entry = new EntityEntry(location, entity);
						entities.add(entry);
					}
				});
		
	}
	
	private void collectLang() {
		logger.info("开始解析语言");
		ClientLanguage zh = ClientLanguage.loadFrom(instance.getResourceManager(), List.of("en_us", "zh_cn"), false);
		ClientLanguage en = ClientLanguage.loadFrom(instance.getResourceManager(), List.of("en_us"), false);
		Language rawl = Language.getInstance();
		for (List<? extends Localizable> s : localizable) {
			Language.inject(en);
			for (Localizable localizable : s) {
				localizable.setEn(localizable.getName().getString());
			}
			Language.inject(zh);
			for (Localizable localizable : s) {
				localizable.setZh(localizable.getName().getString());
			}
		}
		Language.inject(rawl);
	}
	
	private void collectRecipe() throws IOException {
		logger.info("开始获取配方");
		ResourceManager data = Objects.requireNonNull(instance.getSingleplayerServer()).getResourceManager();
		Map<ResourceLocation, Resource> listed = data.listResources("recipes",
				t -> t.getNamespace().equals(modid) && t.getPath().endsWith(".json"));
		for (Map.Entry<ResourceLocation, Resource> value : listed.entrySet()) {
			logger.infoSilent(value.getKey().toString());
			Resource resource = value.getValue();
			JsonObject json = GSON.fromJson(resource.openAsReader(), JsonObject.class);
			String type = json.get("type").getAsString();
			recipeTypes.put(type, BuiltInRegistries.RECIPE_SERIALIZER.get(new ResourceLocation(type)).getClass());
			recipes.add(json);
		}
	}
	
	private void collectEnchant() {
		logger.info("开始获取附魔");
		BuiltInRegistries.ENCHANTMENT.entrySet().stream()
				.filter(i -> i.getKey().location().getNamespace().equals(modid))
				.forEach(i -> {
					ResourceLocation location = i.getKey().location();
					logger.infoSilent(location.toString());
					enchantments.add(new EnchantEntry(location, i.getValue()));
				});
	}
	
	private void collectBiome() {
		logger.info("开始获取生物群系");
		var list = server.registryAccess().registry(Registries.BIOME).orElseThrow().entrySet().stream()
				.filter(i -> i.getKey().location().getNamespace().equals(modid))
				.toList();
		list.forEach(i -> {
			ResourceLocation location = i.getKey().location();
			logger.infoSilent(location.toString());
			biomes.add(new BiomeEntry(location, byId(location.getPath())));
		});
	}
	
	private void collectEffect() {
		logger.info("开始获取药水效果");
		ResourceManager manager = instance.getResourceManager();
		BuiltInRegistries.MOB_EFFECT.entrySet().stream()
				.filter(i -> i.getKey().location().getNamespace().equals(modid))
				.forEach(i -> {
					ResourceLocation location = i.getKey().location();
					logger.infoSilent(location.toString());
					MobEffect value = i.getValue();
					EffectEntry e = new EffectEntry(location, value);
					Optional<Resource> resource = manager.getResource(location.withPrefix("textures/mob_effect/").withSuffix(".png"));
					resource.ifPresent((tex) -> {
						try (InputStream open = tex.open()) {
							e.ico = open.readAllBytes();
						} catch (IOException ex) {
							throw new RuntimeException(ex);
						}
						effects.add(e);
					});
				});
	}
	
	//========================================
	
	public <S extends Storable> void write(String storeType, ZipOutputStream out, String fileName, List<S> lists) throws IOException {
		if (lists == null || lists.isEmpty()) return;
		
		BiFunction<String, byte @Nullable [], String> writer = (path, data) -> {
			try {
				if (data != null) {
					out.putNextEntry(newZipEntry(path));
					out.write(data);
					out.closeEntry();
					return "#" + path;
				}
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
			return null;
		};
		
		LinkedList<JsonObject> jsons = new LinkedList<>();
		
		int version = -1;
		for (int i = 0; i < lists.size(); i++) {
			S storable = lists.get(i);
			JsonObject object = new JsonObject();
			int ver = storable.store(writer, object);
			if (ver < 1) throw new RuntimeException("错误的版本号:" + ver);
			if (i == 0) {
				version = ver;
			} else if (ver != version) {
				throw new RemoteException("不匹配的版本号: encountered:" + ver + ", expected:" + version);
			}
			jsons.add(object);
		}
		
		writeJson(out, fileName, jsons, "#format_version " + storeType + "_v" + version);
	}
	
	private void writeJson(ZipOutputStream out, String fileName, List<JsonObject> jsons, @Nullable String head) throws IOException {
		out.putNextEntry(newZipEntry(fileName));
		var osw = new OutputStreamWriter(out);
		if (head != null && !head.isEmpty()) {
			osw.append(head).append("\n");
		}
		jsons.forEach(i -> {
			try {
				osw.append(i.toString()).append("\n");
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		});
		osw.flush();
		out.closeEntry();
	}
	
	//========================================
	private ZipEntry newZipEntry(String path) {
		logger.infoSilent("write to: " + path);
		return new ZipEntry(path);
	}
	
	private void reportError(String msg, Throwable e) {
		if (logger != null) {
			logger.error(msg, e);
		} else {
			LOGGER.error(msg, e);
		}
	}
	
	
	/**
	 * 尝试运行
	 *
	 * @return 是否失败
	 */
	private boolean tryRun(String msg, Runnable_WithException run, @Nullable Runnable fail) {
		try {
			run.run();
		} catch (Exception e) {
			reportError(msg, e);
			if (fail != null) fail.run();
			return true;
		}
		return false;
	}
}
