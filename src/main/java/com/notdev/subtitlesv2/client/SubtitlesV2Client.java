package com.notdev.subtitlesv2.client;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.text.Text;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.*;

public class SubtitlesV2Client implements ClientModInitializer {
    public static String MOD_ID = "subtitlesvc";

    public static ClassLoader CLASSLOADER;

    public static boolean isRunningScript = false;

    private static ArrayList<String> subtitles = new ArrayList<>(10);


    @Override
    public void onInitializeClient() {

        // read python file
        CLASSLOADER = Thread.currentThread().getContextClassLoader();


        // create directories
        File createFile = new File("whisper/input");
        if (!createFile.exists())
            while (!createFile.mkdirs()) ;
        createFile = new File("whisper/output");
        if (!createFile.exists())
            while (!createFile.mkdirs()) ;


    }


    // SENDS MESSAGE TO CLIENT
    @Environment(EnvType.CLIENT)
    public static void sendMessage(String message) {
        MinecraftClient.getInstance().player.sendMessage(Text.literal(message));
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


    private static final ExecutorService executorService = Executors.newFixedThreadPool(2);

    public static void initTranscriberThread(String file) {
        new Thread(() -> {
                    System.out.println("transcribing");
                    try {
                        transcribe(file);
                    } catch (IOException e) {
                        System.out.println("io");
                    } catch (InterruptedException e) {
                        System.out.println("interupted");
                    } catch (ExecutionException e) {
                        System.out.println("execution error");
                    }
        }).start();
        System.out.println("started thread");

    }

    private static void transcribe(String file) throws IOException, InterruptedException, ExecutionException {
        ProcessBuilder builder = new ProcessBuilder();
        boolean isWindows = System.getProperty("os.name")
                .toLowerCase().startsWith("windows");

        Path file_path = Paths.get("whisper", "input", file);
        Path resolvedPath_file = file_path.normalize();

        Path output_dir = Paths.get("whisper", "output");
        Path resolvedPath_output = output_dir.normalize();

        if (isWindows) {
            builder.command("cmd.exe", "/c", "dir");
        } else {
            builder.command("sh", "-c", " export PATH=ffmpeg:$PATH; source env/bin/activate; whisper " + resolvedPath_file + " --model base --language English --output_dir " + resolvedPath_output + " --output_format txt");
        }
        Process process = builder.start();

        try (InputStream inputStream = process.getInputStream()) {
            StreamGobbler streamGobbler = new StreamGobbler(inputStream, System.out::println);
            Future<?> future = executorService.submit(streamGobbler);

            int exitCode = process.waitFor();

            // Properly handle exceptions
            try {
                future.get(10, TimeUnit.SECONDS);
            } catch (TimeoutException e) {
                // Handle timeout exception
                e.printStackTrace();
            }

            assertEquals(0, exitCode);
        }

        sendMessage(readFileToString("whisper/output/"+file.replaceAll("mp3", "txt")));
    }

    public static String readFileToString(String filePath) throws IOException {
        Path path = Paths.get(filePath);
        byte[] encodedBytes = Files.readAllBytes(path);
        return new String(encodedBytes);
    }

    static class StreamGobbler implements Runnable {
        private final InputStream inputStream;
        private final java.util.function.Consumer<String> consumer;

        public StreamGobbler(InputStream inputStream, java.util.function.Consumer<String> consumer) {
            this.inputStream = inputStream;
            this.consumer = consumer;
        }

        @Override
        public void run() {
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream))) {
                reader.lines().forEach(consumer);
            } catch (IOException e) {
                // Handle IO exception
                e.printStackTrace();
            }
        }
    }

    // Dummy method for the sake of the example
    private static void assertEquals(int expected, int actual) {
        if (expected != actual) {
            throw new AssertionError("Expected: " + expected + ", Actual: " + actual);
        }
    }
}