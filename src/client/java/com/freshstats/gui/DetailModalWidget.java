package com.freshstats.gui;

import com.freshstats.data.CategoryData;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.narration.NarrationMessageBuilder;
import net.minecraft.client.gui.widget.EntryListWidget;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;

import java.util.List;

public class DetailModalWidget {
    private final Screen parent;
    private final CategoryData categoryData;
    private final int x;
    private final int y;
    private final int width;
    private final int height;
    private boolean visible = true;
    private final DetailListWidget listWidget;

    public DetailModalWidget(Screen parent, CategoryData categoryData, int x, int y, int width, int height) {
        this.parent = parent;
        this.categoryData = categoryData;
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;

        MinecraftClient client = MinecraftClient.getInstance();
        this.listWidget = new DetailListWidget(client, width - 20, height - 60, y + 34, 20);
        this.listWidget.setX(x + 10);
    }

    public boolean isVisible() {
        return visible;
    }

    public void setVisible(boolean visible) {
        this.visible = visible;
    }

    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        if (!visible) return;

        // 1. Darken background screen behind modal
        context.fill(0, 0, parent.width, parent.height, 0xD0000000);

        // 2. Solid opaque modal window container (prevents background text bleed-through)
        context.fill(x - 2, y - 2, x + width + 2, y + height + 2, 0xFF3D4659); // Outer border
        context.fill(x - 1, y - 1, x + width + 1, y + height + 1, 0xFF181D28);
        context.fill(x, y, x + width, y + height, 0xFF141822);                // Solid main background
        context.fill(x, y, x + width, y + 28, 0xFF1F2535);                    // Header background

        // Title
        Text title = Text.translatable("freshstats.modal.details",
                categoryData.getCategory().getText(),
                String.format("%,d", categoryData.getTotalValue()),
                categoryData.getMainUnitText());
        context.drawCenteredTextWithShadow(MinecraftClient.getInstance().textRenderer, title, x + width / 2, y + 9, categoryData.getCategory().getColor());

        // Close instruction / hint
        context.drawCenteredTextWithShadow(MinecraftClient.getInstance().textRenderer, Text.translatable("freshstats.modal.close_hint"), x + width / 2, y + height - 16, 0xFF8E9BAE);

        // 3. Draw solid background box for list area to guarantee zero bleed-through
        int listX = x + 10;
        int listY = y + 34;
        int listW = width - 20;
        int listH = height - 60;
        context.fill(listX - 1, listY - 1, listX + listW + 1, listY + listH + 1, 0xFF2A3242);
        context.fill(listX, listY, listX + listW, listY + listH, 0xFF0F121A);

        // 4. Render detailed entries list
        this.listWidget.render(context, mouseX, mouseY, delta);
    }

    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (!visible) return false;
        if (this.listWidget.mouseClicked(mouseX, mouseY, button)) {
            return true;
        }
        // Click outside list closes modal
        visible = false;
        return true;
    }

    public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        if (!visible) return false;
        return this.listWidget.mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount);
    }

    private class DetailListWidget extends EntryListWidget<DetailListWidget.DetailEntryItem> {
        public DetailListWidget(MinecraftClient client, int width, int height, int top, int itemHeight) {
            super(client, width, height, top, itemHeight);
            populate();
        }

        private void populate() {
            List<CategoryData.DetailEntry> entries = categoryData.getDetails();
            if (entries.isEmpty()) {
                addEntry(new DetailEntryItem(Text.translatable("freshstats.modal.no_data"), 0, null));
            } else {
                for (CategoryData.DetailEntry entry : entries) {
                    addEntry(new DetailEntryItem(entry.getName(), entry.getCount(), entry.getIcon()));
                }
            }
        }

        @Override
        public int getRowWidth() {
            return this.width - 10;
        }

        @Override
        protected void appendClickableNarrations(NarrationMessageBuilder builder) {
        }

        private class DetailEntryItem extends EntryListWidget.Entry<DetailEntryItem> {
            private final Text name;
            private final long count;
            private final ItemStack icon;

            public DetailEntryItem(Text name, long count, ItemStack icon) {
                this.name = name;
                this.count = count;
                this.icon = icon;
            }

            @Override
            public void render(DrawContext context, int index, int top, int left, int entryWidth, int entryHeight, int mouseX, int mouseY, boolean hovered, float tickDelta) {
                if (hovered) {
                    context.fill(left, top, left + entryWidth, top + entryHeight, 0x22FFFFFF);
                }

                int textX = left + 5;
                if (icon != null && !icon.isEmpty()) {
                    context.drawItem(icon, left + 2, top + 2);
                    textX = left + 24;
                }

                context.drawTextWithShadow(MinecraftClient.getInstance().textRenderer, name, textX, top + 5, 0xFFEEEEEE);
                String countText = String.format("%,d", count);
                int countWidth = MinecraftClient.getInstance().textRenderer.getWidth(countText);
                context.drawTextWithShadow(MinecraftClient.getInstance().textRenderer, countText, left + entryWidth - countWidth - 8, top + 5, categoryData.getCategory().getColor());
            }
        }
    }
}
