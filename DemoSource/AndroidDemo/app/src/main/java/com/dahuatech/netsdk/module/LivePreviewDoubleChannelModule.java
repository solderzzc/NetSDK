package com.dahuatech.netsdk.module;

import android.content.Context;
import android.content.res.Resources;
import android.support.v7.app.AppCompatActivity;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import com.company.NetSDK.CB_fRealDataCallBackEx;
import com.company.NetSDK.INetSDK;
import com.company.PlaySDK.IPlaySDK;
import com.dahuatech.netsdk.R;
import com.dahuatech.netsdk.activity.LivePreviewDoubleChannelActivity;
import com.dahuatech.netsdk.activity.NetSDKApplication;
import com.dahuatech.netsdk.common.ToolKits;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by 29779 on 2017/4/8.
 */
public class LivePreviewDoubleChannelModule {
    private final int CHANNEL_ID_1 = 0;
    private final int CHANNEL_ID_2 = 1;
    private final int STREAM_BUFFER_SIZE = 1024*1024*2;

    Resources res;
    Context mContext;
    NetSDKApplication app;

    HashMap<Long,Integer> handlersMapPorts = new HashMap<Long, Integer>();
    long playHandle_1 = 0;
    long playHandle_2 = 0;
    public LivePreviewDoubleChannelModule(Context context){
        mContext = context;
        res = mContext.getResources();
        app = ((NetSDKApplication)((AppCompatActivity)mContext).getApplication());
    }

    private CB_fRealDataCallBackEx mRealDataCallBackChannelOne;
    private CB_fRealDataCallBackEx mRealDataCallBackChannelTwo;

    public boolean multiPlay_channel1(int streamType,final SurfaceView view){
        final int port = IPlaySDK.PLAYGetFreePort();
        initSurfaceView(port,view);
        if (!openStream(view,port)){
            ToolKits.showMessage(mContext,res.getString(R.string.channel) + CHANNEL_ID_1+1 + ":" + res.getString(R.string.open_stream));
            return false;
        }
        playHandle_1 = INetSDK.RealPlayEx(app.getLoginHandle(),CHANNEL_ID_1,streamType);
        if (0 == playHandle_1){
            ToolKits.showMessage(mContext, res.getString(R.string.channel) + CHANNEL_ID_1+1 + ":" + res.getString(R.string.live_preview_failed));
            return false;
        }
        handlersMapPorts.put(playHandle_1,port);
        mRealDataCallBackChannelOne = new CB_fRealDataCallBackEx() {
            @Override
            public void invoke(long lRealHandle, int dwDataType, byte[] pBuffer, int dwBufSize, int param) {
                if (0==dwDataType){
                    IPlaySDK.PLAYInputData(port,pBuffer,pBuffer.length);
                }
            }
        };
        INetSDK.SetRealDataCallBackEx(playHandle_1, mRealDataCallBackChannelOne, 1);
        return true;
    }

    public boolean multiPlay_channel2(int streamType,final SurfaceView view){
        final int port = IPlaySDK.PLAYGetFreePort();
        initSurfaceView(port,view);
        if(!openStream(view,port)){
            ToolKits.showMessage(mContext,res.getString(R.string.channel) + 2 + ":" + res.getString(R.string.open_stream));
            return false;
        }
        playHandle_2 = INetSDK.RealPlayEx(app.getLoginHandle(),CHANNEL_ID_2,streamType);
        if (0 == playHandle_2){
            ToolKits.showMessage(mContext, res.getString(R.string.channel) + 2 + ":" + res.getString(R.string.live_preview_failed));
            return false;
        }
        handlersMapPorts.put(playHandle_2,port);
        mRealDataCallBackChannelTwo = new CB_fRealDataCallBackEx() {
            @Override
            public void invoke(long lRealHandle, int dwDataType, byte[] pBuffer, int dwBufSize, int param) {
                if (0==dwDataType){
                    IPlaySDK.PLAYInputData(port,pBuffer,pBuffer.length);
                }
            }
        };
        INetSDK.SetRealDataCallBackEx(playHandle_2, mRealDataCallBackChannelTwo, 1);
        return true;
    }

    private boolean openStream(final SurfaceView view,final int port){
        if (IPlaySDK.PLAYOpenStream(port,null,0,STREAM_BUFFER_SIZE) == 0){
            return false;
        }
        boolean result = IPlaySDK.PLAYPlay(port,view) == 0 ? false:true;
        if (!result){
            IPlaySDK.PLAYCloseStream(port);
            return false;
        }
        result = IPlaySDK.PLAYPlaySoundShare(port) == 0?false:true;
        if (!result){
            IPlaySDK.PLAYStop(port);
            IPlaySDK.PLAYCloseStream(port);
            return false;
        }
        return true;
    }

    public void release(){
        if (playHandle_1 !=0) {
            INetSDK.StopRealPlayEx(playHandle_1);
            if (handlersMapPorts.containsKey(playHandle_1)) {
                int port1 = handlersMapPorts.get(playHandle_1);
                IPlaySDK.PLAYStopSoundShare(port1);
                IPlaySDK.PLAYStop(port1);
                IPlaySDK.PLAYCloseStream(port1);
            }
        }

        if (playHandle_2 != 0) {
            INetSDK.StopRealPlayEx(playHandle_2);
            if (handlersMapPorts.containsKey(playHandle_2)) {
                int port2 = handlersMapPorts.get(playHandle_2);
                IPlaySDK.PLAYStopSoundShare(port2);
                IPlaySDK.PLAYStop(port2);
                IPlaySDK.PLAYCloseStream(port2);
            }
        }
        handlersMapPorts.clear();
        handlersMapPorts = null;
        mContext = null;
        app = null;
        playHandle_1 = 0;
        playHandle_2 = 0;
    }

    public void initSurfaceView(final int port, final SurfaceView view){
        view.getHolder().addCallback(new SurfaceHolder.Callback() {
            @Override
            public void surfaceCreated(SurfaceHolder holder) {
                IPlaySDK.InitSurface(port,view);
            }

            @Override
            public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

            }

            @Override
            public void surfaceDestroyed(SurfaceHolder holder) {

            }
        });
    }

    public  boolean isSupportMultyPlay(Context context){
        if (((NetSDKApplication)((LivePreviewDoubleChannelActivity)context).getApplication()).getDeviceInfo().nChanNum < 2)
            return false;
        return true;
    }


}
