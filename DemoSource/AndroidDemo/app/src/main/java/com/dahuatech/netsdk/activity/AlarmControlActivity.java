package com.dahuatech.netsdk.activity;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;

import com.company.NetSDK.SDK_IOTYPE;
import com.dahuatech.netsdk.R;
import com.dahuatech.netsdk.common.ToolKits;
import com.dahuatech.netsdk.module.AlarmContrlModule;

public class AlarmControlActivity extends AppCompatActivity implements AdapterView.OnItemSelectedListener, View.OnClickListener{
    AlarmContrlModule mAlarmContrlModule;
    Spinner mSelectAlarmInputChn;
    Spinner mSelectAlarmInputStatus;
    Spinner mSelectAlarmOutputChn;
    Spinner mSelectAlarmOutputStatus;
    Spinner mSelectAlarmTriggerChn;
    Spinner mSelectAlarmTriggerMode;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_alarm_control);
        setTitle(R.string.activity_function_list_alarm_control);

        mAlarmContrlModule = new AlarmContrlModule(this);
        setupView();
        initView();
    }

    private void setupView(){
        mSelectAlarmInputChn = (Spinner)findViewById(R.id.spinner_alarm_input_channel);
        mSelectAlarmInputStatus = (Spinner)findViewById(R.id.spinner_alarm_input_status);
        mSelectAlarmOutputChn = (Spinner)findViewById(R.id.spinner_alarm_output_channel);
        mSelectAlarmOutputStatus = (Spinner)findViewById(R.id.spinner_alarm_output_status);
        mSelectAlarmTriggerChn = (Spinner)findViewById(R.id.spinner_alarm_trigger_channel);
        mSelectAlarmTriggerMode = (Spinner)findViewById(R.id.spinner_alarm_trigger_mode);

        ((Button)findViewById(R.id.inputgetbutton)).setOnClickListener(this);
        ((Button)findViewById(R.id.inputsetbutton)).setOnClickListener(this);
        ((Button)findViewById(R.id.outputgetbutton)).setOnClickListener(this);
        ((Button)findViewById(R.id.outputsetbutton)).setOnClickListener(this);
        ((Button)findViewById(R.id.triggergetbutton)).setOnClickListener(this);
        ((Button)findViewById(R.id.triggersetbutton)).setOnClickListener(this);
    }

    private void initView(){
        ///Alarm Input
        ///报警输入
        mSelectAlarmInputChn.setAdapter(new ArrayAdapter<String>(this, android.R.layout.simple_spinner_dropdown_item,
                                            mAlarmContrlModule.getChannelList(SDK_IOTYPE.SDK_ALARMINPUT)));
        mSelectAlarmInputChn.setSelection(0);
        mSelectAlarmInputChn.setOnItemSelectedListener(this);

        mSelectAlarmInputStatus.setAdapter(new ArrayAdapter<String>(this, android.R.layout.simple_spinner_dropdown_item,
                                              mAlarmContrlModule.getSatusTypeList(SDK_IOTYPE.SDK_ALARMINPUT)));
        if(mAlarmContrlModule.nChnIn <= 0) {
            ToolKits.showMessage(AlarmControlActivity.this, getString(R.string.device_no_support_alarm_input_control));
        }

        ///Alarm Output
        ///报警输出
        mSelectAlarmOutputChn.setAdapter(new ArrayAdapter<String>(this, android.R.layout.simple_spinner_dropdown_item,
                                            mAlarmContrlModule.getChannelList(SDK_IOTYPE.SDK_ALARMOUTPUT)));
        mSelectAlarmOutputChn.setSelection(0);
        mSelectAlarmOutputChn.setOnItemSelectedListener(this);

        mSelectAlarmOutputStatus.setAdapter(new ArrayAdapter<String>(this, android.R.layout.simple_spinner_dropdown_item,
                                               mAlarmContrlModule.getSatusTypeList(SDK_IOTYPE.SDK_ALARMOUTPUT)));
        if(mAlarmContrlModule.nChnOut <= 0) {
            ToolKits.showMessage(AlarmControlActivity.this, getString(R.string.device_no_support_alarm_output_control));
        }

        ///Alarm Trigger
        ///报警触发
        mSelectAlarmTriggerChn.setAdapter(new ArrayAdapter<String>(this, android.R.layout.simple_spinner_dropdown_item,
                                            mAlarmContrlModule.getChannelList(SDK_IOTYPE.SDK_ALARM_TRIGGER_MODE)));
        mSelectAlarmTriggerChn.setSelection(0);
        mSelectAlarmTriggerChn.setOnItemSelectedListener(this);

        mSelectAlarmTriggerMode.setAdapter(new ArrayAdapter<String>(this, android.R.layout.simple_spinner_dropdown_item,
                                              mAlarmContrlModule.getSatusTypeList(SDK_IOTYPE.SDK_ALARM_TRIGGER_MODE)));
        if(mAlarmContrlModule.nChnTri <= 0) {
            ToolKits.showMessage(AlarmControlActivity.this, getString(R.string.device_no_support_alarm_trigger_control));
        }
    }

    @Override
    protected void onDestroy() {
        mAlarmContrlModule = null;
        super.onDestroy();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        int parentID = parent.getId();
        switch (parentID){
            case R.id.spinner_alarm_input_channel:
                mSelectAlarmInputStatus.setSelection(mAlarmContrlModule.getAlarmChnStatus(SDK_IOTYPE.SDK_ALARMINPUT,
                                                        mSelectAlarmInputChn.getSelectedItemPosition()),true);
                break;
            case R.id.spinner_alarm_output_channel:
                mSelectAlarmOutputStatus.setSelection(mAlarmContrlModule.getAlarmChnStatus(SDK_IOTYPE.SDK_ALARMOUTPUT,
                                                         mSelectAlarmOutputChn.getSelectedItemPosition()),true);
                break;
            case R.id.spinner_alarm_trigger_channel:
                mSelectAlarmTriggerMode.setSelection(mAlarmContrlModule.getAlarmChnStatus(SDK_IOTYPE.SDK_ALARM_TRIGGER_MODE,
                                                        mSelectAlarmTriggerChn.getSelectedItemPosition()),true);
                break;
            default:
                break;
        }
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.inputgetbutton :
                if(mAlarmContrlModule.nChnIn > 0) {
                    mSelectAlarmInputStatus.setSelection(mAlarmContrlModule.getAlarmChnStatus(SDK_IOTYPE.SDK_ALARMINPUT,
                            mSelectAlarmInputChn.getSelectedItemPosition()),true);
                } else {
                    ToolKits.showMessage(AlarmControlActivity.this, getString(R.string.device_no_support_alarm_input_control));
                }
                break;
            case R.id.inputsetbutton :
                if(mAlarmContrlModule.nChnIn > 0) {
                    mAlarmContrlModule.setAlarmChnStatus(SDK_IOTYPE.SDK_ALARMINPUT, mSelectAlarmInputChn.getSelectedItemPosition(),
                            mSelectAlarmInputStatus.getSelectedItemPosition());
                } else {
                    ToolKits.showMessage(AlarmControlActivity.this, getString(R.string.device_no_support_alarm_input_control));
                }
                break;
            case R.id.outputgetbutton :
                if(mAlarmContrlModule.nChnOut > 0) {
                    mSelectAlarmOutputStatus.setSelection(mAlarmContrlModule.getAlarmChnStatus(SDK_IOTYPE.SDK_ALARMOUTPUT,
                            mSelectAlarmOutputChn.getSelectedItemPosition()),true);
                }else {
                    ToolKits.showMessage(AlarmControlActivity.this, getString(R.string.device_no_support_alarm_output_control));
                }
                break;
            case R.id.outputsetbutton :
                if(mAlarmContrlModule.nChnOut > 0) {
                    mAlarmContrlModule.setAlarmChnStatus(SDK_IOTYPE.SDK_ALARMOUTPUT, mSelectAlarmOutputChn.getSelectedItemPosition(),
                            mSelectAlarmOutputStatus.getSelectedItemPosition());
                }else {
                    ToolKits.showMessage(AlarmControlActivity.this, getString(R.string.device_no_support_alarm_output_control));
                }
                break;
            case R.id.triggergetbutton :
                if(mAlarmContrlModule.nChnTri > 0) {
                    mSelectAlarmTriggerMode.setSelection(mAlarmContrlModule.getAlarmChnStatus(SDK_IOTYPE.SDK_ALARM_TRIGGER_MODE,
                            mSelectAlarmTriggerChn.getSelectedItemPosition()),true);
                }else {
                    ToolKits.showMessage(AlarmControlActivity.this, getString(R.string.device_no_support_alarm_trigger_control));
                }
                break;
            case R.id.triggersetbutton :
                if(mAlarmContrlModule.nChnTri > 0) {
                    mAlarmContrlModule.setAlarmChnStatus(SDK_IOTYPE.SDK_ALARM_TRIGGER_MODE, mSelectAlarmTriggerChn.getSelectedItemPosition(),
                            mSelectAlarmTriggerMode.getSelectedItemPosition());
                }else {
                    ToolKits.showMessage(AlarmControlActivity.this, getString(R.string.device_no_support_alarm_trigger_control));
                }
                break;
            default:
                break;
        }
    }
}