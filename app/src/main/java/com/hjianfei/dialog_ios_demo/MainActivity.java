package com.hjianfei.dialog_ios_demo;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.File;

public class MainActivity extends AppCompatActivity {

    private BottomMenuDialog dialog;
    private File file;
    private int REQUEST_CAMERA = 0x01;
    private ImageView imageView;
    private int READ_PHOTO = 0x02;

    @Override

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        imageView = (ImageView) findViewById(R.id.iv_image);
        findViewById(R.id.btn_show).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog = new BottomMenuDialog.Builder(MainActivity.this)
                        .setTitle("更换封面")
                        .addMenu("从手机相册选择", new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                dialog.dismiss();
                                getPhoto();
                            }
                        }).addMenu("拍一张", new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                dialog.dismiss();
                                applyWritePermission();
                            }
                        }).create();
                dialog.show();
            }
        });
    }

    public void applyWritePermission() {

        String[] permissions = {Manifest.permission.WRITE_EXTERNAL_STORAGE};

        if (Build.VERSION.SDK_INT >= 23) {
            int check = ContextCompat.checkSelfPermission(this, permissions[0]);
            // 权限是否已经 授权 GRANTED---授权  DINIED---拒绝
            if (check == PackageManager.PERMISSION_GRANTED) {
                //调用相机
                takePhoto();
            } else {
                requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
            }
        } else {
            takePhoto();
        }
    }

    /**
     * 使用相机
     */
    private void takePhoto() {
        file = new File(Environment.getExternalStorageDirectory().getAbsolutePath()
                + "/test/" + System.currentTimeMillis() + ".jpg");
        file.getParentFile().mkdirs();

        //改变Uri  com.hjianfei.dialog_ios_demo.provider注意和xml中的一致
        Uri uri = FileProvider.getUriForFile(this, "com.hjianfei.dialog_ios_demo.provider", file);
        PhotoUtils.takePicture(this, uri, REQUEST_CAMERA);
    }

    /**
     * 调用系统相册
     */
    public void getPhoto() {
        PhotoUtils.openPic(this, READ_PHOTO);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            takePhoto();
        } else {
            // 没有获取 到权限，从新请求，或者关闭app
            Toast.makeText(this, "需要存储权限", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1 && resultCode == RESULT_OK) {
            Log.e("TAG", "---------" + FileProvider.getUriForFile(this, "com.hjianfei.dialog_ios_demo.provider", file));
            imageView.setImageBitmap(BitmapFactory.decodeFile(file.getAbsolutePath()));
            //在手机相册中显示刚拍摄的图片
            Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
            Uri contentUri = Uri.fromFile(file);
            mediaScanIntent.setData(contentUri);
            sendBroadcast(mediaScanIntent);
        } else if (requestCode == 2 && resultCode == RESULT_OK) {
            imageView.setImageURI(data.getData());

        }
    }
}
