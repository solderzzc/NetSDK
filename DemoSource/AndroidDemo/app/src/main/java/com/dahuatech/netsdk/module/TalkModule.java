package com.dahuatech.netsdk.module;

import android.content.Context;
import android.content.res.Resources;
import android.support.v7.app.AppCompatActivity;
import android.view.Surface;

import com.company.NetSDK.CB_pfAudioDataCallBack;
import com.company.NetSDK.EM_USEDEV_MODE;
import com.company.NetSDK.FinalVar;
import com.company.NetSDK.INetSDK;
import com.company.NetSDK.NET_SPEAK_PARAM;
import com.company.NetSDK.NET_TALK_TRANSFER_PARAM;
import com.company.NetSDK.SDKDEV_TALKFORMAT_LIST;
import com.company.NetSDK.SDK_TALK_CODING_TYPE;
import com.company.PlaySDK.IPlaySDK;
import com.company.PlaySDK.IPlaySDKCallBack;
import com.dahuatech.netsdk.R;
import com.dahuatech.netsdk.activity.NetSDKApplication;
import com.dahuatech.netsdk.activity.TalkActivity;
import com.dahuatech.netsdk.common.ToolKits;

/**
 * Created by 29779 on 2017/4/8.
 */
public class TalkModule {
    SDKDEV_TALKFORMAT_LIST mTalkFormatList = new SDKDEV_TALKFORMAT_LIST();
    public long mTalkHandle = 0;
    boolean mOpenAudioRecord = false;
    boolean mRecordStatus = false;
    int mMode;
    NetSDKApplication app;
    Context mContext;
    Resources res;

    public TalkModule(Context context) {
        this.mContext = context;
        res = mContext.getResources();
        app = ((NetSDKApplication)((AppCompatActivity)mContext).getApplication());
    }


    public boolean startTalk() {
        ///Set talk encode type
        ///设置对讲编码类型
        mTalkFormatList.type[0].encodeType = SDK_TALK_CODING_TYPE.SDK_TALK_PCM;
        mTalkFormatList.type[0].dwSampleRate = 8000;
        mTalkFormatList.type[0].nAudioBit = 16;
        if( ! INetSDK.SetDeviceMode(app.getLoginHandle(), EM_USEDEV_MODE.SDK_TALK_ENCODE_TYPE, mTalkFormatList.type[0]) ) {
            ToolKits.showMessage(mContext,  res.getString(R.string.set_talk_encode_mode) + res.getString(R.string.info_failed));
            return false;
        }

        ///Set server talk mode
        ///设置服务器对讲方式
        if( ! INetSDK.SetDeviceMode(app.getLoginHandle(), EM_USEDEV_MODE.SDK_TALK_SERVER_MODE, null)) {
            ToolKits.showMessage(mContext, res.getString(R.string.set_server_mode) + res.getString(R.string.info_failed));
            return false;
        }

        ///Set talk transfer mode
        ///设置对讲转发模式
        NET_TALK_TRANSFER_PARAM mTalkTransf = new NET_TALK_TRANSFER_PARAM();
        if( ! INetSDK.SetDeviceMode(app.getLoginHandle(), EM_USEDEV_MODE.SDK_TALK_TRANSFER_MODE, mTalkTransf)) {
            ToolKits.showMessage(mContext, res.getString(R.string.set_transfer_mode) + res.getString(R.string.info_failed));
            return false;
        }

        ///Set talk speak param
        ///设置对讲喊话参数
        mMode = ((TalkActivity)mContext).mRadioTalkBtn.isChecked()? 0:1;
        NET_SPEAK_PARAM mSpeakParam = new NET_SPEAK_PARAM();
        mSpeakParam.nSpeakerChannel = 0;
        mSpeakParam.nMode = mMode;

        if( ! INetSDK.SetDeviceMode(app.getLoginHandle(), EM_USEDEV_MODE.SDK_TALK_SPEAK_PARAM, mSpeakParam)) {
            ToolKits.showMessage(mContext, res.getString(R.string.set_speaking_parameter) + res.getString(R.string.info_failed));
            return false;
        } else {
            ToolKits.writeLog("mode : " +  mSpeakParam.nMode);
        }

        ///Start talk
        ///开始对讲
        AudioDataCallBack mAudiaDatacb = new AudioDataCallBack();
        mTalkHandle = INetSDK.StartTalkEx(app.getLoginHandle(), mAudiaDatacb);
        if(0 != mTalkHandle) {
            ///Start audio record
            ///开始音频录音
            boolean bSuccess = StartAudioRecord();
            if(!bSuccess) {
                INetSDK.StopTalkEx(mTalkHandle);
                ToolKits.showMessage(mContext, res.getString(R.string.start_audio_record) + res.getString(R.string.info_failed));
                return false;
            } else {
                mOpenAudioRecord = true;
                mRecordStatus = true;

                if(mMode == 0) {
                    ToolKits.showMessage(mContext, res.getString(R.string.starttalk));
                } else if(mMode == 1){
                    ToolKits.showMessage(mContext, res.getString(R.string.startspeakonly));
                }
            }
        } else {
            if(mMode == 0) {
                ToolKits.showMessage(mContext, res.getString(R.string.talk) + res.getString(R.string.info_failed));
            } else if(mMode == 1){
                ToolKits.showMessage(mContext, res.getString(R.string.speakonly) + res.getString(R.string.info_failed));
            }
            return false;
        }
        return true;
    }


    public boolean stopTalk() {
        if(mOpenAudioRecord) {
            boolean bSuccess = IPlaySDK.PLAYCloseAudioRecord() == 0 ? false : true;
            if(bSuccess) {
                mOpenAudioRecord = false;
                IPlaySDK.PLAYStop(100);
                IPlaySDK.PLAYStop(99);
                IPlaySDK.PLAYStopSoundShare(100);
                IPlaySDK.PLAYStopSoundShare(99);
                IPlaySDK.PLAYCloseStream(100);
                IPlaySDK.PLAYCloseStream(99);
            }
        }

        if(mRecordStatus) {
            mRecordStatus = false;
        }

        if(0 != mTalkHandle) {
            ///Stop audio talk to the device
            ///停止设备的音频对讲
            if(INetSDK.StopTalkEx(mTalkHandle)) {
                mTalkHandle = 0;
                if(mMode == 0) {
                    ToolKits.showMessage(mContext, res.getString(R.string.stoptalk));
                } else if(mMode == 1){
                    ToolKits.showMessage(mContext, res.getString(R.string.stopspeakonly));
                }
            } else {
                return false;
            }
        }

        return true;
    }

    ///Talk callback
    ///对讲回调函数
    public class AudioDataCallBack implements CB_pfAudioDataCallBack
    {
        public void invoke(long lTalkHandle, byte pDataBuf[], byte byAudioFlag)
        {
            ToolKits.writeLog("AudioDataCallBack received " + byAudioFlag);
            if(mTalkHandle == lTalkHandle)
            {
                ///byAudioFlag Audio data home sign, 0:means audio data collected by local audio recording list; 1:means received audio data sent by devie
                ///byAudioFlag 音频数据归属标志, 0:表示是本地录音库采集的音频数据; 1:表示收到的设备发过来的音频数据
                if(1 == byAudioFlag)
                {
                    int nPort = 99;

                    ///You can use PLAY SDK to decode to get PCM and then encode to other formats if you get a uniform formats.
                    ///通过PLAY SDK解码获取PCM，并且如果你获取统一格式，请对其他格式进行编码
                    IPlaySDK.PLAYInputData(nPort, pDataBuf, pDataBuf.length);
                }
            }
        }
    }

    boolean StartAudioRecord()	{
        ///First confirm decode port.SDK_TALK_DEFAULT is 100 port number and then rest is 99 port number.
        ///首先确定解码端口。SDK_TALK_DEFAULT对应端口号是100，其他的对应端口号是99
        int nPort = 99;

        ///Then specify frame length
        ///指定的帧长度
        int nFrameLength = 1024;
        boolean bRet = false;
        ///Then call PLAYSDK library to begin recording audio
        ///调用PLAYSDK库，来开始录制音频
        boolean bOpenRet = IPlaySDK.PLAYOpenStream(nPort,null,0,1024*1024) == 0? false : true;
        if(bOpenRet) {
            boolean bPlayRet = IPlaySDK.PLAYPlay(nPort, (Surface)null) == 0? false : true;
            if(bPlayRet) {
                IPlaySDK.PLAYPlaySoundShare(nPort);
                TestAudioRecord mAudiorecordcallback = new TestAudioRecord();
                boolean bSuccess = IPlaySDK.PLAYOpenAudioRecord(mAudiorecordcallback,mTalkFormatList.type[0].nAudioBit,
                        mTalkFormatList.type[0].dwSampleRate, nFrameLength, 0) == 0? false : true;
                if(bSuccess) {
                    bRet = true;
                    ToolKits.writeLog("nAudioBit = " + mTalkFormatList.type[0].nAudioBit + "\n" + "dwSampleRate = "
                                      + mTalkFormatList.type[0].dwSampleRate + "\n" + "nFrameLength = " + nFrameLength + "\n");
                } else {
                    IPlaySDK.PLAYStopSoundShare(nPort);
                    IPlaySDK.PLAYStop(nPort);
                    IPlaySDK.PLAYCloseStream(nPort);
                }
            } else {
                IPlaySDK.PLAYCloseStream(nPort);
            }
        }

        return bRet;
    }

    public class TestAudioRecord implements IPlaySDKCallBack.pCallFunction {
        public void invoke(byte[] pDataBuffer,int nBufferLen, long pUserData) {
            try
            {
                ///encode
                ///编码
                ToolKits.writeLog("AudioRecord send " + nBufferLen);
                byte encode[] = AudioRecord(pDataBuffer);

                ///send user's audio data to device.
                ///发送语音数据到设备.
                long lSendLen = INetSDK.TalkSendData(mTalkHandle, encode);
                if(lSendLen != (long)encode.length) {
                    ///Error occurred when sending the user audio data to the device.
                    ///发送音频数据给设备失败.
                }
            } catch(Exception e) {
                e.printStackTrace();
            }
        }
    }

    byte[] AudioRecord(byte[] pDataBuffer) {
        int DataLength = pDataBuffer.length;
        byte pCbData[] = null;
        pCbData = new byte[8+DataLength];

        pCbData[0] = (byte) 0x00;
        pCbData[1] = (byte) 0x00;
        pCbData[2] = (byte) 0x01;
        pCbData[3] = (byte) 0xF0;
        pCbData[4] = (byte) 0x0C;

        pCbData[5] = 0x02; // 8k
        pCbData[6]=(byte)(DataLength & 0x00FF);
        pCbData[7]=(byte)(DataLength >> 8);
        System.arraycopy(pDataBuffer, 0, pCbData, 8, DataLength);
        return pCbData;
    }

    ///Get talk format list，this demo only use PCM.
    ///获取语音对讲格式列表, 本demo只用到了PCM.
    public void getCodeType() {
        if(!INetSDK.QueryDevState(app.getLoginHandle(), FinalVar.SDK_DEVSTATE_TALK_ECTYPE, mTalkFormatList, 4000)) {
            ToolKits.writeLog("QueryDevState TalkList Failed!");
            return;
        }
    }
}
