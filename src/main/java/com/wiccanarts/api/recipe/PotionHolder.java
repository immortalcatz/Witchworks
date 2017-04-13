package com.wiccanarts.api.recipe;

import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;

/**
 * This class was created by Arekkuusu on 11/04/2017.
 * It's distributed as part of Wiccan Arts under
 * the MIT license.
 */
public class PotionHolder {

	private Potion potion;
	private int duration;
	private int amplifier;

	public PotionHolder(Potion potion, int duration, int amplifier) {
		this.potion = potion;
		this.duration = duration;
		this.amplifier = amplifier;
	}

	public PotionEffect getPotionEffect() {
		return new PotionEffect(potion, duration, amplifier);
	}

	public void setPotion(Potion potion) {
		this.potion = potion;
	}

	public void setDuration(int duration) {
		this.duration = duration;
	}

	public void setAmplifier(int amplifier) {
		this.amplifier = amplifier;
	}

	public PotionHolder alter(int durationIn, int amplifierIn) {
		this.duration += durationIn;
		this.amplifier += amplifierIn;

		if(duration < 0) duration = 0;
		if(amplifier < 0) amplifier = 0;
		return this;
	}

	public PotionHolder copy() {
		return new PotionHolder(potion, duration, amplifier);
	}
}
