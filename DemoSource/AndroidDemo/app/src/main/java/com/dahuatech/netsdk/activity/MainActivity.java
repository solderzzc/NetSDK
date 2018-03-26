package com.dahuatech.netsdk.activity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;

import com.dahuatech.netsdk.R;
import com.dahuatech.netsdk.common.NetSDKLib;

public class MainActivity extends AppCompatActivity {
    private Button mButtonIpLogin;
    private Button mButtonP2PLogin;
    private Button mButtonWifiConfig;

    private final View.OnClickListener mButtonHandler = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (v == mButtonIpLogin && mButtonIpLogin != null) {
                startActivity(new Intent(MainActivity.this, IPLoginActivity.class));
            }else if (v == mButtonP2PLogin && mButtonP2PLogin != null) {
                startActivity(new Intent(MainActivity.this, P2PLoginActivity.class));
            }else if (v == mButtonWifiConfig && mButtonWifiConfig != null) {
                startActivity(new Intent(MainActivity.this, WIFIConfigurationActivity.class));
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        /// Initializing the NetSDKLib is important and necessary to ensure that
        /// all the APIs of INetSDK.jar are effective.
        /// 注意: 必须调用 init 接口初始化 INetSDK.jar 仅需要一次初始化
        NetSDKLib.getInstance().init();

        // Open sdk log
        final String file = new String(Environment.getExternalStorageDirectory().getPath() + "/sdk_log.log");
        NetSDKLib.getInstance().openLog(file);

        setupView();
    }

    @Override
    protected void onDestroy() {
        // while exiting the application, please make sure to invoke cleanup.
        /// 退出应用后，调用 cleanup 清理资源
        NetSDKLib.getInstance().cleanup();
        super.onDestroy();
    }

    private void setupView() {
        mButtonIpLogin = (Button) findViewById(R.id.btnIPLogin);
        mButtonIpLogin.setOnClickListener(mButtonHandler);

        mButtonP2PLogin = (Button) findViewById(R.id.btnP2PLogin);
        mButtonP2PLogin.setOnClickListener(mButtonHandler);

        mButtonWifiConfig = (Button) findViewById(R.id.btnWIFIConfig);
        mButtonWifiConfig.setOnClickListener(mButtonHandler);
    }
}


