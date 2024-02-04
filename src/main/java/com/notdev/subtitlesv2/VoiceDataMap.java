package com.notdev.subtitlesv2;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class VoiceDataMap {
    private static Map<String, ArrayList<Short>> map = new HashMap<String, ArrayList<Short>>();
    private static int bufferSize = 200;

    //static class
    private VoiceDataMap() {}

    // returns true if should transcribe
    public static boolean append(String player, short[] data) {
        ArrayList list = getOrCreate(player);
        for(short sh : data)
            list.add(sh);
        return list.size() >= 960*bufferSize;
    }


    private static ArrayList<Short> getOrCreate(String player) {
        if (!map.containsKey(player)) {
            ArrayList list = new ArrayList<Short>(960 * bufferSize);
            map.put(player, list);
            return list;
        }

        return map.get(player);
    }


    public static short[] remove(String player) {
        ArrayList<Short> list = map.remove(player);

        if (list != null) {
            // Convert ArrayList<Short> to short[]
            short[] result = new short[list.size()];
            for (int i = 1; i < list.size(); i++) {
                result[i] = list.get(i);
            }
            return result;
        } else {
            return null; // or handle the case when player is not found
        }
    }
}
