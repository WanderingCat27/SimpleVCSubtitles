package com.notdev.subtitlesv2.client;

import de.maxhenkel.voicechat.api.VoicechatApi;
import de.maxhenkel.voicechat.api.events.ClientReceiveSoundEvent;
import de.maxhenkel.voicechat.api.events.EventRegistration;

import javax.sound.sampled.AudioFormat;

public class VoicechatPlugin implements de.maxhenkel.voicechat.api.VoicechatPlugin {

    private final static int max = 200;
    private int bufferIndex = 0;
    private short[] buffer = new short[960 * (max + 2)];

    static AudioFormat stereoFormat = new AudioFormat(AudioFormat.Encoding.PCM_SIGNED, 16000, 16, 2, 2, 16000, true);

    @Override
    public String getPluginId() {
        return SubtitlesV2Client.MOD_ID;
    }

    @Override
    public void initialize(VoicechatApi api) {

        de.maxhenkel.voicechat.api.VoicechatPlugin.super.initialize(api);
    }

    @Override
    public void registerEvents(EventRegistration registration) {
        de.maxhenkel.voicechat.api.VoicechatPlugin.super.registerEvents(registration);

        registration.registerEvent(ClientReceiveSoundEvent.EntitySound.class, this::receiveSound);
    }

    private void receiveSound(ClientReceiveSoundEvent.EntitySound entitySound) {
        if (bufferIndex >= max * 960) {

            SubtitlesV2Client.sendMessage(Transcriber.Transcribe(buffer));
            buffer = new short[960 * (max + 2)];
            bufferIndex = 0;
        }
        for (short bit : entitySound.getRawAudio())
                buffer[bufferIndex++] = bit;

        bufferIndex++;
    }
}
