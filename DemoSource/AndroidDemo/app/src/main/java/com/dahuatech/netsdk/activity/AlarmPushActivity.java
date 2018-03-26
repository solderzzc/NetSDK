package com.dahuatech.netsdk.activity;

import android.content.res.Resources;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.dahuatech.netsdk.R;
import com.dahuatech.netsdk.common.DialogProgress;
import com.dahuatech.netsdk.common.PushHelper;
import com.dahuatech.netsdk.common.ToolKits;
import com.dahuatech.netsdk.module.AlarmPushModule;

import java.util.HashMap;
import java.util.LinkedList;

public class AlarmPushActivity extends AppCompatActivity {
    private static final String TAG = "AlarmPushActivity";
    private AlarmPushModule mAlarmPushModule;
    private NetSDKApplication sdkApp;
    private TextView mTextViewNotifyInfo;
    private DialogProgress mDialogProgress;
    private Resources res;

    private enum SubEnum {
        NOT_SUPPORT_GOOGLE_SERVICE,
        SUB_SUCCESS,
        SUB_FAILED,
        UNSUB_SUCCESS,
        UNSUB_FAILED,
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_alarm_push);
        setTitle(R.string.activity_function_list_alarm_push);
        res = getResources();
        sdkApp = (NetSDKApplication)getApplication();
        mAlarmPushModule = new AlarmPushModule(sdkApp.getLoginHandle());
        setupView();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    private void setupView() {
        mDialogProgress = new DialogProgress(this);

        Button mButtonSubAlaram = (Button)findViewById(R.id.btnSubAlarm);
        mButtonSubAlaram.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new SubscribeTask().execute();
            }
        });

        Button mButtonUnSubAlaram = (Button) findViewById(R.id.btnUnSubAlarm);
        mButtonUnSubAlaram.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new UnSubscribeTask().execute();
            }
        });

        mTextViewNotifyInfo = (TextView)findViewById(R.id.textViewAlarmPush);
    }

    private SubEnum subButtonPerformed() {
        /// <key:alarm_type, List:channel>
        /// <key: 报警类似， List : 通道号>
        HashMap<String, LinkedList<Integer>> pushMaps = new HashMap<String, LinkedList<Integer>>();
        pushMaps.clear();

        int allChannels = sdkApp.getDeviceInfo().nChanNum;

        /// subscribe all channels
        /// 订阅设备的所有通道
        LinkedList<Integer> alarmIPCChannel = new LinkedList<>();
        LinkedList<Integer> videoMotionChannel = new LinkedList<>();
        LinkedList<Integer> alarmLocalChannel = new LinkedList<>();
        for (int i = 0 ; i < allChannels; ++i) {
            alarmIPCChannel.add(i);
            videoMotionChannel.add(i);
            alarmLocalChannel.add(i);
        }

        pushMaps.put("AlarmIPC", alarmIPCChannel);
        pushMaps.put("VideoMotion", videoMotionChannel);
        pushMaps.put("AlarmLocal", alarmLocalChannel);

        /// Get register id form google service
        /// 获取google 服务，注册ID
        String registerID = PushHelper.instance().getRegisterID(this);
        if (registerID == null) {
            Log.d(TAG, "not support google service.");
            return SubEnum.NOT_SUPPORT_GOOGLE_SERVICE;
        }

        boolean bRet = mAlarmPushModule.subscribeDeviceAlarm(
                PushHelper.instance().getApiKey(),
                registerID,
                500646880,
                pushMaps,
                new String(sdkApp.getDeviceInfo().sSerialNumber).trim(),
                "deviceName");
        return bRet ? SubEnum.SUB_SUCCESS : SubEnum.SUB_FAILED;
    }

    private SubEnum unSubButtonPerformed() {
        /// Get register id form google service
        /// 获取google 服务，注册ID
        String registerID = PushHelper.instance().getRegisterID(this);
        if (registerID == null) {
            Log.d(TAG, "not support google service.");
            return SubEnum.NOT_SUPPORT_GOOGLE_SERVICE;
        }

        boolean bRet = mAlarmPushModule.unSubscribeDeviceAlarm(registerID);
        return bRet ? SubEnum.UNSUB_SUCCESS : SubEnum.UNSUB_FAILED;
    }

    private class SubscribeTask extends AsyncTask<String, Integer, SubEnum> {
        @Override
        protected void onPreExecute(){
            super.onPreExecute();
            showDialogProgress();
        }
        @Override
        protected SubEnum doInBackground(String... params) {
            return subButtonPerformed();
        }
        @Override
        protected void onPostExecute(SubEnum result){
            mDialogProgress.dismiss();
            int resId = R.string.alarm_push_sub_successed;
            switch(result) {
                case NOT_SUPPORT_GOOGLE_SERVICE:
                    resId = R.string.alarm_push_not_support_google_service;
                    break;
                case SUB_FAILED:
                    resId = R.string.alarm_push_sub_failed;
                break;
                default:
            }
            ToolKits.showMessage(AlarmPushActivity.this, getResources().getString(resId));
        }
    }

    private class UnSubscribeTask extends AsyncTask<String, Void, SubEnum> {
        @Override
        protected void onPreExecute(){
            super.onPreExecute();
            showDialogProgress();
        }
        @Override
        protected SubEnum doInBackground(String... params) {
            return unSubButtonPerformed();
        }
        @Override
        protected void onPostExecute(SubEnum result){
            mDialogProgress.dismiss();
            int resId = R.string.alarm_push_unsub_successed;
            switch(result) {
                case NOT_SUPPORT_GOOGLE_SERVICE:
                    resId = R.string.alarm_push_not_support_google_service;
                    break;
                case UNSUB_FAILED:
                    resId = R.string.alarm_push_unsub_failed;
                    break;
                default:
            }
            ToolKits.showMessage(AlarmPushActivity.this, getResources().getString(resId));
        }
    }

    private void showDialogProgress() {
        mDialogProgress.setMessage(res.getString(R.string.waiting));
        mDialogProgress.setSpinnerType(DialogProgress.FADED_ROUND_SPINNER);
        mDialogProgress.setCancelable(false);
        mDialogProgress.show();
    }
}
