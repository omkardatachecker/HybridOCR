# Android Integration Guide for HybridOCR

This documentation is an extension of the [AutoCapture SDK](https://github.com/datacheckerbv/AutoCapture). 
It enables AutoCapture with an external MRZ engine (HybridOCR) that it can use to scan the MRZ of a document. 
Before continuing, please make sure to have read the [AutoCapture SDK Documentation](https://github.com/datacheckerbv/AutoCapture).

Steps for Hybrid OCR.

## Prerequisites

Before you begin, ensure you have the following:

- Android Studio installed on your development machine.
- The [AutoCapture repository](https://github.com/datacheckerbv/AutoCapture).
- The [HybridOCR Android SDK](https://github.com/datacheckerbv/HybridOCR).

## Set Dependencies.
Before proceeding, first add following dependency in `settings.gradle` file.

```groovy
allprojects {
    repositories {
        maven { 
            url 'https://jitpack.io' 
        }
    }
}
```

And then add following dependency in app level `build.gradle`.

```groovy
dependencies {
    implementation 'com.github.omkardatachecker:HybridOCR:<Tag>'
    implementation 'org.jmrtd:jmrtd:0.7.18'
    implementation 'com.google.code.gson:gson:2.10.1'

}
```

## **Set up listeners**

### **OnImage listener**

Define a JavaScript interface class to receive Image from the AutoCapture SDK. This listener will be called `onImage` and is defined within the AutoCapture configuration.

Step 1: Add 'isRunning' flag in the your activity add below in the global scope.

```java
    private boolean isRunning = false;
```

Step 2: Define the OnImageListener class.

```java
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
```

### **Output listener**
Note: This is an extension of [AutoCapture repository Documentation](https://github.com/datacheckerbv/AutoCapture).
Here we will define the 'outputListener'. 

This outputListener will capture the output including MRZ information.

```java
public class OutputListener {
        @JavascriptInterface
        public void postMessage(String message) throws JSONException {
            Gson gson = new Gson();
            Log.d("MRZ_OUTPUT", message);
            ImagesModel imagesModel = gson.fromJson(message, ImagesModel.class);
        }
    }
```

## **Loading AutoCapture SDK**

Note: This is an extension of [AutoCapture repository Documentation](https://github.com/datacheckerbv/AutoCapture).
Here we will configure the [onImageListener](#onimage-listener). 

Before continuing, please make sure to read the [Configuration](../README.md#configuration) documentation.

Now, we will configure the AutoCapture SDK and start it. An example is given below. In this example, the configuration requires two additional variables: `modelURL`, `TOKEN`.

First, `modelURL` indicates the location where the SDK models can be found. As explained in [1.4](#14-setting-up-the-assets-folder) this will be in the created `assets` folder. The `modelURL` will than be: `https://appassets.androidplatform.net/assets/<PATH TO AUTOCAPTURE FOLDER>/html/models/`

Second, `TOKEN` needs to be replaced by a valid [SDK Token](../README.md#sdk-token).

As can be seen in the example below, the `listeners` from [2.1](#21-set-up-listeners) are used within the `onComplete`, `onError` and `onUserExit` callback functions to handle the data.

Note: Within `MRZ_SETTINGS` the setting `OCR` is `false`, please refer to [2.3](#23-hybrid-ocr) and [External MRZ](../README.md/#external-mrz).

```java
private void startAutoCaptureConfig(WebView view) {
    // Requesting Camera Access Permissions
    if (ContextCompat.checkSelfPermission(DocumentCaptureActivity.this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            requestPermissions(new String[]{Manifest.permission.CAMERA}, MY_CAMERA_REQUEST_CODE);
        }
    }else  {
        String modelUrl = "https://appassets.androidplatform.net/assets/<PATH TO AUTOCAPTURE FOLDER>/html/models/";
        
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
        "            TOKEN: \"" + <YOUR SDK TOKEN> + "\",\n" +
        "            onComplete: function (data) {\n" +
        "                outputListenerHandler.postMessage(JSON.stringify(data));\n" +
        "            },\n" +
        "            onImage: function (data) {\n" +
        "                onImageMessageHandler.postMessage(JSON.stringify(data));\n" +
        "            },\n" +
        "            onError: function(error) {\n" +
        "                exitMessageHandler.postMessage(error);\n" +
        "            },\n" +
        "            onUserExit: function (error) {\n" +
        "                exitMessageHandler.postMessage(error);\n" +
        "             }\n" +
        "          });\n" +
        "        \"\"";

        Log.d("JavaScript SCRIPT", script);

        webView.evaluateJavascript(script, new ValueCallback<String>() {
            @Override
            public void onReceiveValue(String s) {
                Log.d("JavaScriptLog", s);
            }
        });
    }
}
```

## **Configuring WebView**

Note: This is an extension of [AutoCapture repository Documentation](https://github.com/datacheckerbv/AutoCapture).
Here we will set the [onImageListener](#onimage-listener) and the [outputListener](#output-listener).

Inside the `onCreate()` method of your activity, configure the `WebView` to load and display your web app. This involves enabling JavaScript, setting up the asset loader, and configuring various settings.

```java
// Create WebView
webView = findViewById(<YOUR ID>);

WebSettings webSettings = webView.getSettings();
webSettings.setJavaScriptEnabled(true);
webSettings.setMediaPlaybackRequiresUserGesture(false);

// Configure asset loader
final WebViewAssetLoader assetLoader = new WebViewAssetLoader.Builder()
        .addPathHandler("/assets/", new WebViewAssetLoader.AssetsPathHandler(this))
        .addPathHandler("/res/", new WebViewAssetLoader.ResourcesPathHandler(this))
        .build();

// Set AutoCaptureWebViewClient client
webView.setWebViewClient(new AutoCaptureWebViewClient(assetLoader));

// Add Output listener
webView.addJavascriptInterface(new OutputListener(), "outputListenerHandler");

// Add onImage listener
webView.addJavascriptInterface(new OnImageListener(), "onImageListenerHandler");

// Add Exit listener
webView.addJavascriptInterface(new ExitListener(), "exitListenerHandler");

// Set WebChromeClient for console messages and permissions
webView.setWebChromeClient(new WebChromeClient() {
    @Override
    public boolean onConsoleMessage(ConsoleMessage consoleMessage) {
        Log.d(consoleMessage.message())
        return true;
    }

    @Override
    public void onPermissionRequest(final PermissionRequest request) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            request.grant(request.getResources());
        }
    }
});

// Load the web app URL
webView.loadUrl("https://appassets.androidplatform.net/assets/<PATH TO AUTOCAPTURE FOLDER>/index.html");
```
 


