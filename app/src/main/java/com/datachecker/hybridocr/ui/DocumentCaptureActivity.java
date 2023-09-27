package com.datachecker.hybridocr.ui;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.webkit.ConsoleMessage;
import android.webkit.JavascriptInterface;
import android.webkit.PermissionRequest;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
import android.webkit.WebSettings;
import android.webkit.WebView;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.webkit.WebViewAssetLoader;
import androidx.webkit.WebViewClientCompat;

import com.datachecker.hybridocr.model.ImagesModel;
import com.datachecker.hybridocr.R;
import com.datachecker.hybridocrlib.HybridOCRLib;
import com.google.gson.Gson;

import org.jmrtd.lds.icao.MRZInfo;
import org.json.JSONException;



public class DocumentCaptureActivity extends AppCompatActivity {
    private static final int MY_CAMERA_REQUEST_CODE = 100;


    private WebView webView;


    private WebView toEvalutateWebView;
    private boolean isRunning = false;

    class JsObject {
        @JavascriptInterface
        public void postMessage(String data) {
            Log.i("JsObject", "postMessage data="+data);
            //handle data here
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_document_capture);
        webView = findViewById(R.id.dc_webview);
        WebSettings webSettings = webView.getSettings();
        webSettings.setJavaScriptEnabled(true);
        webSettings.setMediaPlaybackRequiresUserGesture(false);

        final WebViewAssetLoader assetLoader = new WebViewAssetLoader.Builder()
                .addPathHandler("/assets/", new WebViewAssetLoader.AssetsPathHandler(this))
                .addPathHandler("/res/", new WebViewAssetLoader.ResourcesPathHandler(this))
                .build();

        webView.setWebViewClient(new LocalContentWebViewClient(assetLoader));
        webView.addJavascriptInterface(new OutputListener() , "imageMessageHandler");
        webView.addJavascriptInterface(new ExitListener() , "exitMessageHandler");
        webView.addJavascriptInterface(new OnImageListener() , "onImageMessageHandler");

        webView.setWebChromeClient(new WebChromeClient() {
            @Override
            public boolean onConsoleMessage(ConsoleMessage consoleMessage) {
                String message = consoleMessage.message();
                String sourceId = consoleMessage.sourceId();
                int lineNumber = consoleMessage.lineNumber();
                Log.d("JavaScriptLog", message + " -- From line " + lineNumber + " of " + sourceId);
                return true;
            }

            @Override
            public void onPermissionRequest(final PermissionRequest request) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    request.grant(request.getResources());
                }
            }
        });
        webView.loadUrl("https://appassets.androidplatform.net/assets/index.html");
    }

    private class LocalContentWebViewClient extends WebViewClientCompat {

        private final WebViewAssetLoader mAssetLoader;

        LocalContentWebViewClient(WebViewAssetLoader assetLoader) {
            mAssetLoader = assetLoader;
        }

        @Override
        public void onPageFinished(WebView view, String url) {
            super.onPageFinished(view, url);

           pageLoadFinished(view);

        }

        @Override
        @RequiresApi(21)
        public WebResourceResponse shouldInterceptRequest(WebView view,
                                                          WebResourceRequest request) {
            return mAssetLoader.shouldInterceptRequest(request.getUrl());
        }
        @Override
        @SuppressWarnings("deprecation") // to support API < 21
        public WebResourceResponse shouldInterceptRequest(WebView view,
                                                          String url) {
            return mAssetLoader.shouldInterceptRequest(Uri.parse(url));
        }
    }
    private void pageLoadFinished(WebView view) {
        if (ContextCompat.checkSelfPermission(DocumentCaptureActivity.this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                requestPermissions(new String[]{Manifest.permission.CAMERA}, MY_CAMERA_REQUEST_CODE);
            }
        }else  {
            String modelUrl = "https://appassets.androidplatform.net/assets/models/";
            String script = "\"\"\n" +
                    "        window.AC = new Autocapture();\n" +
                    "        window.AC.init({\n" +
                    "            CONTAINER_ID: 'AC_mount',\n" +
                    "            LANGUAGE: 'en',\n" +
                    "            MRZ: true,\n" +
                    "            CHECK_TOTAL:5,\n" +
                    "            CROP_CARD: true,\n" +
                    "            MRZ_SETTINGS: {\n" +
                    "                MRZ_RETRIES: -1,\n" +
                    "                FLIP: true,\n" +
                    "                FLIP_EXCEPTION: ['P'],\n" +
                    "                MIN_VALID_SCORE: 50,\n" +
                    "                OCR: false\n" +
                    "            },\n" +
                    "            MODELS_PATH:\"" + modelUrl + "\",\n" +
                    "            onComplete: function (data) {\n" +
                    "                imageMessageHandler.postMessage(JSON.stringify(data));\n" +
                    "                window.AC.stop()\n" +
                    "            },\n" +
                    "            onImage: function (data) {\n" +
                    "                onImageMessageHandler.postMessage(JSON.stringify(data));\n" +
                    "            },\n" +
                    "            onError: function(error) {\n" +
                    "                console.log(error)\n" +
                    "                exitMessageHandler.postMessage(error);\n" +
                    "            },\n" +
                    "            onUserExit: function (error) {\n" +
                    "                exitMessageHandler.postMessage(error);\n" +
                    "             }\n" +
                    "          });\n" +
                    "        \"\"";

            Log.d("JavaScript SCRIPT", script);
            toEvalutateWebView = view;
            if (webView == view) {
                Log.d("EQUAL_WEBVIEW", "Both EQUAL_WEBVIEW");
            }
            webView.evaluateJavascript(script, new ValueCallback<String>() {
                @Override
                public void onReceiveValue(String s) {
                    Log.d("JavaScriptLog", s);
                }
            });
        }

    }
    public class OutputListener {
        @JavascriptInterface
        public void postMessage(String message) throws JSONException {
            Gson gson = new Gson();
            Log.d("MRZ_OUTPUT", message);
            ImagesModel imagesModel = gson.fromJson(message, ImagesModel.class);

            Intent returnIntent = new Intent();
            setResult(Activity.RESULT_OK, returnIntent);
            finish();
        }
    }

    public class ExitListener {
        @JavascriptInterface
        public void postMessage(String message) {
            Log.d("ExitListener" , message);
            finish();
        }
    }

    public class OnImageListener implements HybridOCRLib.DCOCRResultListener {
        @JavascriptInterface
        public void postMessage(String message) {
            if (!isRunning) {
                isRunning = true;
                Log.d("OnImageListener", message);
                HybridOCRLib hybridOCRLib = new HybridOCRLib();
                hybridOCRLib.scanImage(message, this);
            }
        }

        @Override
        public void onSuccessMRZScan(MRZInfo mrzInfo, String mrzLines) {
            isRunning = false;
            String script = "window.AC.parse_mrz(\"" + mrzLines + "\");" +
                    " console.log('evaluated script')";
            Log.d("JS CODE", script);
            runOnUiThread(() -> webView.evaluateJavascript(script, s -> Log.d("onReceiveValue" , s)));
        }

        @Override
        public void onFailure(Exception error) {
            isRunning = false;
        }
    }

}