# HybridOCR
Steps for Hybrid OCR.

Before proceeding, first add following dependency in build.gradle file.


allprojects {
repositories {
...
maven { url 'https://jitpack.io' }
}
}

And then add following dependency.


dependencies {
implementation 'com.github.omkardatachecker:HybridOCR:Tag'
}


Before Proceeding to the steps to integrate, you need to do few configurations.

A. In Manifest file, please add following permissions.


    <uses-feature android:name="android.hardware.camera" />
    <uses-permission-sdk-23 android:name="android.permission.CAMERA" />
    <uses-permission android:name="android.permission.INTERNET"/>
    <uses-permission android:name="android.permission.CAMERA" />
    <uses-permission android:name="android.permission.ACCESS_NETWORK_STATE" />

B. Create an 'assets' folder(Go to the app > right-click > New > Folder > Asset Folder and create the asset folder.) and copy the SDK files in that folder.

Now, prepare the Activity to run Javascript SDK to capture document.

1. Request permissions for Camera Access.

2. Declare a webview and, in onCreate() add following configuration for the webview.

```java
WebSettings webSettings = webView.getSettings();
   webSettings.setJavaScriptEnabled(true);
   webSettings.setMediaPlaybackRequiresUserGesture(false);

   final WebViewAssetLoader assetLoader = new WebViewAssetLoader.Builder()
   .addPathHandler("/assets/", new WebViewAssetLoader.AssetsPathHandler(this))
   .addPathHandler("/res/", new WebViewAssetLoader.ResourcesPathHandler(this))
   .build();

   webView.setWebViewClient(new LocalContentWebViewClient(assetLoader));

   webView.addJavascriptInterface("<Add your listner class here>");

           webView.setWebChromeClient(new WebChromeClient() {
            @Override
            public boolean onConsoleMessage(ConsoleMessage consoleMessage) {
                '<For printing logs>''
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
```
  


3. Add a private class extending WebViewClientCompat. as follows.
```java
private class LocalContentWebViewClient extends WebViewClientCompat {

        private final WebViewAssetLoader mAssetLoader;

        LocalContentWebViewClient(WebViewAssetLoader assetLoader) {
            mAssetLoader = assetLoader;
        }

        @Override
        public void onPageStarted(WebView view, String url, Bitmap favicon) {
            super.onPageStarted(view, url, favicon);
            String script = "\"\"\n" +
                    "            function log(emoji, type, args) {\n" +
                    "              window.outPutMessageHandler.postMessage(\n" +
                    "                `${emoji} JS ${type}: ${Object.values(args)\n" +
                    "                  .map(v => typeof(v) === \"undefined\" ? \"undefined\" : typeof(v) === \"object\" ? JSON.stringify(v) : v.toString())\n" +
                    "                  .map(v => v.substring(0, 3000)) // Limit msg to 3000 chars\n" +
                    "                  .join(\", \")}`\n" +
                    "              )\n" +
                    "            }\n" +
                    "        \n" +
                    "            let originalLog = console.log\n" +
                    "            let originalWarn = console.warn\n" +
                    "            let originalError = console.error\n" +
                    "            let originalDebug = console.debug\n" +
                    "        \n" +
                    "            console.log = function() { log(\"\uD83D\uDCD7\", \"log\", arguments); originalLog.apply(null, arguments) }\n" +
                    "            console.warn = function() { log(\"\uD83D\uDCD9\", \"warning\", arguments); originalWarn.apply(null, arguments) }\n" +
                    "            console.error = function() { log(\"\uD83D\uDCD5\", \"error\", arguments); originalError.apply(null, arguments) }\n" +
                    "            console.debug = function() { log(\"\uD83D\uDCD8\", \"debug\", arguments); originalDebug.apply(null, arguments) }\n" +
                    "        \n" +
                    "            window.addEventListener(\"error\", function(e) {\n" +
                    "               log(\"\uD83D\uDCA5\", \"Uncaught\", [`${e.message} at ${e.filename}:${e.lineno}:${e.colno}`])\n" +
                    "            })\n" +
                    "        \"\"";

            view.evaluateJavascript(script, new ValueCallback<String>() {
                @Override
                public void onReceiveValue(String s) {
                    Log.d("JavaScript onPageStart", s);
                }
            });
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

```


4. Now load following javascript method in activity class which will run on finishing page load.(Refer following code snippet.)
```java
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
                    "            MRZ: false,\n" +
                    "            CHECK_TOTAL:5,\n" +
                    "            CROP_CARD: true,\n" +
                    "            MRZ_SETTINGS: {\n" +
                    "                MRZ_RETRIES: 50,\n" +
                    "                FLIP: true,\n" +
                    "                FLIP_EXCEPTION: ['P'],\n" +
                    "                MIN_VALID_SCORE: 50,\n" +
                    "                OCR: false\n" +
                    "            },\n" +
                    "            MODELS_PATH:\"" + modelUrl + "\",\n" +
                    "            onComplete: function (data) {\n" +
                    "                  console.log(data);\n" +
                    "                imageMessageHandler.postMessage(JSON.stringify(data));\n" +
                    "                window.AC.stop()\n" +
                    "            },\n" +
                    "            onError: function(error) {\n" +
                    "                console.log(error)\n" +
                    "                window.AC.stop();\n" +
                    "                window.AC.alert(error)\n" +
                    "            },\n" +
                    "                  onUserExit: function (data) {\n" +
                    "                           window.webkit.messageHandlers.exit.postMessage(data);\n" +
                    "                       }\n" +
                    "                   });\n" +
                    "        \"\"";

            Log.d("JavaScript SCRIPT", script);

            view.evaluateJavascript(script, new ValueCallback<String>() {
                @Override
                public void onReceiveValue(String s) {
                    Log.d("JavaScriptLog", s);
                }
            });
        }

   }
```
       

5. To receive the captured image from above javascript code, write a listner as follwos.
```java
 public class ImageListner{
   @JavascriptInterface
   public void postMessage(String message) {
   //Here you will receive the JSON containing the the images in bytes format.
   }
   }
```
  



6. To work with hybrid OCR, add following dependency in your apps build.gradle file
```groovy
'implementation 'org.jmrtd:jmrtd:0.7.18'
```
   

7. In your activity class, implement 'DCOCRResultListener' interface. It has two methods as follows.
```java
@Override
   public void onSuccessMRZScan(MRZInfo mrzInfo) {
   // Here you will receive the scanned MRZ data in the object.
   }

   @Override
   public void onFailure(Constants.ERROR_CODE error) {
   // Error if MRZ Scan fails.
   Log.d("Error", error.name());
   }
```
   

8. Once you implement these methods. You need to call following function and pass it the output you received in step no. 5
```java
HybridOCRLib hybridOCRLib = new HybridOCRLib(this, this);
   hybridOCRLib.scanImage(dataString, this);
```
   

In above code snippet for scanImage() method, first parameter is image dataString and other parameter is 'DCOCRResultListener'.

              