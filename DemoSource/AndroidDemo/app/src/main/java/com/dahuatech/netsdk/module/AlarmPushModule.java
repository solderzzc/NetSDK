package com.dahuatech.netsdk.module;

import android.util.Log;

import com.company.NetSDK.EM_EVENT_SUB_CODE;
import com.company.NetSDK.EM_MOBILE_SERVER_TYPE;
import com.company.NetSDK.FinalVar;
import com.company.NetSDK.INetSDK;
import com.company.NetSDK.NET_MOBILE_PUSH_NOTIFY;
import com.company.NetSDK.NET_MOBILE_PUSH_NOTIFY_DEL;
import com.company.NetSDK.NET_OUT_DELETECFG;
import com.dahuatech.netsdk.common.ToolKits;

import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;

/**
 * Created by 29779 on 2017/4/8.
 */
public class AlarmPushModule {
    private final static String TAG = "AlarmPushModule";
    private long mLoginHandle;

    public AlarmPushModule(long loginHandle) {
        mLoginHandle = loginHandle;
    }

    /**
     *  Subscribe Alarm, support VideoMotion / AlarmLocal / AlarmIPC
     * @param API_KEY
     * @param registerID
     * @param period
     * @param pushMaps
     * @param devUID you can use this id to discriminate event msg come different device
     * @param devName you can user this name to display device
     * @return
     */
    public boolean subscribeDeviceAlarm(
                                    String API_KEY,
                                    String registerID,
                                    long period,
                                    HashMap<String, LinkedList<Integer>> pushMaps,
                                    String devUID,
                                    String devName) {
        Log.d(TAG, "registerID: " + registerID + "; device sn: " + devUID);
        if (registerID == null) {
            return false;
        }

        int eventCount = pushMaps.size();
        boolean bRet;
        NET_MOBILE_PUSH_NOTIFY stNotify = new NET_MOBILE_PUSH_NOTIFY(eventCount);

        //RegisterID
        StringToByteArray(registerID, stNotify.szRegisterID); // for device service to

        //serverType
        stNotify.emServerType = EM_MOBILE_SERVER_TYPE.EM_MOBILE_SERVER_TYPE_ANDROID;

        //PeriodOfValidity
        stNotify.nPeriodOfValidity = (int) period;

        //AuthServer -- invalid since google not supported C2DM any more
        StringToByteArray("https://www.google.com/accounts/ClientLogin", stNotify.szAuthServerAddr); //
        stNotify.nAuthServerPort = 443;

        //PushServer -- proxy server.
        StringToByteArray("https://cellphonepush.quickddns.com/gcm/send", stNotify.szPushServerAddr);
        stNotify.nPushServerPort = 443;

        // PushServer
        String PushServer = "https://android.googleapis.com/gcm/send";
        StringToByteArray(PushServer, stNotify.stuPushServerMain.szAddress);
        stNotify.stuPushServerMain.nPort = 443;

        // DevName
        StringToByteArray(devName, stNotify.szDevName);
        // DevID
        StringToByteArray(devUID, stNotify.szDevID);
        // user
        StringToByteArray(API_KEY, stNotify.szUser);
        //
        StringToByteArray("", stNotify.szPassword);

        Iterator<Map.Entry<String, LinkedList<Integer>>> it = pushMaps.entrySet().iterator();
        int n = 0;
        while (it.hasNext()) {
            Map.Entry<String, LinkedList<Integer>> entry = it.next();
            if (entry.getKey().equals("VideoMotion")) {
                if (entry.getValue() != null && entry.getValue().size() > 0) {
                    stNotify.pstuSubscribes[n].nCode = FinalVar.EVENT_ALARM_MOTIONDETECT;
                    stNotify.pstuSubscribes[n].emSubCode = EM_EVENT_SUB_CODE.EM_EVENT_SUB_CODE_UNKNOWN;
                    stNotify.pstuSubscribes[n].nChnNum = entry.getValue().size();
                    for (int i = 0; i < entry.getValue().size(); i++) {
                        stNotify.pstuSubscribes[n].nIndexs[i] = entry.getValue().get(i);
                    }
                    n++;
                }
            } else if (entry.getKey().equals("AlarmLocal")) {
                if (entry.getValue() != null && entry.getValue().size() > 0) {
                    stNotify.pstuSubscribes[n].nCode = FinalVar.EVENT_ALARM_LOCALALARM;
                    stNotify.pstuSubscribes[n].emSubCode = EM_EVENT_SUB_CODE.EM_EVENT_SUB_CODE_UNKNOWN;
                    stNotify.pstuSubscribes[n].nChnNum = entry.getValue().size();
                    for (int i = 0; i < entry.getValue().size(); i++) {
                        stNotify.pstuSubscribes[n].nIndexs[i] = entry.getValue().get(i);
                    }
                    n++;
                }
            } else if (entry.getKey().equals("AlarmIPC")) {
                if (entry.getValue() != null && entry.getValue().size() > 0) {
                    stNotify.pstuSubscribes[n].nCode = FinalVar.EVENT_IVS_ALARM_IPC;
                    stNotify.pstuSubscribes[n].emSubCode = EM_EVENT_SUB_CODE.EM_EVENT_SUB_CODE_UNKNOWN;
                    stNotify.pstuSubscribes[n].nChnNum = entry.getValue().size();
                    for (int i = 0; i < entry.getValue().size(); i++) {
                        stNotify.pstuSubscribes[n].nIndexs[i] = entry.getValue().get(i);
                    }
                    n++;
                }
            }
        }

        stNotify.nSubScribeMax = n;
        Log.i("info", "num1=" + stNotify.nSubScribeMax);
        Integer stuErr = new Integer(0);
        Integer stuRes = new Integer(0);
        bRet = INetSDK.SetMobileSubscribe(mLoginHandle, stNotify, stuErr, stuRes, 5000);

        if (!bRet) {
            Log.i("info", "SetMobilePushNotify failed", null);
        }

        return bRet;
    }

    /**
     * unSubscribe Alarm
     * @return
     */
    public boolean unSubscribeDeviceAlarm(String registerId) {
        if (mLoginHandle == 0 || registerId == null ) {
            return false;
        }

        if (registerId.length() < 0 || registerId.equals("")) {
            return false;
        }

        NET_MOBILE_PUSH_NOTIFY_DEL stIn = new NET_MOBILE_PUSH_NOTIFY_DEL();
        StringToByteArray(registerId, stIn.szRegisterID);
        NET_OUT_DELETECFG stOut = new NET_OUT_DELETECFG();
        boolean bRet = INetSDK.DelMobileSubscribe(mLoginHandle, stIn, stOut, 5000);
        if (!bRet) {
            ToolKits.writeErrorLog("DelMobileSubscribe failed");
        } else {
            ToolKits.writeLog("DelMobileSubscribe Succeed!");
        }

        return bRet;
    }

    /**
     * Copy Strings data to Byte Array data
     * @param str
     * @param bytes
     */
    private void StringToByteArray(String str, byte[] bytes) {
        System.arraycopy(str.getBytes(), 0, bytes, 0, str.getBytes().length);
    }
}
