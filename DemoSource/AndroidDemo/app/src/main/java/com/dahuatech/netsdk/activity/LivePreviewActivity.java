package com.dahuatech.netsdk.activity;

import android.content.Context;
import android.content.DialogInterface;
import android.app.Dialog;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.method.DigitsKeyListener;
import android.text.method.KeyListener;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.company.NetSDK.SDK_EXTPTZ_ControlType;
import com.company.NetSDK.SDK_PTZ_ControlType;
import com.dahuatech.netsdk.R;
import com.dahuatech.netsdk.common.DialogProgress;
import com.dahuatech.netsdk.common.ToolKits;
import com.dahuatech.netsdk.module.LivePreviewModule;
import java.util.ArrayList;

public class LivePreviewActivity extends AppCompatActivity implements
        SurfaceHolder.Callback,
        AdapterView.OnItemSelectedListener,
        View.OnClickListener{
    private final String TAG = LivePreviewActivity.class.getSimpleName();
    Spinner mSelectStream;
    Spinner mSelectChannel;
    Spinner mEncodeMode;
    Spinner mEncodeResolve;
    Spinner mEncodeFps;
    Spinner mEncodeBitRate;
    SurfaceView mRealView;
    EditText mEditText;
    View mPtzControlLayoutView;
    LivePreviewModule mLiveModule;
    AlertDialog.Builder builder;

    private boolean isRecord = false;
    private int count = 0;
    ///touch time.
    ///触摸时间.
    long mTouchStartTime = 0;
    long mTouchMoveTime = 0;

    ///single touch.
    ///单点触摸.
    float mSingleTouchStart_x = 0;
    float mSingleTouchStart_y = 0;
    float mSingleTouchEnd_x = 0;
    float mSingleTouchEnd_y = 0;

    ///double touch.
    ///两点触摸.
    float mDoubleTouchStart_x1 = 0;
    float mDoubleTouchStart_y1 = 0;
    float mDoubleTouchStart_x2 = 0;
    float mDoubleTouchStart_y2 = 0;
    float mDoubleTouchEnd_x1 = 0;
    float mDoubleTouchEnd_y1 = 0;
    float mDoubleTouchEnd_x2 = 0;
    float mDoubleTouchEnd_y2 = 0;
    Button  mEncodeBtn = null;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_live_preview);
        mLiveModule = new LivePreviewModule(this);
        setTitle(R.string.activity_function_list_live_preview);

        builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.ptz_control_fragment_info);
        builder.setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        builder.show();
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);
        setupView();
    }

    private void setupView(){
        mSelectStream = (Spinner)findViewById(R.id.select_stream_type);
        mSelectChannel = (Spinner)findViewById(R.id.select_channel);
        mRealView = (SurfaceView)findViewById(R.id.real_view);
        mRealView.getHolder().addCallback(this);
        initializeSpinner(mSelectChannel,(ArrayList)mLiveModule.getChannelList()).setSelection(0);
        initializeSpinner(mSelectStream,(ArrayList)mLiveModule.getStreamTypeList(mSelectChannel.getSelectedItemPosition())).setSelection(1);

        ((Button)findViewById(R.id.preview_ptz_control)).setOnClickListener(this);
        mPtzControlLayoutView = (View)findViewById(R.id.ptz_control);
        mEditText = (EditText)mPtzControlLayoutView.findViewById(R.id.edittext_preset);

        ///Only limit to use number
        ///只允许输入数字
        KeyListener keyListener = new DigitsKeyListener(false, false);
        mEditText.setKeyListener(keyListener);

        ((Button) mPtzControlLayoutView.findViewById(R.id.preview_focus_add)).setOnClickListener(this);
        ((Button) mPtzControlLayoutView.findViewById(R.id.preview_focus_dec)).setOnClickListener(this);
        ((Button) mPtzControlLayoutView.findViewById(R.id.preview_aperture_add)).setOnClickListener(this);
        ((Button) mPtzControlLayoutView.findViewById(R.id.preview_aperture_dec)).setOnClickListener(this);
        ((Button) mPtzControlLayoutView.findViewById(R.id.preview_setpreset)).setOnClickListener(this);
        ((Button) mPtzControlLayoutView.findViewById(R.id.preview_clearpreset)).setOnClickListener(this);
        ((Button) mPtzControlLayoutView.findViewById(R.id.preview_gotopreset)).setOnClickListener(this);

        ((Button)findViewById(R.id.preview_remote_snapPic)).setOnClickListener(this);
        mEncodeBtn = ((Button)findViewById(R.id.preview_encode));
        mEncodeBtn.setOnClickListener(this);
        ((Button)findViewById(R.id.preview_record)).setOnClickListener(this);
    }

    @Override
    protected void onResume(){
        super.onResume();
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        mLiveModule.initSurfaceView(mRealView);
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {

    }

    @Override
    protected void onDestroy(){
        mLiveModule.stopRealPlay();
        mLiveModule = null;
        mRealView = null;
        super.onDestroy();
    }
	
    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        int parentID = parent.getId();
        boolean isMain = isMainStream();

        ///Close the listener event when not triggered.
        ///关闭未触发时的监听事件.
        if((count == 0)&&(position == 0)) {
            count ++;
            return;
        }

        switch (parentID){
            case R.id.select_channel:
                onChannelChanged(position);
                break;
            case R.id.select_stream_type:
                onStreamTypeChanged(position);
                break;
            case R.id.compress_fromat_spinner:
                onUpdateMode(((TextView)view).getText().toString(),isMain);
                break;
            case R.id.resolve_spinner:
                onUpdateResolve(((TextView)view).getText().toString(),isMain);
                break;
            case R.id.frame_rate_spinner:
                onUpdateFps(position,isMain);
                break;
            case R.id.bit_rate_spinner:
                onUpdateBitRate(((TextView)view).getText().toString(),isMain);
                break;
            default:
                break;
        }
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }

    private void onChannelChanged(int pos){
        if (mLiveModule == null)
            return;
        mLiveModule.stopRealPlay();
        mLiveModule.startPlay(pos,mSelectStream.getSelectedItemPosition(),mRealView);
    }

    private void onStreamTypeChanged(int position){
        if (mLiveModule == null)
            return;
        mLiveModule.stopRealPlay();
        mLiveModule.startPlay(mSelectChannel.getSelectedItemPosition(),position,mRealView);
    }

    @Override
    public void onClick(View v) {
        String text = mEditText.getText().toString();
        switch (v.getId()){
            case R.id.preview_ptz_control:
                if(mPtzControlLayoutView.isShown()) {
                    mPtzControlLayoutView.setVisibility(View.GONE);
                } else  {
                    mPtzControlLayoutView.setVisibility(View.VISIBLE);
                }
                break;
            case R.id.preview_remote_snapPic:
                if(mPtzControlLayoutView.isShown()) {
                    mPtzControlLayoutView.setVisibility(View.GONE);
                }
                mLiveModule.snap(mSelectChannel.getSelectedItemPosition());
                break;
            case R.id.preview_encode:
                if(mPtzControlLayoutView.isShown()) {
                    mPtzControlLayoutView.setVisibility(View.GONE);
                }
                mEncodeBtn.setEnabled(false);
                onEncode();
                break;
            case R.id.preview_record:
                if(mPtzControlLayoutView.isShown()) {
                    mPtzControlLayoutView.setVisibility(View.GONE);
                }
                isRecord = !isRecord;
               onRecord(v, isRecord);
                break;
            case R.id.preview_focus_add:
                mLiveModule.ptzControlEx(mSelectChannel.getSelectedItemPosition(), SDK_PTZ_ControlType.SDK_PTZ_FOCUS_ADD_CONTROL,(byte)8);
                break;
            case R.id.preview_focus_dec:
                mLiveModule.ptzControlEx(mSelectChannel.getSelectedItemPosition(), SDK_PTZ_ControlType.SDK_PTZ_FOCUS_DEC_CONTROL,(byte)8);
                break;
            case R.id.preview_aperture_add:
                mLiveModule.ptzControlEx(mSelectChannel.getSelectedItemPosition(), SDK_PTZ_ControlType.SDK_PTZ_APERTURE_ADD_CONTROL, (byte)8);
                break;
            case R.id.preview_aperture_dec:
                mLiveModule.ptzControlEx(mSelectChannel.getSelectedItemPosition(), SDK_PTZ_ControlType.SDK_PTZ_APERTURE_DEC_CONTROL, (byte)8);
                break;
            case R.id.preview_setpreset:
                if(!text.equals("")) {
                    mLiveModule.ptzControlEx(mSelectChannel.getSelectedItemPosition(), SDK_PTZ_ControlType.SDK_PTZ_POINT_SET_CONTROL,
                            (byte)Integer.parseInt(text)) ;
                }else {
                    ToolKits.showMessage(LivePreviewActivity.this, getString(R.string.input_number));
                }
                break;
            case R.id.preview_clearpreset:
                if(!text.equals("")) {
                    mLiveModule.ptzControlEx(mSelectChannel.getSelectedItemPosition(), SDK_PTZ_ControlType.SDK_PTZ_POINT_DEL_CONTROL,
                            (byte)Integer.parseInt(text));
                }else {
                    ToolKits.showMessage(LivePreviewActivity.this, getString(R.string.input_number));
                }
                break;
            case R.id.preview_gotopreset:
                if(!text.equals("")) {
                    mLiveModule.ptzControlEx(mSelectChannel.getSelectedItemPosition(), SDK_PTZ_ControlType.SDK_PTZ_POINT_MOVE_CONTROL,
                            (byte)Integer.parseInt(text));
                } else {
                    ToolKits.showMessage(LivePreviewActivity.this, getString(R.string.input_number));
                }
                break;
            default:
                break;
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        final int mAction = event.getAction();
        int mPointorCount = event.getPointerCount();

        switch(mAction) {
            case MotionEvent.ACTION_DOWN :
                if(mPtzControlLayoutView.isShown()) {
                    mPtzControlLayoutView.setVisibility(View.GONE);
                }

                ///If the input method has already been shown on the window, it is hidden.
                ///如果输入法在窗口上已显示，则隐藏
                InputMethodManager inputManager = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
                inputManager.hideSoftInputFromWindow(mRealView.getWindowToken(), 0);

                mTouchStartTime = System.currentTimeMillis();
                mSingleTouchStart_x = event.getX();
                mSingleTouchStart_y = event.getY();
                break;
            case MotionEvent.ACTION_MOVE :
                mTouchMoveTime = System.currentTimeMillis() - mTouchStartTime;
                int mHistorySize = event.getHistorySize();
                if(mHistorySize == 0) {
                    return true;
                }

                if((mPointorCount == 1) && (mTouchMoveTime > 300)){
                    mSingleTouchEnd_x = event.getX();
                    mSingleTouchEnd_y = event.getY();

                    float mSingleTouchValue_x = mSingleTouchEnd_x - mSingleTouchStart_x;
                    float mSingleTouchValue_y = mSingleTouchEnd_y - mSingleTouchStart_y;

                    float mDeviation = Math.abs(mSingleTouchValue_y/mSingleTouchValue_x);

                    if((mSingleTouchValue_x > 0) && (mDeviation < 0.87)) {
                        return mLiveModule.ptzControl(event, mSelectChannel.getSelectedItemPosition(), SDK_PTZ_ControlType.SDK_PTZ_RIGHT_CONTROL, (byte)0, (byte)8);
                    } else if((mSingleTouchValue_x < 0) && (mDeviation < 0.87)) {
                        return mLiveModule.ptzControl(event, mSelectChannel.getSelectedItemPosition(), SDK_PTZ_ControlType.SDK_PTZ_LEFT_CONTROL, (byte)0, (byte)8);
                    } else if((mSingleTouchValue_y > 0) && (mDeviation > 11.43)) {
                        return mLiveModule.ptzControl(event, mSelectChannel.getSelectedItemPosition(), SDK_PTZ_ControlType.SDK_PTZ_DOWN_CONTROL, (byte)0, (byte)8);
                    } else if((mSingleTouchValue_y < 0) && (mDeviation > 11.43)) {
                        return mLiveModule.ptzControl(event, mSelectChannel.getSelectedItemPosition(), SDK_PTZ_ControlType.SDK_PTZ_UP_CONTROL, (byte)0, (byte)8);
                    } else if((mSingleTouchValue_x < 0) && (mSingleTouchValue_y < 0) && (mDeviation <= 11.43) && (mDeviation >= 0.87)) {
                        return mLiveModule.ptzControl(event, mSelectChannel.getSelectedItemPosition(), SDK_EXTPTZ_ControlType.SDK_EXTPTZ_LEFTTOP, (byte)8, (byte)8);
                    } else if((mSingleTouchValue_x < 0) && (mSingleTouchValue_y > 0) && (mDeviation <= 11.43) && (mDeviation >= 0.87)) {
                        return mLiveModule.ptzControl(event, mSelectChannel.getSelectedItemPosition(), SDK_EXTPTZ_ControlType.SDK_EXTPTZ_LEFTDOWN, (byte)8, (byte)8);
                    } else if((mSingleTouchValue_x > 0) && (mSingleTouchValue_y < 0) && (mDeviation <= 11.43) && (mDeviation >= 0.87)) {
                        return mLiveModule.ptzControl(event, mSelectChannel.getSelectedItemPosition(), SDK_EXTPTZ_ControlType.SDK_EXTPTZ_RIGHTTOP, (byte)8, (byte)8);
                    } else if((mSingleTouchValue_x > 0) && (mSingleTouchValue_y > 0) && (mDeviation <= 11.43) && (mDeviation >= 0.87)) {
                        return mLiveModule.ptzControl(event, mSelectChannel.getSelectedItemPosition(), SDK_EXTPTZ_ControlType.SDK_EXTPTZ_RIGHTDOWN, (byte)8, (byte)8);
                    }

                } else if((mPointorCount == 2) && (mTouchMoveTime > 300)){
                    mDoubleTouchStart_x1 = event.getHistoricalX(0, mHistorySize - 1);
                    mDoubleTouchStart_y1 = event.getHistoricalY(0, mHistorySize - 1);
                    mDoubleTouchStart_x2 = event.getHistoricalX(1, mHistorySize - 1);
                    mDoubleTouchStart_y2 = event.getHistoricalY(1, mHistorySize - 1);

                    mDoubleTouchEnd_x1 = event.getX(0);
                    mDoubleTouchEnd_y1 = event.getY(0);
                    mDoubleTouchEnd_x2 = event.getX(1);
                    mDoubleTouchEnd_y2 = event.getY(1);

                    float mStartDistance_x = mDoubleTouchStart_x2 - mDoubleTouchStart_x1;
                    float mStartDistance_y = mDoubleTouchStart_y2 - mDoubleTouchStart_y1;
                    float mEndDistance_x = mDoubleTouchEnd_x2 - mDoubleTouchEnd_x1;
                    float mEndDistance_y = mDoubleTouchEnd_y2 - mDoubleTouchEnd_y1;

                    float mStartTouchDistance = (float)Math.sqrt(mStartDistance_x * mStartDistance_x + mStartDistance_y * mStartDistance_y);
                    float mEndTouchDistance = (float)Math.sqrt(mEndDistance_x * mEndDistance_x + mEndDistance_y * mEndDistance_y);

                    if(mEndTouchDistance > mStartTouchDistance) {
                        return mLiveModule.ptzControl(event, mSelectChannel.getSelectedItemPosition(), SDK_PTZ_ControlType.SDK_PTZ_ZOOM_ADD_CONTROL, (byte)0, (byte)8);
                    } else if(mEndTouchDistance < mStartTouchDistance) {
                        return mLiveModule.ptzControl(event, mSelectChannel.getSelectedItemPosition(), SDK_PTZ_ControlType.SDK_PTZ_ZOOM_DEC_CONTROL, (byte)0, (byte)8);
                    } else {
                        return false;
                    }
                }

                break;
            case MotionEvent.ACTION_UP :
                break;
            default :
                break;
        }
        return false;
    }

    private void onEncode(){
        final Dialog dialog = new Dialog(this);
        LayoutInflater inflater = LayoutInflater.from(this);
        dialog.setContentView(inflater.inflate(R.layout.encode_config_dialog,null));
        mEncodeMode = ((Spinner)dialog.findViewById(R.id.compress_fromat_spinner));
        mEncodeResolve =((Spinner)dialog.findViewById(R.id.resolve_spinner)) ;
        mEncodeFps = ((Spinner)dialog.findViewById(R.id.frame_rate_spinner));
        mEncodeBitRate = ((Spinner)dialog.findViewById(R.id.bit_rate_spinner));
        mLiveModule.setSpinnerDataCallBack(new LivePreviewModule.SpinnerDataCallback() {
            @Override
            public void onSetSpinner(Bundle data, DialogProgress dhdialog) {
                if (data == null)
                    return;
                initializeSpinner(mEncodeMode,data.getStringArrayList(mLiveModule.MODE)).
                        setSelection(data.getInt(mLiveModule.MODE_POS),true);
                initializeSpinner(mEncodeResolve,data.getStringArrayList(mLiveModule.RESOLUTION))
                        .setSelection(data.getInt(mLiveModule.RESOLUTION_POS),true);
                initializeSpinner(mEncodeFps,data.getStringArrayList(mLiveModule.FPS)).
                        setSelection(data.getInt(mLiveModule.FPS_POS),true);
                initializeSpinner(mEncodeBitRate,data.getStringArrayList(mLiveModule.BITRATE)).
                        setSelection(data.getInt(mLiveModule.BITRATE_POS),true);
                dialog.show();
                if (dhdialog != null && dhdialog.isShowing())
                    dhdialog.dismiss();
            }
        });

        // get encode of device
        mLiveModule.getEncodeData(mSelectChannel.getSelectedItemPosition(), isMainStream());
        ((Button)dialog.findViewById(R.id.encode_setting_config_btn)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
                if(mLiveModule.setEncodeConfig(mSelectChannel.getSelectedItemPosition())){
                    ToolKits.showMessage(LivePreviewActivity.this,getString(R.string.encode_set_success));
                }else {
                    ToolKits.showMessage(LivePreviewActivity.this,getString(R.string.encode_set_failed));
                }
            }
        });
        dialog.setCanceledOnTouchOutside(true);
        dialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                mEncodeBtn.setEnabled(true);
            }
        });
    }

    private void onUpdateMode(String text,boolean isMainStream){
        mLiveModule.updateMode(mSelectChannel.getSelectedItemPosition(),text,isMainStream);
        ((ArrayAdapter)mEncodeResolve.getAdapter()).notifyDataSetChanged();
        ((ArrayAdapter)mEncodeFps.getAdapter()).notifyDataSetChanged();
        ((ArrayAdapter)mEncodeBitRate.getAdapter()).notifyDataSetChanged();
    }
    private void onUpdateResolve(String text,boolean isMainStream){
        mLiveModule.updateResolve(mSelectChannel.getSelectedItemPosition(),text,isMainStream);
        ((ArrayAdapter)mEncodeFps.getAdapter()).notifyDataSetChanged();
        ((ArrayAdapter)mEncodeBitRate.getAdapter()).notifyDataSetChanged();
    }
    private void onUpdateFps(int pos,boolean isMainStream){
        mLiveModule.updateFps(mSelectChannel.getSelectedItemPosition(),pos,isMainStream);
        ((ArrayAdapter)mEncodeBitRate.getAdapter()).notifyDataSetChanged();
    }
    private void onUpdateBitRate(String value,boolean isMainStream){
        mLiveModule.updateBitRate(value,isMainStream);
    }

    private Spinner initializeSpinner(final Spinner spinner, ArrayList array){
        spinner.setSelection(0,true);
        spinner.setOnItemSelectedListener(this);
        spinner.setAdapter(new ArrayAdapter<String>(this,android.R.layout.simple_spinner_dropdown_item,array));
        return spinner;
    }

    private void onRecord(View v, boolean recordFlag){
        if( mLiveModule.record(recordFlag)){
            if(recordFlag){
                ((Button)v).setText(R.string.stop_record);
            }else {
                ((Button)v).setText(R.string.start_record);
            }
        }
    }

    private boolean isMainStream(){
       return mSelectStream.getSelectedItemPosition() == 0 ? true : false;
    }

}
