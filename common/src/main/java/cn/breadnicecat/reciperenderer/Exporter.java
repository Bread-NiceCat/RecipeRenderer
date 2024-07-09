package cn.breadnicecat.reciperenderer;

import cn.breadnicecat.reciperenderer.cmd.ICmdFeedback;
import cn.breadnicecat.reciperenderer.entry.*;
import cn.breadnicecat.reciperenderer.utils.LogUtils;
import cn.breadnicecat.reciperenderer.utils.Timer;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.mojang.blaze3d.systems.RenderSystem;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.ChatFormatting;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.language.ClientLanguage;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.locale.Language;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.item.Item;
import org.slf4j.Logger;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Stream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import static cn.breadnicecat.reciperenderer.RecipeRenderer.*;
import static net.minecraft.network.chat.Component.empty;
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
	final static Logger LOGGER = LogUtils.getModLogger();
	final static Gson GSON = new GsonBuilder().disableHtmlEscaping().create();
	private final Base64.Encoder BASE64 = Base64.getEncoder();
	String modid;
	ICmdFeedback feedback;
	Minecraft instance;
	Timer stt;
	ExportProcessFrame frame;
	File root;
	
	FileOutputStream outputs_zip;
	FileOutputStream images_zip;
	
	List<ItemEntry> items = new LinkedList<>();
	List<EntityEntry> entities = new LinkedList<>();
	List<EffectEntry> effects = new LinkedList<>();
	List<List<? extends Localizable>> locals = List.of(items, entities, effects);
	List<JsonObject> recipes = new LinkedList<>();
	Set<String> recipeTypes = new TreeSet<>();
	
	public Exporter() {
	}
	
	public void run(final String modid, final ICmdFeedback feedback) {
		stt = new Timer();
		RenderSystem.assertOnRenderThread();
		
		this.modid = modid;
		this.feedback = feedback;
		
		ExportProcessFrame frame;
		try {
			frame = new ExportProcessFrame(modid);
		} catch (Exception e) {
			fail(e);
			feedback.sendError(literal("无法创建窗口"));
			frame = ExportProcessFrame.empty();
		}
		this.frame = frame;
		
		
		feedback.sendFeedback(literal("开始导出mod: " + modid));
		instance = Minecraft.getInstance();
		this.root = new File(instance.gameDirectory, "rr_export/" + modid);
		root.mkdirs();
		//防止出现最后因为文件而无法导出的情况
		
		try {
			outputs_zip = new FileOutputStream(new File(root, "output.zip"));
			images_zip = new FileOutputStream(new File(root, "images.zip"));
			collectRenderMust();
		} catch (Exception e) {
			fail(e);
			return;
		}
		Util.backgroundExecutor().submit(() -> {
			boolean fail = false;
			try {
				collectRecipe();
				collectEffect();
				collectLang();
				write();
			} catch (Exception e) {
				fail = true;
				fail(e);
			}
			if (!fail) {
				feedback.sendFeedback(literal(modid + "导出完成").withStyle(ChatFormatting.GREEN));
				String cons = "共计导出了%d条配方(涉及%d个类型),%d个物品,%d个药水效果,%d个实体".formatted(recipes.size(), recipeTypes.size(), items.size(), effects.size(), entities.size());
				feedback.sendFeedback(literal(cons));
				this.frame.setTextMajor(cons);
				String conTime = "用时" + stt.getString();
				feedback.sendFeedback(literal(conTime));
				this.frame.setText(conTime);
			}
			feedback.sendFeedback(empty());
		});
	}
	
	private void fail(Exception e) {
		e.printStackTrace();
		frame.setTextMajor("出错啦！(详情请看日志)");
		frame.setText(e.getMessage());
		frame.invalidate();
		feedback.sendError(literal(" 导出" + modid + "时遇到错误: " + e.getMessage()).withStyle(ChatFormatting.RED));
	}
	
	private void collectRenderMust() {
		Timer stt = new Timer();
		LOGGER.info("开始获取物品");
		frame.setTextMajor("获取物品");
		var iilist = BuiltInRegistries.ITEM.entrySet().stream()
				.filter(i -> i.getKey().location().getNamespace().equals(modid))
				.toList();
		
		float idelta = 0.3f / iilist.size();
		iilist.forEach(i -> {
			frame.setText("添加物品: " + i.getKey().location());
			Item value = i.getValue();
			items.add(new ItemEntry(i.getKey().location(), value.getDefaultInstance()));
			frame.addProcess(idelta, 0, 0.3f);
		});
		LOGGER.info("完成获取物品: {}", stt.getStringAndReset());
//		CreativeModeTabs.tryRebuildTabContents(instance.getConnection().enabledFeatures(), true, instance.getConnection().registryAccess());
//		LOGGER.info("rebuild: {}", stt.getStringAndReset());
//		Set<ResourceLocation> blackList = Set.of(getHOTBAR(), getINVENTORY(), getSEARCH()).stream().map(i -> i.location()).collect(Collectors.toSet());
//		List<CreativeModeTab> tabs = CreativeModeTabs.allTabs();
//		for (CreativeModeTab group : tabs) {
//			ResourceLocation tabKey = BuiltInRegistries.CREATIVE_MODE_TAB.getKey(group);
//			if (blackList.contains(tabKey)) continue;
//
//			frame.addProcess(400 / tabs.size(), 0, 400);
//			frame.setTextMajor("搜索物品栏: " + tabKey);
//
//			LOGGER.info("\tnext: {}", stt.getStringAndReset());
//			Collection<ItemStack> itemStacks = group.getDisplayItems();
//			LOGGER.info("\tgetItem: {}", stt.getStringAndReset());
//			for (ItemStack stack : itemStacks) {
//				ResourceLocation id = ITEM.getKey(stack.getItem());
//				if (id.getNamespace().equals(modid)) {
//
//					frame.setText("添加物品: " + id);
//					ItemEntry e = new ItemEntry(id, stack);
//					frame.addProcess(10, 0, 400);
//					items.add(e);
//				}
//			}
//			LOGGER.info("\texit: {}", stt.getStringAndReset());
//			LOGGER.info("");
//
//		}
		LOGGER.info("开始获取实体");
		frame.setTextMajor("获取实体");
		var eelist = BuiltInRegistries.ENTITY_TYPE.entrySet().stream()
				.filter(i -> i.getKey().location().getNamespace().equals(modid))
				.toList();
		float edelta = 0.2f / eelist.size();
		eelist.forEach(i -> {
			frame.setText("添加实体: " + i.getKey().location());
			Entity entity = i.getValue().create(instance.level);
			if (entity instanceof Mob) entities.add(new EntityEntry(i.getKey().location(), entity));
			frame.addProcess(edelta, 0.3f, 0.4f);
		});
		LOGGER.info("完成获取实体: {}", stt.getString());
		
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
			frame.setText("添加药水效果: " + i.getKey().location());
			MobEffect value = i.getValue();
			EffectEntry e = new EffectEntry(i.getKey().location().toString(), value);
			Optional<Resource> resource = manager.getResource(i.getKey().location().withPrefix("textures/mob_effect/").withSuffix(".png"));
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
		LOGGER.info("完成获取药水效果: {}", stt.getString());
		
	}
	
	private void collectRecipe() throws IOException {
		Timer stt = new Timer();
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
		LOGGER.info("完成获取配方: {}", stt.getString());
	}
	
	private void collectLang() {
		Timer stt = new Timer();
		LOGGER.info("开始获取语言");
		frame.setTextMajor("获取语言");
		ClientLanguage zh = ClientLanguage.loadFrom(instance.getResourceManager(), List.of("en_us", "zh_cn"), false);
		ClientLanguage en = ClientLanguage.loadFrom(instance.getResourceManager(), List.of("en_us"), false);
		float delta = 0.14f / (items.size() + entities.size());
		Language rawl = Language.getInstance();
		for (List<? extends Localizable> s : locals) {
			frame.setText("获取I18n(英)");
			Language.inject(en);
			for (Localizable localizable : s) {
				localizable.setEn(localizable.getName().getString());
				frame.addProcess(delta, 0.70f, 0.84f);
			}
			frame.setText("获取I18n(英)");
			Language.inject(zh);
			for (Localizable localizable : s) {
				localizable.setZh(localizable.getName().getString());
				frame.addProcess(delta, 0.70f, 0.84f);
			}
		}
		Language.inject(rawl);
		LOGGER.info("完成获取语言: {}", stt.getString());
		
	}
	
	private void write() throws IOException {
		Timer stt = new Timer();
		LOGGER.info("开始写入");
		
		String head = "#format " + MOD_ID + "@" + MOD_VERSION +
				"\n#env minecraft@" + getVersion("minecraft") + "&" + getPlatform().toString().toLowerCase() +
				"\n#target " + modid + "@" + getVersion(modid);
		
		frame.setTextMajor("序列化");
		
		frame.setText("序列化配方");
		List<String> rl = storeRaw(recipes.stream());
		frame.addProcess(0.02f);//0.84
		frame.setText("序列化物品");
		List<String> il = store(items);
		frame.addProcess(0.02f);//0.86
		frame.setText("序列化药水效果");
		List<String> effl = store(effects);
		frame.addProcess(0.02f);//0.86
		frame.setText("序列化实体");
		List<String> el = store(entities);
		frame.addProcess(0.02f);//0.90
		
		
		frame.setTextMajor("写入 output.zip");
		try (var out = new ZipOutputStream(outputs_zip)) {
			if (!rl.isEmpty()) {
				frame.setTextMajor("写入 recipes.jsons");
				write(out, "recipes.jsons", rl, head + "\n#types " + recipeTypes);
			}
			if (!il.isEmpty()) {
				frame.setTextMajor("写入 items.jsons");
				write(out, "items.jsons", il, head);
			}
			if (!el.isEmpty()) {
				frame.setTextMajor("写入 entites.jsons");
				write(out, "entites.jsons", el, head);
			}
			if (!effl.isEmpty()) {
				frame.setTextMajor("写入 effects.jsons");
				write(out, "effects.jsons", effl, head);
			}
		}
		frame.addProcess(0.05f);
		
		frame.setTextMajor("写入 images.zip");
		try (var out = new ZipOutputStream(images_zip)) {
			for (ItemEntry item : items) {
				String s = item.id.split(":", 2)[1] + ".png";
				frame.setText("写入 item32/" + s);
				out.putNextEntry(new ZipEntry("item32/" + s));
				out.write(item.ico32.getImage().asByteArray());
				frame.setText("写入 item128/" + s);
				out.putNextEntry(new ZipEntry("item128/" + s));
				out.write(item.ico128.getImage().asByteArray());
			}
			for (EntityEntry entity : entities) {
				String s = entity.id.split(":", 2)[1] + ".png";
				frame.setText("写入 entity/" + s);
				out.putNextEntry(new ZipEntry("entity/" + s));
				out.write(entity.ico.getImage().asByteArray());
			}
			for (EffectEntry effect : effects) {
				String s = effect.id.split(":", 2)[1] + ".png";
				frame.setText("写入 effect/" + s);
				out.putNextEntry(new ZipEntry("effect/" + s));
				out.write(effect.icoRaw);
				
			}
		}
		frame.setTextMajor("完毕！");
		frame.finished(root);
		
		LOGGER.info("完成写入: {}", stt.getString());
		
	}
	
	void write(ZipOutputStream out, String fileName, List<String> lines, String head) throws IOException {
		out.putNextEntry(new ZipEntry(fileName));
		OutputStreamWriter writer = new OutputStreamWriter(out, StandardCharsets.UTF_8);
		writer.write(head);
		for (String line : lines) {
			writer.write("\n");
			writer.write(line);
		}
		writer.flush();
		out.closeEntry();
	}
	
	private static List<String> store(List<? extends Storable> storables) {
		return storeRaw(storables.stream().map(i -> {
			JsonObject object = new JsonObject();
			i.store(object);
			return object;
		}));
	}
	
	private static List<String> storeRaw(Stream<JsonObject> objects) {
		LinkedList<String> list = new LinkedList<>();
		objects.forEach(s -> list.add(s.toString()));
		return list;
	}
}
