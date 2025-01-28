package old;

import cn.breadnicecat.reciperenderer.utils.RRUtils;
import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.platform.Lighting;
import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexSorting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import org.joml.Matrix4f;
import org.joml.Matrix4fStack;

import java.util.concurrent.ExecutionException;


/**
 * Created in 2024/7/8 下午9:35
 * Project: reciperenderer
 *
 * @author <a href="https://github.com/Bread-Nicecat">Bread_NiceCat</a>
 * <p>
 * 参考 <a href="https://github.com/Nova-Committee/IconR/blob/master/src/main/java/top/gregtao/iconrenderer/utils/ImageHelper.java">IconR</a>,
 * <p>
 * 参考{@link net.minecraft.client.gui.GuiGraphics#renderItem(LivingEntity, net.minecraft.world.level.Level, ItemStack, int, int, int, int)}
 * <p>
 **/
public class ItemIcon1 {
	private final Minecraft instance = Minecraft.getInstance();
	private final LivingEntity holder;
	RenderTarget target;
	final int size;
	ItemStack item;
	public byte[] image;
	
	public ItemIcon1(int size, ItemStack item) {
		this(size, item, null);
	}
	
	public ItemIcon1(int size, ItemStack item, LivingEntity holder) {
		this.size = size;
		this.item = item;
		this.holder = holder;
		//开始
		
		try {
			image = RRUtils.render(size, size, (graphics) -> {
				RenderSystem.backupProjectionMatrix();
				Matrix4f p = new Matrix4f().setOrtho(0, 16, 16, 0, -1000, 1000);
				RenderSystem.setProjectionMatrix(p, VertexSorting.ORTHOGRAPHIC_Z);
				//渲染
				ItemRenderer renderer = instance.getItemRenderer();
				BakedModel model = renderer.getModel(item, holder == null ? null : holder.level(), holder, 0);
				
				RenderSystem.setShaderTexture(0, InventoryMenu.BLOCK_ATLAS);
				RenderSystem.enableBlend();
				RenderSystem.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
				RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
				Matrix4fStack modelViewStack = RenderSystem.getModelViewStack();
				modelViewStack.pushMatrix();
				{
					modelViewStack.translate(8.0f, 8.0f, (model.isGui3d() ? 150 : 0));
					modelViewStack.scale(16.0F, -16.0F, 16.0F);
					RenderSystem.applyModelViewMatrix();
					MultiBufferSource.BufferSource immediate = instance.renderBuffers().bufferSource();
					boolean flag = !model.usesBlockLight();
					if (flag) {
						Lighting.setupForFlatItems();
					}
					renderer.render(item, ItemDisplayContext.GUI, false, new PoseStack(), immediate, 0xf000f0, OverlayTexture.NO_OVERLAY, model);
					immediate.endBatch();
					if (flag) {
						Lighting.setupFor3DItems();
					}
					RenderSystem.enableDepthTest();
				}
				modelViewStack.popMatrix();
				//收尾
				RenderSystem.restoreProjectionMatrix();
			}, NativeImage::flipY).get();
		} catch (InterruptedException | ExecutionException e) {
			throw new RuntimeException(e);
		}
	}
	
}