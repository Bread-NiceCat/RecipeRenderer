package cn.breadnicecat.reciperenderer;

import cn.breadnicecat.reciperenderer.entry.*;
import cn.breadnicecat.reciperenderer.gui.ExportFrame;
import cn.breadnicecat.reciperenderer.render.IconWrapper;
import cn.breadnicecat.reciperenderer.utils.ExistHelper;
import cn.breadnicecat.reciperenderer.utils.ItemState;
import cn.breadnicecat.reciperenderer.utils.RTimer;
import cn.breadnicecat.reciperenderer.utils.Runnable_WithException;
import com.google.gson.JsonObject;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.JsonOps;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.language.ClientLanguage;
import net.minecraft.client.server.IntegratedServer;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.locale.Language;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.item.crafting.RecipeSerializer;
import org.jetbrains.annotations.Nullable;

import java.io.*;
import java.rmi.RemoteException;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.BiFunction;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import static cn.breadnicecat.reciperenderer.RecipeRenderer.*;
import static cn.breadnicecat.reciperenderer.mixin.MixinCreativeModeTabs.*;
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
public class Exporter {
	private final AtomicBoolean validator = new AtomicBoolean(true);
	
	static Minecraft instance = Minecraft.getInstance();
	public static Exporter current;
	public static final File ROOT_DIR = new File(ExportFrame.debug ? new File("build") : instance.gameDirectory, "rr_export");
	
	final String modid;
	
	RTimer startTime;
	/**
	 * .minecraft/rr_export/modid
	 */
	final File modRoot;
	/**
	 * $root/output.zip
	 */
	final File outputfile;
	
	List<ItemEntry> items = new LinkedList<>();
	List<EntityEntry> entities = new LinkedList<>();
	List<EnchantEntry> enchantments = new LinkedList<>();
	List<EffectEntry> effects = new LinkedList<>();
	List<BiomeEntry> biomes = new LinkedList<>();
	List<DimensionEntry> dimensions = new LinkedList<>();
	List<RecipeEntry> recipes = new LinkedList<>();
	
	List<List<? extends LocalizableV2>> localizable = List.of(items, entities, enchantments, effects);
	
	public Exporter(String modid) {
		this.modid = modid;
		this.modRoot = new File(ROOT_DIR, modid);
		ExistHelper nameHelper = new ExistHelper(ExistHelper.fileBase(modRoot));
		this.outputfile = new File(modRoot, nameHelper.getModified(modid + "-output.zip"));
	}
	
	public void runAsync() {
		if (current != null) throw new RuntimeException("已经有一个程序在导出了");
		current = this;
		EXECUTOR.submit(this::run);
		PLAYER_LOGGER.info("异步导出任务创建完成");
	}
	
	private void run() {
		if (startTime != null) {
			throw new RuntimeException("重复使用的Exporter");
		}
		this.startTime = new RTimer();
		PLAYER_LOGGER.info("开始初始化");
		modRoot.mkdirs();
		try (var outputs_z = new ZipOutputStream(new FileOutputStream(outputfile))) {
			outputs_z.setLevel(9);
			outputs_z.setComment("Exported by " + MOD_NAME + " v" + modVersion
					+ ", \nTargetMod " + modid + "@" + getVersion(modid)
					+ ", \nEnvironment Minecraft@" + mcVersion + "+" + platform.getName() + "@" + platform.getLoaderVersion());
			PLAYER_LOGGER.info("开始导出" + modid);
			
			PLAYER_LOGGER.info("开始收集条目");
			
			tryRun("导出物品时遇到致命错误", this::_collectItem, items::clear);
			tryRun("导出实体时遇到致命错误", this::_collectEntity, entities::clear);
			tryRun("导出配方时遇到致命错误", this::_collectRecipe, recipes::clear);
			tryRun("导出时药水效果时遇到致命错误", this::_collectEffect, effects::clear);
			tryRun("导出附魔时遇到致命错误", this::_collectEnchant, enchantments::clear);
			tryRun("导出生物群系时遇到致命错误", this::_collectBiome, biomes::clear);
			tryRun("导出维度时遇到致命错误", this::_collectDim, dimensions::clear);
			_collectLang();
			
			PLAYER_LOGGER.info("向客户端发送渲染任务");
			items.forEach(i -> {
				scheduleRender(i.id.getPath() + "_ico32", i.ico32);
				scheduleRender(i.id.getPath() + "_ico128", i.ico128);
			});
			entities.forEach(i -> {
				scheduleRender(i.id.getPath() + "_ico32", i.ico32);
				scheduleRender(i.id.getPath() + "_ico128", i.ico128);
			});
			
			PLAYER_LOGGER.info("开始写入数据");
			if (tryRun("写入数据时发生致命错误", () -> write(outputs_z), null)) return;
			
			PLAYER_LOGGER.info("导出完成,共耗时" + startTime.getStringMs());
			open(modRoot);
		} catch (Throwable e) {
			validator.set(false);
			LOGGER.error("遭遇致命错误", e);
			throw new RuntimeException(e.getMessage(), e);
		} finally {
			current = null;
		}
		validator.set(false);
	}
	
	//========================================
	
	private void write(ZipOutputStream output) throws IOException {
		StringJoiner joiner = new StringJoiner(", ", "共导出", ".");
		if (!effects.isEmpty()) {
			joiner.add(effects.size() + "个药水效果");
			PLAYER_LOGGER.info("写入药水效果");
			write("effect", output, "effect.jsons", effects);
		}
		if (!enchantments.isEmpty()) {
			joiner.add(enchantments.size() + "个附魔");
			PLAYER_LOGGER.info("写入附魔");
			write("enchantment", output, "enchantment.jsons", enchantments);
		}
		if (!dimensions.isEmpty()) {
			joiner.add(dimensions.size() + "个维度");
			PLAYER_LOGGER.info("写入维度");
			write("dimension", output, "dimension.jsons", dimensions);
		}
		if (!biomes.isEmpty()) {
			joiner.add(biomes.size() + "个群系");
			PLAYER_LOGGER.info("写入生物群系");
			write("biome", output, "biome.jsons", biomes);
		}
		if (!recipes.isEmpty()) {
			joiner.add(recipes.size() + "个配方");
			PLAYER_LOGGER.info("写入配方");
			var list = recipes.stream().sorted(Comparator.comparing(other -> other.type))
					.map(RecipeEntry::format)
					.toList();
			writeJson(output, "recipe.jsons", list, null);
			
		}
		
		if (!items.isEmpty()) {
			joiner.add(items.size() + "个物品");
			PLAYER_LOGGER.info("写入物品");
			write("item", output, "item.jsons", items);
		}
		
		if (!entities.isEmpty()) {
			joiner.add(entities.size() + "个实体");
			PLAYER_LOGGER.info("写入实体");
			write("entity", output, "entity.jsons", entities);
		}
		PLAYER_LOGGER.info(joiner.toString());
	}
	
	
	//========================================
	private void scheduleRender(@Nullable String name, IconWrapper ico) {
		RecipeRenderer.hookRenderer(() -> {
			if (validator.get()) {
				if (name != null) LOGGER.info("开始渲染：{},wrapId={}", name, ico.wrapId);
				try {
					instance.getProfiler().push("Render Exporter Icon");
					ico.render();
					instance.getProfiler().pop();
				} catch (Exception e) {
					LOGGER.error(name + "渲染失败,wrapId=" + ico.wrapId, e);
				}
			} else {
				LOGGER.warn("渲染失败:无效的会话,wrapId={}", ico.wrapId);
			}
		});
	}
	
	private void _collectItem() {
		PLAYER_LOGGER.info("开始收集物品");
		CreativeModeTabs.tryRebuildTabContents(FeatureFlags.REGISTRY.allFlags(), true, instance.level.registryAccess());
		HashMap<ItemState, ItemEntry> holders = new HashMap<>();
		CreativeModeTabs.allTabs().stream()
				.map(t -> Pair.of(BuiltInRegistries.CREATIVE_MODE_TAB.getResourceKey(t).orElse(null), t))
				.filter(key -> {
					ResourceKey<CreativeModeTab> id = key.getFirst();
					return id != null && id != getHOTBAR() && id != getSEARCH() && id != getINVENTORY();
				})
				.forEach(tabs -> tabs.getSecond().getDisplayItems().stream()
						.map(i -> Pair.of(BuiltInRegistries.ITEM.getKey(i.getItem()), new ItemState(i)))
						.filter(p -> p.getFirst().getNamespace().equals(modid))
						.forEach(i -> {
							ResourceLocation location = i.getFirst();
							holders.computeIfAbsent(i.getSecond(), (state) -> {
								LOGGER.info(location.toString());
								ItemEntry e = new ItemEntry(location, state);
								items.add(e);
								return e;
							}).tabs.add(tabs.getSecond());
						}));
		//从注册表拿东西可能会拿不到带nbt的东西
//		BuiltInRegistries.ITEM.entrySet().stream()
//				.filter(i -> i.getKey().location().getNamespace().equals(modid))
//				.forEach(i -> {
//					ResourceLocation location = i.getKey().location();
//					logger.infoSilent(location.toString());
//					Item value = i.getValue();
//					ItemEntry e = new ItemEntry(location, value.getDefaultInstance());
//					items.add(e);
//				});
	}
	
	private void _collectEntity() {
		PLAYER_LOGGER.info("开始获取实体");
		BuiltInRegistries.ENTITY_TYPE.entrySet().stream()
				.filter(i -> i.getKey().location().getNamespace().equals(modid))
				.forEach(i -> {
					ResourceLocation location = i.getKey().location();
					LOGGER.info(location.toString());
					Entity entity = i.getValue().create(instance.level);
					if (entity instanceof Mob mob) {
						EntityEntry entry = new EntityEntry(location, mob);
						entities.add(entry);
					}
				});
		
	}
	
	private void _collectLang() {
		PLAYER_LOGGER.info("开始解析语言");
		ClientLanguage zh = ClientLanguage.loadFrom(instance.getResourceManager(), List.of("en_us", "zh_cn"), false);
		ClientLanguage en = ClientLanguage.loadFrom(instance.getResourceManager(), List.of("en_us"), false);
		Language rawl = Language.getInstance();
		for (List<? extends LocalizableV2> s : localizable) {
			Language.inject(en);
			for (LocalizableV2 localizable : s) {
				localizable.localizeEn();
			}
			Language.inject(zh);
			for (LocalizableV2 localizable : s) {
				localizable.localizeZh();
			}
		}
		Language.inject(rawl);
	}
	
	@SuppressWarnings({"unchecked", "rawtypes"})
	private void _collectRecipe() {
		PLAYER_LOGGER.info("开始获取配方");
		RecipeManager manager = Objects.requireNonNull(instance.getSingleplayerServer().getRecipeManager());
		for (RecipeHolder<?> holder : manager.getRecipes()) {
			if (holder.id().getNamespace().equals(modid)) {
				DataResult<JsonObject> result = ((RecipeSerializer) (holder.value().getSerializer()))
						.codec()
						.codec()
						.encodeStart(JsonOps.INSTANCE, holder.value());
				JsonObject json = result.getOrThrow();
				recipes.add(new RecipeEntry(holder.id(), json));
			}
		}
//		ResourceManager data = Objects.requireNonNull(instance.getSingleplayerServer()).getResourceManager();
//		Map<ResourceLocation, Resource> listed = data.listResources("recipes",
//				t -> t.getNamespace().equals(modid) && t.getPath().endsWith(".json"));
//		for (Map.Entry<ResourceLocation, Resource> value : listed.entrySet()) {
//			LOGGER.info(value.getKey().toString());
//			Resource resource = value.getValue();
//			JsonObject json = GSON.fromJson(resource.openAsReader(), JsonObject.class);
//			String type = json.get("type").getAsString();
//			recipeTypes.put(type, BuiltInRegistries.RECIPE_SERIALIZER.get(ResourceLocation.parse(type)).getClass());
//			recipes.add(json);
//		}
	}
	
	private void _collectEnchant() {
		PLAYER_LOGGER.info("开始获取附魔");
		instance.level.registryAccess().registryOrThrow(Registries.ENCHANTMENT).entrySet().stream()
				.filter(i -> i.getKey().location().getNamespace().equals(modid))
				.forEach(i -> {
					ResourceLocation location = i.getKey().location();
					LOGGER.info(location.toString());
					enchantments.add(new EnchantEntry(location, i.getValue()));
				});
	}
	
	private void _collectDim() {
		IntegratedServer server = Minecraft.getInstance().getSingleplayerServer();
		PLAYER_LOGGER.info("开始获取维度");
		server.registryAccess().registry(Registries.DIMENSION_TYPE).orElseThrow().entrySet().stream()
				.filter(i -> i.getKey().location().getNamespace().equals(modid))
				.forEach(i -> {
					ResourceLocation location = i.getKey().location();
					LOGGER.info(location.toString());
					dimensions.add(new DimensionEntry(location, byId(location.getPath()), i.getValue()));
				});
		
	}
	
	private void _collectBiome() {
		IntegratedServer server = Minecraft.getInstance().getSingleplayerServer();
		PLAYER_LOGGER.info("开始获取生物群系");
		var list = server.registryAccess().registry(Registries.BIOME).orElseThrow().entrySet().stream()
				.filter(i -> i.getKey().location().getNamespace().equals(modid))
				.toList();
		list.forEach(i -> {
			ResourceLocation location = i.getKey().location();
			LOGGER.info(location.toString());
			biomes.add(new BiomeEntry(location, byId(location.getPath())));
		});
	}
	
	private void _collectEffect() {
		PLAYER_LOGGER.info("开始获取药水效果");
		ResourceManager manager = instance.getResourceManager();
		BuiltInRegistries.MOB_EFFECT.entrySet().stream()
				.filter(i -> i.getKey().location().getNamespace().equals(modid))
				.forEach(i -> {
					ResourceLocation location = i.getKey().location();
					LOGGER.info(location.toString());
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
	final ExistHelper existHelper = new ExistHelper();
	
	public <S extends StorableV2> void write(String storeType, ZipOutputStream out, String fileName, List<S> lists) throws IOException {
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
			int ver = storable.store(existHelper, writer, object, PLAYER_LOGGER);
			if (ver < 1) throw new RuntimeException("错误的版本号:" + ver);
			if (i == 0) {
				version = ver;
			} else if (ver != version) {
				throw new RemoteException("不匹配的版本号: encountered:" + ver + ", expected:" + version);
			}
			jsons.add(object);
			
			if (storable instanceof Closeable closeable) {
				closeable.close();
			}
		}
		writeJson(out, fileName, jsons, "#format_version " + storeType + "_v" + version);
		lists.clear();
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
		LOGGER.info("准备写入: " + path);
		return new ZipEntry(path);
	}
	
	
	/**
	 * 尝试运行
	 *
	 * @return 是否失败
	 */
	private boolean tryRun(String msg, Runnable_WithException<Exception> run, @Nullable Runnable fail) {
		try {
			run.run();
		} catch (Throwable e) {
			PLAYER_LOGGER.error(msg + "," + e);
			LOGGER.error(msg, e);
			if (fail != null) fail.run();
			return true;
		}
		return false;
	}
	
	public static Exporter getInstance() {
		return current;
	}
}
