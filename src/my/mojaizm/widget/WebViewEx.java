package my.mojaizm.widget;

import java.util.Map;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Picture;
import android.net.http.SslError;
import android.os.Build;
import android.os.Message;
import android.util.AttributeSet;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.webkit.ConsoleMessage;
import android.webkit.GeolocationPermissions;
import android.webkit.HttpAuthHandler;
import android.webkit.JsPromptResult;
import android.webkit.JsResult;
import android.webkit.SslErrorHandler;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebSettings.RenderPriority;
import android.webkit.WebStorage;
import android.webkit.WebView;
import android.webkit.WebViewClient;

public class WebViewEx extends WebView {
    private static final String TAG = WebViewEx.class.getSimpleName();
    
    public interface Client {
        public boolean shouldOverrideUrlLoading(WebView webview, String url);
        public boolean onRedirectUrlLoading(WebView webview, String url);
        public void onPageStarted(WebView webview, String url, Bitmap favicon);
        public void onPageFinished(WebView webview, String url);
        public void onPageHalfFinished(WebView webview);
        public void onReceivedError(WebView webview, int errorCode, String errormsg, String failingUrl);
        
        public void onNewPicture(WebView webview, Picture picture);
        public boolean onJsAlert(WebView webview, String url, String message, android.webkit.JsResult result);
    }
    
    private int mProgress;
    private boolean mIsHalfFinished;
    private WebViewClient mWebViewClient;
    private WebChromeClient mWebChromeClient;
    private WebViewEx.Client mWebViewExClient;
    
    private void init(Context context) {
        mProgress = 0;
        mIsHalfFinished = false;
        
        //--------------------------------------------->
        // dummy
        mWebViewClient = new WebViewClient();
        mWebChromeClient = new WebChromeClient();
        mWebViewExClient = new WebViewEx.Client() {
            
            @Override
            public boolean shouldOverrideUrlLoading(WebView webview, String url) {
                return false;
            }
            
            @Override
            public boolean onRedirectUrlLoading(WebView webview, String url) {
                return false;
            }
            
            @Override
            public void onPageStarted(WebView webview, String url, Bitmap favicon) {}

            @Override
            public void onPageHalfFinished(WebView webview) {}

            @Override
            public void onPageFinished(WebView webview, String url) {}
            
            @Override
            public void onNewPicture(WebView webview, Picture picture) {}

            @Override
            public void onReceivedError(WebView webview, int errorCode, String errormsg, String failingUrl) {}

            @Override
            public boolean onJsAlert(WebView webview, String url, String message, JsResult result) {
                return false;
            }
        };
        //<---------------------------------------------
        super.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView webview, String url) {
                if (mWebViewClient.shouldOverrideUrlLoading(webview, url)) {
                    return true;
                }
                if (mProgress > 0 && !mIsHalfFinished && url.startsWith("http")) {
                    return mWebViewExClient.onRedirectUrlLoading(webview, url);
                }
                return mWebViewExClient.shouldOverrideUrlLoading(webview, url);
            }
            
            @Override
            public void onPageStarted(WebView webview, String url, Bitmap favicon) {
                mWebViewClient.onPageStarted(webview, url, favicon);
                mWebViewExClient.onPageStarted(webview, url, favicon);
            }
            
            @Override
            public void onPageFinished(WebView webview, String url) {
                mWebViewClient.onPageFinished(webview, url);
                mWebViewExClient.onPageFinished(webview, url);
            }
            
            @Override
            public void onReceivedError(WebView webview, int errorCode, String errormsg, String failingUrl) {
                mWebViewClient.onReceivedError(webview, errorCode, errormsg, failingUrl);
                mWebViewExClient.onReceivedError(webview, errorCode, errormsg, failingUrl);
            }
            
            @Override
            public void onLoadResource(WebView view, String url) {
                mWebViewClient.onLoadResource(view, url);
            }

            @Override
            public void onFormResubmission(WebView view, Message dontResend, Message resend) {
                super.onFormResubmission(view, dontResend, resend);
                mWebViewClient.onFormResubmission(view, dontResend, resend);
            }

            @Override
            public void doUpdateVisitedHistory(WebView view, String url, boolean isReload) {
                mWebViewClient.doUpdateVisitedHistory(view, url, isReload);
            }

            @Override
            public void onReceivedSslError(WebView view, SslErrorHandler handler, SslError error) {
                super.onReceivedSslError(view, handler, error);
                mWebViewClient.onReceivedSslError(view, handler, error);
            }

            @Override
            public void onReceivedHttpAuthRequest(WebView view, HttpAuthHandler handler, String host, String realm) {
                super.onReceivedHttpAuthRequest(view, handler, host, realm);
                mWebViewClient.onReceivedHttpAuthRequest(view, handler, host, realm);
            }

            @Override
            public boolean shouldOverrideKeyEvent(WebView view, KeyEvent event) {
                mWebViewClient.shouldOverrideKeyEvent(view, event);
                return false;
            }

            @Override
            public void onUnhandledKeyEvent(WebView view, KeyEvent event) {
                mWebViewClient.onUnhandledKeyEvent(view, event);
            }

            @Override
            public void onScaleChanged(WebView view, float oldScale, float newScale) {
                mWebViewClient.onScaleChanged(view, oldScale, newScale);
            }
        });
        
        
        super.setWebChromeClient(new WebChromeClient() {
            @Override
            public void onProgressChanged(WebView webview, int progress) {
                mProgress = progress;
                mWebChromeClient.onProgressChanged(webview, progress);
                
                if (mProgress >= 40 && !mIsHalfFinished) {
                    mIsHalfFinished = true;
                    mWebViewExClient.onPageHalfFinished(webview);
                }
            }

            @Override
            public boolean onJsAlert(WebView webview, String url, String message, android.webkit.JsResult result) {
                if (mWebChromeClient.onJsAlert(webview, url, message, result)) {
                    return true;
                }
                return mWebViewExClient.onJsAlert(webview, url, message, result);
            }
            
            @Override
            public boolean onJsConfirm(WebView view, String url, String message, JsResult result) {
                return mWebChromeClient.onJsConfirm(view, url, message, result);
            }
            
            @Override
            public boolean onJsPrompt(WebView view, String url, String message, String defaultValue, JsPromptResult result) {
                return mWebChromeClient.onJsPrompt(view, url, message, defaultValue, result);
            }

            @Override
            public boolean onJsBeforeUnload(WebView view, String url, String message, JsResult result) {
                return mWebChromeClient.onJsBeforeUnload(view, url, message, result);
            }
            
            @Override
            public boolean onJsTimeout() {
                return mWebChromeClient.onJsTimeout();
            }

            @Override
            public void onReceivedTitle(WebView view, String title) {
                mWebChromeClient.onReceivedTitle(view, title);
            }

            @Override
            public void onReceivedIcon(WebView view, Bitmap icon) {
                mWebChromeClient.onReceivedIcon(view, icon);
            }

            @Override
            public void onReceivedTouchIconUrl(WebView view, String url, boolean precomposed) {
                mWebChromeClient.onReceivedTouchIconUrl(view, url, precomposed);
            }

            @Override
            public void onShowCustomView(View view, CustomViewCallback callback) {
                mWebChromeClient.onShowCustomView(view, callback);
            }

            @Override
            public void onHideCustomView() {
                mWebChromeClient.onHideCustomView();
            }

            @Override
            public boolean onCreateWindow(WebView view, boolean dialog, boolean userGesture, Message resultMsg) {
                return mWebChromeClient.onCreateWindow(view, dialog, userGesture, resultMsg);
            }

            @Override
            public void onRequestFocus(WebView view) {
                mWebChromeClient.onRequestFocus(view);
            }

            @Override
            public void onCloseWindow(WebView window) {
                mWebChromeClient.onCloseWindow(window);
            }

            @Override
            public void onExceededDatabaseQuota(String url, String databaseIdentifier,
                long currentQuota, long estimatedSize, long totalUsedQuota, WebStorage.QuotaUpdater quotaUpdater) {
                
                super.onExceededDatabaseQuota(url, databaseIdentifier, currentQuota, estimatedSize, totalUsedQuota, quotaUpdater);
                mWebChromeClient.onExceededDatabaseQuota(url, databaseIdentifier, currentQuota, estimatedSize, totalUsedQuota, quotaUpdater);
            }

            @Override
            public void onReachedMaxAppCacheSize(long spaceNeeded, long totalUsedQuota, WebStorage.QuotaUpdater quotaUpdater) {
                super.onReachedMaxAppCacheSize(spaceNeeded, totalUsedQuota, quotaUpdater);
                mWebChromeClient.onReachedMaxAppCacheSize(spaceNeeded, totalUsedQuota, quotaUpdater);
            }

            @Override
            public void onGeolocationPermissionsShowPrompt(String origin, GeolocationPermissions.Callback callback) {
                mWebChromeClient.onGeolocationPermissionsShowPrompt(origin, callback);
            }

            @Override
            public void onGeolocationPermissionsHidePrompt() {
                mWebChromeClient.onGeolocationPermissionsHidePrompt();
            }

            @Override
            public boolean onConsoleMessage(ConsoleMessage consoleMessage) {
                // Call the old version of this function for backwards compatability.
                super.onConsoleMessage(consoleMessage.message(), consoleMessage.lineNumber(), consoleMessage.sourceId());
                return mWebChromeClient.onConsoleMessage(consoleMessage);
            }
        });
        
        
        super.setPictureListener(new PictureListener() {
            @Override
            public void onNewPicture(WebView webview, Picture picture) {
                mWebViewExClient.onNewPicture(webview, picture);
            }
        });
        
        //##################################################################
        // sample setting
        //##################################################################
        setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                    case MotionEvent.ACTION_UP:
                        if (!v.hasFocus()) {
                            v.requestFocus();
                        }
                        break;
                }
                return false;
            }
        });

        setDuplicateParentStateEnabled(true);
        setSaveEnabled(true);
        setLongClickable(true);
        setHapticFeedbackEnabled(true);
        setScrollbarFadingEnabled(true);
        setScrollBarStyle(View.SCROLLBARS_INSIDE_INSET);
        setVerticalFadingEdgeEnabled(false);

        WebSettings ws = getSettings();
        ws.setAppCacheEnabled(true);
        ws.setAppCacheMaxSize(8388608);
        ws.setCacheMode(WebSettings.LOAD_CACHE_ELSE_NETWORK);
        ws.setLoadWithOverviewMode(true);
        ws.setJavaScriptEnabled(true);
        ws.setDomStorageEnabled(true);
        ws.setPluginsEnabled(false);
        ws.setDefaultTextEncodingName("UTF-8");
        ws.setLightTouchEnabled(true);
        ws.setRenderPriority(RenderPriority.HIGH);
        if (!Build.VERSION.RELEASE.startsWith("2.1")) {
            // 2.1でこれをセットするとタッチイベントが発生しない不具合
            ws.setUseWideViewPort(true);
        }
        setScrollBarStyle(View.SCROLLBARS_INSIDE_OVERLAY);
        
        setClickable(true);
        requestFocus(View.FOCUS_DOWN);
    }

    public WebViewEx(Context context) {
        super(context);
        init(context);
    }
    
    public WebViewEx(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }
    
    public WebViewEx(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(context);
    }
    
    public void resume() {
        try {
            WebView.class.getMethod("onResume").invoke(this);
        } catch (Exception e) {
        }
    }

    
    public void pause() {
        try {
            WebView.class.getMethod("onPause").invoke(this);
        } catch (Exception e) {
        }
    }
   
    @Override
    public void scrollTo(int x, int y) {
        // scroll幅が実際のページよりはみ出した時、画面が真っ白になるので調整する
        int calc = (int)(getContentHeight() * getScale() - getHeight());
        if (calc < 0) {
            calc = 0;
        }
        int scl_y = y;
        if (scl_y > calc) {
            scl_y = calc;
        }
        super.scrollTo(x, scl_y);
    }
    
    @Override
    public void setWebViewClient(WebViewClient client) {
        mWebViewClient = client;
    }

    public WebViewClient getWebViewClient() {
        return mWebViewClient;
    }
    
    @Override
    public void setWebChromeClient(WebChromeClient client) {
        mWebChromeClient = client;
    }
    
    public WebChromeClient getWebChromeClient() {
        return mWebChromeClient;
    }
    
    @Override
    public void setPictureListener(PictureListener listener) {
        throw new RuntimeException("Don'nt use PictureListener, Please use WebViewEx.Client.");
    }
    
    
    public void setWebViewExClient(WebViewEx.Client client) {
        mWebViewExClient = client;
    }
    
    @Override
    public void loadUrl(String ustr) {
        if (ustr.indexOf("javascript:") < 0) {
            mProgress = 0;
            mIsHalfFinished = false;
        }
        super.loadUrl(ustr);
    }
    
    @Override
    public void loadUrl(String ustr, Map<String, String>headers) {
        if (ustr.indexOf("javascript:") < 0) {
            mProgress = 0;
            mIsHalfFinished = false;
        }
        super.loadUrl(ustr, headers);
    }
    
    @Override
    public void loadData(String data, String mimeType, String encoding) {
        mProgress = 0;
        mIsHalfFinished = false;
        super.loadData(data, mimeType, encoding);
    }
    
    @Override
    public void loadDataWithBaseURL(String baseUrl, String data, String mimeType, String encoding, String historyUrl) {
        mProgress = 0;
        mIsHalfFinished = false;
        super.loadDataWithBaseURL(baseUrl, data, mimeType, encoding, historyUrl);
    }
    
    @Override
    public void goBack() {
        mProgress = 0;
        mIsHalfFinished = false;
        super.goBack();
    }
    
    @Override
    public void goForward() {
        mProgress = 0;
        mIsHalfFinished = false;
        super.goForward();
    }
    
    @Override
    public void stopLoading() {
        // ------------------------>
        // flash止める処理 ??
        try {
            WebView.class.getMethod("onPause").invoke(this);
        } catch (Exception e) {
        }
        try {
            WebView.class.getMethod("onResume").invoke(this);
        } catch (Exception e) {
        }
        // <------------------------
        super.stopLoading();
    }
    
    public int progress() {
        return mProgress;
    }
}
