package com.teamwizardry.wizardry.common.module.effects;

import com.teamwizardry.wizardry.api.spell.SpellData;
import com.teamwizardry.wizardry.api.spell.SpellRing;
import com.teamwizardry.wizardry.api.spell.annotation.RegisterModule;
import com.teamwizardry.wizardry.api.spell.attribute.AttributeRegistry;
import com.teamwizardry.wizardry.api.spell.module.IModuleEffect;
import com.teamwizardry.wizardry.api.spell.module.ModuleInstanceEffect;
import com.teamwizardry.wizardry.api.util.BlockUtils;
import com.teamwizardry.wizardry.api.util.RandUtil;
import com.teamwizardry.wizardry.client.fx.LibParticles;
import com.teamwizardry.wizardry.init.ModSounds;
import net.minecraft.block.Block;
import net.minecraft.block.IGrowable;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Items;
import net.minecraft.item.ItemDye;
import net.minecraft.item.ItemStack;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraftforge.common.IPlantable;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nonnull;

/**
 * Created by Demoniaque.
 */
@RegisterModule(ID="effect_thrive")
public class ModuleEffectThrive implements IModuleEffect {

	@Override
	public String[] compatibleModifiers() {
		return new String[]{"modifier_increase_potency"};
	}

	@Override
	public boolean run(ModuleInstanceEffect instance, @Nonnull SpellData spell, @Nonnull SpellRing spellRing) {
		World world = spell.world;
		BlockPos targetPos = spell.getTargetPos();
		Entity targetEntity = spell.getVictim();
		Entity caster = spell.getCaster();
		Vec3d pos = spell.getTarget();

		if (pos != null)
			spell.world.playSound(null, new BlockPos(pos), ModSounds.HEAL, SoundCategory.NEUTRAL, 1, 1);
		if (targetEntity instanceof EntityLivingBase) {
			double potency = spellRing.getAttributeValue(AttributeRegistry.POTENCY, spell) / 2;

			if (!spellRing.taxCaster(spell, true)) return false;

			((EntityLivingBase) targetEntity).heal((float) potency);
		}

		if (targetPos != null) {
			if (world.getBlockState(targetPos).getBlock() instanceof IGrowable) {
				if (!spellRing.taxCaster(spell, true)) return false;
				if (caster == null || (caster instanceof EntityPlayer && BlockUtils.hasEditPermission(targetPos, (EntityPlayerMP) caster)))
					ItemDye.applyBonemeal(new ItemStack(Items.DYE), world, targetPos);
			} else if (world.getBlockState(targetPos).getBlock() instanceof IPlantable) {
				IBlockState state = world.getBlockState(targetPos);
				Block block = state.getBlock();
				if (!spellRing.taxCaster(spell, true)) return false;
				if (caster == null || (caster instanceof EntityPlayer && BlockUtils.hasEditPermission(targetPos, (EntityPlayerMP) caster))) {
					while (world.getBlockState(targetPos.up()).getBlock() == block) {
						targetPos = targetPos.up();
						state = world.getBlockState(targetPos);
						block = state.getBlock();
					}
					world.immediateBlockTick(targetPos, state, RandUtil.random);
				}
			}
		}
		return true;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void renderSpell(ModuleInstanceEffect instance, @Nonnull SpellData spell, @Nonnull SpellRing spellRing) {
		World world = spell.world;
		Vec3d position = spell.getTarget();

		if (position == null) return;
		LibParticles.EFFECT_REGENERATE(world, position, instance.getPrimaryColor());
	}
}
