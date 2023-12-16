package org.notdev.voicechatsubtitles;

import com.mojang.authlib.minecraft.client.MinecraftClient;
import de.maxhenkel.voicechat.VoicechatClient;
import de.maxhenkel.voicechat.api.VoicechatApi;
import de.maxhenkel.voicechat.api.VoicechatPlugin;
import de.maxhenkel.voicechat.api.events.ClientEvent;
import de.maxhenkel.voicechat.api.events.ClientReceiveSoundEvent;
import de.maxhenkel.voicechat.api.events.EventRegistration;
import de.maxhenkel.voicechat.api.mp3.Mp3Encoder;
import de.maxhenkel.voicechat.plugins.impl.mp3.Mp3EncoderImpl;
import de.maxhenkel.voicechat.voice.client.SoundManager;
import org.notdev.voicechatsubtitles.client.VoicechatSubtitlesClient;

import javax.sound.sampled.*;
import java.io.*;
import java.nio.ByteBuffer;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;

public class VoiceChatPlugin implements VoicechatPlugin {


    public static String outputString;

    File outputFile;

    final int max = 960 * 75;
    final int numSections = 10;

    int sectionIndex;
    int fileIndex;
    short[] buffer = new short[max + 960 * 2];
    AudioFormat stereoFormat = new AudioFormat(AudioFormat.Encoding.PCM_SIGNED, SoundManager.SAMPLE_RATE / 2, 16, 2, 4, SoundManager.FRAME_SIZE, false);

    File recordingDir = new File("whisper/");


    /**
     * @return the unique ID for this voice chat plugin
     */
    @Override
    public String getPluginId() {
        return VoicechatSubtitles.MOD_ID;
    }

    /**
     * Called when the voice chat initializes the plugin.
     *
     * @param api the voice chat API
     */
    @Override
    public void initialize(VoicechatApi api) {
        outputFile = new File("test");

        Thread monitor = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    // Specify the directory to monitor
                    Path directory = Paths.get("whisper-output");

                    // Create a file system watcher
                    WatchService watchService = FileSystems.getDefault().newWatchService();
                    directory.register(watchService, StandardWatchEventKinds.ENTRY_CREATE);

                    while (true) {
                        WatchKey key = watchService.take();

                        for (WatchEvent<?> event : key.pollEvents()) {
                            Path filePath = (Path) event.context();
                            if (filePath.toString().endsWith(".txt")) {
                                // Print file contents
                                try {
                                    String fileContent = Files.readString(directory.resolve(filePath));
                                    outputString = fileContent;

                                    // Delete the file
                                    Files.delete(directory.resolve(filePath));
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                            }
                        }

                        key.reset();
                    }
                } catch (IOException | InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });

        monitor.start();
    }

    /**
     * Called once by the voice chat to register all events.
     *
     * @param registration the event registration
     */
    @Override
    public void registerEvents(EventRegistration registration) {
        registration.registerEvent(ClientReceiveSoundEvent.EntitySound.class, this::receiveSound);

    }

    int index = 0;

    private void receiveSound(ClientReceiveSoundEvent clientReceiveSoundEvent) {
        if(outputString != null) {
            System.out.println(outputString);
            outputString = null;
        }
        if (index >= max) {
            System.out.println("Appending");
            appendShortArrayToWav(outputFile.getAbsolutePath(), buffer);
            buffer = new short[(max) + 960 * 2];
            index = 0;
        }
        for (short sh : clientReceiveSoundEvent.getRawAudio())
            buffer[index++] = sh;
    }

    public void appendShortArrayToWav(String wavFilePath, short[] newAudioData) {
        if (sectionIndex >= numSections) {
            sectionIndex = 0;

            Path sourcePath = getFile().toPath();
            Path targetPath = Paths.get("whisper-input", getFile().getName());

            try {
                Files.move(sourcePath, targetPath);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            fileIndex++;
        }
        try {
            File file = getFile();
            if(!file.exists()) {
                recordingDir.mkdir();
                file.createNewFile();
                file.deleteOnExit();
            }
            FileOutputStream outStream = new FileOutputStream(file.getPath(), true);

            Mp3Encoder encoder = Mp3EncoderImpl.createEncoder(stereoFormat, 120, VoicechatClient.CLIENT_CONFIG.recordingQuality.get(), outStream);
            encoder.encode(newAudioData);
            sectionIndex++;

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private File getFile() {
        return new File(recordingDir.getPath() + "/recording" + fileIndex + ".mp3");
    }

}
