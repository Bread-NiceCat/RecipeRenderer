package cn.breadnicecat.reciperenderer.datafix;

import cn.breadnicecat.reciperenderer.entry.EntityEntry;
import cn.breadnicecat.reciperenderer.entry.ItemEntry;
import cn.breadnicecat.reciperenderer.entry.Storable;
import com.google.gson.JsonObject;
import com.mojang.datafixers.util.Pair;

import java.util.Arrays;

/**
 * Created in 2024/7/10 上午9:02
 * Project: reciperenderer
 *
 * @author <a href="https://github.com/Bread-Nicecat">Bread_NiceCat</a>
 * <p>
 *
 * <p>
 **/
public class IconRStorer implements DataStorer {
	public static final IconRStorer DEFAULT = new IconRStorer(RRStorer.INSTANCE);
	private final DataStorer parent;
	
	public IconRStorer(DataStorer parent) {
		this.parent = parent;
	}
	
	@Override
	public Pair<JsonObject, String> store(Storable storable) {
		JsonObject object = new JsonObject();
		if (storable instanceof ItemEntry ie) {
			//result.addProperty("name", this.zhName);
			//result.addProperty("englishName",this.enName);
			//result.addProperty("registerName",this.regName);
			//result.addProperty("CreativeTabName",this.creativeTab);
			//result.addProperty("type", this.type.tostring());
			//result.addProperty("oredictList",this.tags);
			//result.addProperty("maxStackssize",this.maxstacksize);
			//result.addProperty("maxDurability",this.maxDurability);
			//result.addProperty("smallIcon",this.smallIcon);
			//result.addProperty("largeIcon",this.largeIcon);
			object.addProperty("name", ie.zh);
			object.addProperty("englishName", ie.en);
			object.addProperty("registerName", ie.id);
			object.add("CreativeTabName", null);
			object.addProperty("type", "item");
			object.addProperty("oredictList", Arrays.toString(ie.tags));
			object.addProperty("maxStackssize", ie.stackSize);
			object.addProperty("maxDurability", ie.durability);
			object.addProperty("smallIcon", ie.ico32.getBase64());
			object.addProperty("largeIcon", ie.ico128.getBase64());
			return Pair.of(object, "iconr");
		} else if (storable instanceof EntityEntry ee) {
			//result.addProperty("name",this.zhName);
			//result.addProperty("englishName", this.enName):
			//result.addProperty("registerName", this.regName
			//result.addProperty("mod", this.mod);
			//result.addProperty("type",this.type.tostring()
			//result.addProperty("Icon",this.icon);
			object.addProperty("name", ee.zh);
			object.addProperty("englishName", ee.en);
			object.addProperty("registerName", ee.id);
			object.addProperty("mod", ee.id.split(":", 2)[0]);
			object.addProperty("type", "entity");
			object.addProperty("Icon", ee.ico.getBase64());
			return Pair.of(object, "iconr");
		} else return parent.store(storable);
	}
}
