package com.notdev.subtitlesv2.client;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.entity.boss.BossBar;
import net.minecraft.text.OrderedText;
import net.minecraft.text.Text;

import java.awt.Color;
import java.io.*;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import static net.minecraft.entity.boss.BossBar.*;
import static net.minecraft.entity.boss.BossBar.Color.*;

public class SubtitlesV2Client implements ClientModInitializer {
    public static String MOD_ID = "subtitlesvc";

    private static final int textColor = (255 << 16) + (255 << 8) + (255) + (255 << 24);
    private static final int bgColor = (0 << 16) + (0 << 8) + (0) + (255 << 24);

    @Override
    public void onInitializeClient() {
        try {
            downloadFile("https://ggml.ggerganov.com/ggml-model-whisper-base.bin", "models", "ggml-base.bin");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        Transcriber.init();


        HudRenderCallback.EVENT.register((matrixStack, tickDelta) -> {

            if(!Transcriber.lastSubtitle.isBlank()) {
                int index = 0;
                for (String line : wrapText(Transcriber.lastSubtitle, 50)) {
                    MinecraftClient.getInstance().inGameHud.getTextRenderer().drawWithOutline(Text.of(line).asOrderedText(), 10, 10 + index * 10, textColor, bgColor, matrixStack.getMatrices().peek().getPositionMatrix(), matrixStack.getVertexConsumers(), 255);

                    index++;
                }
            }
            });

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



    public static void downloadFile(String url, String directory, String fileName) throws IOException {
        Path filePath = Paths.get(directory, fileName);

        // Check if the file already exists in the specified directory
        if (Files.exists(filePath)) {
            System.out.println("File already exists: " + filePath);
            return;
        }

        // Create the directory if it doesn't exist
        if (!Files.exists(Paths.get(directory))) {
            Files.createDirectory(Paths.get(directory));
            System.out.println("created dir at " + Paths.get(directory).toAbsolutePath());
        }

        // Open a connection to the URL and create an input stream
        try (InputStream inputStream = new URL(url).openStream();
             FileOutputStream outputStream = new FileOutputStream(filePath.toFile())) {

            // Read data from the input stream and write it to the output stream
            byte[] buffer = new byte[1024];
            int bytesRead;
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, bytesRead);
            }

            System.out.println("File downloaded to: " + filePath.toAbsolutePath());

        } catch (IOException e) {
            e.printStackTrace();
            throw e; // Propagate the exception if needed
        }
    }


    // SENDS MESSAGE TO CLIENT
    @Environment(EnvType.CLIENT)
    public static void sendMessage(String message) {
        MinecraftClient.getInstance().player.sendMessage(net.minecraft.text.Text.literal(message));
    }

    // RUNS A SHELL COMMAND
    public static Process runCommand(String command) {
        System.out.println("Attempting command: " + command);
        try {
            ProcessBuilder processBuilder = new ProcessBuilder("/bin/bash", "-c", command);
            processBuilder.redirectErrorStream(true); // Redirect error stream to the input stream

            Process process = processBuilder.start();

            // Capture and print the combined output of the process
            InputStream in = process.getInputStream();
            BufferedReader reader = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8));
            String line;
            while ((line = reader.readLine()) != null) {
                System.out.println(line);
            }

            int exitCode = process.waitFor();
            System.out.println("Command exited with code: " + exitCode);

            return process;
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
            throw new RuntimeException("Failed to run " + command + ", try again");
        }
    }



    public static String readFileToString(Path filePath) throws IOException {
        String str = Files.readString(Path.of(filePath.toAbsolutePath() + ".txt"));
        File out =new File(String.valueOf(filePath));
        out.deleteOnExit();
        out.delete();
        return str;
    }


}