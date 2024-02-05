package com.notdev.subtitlesv2;

import de.maxhenkel.lame4j.Mp3Encoder;
import de.maxhenkel.lame4j.UnknownPlatformException;
import de.maxhenkel.voicechat.voice.common.Utils;
import io.github.givimad.whisperjni.WhisperFullParams;
import io.github.givimad.whisperjni.WhisperJNI;
import io.github.givimad.whisperjni.WhisperSamplingStrategy;

import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Arrays;


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
            Path model = Path.of("models/" + SubtitlesClient.tinyPath);
            ctx = whisper.init(model);
        } catch (IOException e) {
            System.err.println(e.toString());
        }

    }
        public static float[] convertStereoToMonoFloat(short[] stereoData) {
            int stereoLength = stereoData.length;
            int monoLength = stereoLength / 2; // Assuming stereoData is interleaved left-right.

            float[] monoData = new float[monoLength];

            for (int i = 0, j = 0; i < stereoLength; i += 2, j++) {
                int leftSample = stereoData[i];
                int rightSample = stereoData[i + 1];

                // Combine left and right samples and convert to float between -1 and 1.
                float monoSample = (float) (leftSample + rightSample) / (float) Short.MAX_VALUE / 2;

                // Store the float mono sample.
                monoData[j] = monoSample;
            }

            return monoData;
        }


    public static String Transcribe(short[] shorts) {
//        float[] samples = Utils.shortsToFloats(shorts);
//
//        float[] mono = new float[samples.length / 2];
//        for (int index = 0; index < samples.length; index += 4)
//            mono[index / 4] = Float.max(-1.0F, Float.min(samples[index] / 32767.0F, 1.0F));

        float[] mono = convertStereoToMonoFloat(shorts);
        WhisperFullParams params = new WhisperFullParams();
//        WhisperFullParams params = new WhisperFullParams(WhisperSamplingStrategy.BEAN_SEARCH);
        System.out.println(ctx);
        int result = whisper.full(ctx, params, mono, mono.length);
        if(result == 0) {
            String text = whisper.fullGetSegmentText(ctx, 0);

            return text;
        }
        return "";

    }

}
