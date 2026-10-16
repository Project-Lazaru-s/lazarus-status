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
    private static final int PANEL_WIDTH = 256;
    private static final int PANEL_HEIGHT = 202;
    private static final int CONTENT_MARGIN = 20;
    private static final ResourceLocation ICON = new ResourceLocation(
            LazarusStatus.ID, "textures/gui/survivor_status.png");
    private static final ResourceLocation PANEL = new ResourceLocation(
            LazarusStatus.ID, "textures/gui/survivor_record.png");

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
            super(Component.literal("Survivor Record"));
            this.stats = stats;
        }

        @Override
        protected void init() {
            int left = (width - PANEL_WIDTH) / 2;
            int top = (height - PANEL_HEIGHT) / 2;
            addRenderableWidget(Button.builder(Component.literal("Back"), button -> returnToInventory())
                    .bounds(left + 202, top + 8, 42, 16).build());
        }

        private void returnToInventory() {
            if (minecraft != null && minecraft.player != null)
                minecraft.setScreen(new InventoryScreen(minecraft.player));
        }

        @Override
        public void onClose() {
            returnToInventory();
        }

        @Override
        public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
            renderBackground(graphics);
            int left = (width - PANEL_WIDTH) / 2;
            int top = (height - PANEL_HEIGHT) / 2;
            graphics.blit(PANEL, left, top, 0, 0, PANEL_WIDTH, PANEL_HEIGHT, PANEL_WIDTH, PANEL_HEIGHT);
            graphics.drawString(font, "LAZARUS / SURVIVOR RECORD", left + 16, top + 12, 0xFFE4D9C8);

            graphics.fill(left + 16, top + 31, left + 50, top + 65, 0xFF101714);
            graphics.pose().pushPose();
            graphics.pose().translate(left + 17, top + 32, 0);
            graphics.pose().scale(2, 2, 1);
            graphics.blit(ICON, 0, 0, 0, 0, 16, 16, 16, 16);
            graphics.pose().popPose();
            String playerName = minecraft != null && minecraft.player != null
                    ? minecraft.player.getName().getString() : "Survivor";
            graphics.drawString(font, playerName, left + 60, top + 39, 0xFFF4ECE0);
            graphics.drawString(font, "WORLD DAY  " + stats.worldDay(), left + 60, top + 54, 0xFFB9A995);

            graphics.fill(left + 17, top + 69, left + 239, top + 70, 0xFF7B473A);
            graphics.drawString(font, "CURRENT RUN", left + CONTENT_MARGIN, top + 75, 0xFFD4A185);
            row(graphics, left, top + 91, "Days survived", StatusClock.display(stats.survivalTicks()));
            row(graphics, left, top + 107, "Zombies killed", Integer.toString(stats.zombieKills()));

            graphics.fill(left + 17, top + 124, left + 239, top + 125, 0xFF7B473A);
            graphics.drawString(font, "PERSONAL RECORD", left + CONTENT_MARGIN, top + 131, 0xFFD4A185);
            row(graphics, left, top + 147, "Longest run", StatusClock.display(stats.longestRunTicks()));
            row(graphics, left, top + 162, "Total zombies killed", Integer.toString(stats.totalKills()));
            row(graphics, left, top + 177, "Deaths", Integer.toString(stats.deaths()));
            super.render(graphics, mouseX, mouseY, partialTick);
        }

        private void row(GuiGraphics graphics, int left, int y, String label, String value) {
            graphics.drawString(font, label, left + CONTENT_MARGIN, y, 0xFFD1CEC3);
            graphics.drawString(font, value, left + PANEL_WIDTH - CONTENT_MARGIN - font.width(value), y, 0xFFFFFFFF);
        }

        @Override
        public boolean isPauseScreen() { return false; }
    }
}
