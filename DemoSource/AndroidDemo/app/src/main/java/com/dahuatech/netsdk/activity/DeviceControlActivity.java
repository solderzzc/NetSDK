package com.dahuatech.netsdk.activity;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.dahuatech.netsdk.R;
import com.dahuatech.netsdk.module.DeviceControlModule;

public class DeviceControlActivity extends AppCompatActivity implements View.OnClickListener{
    DeviceControlModule mDeviceControlModule;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_device_control);
        setTitle(R.string.activity_function_list_device_control);

        mDeviceControlModule = new DeviceControlModule(this);

        setupView();
    }

    private void setupView() {
        ((Button)findViewById(R.id.buttonReStart)).setOnClickListener(this);
        ((Button)findViewById(R.id.buttonSetUpTime)).setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.buttonReStart:
                mDeviceControlModule.restart();
                break;
            case R.id.buttonSetUpTime:
                mDeviceControlModule.setUpTime();
                break;
            default:
                break;
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onDestroy() {
        mDeviceControlModule = null;
        super.onDestroy();
    }
}
