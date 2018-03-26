package com.dahuatech.netsdk.module;

import android.content.Context;
import android.content.res.Resources;
import android.support.v7.app.AppCompatActivity;

import com.company.NetSDK.CtrlType;
import com.company.NetSDK.INetSDK;
import com.company.NetSDK.NET_TIME;
import com.dahuatech.netsdk.R;
import com.dahuatech.netsdk.activity.NetSDKApplication;
import com.dahuatech.netsdk.common.ToolKits;

import java.text.SimpleDateFormat;

/**
 * Created by 29779 on 2017/4/8.
 */
public class DeviceControlModule {
    NetSDKApplication app;
    NET_TIME mDeviceTime;
    SimpleDateFormat mDateFormat;
    Context mContext;
    Resources res;

    public DeviceControlModule(Context context) {
        this.mContext = context;
        res = mContext.getResources();
        app = ((NetSDKApplication)((AppCompatActivity)mContext).getApplication());
    }

    ///Reboot device
    ///重启设备
    public boolean restart() {
        if(!INetSDK.ControlDevice(app.getLoginHandle(), CtrlType.SDK_CTRL_REBOOT, null, 3000)) {
            ToolKits.showMessage(mContext, res.getString(R.string.device_control_restart_failed));
            return false;
        } else {
            ToolKits.showMessage(mContext, res.getString(R.string.device_control_restart_succeed));
        }
        return true;
    }

    ///Setup time
    ///时间同步
    public boolean setUpTime() {
        mDeviceTime = new NET_TIME();

        ///Get the current system time of the phone
        ///获取当前的手机系统时间
        mDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String date = mDateFormat.format(new java.util.Date());

        String[] dateTime = date.split(" ");
        String[] mDate1 = dateTime[0].split("-");
        String[] mDate2 = dateTime[1].split(":");
        mDeviceTime.dwYear = Integer.parseInt(mDate1[0]);
        mDeviceTime.dwMonth = Integer.parseInt(mDate1[1]);
        mDeviceTime.dwDay = Integer.parseInt(mDate1[2]);
        mDeviceTime.dwHour = Integer.parseInt(mDate2[0]);
        mDeviceTime.dwMinute = Integer.parseInt(mDate2[1]);
        mDeviceTime.dwSecond = Integer.parseInt(mDate2[2]);

        if(!(INetSDK.SetupDeviceTime(app.getLoginHandle(), mDeviceTime))) {
            ToolKits.showMessage(mContext, res.getString(R.string.device_control_setuptime_failed));
            return false;
        } else {
            ToolKits.showMessage(mContext, res.getString(R.string.device_control_setuptime_succeed));
        }
        return true;
    }
}
