package com.gdtv.live;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity {

    private static final String TAG = "GDTVLive";

    // 频道列表 —— 与荔枝网 tvChannelDetail 对应
    private static final String[][] CHANNELS = {
            {"广东卫视",  "https://www.gdtv.cn/tvChannelDetail/43"},
            {"广东珠江",  "https://www.gdtv.cn/tvChannelDetail/44"},
            {"广东新闻",  "https://www.gdtv.cn/tvChannelDetail/45"},
            {"大湾区卫视","https://www.gdtv.cn/tvChannelDetail/46"},
            {"广东体育",  "https://www.gdtv.cn/tvChannelDetail/47"},
            {"广东民生",  "https://www.gdtv.cn/tvChannelDetail/48"},
            {"广东影视",  "https://www.gdtv.cn/tvChannelDetail/53"},
            {"广东少儿",  "https://www.gdtv.cn/tvChannelDetail/54"},
            {"嘉佳卡通",  "https://www.gdtv.cn/tvChannelDetail/66"},
            {"南方购物",  "https://www.gdtv.cn/tvChannelDetail/42"},
            {"岭南戏曲",  "https://www.gdtv.cn/tvChannelDetail/15"},
            {"4K超高清",  "https://www.gdtv.cn/tvChannelDetail/16"},
    };

    private WebView webView;
    private TextView channelInfoView;
    private TextView helpView;
    private FrameLayout overlayLayout;

    private int currentChannelIndex = 0;
    private Handler handler = new Handler();
    private boolean overlayVisible = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // 全屏
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        setContentView(R.layout.activity_main);

        webView = (WebView) findViewById(R.id.webView);
        channelInfoView = (TextView) findViewById(R.id.channelInfo);
        helpView = (TextView) findViewById(R.id.helpText);
        overlayLayout = (FrameLayout) findViewById(R.id.overlayLayout);

        initWebView();
        loadChannel(currentChannelIndex);

        // 5秒后隐藏帮助文字
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                helpView.setVisibility(View.GONE);
            }
        }, 5000);
    }

    private void initWebView() {
        WebSettings ws = webView.getSettings();

        // 兼容 Android 4.4
        ws.setJavaScriptEnabled(true);
        ws.setDomStorageEnabled(true);
        ws.setDatabaseEnabled(true);
        ws.setAppCacheEnabled(true);
        ws.setCacheMode(WebSettings.LOAD_DEFAULT);

        // 媒体播放支持
        ws.setMediaPlaybackRequiresUserGesture(false);
        ws.setAllowFileAccess(true);
        ws.setAllowContentAccess(true);

        // 屏幕适配
        ws.setUseWideViewPort(true);
        ws.setLoadWithOverviewMode(true);
        ws.setSupportZoom(false);
        ws.setBuiltInZoomControls(false);

        // 用户代理 —— 模拟电视/盒子浏览器
        ws.setUserAgentString(
                "Mozilla/5.0 (Linux; Android 4.4.4; TV Build/KTU84P) " +
                "AppleWebKit/537.36 (KHTML, like Gecko) " +
                "Version/4.0 Chrome/33.0.0.0 Safari/537.36");

        // 硬件加速
        webView.setLayerType(View.LAYER_TYPE_HARDWARE, null);

        webView.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                // 在 WebView 内打开所有链接
                view.loadUrl(url);
                return true;
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                Log.d(TAG, "Page loaded: " + url);
                // 注入 JS：自动点击播放按钮、隐藏不必要元素
                injectJS();
            }
        });

        webView.setWebChromeClient(new WebChromeClient() {
            @Override
            public void onProgressChanged(WebView view, int newProgress) {
                super.onProgressChanged(view, newProgress);
            }
        });

        // 让 WebView 可以获取焦点（遥控器导航需要）
        webView.setFocusable(true);
        webView.setFocusableInTouchMode(true);
        webView.requestFocus();
    }

    /**
     * 注入 JS：
     * 1. 自动点击播放按钮
     * 2. 进入全屏
     * 3. 隐藏页面顶部导航等无关元素
     */
    private void injectJS() {
        String js =
                "javascript:(function(){" +
                "  try{" +
                "    var v=document.querySelector('video');" +
                "    if(v){" +
                "      v.muted=false;" +
                "      v.play();" +
                "      v.setAttribute('controls','controls');" +
                "      v.style.width='100%';" +
                "      v.style.height='100%';" +
                "    }" +
                "  }catch(e){}" +
                "  try{" +
                "    var btns=document.querySelectorAll('button');" +
                "    for(var i=0;i<btns.length;i++){" +
                "      var t=btns[i].textContent||'';" +
                "      if(t.indexOf('播放')>=0||t.indexOf('播放')>=0||t.indexOf('▶')>=0||t.indexOf('▶')>=0){" +
                "        btns[i].click();" +
                "      }" +
                "    }" +
                "  }catch(e){}" +
                "  try{" +
                "    var plays=document.querySelectorAll('.play-btn,.playBtn,.vjs-big-play-button,.prism-play-btn');" +
                "    for(var j=0;j<plays.length;j++){plays[j].click();}" +
                "  }catch(e){}" +
                "})();";

        if (android.os.Build.VERSION.SDK_INT >= 19) {
            webView.evaluateJavascript(js, null);
        } else {
            webView.loadUrl(js);
        }
    }

    private void loadChannel(int index) {
        if (index < 0) index = CHANNELS.length - 1;
        if (index >= CHANNELS.length) index = 0;

        currentChannelIndex = index;
        String name = CHANNELS[index][0];
        String url = CHANNELS[index][1];

        showChannelOverlay(name);
        webView.loadUrl(url);

        Toast.makeText(this, "正在切换: " + name, Toast.LENGTH_SHORT).show();
    }

    private void showChannelOverlay(String name) {
        channelInfoView.setText(name);
        overlayLayout.setVisibility(View.VISIBLE);
        overlayVisible = true;

        // 3秒后自动隐藏
        handler.removeCallbacks(hideOverlayRunnable);
        handler.postDelayed(hideOverlayRunnable, 3000);
    }

    private Runnable hideOverlayRunnable = new Runnable() {
        @Override
        public void run() {
            overlayLayout.setVisibility(View.GONE);
            overlayVisible = false;
        }
    };

    // ========== 遥控器按键处理 ==========

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        switch (keyCode) {
            case KeyEvent.KEYCODE_DPAD_RIGHT:
            case KeyEvent.KEYCODE_CHANNEL_UP:
            case KeyEvent.KEYCODE_MEDIA_NEXT:
                // 下一个频道
                loadChannel(currentChannelIndex + 1);
                return true;

            case KeyEvent.KEYCODE_DPAD_LEFT:
            case KeyEvent.KEYCODE_CHANNEL_DOWN:
            case KeyEvent.KEYCODE_MEDIA_PREVIOUS:
                // 上一个频道
                loadChannel(currentChannelIndex - 1);
                return true;

            case KeyEvent.KEYCODE_DPAD_UP:
                // 显示频道列表提示
                showAllChannels();
                return true;

            case KeyEvent.KEYCODE_DPAD_CENTER:
            case KeyEvent.KEYCODE_ENTER:
                // 重新加载当前频道
                webView.reload();
                Toast.makeText(this, "刷新中...", Toast.LENGTH_SHORT).show();
                return true;

            case KeyEvent.KEYCODE_BACK:
                if (overlayVisible) {
                    overlayLayout.setVisibility(View.GONE);
                    overlayVisible = false;
                    return true;
                }
                if (webView.canGoBack()) {
                    webView.goBack();
                    return true;
                }
                // 双击退出
                if (backPressedOnce) {
                    finish();
                    return true;
                }
                backPressedOnce = true;
                Toast.makeText(this, "再按一次退出", Toast.LENGTH_SHORT).show();
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        backPressedOnce = false;
                    }
                }, 2000);
                return true;

            case KeyEvent.KEYCODE_MENU:
                showAllChannels();
                return true;

            default:
                // 数字键选台 1-9
                if (keyCode >= KeyEvent.KEYCODE_1 && keyCode <= KeyEvent.KEYCODE_9) {
                    int num = keyCode - KeyEvent.KEYCODE_1;
                    if (num < CHANNELS.length) {
                        loadChannel(num);
                    }
                    return true;
                }
                if (keyCode == KeyEvent.KEYCODE_0) {
                    loadChannel(9); // 0 键 → 第10个频道
                    return true;
                }
                break;
        }
        return super.onKeyDown(keyCode, event);
    }

    private boolean backPressedOnce = false;

    private void showAllChannels() {
        StringBuilder sb = new StringBuilder("频道列表 (按数字键切换)\n");
        sb.append("─────────────\n");
        for (int i = 0; i < CHANNELS.length; i++) {
            sb.append(i + 1).append(". ").append(CHANNELS[i][0]);
            if (i == currentChannelIndex) sb.append(" ◀ 当前");
            sb.append("\n");
        }
        channelInfoView.setText(sb.toString());
        overlayLayout.setVisibility(View.VISIBLE);
        overlayVisible = true;

        handler.removeCallbacks(hideOverlayRunnable);
        handler.postDelayed(hideOverlayRunnable, 6000);
    }

    @Override
    protected void onPause() {
        super.onPause();
        webView.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        webView.onResume();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (webView != null) {
            webView.stopLoading();
            webView.removeAllViews();
            webView.destroy();
        }
        handler.removeCallbacksAndMessages(null);
    }
}
