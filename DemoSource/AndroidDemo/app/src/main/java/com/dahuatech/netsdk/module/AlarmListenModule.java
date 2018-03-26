package com.dahuatech.netsdk.module;

import android.content.Context;
import android.support.v7.app.AppCompatActivity;

import com.company.NetSDK.CB_fMessageCallBack;
import com.company.NetSDK.FinalVar;
import com.company.NetSDK.INetSDK;
import com.company.NetSDK.SDK_MOTION_DETECT_CFG_EX;
import com.dahuatech.netsdk.activity.NetSDKApplication;
import com.dahuatech.netsdk.common.NetSDKLib;

/**
 * Created by 29779 on 2017/4/8.
 */
public class AlarmListenModule {

    NetSDKApplication sdkApp;
    public AlarmListenModule(Context context){
        sdkApp = (NetSDKApplication)((AppCompatActivity)context).getApplication();
    }

    public void setCallback(CB_fMessageCallBack callback){
        INetSDK.SetDVRMessCallBack(callback);
    }

    /// Alarm monitor control,isListen==true start Listen, isListen==false stop Listen.
    ///报警监听控制，isListen == true开始监听，isListen==false结束监听
    public boolean listen(boolean isListen){
        if (isListen)
            return startListenAlarm(0);
        else
            return stopListenAlarm();
    }

    ///start listening   default channel == 0
    ///开始监听 默认的channel==0
    private boolean startListenAlarm(int channel){
        /*
        SDK_MOTION_DETECT_CFG_EX[]  detect = new SDK_MOTION_DETECT_CFG_EX[1];
        detect[0] = new SDK_MOTION_DETECT_CFG_EX();
        Integer retBytes = new Integer(0);
        if (!INetSDK.GetDevConfig(sdkApp.getLoginHandle(), FinalVar.SDK_DEV_MOTIONALARM_CFG,channel,detect,retBytes, NetSDKLib.TIMEOUT_5S)){
            return false;
        }
        detect[0].byMotionEn = 1;
        if(!INetSDK.SetDevConfig(sdkApp.getLoginHandle(),FinalVar.SDK_DEV_MOTIONALARM_CFG,channel,detect,NetSDKLib.TIMEOUT_5S)){
            return false;
        }
        Integer proVer = new Integer(0);
        if(!INetSDK.QueryDevState(sdkApp.getLoginHandle(),FinalVar.SDK_DEVSTATE_PROTOCAL_VER,proVer,NetSDKLib.TIMEOUT_5S)){
            return false;
        }
        if (proVer.intValue()<5){
            if (!INetSDK.StartListen(sdkApp.getLoginHandle())){
                return false;
            }
        }else {
            if (! INetSDK.StartListenEx(sdkApp.getLoginHandle())){
                return false;
            }
        }
        */
        if (! INetSDK.StartListenEx(sdkApp.getLoginHandle())){
            return false;
        }
        return true;
    }

    /// stop listening
    /// 结束监听
    private boolean stopListenAlarm(){
        return INetSDK.StopListen(sdkApp.getLoginHandle());
    }

    public void release(){
        sdkApp = null;
    }

}
