package com.teamwizardry.wizardry.common.module.modifiers;

import com.teamwizardry.wizardry.api.spell.*;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;

import javax.annotation.Nonnull;

/**
 * Created by LordSaad.
 */
@RegisterModule
public class ModuleModifierExtend extends Module implements IModifier {

	public ModuleModifierExtend() {
	}

	@Nonnull
	@Override
	public ItemStack getRequiredStack() {
		return new ItemStack(Items.PRISMARINE_CRYSTALS);
	}

	@Nonnull
	@Override
	public ModuleType getModuleType() {
		return ModuleType.MODIFIER;
	}

	@Nonnull
	@Override
	public String getID() {
		return "modifier_extend";
	}

	@Nonnull
	@Override
	public String getReadableName() {
		return "Extend";
	}

	@Nonnull
	@Override
	public String getDescription() {
		return "Can increase range or time on shapes and effects.";
	}

	@Override
	public double getManaToConsume() {
		return 50;
	}

	@Override
	public double getBurnoutToFill() {
		return 50;
	}

	@Override
	public void apply(Module module) {
		int power = 2;
		module.attributes.setDouble(Attributes.EXTEND, module.attributes.getDouble(Attributes.EXTEND) + power);
	}

	@Nonnull
	@Override
	public ModuleModifierExtend copy() {
		ModuleModifierExtend module = new ModuleModifierExtend();
		module.deserializeNBT(serializeNBT());
		process(module);
		return module;
	}
}
