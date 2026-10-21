package com.example.infinitejump;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import org.lwjgl.glfw.GLFW;

import java.nio.file.Files;
import java.nio.file.Path;

public class InfiniteJumpClient implements ClientModInitializer {
    public static int boundKey = GLFW.GLFW_KEY_K; // default key
    public static boolean enabled = false;         // infinite jump on/off
    public static boolean suppressOpen = false;    // prevents instant re-open after closing with Right Shift

    private boolean wasRShift = false;
    private boolean wasBound = false;

    private static Path configPath() {
        return FabricLoader.getInstance().getConfigDir().resolve("jumpers.txt");
    }

    public static void saveConfig() {
        try {
            Files.writeString(configPath(), Integer.toString(boundKey));
        } catch (Exception ignored) {
        }
    }

    private static void loadConfig() {
        try {
            Path p = configPath();
            if (Files.exists(p)) {
                boundKey = Integer.parseInt(Files.readString(p).trim());
            }
        } catch (Exception ignored) {
        }
    }

    @Override
    public void onInitializeClient() {
        loadConfig();
        ClientTickEvents.START_CLIENT_TICK.register(this::tick);
    }

    private void tick(MinecraftClient client) {
        if (client.getWindow() == null) return;
        long window = client.getWindow().getHandle();

        boolean rShift = InputUtil.isKeyPressed(window, GLFW.GLFW_KEY_RIGHT_SHIFT);
        boolean bound = InputUtil.isKeyPressed(window, boundKey);

        if (suppressOpen && !rShift) suppressOpen = false;

        if (client.player == null) {
            enabled = false;
        } else if (client.currentScreen == null) {
            if (rShift && !wasRShift && !suppressOpen) {
                client.setScreen(new JumpScreen());
            } else if (bound && !wasBound) {
                enabled = !enabled;
                if (!enabled) KeyBinding.updatePressedStates(); // release the simulated jump key
            }
        }

        wasRShift = rShift;
        wasBound = bound;

        // Hold the jump key every tick -> the player jumps again as soon as they land
        if (enabled && client.player != null && client.currentScreen == null) {
            client.options.jumpKey.setPressed(true);
        }
    }
}
