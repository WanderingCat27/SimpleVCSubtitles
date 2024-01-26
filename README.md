# live captioning of SimpleVc mod

(unaffilated with simplevc mod independent plugin)

"working" beta (wip unreleased) of subtitles for simple vc in minecraft. Uses whisper https://github.com/openai/whisper. specifically master branch uses https://github.com/ggerganov/whisper.cpp (working but requires setup steps not currently provided)


This is in progress, trying to reduce the barrier to entry and packaging as much as possible within the mod and requiring as little setup as possible while maintaining speed using fast whisper versions like whisper.cpp or whisper jni


java whisper wrapper (https://github.com/GiviMAD/whisper-jni) (also unaffiliated) most promising but only works in dev not build (at least on macos arm) might work on windows, not investigated fully
