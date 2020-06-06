package tojoy.it.tvcontrol;

import android.Manifest;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    private Button mButton1;
    private Button mButton2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mButton1 = findViewById(R.id.network);
        mButton2 = findViewById(R.id.bluetooth);
        mButton1.setOnClickListener(this);
        mButton2.setOnClickListener(this);
        request();
    }

    @Override
    public void onClick(View v) {
        Intent intent = null;
        switch (v.getId()) {
            case R.id.network:
                intent = new Intent(this, NetActivity.class);
                break;
            case R.id.bluetooth:
                intent = new Intent(this, BlueToothActivity.class);
                break;
        }
        startActivity(intent);
    }

    public void request() {
        String permissions[] = {Manifest.permission.RECORD_AUDIO, Manifest.permission.INTERNET};
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            requestPermissions(permissions, 1);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }
}