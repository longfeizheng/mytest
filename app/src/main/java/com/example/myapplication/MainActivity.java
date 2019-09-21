package com.example.myapplication;

import android.Manifest;
import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.ClipData;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.webkit.JavascriptInterface;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.afollestad.materialdialogs.MaterialDialog;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class MainActivity extends AppCompatActivity {

    private WebView webView;
    private TextView textView;
    private ImageView imageView;
    private Button button;
    private static final String TAG = MainActivity.class.getSimpleName();
    private String cameraFilePath;
    private ValueCallback mUM;
    ValueCallback<Uri> mUploadMessage;
    ValueCallback<Uri[]> mUploadCallbackAboveL;
    private final static int FCR=1;

    private boolean multiple_files = false;

    private Context context;

    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //1、使用findViewById()方法获取到WebView的实例
        webView = findViewById(R.id.web_view);
        textView = findViewById(R.id.text);
        imageView = findViewById(R.id.image);
        button = findViewById(R.id.button);
        //2、调用WebView的getSettings()方法去设置一些浏览器的属性
        //setJavaScriptEnabled()方法用于设置是否执行页面的js方法
        WebSettings settings = webView.getSettings();

        settings.setJavaScriptEnabled(true);    //设置webview支持javascript
        settings.setLoadsImagesAutomatically(true);    //支持自动加载图片
        settings.setUseWideViewPort(true);    //设置webview推荐使用的窗口，使html界面自适应屏幕
        settings.setLoadWithOverviewMode(true);
        settings.setSaveFormData(true);    //设置webview保存表单数据

        settings.setSupportMultipleWindows(true);
        settings.setAppCacheEnabled(true); //设置APP可以缓存
        settings.setDatabaseEnabled(true);
        settings.setDomStorageEnabled(true);//返回上个界面不刷新  允许本地缓存
        settings.setAllowFileAccess(true);// 设置可以访问文件

        webView.addJavascriptInterface(this, "wjj");
        //3、调用WenView的setWebViewClient()方法并传入一个WebViewClient的实例

        //4、调用WebView的loadUrl()方法并将网址传入
        webView.loadUrl("file:///android_asset/show.html");
        button.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                webView.loadUrl("javascript:javacalljs()");
                webView.loadUrl("javascript:javacalljswhthargs("+"' hello word'"+")");
            }
        });

        webView.setLongClickable(true);
        webView.setScrollbarFadingEnabled(true);
        webView.setScrollBarStyle(View.SCROLLBARS_INSIDE_OVERLAY);
        webView.setDrawingCacheEnabled(true);


        //添加客户端支持
        webView.setWebChromeClient(new WebChromeClient(){

            @Override
            public boolean onShowFileChooser(WebView webView, ValueCallback<Uri[]> filePathCallback, FileChooserParams fileChooserParams) {
                boolean fromCamera = fileChooserParams.isCaptureEnabled();
                String[] acceptTypes = fileChooserParams.getAcceptTypes();
                if (acceptTypes != null && acceptTypes.length > 0) {
                    boolean isVideo = false;
                    for (String acceptType : acceptTypes) {
                        if (acceptType.contains("video")) {
                            isVideo = true;
                            break;
                        }
                    }

                    boolean isImage = false;
                    for (String acceptType : acceptTypes) {
                        if (acceptType.contains("image")) {
                            isImage = true;
                            break;
                        }
                    }
                    if (isImage) {//处理图片
                        if (fromCamera) {//只处理拍照

                            mUploadCallbackAboveL = filePathCallback;//暂存，用于拍完照片后回调H5
                                return true;//返回true表示APP处理文件选择

                        }
                    }
                }
                return super.onShowFileChooser(webView, filePathCallback, fileChooserParams);
            }
        });

    }




    //拍照

    private File tempFile = null;   //新建一个 File 文件（用于相机拿数据）

    // 获取 相机 或者 图库 返回的图片
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        //判断返回码不等于0
        if (requestCode != RESULT_CANCELED){    //RESULT_CANCELED = 0(也可以直接写“if (requestCode != 0 )”)
            //读取返回码
            switch (requestCode){
                case 100:   //相册返回的数据（相册的返回码）

                    Uri uri01 = data.getData();
                    try {
                        Bitmap bitmap = BitmapFactory.decodeStream(getContentResolver().openInputStream(uri01));
                        imageView.setImageBitmap(bitmap);

                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                    break;
                case 101:  //相机返回的数据（相机的返回码）

                    try {
                        Bundle bundle = data.getExtras();
                        // 获取相机返回的数据，并转换为Bitmap图片格式，这是缩略图
                        Bitmap bitmap = (Bitmap) bundle.get("data");
                        imageView.setImageBitmap(bitmap);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    break;
            }
        }
    }


    @JavascriptInterface
    public void startFunction() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                textView.setText(textView.getText() + "\n js调用android");
            }
        });
    }

    @JavascriptInterface
    public void startFunction(final String str) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                textView.setText(textView.getText() + "\n js调用android 传递参数+" + str);
            }
        });
    }


    @JavascriptInterface
    public void startCamera() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
                    startActivityForResult(takePictureIntent, 101);
                }
            }
        });
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }


}
