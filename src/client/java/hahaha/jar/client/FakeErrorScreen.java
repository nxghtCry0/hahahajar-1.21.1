package hahaha.jar.client;

import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;

import java.util.List;

public class FakeErrorScreen extends Screen {
    private int ticks = 0;
    private final List<String> originalLines = List.of(
        "Fatal OpenGL Error: GL_INVALID_OPERATION (1282)",
        "Shader compile failed in shaders/squircle.frag",
        "Device context lost. Core profile failed to initialize.",
        "Attempting to restore rendering state...",
        "it is watching you"
    );

    public FakeErrorScreen() {
        super(Component.literal("Error"));
    }

    @Override
    public boolean shouldCloseOnEsc() {
        return false;
    }

    @Override
    public void tick() {
        ticks++;
        if (ticks >= 150) {
            this.minecraft.setScreen(null);
        }
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        guiGraphics.fill(0, 0, width, height, 0xFFFFFFFF);
        
        float progress = 0.0f;
        if (ticks > 40) {
            progress = Math.min(1.0f, (ticks - 40) / 80.0f);
        }

        int startY = height / 2 - (originalLines.size() * 12) / 2;
        String laughStr = "laugh";
        for (int i = 0; i < originalLines.size(); i++) {
            String orig = originalLines.get(i);
            StringBuilder sb = new StringBuilder();
            for (int j = 0; j < orig.length(); j++) {
                char originalChar = orig.charAt(j);
                if (originalChar == ' ') {
                    sb.append(' ');
                } else {
                    java.util.Random charRand = new java.util.Random((long) i * 100 + j);
                    if (charRand.nextFloat() < progress) {
                        int laughIndex = j % laughStr.length();
                        sb.append(laughStr.charAt(laughIndex));
                    } else {
                        sb.append(originalChar);
                    }
                }
            }
            String lineText = sb.toString();
            int textWidth = this.font.width(lineText);
            guiGraphics.drawString(this.font, lineText, width / 2 - textWidth / 2, startY + i * 12, 0xFF000000, false);
        }
    }
}
