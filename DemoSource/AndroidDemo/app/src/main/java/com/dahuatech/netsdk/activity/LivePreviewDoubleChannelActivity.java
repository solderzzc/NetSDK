package com.dahuatech.netsdk.activity;

import android.content.DialogInterface;

import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.SurfaceView;
import android.widget.TextView;

import com.company.NetSDK.SDK_RealPlayType;
import com.dahuatech.netsdk.R;
import com.dahuatech.netsdk.module.LivePreviewDoubleChannelModule;

public class LivePreviewDoubleChannelActivity extends AppCompatActivity{
    LivePreviewDoubleChannelModule mDoubleChannelModule;
    SurfaceView mView1;
    SurfaceView mView2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mDoubleChannelModule = new LivePreviewDoubleChannelModule(this);
        if (!mDoubleChannelModule.isSupportMultyPlay(this)) {
            final AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setMessage(R.string.live_preview_not_support_double_channel);
            builder.setCancelable(false);
            builder.setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                    LivePreviewDoubleChannelActivity.this.finish();
                }
            });
            builder.show();
            return;
        }else {
            setTitle(R.string.activity_function_list_double_channel);
            setContentView(R.layout.activity_double_channel_preview);
            setupView();
            mDoubleChannelModule.multiPlay_channel1(SDK_RealPlayType.SDK_RType_Realplay, mView1);
            mDoubleChannelModule.multiPlay_channel2(SDK_RealPlayType.SDK_RType_Realplay, mView2);
        }
    }


    private void setupView(){
        mView1 = (SurfaceView)findViewById(R.id.mulity_view_1);
        mView2 = (SurfaceView)findViewById(R.id.mulity_view_2);
        ((TextView)findViewById(R.id.mulity_channel_1)).setText(getResources().getString(R.string.channel)+0);
        ((TextView)findViewById(R.id.mulity_channel_2)).setText(getResources().getString(R.string.channel)+String.valueOf(1));
    }


    @Override
    protected void onDestroy(){
        super.onDestroy();
        if (mDoubleChannelModule!=null)
            mDoubleChannelModule.release();
        mView1 = null;
        mView2 = null;
        mDoubleChannelModule = null;
    }
}
