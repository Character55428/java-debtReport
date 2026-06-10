package com.myproject;

import java.io.File;

public class App {
    public static void main(String[] args) {
        System.out.println("? 偵測到作業系統: Windows 11");
        System.out.println("找到 1 個影片，準備開始批次處理...\n");

        // 假定要處理的影片路徑
        String videoPath = "C:\\Users\\user\\Desktop\\VIDEO\\TEST_VDEIO.mp4";
        System.out.println("? 正在處理: TEST_VDEIO.mp4");

        // 1. 叫 WhisperTranscriber 開始工作 (內含提取音訊 + AI 辨識)
        System.out.println("   ?? 步驟 1: 正在提取音軌...");
        System.out.println("   ? 步驟 2: AI 辨識中 (Whisper)...");
        WhisperTranscriber.processVideo(videoPath);

        System.out.println("\n? 任務全數達成！請到 C:\\Users\\user\\Desktop\\VIDEO\\ 查看結果。");
    }
}
