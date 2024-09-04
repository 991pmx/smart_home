package com.neusoft.testapplication.MusicPlayer.viewmodel.utils;

public class MusicFormatConverter {
    public static String formatDuration(long millis) {
        StringBuilder sb = new StringBuilder();
        long durationInSeconds = millis / 1000;
        long hours = durationInSeconds / 3600;
        long minutes = (durationInSeconds % 3600) / 60;
        long seconds = durationInSeconds % 60;

        if (hours > 0) {
            sb.append(String.format("%02d:", hours));
        }
        sb.append(String.format("%02d:", minutes));
        sb.append(String.format("%02d", seconds));

        return sb.toString();
    }
}
