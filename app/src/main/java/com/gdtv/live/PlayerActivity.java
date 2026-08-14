package com.gdtv.live;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.WindowManager;
import android.widget.MediaController;
import android.widget.Toast;
import android.widget.VideoView;

/**
 * 备用播放器：直接用 VideoView 播放 m3u8 直播流
 * 当 WebView 方式不可用时可跳转至此
 */
public class PlayerActivity extends Activity {

    // 与 MainActivity 相同的频道列表，但使用 m3u8 直链
    private static final String[][] CHANNELS = {
            {"广东卫视",  "http://web.timetv.cn/live03/gdtv.m3u8?channel=gdws"},
            {"广东珠江",  "http://web.timetv.cn/live03/gdtv.m3u8?channel=gdzj"},
            {"广东新闻",  "http://web.timetv.cn/live03/gdtv.m3u8?channel=gdxw"},
            {"大湾区卫视","http://web.timetv.cn/live03/gdtv.m3u8?channel=gdwa"},
            {"广东体育",  "http://web.timetv.cn/live03/gdtv.m3u8?channel=gdty"},
            {"广东民生",  "http://web.timetv.cn/live03/gdtv.m3u8?channel=gdms"},
            {"广东影视",  "http://web.timetv.cn/live03/gdtv.m3u8?channel=gdys"},
            {"广东少儿",  "http://web.timetv.cn/live03/gdtv.m3u8?channel=gdse"},
            {"嘉佳卡通",  "http://web.timetv.cn/live03/gdtv.m3u8?channel=gdkt"},
            {"广东4K",    "http://web.timetv.cn/live03/gdtv.m3u8?channel=gd4k"},
    };

    private VideoView videoView;
    private int currentIndex = 0;
    private boolean controlsVisible = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        videoView = new VideoView(this);
        setContentView(videoView);

        // 从 Intent 获取频道索引
        Intent intent = getIntent();
        currentIndex = intent.getIntExtra("channel_index", 0);

        MediaController mc = new MediaController(this);
        mc.setAnchorView(videoView);
        videoView.setMediaController(mc);

        loadChannel(currentIndex);

        videoView.setOnErrorListener(new android.media.MediaPlayer.OnErrorListener() {
            @Override
            public boolean onError(android.media.MediaPlayer mp, int what, int extra) {
                Toast.makeText(PlayerActivity.this,
                        "播放出错 (what=" + what + ")，尝试下一个源...",
                        Toast.LENGTH_LONG).show();
                // 自动尝试下一个频道
                currentIndex = (currentIndex + 1) % CHANNELS.length;
                loadChannel(currentIndex);
                return true;
            }
        });

        videoView.setOnPreparedListener(new android.media.MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(android.media.MediaPlayer mp) {
                mp.start();
                Toast.makeText(PlayerActivity.this,
                        "正在播放: " + CHANNELS[currentIndex][0],
                        Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadChannel(int index) {
        if (index < 0) index = CHANNELS.length - 1;
        if (index >= CHANNELS.length) index = 0;
        currentIndex = index;

        String url = CHANNELS[index][1];
        videoView.setVideoURI(Uri.parse(url));
        videoView.requestFocus();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        switch (keyCode) {
            case KeyEvent.KEYCODE_DPAD_RIGHT:
            case KeyEvent.KEYCODE_CHANNEL_UP:
                loadChannel(currentIndex + 1);
                return true;

            case KeyEvent.KEYCODE_DPAD_LEFT:
            case KeyEvent.KEYCODE_CHANNEL_DOWN:
                loadChannel(currentIndex - 1);
                return true;

            case KeyEvent.KEYCODE_DPAD_CENTER:
                videoView.start();
                return true;

            case KeyEvent.KEYCODE_BACK:
                finish();
                return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (videoView.isPlaying()) {
            videoView.pause();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        videoView.stopPlayback();
    }
}
