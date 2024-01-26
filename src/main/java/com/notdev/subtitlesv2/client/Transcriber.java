package com.notdev.subtitlesv2.client;

import de.maxhenkel.lame4j.Mp3Encoder;
import de.maxhenkel.lame4j.UnknownPlatformException;
import de.maxhenkel.voicechat.api.audio.AudioConverter;
import de.maxhenkel.voicechat.voice.common.Utils;
import io.github.givimad.whisperjni.WhisperContext;
import io.github.givimad.whisperjni.WhisperFullParams;
import io.github.givimad.whisperjni.WhisperJNI;

import javax.lang.model.element.ModuleElement;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Path;

import static de.maxhenkel.voicechat.voice.common.Utils.shortsToFloats;


public class Transcriber {

    private static io.github.givimad.whisperjni.WhisperJNI whisper;
    private static io.github.givimad.whisperjni.WhisperContext ctx;

    public static void init() {
        // Get the current working directory
        try {
            io.github.givimad.whisperjni.WhisperJNI.loadLibrary(); // load platform binaries

            io.github.givimad.whisperjni.WhisperJNI.setLibraryLogger(System.out::println); // capture/disable whisper.cpp log
            whisper = new WhisperJNI();

//            URL model = Thread.currentThread().getContextClassLoader().getResource("ggml-base.bin");
            Path model = Path.of("models/ggml-base.bin");
            ctx = whisper.init(model);
        } catch (IOException e) {
            System.err.println(e.toString());
        }

    }


    // tried but only kinda works
    private static float[] toFloat(short[] shorts) {
        // transform the samples to f32 samples
        float[] samples = new float[shorts.length];
        for (int i = 0; i < samples.length; i++)
            samples[i] = Float.max(-1f, Float.min(((float) shorts[i]) / (float) Short.MAX_VALUE, 1f));
        return samples;
    }


    private static float[] toFloat2(short[] shorts) {
        // transform the samples to f32 samples
        float[] samples = new float[shorts.length / 2];

        for (int i = 0; i < samples.length; i += 2)
            samples[i / 2] = (float) (short) ((shorts[i] & 0xFF) + ((shorts[i + 1] & 0xFF) << 8));
        return samples;
    }


    private static float[] shortToByteTofloat(short[] shorts) {
        byte[] bytes = shortToBytes(shorts);
        float[] floats = new float[bytes.length / 2];
        for (int i = 0; i < bytes.length; i += 2) {
            floats[i / 2] = bytes[i] | (bytes[i + 1] < 128 ? (bytes[i + 1] << 8) : ((bytes[i + 1] - 256) << 8));
        }
        return floats;
    }

    private static byte[] shortToBytes(short[] shorts) {
        byte[] bytes = new byte[shorts.length * 2];
        for (int index = 0; index < bytes.length; index += 2) {
            bytes[index] = (byte) (shorts[index / 2] >> 8);
            bytes[index + 1] = (byte) shorts[index / 2];
        }

        return bytes;
    }

    private static float[] toFloat3(short[] audioShorts) {
        float[] audioFloats = new float[audioShorts.length];
        for (int i = 0; i < audioShorts.length; i++)
            audioFloats[i] = ((float) audioShorts[i]) / 0x8000;
        return audioFloats;
    }

    public static void saveFloatArrayToMp3(float[] audioData, int sampleRate, String filePath) {
        // Convert float array to 16-bit signed PCM
        short[] pcmData = floatArrayTo16BitPCM(audioData);

        try (FileOutputStream fileOutputStream = new FileOutputStream(filePath);
             // i think this is roughly the format expected by whisper jni
             Mp3Encoder mp3Encoder = new Mp3Encoder(1, (int) 16000, 16, 5, fileOutputStream)) {

            // Encode and save the audio data
            mp3Encoder.write(pcmData);

        } catch (IOException | UnknownPlatformException e) {
            e.printStackTrace();
        }
    }

    private static short[] floatArrayTo16BitPCM(float[] floatArray) {
        short[] pcmData = new short[floatArray.length];

        for (int i = 0; i < floatArray.length; i++) {
            pcmData[i] = (short) (floatArray[i] * (Short.MAX_VALUE));
        }

        return pcmData;
    }



    public static String Transcribe(short[] shorts) {
        // voice chat built in method
        float samples[] = shortsToFloats(shorts);
        float mono[] = new float[samples.length / 2];
        for (int index = 0; index < samples.length; index += 4) {
            // if i throw out a bunch of the data it seems get down to the format that whisper wants
            mono[index/4] = Float.max(-1f, Float.min((samples[index]) / (float) Short.MAX_VALUE, 1f));
        }
//        saveFloatArrayToMp3(mono, 16000, "test.mp3");

        var params = new WhisperFullParams();
        System.out.println(ctx);
        int result = whisper.full(ctx, params, mono, mono.length);
        String text = whisper.fullGetSegmentText(ctx, 0);
        System.out.println(text);
        return text;
    }

}
