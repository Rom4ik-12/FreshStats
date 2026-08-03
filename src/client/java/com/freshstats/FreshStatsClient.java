package com.freshstats;

import com.freshstats.data.GlobalStatsStorage;
import com.freshstats.gui.RadarChartScreen;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents;
import net.fabricmc.fabric.api.client.screen.v1.Screens;
import net.minecraft.client.gui.screen.StatsScreen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.text.Text;
import org.lwjgl.glfw.GLFW;

public class FreshStatsClient implements ClientModInitializer {
    public static KeyBinding OPEN_RADAR_STATS_KEY;

    @Override
    public void onInitializeClient() {
        GlobalStatsStorage.load();

        OPEN_RADAR_STATS_KEY = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.freshstats.open",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_O,
                "category.freshstats"
        ));

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            while (OPEN_RADAR_STATS_KEY.wasPressed()) {
                if (client.player != null && client.player.getStatHandler() != null) {
                    client.setScreen(new RadarChartScreen(client.currentScreen, client.player.getStatHandler()));
                }
            }
        });

        // 1. Redirect vanilla StatsScreen to RadarChartScreen automatically when opened from pause menu
        ScreenEvents.BEFORE_INIT.register((client, screen, scaledWidth, scaledHeight) -> {
            if (screen instanceof StatsScreen && !(screen instanceof RadarChartScreen)) {
                if (!RadarChartScreen.allowVanillaStats) {
                    client.execute(() -> {
                        if (client.player != null && client.player.getStatHandler() != null) {
                            client.setScreen(new RadarChartScreen(null, client.player.getStatHandler()));
                        }
                    });
                }
            }
        });

        // 2. Add "Радар Диаграмма" button to top-right corner when vanilla StatsScreen is opened intentionally
        ScreenEvents.AFTER_INIT.register((client, screen, scaledWidth, scaledHeight) -> {
            if (screen instanceof StatsScreen && !(screen instanceof RadarChartScreen)) {
                if (RadarChartScreen.allowVanillaStats) {
                    RadarChartScreen.allowVanillaStats = false;

                    int buttonWidth = 125;
                    int buttonHeight = 20;
                    int x = scaledWidth - buttonWidth - 10;
                    int y = 10;

                    ButtonWidget button = ButtonWidget.builder(
                            Text.literal("Радар Диаграмма"),
                            b -> {
                                if (client.player != null && client.player.getStatHandler() != null) {
                                    client.setScreen(new RadarChartScreen(screen, client.player.getStatHandler()));
                                }
                            }
                    ).dimensions(x, y, buttonWidth, buttonHeight).build();

                    Screens.getButtons(screen).add(button);
                }
            }
        });
    }
}
