# 广东卫视直播 TV App

基于 WebView 的 Android TV 电视直播应用，专为 **Android 4.4 (API 19)** 机顶盒/智能电视打造。

## 功能特性

- ✅ 加载广东广播电视台荔枝网直播页面（WebView 方式）
- ✅ 遥控器按键换台（← → / CH+/CH-）
- ✅ 数字键 1-9 直接选台
- ✅ 上键显示全部频道列表
- ✅ 中键刷新当前频道
- ✅ 返回键退出（双击确认）
- ✅ 自动注入 JS 点击播放、全屏
- ✅ 备用 VideoView 播放器（m3u8 直链）
- ✅ 全屏 + 屏幕常亮
- ✅ 兼容 Android 4.4 旧版 WebView

## 频道列表

| 编号 | 频道名 | 源页面 |
|------|--------|--------|
| 1 | 广东卫视 | tvChannelDetail/43 |
| 2 | 广东珠江 | tvChannelDetail/44 |
| 3 | 广东新闻 | tvChannelDetail/45 |
| 4 | 大湾区卫视 | tvChannelDetail/46 |
| 5 | 广东体育 | tvChannelDetail/47 |
| 6 | 广东民生 | tvChannelDetail/48 |
| 7 | 广东影视 | tvChannelDetail/53 |
| 8 | 广东少儿 | tvChannelDetail/54 |
| 9 | 嘉佳卡通 | tvChannelDetail/66 |
| 0 | 南方购物 | tvChannelDetail/42 |

## 项目结构

```
GDTVLive/
├── build.gradle                    # 项目级 Gradle 配置
├── settings.gradle
├── gradle/wrapper/
│   └── gradle-wrapper.properties  # Gradle 3.5.1 (兼容 4.4)
├── app/
│   ├── build.gradle                # minSdk 19, targetSdk 19
│   ├── proguard-rules.pro
│   └── src/main/
│       ├── AndroidManifest.xml
│       ├── java/com/gdtv/live/
│       │   ├── MainActivity.java   # 主界面 (WebView)
│       │   ├── PlayerActivity.java # 备用播放器 (VideoView)
│       │   └── GDTVLiveApp.java   # Application
│       └── res/
│           ├── layout/
│           │   ├── activity_main.xml
│           │   └── tv_banner.xml
│           ├── drawable/
│           │   └── tv_banner.xml
│           └── values/
│               ├── strings.xml
│               └── styles.xml
```

## 编译方式

### 方式一：Android Studio（推荐）

1. 用 **Android Studio 3.0 ~ 3.2** 打开 `GDTVLive` 文件夹
2. 等待 Gradle 同步完成（会自动下载 Gradle 3.5.1）
3. 连接 Android 4.4 电视/盒子（USB 调试）
4. 点击 ▶ Run 即可安装运行

### 方式二：命令行编译

```bash
cd GDTVLive
gradle wrapper --gradle-version 3.5.1
./gradlew assembleDebug
# APK 输出: app/build/outputs/apk/debug/app-debug.apk
```

### 方式三：直接安装到设备

```bash
adb connect <电视IP>:5555
adb install app/build/outputs/apk/debug/app-debug.apk
```

## 遥控器按键映射

| 按键 | 功能 |
|------|------|
| ← / CH- | 上一个频道 |
| → / CH+ | 下一个频道 |
| ↑ / MENU | 显示频道列表 |
| 中键 / OK | 刷新当前页 |
| 数字键 0-9 | 直接跳转到对应频道 |
| 返回键 (×2) | 退出 App |

## 技术原理

### WebView 加载模式（默认）
- 直接加载 `https://www.gdtv.cn/tvChannelDetail/XX` 页面
- JS 自动注入：查找 video 标签 → 调用 play() → 设置全屏
- 模拟 TV User-Agent 绕过移动端限制
- 兼容 Android 4.4 的 `evaluateJavascript` API

### VideoView 直连模式（备用）
- 使用 `http://web.timetv.cn/live03/gdtv.m3u8?channel=xxx` 源
- Android 4.4 原生支持 HLS (m3u8) 播放
- 播放失败自动切换下一个源

## 注意事项

1. **WebView 版本**：Android 4.4 使用 Chromium 33 内核，部分新特性不支持。
   如果直播页面无法正常播放，可尝试系统设置 → 应用 → 全部 → Android System WebView → 更新。
2. **网络要求**：需电视/盒子连接互联网，且能访问 gdtv.cn 和 timetv.cn
3. **直播源时效**：第三方 m3u8 源可能失效，WebView 方式更稳定（直接加载官网）
4. **仅供学习交流**：直播源版权归广东广播电视台所有

## 兼容性说明

| 系统版本 | WebView 方式 | VideoView 方式 |
|----------|-------------|----------------|
| Android 4.4 | ✅ Chromium 33 | ✅ 原生 HLS |
| Android 5.0+ | ✅ Chromium 37+ | ✅ |
| Android 7.0+ | ✅ | ✅ |
| Android TV | ✅ | ✅ |

## License

仅供个人学习研究使用，不得用于商业用途。
