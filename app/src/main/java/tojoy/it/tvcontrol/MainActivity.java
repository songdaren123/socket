package tojoy.it.tvcontrol;

import android.Manifest;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import java.util.LinkedList;
import java.util.Queue;

import tojoy.it.tvcontrol.bluetooth.BlueToothActivity;
import tojoy.it.tvcontrol.net.NetActivity;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    private Button mButton1;
    private Button mButton2;
    private Queue<String> runingrequest = new LinkedList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mButton1 = findViewById(R.id.network);
        mButton2 = findViewById(R.id.bluetooth);
        mButton1.setOnClickListener(this);
        mButton2.setOnClickListener(this);
        runingrequest.offer(Manifest.permission.RECORD_AUDIO);
        runingrequest.offer(Manifest.permission.CAMERA);
        runingrequest.offer(Manifest.permission.READ_EXTERNAL_STORAGE);
        runingrequest.offer(Manifest.permission.WRITE_EXTERNAL_STORAGE);
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
        String permissions[] = {Manifest.permission.INTERNET};
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            requestPermissions(permissions, 1);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (!runingrequest.isEmpty()) {
            String permis[] = {runingrequest.poll()};
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                requestPermissions(permis, 1);
            }
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }
}