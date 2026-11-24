package br.com.biel.fuga;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.FrameLayout;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Collections;

public final class MainActivity extends Activity {
    private static final String HOME = "https://appassets.androidplatform.net/index.html";
    private WebView web;
    private View customView;
    private WebChromeClient.CustomViewCallback customCallback;
    private FrameLayout root;
    private void immersive() {
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY | View.SYSTEM_UI_FLAG_FULLSCREEN | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_LAYOUT_STABLE | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION);
    }
    private void hideCustomView() {
        if (customView == null) return;
        root.removeView(customView); customView = null; web.setVisibility(View.VISIBLE);
        WebChromeClient.CustomViewCallback callback = customCallback; customCallback = null;
        if (callback != null) callback.onCustomViewHidden();
        immersive();
    }
    @Override public void onCreate(Bundle state) {
        super.onCreate(state);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        immersive();
        root = new FrameLayout(this);
        web = new WebView(this);
        root.addView(web, new FrameLayout.LayoutParams(-1, -1));
        setContentView(root);
        WebSettings settings = web.getSettings();
        settings.setJavaScriptEnabled(true);
        settings.setDomStorageEnabled(true);
        settings.setAllowFileAccess(false);
        settings.setAllowContentAccess(false);
        settings.setMediaPlaybackRequiresUserGesture(false);
        web.setLayerType(View.LAYER_TYPE_HARDWARE, null);
        web.setWebViewClient(new WebViewClient() {
            @Override public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
                String url = request.getUrl().toString();
                return !(url.equals(HOME) || url.startsWith(HOME + "#") || url.equals("about:blank") || url.equals("about:srcdoc"));
            }
            @Override public WebResourceResponse shouldInterceptRequest(WebView view, WebResourceRequest request) {
                String scheme = request.getUrl().getScheme();
                if (!"http".equalsIgnoreCase(scheme) && !"https".equalsIgnoreCase(scheme)) return null;
                if ("GET".equals(request.getMethod()) && "https".equalsIgnoreCase(scheme) && "appassets.androidplatform.net".equalsIgnoreCase(request.getUrl().getHost()) && "/index.html".equals(request.getUrl().getPath()) && request.getUrl().getQuery() == null) {
                    try { return new WebResourceResponse("text/html", "UTF-8", getAssets().open("index.html")); } catch (IOException ignored) {}
                }
                return new WebResourceResponse("text/plain", "UTF-8", 403, "Blocked", Collections.emptyMap(), new ByteArrayInputStream(new byte[0]));
            }
        });
        web.setWebChromeClient(new WebChromeClient() {
            @Override public void onShowCustomView(View view, CustomViewCallback callback) {
                if (customView != null) { callback.onCustomViewHidden(); return; }
                customView = view; customCallback = callback;
                root.addView(view, new FrameLayout.LayoutParams(-1, -1));
                web.setVisibility(View.GONE); immersive();
            }
            @Override public void onHideCustomView() { hideCustomView(); }
        });
        web.loadUrl(HOME);
    }
    @Override public void onBackPressed() {
        if (customView != null) { hideCustomView(); }
        else if (web.canGoBack()) { web.goBack(); }
        else { super.onBackPressed(); }
    }
    @Override public void onWindowFocusChanged(boolean focus) { super.onWindowFocusChanged(focus); if (focus) immersive(); }
    @Override protected void onDestroy() {
        if (web != null) { ((ViewGroup) web.getParent()).removeView(web); web.destroy(); }
        super.onDestroy();
    }
}
