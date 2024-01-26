package com.notdev.subtitlesv2.client;

import io.github.givimad.whisperjni.WhisperContext;
import io.github.givimad.whisperjni.WhisperFullParams;
import io.github.givimad.whisperjni.WhisperJNI;

import javax.lang.model.element.ModuleElement;
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


    public static String Transcribe(short[] shorts) {
        float samples[] = shortsToFloats(shorts);

        float mono[] = new float[samples.length/2];
        int monoIndex = 0;
        for(int index = 0; index < samples.length; index+=2) {
            mono[monoIndex] = samples[index] /(32760.0f);
            monoIndex++;
        }


        var params = new WhisperFullParams();
        System.out.println(ctx);
        int result = whisper.full(ctx, params, mono, mono.length);
        String text = whisper.fullGetSegmentText(ctx, 0);
        System.out.println(text);
        return text;
    }

}
