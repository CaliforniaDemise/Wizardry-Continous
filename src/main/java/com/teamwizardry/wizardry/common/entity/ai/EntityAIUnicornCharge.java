package com.teamwizardry.wizardry.common.entity.ai;

import com.teamwizardry.wizardry.common.entity.EntityUnicorn;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.DamageSource;
import net.minecraft.util.math.MathHelper;

public class EntityAIUnicornCharge extends EntityAIBase {

	private EntityUnicorn unicorn;
	private float speed;
	private float maxRange;
	private double damage;
	private boolean targetHit = false;

	public EntityAIUnicornCharge(EntityUnicorn unicorn, float speed, float maxRange, double damage) {
		this.unicorn = unicorn;
		this.speed = speed;
		this.maxRange = maxRange;
		this.damage = damage;
	}

	@Override
	public boolean shouldExecute() {
		EntityLivingBase target = this.unicorn.getAttackTarget();
		if (target == null || !target.isEntityAlive()) return false;
		return !(target.getDistance(unicorn) > maxRange);
	}

	@Override
	public void startExecuting() {
		if (unicorn.getAttackTarget() == null) return;
		unicorn.isCharging = true;
		unicorn.prepareChargeTicks = 0;
	}

	@Override
	public boolean shouldContinueExecuting() {
		EntityLivingBase target = this.unicorn.getAttackTarget();
		if (target == null || !target.isEntityAlive() || unicorn.getDistance(target) >= maxRange * 1.5)
			return false;

		if (target instanceof EntityPlayer && (((EntityPlayer) target).capabilities.isCreativeMode || ((EntityPlayer) target).isSpectator()))
			return false;

		return !targetHit;
	}

	@Override
	public void updateTask() {
		if (unicorn.isDead) return;
		if (unicorn.getAttackTarget() == null) return;

		if (unicorn.isCharging && unicorn.prepareChargeTicks < 40) {
			unicorn.limbSwingAmount += 0.6F;
			unicorn.prepareChargeTicks++;
			return;
		} else unicorn.getNavigator().tryMoveToEntityLiving(unicorn.getAttackTarget(), speed);

		if (unicorn.getEntityBoundingBox().grow(1, 1, 1).intersects(unicorn.getAttackTarget().getEntityBoundingBox())) {
			unicorn.getAttackTarget().knockBack(unicorn, 3F, MathHelper.sin(this.unicorn.rotationYaw), -MathHelper.cos(this.unicorn.rotationYaw));
			unicorn.knockBack(unicorn, 0.5F, -MathHelper.sin(this.unicorn.rotationYaw), MathHelper.cos(this.unicorn.rotationYaw));
			unicorn.getAttackTarget().attackEntityFrom(DamageSource.causeMobDamage(unicorn), (float) damage);
			int invTime = unicorn.getAttackTarget().hurtResistantTime;
			unicorn.getAttackTarget().hurtResistantTime = 0;
			unicorn.getAttackTarget().attackEntityFrom(DamageSource.causeIndirectDamage(unicorn, unicorn.getAttackTarget()), (float) damage * 0.2f);
			unicorn.getAttackTarget().hurtResistantTime = invTime;
			targetHit = true;
		}
	}

	@Override
	public void resetTask() {
		this.unicorn.getNavigator().clearPath();
		unicorn.isCharging = false;
		unicorn.prepareChargeTicks = 0;
		targetHit = false;
	}
}
