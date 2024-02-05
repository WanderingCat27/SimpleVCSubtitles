package com.notdev.subtitlesv2;

import de.maxhenkel.voicechat.api.Player;
import de.maxhenkel.voicechat.api.VoicechatApi;
import de.maxhenkel.voicechat.api.events.ClientReceiveSoundEvent;
import de.maxhenkel.voicechat.api.events.EventRegistration;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.PlayerManager;
import net.minecraft.server.ServerMetadata;
import net.minecraft.util.Uuids;

import javax.sound.sampled.AudioFormat;
import java.util.UUID;

public class VoicechatPlugin implements de.maxhenkel.voicechat.api.VoicechatPlugin {


    static AudioFormat stereoFormat = new AudioFormat(AudioFormat.Encoding.PCM_SIGNED, 16000, 16, 2, 2, 16000, true);

    @Override
    public String getPluginId() {
        return SubtitlesClient.MOD_ID;
    }

    @Override
    public void initialize(VoicechatApi api) {

        de.maxhenkel.voicechat.api.VoicechatPlugin.super.initialize(api);
    }

    @Override
    public void registerEvents(EventRegistration registration) {
        de.maxhenkel.voicechat.api.VoicechatPlugin.super.registerEvents(registration);

        registration.registerEvent(ClientReceiveSoundEvent.EntitySound.class, this::receiveSound);
        registration.registerEvent(ClientReceiveSoundEvent.StaticSound.class, this::receiveStaticSound);
    }

    private void receiveStaticSound(ClientReceiveSoundEvent.StaticSound staticSound) {
        processSound(staticSound.getId(), staticSound.getRawAudio());
    }

    private void receiveSound(ClientReceiveSoundEvent.EntitySound entitySound) {
        processSound(entitySound.getId(), entitySound.getRawAudio());
    }
    private static void processSound(UUID uuid, short[] rawAudio) {
        PlayerEntity sender = MinecraftClient.getInstance().world.getPlayerByUuid(uuid);
        if(sender != null)
        if(VoiceDataMap.append(sender.getDisplayName().getString(), rawAudio)) {
            // transcribes text into a subtitle [<ign>]: <transcription>
            SubtitlesHandler.add(String.format("[%s]: %s",sender.getDisplayName().getString(), Transcriber.Transcribe(VoiceDataMap.remove(sender.getDisplayName().getString()))));
        }
    }
}
