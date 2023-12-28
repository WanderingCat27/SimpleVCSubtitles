package com.notdev.subtitlesv2.client;

import de.maxhenkel.voicechat.VoicechatClient;
import de.maxhenkel.voicechat.api.VoicechatApi;
import de.maxhenkel.voicechat.api.events.ClientReceiveSoundEvent;
import de.maxhenkel.voicechat.api.events.EventRegistration;
import de.maxhenkel.voicechat.api.mp3.Mp3Encoder;
import de.maxhenkel.voicechat.plugins.impl.mp3.Mp3EncoderImpl;
import de.maxhenkel.voicechat.voice.client.SoundManager;
import net.minecraft.client.sound.AudioStream;

import javax.sound.sampled.*;
import java.io.*;
import java.nio.ByteBuffer;
import java.nio.file.Files;

public class VoicechatPlugin implements de.maxhenkel.voicechat.api.VoicechatPlugin {


    public static String tempDirPath ="tmp/";

    private final static int max = 200;
    private int bufferIndex = 0;
    private short[] buffer = new short[960 * (max+2)];

    private static int fileIndex = 0;

    private int audioBufferLength = 1;
    private int audioIndex = 0;

    static AudioFormat stereoFormat = new AudioFormat(AudioFormat.Encoding.PCM_SIGNED, 16000, 16, 2, 2, 16000, true);    @Override
    public String getPluginId() {
        return SubtitlesV2Client.MOD_ID;
    }

    @Override
    public void initialize(VoicechatApi api) {

        de.maxhenkel.voicechat.api.VoicechatPlugin.super.initialize(api);
        System.out.println("sample rate " + stereoFormat.getSampleRate());
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
            appendToWav(buffer);
            buffer = new short[960 * (max+2)];
            bufferIndex = 0;
            if(audioIndex++ >= audioBufferLength-1) {
                System.out.println("moving");
//                moveFile();
                audioIndex = 0;
            }
        }
        for(short bit : entitySound.getRawAudio())
            buffer[bufferIndex++] = bit;

        bufferIndex++;
    }

    private void moveFile() {
        try {
            File newFile = new File("whisper/input/out_" + (fileIndex++) +".wav");
            Files.move(getFile().toPath(), newFile.toPath());
            System.out.println(newFile.getName());
            SubtitlesV2Client.initTranscriberThread(newFile.getName());
            newFile.deleteOnExit();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static void appendToWav(short[] newAudioData) {
        try {
            writeWav(getFile(), shortArrayToByteArray(newAudioData),stereoFormat);
            SubtitlesV2Client.initTranscriberThread(getFile().getName());
            fileIndex++;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void writeWav(File file, byte[] data, AudioFormat format)
            throws IllegalArgumentException, IOException {
        ByteArrayInputStream bais = new ByteArrayInputStream(data);
        AudioInputStream ais = new AudioInputStream(bais, format,
               data.length);
        AudioSystem.write(ais, AudioFileFormat.Type.WAVE, file);
    }

    private static byte[] shortArrayToByteArray(short[] shortArray) {
        byte[] bytes = new byte[(int) (shortArray.length*1.5)];
        int byteIndex = 0;
        for (int index = 0; index < shortArray.length; index++) {
            if(index % 3 == 0) continue; // discard 1/3 of data to bring down to 16khz
            bytes [byteIndex] = (byte)(shortArray[index] >>>8);
            bytes [byteIndex+1] = (byte)((shortArray[index]&0xFF));
            byteIndex+=2;
        }
        return bytes;
    }

    private static File getFile() {
        File dir = new File("whisper/input");
        File file = new File(dir.getAbsolutePath() + "/out_" + fileIndex  + ".wav");
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
