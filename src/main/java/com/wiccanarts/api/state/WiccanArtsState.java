package com.wiccanarts.api.state;

import net.minecraft.block.properties.PropertyEnum;
import net.minecraft.item.EnumDyeColor;

/**
 * This class was created by Arekkuusu on 28/02/2017.
 * It's distributed as part of Wiccan Arts under
 * the MIT license.
 */
public final class WiccanArtsState {

	public static final PropertyEnum<EnumDyeColor> COLOR = PropertyEnum.create("color", EnumDyeColor.class);

	private WiccanArtsState() {
	}
}