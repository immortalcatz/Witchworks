package com.wiccanarts.common.block.tile;

import com.wiccanarts.api.WiccanArtsAPI;
import com.wiccanarts.api.recipe.*;
import com.wiccanarts.api.sound.WiccaSoundEvents;
import com.wiccanarts.client.fx.ParticleF;
import com.wiccanarts.common.WiccanArts;
import com.wiccanarts.common.item.ModItems;
import com.wiccanarts.common.net.PacketHandler;
import com.wiccanarts.common.potions.BrewUtils;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Items;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.potion.PotionEffect;
import net.minecraft.potion.PotionUtils;
import net.minecraft.util.*;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.WorldServer;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Random;

/**
 * This class was created by Arekkuusu on 08/03/2017.
 * It's distributed as part of Wiccan Arts under
 * the MIT license.
 */
@SuppressWarnings ("WeakerAccess")
public class TileKettle extends TileItemInventory implements ITickable {

	private final String TAG_WATER = "waterLevel";
	private final String TAG_HEAT = "heat";
	private final String TAG_MODE = "mode";
	private final String TAG_RECIPE = "recipe";
	private final int RECIPE_IDLE = -1;
	private float[] colors = new float[]{0.0f, 0.39215687f, 0.0f};
	private KettleMode mode = KettleMode.DEFAULT;
	private int waterLevel;
	private int heat;
	private int ticks;
	private int recipeBoilingTime = RECIPE_IDLE;
	private IKettleRecipe recipe;
	private int itemTimer;

	@SuppressWarnings("ConstantConditions")
	public void collideItem(EntityItem entityItem) {
		if (!hasWater()) return;

		final ItemStack stack = entityItem.getEntityItem();
		if (stack == null || entityItem.isDead)
			return;

		if (!world.isRemote) {
			PacketHandler.sendTileUpdateNearbyPlayers(this);
			fancySplash();

			if (isHot() && isEmpty()) {
				if (WiccanArtsAPI.getKettleExchanges().containsKey(stack.getItem())) {
					final KettleExchange exchange = WiccanArtsAPI.getKettleExchanges().get(stack.getItem());
					final ItemStack out = exchange.getExchange(stack);
					if (out != null) {

						setWaterLevel(getWaterLevel() - 1);

						stack.stackSize--;
						if (stack.stackSize == 0)
							entityItem.setDead();

						final double x = getPos().getX();
						final double y = getPos().getY() + 1D;
						final double z = getPos().getZ();
						final EntityItem item = new EntityItem(world, x + 0.5D, y, z + 0.5D, out);
						item.motionX = world.rand.nextDouble() * 2 - 1;
						item.motionZ = world.rand.nextDouble() * 2 - 1;
						item.motionY = 0.1D;
						item.setPickupDelay(0);
						world.spawnEntity(item);

						for (int i = 0; i < 4; i++) {
							PacketHandler.spawnParticle(ParticleF.STEAM, world, x + world.rand.nextFloat(), y, z + world.rand.nextFloat(), 5, 0, 0, 0);
						}

						return;
					}
				} else if (stack.getItem() == ModItems.MANDRAKE_ROOT) {
					mode = KettleMode.CUSTOM;

					stack.stackSize--;
					if (stack.stackSize == 0)
						entityItem.setDead();

					return;
				}
			}

			stack.stackSize--;
			if (stack.stackSize == 0)
				entityItem.setDead();

			for (int i = 0; i < getSizeInventory(); i++) {
				if (itemHandler.getItemSimulate(i) == null) {
					final ItemStack stackToAdd = stack.copy();
					stackToAdd.stackSize = 1;
					itemHandler.insertItem(i, stackToAdd, false);
					break;
				}
			}

			if (loadRecipe() && !recipe.isPotion()) {
				recipeBoilingTime = 10;
				itemTimer = 5;
			}
		}

		colors[0] = world.rand.nextFloat();
		colors[1] = world.rand.nextFloat();
		colors[2] = world.rand.nextFloat();
	}

	private void fancySplash() {
		world.playSound(null, pos, SoundEvents.ENTITY_GENERIC_SPLASH, SoundCategory.BLOCKS, 0.1F, 1F);

		if (world instanceof WorldServer) {
			for (int i = 0; i < 10; i++) {
				final BlockPos pos = getPos();
				final Random rand = new Random();
				final float d3 = pos.getX() + rand.nextFloat();
				final float d4 = (float) (pos.getY() + 1);
				final float d5 = pos.getZ() + rand.nextFloat();
				((WorldServer) world).spawnParticle(EnumParticleTypes.CRIT_MAGIC, d3, d4, d5, 1, 0, 0, 0, 0D);
			}
		}
	}

	public boolean useKettle(@Nullable EntityPlayer player, EnumHand hand, ItemStack stack) {
		if (!world.isRemote) {
			if (stack.hasCapability(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY, null)) {
				final IFluidHandler handler = stack.getCapability(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY, null);
				final FluidStack drained = new FluidStack(FluidRegistry.WATER, Fluid.BUCKET_VOLUME);
				if (waterLevel < 6) {
					final FluidStack drainWater = handler.drain(drained, false);

					if (drainWater != null && drainWater.getFluid() == FluidRegistry.WATER
							&& drainWater.amount == Fluid.BUCKET_VOLUME) {

						world.playSound(null, getPos(), SoundEvents.ITEM_BUCKET_EMPTY, SoundCategory.BLOCKS, 1F, 8F);

						handler.drain(drained, true);
						setWaterLevel(6);
						world.updateComparatorOutputLevel(pos, world.getBlockState(pos).getBlock());

						removeItems();
					}
				} else if (waterLevel == 6) {
					final int fill = handler.fill(drained, false);

					if (fill == Fluid.BUCKET_VOLUME) {

						world.playSound(null, getPos(), SoundEvents.ITEM_BUCKET_FILL, SoundCategory.BLOCKS, 1F, 8F);

						handler.fill(drained, true);
						setWaterLevel(0);
						world.updateComparatorOutputLevel(pos, world.getBlockState(pos).getBlock());

						removeItems();
					}
				}
			} else if (waterLevel < 6 && stack.getItem() == Items.POTIONITEM && PotionUtils.getEffectsFromStack(stack).isEmpty()) {
				if (player != null && !player.capabilities.isCreativeMode) {
					final ItemStack itemStack = new ItemStack(Items.GLASS_BOTTLE);

					if (--stack.stackSize == 0) {
						player.setHeldItem(hand, itemStack);
					} else if (!player.inventory.addItemStackToInventory(itemStack)) {
						player.dropItem(itemStack, false);
					} else if (player instanceof EntityPlayerMP) {
						((EntityPlayerMP) player).sendContainerToPlayer(player.inventoryContainer);
					}
				}
				world.playSound(null, getPos(), SoundEvents.ITEM_BUCKET_FILL, SoundCategory.BLOCKS, 1F, 8F);
				setWaterLevel(getWaterLevel() + 1);
				if (world.rand.nextBoolean()) {
					removeItems();
				}
			} else if (hasWater()) {
				ItemStack itemStack = null;
				if (mode == KettleMode.DEFAULT) {
					if (recipe != null && recipe.canTake(world, player, stack)) {
						itemStack = recipe.getResult();
					}
				} else {
					itemStack = buildCustomPotion();
				}

				if (itemStack == null) {

					return false;
				}

				if (player != null && !player.capabilities.isCreativeMode) {

					if (--stack.stackSize == 0) {
						player.setHeldItem(hand, itemStack);
					} else if (!player.inventory.addItemStackToInventory(itemStack)) {
						player.dropItem(itemStack, false);
					} else if (player instanceof EntityPlayerMP) {
						((EntityPlayerMP) player).sendContainerToPlayer(player.inventoryContainer);
					}
				}
				world.playSound(null, getPos(), SoundEvents.ITEM_BUCKET_EMPTY, SoundCategory.BLOCKS, 1F, 8F);
				setWaterLevel(getWaterLevel() - 1);
			}
		}
		return true;
	}

	private ItemStack buildCustomPotion() {
		final List<PotionEffect> potions = new ArrayList<>();
		final List<IEffectModifier> modifiers = new ArrayList<>();
		for (int i = 0, slots = itemHandler.getSlots(); i < slots; i++) {
			final ItemStack out = itemHandler.getItemSimulate(i);
			if (out == null) break;
			if (!isModifier(out) && !isEffect(out)) return null;

			if (isModifier(out)) {
				final PotionValidator<IEffectModifier> validator = WiccanArtsAPI.getKettleModifiers().get(out.getItem());
				final Optional optional = validator.get(out);
				if (optional.isPresent()) {
					modifiers.add((IEffectModifier) ((Tuple) optional.get()).getSecond());
				}
			} else {
				final PotionValidator<PotionHolder> validator = WiccanArtsAPI.getKettleEffects().get(out.getItem());
				final Optional optional = validator.get(out);
				if (optional.isPresent()) {
					final PotionHolder effect = ((PotionHolder) ((Tuple) optional.get()).getSecond()).copy();
					for (IEffectModifier mod : modifiers) {
						mod.apply(effect);
					}
					modifiers.clear();
					potions.add(effect.getPotionEffect());
				}
			}
		}

		return BrewUtils.createPotion(Items.POTIONITEM, potions.toArray(new PotionEffect[potions.size()]));
	}

	private boolean isEffect(ItemStack stack) {
		return WiccanArtsAPI.getKettleEffects().containsKey(stack.getItem());
	}

	private boolean isModifier(ItemStack stack) {
		return WiccanArtsAPI.getKettleModifiers().containsKey(stack.getItem());
	}

	@Override
	public void update() {
		final List<EntityItem> entityItemList = world.getEntitiesWithinAABB(EntityItem.class, new AxisAlignedBB(getPos()));
		entityItemList.forEach(this::collideItem);

		if (recipeBoilingTime == 0 && !world.isRemote) {
			if (ticks % 2 == 0) {
				final float x = getPos().getX();
				final float y = getPos().getY() + 0.65F;
				final float z = getPos().getZ();

				PacketHandler.spawnParticle(ParticleF.STEAM, world, x + world.rand.nextFloat(), y, z + world.rand.nextFloat(), 3, 0, 0, 0);
			}

			if (ticks % 20 == 0 && --itemTimer <= 0) {
				final float x = getPos().getX() + 0.5F;
				final float y = getPos().getY() + 0.65F;
				final float z = getPos().getZ() + 0.5F;

				if (recipe != null) {
					final EntityItem item = new EntityItem(world, x, y, z, recipe.getResult());
					world.spawnEntity(item);
				}
				setWaterLevel(0);
				recipeBoilingTime = RECIPE_IDLE;
			}
		}

		if (isHot() && hasWater()) {
			if (world.rand.nextInt(10) == 0) {
				final BlockPos pos = getPos();
				final float d3 = (float) pos.getX() + world.rand.nextFloat();
				final float d4 = (float) pos.getY() + 0.65F;
				final float d5 = (float) pos.getZ() + world.rand.nextFloat();
				WiccanArts.proxy.spawnParticle(ParticleF.CAULDRON_BUBBLE, d3, d4, d5, 0.0D, 0.1D, 0.0D, colors);
			}
			if (ticks % 60 == 0) {
				world.playSound(null, getPos(), WiccaSoundEvents.BOIL, SoundCategory.BLOCKS, 0.2F, 1F);
			}
		}

		if (!hasWater()) {
			removeItems();
		}

		if (ticks % 10 == 0) {
			handleRain();
			if (!world.isRemote && recipeBoilingTime > 0) {
				final float x = getPos().getX() + world.rand.nextFloat();
				final float y = getPos().getY() + 0.65F;
				final float z = getPos().getZ() + world.rand.nextFloat();

				PacketHandler.spawnParticle(ParticleF.STEAM, world, x, y, z, 1, 0, 0, 0);

				--recipeBoilingTime;
			}
		}

		if (ticks % 20 == 0) {
			handleHeat();
		}
		++ticks;
	}

	private void removeItems() {
		for (int i = 0; i < itemHandler.getSlots(); i++) {
			itemHandler.extractItem(i, 1, false);
		}
		mode = KettleMode.DEFAULT;
	}

	private boolean isEmpty() {
		return itemHandler.getItemSimulate(0) == null;
	}

	private boolean loadRecipe() {
		recipe = WiccanArtsAPI.getKettleRecipes().stream()
				.filter(kettleRecipe -> kettleRecipe.checkRecipe(itemHandler, world))
				.findFirst().orElse(null);

		return recipe != null;
	}

	private void handleHeat() {
		if (isAboveFire() && hasWater() && heat < 5) {
			++heat;
		} else if ((!isAboveFire() || !hasWater()) && heat > 0) {
			--heat;
		}
	}

	private boolean isAboveFire() {
		final IBlockState state = world.getBlockState(getPos().down());
		return state.getMaterial() == Material.FIRE;
	}

	private void handleRain() {
		if (waterLevel != 6 && world.isRainingAt(getPos().up())) {
			if (world.isRemote) {
				for (int i = 0; i < 4; i++) {
					final double d3 = pos.getX() + world.rand.nextFloat();
					final double d4 = pos.getY() + 0.65F;
					final double d5 = pos.getZ() + world.rand.nextFloat();
					world.spawnParticle(EnumParticleTypes.WATER_BUBBLE, d3, d4, d5, 0.0D, 0.0D, 0.0D);
				}
			}
			world.playSound(null, getPos(), SoundEvents.ITEM_BUCKET_FILL, SoundCategory.BLOCKS, 0.2F, 5F);
			setWaterLevel(waterLevel + 1);
		}
	}

	@Override
	public <T> T getCapability(Capability<T> capability, EnumFacing side) {
		return null;
	}

	@Override
	public int getSizeInventory() {
		return 16;
	}

	@Override
	public void writeDataNBT(NBTTagCompound cmp) {
		super.writeDataNBT(cmp);
		cmp.setInteger(TAG_WATER, waterLevel);
		cmp.setInteger(TAG_HEAT, heat);
		cmp.setString(TAG_MODE, mode.name());
		cmp.setInteger(TAG_RECIPE, WiccanArtsAPI.getKettleRecipes().indexOf(recipe));
	}

	@Override
	public void readDataNBT(NBTTagCompound cmp) {
		super.readDataNBT(cmp);
		waterLevel = cmp.getInteger(TAG_WATER);
		heat = cmp.getInteger(TAG_HEAT);
		mode = KettleMode.valueOf(cmp.getString(TAG_MODE));

		final int index = cmp.getInteger(TAG_RECIPE);
		if(index >= 0) {
			recipe = WiccanArtsAPI.getKettleRecipes().get(index);
		}
	}

	public int getWaterLevel() {
		return waterLevel;
	}

	public void setWaterLevel(int water) {
		waterLevel = water;
		PacketHandler.updateToNearbyPlayers(world, pos);
	}

	public boolean hasWater() {
		return waterLevel > 0;
	}

	public boolean isHot() {
		return heat == 5;
	}

	public KettleMode getMode() {
		return mode;
	}

	public float[] getColor() {
		return colors;
	}

	public enum KettleMode {
		DEFAULT,
		CUSTOM
	}
}
