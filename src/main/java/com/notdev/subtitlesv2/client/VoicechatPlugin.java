package com.notdev.subtitlesv2.client;

import de.maxhenkel.voicechat.VoicechatClient;
import de.maxhenkel.voicechat.api.VoicechatApi;
import de.maxhenkel.voicechat.api.events.ClientReceiveSoundEvent;
import de.maxhenkel.voicechat.api.events.EventRegistration;
import de.maxhenkel.voicechat.api.mp3.Mp3Encoder;
import de.maxhenkel.voicechat.plugins.impl.mp3.Mp3EncoderImpl;
import de.maxhenkel.voicechat.voice.client.SoundManager;

import javax.sound.sampled.AudioFormat;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;

public class VoicechatPlugin implements de.maxhenkel.voicechat.api.VoicechatPlugin {


    public static String tempDirPath ="tmp/";

    private final static int max = 200;
    private int bufferIndex = 0;
    private short[] buffer = new short[960 * (max+2)];

    private int fileIndex = 0;

    private int audioBufferLength = 3;
    private int audioIndex = 0;

    AudioFormat stereoFormat = new AudioFormat(AudioFormat.Encoding.PCM_SIGNED, SoundManager.SAMPLE_RATE / 2, 16, 2, 4, SoundManager.FRAME_SIZE, false);

    @Override
    public String getPluginId() {
        return SubtitlesV2Client.MOD_ID;
    }

    @Override
    public void initialize(VoicechatApi api) {

        de.maxhenkel.voicechat.api.VoicechatPlugin.super.initialize(api);
        try {
            getFile().createNewFile();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void registerEvents(EventRegistration registration) {
        de.maxhenkel.voicechat.api.VoicechatPlugin.super.registerEvents(registration);

        registration.registerEvent(ClientReceiveSoundEvent.EntitySound.class, this::receiveSound);
    }

    private void receiveSound(ClientReceiveSoundEvent.EntitySound entitySound) {
        if(bufferIndex >= max * 960) {

            System.out.println("appending");
            appendSound(buffer);
            buffer = new short[960 * (max+2)];
            bufferIndex = 0;
            if(audioIndex++ >= audioBufferLength-1) {
                System.out.println("moving");
                moveFile();
                audioIndex = 0;
            }
        }
        for(short bit : entitySound.getRawAudio())
            buffer[bufferIndex++] = bit;

        bufferIndex++;
    }

    private void moveFile() {
        try {
            File newFile = new File("whisper/input/out_" + (fileIndex++) +".mp3");
            Files.move(getFile().toPath(), newFile.toPath());
            System.out.println(newFile.getName());
            SubtitlesV2Client.initTranscriberThread(newFile.getName());
            newFile.deleteOnExit();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void appendSound(short[] bytes) {
        try {
            FileOutputStream outStream = new FileOutputStream(getFile().getPath(), true);
            Mp3Encoder encoder = Mp3EncoderImpl.createEncoder(stereoFormat, 120, VoicechatClient.CLIENT_CONFIG.recordingQuality.get(), outStream);
            encoder.encode(bytes);
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }

    private static File getFile() {
        File dir = new File("whisper/tmp");
        File file = new File(dir.getAbsolutePath() + "/tmp.mp3");
        while(!file.exists()) {
            try {
                dir.mkdirs();
                file.createNewFile();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        file.deleteOnExit();
        return file;
    }
}
