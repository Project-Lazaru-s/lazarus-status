package com.lazarus.status;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ScreenEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = LazarusStatus.ID, value = Dist.CLIENT)
public final class ClientStatus {
    private static final ResourceLocation ICON = new ResourceLocation(
            LazarusStatus.ID, "textures/gui/survivor_status.png");

    private ClientStatus() { }

    @SubscribeEvent
    public static void inventoryButton(ScreenEvent.Init.Post event) {
        if (!(event.getScreen() instanceof InventoryScreen)) return;
        int left = (event.getScreen().width - 176) / 2;
        int top = (event.getScreen().height - 166) / 2;
        event.addListener(Button.builder(Component.empty(), button -> LazarusStatus.request())
                .bounds(left + 147, top + 61, 18, 18)
                .tooltip(Tooltip.create(Component.literal("Survivor status"))).build());
    }

    @SubscribeEvent
    public static void renderInventoryIcon(ScreenEvent.Render.Post event) {
        if (!(event.getScreen() instanceof InventoryScreen)) return;
        int left = (event.getScreen().width - 176) / 2;
        int top = (event.getScreen().height - 166) / 2;
        event.getGuiGraphics().blit(ICON, left + 148, top + 62, 0, 0, 16, 16, 16, 16);
    }

    public static void open(LazarusStatus.Snapshot snapshot) {
        Minecraft.getInstance().setScreen(new StatusScreen(snapshot));
    }

    private static final class StatusScreen extends Screen {
        private final LazarusStatus.Snapshot stats;

        private StatusScreen(LazarusStatus.Snapshot stats) {
            super(Component.literal("Lazarus Status"));
            this.stats = stats;
        }

        @Override
        public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
            renderBackground(graphics);
            int left = (width - 220) / 2;
            int top = (height - 110) / 2;
            graphics.fill(left, top, left + 220, top + 110, 0xD9121719);
            graphics.fill(left, top, left + 220, top + 2, 0xFFAE442D);
            graphics.drawCenteredString(font, title, width / 2, top + 12, 0xFFE2DCCF);
            graphics.drawString(font, "World Day", left + 16, top + 39, 0xFFB7B4AC);
            graphics.drawString(font, Long.toString(stats.worldDay()), left + 178, top + 39, 0xFFFFFFFF);
            graphics.drawString(font, "Days Survived", left + 16, top + 61, 0xFFB7B4AC);
            graphics.drawString(font, Long.toString(stats.daysSurvived()), left + 178, top + 61, 0xFFFFFFFF);
            graphics.drawString(font, "Zombies Killed", left + 16, top + 83, 0xFFB7B4AC);
            graphics.drawString(font, Integer.toString(stats.zombieKills()), left + 178, top + 83, 0xFFFFFFFF);
            super.render(graphics, mouseX, mouseY, partialTick);
        }

        @Override
        public boolean isPauseScreen() { return false; }
    }
}
