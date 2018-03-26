package com.dahuatech.netsdk.activity;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.company.NetSDK.CB_fMessageCallBack;
import com.company.NetSDK.FinalVar;
import com.dahuatech.netsdk.R;
import com.dahuatech.netsdk.module.AlarmListenModule;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.StringTokenizer;

public class AlarmListenActivity extends AppCompatActivity implements View.OnClickListener,CB_fMessageCallBack{
    private final String TAG = "AlarmListenActivity";
    TextView mAlarmInfortv;
    AlarmListenModule mAlarmModule;
    boolean isListening = false;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_alarm_listen);
        setTitle(R.string.activity_function_list_alarm_listen);
        setupView();
        mAlarmModule = new AlarmListenModule(this);
        mAlarmModule.setCallback(this);
    }


    private void setupView(){
        ((Button) findViewById(R.id.alarm_start_listen_bt)).setOnClickListener(this);
        mAlarmInfortv = (TextView) findViewById(R.id.alarm_infor_tv);
    }

    private void listenAlarm(View v){
        isListening = !isListening;
       if (mAlarmModule.listen(isListening)){
           if (isListening){
               ((Button)v).setText(R.string.stop_alarm_listen);
           }else {
               mIndex = 0;
               mAlarmInfortv.setText("");
               ((Button)v).setText(R.string.start_alarm_listen);
           }
       }else {
           Toast.makeText(this,getString(R.string.info_failed),Toast.LENGTH_SHORT).show();
           isListening = !isListening;
       }
    }


    @Override
    public void onClick(View v) {
        int id = v.getId();
        switch (id){
            case R.id.alarm_start_listen_bt:
                listenAlarm(v);
                break;
            default:
                break;
        }
    }

     int mIndex = 0;
    @Override
    public boolean invoke(int lCommand, long lLoginID, Object obj, String pchDVRIP, int nDVRPort) {
        switch (lCommand) {
            case FinalVar.SDK_MOTION_ALARM_EX:
                Log.d(TAG,"lCommand:"+lCommand);
                byte[] motionAlarm = (byte[])obj;
                int channel = 0;
                int channelCount = NetSDKApplication.getInstance().getDeviceInfo().nChanNum;
                for (int i=0;i<motionAlarm.length && i< channelCount;i++){
                    if (1 == motionAlarm[i]) {
                        channel = i + 1;
                        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                        String time = format.format(new Date());
                        String AlarmDetect = getString(R.string.alarm_listen_motion) + ": " + getString(R.string.alarm_listen_detect);
                        mIndex++;
                        Message msg = mHandler.obtainMessage(UPDATE_ALARM_INFOR);
                        msg.obj = "[ " + mIndex + " ]" + AlarmDetect+"         "+time+ "     "+getString(R.string.channel)+channel+ "\n";
                        mHandler.sendMessage(msg);
                    }
                }
                break;
            default:
                return true;
        }
        return true;
    }
    ///message identifier for updating motion detection alarm.
    ///更新动检报警信息的消息标识
    private static final int UPDATE_ALARM_INFOR = 0x10;

    Handler mHandler = new Handler(Looper.myLooper()){
        @Override
        public void handleMessage(Message msg){
            if (msg.what == UPDATE_ALARM_INFOR && msg.obj !=null){
                String infor = (String)msg.obj;
                Log.d(TAG,"infor:"+infor);
                mAlarmInfortv.append(infor);
            }
        }
    };

    @Override
    protected void onDestroy(){
        super.onDestroy();
        if (isListening){
            mAlarmModule.listen(false);
        }
        mAlarmInfortv = null;
        mAlarmModule.release();
        mAlarmModule = null;
        mIndex = 0;
    }

}
