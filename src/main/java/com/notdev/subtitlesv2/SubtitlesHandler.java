package com.notdev.subtitlesv2;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.text.Text;

import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

public class SubtitlesHandler {
    // static class
    private SubtitlesHandler() {}
    private static ArrayList<String> subtitles;

    public static final int MAX_BACKLOG = 3;
    public static final int TIMEOUT = 5000;

    private static final int textColor = (255 << 16) + (255 << 8) + (255) + (255 << 24);
    private static final int bgColor = (0 << 16) + (0 << 8) + (0) + (255 << 24);


    public static void init() {
        subtitles = new ArrayList<>(MAX_BACKLOG);
    }
    public static void add(String text) {
        if(subtitles.size() >= MAX_BACKLOG) {
            subtitles.remove(0);
        }
        subtitles.add(text);

        // Schedule a task to be executed after TIMEOUT
        // removes subtitle from list (so subtitles dont linger forever)
        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                subtitles.remove(text);
            }
        }, TIMEOUT);

    }

    public static void draw(DrawContext matrixStack) {
        int index = 0;
        for(String subtitle : subtitles) {
            for (String line : wrapText(subtitle, 50)) {
                MinecraftClient.getInstance().inGameHud.getTextRenderer().drawWithOutline(Text.of(line).asOrderedText(), 10, 10 + index * 10, textColor, bgColor, matrixStack.getMatrices().peek().getPositionMatrix(), matrixStack.getVertexConsumers(), 255);
                index++;
            }
        }


}

private static String[] wrapText(String text, int characterWrap) {
    // Calculate the number of lines required
    int numLines = (int) Math.ceil((double) text.length() / characterWrap);
    String[] lines = new String[numLines];

    int arrayIndex = 0;
    int currentIndex = 0;

    while (currentIndex < text.length()) {
        // Find the next space within the character wrap limit
        int endIndex = currentIndex + characterWrap;
        if (endIndex > text.length()) {
            endIndex = text.length();
        } else {
            while (endIndex < text.length() && text.charAt(endIndex) != ' ') {
                endIndex++;
            }
        }

        lines[arrayIndex] = text.substring(currentIndex, endIndex).trim();
        arrayIndex++;
        currentIndex = endIndex + 1; // Move the index to the next character after the space
    }

    return lines;
}

}
