package com.wiccanarts.common.item.tool;

import com.wiccanarts.common.core.WiccanArtsCreativeTabs;
import com.wiccanarts.common.item.ModItems;
import net.minecraft.item.ItemSword;

/**
 * Created by BerciTheBeast on 27.3.2017.
 */
public class ItemSilverSword extends ItemSword {
	public ItemSilverSword(String id, ToolMaterial material) {
		super(MaterialSilver.SILVER);
		setRegistryName(id);
		setUnlocalizedName(id);
		setCreativeTab(WiccanArtsCreativeTabs.ITEMS_CREATIVE_TAB);
	}
}