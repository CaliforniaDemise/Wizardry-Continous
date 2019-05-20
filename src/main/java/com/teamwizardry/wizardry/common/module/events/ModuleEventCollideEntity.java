package com.teamwizardry.wizardry.common.module.events;

import com.teamwizardry.wizardry.api.spell.SpellData;
import com.teamwizardry.wizardry.api.spell.SpellRing;
import com.teamwizardry.wizardry.api.spell.annotation.RegisterModule;
import com.teamwizardry.wizardry.api.spell.module.IModuleEvent;
import com.teamwizardry.wizardry.api.spell.module.ModuleInstanceEvent;
import net.minecraft.entity.Entity;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nonnull;

import static com.teamwizardry.wizardry.api.spell.SpellData.DefaultKeys.BLOCK_HIT;

/**
 * Created by Demoniaque.
 */
@RegisterModule(ID="event_collide_entity")
public class ModuleEventCollideEntity implements IModuleEvent {

	@Override
	public boolean run(@NotNull World world, ModuleInstanceEvent instance, @Nonnull SpellData spell, @Nonnull SpellRing spellRing) {
		Entity entity = spell.getVictim(world);
		spell.removeData(BLOCK_HIT);
		return entity != null;
	}
}
