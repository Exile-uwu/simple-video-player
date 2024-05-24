import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.util.ArrayList;
import java.util.Comparator;

import javax.swing.*;
import javax.swing.border.EmptyBorder;

import uk.co.caprica.vlcj.component.EmbeddedMediaPlayerComponent;
import uk.co.caprica.vlcj.player.embedded.EmbeddedMediaPlayer;

public class Window extends JFrame { // 继承

    private static final int PROGRESS_HEIGHT = 10;
    private static final int WINDOW_X = 100;
    private static final int WINDOW_Y = 100;
    private static final int WINDOW_WIDTH = 800;
    private static final int WINDOW_HEIGHT = 600;
    private static final int LIST_WINDOW_WIDTH = 200;
    // 总时间
    private static String TOTAL_TIME;
    // 播放速度
    private float speed;
    // 首次播放
    private boolean firstPlay = true;

    // 播放器组件
    private EmbeddedMediaPlayerComponent mediaPlayerComponent;
    // 进度条
    private JProgressBar progress;
    // 暂停按钮
    private Button pauseButton;
    // 快进
    private Button forwardButton;
    // 快退
    private Button backwardButton;
    // 显示播放速度的标签
    private Label displaySpeed;
    // 显示时间
    private Label displayTime;
    // 进度定时器
    private Timer progressTimer;
    // 继续播放定时器
    private Timer continueTimer;
    // 所有视频路径
    private java.util.List<String> videos;
    // 当前播放视频的位置
    private int videoIndex;
    // 声音控制进度条
    private JProgressBar volumeProgress;
    // 音量显示标签
    private Label volumeLabel;
    // 文件对话框
    private FileDialog fileDialog;
    // 播放文件列表按钮
    private Button listButton;
    // 播放文件列表窗口
    private JFrame listWindow;
    // 播放文件列表显示内容
    private JTextArea listContent;

    public Window() {
        this.videos = new ArrayList<>(10);
        // 设置默认速度为原速
        speed = 1.0f;
        // 设置窗口标题
        setTitle("媒体播放器");

        // 设置窗口位置
        setBounds(WINDOW_X, WINDOW_Y, WINDOW_WIDTH, WINDOW_HEIGHT);

        // 图标
        Image icon = Toolkit.getDefaultToolkit().getImage("/resources/icon.jpeg");
        this.setIconImage(icon);

        // 主面板
        JPanel contentPane = new JPanel();
        contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
        contentPane.setLayout(new BorderLayout(0, 0));
        setContentPane(contentPane);

        // ======播放面板======
        JPanel player = new JPanel();
        contentPane.add(player, BorderLayout.CENTER);
        player.setLayout(new BorderLayout(0, 0));
        // 创建播放器组件并添加到容器中去
        mediaPlayerComponent = new EmbeddedMediaPlayerComponent();
        player.add(mediaPlayerComponent);

        // ======底部面板======
        JPanel bottomPanel = new JPanel();
        BoxLayout boxLayout = new BoxLayout(bottomPanel, 1);
        bottomPanel.setLayout(boxLayout);
        contentPane.add(bottomPanel, BorderLayout.SOUTH);

        // ------进度条组件面板------
        JPanel progressPanel = new JPanel();
        progress = new JProgressBar();
        progress.setPreferredSize(getNewDimension());

        // 点击进度条调整视频播放指针
        progress.addMouseListener(setVideoPlayPoint());
        // 定时器
        progressTimer = getProgressTimer();

        progressPanel.add(progress);
        progressPanel.setAlignmentX(Component.CENTER_ALIGNMENT);
        bottomPanel.add(progressPanel);

        // ------按钮组件面板------
        JPanel buttonPanel = new JPanel();
        buttonPanel.setAlignmentX(Component.CENTER_ALIGNMENT);
        bottomPanel.add(buttonPanel);

        displayTime = new Label();
        displayTime.setText(getTimeString());
        buttonPanel.add(displayTime);

        Button chooseButton = new Button("选择文件");
        fileDialog = new FileDialog(this);
        fileDialog.setMultipleMode(true);
        chooseButton.setFocusable(false);
        chooseButton.addMouseListener(mouseClickedChooseFiles());
        buttonPanel.add(chooseButton);

        // 重置按钮
        Button resetButton = new Button("重置");
        resetButton.addMouseListener(mouseClickedResetVideo());
        buttonPanel.add(resetButton);

        // 快进
        forwardButton = new Button("快进");
        forwardButton.addMouseListener(mouseClickedForward());
        buttonPanel.add(forwardButton);

        // 快退
        backwardButton = new Button("快退");
        backwardButton.addMouseListener(mouseClickedBackward());
        buttonPanel.add(backwardButton);

        // 暂停/播放按钮
        pauseButton = new Button("播放");
        pauseButton.setPreferredSize(new Dimension(49, 23));
        pauseButton.addMouseListener(mouseClickedMediaPause());
        buttonPanel.add(pauseButton);

        // 倍速播放按钮：每次递增0.5，最大为3倍速
        Button fastForwardButton = new Button(">>>");
        fastForwardButton.setFocusable(false);
        fastForwardButton.addMouseListener(mouseClickedFastForward());
        buttonPanel.add(fastForwardButton);

        // 播放速度显示按钮
        displaySpeed = new Label();
        displaySpeed.setText("x" + speed);
        displaySpeed.setFocusable(false);
        displaySpeed.setEnabled(false);
        buttonPanel.add(displaySpeed);

        // 添加声音控制进度条
        volumeProgress = new JProgressBar();
        volumeProgress.setMinimum(0);
        volumeProgress.setMaximum(100);
        volumeProgress.setValue(100);
        volumeProgress.setPreferredSize(new Dimension(100, 10));
        volumeProgress.addMouseListener(mouseClickedSetVolumeValue());
        buttonPanel.add(volumeProgress);

        // 音量显示
        volumeLabel = new Label();
        setVolumeLabel(volumeProgress.getValue());
        buttonPanel.add(volumeLabel);

        // 播放文件列表显示内容
        listContent = new JTextArea();
        listContent.setText("");
        listContent.setLineWrap(true);
        listContent.setEditable(false);

        // 播放文件列表按钮
        listButton = new Button("播放列表");
        listButton.addMouseListener(mouseClickedSetListWindow());
        buttonPanel.add(listButton);

        // 播放文件列表按钮
        Button ClearButton = new Button("清空列表");
        ClearButton.addMouseListener(mouseClickedClearWindow());
        buttonPanel.add(ClearButton);

        continueTimer = getContinueTimer();

    }

    public void init() {
        // 设置窗口可见
        this.setVisible(true);
    }

    // 播放列表按钮点击事件
    private MouseAdapter mouseClickedSetListWindow() {
        return new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (listWindow == null) {
                    // 播放文件列表窗口
                    listWindow = new JFrame();
                    listWindow.add(listContent);
                    listWindow.setUndecorated(true);
                    // 设置透明度
                    listWindow.setOpacity(0.8f);
                    setListWindowBounds();
                    listWindow.setVisible(true);
                    return;
                }
                int x = getX();
                int width = getWidth();
                if (WINDOW_X != x || WINDOW_WIDTH != width) {
                    setListWindowBounds();
                }
                boolean visible = listWindow.isVisible();
                if (visible) {
                    listWindow.setVisible(false);
                } else {
                    listWindow.setVisible(true);
                }
            }
        };
    }

    // 清空列表
    private MouseAdapter mouseClickedClearWindow() {
        return new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                getMediaPlayer().stop();
                setWindowTitle();
                pauseButton.setLabel("播放");
                listContent.setText("");
                videos.clear();
                setProgress(0, 0);
            }
        };
    }

    // 设置边界
    private void setListWindowBounds() {
        if (listWindow != null) {
            listWindow.setBounds(getWidth() + getX() - LIST_WINDOW_WIDTH - 6, getY() + 37,
                    LIST_WINDOW_WIDTH - 8, getHeight() - 100);
        }
    }

    // 选择文件按钮
    private MouseAdapter mouseClickedChooseFiles() {
        return new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                fileDialog.setVisible(true);
                File[] files = fileDialog.getFiles();
                listContent.setText(listContent.getText());
                for (File file : files) {
                    videos.add(file.getAbsolutePath());
                    listContent.append(videos.size() + "." + file.getName() + "\n");

                }
                videos.sort(Comparator.naturalOrder());
                if (getMediaPlayer().isPlaying()) {
                    return;
                }
                initPlay();
            }
        };
    }

    private void setVolumeLabel(int value) {
        volumeLabel.setText(value + "%");
    }

    private MouseAdapter mouseClickedSetVolumeValue() {
        return new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                setVolume(e.getX());
            }
        };
    }

    private void setVolume(int value) {
        if (value < 0) {
            value = 0;
        } else if (value > 100) {
            value = 100;
        }
        if (volumeProgress.getValue() == value) {
            return;
        }
        volumeProgress.setValue(value);
        setVolumeLabel(value);
        getMediaPlayer().setVolume(value);
    }

    // 初始化播放
    private void initPlay() {
        if (videos.isEmpty()) {
            return;
        }
        getMediaPlayer().playMedia(videos.get(videoIndex));
        setWindowTitle();
        pauseButton.setLabel("暂停");
        setProgress(getMediaPlayer().getTime(), getMediaPlayer().getLength());
        progressTimer.start();
        continueTimer.start();
        this.firstPlay = false;
    }

    private void setWindowTitle() {
        String title = getMediaPlayer().getMediaMeta().getTitle();
        setTitle("媒体播放器-正在播放:" + title);
    }

    private void setWindowTitle1() {
        String title = getMediaPlayer().getMediaMeta().getTitle();
        setTitle("媒体播放器-暂停播放:" + title);
    }

    private String getTimeString(long curr, long total) {
        return formatSecond2Time(curr) + " / " + formatSecond2Time(total);
    }

    private String getTimeString() {
        setTotalTime();
        return formatSecond2Time(getMediaPlayer().getTime()) + " / " + TOTAL_TIME;
    }

    private void setTotalTime() {
        if (TOTAL_TIME == null) {
            long totalSecond = getMediaPlayer().getLength();
            TOTAL_TIME = formatSecond2Time(totalSecond);
        }
    }

    private String formatSecond2Time(long milliseconds) {
        int second = (int) (milliseconds / 1000);
        int h = second / 3600;
        int m = (second % 3600) / 60;
        int s = (second % 3600) % 60;
        return String.format("%02d", h) + ":" + String.format("%02d", m) + ":"
                + String.format("%02d", s);
    }

    private Timer getContinueTimer() {
        return new Timer(1000, new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                long total = getMediaPlayer().getLength();
                long curr = getMediaPlayer().getTime();
                if (curr == total) {
                    videoIndex++;
                    if (videoIndex >= videos.size()) {
                        continueTimer.stop();
                        System.out.println("所有视频播放完毕");
                        return;
                    }
                    getMediaPlayer().playMedia(videos.get(videoIndex));
                    setWindowTitle();
                    setProgress(getMediaPlayer().getTime(), getMediaPlayer().getLength());
                    progressTimer.restart();
                }
            }
        });
    }

    private Timer getProgressTimer() {
        return new Timer(1000, new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // 设置进度值
                setProgress(getMediaPlayer().getTime(), getMediaPlayer().getLength());
            }
        });
    }

    private void setProgress(long curr, long total) {
        float percent = (float) curr / total;
        int value = (int) (percent * 100);
        getProgress().setValue(value);
        displayTime.setText(getTimeString(curr, total));
    }

    private Dimension getNewDimension() {
        return new Dimension(getWidth(), PROGRESS_HEIGHT);
    }

    private MouseAdapter setVideoPlayPoint() {
        return new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                int x = e.getX();
                long total = getMediaPlayer().getLength();
                long time = (long) ((float) x / progress.getWidth() * total);
                setProgress(time, total);
                getMediaPlayer().setTime(time);
            }
        };
    }

    private MouseListener mouseClickedResetVideo() {
        return new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                getMediaPlayer().setTime(0);
            }
        };
    }

    private MouseAdapter mouseClickedBackward() {
        return new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                getMediaPlayer().setTime(getMediaPlayer().getTime() - 5000);
            }
        };
    }

    private MouseAdapter mouseClickedForward() {
        return new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                getMediaPlayer().setTime(getMediaPlayer().getTime() + 5000);
            }
        };
    }

    private MouseAdapter mouseClickedFastForward() {
        return new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (speed >= 3.0f) {
                    speed = 1.0f;
                } else {
                    speed += 0.5f;
                }
                getMediaPlayer().setRate(speed);
                displaySpeed.setText("x" + speed);
            }
        };
    }

    private MouseAdapter mouseClickedMediaPause() {
        return new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (videos.isEmpty()) {
                    return;
                }
                if (firstPlay) {
                    initPlay();
                    return;
                }
                setMediaStatusAndPauseButton();
                if (progressTimer.isRunning()) {
                    progressTimer.stop();
                } else {
                    progressTimer.restart();
                }
            }
        };
    }

    private void setMediaStatusAndPauseButton() {
        if (getMediaPlayer().isPlaying()) {
            getMediaPlayer().pause();
            setWindowTitle();
            pauseButton.setLabel("播放");
        } else {
            getMediaPlayer().play();
            pauseButton.setLabel("暂停");
            setWindowTitle1();
        }
    }

    private JProgressBar getProgress() {
        return progress;
    }

    private EmbeddedMediaPlayer getMediaPlayer() {
        return mediaPlayerComponent.getMediaPlayer();
    }

}
