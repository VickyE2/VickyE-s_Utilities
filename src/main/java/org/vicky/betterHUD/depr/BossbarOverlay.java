package org.vicky.betterHUD.depr;
public class BossbarOverlay {

    private String type;
    private String file;
    private int split;
    private BossbarType splitType;

    // Nested class for listener settings.
    public static class ListenerSetting {
        private String clazz;
        private String value;
        private String max;

        public ListenerSetting(String clazz, String value, String max) {
            this.clazz = clazz;
            this.value = value;
            this.max = max;
        }

        public String getClazz() {
            return clazz;
        }
        public String getValue() {
            return value;
        }
        public String getMax() {
            return max;
        }
    }

    private ListenerSetting setting;

    public BossbarOverlay(String type, String file, int split, BossbarType splitType, ListenerSetting setting) {
        this.type = type;
        this.file = file;
        this.split = split;
        this.splitType = splitType;
        this.setting = setting;
    }

    public String getType() {
        return type;
    }
    public String getFile() {
        return file;
    }
    public int getSplit() {
        return split;
    }
    public BossbarType getSplitType() {
        return splitType;
    }
    public ListenerSetting getSetting() {
        return setting;
    }
}