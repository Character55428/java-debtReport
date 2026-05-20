package com.myproject;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.io.File;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import javafx.application.Platform;
import javafx.embed.swing.JFXPanel;
import javafx.scene.Scene;
import javafx.scene.control.Slider;
import javafx.scene.layout.BorderPane;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.media.MediaView;
import javafx.util.Duration;

public class VideoPlayer extends JFrame {

    private final JFXPanel jfxPanel = new JFXPanel();
    private MediaPlayer mediaPlayer;
    private MediaView mediaView;
    private Slider fxSlider; // 💡 使用 JavaFX 原生 Slider，完美與播放器同步不卡死
    private JLabel statusLabel;

    public VideoPlayer() {
        // 1. 設定 Swing 主視窗
        setTitle("Java 官方原生影音播放器 (執行緒隔離、進度條修復版)");
        setSize(1024, 720);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        getContentPane().setBackground(new Color(15, 15, 15));
        setLayout(new BorderLayout(5, 5));

        // 2. 頂部控制面板
        JPanel topPanel = new JPanel(new BorderLayout(10, 0));
        topPanel.setBackground(new Color(32, 32, 32));
        topPanel.setBorder(BorderFactory.createEmptyBorder(10, 15, 10, 15));

        JButton chooseButton = new JButton("選擇電腦中的影片檔 (.mp4)");
        chooseButton.setFont(new Font("Microsoft JhengHei", Font.BOLD, 14));
        chooseButton.setBackground(new Color(0, 122, 255)); // 質感藍
        chooseButton.setForeground(Color.WHITE);
        chooseButton.setFocusPainted(false);
        topPanel.add(chooseButton, BorderLayout.WEST);

        statusLabel = new JLabel(" 尚未選擇影片，請點擊左側按鈕...");
        statusLabel.setFont(new Font("Microsoft JhengHei", Font.PLAIN, 13));
        statusLabel.setForeground(new Color(200, 200, 200));
        topPanel.add(statusLabel, BorderLayout.CENTER);

        add(topPanel, BorderLayout.NORTH);
        add(jfxPanel, BorderLayout.CENTER); // 將 JavaFX 畫布放入 Swing 中央

        // 3. 監聽「選擇檔案」按鈕
        chooseButton.addActionListener(e -> {
            JFileChooser fileChooser = new JFileChooser();
            fileChooser.setCurrentDirectory(new File("C:\\Users\\user\\Desktop"));
            fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
            
            if (fileChooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
                File selectedFile = fileChooser.getSelectedFile();
                statusLabel.setText(" 正在載入並播放: " + selectedFile.getName());
                
                // 存入全域共享中心，供其他 AI 字幕模組提取路徑
                ProjectContext.setSelectedVideoPath(selectedFile.getAbsolutePath());
                
                // 💡 強制交給 JavaFX 執行緒去處理影片載入與播放
                Platform.runLater(() -> startPlayback(selectedFile.getAbsolutePath()));
            }
        });

        // 4. 初始化 JavaFX 的 UI 佈局
        Platform.runLater(this::initFXAndLayout);
    }

    /**
     * 💡 初始化 JavaFX 的場景與佈局
     */
    /**
     * 💡 初始化 JavaFX 的場景與佈局 (完美修復畫面卡出螢幕、控制列被擠掉的問題)
     */
    private void initFXAndLayout() {
        BorderPane root = new BorderPane();
        root.setStyle("-fx-background-color: black;");

        // 1. 建立影片顯示畫面
        mediaView = new MediaView();
        
        // 💡 關鍵修正 1：不要給死高度！改用綁定（Binding）讓影片畫面隨著視窗大小自動縮放
        // 這樣無論視窗怎麼拉，影片永遠會乖乖待在格子裡，絕對不會把底部的按鈕擠出去
        javafx.scene.layout.StackPane videoContainer = new javafx.scene.layout.StackPane();
        videoContainer.setStyle("-fx-background-color: black;");
        videoContainer.getChildren().add(mediaView);
        
        // 讓 mediaView 的寬高自動跟隨外層容器
        mediaView.fitWidthProperty().bind(videoContainer.widthProperty());
        mediaView.fitHeightProperty().bind(videoContainer.heightProperty());
        mediaView.setPreserveRatio(true); // 保持 16:9 比例不變形

        root.setCenter(videoContainer);

        // 2. 建立 JavaFX 原生進度條（Slider）
        fxSlider = new Slider();
        fxSlider.setMin(0);
        fxSlider.setValue(0);
        fxSlider.setStyle("-fx-background-color: #222; -fx-padding: 5 10 5 10;");

        // 監聽進度條的滑鼠拖拉事件
        fxSlider.valueProperty().addListener((observable, oldValue, newValue) -> {
            if (fxSlider.isValueChanging() && mediaPlayer != null) {
                mediaPlayer.seek(Duration.seconds(newValue.doubleValue()));
            }
        });

        // 3. 建立「播放/暫停」按鈕
        javafx.scene.control.Button playPauseBtn = new javafx.scene.control.Button("⏸ 暫停");
        playPauseBtn.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-background-color: #007AFF; -fx-text-fill: white; -fx-padding: 5 15 5 15;");
        
        playPauseBtn.setOnAction(event -> {
            if (mediaPlayer != null) {
                if (mediaPlayer.getStatus() == MediaPlayer.Status.PLAYING) {
                    mediaPlayer.pause();
                    playPauseBtn.setText("▶ 播放");
                } else {
                    mediaPlayer.play();
                    playPauseBtn.setText("⏸ 暫停");
                }
            }
        });

        // 4. 把「進度條」和「播放按鈕」用 VBox 包在一起
        javafx.scene.layout.VBox controlsContainer = new javafx.scene.layout.VBox(5);
        controlsContainer.setStyle("-fx-background-color: #222; -fx-padding: 10; -fx-alignment: center;");
        controlsContainer.getChildren().addAll(fxSlider, playPauseBtn);
        
        // 💡 關鍵修正 2：確保控制面板有固定高度，不會被壓縮
        controlsContainer.setMinHeight(80); 
        
        root.setBottom(controlsContainer);

        Scene scene = new Scene(root);
        jfxPanel.setScene(scene);
    }

    /**
     * 💡 核心播放與進度條同步邏輯（完全在 FX Thread 執行）
     */
    private void startPlayback(String filePath) {
        try {
            if (mediaPlayer != null) {
                mediaPlayer.stop();
                mediaPlayer.dispose();
            }

            File file = new File(filePath);
            Media media = new Media(file.toURI().toString());
            mediaPlayer = new MediaPlayer(media);
            mediaView.setMediaPlayer(mediaPlayer);

            // 💡 當影片載入完成 (Ready) 時，動態初始化進度條的最大值
            mediaPlayer.setOnReady(() -> {
                double totalSeconds = mediaPlayer.getTotalDuration().toSeconds();
                fxSlider.setMax(totalSeconds);
                System.out.println(" 🎥 影片載入成功，總長度: " + totalSeconds + " 秒。進度條解鎖！");
            });

            // 💡 隨著影片播放，自動同步更新進度條的位置
            mediaPlayer.currentTimeProperty().addListener((observable, oldTime, newTime) -> {
                if (!fxSlider.isValueChanging()) {
                    // 如果使用者沒有在拖拉，進度條就跟著影片時間走
                    fxSlider.setValue(newTime.toSeconds());
                }
            });

            mediaPlayer.setOnError(() -> System.out.println("❌ 播放器錯誤: " + mediaPlayer.getError().getMessage()));
            mediaPlayer.setAutoPlay(true);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            new VideoPlayer().setVisible(true);
        });
    }
}