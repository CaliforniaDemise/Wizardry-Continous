package com.teamwizardry.wizardry.api.spell;

/**
 * Created by LordSaad.
 */
public enum ModuleType {

	BOOLEAN("boolean"), EFFECT("effect"), SHAPE("shape"), EVENT("event"), MODIFIER("modifier");

	public String name;
	
	private ModuleType(String name) {
		this.name = name;
	}
}
