package com.dahuatech.netsdk.activity;
import android.graphics.Color;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.company.NetSDK.NET_TIME;
import com.dahuatech.netsdk.R;
import com.dahuatech.netsdk.common.ToolKits;
import com.dahuatech.netsdk.module.LivePreviewModule;
import com.dahuatech.netsdk.module.PlayBackModule;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PlaybackActivity extends AppCompatActivity implements PlayBackModule.OnPlayBackCallBack,
        View.OnClickListener,DataTimePicker.OnDateTimeChangeed,AdapterView.OnItemSelectedListener {
    private static final String TAG = PlaybackActivity.class.getSimpleName();
    PlayBackModule mPlayBackModule;
    SurfaceView mSurface;
    TextView mStartTimeTv;
    DataTimePicker picker;
    SeekBar mSeekbar;
    TextView mCurrentOSDTime;
    boolean isPlaying = false;
    String[] mPlaySpeed;
    private final int NORMAL_SPEED_INDEX = 2;
    int CURRENT_SPEED_INDEX = NORMAL_SPEED_INDEX;
    Spinner mSelectChannel;
    Spinner mSelectStreamType;
    Button mPlayButton;
    Button mStartAndPause;
    private final int INDEX_UPPER = 4;
    private final int INDEX_LOWER = 0;
    private final int UPDATE_PLAYBACK_PROGRESS = 0x25;

    Handler mHandler = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(Message msg) {
            //    Log.d(TAG,"CurrentProgress:"+mSeekbar.getProgress());
            switch (msg.what) {
                case UPDATE_PLAYBACK_PROGRESS:
                    long offset = (long) msg.obj;
                    mSeekbar.setProgress((int) offset);
                    break;
                default:
                    break;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTitle(R.string.activity_function_list_play_back);
        setContentView(R.layout.activity_playback);
        setupView();
        mPlaySpeed = getResources().getStringArray(R.array.play_back_speed);
        mPlayBackModule = new PlayBackModule(this);
        mPlayBackModule.setCallBack(this);
    }

    private void setupView() {
        mSurface = (SurfaceView) findViewById(R.id.play_back_view);
        mPlayButton = ((Button) findViewById(R.id.start_playback));
        mPlayButton.setOnClickListener(this);
        mStartTimeTv = ((TextView) findViewById(R.id.start_time));
        mStartTimeTv.setText(new SimpleDateFormat("yyyy-MM-dd").format(new Date()));
        mStartTimeTv.setOnClickListener(this);
        picker = new DataTimePicker(this, this);
        mSeekbar = (SeekBar) findViewById(R.id.play_back_seekbar);
        mSeekbar.setProgress(0);
        mSeekbar.setOnSeekBarChangeListener(new PlayBackProgress());

        mStartAndPause = ((Button) findViewById(R.id.play_start_pause));
        mStartAndPause.setOnClickListener(this);
        ((Button) findViewById(R.id.play_fast)).setOnClickListener(this);
        ((Button) findViewById(R.id.play_slow)).setOnClickListener(this);
        ((Button) findViewById(R.id.play_normal)).setOnClickListener(this);

        mSelectChannel = (Spinner) findViewById(R.id.select_channel);
        mSelectStreamType = (Spinner) findViewById(R.id.select_stream_type);
        initializeSpinner(mSelectChannel, (ArrayList) new LivePreviewModule(this).getChannelList()).setSelection(0);
        initializeSpinner(mSelectStreamType, (ArrayList) new LivePreviewModule(this).getStreamTypeList(mSelectChannel.getSelectedItemPosition())).setSelection(0);
        mSelectStreamType.setSelected(false);
        mCurrentOSDTime = (TextView) findViewById(R.id.current_osd_time);
    }

    private class PlayBackProgress implements SeekBar.OnSeekBarChangeListener {
        @Override
        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            Log.i(TAG,"onProgressChanged");
            NET_TIME time = cal(progress);
            String hour = "";
            String minute = "";
            String second = "";
            if (time.dwHour<10){
                hour = "0"+time.dwHour;
            }else {
                hour = String.valueOf(time.dwHour);
            }
            if (time.dwMinute<10){
                minute = "0"+time.dwMinute;
            }else {
                minute = String.valueOf(time.dwMinute);
            }
            if (time.dwSecond<10){
                second = "0"+time.dwSecond;
            }else {
                second = String.valueOf(time.dwSecond);
            }
            String temp = hour+ ":" + minute+ ":" + second;
            mCurrentOSDTime.setText(temp);
        }

        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {
            Log.i(TAG,"onStartTrackingTouch");
        }

        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {
            Log.i(TAG,"onStopTrackingTouch");
            int progress = seekBar.getProgress();
            NET_TIME start = cal(progress);
            NET_TIME end = getEndTime(start);
            mPlayBackModule.logTime("Start", start);
            mPlayBackModule.logTime("End", end);
            startPlayBack(start, end);

        }
    }

    private int dip2px(float dpValue) {
        final float scale = getResources().getDisplayMetrics().density;
        return (int)(dpValue*scale+0.5f);
    }

    private NET_TIME cal(int second){
        int h = 0;
        int d = 0;
        int s = 0;
        int temp = second%3600;
        if (second>3600){
            h = second/3600;
            if(temp!=0){
                if (temp>60){
                    d = temp/60;
                    if (temp%60!=0){
                        s = temp%60;
                    }
                }else {
                    s = temp;
                }
            }
        }else {
            d = second/60;
            if(second%60 !=0){
                s = second%60;
            }
        }
        NET_TIME time = getStartTime(mStartTimeTv.getText().toString());
        time.dwHour = h;
        time.dwMinute = d;
        time.dwSecond = s;
        return time;
    }

    //progress callback
    @Override
    public void invoke(long playHandler,final int totalSize,final int downloadSize) {
            Message msg;
            NET_TIME time = mPlayBackModule.getOSDtime();
            if(time !=null) {
                msg = mHandler.obtainMessage(UPDATE_PLAYBACK_PROGRESS);//send message to UI Thread  update seekbar.
                long second = time.dwHour * 60 * 60 + time.dwMinute * 60 + time.dwSecond;
                msg.obj = second;
                mHandler.sendMessage(msg);
            }
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        switch (id){
            case R.id.play_start_pause:
                isPlaying = !isPlaying;
                play(isPlaying, v);
                break;
            case R.id.play_fast:
                playFast();
                break;
            case R.id.play_slow:
                playSlow();
                break;
            case R.id.play_normal:
                playNormal();
                break;
            case R.id.start_playback:
                Button button = (Button)v;
                if (button.getText().equals(getString(R.string.start_play_back))){
                    NET_TIME start = getStartTime(mStartTimeTv.getText().toString());
                    if (start == null) {
                        ToolKits.showMessage(this,getString(R.string.start_time_error));
                    }
                    NET_TIME end = getEndTime(start);
                    startPlayBack(start,end);
                }else if (button.getText().equals(getString(R.string.stop_plby_back))){
                    stopPlayBack();
                }
                break;
            case R.id.start_time:
                if (isPlaying) {
                    ToolKits.showMessage(this, getString(R.string.repick_date_warn));
                    break;
                }
                picker.showAt(R.id.play_start_time,findViewById(R.id.parent));
                break;
            default:
                break;
        }
    }
    private NET_TIME getEndTime(NET_TIME time){
        NET_TIME t = new NET_TIME();
        t.dwYear = time.dwYear;
        t.dwMonth = time.dwMonth;
        t.dwDay = time.dwDay;
        t.dwHour = 23;
        t.dwMinute = 59;
        t.dwSecond = 59;
        return t;
    }
    private NET_TIME getStartTime(String time){
        if (time == null || time.equals(""))
            return null;
        Pattern pattern =  Pattern.compile("[^0-9]");
        Matcher matcher = pattern.matcher(time);
        String[] temp = matcher.replaceAll(" ").split(" ");
        if (temp.length<3)
            return null;
        ToolKits.writeLog(matcher.replaceAll(" "));
        NET_TIME Time = new NET_TIME();
        Time.dwYear = Integer.parseInt(temp[0]);
        Time.dwMonth = Integer.parseInt(temp[1]);
        Time.dwDay = Integer.parseInt(temp[2]);
        Time.dwHour = 00;
        Time.dwMinute = 00;
        Time.dwSecond = 00;
        return Time;
    }

    private void clearSurface(){
        if (mSurface!=null){
            mSurface.setBackgroundColor(Color.BLACK);
        }
    }
    private void play(boolean playing, View v){
        if(!mPlayBackModule.play(playing)) {
            isPlaying = !isPlaying;
            ToolKits.showMessage(this,getString(R.string.operation_failed));
            return;
        }
        if(!playing){
            ((Button)v).setText(R.string.play_back_start);
        }else {
            ((Button)v).setText(R.string.play_back_pause);
        }
    }

    private void playFast(){
        CURRENT_SPEED_INDEX++;
        if (CURRENT_SPEED_INDEX>INDEX_UPPER) {
            CURRENT_SPEED_INDEX = INDEX_UPPER;
            Toast.makeText(this, getString(R.string.fast_upper)+":"+mPlaySpeed[CURRENT_SPEED_INDEX], Toast.LENGTH_SHORT).show();
            return;
        }
        if(mPlayBackModule.playFast()) {
            Toast.makeText(this, mPlaySpeed[CURRENT_SPEED_INDEX], Toast.LENGTH_SHORT).show();
        } else {
            CURRENT_SPEED_INDEX--;
            Toast.makeText(this, getString(R.string.operation_failed), Toast.LENGTH_SHORT).show();
        }
    }
    private void playSlow(){
        CURRENT_SPEED_INDEX--;
        if (CURRENT_SPEED_INDEX < INDEX_LOWER) {
            CURRENT_SPEED_INDEX = INDEX_LOWER;
            Toast.makeText(this, getString(R.string.slow_lower)+":"+mPlaySpeed[CURRENT_SPEED_INDEX], Toast.LENGTH_SHORT).show();
            return;
        }
        if(mPlayBackModule.playSlow()) {
            Toast.makeText(this, mPlaySpeed[CURRENT_SPEED_INDEX], Toast.LENGTH_SHORT).show();
        } else {
            CURRENT_SPEED_INDEX++;
            Toast.makeText(this, getString(R.string.operation_failed), Toast.LENGTH_SHORT).show();
        }
    }
    private void playNormal(){
        if (CURRENT_SPEED_INDEX == NORMAL_SPEED_INDEX)
            return;
        if(mPlayBackModule.playNormal()) {
            CURRENT_SPEED_INDEX = NORMAL_SPEED_INDEX;
            Toast.makeText(this, getResources().getString(R.string.play_back_normal), Toast.LENGTH_SHORT).show();
        } else
            Toast.makeText(this,getString(R.string.operation_failed), Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onDestroy(){
        super.onDestroy();
        mPlayBackModule.release();
        mPlayBackModule = null;
        picker.release();
        picker = null;
    }

    @Override
    public void onDateTimeChangeed(int id, int year, int month, int day) {
        switch (id){
            case R.id.play_start_time:
                mStartTimeTv.setText(String.valueOf(year)+"-"+String.valueOf(month)+"-"+
                        String.valueOf(day));
                break;
            default:
                break;
        }
    }

    private Spinner initializeSpinner(final Spinner spinner, ArrayList array){
        spinner.setOnItemSelectedListener(this);
        spinner.setAdapter(new ArrayAdapter<String>(this,android.R.layout.simple_spinner_dropdown_item,array));
        return spinner;
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        int parentID = parent.getId();
        switch (parentID) {
            case R.id.select_channel:
            case R.id.select_stream_type:
                onChanged();
                break;
            default:
                break;
        }
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }


    private void onChanged(){
        if (isPlaying){
            stopPlayBack();
            Toast.makeText(this,getString(R.string.playback_changed),Toast.LENGTH_SHORT).show();
        }
    }

    private void reset(){
        CURRENT_SPEED_INDEX = NORMAL_SPEED_INDEX;
        isPlaying = false;
        mPlayButton.setText(R.string.start_play_back);
        mStartAndPause.setText(R.string.play_back_start);
    }
    private void stopPlayBack(){
        mPlayBackModule.stopPlayBack();
        reset();
        clearSurface();
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                mSeekbar.setProgress(0);
                mCurrentOSDTime.setText("");
            }
        });
    }

    private void startPlayBack(NET_TIME start, NET_TIME end){
        reset();
        mPlayBackModule.doPlayBack(mSelectStreamType.getSelectedItemPosition(),mSurface,mSelectChannel.getSelectedItemPosition(), start, end,
        new PlayBackModule.OnPlayBackTaskDone() {
            @Override
            public void onTaskDone(boolean result) {
                if (!result){
                    Toast.makeText(PlaybackActivity.this,getString(R.string.play_back_failed),Toast.LENGTH_SHORT).show();
                }else {
                    mSurface.setBackgroundColor(Color.TRANSPARENT);
                    isPlaying = true;
                    mPlayButton.setText(R.string.stop_plby_back);
                    mStartAndPause.setText(R.string.play_back_pause);
                }
            }
        });
    }



}
