package com.myproject;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class WhisperTranscriber {
    public static void processVideo(String videoPath) {
        String projectDir = "C:\\Users\\user\\Desktop\\Debahomework\\java\\java\\video-word";
        String whisperExePath = projectDir + "\\whisper-cli.exe";
        String modelPath = projectDir + "\\libs\\ggml-base.bin";
        String wavPath = ProjectContext.getSelectedVideoPath().replaceAll("\\.mp4$", ".wav"); 
        String ffmpegExePath = projectDir + "\\ffmpeg.exe"; 

        // =================================================================
        // 🎬 【步驟 1】FFmpeg 提取音軌
        // =================================================================
        try {
            System.out.println("   ?? 正在啟動 FFmpeg 提取 16kHz 單聲道音軌...");
            File tempWav = new File(wavPath);
            if (tempWav.exists()) {
                tempWav.delete();
            }

            List<String> ffmpegCmd = new ArrayList<>();
            ffmpegCmd.add(ffmpegExePath); 
            ffmpegCmd.add("-i"); ffmpegCmd.add(videoPath);
            ffmpegCmd.add("-vn"); 
            ffmpegCmd.add("-acodec"); ffmpegCmd.add("pcm_s16le"); 
            ffmpegCmd.add("-ar"); ffmpegCmd.add("16000"); 
            ffmpegCmd.add("-ac"); ffmpegCmd.add("1");     
            ffmpegCmd.add("-y");  
            ffmpegCmd.add(wavPath);

            ProcessBuilder ffmpegPb = new ProcessBuilder(ffmpegCmd);
            ffmpegPb.redirectErrorStream(true);
            Process ffmpegProcess = ffmpegPb.start();
            
            BufferedReader ffmpegReader = new BufferedReader(new InputStreamReader(ffmpegProcess.getInputStream()));
            while (ffmpegReader.readLine() != null) {}
            ffmpegProcess.waitFor();
            System.out.println("   ?? 音軌實體轉換成功，暫存檔已就緒。");

        } catch (Exception e) {
            System.out.println("   ❌ FFmpeg 執行失敗: " + e.getMessage());
            return;
        }

        // =================================================================
        // 🤖 【步驟 2】啟動 Whisper AI 辨識並精確解析
        // =================================================================
        File wavFile = new File(wavPath);
        if (!wavFile.exists() || wavFile.length() == 0) {
            System.out.println("   ❌ 錯誤：找不到音訊暫存實體，無法辨識。");
            return;
        }

        List<String> command = new ArrayList<>();
        command.add(whisperExePath);
        command.add("-m"); command.add(modelPath);
        command.add("-f"); command.add(wavPath);
        command.add("-l"); command.add("auto"); 

        try {
            ProcessBuilder pb = new ProcessBuilder(command);
            pb.directory(new File(projectDir)); 
            
            Map<String, String> env = pb.environment();
            String currentPath = env.getOrDefault("Path", "");
            env.put("Path", projectDir + ";" + currentPath); 
            
            pb.redirectErrorStream(true);
            Process process = pb.start();
            
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream(), "UTF-8"));
            String line;
            VideoClipLinkList list = new VideoClipLinkList();

            // 🔥 【黃金修正】調整為精確匹配 "[00:00:00.000 --> 00:00:00.000]" 格式的正則表達式
            Pattern pattern = Pattern.compile("\\[(\\d{2}):(\\d{2}):(\\d{2})\\.(\\d{3})\\s*-->\\s*(\\d{2}):(\\d{2}):(\\d{2})\\.(\\d{3})\\](.*)");

            while ((line = reader.readLine()) != null) {
                String trimmedLine = line.trim();
                Matcher matcher = pattern.matcher(trimmedLine);
                
                if (matcher.matches()) {
                    long startMillis = Integer.parseInt(matcher.group(1)) * 3600000L  
                                     + Integer.parseInt(matcher.group(2)) * 60000L    
                                     + Integer.parseInt(matcher.group(3)) * 1000L     
                                     + Integer.parseInt(matcher.group(4));            

                    long endMillis = Integer.parseInt(matcher.group(5)) * 3600000L 
                                   + Integer.parseInt(matcher.group(6)) * 60000L   
                                   + Integer.parseInt(matcher.group(7)) * 1000L    
                                   + Integer.parseInt(matcher.group(8));           

                    String text = matcher.group(9).trim();

                    ClipData clip = new ClipData(startMillis, endMillis, text);
                    list.add(clip); // 這次絕對能成功掛進你的鏈結串列了！

                    System.out.println("   [Whisper 辨識到字幕] " + clip.toString());
                } else {
                    if(!trimmedLine.isBlank() && !trimmedLine.startsWith("whisper_")) {
                        System.out.println("   [Whisper 系統資訊] " + trimmedLine);
                    }
                }
            }

            int exitCode = process.waitFor();
            if (exitCode == 0) {
                String finalOutputPath = ProjectContext.getSelectedVideoPath().replaceAll("\\.mp4$", ".txt");
                list.saveToFile(finalOutputPath);
                
                System.out.println("\n   ? 辨識與斷句儲存完成！結果已存至: " + finalOutputPath);
                System.out.println("   ? 鏈結串列成功儲存了 " + list.size() + " 個斷句片段。");
                
                if (wavFile.exists()) { wavFile.delete(); } 
            } else {
                System.out.println("   ❌ Whisper 執行失敗，錯誤代碼: " + exitCode);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}