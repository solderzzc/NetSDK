package com.dahuatech.netsdk.activity;

import android.content.pm.PackageManager;
import android.os.Build;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.RadioButton;

import com.dahuatech.netsdk.R;
import com.dahuatech.netsdk.module.TalkModule;

public class TalkActivity extends AppCompatActivity {
    TalkModule mTalkModule;
    public RadioButton mRadioTalkBtn;
    RadioButton mRadioSpeakOnlyBtn;
    Button mStartBtn;
    boolean mTalkFlag = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_talk);
        setTitle(R.string.activity_main_talk);

        mTalkModule = new TalkModule(this);
        checkPromission();
        setupView();
    }

    private void setupView(){
        mRadioTalkBtn = (RadioButton)findViewById(R.id.talk);
        mRadioSpeakOnlyBtn = (RadioButton)findViewById(R.id.speak_only);
        mStartBtn = (Button)findViewById(R.id.buttonstart);
        mStartBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!mTalkFlag) {
                    if(mTalkModule.startTalk()) {
                        mRadioTalkBtn.setEnabled(false);
                        mRadioSpeakOnlyBtn.setEnabled(false);
                        mTalkFlag = true;
                        mStartBtn.setText(R.string.stop);
                    }
                } else {
                    mTalkModule.stopTalk();
                    mRadioTalkBtn.setEnabled(true);
                    mRadioSpeakOnlyBtn.setEnabled(true);
                    mTalkFlag = false;
                    mStartBtn.setText(R.string.start);
                }
            }
        });
    }


    @Override
    protected void onDestroy() {
        mTalkModule.stopTalk();
        mTalkModule.mTalkHandle = 0;
        mTalkModule = null;
        super.onDestroy();
    }

    @Override
    protected void onResume() {
        // while onResume we should logout the device.
        super.onResume();
    }

    private void  checkPromission(){
        if(Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP_MR1) {
            if(checkSelfPermission(android.Manifest.permission.RECORD_AUDIO)
                    != PackageManager.PERMISSION_GRANTED){
                String[] per = {android.Manifest.permission.RECORD_AUDIO};
                requestPermissions(per,123);
            }
        }
    }
}
