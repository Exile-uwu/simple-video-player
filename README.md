# simple-video-player

## 项目框架

### 批处理命令

runner.bat 运行项目

### java代码

1. Main.java 程序入口：调用dll库，加载Window类；
2. Window.java 窗口生成：初始化窗口，调用FileUtils获取文件，调用vlc播放媒体，布置按钮以及按钮功能函数；
3. utils.FileUtils 文件处理：获取选择的文件，进行处理，传递给Window；

### 媒体工具

4. vlc 媒体播放器；

### 配置文件

5. pom.xml maven配置；
6. .git git配置

### 构建文件

6. simple-video-player.jar 可执行jar文件；
7. target 构建文件；
