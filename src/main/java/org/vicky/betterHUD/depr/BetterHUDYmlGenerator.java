package org.vicky.betterHUD.depr;

import java.util.Map;

public class BetterHUDYmlGenerator {
    public enum ImageSplitType {
        UP,
        DOWN,
        LEFT,
        RIGHT,
        CIRCLE,
        REVERSED_CIRCLE

    }

    public static StringBuilder generateSingleImage(String imageID, String imageName) {
        return new StringBuilder()
                .append(imageID).append(":\n")
                .append("  type: single\n")
                .append("  file: ").append("\"").append(imageName).append("\"");
    }

    public static StringBuilder generateListenerImage(String imageID, String imageName, int splits, ImageSplitType splitType, BossbarOverlay.ListenerSetting listener) {
        return new StringBuilder()
                .append(imageID).append(":\n")
                .append("  type: listener\n")
                .append("  file: ").append("\"").append(imageName).append("\"").append("\n")
                .append("  split: ").append(splits).append("\n")
                .append("  split-type: ").append(splitType.toString().toUpperCase()).append("\n")
                .append("  setting: ").append("\n")
                .append("    listener: ").append("\n")
                .append("      class: ").append(listener.getClazz()).append("\n")
                .append(listener.getValue() != null ? "      value: \"" + listener.getValue() + "\"\n" : "")
                .append(listener.getMax() != null ? "      max: \"" + listener.getMax() + "\"\n" : "");
    }

    public static StringBuilder generateSequenceImage(String imageID, Map<String, Integer> files) {
        StringBuilder builder = new StringBuilder();
        builder.append(imageID).append(":\n")
                .append("  type: sequence\n")
                .append("  files: ").append("\n");
        for (Map.Entry<String, Integer> file : files.entrySet()) {
            builder.append("    - ").append(file.getKey()).append(file.getValue() != 0 && file.getValue() != null ? ": " + file.getValue() : "").append("\n");
        }
        return builder;
    }
}
