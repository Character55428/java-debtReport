package com.myproject;

public class ProjectContext {
    // 用一個靜態變數（static）來存放被選擇的影片路徑
    private static String selectedVideoPath = "";

    public static void setSelectedVideoPath(String path) {
        selectedVideoPath = path;
    }

    public static String getSelectedVideoPath() {
        return selectedVideoPath;
    }
}
