package com.freshstats.gui;

import com.freshstats.data.CategoryData;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
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
    private float scrollAmount = 0.0f;

    public DetailModalWidget(Screen parent, CategoryData categoryData, int x, int y, int width, int height) {
        this.parent = parent;
        this.categoryData = categoryData;
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
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

        // 3. Draw solid background box for list area
        int listX = x + 10;
        int listY = y + 34;
        int listW = width - 20;
        int listH = height - 56;

        context.fill(listX - 1, listY - 1, listX + listW + 1, listY + listH + 1, 0xFF2A3242);
        context.fill(listX, listY, listX + listW, listY + listH, 0xFF0F121A);

        // 4. Render detailed entries list using scissor clipping (1.20.1 & 1.21.1 cross-compatible)
        List<CategoryData.DetailEntry> entries = categoryData.getDetails();
        int itemH = 20;
        int totalContentH = Math.max(listH, (entries.isEmpty() ? 1 : entries.size()) * itemH);
        int maxScroll = Math.max(0, totalContentH - listH);

        if (scrollAmount > maxScroll) scrollAmount = maxScroll;
        if (scrollAmount < 0) scrollAmount = 0;

        context.enableScissor(listX, listY, listX + listW, listY + listH);

        if (entries.isEmpty()) {
            context.drawTextWithShadow(MinecraftClient.getInstance().textRenderer, Text.translatable("freshstats.modal.no_data"), listX + 10, listY + 10, 0xFF8E9BAE);
        } else {
            for (int i = 0; i < entries.size(); i++) {
                int itemY = listY + (i * itemH) - (int) scrollAmount;
                if (itemY + itemH < listY || itemY > listY + listH) continue;

                CategoryData.DetailEntry entry = entries.get(i);
                boolean hovered = mouseX >= listX && mouseX <= listX + listW && mouseY >= itemY && mouseY < itemY + itemH;

                if (hovered) {
                    context.fill(listX, itemY, listX + listW, itemY + itemH, 0x22FFFFFF);
                }

                int textX = listX + 6;
                ItemStack icon = entry.getIcon();
                if (icon != null && !icon.isEmpty()) {
                    context.drawItem(icon, listX + 4, itemY + 2);
                    textX = listX + 26;
                }

                context.drawTextWithShadow(MinecraftClient.getInstance().textRenderer, entry.getName(), textX, itemY + 6, 0xFFEEEEEE);

                String countText = String.format("%,d", entry.getCount());
                int countWidth = MinecraftClient.getInstance().textRenderer.getWidth(countText);
                context.drawTextWithShadow(MinecraftClient.getInstance().textRenderer, countText, listX + listW - countWidth - 8, itemY + 6, categoryData.getCategory().getColor());
            }
        }

        context.disableScissor();

        // 5. Draw Scrollbar if list overflows container
        if (maxScroll > 0) {
            int barW = 4;
            int barX = listX + listW - barW - 2;
            float ratio = (float) listH / totalContentH;
            int barH = Math.max(16, (int) (listH * ratio));
            int barY = listY + (int) ((listH - barH) * (scrollAmount / maxScroll));

            context.fill(barX, listY, barX + barW, listY + listH, 0x44000000);
            context.fill(barX, barY, barX + barW, barY + barH, 0xFF556677);
        }
    }

    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (!visible) return false;

        // Click outside modal container closes modal
        if (mouseX < x || mouseX > x + width || mouseY < y || mouseY > y + height) {
            visible = false;
            return true;
        }
        return true;
    }

    public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        if (!visible) return false;

        List<CategoryData.DetailEntry> entries = categoryData.getDetails();
        int listH = height - 56;
        int itemH = 20;
        int totalContentH = Math.max(listH, (entries.isEmpty() ? 1 : entries.size()) * itemH);
        int maxScroll = Math.max(0, totalContentH - listH);

        if (maxScroll > 0) {
            scrollAmount = (float) Math.max(0, Math.min(maxScroll, scrollAmount - verticalAmount * 14));
            return true;
        }
        return false;
    }
}
