package com.teamwizardry.wizardry.common.module.shapes;

import com.teamwizardry.librarianlib.features.math.interpolate.StaticInterp;
import com.teamwizardry.librarianlib.features.math.interpolate.numeric.InterpFloatInOut;
import com.teamwizardry.librarianlib.features.particle.ParticleBuilder;
import com.teamwizardry.librarianlib.features.particle.ParticleSpawner;
import com.teamwizardry.librarianlib.features.particle.functions.InterpColorHSV;
import com.teamwizardry.wizardry.Wizardry;
import com.teamwizardry.wizardry.api.Constants;
import com.teamwizardry.wizardry.api.spell.SpellData;
import com.teamwizardry.wizardry.api.spell.SpellRing;
import com.teamwizardry.wizardry.api.spell.annotation.ModuleOverride;
import com.teamwizardry.wizardry.api.spell.annotation.RegisterModule;
import com.teamwizardry.wizardry.api.spell.module.IModuleShape;
import com.teamwizardry.wizardry.api.spell.module.ModuleInstanceShape;
import com.teamwizardry.wizardry.api.util.RandUtil;
import com.teamwizardry.wizardry.api.util.interp.InterpScale;
import net.minecraft.entity.Entity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nonnull;

/**
 * Created by Demoniaque.
 */
@RegisterModule(ID="shape_self")
public class ModuleShapeSelf implements IModuleShape {

	@Override
	public boolean run(@NotNull World world, ModuleInstanceShape instance, @Nonnull SpellData spell, @Nonnull SpellRing spellRing) {
		Entity caster = spell.getCaster(world);
		if (caster == null) return false;

		if (!spellRing.taxCaster(world, spell, true)) return false;
		
		IShapeOverrides overrides = spellRing.getOverrideHandler().getConsumerInterface(IShapeOverrides.class);
		overrides.onRunSelf(world, spell, spellRing);
		
		spell.processEntity(caster, false);

		return true;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void renderSpell(World world, ModuleInstanceShape instance, @Nonnull SpellData spell, @Nonnull SpellRing spellRing) {
		IShapeOverrides overrides = spellRing.getOverrideHandler().getConsumerInterface(IShapeOverrides.class);
		if (overrides.onRenderSelf(world, spell, spellRing)) return;

		Entity caster = spell.getCaster(world);

		if (caster == null) return;

		ParticleBuilder glitter = new ParticleBuilder(30);
		glitter.setRender(new ResourceLocation(Wizardry.MODID, Constants.MISC.SPARKLE_BLURRED));
		glitter.setAlphaFunction(new InterpFloatInOut(0.3f, 0.3f));
		glitter.enableMotionCalculation();
		ParticleSpawner.spawn(glitter, world, new StaticInterp<>(caster.getPositionVector()), 50, 5, (i, build) -> {
			double radius = 1;
			double theta = 2.0f * (float) Math.PI * RandUtil.nextFloat();
			double r = radius * RandUtil.nextFloat();
			double x = r * MathHelper.cos((float) theta);
			double z = r * MathHelper.sin((float) theta);
			build.setPositionOffset(new Vec3d(
					RandUtil.nextDouble(-0.5, 0.5),
					RandUtil.nextDouble(-0.5, 0.5),
					RandUtil.nextDouble(-0.5, 0.5)
			));
			build.setScaleFunction(new InterpScale(RandUtil.nextFloat(0.2f, 1f), 0f));
			build.setMotion(new Vec3d(x, RandUtil.nextDouble(radius / 2.0, radius), z).normalize().scale(RandUtil.nextFloat()));
			build.setAcceleration(Vec3d.ZERO);
			build.setLifetime(50);
			build.setDeceleration(new Vec3d(0.8, 0.8, 0.8));

			if (RandUtil.nextBoolean()) {
				build.setColorFunction(new InterpColorHSV(spellRing.getPrimaryColor(), spellRing.getSecondaryColor()));
			} else {
				build.setColorFunction(new InterpColorHSV(spellRing.getSecondaryColor(), spellRing.getPrimaryColor()));
			}
		});

	}
	
	///////////////////
	
	@ModuleOverride("shape_self_run")
	public void onRunSelf(World world, SpellData data, SpellRing shape) {
		// Default implementation
	}
	
	@ModuleOverride("shape_self_render")
	public boolean onRenderSelf(World world, SpellData data, SpellRing shape) {
		// Default implementation
		return false;
	}
}
