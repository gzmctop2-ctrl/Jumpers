package com.example.infinitejump;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.util.InputUtil;
import net.minecraft.text.Text;
import org.lwjgl.glfw.GLFW;

public class JumpScreen extends Screen {
    private boolean listening = false;
    private ButtonWidget keyButton;

    public JumpScreen() {
        super(Text.literal("Jumpers"));
    }

    @Override
    protected void init() {
        int cx = this.width / 2;
        int y = this.height / 2 - 10;

        // "Bind" - fixed label, does nothing
        addDrawableChild(ButtonWidget.builder(Text.literal("Bind"), b -> {})
                .dimensions(cx - 50, y, 60, 20).build());

        // Key button, flush against the first one (cx - 50 + 60 = cx + 10)
        keyButton = addDrawableChild(ButtonWidget.builder(keyLabel(), b -> {
            listening = true;
            keyButton.setMessage(keyLabel());
        }).dimensions(cx + 10, y, 40, 20).build());
    }

    private Text keyLabel() {
        if (listening) return Text.literal("...");
        String name = InputUtil.fromKeyCode(InfiniteJumpClient.boundKey, 0)
                .getLocalizedText().getString().toUpperCase();
        return Text.literal(name);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (listening) {
            if (keyCode == GLFW.GLFW_KEY_ESCAPE) {
                listening = false;                       // cancel
            } else if (keyCode != GLFW.GLFW_KEY_RIGHT_SHIFT) { // RShift is reserved for the menu
                InfiniteJumpClient.boundKey = keyCode;
                InfiniteJumpClient.saveConfig();
                listening = false;
            }
            keyButton.setMessage(keyLabel());
            return true;
        }
        if (keyCode == GLFW.GLFW_KEY_RIGHT_SHIFT) {
            InfiniteJumpClient.suppressOpen = true;
            this.close();
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    // No blur/dark background -> light HUD
    @Override
    public void renderBackground(DrawContext context, int mouseX, int mouseY, float delta) {
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        super.render(context, mouseX, mouseY, delta);
        boolean on = InfiniteJumpClient.enabled;
        context.drawCenteredTextWithShadow(this.textRenderer,
                Text.literal("Jumpers: " + (on ? "ON" : "OFF")),
                this.width / 2, this.height / 2 - 26, on ? 0xFF55FF55 : 0xFFAAAAAA);
    }

    @Override
    public boolean shouldPause() {
        return false; // game keeps running while the menu is open
    }
}
