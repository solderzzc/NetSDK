package com.dahuatech.netsdk.module;

import android.content.Context;

import com.company.NetSDK.CB_fSearchDevicesCB;
import com.company.NetSDK.INetSDK;
import com.company.SmartConfig.ISmartConfig;
import com.dahuatech.netsdk.common.ToolKits;

/**
 * Created by 29779 on 2017/4/8.
 */
public class WIFIConfigurationModule {
    private final int CONFIG_WAIT_TIME_SEC = 5;
    public long lDevSearchHandle = 0;
    Context mContext;

    public WIFIConfigurationModule(Context context) {
        this.mContext = context;
    }

    ///Wifi config
    ///Wifi配置
    public void configIPCWifi(String sn,String ssid,String pwd){
        if ((sn == null||ssid == null||pwd == null)
                ||(sn.equals("")||ssid.equals(""))){
            ToolKits.writeLog("parameters is invalied");
            return;
        }
        ISmartConfig.ConfigIPCWifi(sn,ssid,pwd,CONFIG_WAIT_TIME_SEC);
    }

    ///Search device
    ///设备搜索
    public boolean StartSearchDevices(CB_fSearchDevicesCB callback) {
        if (callback == null)
            throw new NullPointerException("callback parameter is null");
        lDevSearchHandle = INetSDK.StartSearchDevices(callback);
        if(lDevSearchHandle == 0) {
            return false;
        }
        return true;
    }

    ///Stop search device
    ///停止设备搜索
    public void StopSearchDevices() {
        INetSDK.StopSearchDevices(lDevSearchHandle);
        lDevSearchHandle = 0;
    }
}
