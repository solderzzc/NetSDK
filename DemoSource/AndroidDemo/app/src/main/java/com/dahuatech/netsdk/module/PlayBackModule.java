package com.dahuatech.netsdk.module;

import android.content.Context;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import com.company.NetSDK.CB_fDataCallBack;
import com.company.NetSDK.CB_fDownLoadPosCallBack;
import com.company.NetSDK.EM_USEDEV_MODE;
import com.company.NetSDK.INetSDK;
import com.company.NetSDK.NET_RECORD_TYPE;
import com.company.NetSDK.NET_TIME;
import com.company.NetSDK.SDK_RealPlayType;
import com.company.PlaySDK.Constants;
import com.company.PlaySDK.IPlaySDK;
import com.dahuatech.netsdk.R;
import com.dahuatech.netsdk.activity.NetSDKApplication;
import com.dahuatech.netsdk.common.DialogProgress;
import com.dahuatech.netsdk.common.ToolKits;

/**
 * Created by 36141 on 2017/5/11.
 */
public class PlayBackModule {
    private static final String TAG = PlayBackModule.class.getSimpleName();
    private final static int NO_ERROR = 0 ;
    private final int EMPTY_BIT_STREAM_LIBRARY = 1;
    private final int EMPTY_PLAY_QUEUE = 3;
    private final int INVALID_OFFSET_RELATIVE_FILES = 0xFFFFFFFF;
    private final int STREAM_BUFFER_SIZE = 1024*1024*2;
    Context mContext;
    long mLoginHandler = 0;
    volatile long mPlayHandler = 0;   /// 多线程共享，  防止发生线程不同步
    OnPlayBackCallBack mCallBack;
    SurfaceView mSurface;
    volatile int mPort;   ///多线程共享，防止发生线程不同步


    public PlayBackModule(Context context){
        this.mContext = context;
        mLoginHandler = ((NetSDKApplication)((AppCompatActivity)mContext).getApplication()).getLoginHandle();

    }

    ///initialize player
    ///初始化播放控件
    public void initSurfaceView(final int streamType, final SurfaceView sv){
        mPort = IPlaySDK.PLAYGetFreePort();
        int stream = SDK_RealPlayType.SDK_RType_Multiplay;  ///默认主码流。  default main stream.
        if (streamType ==1)
            stream = SDK_RealPlayType.SDK_RType_Realplay_0; ///辅码流   sub stream.
        if (!setDeviceMode(stream,NET_RECORD_TYPE.NET_RECORD_TYPE_ALL)) {
            return;
        }
        this.mSurface = sv;

        sv.getHolder().addCallback(new SurfaceHolder.Callback() {
            @Override
            public void surfaceCreated(SurfaceHolder holder) {
                IPlaySDK.InitSurface(mPort,sv);
                Log.d(TAG,"surfaceCreated");
            }

            @Override
            public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
                Log.d(TAG,"surfaceChanged");
            }

            @Override
            public void surfaceDestroyed(SurfaceHolder holder) {
                Log.d(TAG,"surfaceDestroyed");
            }
        });
        initPlayer(this.mSurface);
    }
    public boolean setDeviceMode(int streamType,int recordType){
        if(!INetSDK.SetDeviceMode(mLoginHandler, EM_USEDEV_MODE.SDK_RECORD_STREAM_TYPE,streamType))
            return false;
        if( !INetSDK.SetDeviceMode(mLoginHandler, EM_USEDEV_MODE.SDK_RECORD_TYPE, recordType))
            return false;
        return true;
    }

    public int startPlayBack(int channelID, NET_TIME startTime,NET_TIME endTime){
        if (mLoginHandler == 0)
            return -1;
        logTime("startTime", startTime);
        logTime("endTime", endTime);
        mPlayHandler = INetSDK.PlayBackByTimeEx(mLoginHandler, channelID, startTime, endTime,
                                                    this.mCallBack, new CB_fDataCallBack() {
            @Override
            public int invoke(long l, int i, byte[] buffers, int i1) {
                return IPlaySDK.PLAYInputData(mPort, buffers, buffers.length);
            }
        });

        if(mPlayHandler == 0)
            return (INetSDK.GetLastError()&0x7fffffff);
        IPlaySDK.PLAYSetPlayedTimeEx(mPort,0);
        IPlaySDK.PLAYResetBuffer(mPort,EMPTY_BIT_STREAM_LIBRARY);
        IPlaySDK.PLAYResetBuffer(mPort,EMPTY_PLAY_QUEUE);
        return NO_ERROR;
    }
    public void logTime(String head , NET_TIME time){
        Log.i(TAG,head+" Year:"+time.dwYear+";Month:"+time.dwMonth+";Day:"+time.dwDay+
                ";Hour:"+time.dwHour+";Minute:"+time.dwMinute+";Second:"+time.dwSecond);
    }

    ///
    /// 播放器控制函数，play==false表示暂停；  play==true表示
    public boolean play(boolean play){
        if (mPlayHandler == 0)
            return false;
        if(!play){
            if(IPlaySDK.PLAYPause(mPort,(short)1) == 0)
                return false;
        }else {
            if(IPlaySDK.PLAYPause(mPort,(short)0) == 0)
                return false;
        }
         return INetSDK.PausePlayBack(mPlayHandler,play);
    }

    public boolean playFast(){
        if (mPlayHandler == 0)
            return false;
        if (IPlaySDK.PLAYFast(mPort) == 0)
            return false;
        return INetSDK.FastPlayBack(mPlayHandler);
    }

    public boolean playSlow(){
        if (mPlayHandler == 0)
            return false;
        if (IPlaySDK.PLAYSlow(mPort) == 0)
            return false;
        return INetSDK.SlowPlayBack(mPlayHandler);
    }
    public int getPort(){
        return mPort;
    }

    public long getmPlayHandler(){
        return this.mPlayHandler;
    }
    /*
    public boolean onSeekBarChanged(int progress){
        if (INetSDK.SeekPlayBack(getmPlayHandler(),progress,INVALID_OFFSET_RELATIVE_FILES)){
            IPlaySDK.PLAYSetPlayedTimeEx(getPort(),progress*1000);
            IPlaySDK.PLAYResetBuffer(getPort(),EMPTY_BIT_STREAM_LIBRARY);   //清码流分析库
            IPlaySDK.PLAYResetBuffer(getPort(),EMPTY_PLAY_QUEUE);   //清播放队列
            return true;
        }
        return false;
    }
    */
    public boolean playNormal(){
        if (mPlayHandler == 0)
            return false;
        if (IPlaySDK.PLAYPlay(mPort,mSurface) == 0)
            return false;
        return INetSDK.NormalPlayBack(mPlayHandler);
    }

    public void setCallBack(OnPlayBackCallBack callBack){
        this.mCallBack = callBack;
    }
    private boolean initPlayer(final SurfaceView view){
        if (view == null)
            throw new NullPointerException("the parameter Surface is null");
        if (!(IPlaySDK.PLAYOpenStream(mPort,null,0,STREAM_BUFFER_SIZE) == 0 ? false:true))
            return false;
        IPlaySDK.PLAYSetStreamOpenMode(mPort, Constants.STREAME_FILE);
        if(!(IPlaySDK.PLAYPlay(mPort,view) == 0 ? false:true)) {
            IPlaySDK.PLAYCloseStream(mPort);
            return false;
        }
        int result = IPlaySDK.PLAYPlaySoundShare(mPort);
        if (result == 0) {
            IPlaySDK.PLAYStop(mPort);
            IPlaySDK.PLAYCloseStream(mPort);
            return false;
        }
        return true;
    }

    public void stopPlayBack(){
        INetSDK.StopPlayBack(mPlayHandler);
        IPlaySDK.PLAYStop(mPort);
        IPlaySDK.PLAYCloseStream(mPort);
        IPlaySDK.PLAYStopSoundShare(mPort);
        mPort = 0;
        mPlayHandler = 0;
    }
    public void release(){
        stopPlayBack();
        mCallBack = null;
        mLoginHandler = 0;
        mContext = null;
    }

    public interface OnPlayBackCallBack extends CB_fDownLoadPosCallBack {

    }

    public NET_TIME getOSDtime(){
        byte[] rr = new byte[24];  // array size must large than 24 , because it is used to store 6 int. if not , QueryInfo will return false.
        long year = 0;
        long month = 0;
        long day = 0;
        long hour = 0;
        long minute = 0;
        long second = 0;
        Integer gf = new Integer(0);

        if(IPlaySDK.PLAYQueryInfo(mPort, Constants.PLAY_CMD_GetTime, rr, rr.length, gf) != 0) {
            year 	= ((long)(rr[3]  & 0xff) << 24) + ((long)(rr[2]  & 0xff) << 16) + ((long)(rr[1]  & 0xff) << 8) + (long)(rr[0]  & 0xff);
            month 	= ((long)(rr[7]  & 0xff) << 24) + ((long)(rr[6]  & 0xff) << 16) + ((long)(rr[5]  & 0xff) << 8) + (long)(rr[4]  & 0xff);
            day 	= ((long)(rr[11] & 0xff) << 24) + ((long)(rr[10] & 0xff) << 16) + ((long)(rr[9]  & 0xff) << 8) + (long)(rr[8]  & 0xff);
            hour 	= ((long)(rr[15] & 0xff) << 24) + ((long)(rr[14] & 0xff) << 16) + ((long)(rr[13] & 0xff) << 8) + (long)(rr[12] & 0xff);
            minute 	= ((long)(rr[19] & 0xff) << 24) + ((long)(rr[18] & 0xff) << 16) + ((long)(rr[17] & 0xff) << 8) + (long)(rr[16] & 0xff);
            second 	= ((long)(rr[23] & 0xff) << 24) + ((long)(rr[22] & 0xff) << 16) + ((long)(rr[21] & 0xff) << 8) + (long)(rr[20] & 0xff);
            NET_TIME time = new NET_TIME();
            time.dwYear = year;
            time.dwMonth = month;
            time.dwDay = day;
            time.dwHour = hour;
            time.dwMinute = minute;
            time.dwSecond = second;
            return time;
        }
        return null;

    }

    public void doPlayBack(int stream,final SurfaceView sv, int channel, NET_TIME start, NET_TIME end,OnPlayBackTaskDone callback){
        final PlayBackTask task = new PlayBackTask(callback);
        task.execute(stream,sv,channel,start,end);
    }

    private class PlayBackTask extends AsyncTask<Object,Object,Integer>{
        private final DialogProgress dialog = new DialogProgress(mContext);
        private OnPlayBackTaskDone callback;
        public PlayBackTask(OnPlayBackTaskDone done){
            this.callback = done;
        }
        @Override
        protected void onPreExecute(){
            super.onPreExecute();
            dialog.setMessage(mContext.getString(R.string.waiting));
            dialog.setSpinnerType(DialogProgress.FADED_ROUND_SPINNER);
            dialog.setCancelable(false);
            dialog.show();
            stopPlayBack();
        }
        @Override
        protected Integer doInBackground(Object[] params) {
            final int stream = (Integer) params[0];
            final SurfaceView sv = (SurfaceView) params[1];
            final int channel = (Integer) params[2];
            final NET_TIME start = (NET_TIME) params[3];
            final NET_TIME end = (NET_TIME)params[4];
            initSurfaceView(stream, sv);
            final int result = startPlayBack(channel,start,end);
            return result;
        }
        @Override
        protected void onPostExecute(Integer result){
            super.onPostExecute(result);
            boolean r = result == 0 ? true:false;
            if (this.callback!=null){
                this.callback.onTaskDone(r);
            }
            if (dialog!=null && dialog.isShowing()){
                dialog.dismiss();
            }
        }

    }
    public interface  OnPlayBackTaskDone{
        public void onTaskDone(boolean result);
    }

}














