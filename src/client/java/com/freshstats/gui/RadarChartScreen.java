package com.freshstats.gui;

import com.mojang.blaze3d.systems.RenderSystem;
import com.freshstats.data.CategoryData;
import com.freshstats.data.GlobalStatsStorage;
import com.freshstats.data.StatAggregator;
import com.freshstats.data.StatCategory;
import com.freshstats.util.NetworkUtils;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.StatsScreen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.network.packet.c2s.play.ClientStatusC2SPacket;
import net.minecraft.stat.StatHandler;
import net.minecraft.text.Text;

import java.util.*;

public class RadarChartScreen extends Screen {
    public static boolean allowVanillaStats = false;

    private final Screen parent;
    private final StatHandler statHandler;

    private int activeTab = 0; // 0 = Current World, 1 = Global
    private Map<StatCategory, CategoryData> currentWorldStats;
    private Map<StatCategory, CategoryData> globalStats;
    private DetailModalWidget activeModal;

    private final long openTime;
    private final StatCategory[] categories = StatCategory.values();
    private final double[] angles = new double[6];
    private final float[] pointX = new float[6];
    private final float[] pointY = new float[6];
    private final float[] maxPointX = new float[6];
    private final float[] maxPointY = new float[6];

    public RadarChartScreen(Screen parent, StatHandler statHandler) {
        super(Text.translatable("freshstats.title"));
        this.parent = parent;
        this.statHandler = statHandler;
        this.openTime = System.currentTimeMillis();

        for (int i = 0; i < 6; i++) {
            // Angle 0 points to top (-PI/2)
            angles[i] = (2 * Math.PI * i / 6.0) - (Math.PI / 2.0);
        }
    }

    @Override
    protected void init() {
        super.init();

        // Request latest stats from server using reflection helper for 1.20.1 & 1.21.1 compatibility
        if (this.client != null && this.client.getNetworkHandler() != null) {
            NetworkUtils.sendPacket(this.client.getNetworkHandler(), new ClientStatusC2SPacket(ClientStatusC2SPacket.Mode.REQUEST_STATS));
        }

        // Load statistics
        refreshStats();

        int topY = 24;
        int btnWidth = 140;

        // Tab 1: Current World
        this.addDrawableChild(ButtonWidget.builder(
                Text.translatable(activeTab == 0 ? "freshstats.tab.current_selected" : "freshstats.tab.current"),
                button -> {
                    activeTab = 0;
                    rebuildTabButtons();
                }
        ).dimensions(this.width / 2 - btnWidth - 10, topY, btnWidth, 20).build());

        // Tab 2: Global Stats
        this.addDrawableChild(ButtonWidget.builder(
                Text.translatable(activeTab == 1 ? "freshstats.tab.global_selected" : "freshstats.tab.global"),
                button -> {
                    activeTab = 1;
                    rebuildTabButtons();
                }
        ).dimensions(this.width / 2 + 10, topY, btnWidth, 20).build());

        // Bottom buttons
        int bottomY = this.height - 26;

        // Button: Vanilla Stats
        this.addDrawableChild(ButtonWidget.builder(
                Text.translatable("freshstats.btn.vanilla"),
                button -> {
                    if (this.client != null) {
                        allowVanillaStats = true;
                        this.client.setScreen(new StatsScreen(this.parent, this.statHandler));
                    }
                }
        ).dimensions(this.width / 2 - 130, bottomY, 120, 20).build());

        // Button: Close
        this.addDrawableChild(ButtonWidget.builder(
                Text.translatable("freshstats.btn.close"),
                button -> this.close()
        ).dimensions(this.width / 2 + 10, bottomY, 120, 20).build());
    }

    private void refreshStats() {
        currentWorldStats = StatAggregator.aggregateCurrent(statHandler);
        globalStats = GlobalStatsStorage.getGlobalAggregatedStats(currentWorldStats);
    }

    private void rebuildTabButtons() {
        this.clearAndInit();
    }

    @Override
    public void renderBackground(DrawContext context, int mouseX, int mouseY, float delta) {
        // Deep elegant dark background
        context.fill(0, 0, this.width, this.height, 0xFF0D1117);
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        // Refresh stats dynamically
        refreshStats();

        // Calculate smooth opening animation progress (Cubic Ease-Out over 450ms)
        float animProgress = Math.min(1.0f, (System.currentTimeMillis() - openTime) / 450.0f);
        float eased = (float) (1.0 - Math.pow(1.0 - animProgress, 3));

        // 1. FIRST render solid background & button widgets
        this.renderBackground(context, mouseX, mouseY, delta);
        super.render(context, mouseX, mouseY, delta);

        // 2. Enable blend for smooth semi-transparent radar polygon & thin lines
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();

        Map<StatCategory, CategoryData> statsData = (activeTab == 0) ? currentWorldStats : globalStats;

        // Header Title & Subtitle
        Text subTitleText = (activeTab == 0) ? Text.translatable("freshstats.sub.current", GlobalStatsStorage.getCurrentWorldOrServerId())
                : Text.translatable("freshstats.sub.global", GlobalStatsStorage.getSavedWorldsCount(), GlobalStatsStorage.getSavedWorldNamesSummary());
        context.drawCenteredTextWithShadow(textRenderer, Text.translatable("freshstats.title"), this.width / 2, 4, 0xFFFFFFFF);
        context.drawCenteredTextWithShadow(textRenderer, subTitleText, this.width / 2, 14, 0xFF8E9BAE);

        // Calculate Radar Center & Radius with smooth opening scaling
        int centerX = this.width / 2;
        int centerY = (48 + (this.height - 26)) / 2;
        int baseRadius = Math.min(this.width, this.height) / 2 - 95;
        if (baseRadius < 65) baseRadius = 65;
        int radius = (int) (baseRadius * (0.25f + 0.75f * eased));

        // Calculate max category score
        long maxScore = 0;
        for (StatCategory cat : categories) {
            CategoryData cd = statsData.get(cat);
            if (cd != null && cd.getTotalValue() > maxScore) {
                maxScore = cd.getTotalValue();
            }
        }

        // Compute Radar Points with Square Root Scaling & opening animation
        for (int i = 0; i < 6; i++) {
            maxPointX[i] = (float) (centerX + Math.cos(angles[i]) * radius);
            maxPointY[i] = (float) (centerY + Math.sin(angles[i]) * radius);

            CategoryData cd = statsData.get(categories[i]);
            long val = (cd != null) ? cd.getTotalValue() : 0;

            double ratio;
            if (maxScore > 0 && val > 0) {
                double linearRatio = (double) val / maxScore;
                ratio = (0.25 + 0.75 * Math.sqrt(linearRatio)) * eased;
            } else {
                ratio = 0.20 * eased;
            }

            pointX[i] = (float) (centerX + Math.cos(angles[i]) * (baseRadius * ratio));
            pointY[i] = (float) (centerY + Math.sin(angles[i]) * (baseRadius * ratio));
        }

        // 3. Draw thin elegant radar grid & axis rays
        drawRadarGrid(context, centerX, centerY, radius);

        // 4. Draw Radar Filled Polygon & Outline (Dotless clean geometry)
        drawRadarPolygon(context, centerX, centerY, pointX, pointY);

        boolean isModalOpen = (activeModal != null && activeModal.isVisible());

        // 5. Draw Axis Labels and Category Values ONLY IF MODAL IS NOT OPEN
        if (!isModalOpen) {
            drawCategoryLabels(context, centerX, centerY, radius, statsData, mouseX, mouseY, eased);
        }

        // 6. Check Hover (No idle dots, subtle highlight on hover only)
        int hoveredCategoryIndex = -1;
        if (!isModalOpen) {
            hoveredCategoryIndex = checkVertexHover(context, mouseX, mouseY);
        }

        // 7. Draw Tooltip for Hovered Category Point
        if (hoveredCategoryIndex != -1 && !isModalOpen) {
            renderCategoryTooltip(context, categories[hoveredCategoryIndex], statsData.get(categories[hoveredCategoryIndex]), mouseX, mouseY);
        }

        // 8. Render Active Modal Popup
        if (isModalOpen) {
            activeModal.render(context, mouseX, mouseY, delta);
        }
    }

    // ========================
    // ELEGANT FINE DRAWING PRIMITIVES
    // ========================

    private void drawLine(DrawContext context, float x1, float y1, float x2, float y2, float thickness, int color) {
        float dx = x2 - x1;
        float dy = y2 - y1;
        float len = (float) Math.hypot(dx, dy);
        if (len < 0.5f) return;

        int steps = (int) Math.ceil(len * 2.0f);
        int half = Math.max(0, Math.round((thickness - 1.0f) / 2.0f));

        for (int s = 0; s <= steps; s++) {
            float t = (float) s / steps;
            int px = Math.round(x1 + dx * t);
            int py = Math.round(y1 + dy * t);
            context.fill(px - half, py - half, px + half + 1, py + half + 1, color);
        }
    }

    private void fillTriangle(DrawContext context, float x1, float y1, float x2, float y2, float x3, float y3, int color) {
        int minY = Math.max(0, (int) Math.floor(Math.min(y1, Math.min(y2, y3))));
        int maxY = (int) Math.ceil(Math.max(y1, Math.max(y2, y3)));

        for (int y = minY; y <= maxY; y++) {
            float xA = Float.MAX_VALUE;
            float xB = -Float.MAX_VALUE;

            if ((y1 <= y && y <= y2) || (y2 <= y && y <= y1)) {
                if (Math.abs(y2 - y1) > 0.001f) {
                    float x = x1 + (y - y1) * (x2 - x1) / (y2 - y1);
                    xA = Math.min(xA, x); xB = Math.max(xB, x);
                }
            }
            if ((y2 <= y && y <= y3) || (y3 <= y && y <= y2)) {
                if (Math.abs(y3 - y2) > 0.001f) {
                    float x = x2 + (y - y2) * (x3 - x2) / (y3 - y2);
                    xA = Math.min(xA, x); xB = Math.max(xB, x);
                }
            }
            if ((y3 <= y && y <= y1) || (y1 <= y && y <= y3)) {
                if (Math.abs(y1 - y3) > 0.001f) {
                    float x = x3 + (y - y3) * (x1 - x3) / (y1 - y3);
                    xA = Math.min(xA, x); xB = Math.max(xB, x);
                }
            }

            if (xA <= xB && xA != Float.MAX_VALUE) {
                context.fill((int) Math.floor(xA), y, (int) Math.ceil(xB) + 1, y + 1, color);
            }
        }
    }

    // ========================
    // RADAR CHART COMPONENTS
    // ========================

    private void drawRadarGrid(DrawContext context, int centerX, int centerY, int radius) {
        float[] levels = {0.25f, 0.50f, 0.75f, 1.0f};

        // Draw axis rays (Muted dark slate lines)
        for (int i = 0; i < 6; i++) {
            drawLine(context, centerX, centerY, maxPointX[i], maxPointY[i], 1.0f, 0xFF2A394A);
        }

        // Draw web rings (Soft steel-blue thin rings)
        for (float level : levels) {
            int ringColor = (level == 1.0f) ? 0xFF3D526A : 0xFF212E3D;
            float thickness = 1.0f;

            for (int i = 0; i < 6; i++) {
                int next = (i + 1) % 6;
                float lx1 = (float) (centerX + Math.cos(angles[i]) * (radius * level));
                float ly1 = (float) (centerY + Math.sin(angles[i]) * (radius * level));
                float lx2 = (float) (centerX + Math.cos(angles[next]) * (radius * level));
                float ly2 = (float) (centerY + Math.sin(angles[next]) * (radius * level));
                drawLine(context, lx1, ly1, lx2, ly2, thickness, ringColor);
            }
        }
    }

    private void drawRadarPolygon(DrawContext context, int centerX, int centerY, float[] px, float[] py) {
        int fillColor = 0x3800B4D8; // Soft semi-transparent cyan-blue

        // 1. Render Filled Polygon
        for (int i = 0; i < 6; i++) {
            int next = (i + 1) % 6;
            fillTriangle(context, centerX, centerY, px[i], py[i], px[next], py[next], fillColor);
        }

        // 2. Render Polygon Outline (Thin modern teal stroke without dots)
        for (int i = 0; i < 6; i++) {
            int next = (i + 1) % 6;
            drawLine(context, px[i], py[i], px[next], py[next], 1.8f, 0xFF00B4D8);
        }
    }

    private void drawCategoryLabels(DrawContext context, int centerX, int centerY, int radius, Map<StatCategory, CategoryData> statsData, int mouseX, int mouseY, float eased) {
        int labelOffset = (int) (26 * eased);

        for (int i = 0; i < 6; i++) {
            StatCategory cat = categories[i];
            double lx = centerX + Math.cos(angles[i]) * (radius + labelOffset);
            double ly = centerY + Math.sin(angles[i]) * (radius + labelOffset);

            CategoryData cd = statsData.get(cat);
            long val = (cd != null) ? cd.getTotalValue() : 0;
            String textStr = cat.getText().getString();
            String valueStr = String.format("%,d %s", val, cd != null ? cd.getMainUnit() : "");

            int textWidth = textRenderer.getWidth(textStr);
            int valWidth = textRenderer.getWidth(valueStr);
            int cardW = Math.max(textWidth, valWidth) + 14;
            int cardH = 22;

            int drawX = (int) lx - cardW / 2;
            int drawY = (int) ly - 11;

            // Sleek dark label cards with subtle border
            context.fill(drawX - 1, drawY - 1, drawX + cardW + 1, drawY + cardH + 1, 0xFF2A3444);
            context.fill(drawX, drawY, drawX + cardW, drawY + cardH, 0xEE121722);

            // Category Name
            context.drawCenteredTextWithShadow(textRenderer, textStr, (int) lx, drawY + 2, cat.getColor());
            // Category Score
            context.drawCenteredTextWithShadow(textRenderer, valueStr, (int) lx, drawY + 12, 0xFFFFFFFF);
        }
    }

    private int checkVertexHover(DrawContext context, int mouseX, int mouseY) {
        int hoveredIndex = -1;

        for (int i = 0; i < 6; i++) {
            int vx = Math.round(pointX[i]);
            int vy = Math.round(pointY[i]);
            StatCategory cat = categories[i];

            double distSq = (mouseX - vx) * (mouseX - vx) + (mouseY - vy) * (mouseY - vy);
            boolean hovered = distSq <= 196; // 14px check

            if (hovered) {
                hoveredIndex = i;
                // Render sleek white/category highlight marker ONLY when mouse hovers over vertex
                context.fill(vx - 3, vy - 3, vx + 4, vy + 4, 0xFFFFFFFF);
                context.fill(vx - 2, vy - 2, vx + 3, vy + 3, cat.getColor());
            }
        }

        return hoveredIndex;
    }

    private void renderCategoryTooltip(DrawContext context, StatCategory cat, CategoryData cd, int mouseX, int mouseY) {
        if (cd == null) return;

        List<Text> tooltip = new ArrayList<>();
        tooltip.add(cat.getText().copy().formatted(net.minecraft.util.Formatting.BOLD, net.minecraft.util.Formatting.GOLD));
        tooltip.add(cat.getDescriptionText().copy().formatted(net.minecraft.util.Formatting.GRAY));
        tooltip.add(Text.translatable("freshstats.tooltip.total", String.format("%,d", cd.getTotalValue()), cd.getMainUnitText()).formatted(net.minecraft.util.Formatting.GREEN));
        tooltip.add(Text.literal(""));

        List<CategoryData.DetailEntry> details = cd.getDetails();
        if (!details.isEmpty()) {
            tooltip.add(Text.translatable("freshstats.tooltip.top").formatted(net.minecraft.util.Formatting.DARK_AQUA));
            int limit = Math.min(3, details.size());
            for (int i = 0; i < limit; i++) {
                CategoryData.DetailEntry entry = details.get(i);
                tooltip.add(Text.literal(" • ").append(entry.getName()).append(": " + String.format("%,d", entry.getCount())));
            }
        }

        tooltip.add(Text.literal(""));
        tooltip.add(Text.translatable("freshstats.tooltip.click_hint").formatted(net.minecraft.util.Formatting.YELLOW, net.minecraft.util.Formatting.ITALIC));

        context.drawTooltip(textRenderer, tooltip, mouseX, mouseY);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (activeModal != null && activeModal.isVisible()) {
            if (activeModal.mouseClicked(mouseX, mouseY, button)) {
                return true;
            }
        }

        Map<StatCategory, CategoryData> statsData = (activeTab == 0) ? currentWorldStats : globalStats;

        for (int i = 0; i < 6; i++) {
            float vx = pointX[i];
            float vy = pointY[i];
            double distSq = (mouseX - vx) * (mouseX - vx) + (mouseY - vy) * (mouseY - vy);

            if (distSq <= 196) { // 14px click radius
                CategoryData cd = statsData.get(categories[i]);
                if (cd != null) {
                    int modalW = Math.min(420, this.width - 40);
                    int modalH = Math.min(320, this.height - 60);
                    int modalX = (this.width - modalW) / 2;
                    int modalY = (this.height - modalH) / 2;

                    activeModal = new DetailModalWidget(this, cd, modalX, modalY, modalW, modalH);
                    return true;
                }
            }
        }

        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        if (activeModal != null && activeModal.isVisible()) {
            return activeModal.mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount);
        }
        return super.mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount);
    }

    @Override
    public boolean shouldPause() {
        return false;
    }
}
