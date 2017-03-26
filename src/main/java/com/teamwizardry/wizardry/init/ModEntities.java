package com.teamwizardry.wizardry.init;

import com.teamwizardry.wizardry.Wizardry;
import com.teamwizardry.wizardry.client.render.entity.*;
import com.teamwizardry.wizardry.common.entity.*;
import com.teamwizardry.wizardry.common.entity.gods.EntityGavreel;
import net.minecraft.entity.Entity;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.client.registry.RenderingRegistry;
import net.minecraftforge.fml.common.registry.EntityRegistry;

//github.com/TeamWizardry/Wizardry

/**
 * Created by Saad on 8/17/2016.
 */
public class ModEntities {

	private static int i = 0;

	public static void init() {
		registerEntity(new ResourceLocation(Wizardry.MODID, "spirit_wight"), EntitySpiritWight.class, "spirit_wight", 64, 3, true);
		registerEntity(new ResourceLocation(Wizardry.MODID, "gavreel"), EntityGavreel.class, "gavreel", 64, 3, true);
		registerEntity(new ResourceLocation(Wizardry.MODID, "fairy"), EntityFairy.class, "fairy", 64, 3, true);
		registerEntity(new ResourceLocation(Wizardry.MODID, "dust_tracker"), EntityDevilDust.class, "dust_tracker", 64, 1, false);
		registerEntity(new ResourceLocation(Wizardry.MODID, "book_tracker"), EntitySpellCodex.class, "book_tracker", 64, 1, false);
		registerEntity(new ResourceLocation(Wizardry.MODID, "spell_projectile"), EntitySpellProjectile.class, "spell_projectile", 64, 1, false);
		registerEntity(new ResourceLocation(Wizardry.MODID, "jump_pad"), EntityJumpPad.class, "jump_pad", 64, 1, false);
		registerEntity(new ResourceLocation(Wizardry.MODID, "unicorn"), EntityUnicorn.class, "unicorn");
	}

	public static void registerEntity(ResourceLocation loc, Class<? extends Entity> entityClass, String entityName) {
		registerEntity(loc, entityClass, entityName, 64, 1, true);
	}

	//Use when default parameters are not sufficient, e.g fast-moving projectiles
	public static void registerEntity(ResourceLocation loc, Class<? extends Entity> entityClass, String entityName, int trackingRange, int updateFrequency, boolean sendsVelocityUpdates) {
		EntityRegistry.registerModEntity(loc, entityClass, entityName, i, Wizardry.instance, trackingRange, updateFrequency, sendsVelocityUpdates);
		i++;
	}

	public static void initModels() {
		RenderingRegistry.registerEntityRenderingHandler(EntityGavreel.class, manager -> new RenderGavreel(manager, new ModelGavreel()));
		RenderingRegistry.registerEntityRenderingHandler(EntitySpiritWight.class, manager -> new RenderSpiritWight(manager, new ModelSpiritWight()));
		RenderingRegistry.registerEntityRenderingHandler(EntityFairy.class, manager -> new RenderFairy(manager, new ModelNull()));
		RenderingRegistry.registerEntityRenderingHandler(EntityUnicorn.class, manager -> new RenderUnicorn(manager, new ModelUnicorn()));
		RenderingRegistry.registerEntityRenderingHandler(EntityJumpPad.class, manager -> new RenderJumpPad(manager, new ModelNull()));
	}
}
