/**
 * This class was created by <ArekkuusuJerii>. It's distributed as
 * part of the Grimoire Of Alice Mod. Get the Source Code in github:
 * https://github.com/ArekkuusuJerii/Grimore-Of-Alice
 * <p>
 * Grimoire Of Alice is Open Source and distributed under the
 * Grimoire Of Alice license: https://github.com/ArekkuusuJerii/Grimoire-Of-Alice/blob/master/LICENSE.md
 */
package com.wiccanarts.client.fx;

import net.minecraft.client.*;
import net.minecraft.client.particle.*;
import net.minecraftforge.fml.relauncher.*;

@SideOnly (Side.CLIENT)
public enum ParticleF {
	CAULDRON_BUBBLE (new ParticleCaudronBubble.Factory ()),
	STEAM (new ParticleSteam.Factory ());

	private final IParticleF factory;

	ParticleF (IParticleF factory) {
		this.factory = factory;
	}

	public Particle newInstance (double x, double y, double z, double xSpeed, double ySpeed, double zSpeed, float... args) {
		return factory.createParticle (Minecraft.getMinecraft ().world, x, y, z, xSpeed, ySpeed, zSpeed, args);
	}
}