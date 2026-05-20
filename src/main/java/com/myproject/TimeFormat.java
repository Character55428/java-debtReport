package com.myproject;

public enum TimeFormat {
    /** 標準字幕格式 00:00:00,000 (包含毫秒) */
    SRT_TIMESTAMP {
        @Override
        public String format(long totalMillis) {
            long hours = totalMillis / 3600000;
            long minutes = (totalMillis % 3600000) / 60000;
            long seconds = (totalMillis % 60000) / 1000;
            long millis = totalMillis % 1000;
            return String.format("%02d:%02d:%02d,%03d", hours, minutes, seconds, millis);
        }
    },
    /** 簡短格式 00:00 (只顯示分:秒) */
    MS {
        @Override
        public String format(long totalMillis) {
            long minutes = (totalMillis / 1000) / 60;
            long seconds = (totalMillis / 1000) % 60;
            return String.format("%02d:%02d", minutes, seconds);
        }
    };

    // 注意：這裡將參數改為毫秒 (totalMillis)，對語音斷句更精準
    public abstract String format(long totalMillis);
}