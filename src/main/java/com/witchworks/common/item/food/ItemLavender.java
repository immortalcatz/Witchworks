package com.witchworks.common.item.food;

import com.witchworks.common.lib.LibItemName;
import net.minecraft.init.MobEffects;

/**
 * This class was created by Arekkuusu on 03/07/2017, and modified by Sunconure11 on 03/17/2017.
 * It's distributed as part of Witchworks under
 * the MIT license.
 */
public class ItemLavender extends ItemCrop {

	public ItemLavender() {
		super(LibItemName.LAVENDER, 1, 2F, false);
		addPotion(MobEffects.REGENERATION);
	}
}
