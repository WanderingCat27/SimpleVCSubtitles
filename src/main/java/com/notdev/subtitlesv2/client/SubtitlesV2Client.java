package com.notdev.subtitlesv2.client;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

public class SubtitlesV2Client implements ClientModInitializer {
    public static String MOD_ID = "subtitlesvc";


    private static File config = new File("config/subtitles.config");

    @Override
    public void onInitializeClient() {
        Transcriber.init();

        // read python file
//        CLASSLOADER = Thread.currentThread().getContextClassLoader();

        // create directories
//        File createFile = new File("whisper/input");
//        if (!createFile.exists())
//            while (!createFile.mkdirs()) ;
//        createFile = new File("whisper/output");
//        if (!createFile.exists())
//            while (!createFile.mkdirs()) ;
//        if (!config.exists()) {
//            try {
//                while (!config.createNewFile()) ;
//                Files.write(config.toPath(), "path: \"insert/path/here\"".getBytes(StandardCharsets.UTF_8));
//            } catch (IOException e) {
//                throw new RuntimeException("Pl");
//            }
//        }

//        try {
//            if (!getConfigPath().equalsIgnoreCase("insert/path/here"))
//                whisper_cppPath = Paths.get(getConfigPath());
//        } catch (IOException e) {
//            throw new RuntimeException(e);
//        }
    }

    // hacky config solution dont feel like dealing with json rn
    public static String getConfigPath() throws IOException {
        Path path = config.toPath();

        // Read all lines from the file into a List of Strings
        List<String> lines = Files.readAllLines(path);

        // Print each line from the file
        for (String line : lines) {
            if (line.contains("path")) {
                return line.split("\"")[1];
            }
        }

        return null;
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