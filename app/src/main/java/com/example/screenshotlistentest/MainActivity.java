package com.example.screenshotlistentest;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {
    private ScreenShotListenManager manager;
    Button openListener,closeListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        manager = ScreenShotListenManager.newInstance(MainActivity.this);
        if(ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(MainActivity.this,new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},1);
        }else{
            listenScreenShot();
        }

        openListener = findViewById(R.id.open_listener);
        closeListener = findViewById(R.id.close_listener);

        openListener.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                manager.startListen();
                Toast.makeText(MainActivity.this,"开启截图监听",Toast.LENGTH_SHORT).show();
            }
        });

        closeListener.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                manager.stopListen();
                Toast.makeText(MainActivity.this,"关闭截图监听",Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode){
            case 1:
                if(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                    ActivityCompat.requestPermissions(MainActivity.this,new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},1);
                }else {
                    listenScreenShot();
                }
                break;
        }
    }

    protected void listenScreenShot(){
        manager.setListener(new ScreenShotListenManager.OnScreenShotListener() {
            @Override
            public void onShot(String imagePath) {
                Toast.makeText(MainActivity.this,"截图监听成功，图片路径为：" + imagePath,Toast.LENGTH_SHORT).show();
                Log.d("MainActivity.class","imagePath:" + imagePath);
            }
        });
    }

    @Override
    protected void onDestroy() {
        manager.stopListen();
        super.onDestroy();
    }
}