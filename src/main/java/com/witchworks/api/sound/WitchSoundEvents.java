package com.witchworks.api.sound;

import com.witchworks.api.WitchWorksAPI;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvent;

/**
 * This class was created by <Arekkuusu> on 26/02/2017.
 * It's distributed as part of Witchworks under
 * the MIT license.
 */
public final class WitchSoundEvents {

	public static final SoundEvent BOIL = getRegisteredSound(WitchWorksAPI.BOIL);
	public static final SoundEvent BUZZ = getRegisteredSound(WitchWorksAPI.BUZZ);
	public static final SoundEvent CHALK_SCRIBBLE = getRegisteredSound(WitchWorksAPI.CHALK_SCRIBBLE);

	private WitchSoundEvents() {
	}

	private static SoundEvent getRegisteredSound(ResourceLocation name) {
		return SoundEvent.REGISTRY.getObject(name);
	}
}
