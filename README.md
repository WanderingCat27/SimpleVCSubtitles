# live captioning of SimpleVc mod

(Simple Voice Chat independent plugin Mod)

"working" beta (wip unreleased) of subtitles for simple vc in minecraft. Uses whisper https://github.com/openai/whisper, specifically whisper JNI.

version 1.0 should work out of the box and download the model into instance folder, no additional setup. If this does not work you can manually download https://ggml.ggerganov.com/ggml-model-whisper-base.bin and **rename it to ggml-base.bin** and create a folder in your instance folder (.minecrft) called models then put ggml-base.bin into models

transcription is a bit clunky rn because idk how to translate shorts to floats properly and between two different formats so whisper is not getting the best quality audio data because idk how to fix it (if you know how to work with audio help would be appreciated)

but works pretty good currently
