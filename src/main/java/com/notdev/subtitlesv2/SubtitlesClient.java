package com.notdev.subtitlesv2;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.text.Text;

import java.io.*;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class SubtitlesClient implements ClientModInitializer {
    public static String MOD_ID = "subtitlesvc";

    private static String baseURL = "https://ggml.ggerganov.com/ggml-model-whisper-base.bin";
    private static String tinyURL = "https://ggml.ggerganov.com/ggml-model-whisper-tiny.bin";
    private static String smallURL = "https://ggml.ggerganov.com/ggml-model-whisper-small.bin";

    private static String DISTILL_EN_SMALL_URL = "https://huggingface.co/distil-whisper/distil-small.en/resolve/main/ggml-distil-small.en.bin?download=true";

    public static String basePath = "ggml-base.bin";
    public static String tinyPath = "ggml-tiny.bin";
    public static String smallPath = "ggml-small.bin";
    public static String DISTIlL_EN_SMALL_PATH = "distill-small.bin";


    @Override
    public void onInitializeClient() {
        SubtitlesHandler.init();
        try {
            downloadFile(tinyURL, "models", tinyPath);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        Transcriber.init();


        HudRenderCallback.EVENT.register((matrixStack, tickDelta) -> {
            SubtitlesHandler.draw(matrixStack);
        });
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



    public static String readFileToString(Path filePath) throws IOException {
        String str = Files.readString(Path.of(filePath.toAbsolutePath() + ".txt"));
        File out =new File(String.valueOf(filePath));
        out.deleteOnExit();
        out.delete();
        return str;
    }


}