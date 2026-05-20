package com.myproject;

public class ClipData {
    private long startTime; // 開始時間 (毫秒)
    private long endTime;   // 結束時間 (毫秒)
    private String text;    // 文字

    public ClipData(long startTime, long endTime, String text) {
        this.startTime = startTime;
        this.endTime = endTime;
        this.text = text;
    }

    // Getters 和 Setters
    public long getStartTime() { return startTime; }
    public long getEndTime() { return endTime; }
    public String getText() { return text; }

    @Override
    public String toString() {
        // 呼叫時間化妝師轉成簡短格式 (例如 01:23)
        String startStr = TimeFormat.MS.format(startTime);
        String endStr = TimeFormat.MS.format(endTime);
        return "[" + startStr + " -> " + endStr + "] " + text;
    }
}