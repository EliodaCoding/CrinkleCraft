package net.eli.crinklecraft.client;

import net.eli.crinklecraft.menu.CrinkleCraftSlotsMenu;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

public class CrinkleCraftSlotsScreen extends AbstractContainerScreen<CrinkleCraftSlotsMenu> {

    // Compact equipment panel - uses dispenser-style texture (not chest-like)
    private static final ResourceLocation TEXTURE =
            ResourceLocation.withDefaultNamespace("textures/gui/container/dispenser.png");

    public CrinkleCraftSlotsScreen(CrinkleCraftSlotsMenu menu, Inventory playerInv, Component title) {
        super(menu, playerInv, title);
        imageHeight = 140;
        imageWidth = 176;
        inventoryLabelY = -9999; // Hide "Inventory" label
    }

    @Override
    protected void renderBg(GuiGraphics guiGraphics, float partialTick, int mouseX, int mouseY) {
        int x = (width - imageWidth) / 2;
        int y = (height - imageHeight) / 2;
        guiGraphics.blit(TEXTURE, x, y, 0, 0, imageWidth, imageHeight);
    }

    @Override
    protected void renderLabels(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        guiGraphics.drawString(this.font, this.title, this.titleLabelX, this.titleLabelY, 0x404040, false);
        int labelX = 8;
        guiGraphics.drawString(this.font, Component.translatable("container.crinklecraft.slot.diaper"), labelX, 23, 0x404040, false);
        guiGraphics.drawString(this.font, Component.translatable("container.crinklecraft.slot.pacifier"), labelX, 41, 0x404040, false);
        guiGraphics.drawString(this.font, Component.translatable("container.crinklecraft.slot.mittens"), labelX, 59, 0x404040, false);
        // Onesie label near top-right slot
        guiGraphics.drawString(this.font, Component.translatable("container.crinklecraft.slot.onesie"), 116, 23, 0x404040, false);
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        renderBackground(guiGraphics, mouseX, mouseY, partialTick);
        super.render(guiGraphics, mouseX, mouseY, partialTick);
        renderTooltip(guiGraphics, mouseX, mouseY);
    }
}
