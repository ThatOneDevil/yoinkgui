package me.thatonedevil.mixin.client;

import me.thatonedevil.YoinkGUIClient;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static net.minecraft.text.Text.literal;

@Mixin(Screen.class)
public class ScreenMixin {
    @Inject(at = @At("HEAD"), method = "render")
    private void render(DrawContext context, int mouseX, int mouseY, float deltaTicks, CallbackInfo ci) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null) {
            return;
        }

        int parseButtonX = 40;
        int parseButtonY = 35; // Position below the first button
        int parseButtonWidth = 160;
        int parseButtonHeight = 20;
        String parseButtonText = "Yoink and Parse NBT into file";

        // Register HUD rendering event
        // Calculate mouse position in UI space
        int scaledWidth = client.getWindow().getScaledWidth();
        int scaledHeight = client.getWindow().getScaledHeight();
        int mouseXUi = (int) (client.mouse.getX() * scaledWidth / client.getWindow().getWidth());
        int mouseYUi = (int) (client.mouse.getY() * scaledHeight / client.getWindow().getHeight());

        YoinkGUIClient.INSTANCE.setParseButtonHovered(mouseXUi >= parseButtonX && mouseXUi <= parseButtonX + parseButtonWidth &&
                mouseYUi >= parseButtonY && mouseYUi <= parseButtonY + parseButtonHeight);

        // Draw second button (Parse NBT) with appropriate color
        int parseBgColor = YoinkGUIClient.INSTANCE.getParseButtonHovered() ? 0xAA444444 : 0xAA000000;
        context.fill(parseButtonX, parseButtonY, parseButtonX + parseButtonWidth, parseButtonY + parseButtonHeight, parseBgColor);
        context.drawCenteredTextWithShadow(
                client.textRenderer,
                literal(parseButtonText),
                parseButtonX + parseButtonWidth / 2,
                parseButtonY + (parseButtonHeight - 8) / 2,
                0xFFFFFFFF
        );

    }

}