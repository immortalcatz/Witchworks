package com.witchworks.client.core.event;

import com.witchworks.api.capability.IEnergy;
import com.witchworks.client.ResourceLocations;
import com.witchworks.common.core.capability.energy.EnergyHandler;
import com.witchworks.common.core.handler.ConfigHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.Optional;

/**
 * This class was created by Arekkuusu on 21/04/2017.
 * It's distributed as part of Witchworks under
 * the MIT license.
 */
@SideOnly(Side.CLIENT)
public class EnergyHUD {

	private int renderTime;
	private float visible;
	private int oldEnergy;
	private float barAlpha;
	private boolean reverse;

	@SubscribeEvent
	public void onTick(TickEvent.ClientTickEvent event) {
		if (event.phase == TickEvent.Phase.END && Minecraft.getMinecraft().player != null) {
			Optional<IEnergy> optional = EnergyHandler.getEnergy(Minecraft.getMinecraft().player);

			if (optional.isPresent()) {
				IEnergy energy = optional.get();

				if (oldEnergy != energy.get()) {
					oldEnergy = energy.get();
					renderTime = 60;
					visible = 1F;
				}

				if (renderTime > 0 && energy.get() == energy.getMax()) {
					if (ConfigHandler.ENERGY_HUD.hide && renderTime < 20) {
						visible -= 0.05F;
						visible = MathHelper.clamp(visible, 0F, 1F);
					}

					renderTime--;
				}

				if (!reverse) {
					barAlpha += 0.05F;
					if (barAlpha > 1F) {
						barAlpha = 1F;
						reverse = true;
					}
				} else {
					barAlpha -= 0.05F;
					if (barAlpha < 0F) {
						barAlpha = 0;
						reverse = false;
					}
				}
			}
		}
	}

	@SubscribeEvent
	public void renderOverlay(RenderGameOverlayEvent.Post event) {
		if (event.getType() == RenderGameOverlayEvent.ElementType.HOTBAR && renderTime > 0) {
			Minecraft mc = Minecraft.getMinecraft();
			Optional<IEnergy> optional = EnergyHandler.getEnergy(mc.player);

			if (optional.isPresent()) {
				IEnergy energy = optional.get();

				ScaledResolution res = event.getResolution();
				GlStateManager.pushMatrix();
				GlStateManager.enableBlend();

				float filled = (float) energy.get() / (float) energy.getMax();
				int heightEnd = ConfigHandler.ENERGY_HUD.heightEnd;
				int heightStart = ConfigHandler.ENERGY_HUD.heightStart;

				int width = ConfigHandler.ENERGY_HUD.width;

				int heightUsed = heightEnd - heightStart;
				int heightFilled = (int) ((heightUsed - 28) * filled);

				int posX = ConfigHandler.ENERGY_HUD.x;
				int posY = res.getScaledHeight() - ConfigHandler.ENERGY_HUD.y;

				int startPosX = posX - heightEnd;

				if (ConfigHandler.ENERGY_HUD.hide)
					GlStateManager.color(1F, 1F, 1F, visible);

				mc.getTextureManager().bindTexture(ResourceLocations.ENERGY_BACKGROUND[0]);
				Gui.drawModalRectWithCustomSizedTexture(startPosX - 4, posY + 14, 0F, heightUsed - 28, width, heightFilled, width, heightUsed - 28);

				GlStateManager.pushMatrix();

				GlStateManager.color(1F, 1F, 1F, visible == 1F ? barAlpha : visible);

				mc.getTextureManager().bindTexture(ResourceLocations.ENERGY_BACKGROUND[1]);
				Gui.drawModalRectWithCustomSizedTexture(startPosX - 4, posY + 14, 0F, heightUsed - 28, width, heightFilled, width, heightUsed - 28);
				GlStateManager.popMatrix();

				if (ConfigHandler.ENERGY_HUD.hide)
					GlStateManager.color(1F, 1F, 1F, visible);

				mc.getTextureManager().bindTexture(ResourceLocations.ENERGY);
				Gui.drawModalRectWithCustomSizedTexture(startPosX - 4, posY, 0F, 0F, width, heightUsed, width, heightUsed);

				int textColor = 0x990066;
				if (ConfigHandler.ENERGY_HUD.hide) {
					int alpha = (int) (visible * 255);
					textColor = alpha << 24 | 0x990066;
				}

				mc.fontRenderer.drawStringWithShadow("E: " + energy.get(), startPosX, posY - 15, textColor);
				GlStateManager.disableBlend();
				GlStateManager.popMatrix();
			}
		}
	}
}
