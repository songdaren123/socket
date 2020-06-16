package tojoy.it.tvcontrol.net;

import android.content.ContentResolver;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;


import com.uuzuche.lib_zxing.activity.CaptureFragment;
import com.uuzuche.lib_zxing.activity.CodeUtils;

import tojoy.it.tvcontrol.R;
import tojoy.it.tvcontrol.utils.LogUtil;
import tojoy.it.tvcontrol.utils.UrlUtil;

/**
 * Initial the camera
 * <p>
 * 默认的二维码扫描Activity
 */
public class CaptureActivity extends AppCompatActivity {
    private int REQUEST_IMAGE = 1002;
    private String TAG = this.getClass().getSimpleName();

    /**
     * 二维码解析回调函数
     */
    CodeUtils.AnalyzeCallback analyzeCallback = new CodeUtils.AnalyzeCallback() {
        @Override
        public void onAnalyzeSuccess(Bitmap mBitmap, String result) {
            CaptureActivity.this.onAnalyzeSucess(result);
        }

        @Override
        public void onAnalyzeFailed() {
            CaptureActivity.this.onAnalyzeFailedHandler();
        }
    };

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.camera);
        initView();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_IMAGE) {
            if (data != null) {
                Uri uri = data.getData();
                ContentResolver cr = getContentResolver();
                try {
                    Bitmap mBitmap = MediaStore.Images.Media.getBitmap(cr, uri);//显得到bitmap图片
                    String imagePath = CodeUtils.getImageAbsolutePath(CaptureActivity.this, uri);
                    CodeUtils.analyzeBitmap(imagePath, new CodeUtils.AnalyzeCallback() {
                        @Override
                        public void onAnalyzeSuccess(Bitmap mBitmap, String result) {
                            onAnalyzeSucess(result);
                        }

                        @Override
                        public void onAnalyzeFailed() {
                            onAnalyzeFailedHandler();
                        }
                    });

                    if (mBitmap != null) {
                        mBitmap.recycle();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public void onAnalyzeSucess(String result) {
        LogUtil.logd("江国球", result);
        UrlUtil.UrlEntity urlEntity = UrlUtil.parse(result);
        if (urlEntity != null && urlEntity.params != null) {
            if (!TextUtils.isEmpty(urlEntity.params.get("QR_PORT"))) {
                String ip = urlEntity.params.get("QR_IP");
                String port = urlEntity.params.get("QR_PORT");
                Intent intent = new Intent();
                intent.putExtra("ip", ip);
                intent.putExtra("port", Integer.valueOf(port));
                setResult(3, intent);

                LogUtil.logd(TAG, "IP" + ip + ">>>>>>>>port:" + port);
                finish();
            }
        }

    }


    public void onAnalyzeFailedHandler() {
        Toast.makeText(this, "解析二维码失败", Toast.LENGTH_SHORT).show();
    }


    public void initView() {
        CaptureFragment captureFragment = new CaptureFragment();
        captureFragment.setAnalyzeCallback(analyzeCallback);
        getSupportFragmentManager().beginTransaction().replace(R.id.fl_zxing_container, captureFragment).commit();
        captureFragment.setCameraInitCallBack(new CaptureFragment.CameraInitCallBack() {
            @Override
            public void callBack(Exception e) {
                if (e == null) {

                } else {
                    Log.e("TAG", "callBack: ", e);
                }
            }
        });
        findViewById(R.id.back_iv).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CaptureActivity.this.finish();
            }
        });

    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}