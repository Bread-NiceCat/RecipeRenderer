package cn.breadnicecat.reciperenderer;

import cn.breadnicecat.reciperenderer.cmd.ICmdFeedback;
import cn.breadnicecat.reciperenderer.datafix.DataStorer;
import cn.breadnicecat.reciperenderer.entry.*;
import cn.breadnicecat.reciperenderer.utils.Runnable_WithException;
import cn.breadnicecat.reciperenderer.utils.Timer;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.datafixers.util.Pair;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.ChatFormatting;
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

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Stream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import static cn.breadnicecat.reciperenderer.RecipeRenderer.*;
import static cn.breadnicecat.reciperenderer.utils.CommonUtils.byId;
import static net.minecraft.network.chat.Component.literal;

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
	static final Object I18N_LOCK = new Object();
	
	public static final Gson GSON = new GsonBuilder().disableHtmlEscaping().create();
	public static final Base64.Encoder BASE64 = Base64.getEncoder();
	
	static Minecraft instance = Minecraft.getInstance();
	static IntegratedServer server = instance.getSingleplayerServer();
	
	String modid;
	ICmdFeedback feedback;
	DataStorer dataStorer;
	
	Timer stt, partStt;
	ExportProcessFrame frame;
	File root;
	
	ZipOutputStream outputs_z;
	ZipOutputStream images_z;
	
	List<ItemEntry> items = new LinkedList<>();
	List<EntityEntry> entities = new LinkedList<>();
	List<EffectEntry> effects = new LinkedList<>();
	List<EnchantEntry> enchantments = new LinkedList<>();
	List<NameEntry> biomes = new LinkedList<>();
	
	List<List<? extends Localizable>> locals = List.of(items, entities, effects, enchantments);
	List<JsonObject> recipes = new LinkedList<>();
	Set<String> recipeTypes = new TreeSet<>();
	
	public Exporter(String modid) {
		this.modid = modid;
	}
	
	/**
	 * @return true:已取消
	 */
	private boolean tryRun(Runnable_WithException rr, @Nullable Runnable fail) {
		try {
			rr.run();
		} catch (Exception e) {
			softFail(e);
			if (fail != null) {
				fail.run();
				if (e instanceof ExportCancelledException) {
					return true;
				}
			}
		}
		return false;
	}
	
	public void run(final ICmdFeedback feedback, final DataStorer dataStorer) {
		stt = new Timer();
		partStt = new Timer();
		
		this.feedback = feedback;
		this.dataStorer = dataStorer;
		
		if (!feedback.isPlayer()) {
			throw new RuntimeException("不是玩家");
		}
		RenderSystem.assertOnRenderThread();
		
		ExportProcessFrame frame;
		try {
			frame = new ExportProcessFrame(modid);
		} catch (Exception e) {
			feedback.sendError(literal("无法创建窗口"));
			softFail(e);
			frame = ExportProcessFrame.empty();
		}
		frame.setDaemon(Thread.currentThread());
		this.frame = frame;
		Util.backgroundExecutor().submit(() -> this.frame.show());
		
		try {
			this.root = new File(instance.gameDirectory, "rr_export/" + modid);
			File outfile = new File(root, "output.zip");
			File imgfile = new File(root, "images.zip");
			root.mkdirs();
			//防止出现最后因为文件而无法导出的情况
			//先占茅坑(bushi
			outputs_z = new ZipOutputStream(new FileOutputStream(outfile));
			images_z = new ZipOutputStream(new FileOutputStream(imgfile));
			if (tryRun(this::collectItem, items::clear)) return;
			if (tryRun(this::collectEntity, entities::clear)) return;
			Util.backgroundExecutor().submit(() -> {
				try (var z1 = outputs_z; var z2 = images_z) {
					if (tryRun(this::collectRecipe, recipes::clear)) return;
					if (tryRun(this::collectEffect, effects::clear)) return;
					if (tryRun(this::collectEnchant, enchantments::clear)) return;
					if (tryRun(this::collectBiome, biomes::clear)) return;
					
					int rs = recipes.size();
					int rts = recipeTypes.size();
					int is = items.size();
					int effs = effects.size();
					int es = entities.size();
					int bios = biomes.size();
					int encs = enchantments.size();
					if ((rs | rts | is | effs | es | bios | encs) == 0) {
						feedback.sendFeedback(literal(modid + "无条目可导出").withStyle(ChatFormatting.YELLOW));
						outputs_z.close();
						images_z.close();
						imgfile.delete();
						outfile.delete();
						root.delete();
						this.frame.close();
						return;
					}
					
					collectLang();//sync
					
					write();
					var ref = new Object() {
						StringBuilder text = new StringBuilder("<html><body>共计导出了");
						StringJoiner msg = new StringJoiner(",", "共计导出了", "");
						StringJoiner temp = new StringJoiner(",");
						
						void append(String s) {
							msg.add(s);
							temp.add(s);
						}
						
						void save() {
							text.append(temp).append("<br/>");
							temp = new StringJoiner(",");
						}
						
						String textEnd() {
							return text.append("</body></html>").toString();
						}
					};
					ref.append("%d条配方(涉及%d个类型)".formatted(rs, rts));
					ref.save();
					ref.append("%d个物品".formatted(is));
					ref.append("%d个实体".formatted(es));
					ref.save();
					ref.append("%d个效果".formatted(effs));
					ref.append("%d个附魔".formatted(encs));
					ref.append("%d个群系".formatted(bios));
					ref.save();
					this.frame.setTextMajor(ref.textEnd());
					String conTime = "用时" + stt.getStringAndReset();
					feedback.sendFeedback(literal(modid + "导出完成" + ref.msg + "," + conTime).withStyle(ChatFormatting.GREEN));
					this.frame.setText(conTime);
				} catch (Exception e) {
					fastFail(e);
				}
			});
		} catch (Exception e) {
			fastFail(e);
		}
	}
	
	
	private void softFail(Exception e) {
		LOGGER.error("出错了!", e);
		if (frame != null) {
			if (frame.isCancel()) {
				frame.revalidate();
				frame.setTextBar(e.getMessage());
				frame.invalidate();
			}
		}
		feedback.sendError(literal(" 导出" + modid + "时遇到错误: " + e.getMessage()).withStyle(ChatFormatting.RED));
	}
	
	private void fastFail(Exception e) {
		frame.cancel();
		softFail(e);
	}
	
	private void collectItem() {
		LOGGER.info("开始获取物品");
		frame.setTextMajor("获取物品");
		var iilist = BuiltInRegistries.ITEM.entrySet().stream()
				.filter(i -> i.getKey().location().getNamespace().equals(modid))
				.toList();
		
		float idelta = 0.3f / iilist.size();
		iilist.forEach(i -> {
			ResourceLocation location = i.getKey().location();
			frame.setText("添加物品: " + location);
			Item value = i.getValue();
			items.add(new ItemEntry(location, value.getDefaultInstance()));
			frame.addProcess(idelta, 0, 0.3f);
		});
		LOGGER.info("完成获取物品: {}", partStt.getStringAndReset());
	}
	
	private void collectEntity() {
		LOGGER.info("开始获取实体");
		frame.setTextMajor("获取实体");
		var eelist = BuiltInRegistries.ENTITY_TYPE.entrySet().stream()
				.filter(i -> i.getKey().location().getNamespace().equals(modid))
				.toList();
		float edelta = 0.2f / eelist.size();
		eelist.forEach(i -> {
			ResourceLocation location = i.getKey().location();
			frame.setText("添加实体: " + location);
			Entity entity = i.getValue().create(instance.level);
			if (entity instanceof Mob) entities.add(new EntityEntry(location, entity));
			frame.addProcess(edelta, 0.3f, 0.4f);
		});
		LOGGER.info("完成获取实体: {}", partStt.getString());
	}
	
	private void collectEnchant() {
		LOGGER.info("开始获取附魔");
		frame.setTextMajor("获取附魔");
		var list = BuiltInRegistries.ENCHANTMENT.entrySet().stream()
				.filter(i -> i.getKey().location().getNamespace().equals(modid))
				.toList();
		list.forEach(i -> {
			ResourceLocation location = i.getKey().location();
			frame.setText("添加附魔: " + location);
			enchantments.add(new EnchantEntry(location.toString(), i.getValue()));
		});
		LOGGER.info("完成获取附魔: {}", partStt.getStringAndReset());
	}
	
	private void collectBiome() {
		var list = server.registryAccess().registry(Registries.BIOME).orElseThrow().entrySet().stream()
				.filter(i -> i.getKey().location().getNamespace().equals(modid))
				.toList();
		list.forEach(i -> {
			ResourceLocation location = i.getKey().location();
			frame.setText("添加群系: " + location);
			biomes.add(new NameEntry(location.toString(), byId(location.getPath())));
		});
		LOGGER.info("完成获取群系: {}", partStt.getStringAndReset());
	}
	
	private void collectEffect() {
		LOGGER.info("开始获取药水效果");
		frame.setTextMajor("获取药水效果");
		var list = BuiltInRegistries.MOB_EFFECT.entrySet().stream()
				.filter(i -> i.getKey().location().getNamespace().equals(modid))
				.toList();
		float delta = 0.2f / list.size();
		ResourceManager manager = instance.getResourceManager();
		list.forEach(i -> {
			ResourceLocation location = i.getKey().location();
			frame.setText("添加药水效果: " + location);
			MobEffect value = i.getValue();
			EffectEntry e = new EffectEntry(location.toString(), value);
			Optional<Resource> resource = manager.getResource(location.withPrefix("textures/mob_effect/").withSuffix(".png"));
			resource.ifPresent((tex) -> {
				try (InputStream open = tex.open()) {
					byte[] png = open.readAllBytes();
					e.ico = BASE64.encodeToString(png);
					e.icoRaw = png;
				} catch (IOException ex) {
					throw new RuntimeException(ex);
				}
				effects.add(e);
			});
			frame.addProcess(delta, 0.4f, 0.5f);
		});
		LOGGER.info("完成获取药水效果: {}", partStt.getStringAndReset());
	}
	
	private void collectRecipe() throws IOException {
		LOGGER.info("开始获取配方");
		frame.setTextMajor("获取配方");
		ResourceManager data = Objects.requireNonNull(instance.getSingleplayerServer()).getResourceManager();
		Map<ResourceLocation, Resource> listed = data.listResources("recipes",
				t -> t.getNamespace().equals(modid) && t.getPath().endsWith(".json"));
		float delta = 0.2f / listed.size();
		for (Map.Entry<ResourceLocation, Resource> value : listed.entrySet()) {
			Resource resource = value.getValue();
			frame.setText("添加配方: " + value.getKey());
			JsonObject json = GSON.fromJson(resource.openAsReader(), JsonObject.class);
			recipeTypes.add(json.get("type").getAsString());
			recipes.add(json);
			frame.addProcess(delta, 0.5f, 0.7f);
		}
		LOGGER.info("完成获取配方: {}", partStt.getStringAndReset());
	}
	
	private void collectLang() {
		LOGGER.info("开始获取语言");
		frame.setTextMajor("获取语言");
		int tot = locals.stream().map(List::size).reduce(0, Integer::sum);
		if (tot != 0) {
			ClientLanguage zh = ClientLanguage.loadFrom(instance.getResourceManager(), List.of("en_us", "zh_cn"), false);
			ClientLanguage en = ClientLanguage.loadFrom(instance.getResourceManager(), List.of("en_us"), false);
			float delta = 0.07f / tot;
			
			synchronized (I18N_LOCK) {
				Language rawl = Language.getInstance();
				for (List<? extends Localizable> s : locals) {
					frame.setText("获取I18n(英)");
					Language.inject(en);
					for (Localizable localizable : s) {
						localizable.setEn(localizable.getName().getString());
						frame.addProcess(delta);//0.7-0.77
					}
					frame.setText("获取I18n(英)");
					Language.inject(zh);
					for (Localizable localizable : s) {
						localizable.setZh(localizable.getName().getString());
						frame.addProcess(delta);//0.77-0.84
					}
				}
				Language.inject(rawl);
			}
		}
		LOGGER.info("完成获取语言: {}", partStt.getStringAndReset());
	}
	
	private void write() throws IOException {
		LOGGER.info("开始写入");
		String head;
		StringBuilder headBuilder = new StringBuilder();
		try {
			headBuilder.append("#provider ").append(MOD_ID).append("@" + modVersion)
					.append("\n#target ").append(modid).append("@").append(RR.getVersion(modid))
					.append("\n#env minecraft@").append(RR.getVersion("minecraft")).append("&").append(RR.getPlatform().toString().toLowerCase());
			
		} catch (Exception e) {
			LOGGER.info("文件头获取失败");
			softFail(e);
		}
		head = headBuilder.toString();
		frame.setTextMajor("序列化");
		
		//0.84
		frame.setText("序列化配方");
		List<String> rl = storeRaw(recipes.stream());
		frame.addProcess(0.01f);
		frame.setText("序列化物品");
		var il = store(items);
		frame.addProcess(0.02f);
		frame.setText("序列化药水效果");
		var effl = store(effects);
		frame.addProcess(0.01f);
		frame.setText("序列化附魔");
		var enl = store(enchantments);
		frame.addProcess(0.01f);
		frame.setText("序列化实体");
		var el = store(entities);
		frame.addProcess(0.02f);
		frame.setText("序列化群系");
		var biol = store(biomes);
		frame.addProcess(0.01f);
		//0.90
		
		
		frame.setTextMajor("写入 output.zip");
		try (var out = outputs_z) {
			if (!rl.isEmpty()) {
				frame.setText("写入 recipes.jsons");
				write(out, "recipes.jsons", rl, head + "\n#types " + recipeTypes);
			}
			if (il != null) {
				frame.setText("写入 items.jsons");
				write(out, "items.jsons", il.getFirst(), head + "\n#format " + il.getSecond());
			}
			if (el != null) {
				frame.setText("写入 entites.jsons");
				write(out, "entites.jsons", el.getFirst(), head + "\n#format " + el.getSecond());
			}
			if (effl != null) {
				frame.setText("写入 effects.jsons");
				write(out, "effects.jsons", effl.getFirst(), head + "\n#format " + effl.getSecond());
			}
			if (enl != null) {
				frame.setText("写入 enchantments.jsons");
				write(out, "enchantments.jsons", enl.getFirst(), head + "\n#format " + enl.getSecond());
			}
			if (biol != null) {
				frame.setText("写入 biomes.jsons");
				write(out, "biomes.jsons", biol.getFirst(), head + "\n#format " + biol.getSecond());
			}
		}
		frame.addProcess(0.05f);
		
		frame.setTextMajor("写入 images.zip");
		try (var out = images_z) {
			for (ItemEntry item : items) {
				String s = item.id.split(":", 2)[1] + ".png";
				frame.setText("写入 item32/" + s);
				out.putNextEntry(createEntry("item32/" + s));
				out.write(item.ico32.getImage().asByteArray());
				frame.setText("写入 item128/" + s);
				out.putNextEntry(createEntry("item128/" + s));
				out.write(item.ico128.getImage().asByteArray());
			}
			for (EntityEntry entity : entities) {
				String s = entity.id.split(":", 2)[1] + ".png";
				frame.setText("写入 entity/" + s);
				out.putNextEntry(createEntry("entity/" + s));
				out.write(entity.ico.getImage().asByteArray());
			}
			for (EffectEntry effect : effects) {
				String s = effect.id.split(":", 2)[1] + ".png";
				frame.setText("写入 effect/" + s);
				out.putNextEntry(createEntry("effect/" + s));
				out.write(effect.icoRaw);
				
			}
		}
		frame.setTextMajor("完毕！");
		frame.finished(root);
		
		LOGGER.info("完成写入: {}", partStt.getStringAndReset());
		
	}
	
	private ZipEntry createEntry(String path) {
		ZipEntry entry = new ZipEntry(path);
		return entry;
	}
	
	void write(ZipOutputStream out, String fileName, List<String> lines, String head) throws IOException {
		out.putNextEntry(createEntry(fileName));
		var writer = new StringBuilder();
		writer.append(head);
		for (String line : lines) {
			writer.append("\n").append(line);
		}
		out.write(writer.toString().getBytes(StandardCharsets.UTF_8));
		out.closeEntry();
	}
	
	private Pair<List<String>, String> store(List<? extends Storable> storables) {
		if (storables.isEmpty()) return null;
		var ref = new Object() {
			String format = null;
		};
		List<String> st = storeRaw(storables.stream().map(storable -> {
			Pair<JsonObject, String> stored = dataStorer.store(storable);
			if (ref.format == null) ref.format = stored.getSecond();
			else if (!ref.format.equals(stored.getSecond())) {
				throw new UnsupportedOperationException("同一类型的条目导出格式不同");
			}
			return stored.getFirst();
		}));
		return Pair.of(st, ref.format);
	}
	
	private List<String> storeRaw(Stream<JsonObject> objects) {
		LinkedList<String> list = new LinkedList<>();
		objects.forEach(s -> list.add(s.toString()));
		return list;
	}
}
