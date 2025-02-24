/*
 * Licensed under the EUPL, Version 1.2.
 * You may obtain a copy of the Licence at:
 * https://joinup.ec.europa.eu/collection/eupl/eupl-text-eupl-12
 */

package net.dries007.tfc.client.screen;

import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

import com.mojang.blaze3d.vertex.PoseStack;
import net.dries007.tfc.common.blockentities.PotBlockEntity;
import net.dries007.tfc.common.capabilities.heat.Heat;
import net.dries007.tfc.common.container.PotContainer;

import static net.dries007.tfc.TerraFirmaCraft.MOD_ID;

public class PotScreen extends BlockEntityScreen<PotBlockEntity, PotContainer>
{
    private static final ResourceLocation BACKGROUND = new ResourceLocation(MOD_ID, "textures/gui/fire_pit_cooking_pot.png");

    public PotScreen(PotContainer container, Inventory playerInventory, Component name)
    {
        super(container, playerInventory, name, BACKGROUND);
        inventoryLabelY += 20;
        imageHeight += 20;
    }

    @Override
    protected void renderLabels(PoseStack poseStack, int mouseX, int mouseY)
    {
        super.renderLabels(poseStack, mouseX, mouseY);
        if (blockEntity.shouldRenderAsBoiling())
        {
            drawDisabled(poseStack, PotBlockEntity.SLOT_EXTRA_INPUT_START, PotBlockEntity.SLOT_EXTRA_INPUT_END);
        }
    }

    @Override
    protected void renderBg(PoseStack poseStack, float partialTicks, int mouseX, int mouseY)
    {
        super.renderBg(poseStack, partialTicks, mouseX, mouseY);
        int temp = (int) (51 * blockEntity.getTemperature() / Heat.maxVisibleTemperature());
        if (temp > 0)
        {
            blit(poseStack, leftPos + 30, topPos + 76 - Math.min(51, temp), 176, 0, 15, 5);
        }
    }
}
