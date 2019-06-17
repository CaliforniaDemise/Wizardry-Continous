package com.teamwizardry.wizardry.client.render.block;

import com.teamwizardry.librarianlib.core.client.ClientTickHandler;
import com.teamwizardry.librarianlib.features.math.interpolate.StaticInterp;
import com.teamwizardry.librarianlib.features.math.interpolate.numeric.InterpFloatInOut;
import com.teamwizardry.librarianlib.features.math.interpolate.position.InterpCircle;
import com.teamwizardry.librarianlib.features.particle.ParticleBuilder;
import com.teamwizardry.librarianlib.features.particle.ParticleSpawner;
import com.teamwizardry.librarianlib.features.tesr.TileRenderHandler;
import com.teamwizardry.librarianlib.features.utilities.client.ClientRunnable;
import com.teamwizardry.wizardry.Wizardry;
import com.teamwizardry.wizardry.api.Constants;
import com.teamwizardry.wizardry.api.block.IStructure;
import com.teamwizardry.wizardry.api.capability.player.mana.ManaManager;
import com.teamwizardry.wizardry.api.util.ColorUtils;
import com.teamwizardry.wizardry.api.util.RandUtil;
import com.teamwizardry.wizardry.client.core.renderer.StructureErrorRenderer;
import com.teamwizardry.wizardry.common.tile.TileManaBattery;
import com.teamwizardry.wizardry.init.ModBlocks;
import com.teamwizardry.wizardry.init.ModStructures;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.Vec3i;
import net.minecraftforge.client.model.IModel;
import net.minecraftforge.client.model.ModelLoaderRegistry;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.opengl.GL11;

import javax.annotation.Nonnull;
import java.awt.*;
import java.util.ArrayList;

/**
 * Created by Demoniaque.
 */
@SideOnly(Side.CLIENT)
@Mod.EventBusSubscriber(value = Side.CLIENT, modid = Wizardry.MODID)
public class TileManaBatteryRenderer extends TileRenderHandler<TileManaBattery> {

	private static IBakedModel modelRing, modelCrystal, modelRingOuter;

	public TileManaBatteryRenderer(@Nonnull TileManaBattery manaBattery) {
		super(manaBattery);
	}

	static {
		ClientRunnable.registerReloadHandler(() -> {
			modelRing = null;
			modelCrystal = null;
			modelRingOuter = null;
		});
	}

	private static boolean getBakedModels() {
		IModel model;
		if (modelRing == null) {
			try {
				model = ModelLoaderRegistry.getModel(new ResourceLocation(Wizardry.MODID, "block/mana_crystal_ring"));
				modelRing = model.bake(model.getDefaultState(), DefaultVertexFormats.ITEM,
						location -> Minecraft.getMinecraft().getTextureMapBlocks().getAtlasSprite(location.toString()));
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		if (modelRingOuter == null) {
			try {
				model = ModelLoaderRegistry.getModel(new ResourceLocation(Wizardry.MODID, "block/mana_crystal_ring_outer"));
				modelRingOuter = model.bake(model.getDefaultState(), DefaultVertexFormats.ITEM,
						location -> Minecraft.getMinecraft().getTextureMapBlocks().getAtlasSprite(location.toString()));
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		if (modelCrystal == null) {
			try {
				model = ModelLoaderRegistry.getModel(new ResourceLocation(Wizardry.MODID, "block/mana_crystal"));
				modelCrystal = model.bake(model.getDefaultState(), DefaultVertexFormats.ITEM,
						location -> Minecraft.getMinecraft().getTextureMapBlocks().getAtlasSprite(location.toString()));
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		return modelRing != null && modelRingOuter != null && modelCrystal != null;
	}

	@Override
	public void render(float partialTicks, int destroyStage, float alpha) {
		if (!getBakedModels()) return;

		GlStateManager.pushMatrix();
		GlStateManager.enableBlend();
		GlStateManager.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);

		TextureManager texturemanager = Minecraft.getMinecraft().renderEngine;

		if (texturemanager != null) {
			texturemanager.bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);
		}

		if (Minecraft.isAmbientOcclusionEnabled())
			GlStateManager.shadeModel(GL11.GL_SMOOTH);
		else GlStateManager.shadeModel(GL11.GL_FLAT);

		float fill = (float) (ManaManager.getMana(tile.getWizardryCap()) / ManaManager.getMaxMana(tile.getWizardryCap())) / 40.0f;


		GlStateManager.translate(0, 0.5, 0);
		GlStateManager.disableRescaleNormal();

		{
			double x = RandUtil.nextDouble(-fill, fill), y = RandUtil.nextDouble(-fill, fill), z = RandUtil.nextDouble(-fill, fill);
			GlStateManager.translate(0, Math.sin((ClientTickHandler.getTicks() + ClientTickHandler.getPartialTicks()) / 40) / 8, 0);

			GlStateManager.translate(x, y, z);
			Minecraft.getMinecraft().getBlockRendererDispatcher().getBlockModelRenderer().renderModelBrightnessColor(modelCrystal, 1.0F, 1, 1, 1);
			GlStateManager.translate(-x, -y, -z);
		}

		GlStateManager.translate(0.5, 0, 0.5);
		GlStateManager.rotate(ClientTickHandler.getTicks() + ClientTickHandler.getPartialTicks(), 0, 1, 0);
		GlStateManager.translate(-0.5, 0, -0.5);

		Minecraft.getMinecraft().getBlockRendererDispatcher().getBlockModelRenderer().renderModelBrightnessColor(modelRing, 1.0F, 1, 1, 1);

		GlStateManager.translate(0.5, 0, 0.5);
		GlStateManager.rotate(ClientTickHandler.getTicks() + ClientTickHandler.getPartialTicks(), 0, -1, 0);
		GlStateManager.rotate(ClientTickHandler.getTicks() + ClientTickHandler.getPartialTicks(), 0, -1, 0);
		GlStateManager.translate(-0.5, 0, -0.5);

		Minecraft.getMinecraft().getBlockRendererDispatcher().getBlockModelRenderer().renderModelBrightnessColor(modelRingOuter, 1.0F, 1, 1, 1);

		GlStateManager.disableBlend();
		GlStateManager.popMatrix();

		ArrayList<BlockPos> errors = new ArrayList<>(((IStructure) tile.getBlockType()).testStructure(tile.getWorld(), tile.getPos()));
		errors.sort(Vec3i::compareTo);

		if (!errors.isEmpty()) {
			for (BlockPos error : errors) {
				StructureErrorRenderer.addError(error);
			}
		}

		if (!errors.isEmpty() && tile.revealStructure && tile.getBlockType() instanceof IStructure) {
			ModStructures.structureManager.draw(ModStructures.MANA_BATTERY, (float) (Math.sin(ClientTickHandler.getTicks() / 10.0) + 1) / 10.0f + 0.3f);
			return;
		}

		if (tile.getBlockType() == ModBlocks.CREATIVE_MANA_BATTERY) {
			double angle = ClientTickHandler.getTicks() / 10.0;
			double x1 = Math.cos((float) angle);
			double y1 = Math.sin((float) angle);

			ParticleBuilder builder = new ParticleBuilder(10);
			builder.setRender(new ResourceLocation(Wizardry.MODID, Constants.MISC.SPARKLE_BLURRED));
			builder.setCollision(true);
			builder.disableRandom();
			builder.disableMotionCalculation();

			ParticleSpawner.spawn(builder, tile.getWorld(), new InterpCircle(new Vec3d(tile.getPos()).add(0.5, 0.5, 0.5), new Vec3d(x1, x1, y1), 1.5f), 20, 0, (aFloat, particleBuilder) -> {
				particleBuilder.setScale(0.5f);
				particleBuilder.setColor(new Color(0xd600d2));
				particleBuilder.setAlphaFunction(new InterpFloatInOut(1, 1));
				particleBuilder.setLifetime(RandUtil.nextInt(5, 10));
			});
		} else if (ClientTickHandler.getTicks() % 10 == 0 && ManaManager.forObject(tile.getWizardryCap()).isManaFull()) {
			double radius = 1;
			double theta = 2.0f * (float) Math.PI * RandUtil.nextFloat();
			double r = radius * RandUtil.nextFloat();
			double x = r * MathHelper.cos((float) theta);
			double z = r * MathHelper.sin((float) theta);

			ParticleBuilder helix = new ParticleBuilder(20);
			helix.setRender(new ResourceLocation(Wizardry.MODID, Constants.MISC.SPARKLE_BLURRED));
			helix.setAlphaFunction(new InterpFloatInOut(0.1f, 0.4f));
			ParticleSpawner.spawn(helix, tile.getWorld(), new StaticInterp<>(new Vec3d(tile.getPos()).add(0.5, 0.5, 0.5)), 1, 0, (someFloat, particleBuilder) -> {
				particleBuilder.setColor(ColorUtils.changeColorAlpha(new Color(0x0097FF), RandUtil.nextInt(50, 200)));
				particleBuilder.setScale(RandUtil.nextFloat(0.5f, 1.2f));
				particleBuilder.setMotion(new Vec3d(x, RandUtil.nextDouble(-1, 1), z).scale(0.05));
				particleBuilder.setLifetime(RandUtil.nextInt(20, 40));
			});
		}
	}

}
