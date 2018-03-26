package com.dahuatech.netsdk.module;

import android.content.Context;
import android.content.res.Resources;
import android.support.v7.app.AppCompatActivity;

import com.company.NetSDK.ALARM_CONTROL;
import com.company.NetSDK.INetSDK;
import com.company.NetSDK.SDK_IOTYPE;
import com.company.NetSDK.TRIGGER_MODE_CONTROL;
import com.dahuatech.netsdk.R;
import com.dahuatech.netsdk.activity.NetSDKApplication;
import com.dahuatech.netsdk.common.ToolKits;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by 29779 on 2017/4/8.
 */
public class AlarmContrlModule {
    Context mContext;
    Resources res;
    NetSDKApplication app;
    Integer nIOCount = new Integer(0);
    public int status = 0;
    public int nChnIn = 0;
    public int nChnOut = 0;
    public int nChnTri = 0;
    ALARM_CONTROL[] alarm_input;
    ALARM_CONTROL[] alarm_output;
    TRIGGER_MODE_CONTROL[] alarm_trigger;

    public AlarmContrlModule(Context context) {
        this.mContext = context;
        res = mContext.getResources();
        app = ((NetSDKApplication)((AppCompatActivity)mContext).getApplication());

        nChnIn = getChannel(SDK_IOTYPE.SDK_ALARMINPUT);
        nChnOut = getChannel(SDK_IOTYPE.SDK_ALARMOUTPUT);
        nChnTri =  getChannel(SDK_IOTYPE.SDK_ALARM_TRIGGER_MODE);

        if(nChnIn > 0) {
            alarm_input = new ALARM_CONTROL[nChnIn];
            for(int i=0; i<nChnIn; i++) {
                alarm_input[i] = new ALARM_CONTROL();
            }
        }

        if(nChnOut > 0) {
            alarm_output = new ALARM_CONTROL[nChnOut];
            for(int j=0; j<nChnOut; j++) {
                alarm_output[j] = new ALARM_CONTROL();
            }
        }

        if(nChnTri > 0) {
            alarm_trigger = new TRIGGER_MODE_CONTROL[nChnTri];
            for(int k=0; k<nChnTri; k++) {
                alarm_trigger[k] = new TRIGGER_MODE_CONTROL();
            }
        }
    }

    ///Get the count of alarm channel
    ///获取报警通道数量
    public int getChannel(int mAlarmIOType){
        if (INetSDK.QueryIOControlState(app.getLoginHandle(), mAlarmIOType, null, nIOCount, 3000)) {
            ToolKits.writeLog("channelNum : " + nIOCount.intValue());
        } else {
            return -1;
        }
        return nIOCount.intValue();
    }

    ///Depending on the count of alarm channel, add the number of alarm channel on the widget.
    ///根据获取的报警通道数量，在控件上来添加报警通道号.
    public List getChannelList(int mAlarmIOType){
        ArrayList<String> channelList = new ArrayList<String>();
        for (int i=0; i<getChannel(mAlarmIOType); i++){
            channelList.add(res.getString(R.string.channel) + i);
        }
        return channelList;
    }

    ///Depending on the count of alarm channel, add the state of alarm channel on the widget.
    ///根据获取的报警通道数量，在控件上来添加报警通道状态.
    public List getSatusTypeList(int mAlarmIOType){
        String[] statusNames;
        ArrayList<String> statusList = new ArrayList<String>();

        if(getChannel(mAlarmIOType) > 0) {
            if( (mAlarmIOType == SDK_IOTYPE.SDK_ALARMINPUT) ||
                    (mAlarmIOType == SDK_IOTYPE.SDK_ALARMOUTPUT) ) {
                statusNames = res.getStringArray(R.array.alarm_status_array);
                for (int i = 0; i < statusNames.length; i++) {
                    statusList.add(statusNames[i]);
                }
            } else if(mAlarmIOType == SDK_IOTYPE.SDK_ALARM_TRIGGER_MODE) {
                statusNames = res.getStringArray(R.array.alarm_trigger_mode_array);
                for (int i=0; i<statusNames.length; i++){
                    statusList.add(statusNames[i]);
                }
            }
        }
        return statusList;
    }

    ///Get the state of alarm channel
    ///获取报警通道状态
    public int getAlarmChnStatus(int mAlarmIOType, int channel) {
        if(mAlarmIOType == SDK_IOTYPE.SDK_ALARMINPUT) {
            if (INetSDK.QueryIOControlState(app.getLoginHandle(), mAlarmIOType, alarm_input, nIOCount, 3000)) {
                status = alarm_input[channel].state;
                ToolKits.showMessage(mContext, res.getString(R.string.get) + res.getString(R.string.info_success));
            } else {
                ToolKits.showMessage(mContext, res.getString(R.string.get) + res.getString(R.string.info_failed));
                return -1;
            }
        } else if(mAlarmIOType == SDK_IOTYPE.SDK_ALARMOUTPUT) {
            if (INetSDK.QueryIOControlState(app.getLoginHandle(), mAlarmIOType, alarm_output, nIOCount, 3000)) {
                status = alarm_output[channel].state;
                ToolKits.showMessage(mContext, res.getString(R.string.get) + res.getString(R.string.info_success));
            } else {
                ToolKits.showMessage(mContext, res.getString(R.string.get) + res.getString(R.string.info_failed));
                return -1;
            }
        } else if(mAlarmIOType == SDK_IOTYPE.SDK_ALARM_TRIGGER_MODE) {
            if (INetSDK.QueryIOControlState(app.getLoginHandle(), mAlarmIOType, alarm_trigger, nIOCount, 3000)) {
                status =  alarm_trigger[channel].mode;
                ToolKits.showMessage(mContext, res.getString(R.string.get) + res.getString(R.string.info_success));
            } else {
                ToolKits.showMessage(mContext, res.getString(R.string.get) + res.getString(R.string.info_failed));
                return  -1;
            }
        }
        return status;
    }

    ///Set the state of alarm channel
    ///设置报警通道状态
    public boolean setAlarmChnStatus(int mAlarmIOType, int channel, int status) {
        if(mAlarmIOType == SDK_IOTYPE.SDK_ALARMINPUT) {
            ALARM_CONTROL[] alarm_in = new ALARM_CONTROL[1];
            alarm_in[0] = new ALARM_CONTROL();
            alarm_in[0].index = (short)channel;
            alarm_in[0].state = (short)status;
            if(INetSDK.IOControl(app.getLoginHandle(), mAlarmIOType, alarm_in)) {
                ToolKits.showMessage(mContext, res.getString(R.string.set) + res.getString(R.string.info_success));
            } else {
                ToolKits.showMessage(mContext, res.getString(R.string.set) + res.getString(R.string.info_failed));
                return false;
            }
        } else if(mAlarmIOType == SDK_IOTYPE.SDK_ALARMOUTPUT) {
            ALARM_CONTROL[] alarm_out = new ALARM_CONTROL[1];
            alarm_out[0] = new ALARM_CONTROL();
            alarm_out[0].index = (short)channel;
            alarm_out[0].state = (short)status;
            if(INetSDK.IOControl(app.getLoginHandle(), mAlarmIOType, alarm_out)) {
                ToolKits.showMessage(mContext, res.getString(R.string.set) + res.getString(R.string.info_success));
            } else {
                ToolKits.showMessage(mContext, res.getString(R.string.set) + res.getString(R.string.info_failed));
                return false;
            }
        } else if(mAlarmIOType == SDK_IOTYPE.SDK_ALARM_TRIGGER_MODE) {
            TRIGGER_MODE_CONTROL[] alarm_trig = new TRIGGER_MODE_CONTROL[1];
            alarm_trig[0] = new TRIGGER_MODE_CONTROL();
            alarm_trig[0].index = (short)channel;
            alarm_trig[0].mode = (short)status;
            if(INetSDK.IOControl(app.getLoginHandle(), mAlarmIOType, alarm_trig)) {
                ToolKits.showMessage(mContext, res.getString(R.string.set) + res.getString(R.string.info_success));
            } else {
                ToolKits.showMessage(mContext, res.getString(R.string.set) + res.getString(R.string.info_failed));
                return false;
            }
        }
        return true;
    }
}
