package com.witchworks.common.brew;

import com.witchworks.api.brew.IBrew;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.EnumCreatureAttribute;
import net.minecraft.util.DamageSource;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/**
 * This class was created by Arekkuusu on 24/04/2017.
 * It's distributed as part of Witchworks under
 * the MIT license.
 */
public class OutcastsShameBrew implements IBrew {

	@Override
	//Todo: Make it target witches and mobs that extend witches, too.
	public void apply(World world, BlockPos pos, EntityLivingBase entity, int amplifier, int tick) {
		if (entity.getCreatureAttribute() == EnumCreatureAttribute.ILLAGER) {
			int damage = (int) (entity.getHealth() * (double) (6 << amplifier) + 0.5D);
			entity.attackEntityFrom(DamageSource.MAGIC, (float) damage);
		}
	}

	@Override
	public boolean isBad() {
		return false;
	}

	@Override
	public boolean isInstant() {
		return false;
	}

	@Override
	public int getColor() {
		return 0x8A3324;
	}

	@Override
	public String getName() {
		return "outcasts_shame";
	}

	@SideOnly(Side.CLIENT)
	@Override
	public void renderHUD(int x, int y, Minecraft mc, int amplifier) {
		render(x, y, mc, 14);
	}

}
