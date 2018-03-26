package com.dahuatech.netsdk.activity;

import android.content.res.Resources;
import android.graphics.Color;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import com.company.NetSDK.CB_fSearchDevicesCB;
import com.company.NetSDK.DEVICE_NET_INFO_EX;
import com.dahuatech.netsdk.R;
import com.dahuatech.netsdk.module.WIFIConfigurationModule;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

public class WIFIConfigurationActivity extends AppCompatActivity implements View.OnClickListener{
    boolean mSearchFlag = false;
    EditText mSnEditText;
    EditText mSsidEditText;
    EditText mPwdEditText;
    ListView mInforLv;
    Button mSearchDeviceBtn;
    WIFIConfigurationModule mConfigModule;
    final Set<String> inforSet = new HashSet<String>();
    Resources res;
    private final int UPDATE_SEARCH_DEV_INFOR = 0x10;

    Handler mHandler = new Handler(Looper.getMainLooper()){
        @Override
        public void handleMessage(Message msg){
            switch (msg.what){
                case UPDATE_SEARCH_DEV_INFOR:
                    Log.i("SearchDevMessage","infor:"+(String)msg.obj);
                    ((InforAdapter)mInforLv.getAdapter()).insertInfor((String)msg.obj);
                    break;
                default:
                    break;
            }
      }
    };

    private CB_fSearchDevicesCB callback = new  CB_fSearchDevicesCB(){

        @Override
        public void invoke(DEVICE_NET_INFO_EX device_net_info_ex) {
            String temp = res.getString(R.string.activity_iplogin_device_ip) + " : "+ new String(device_net_info_ex.szIP).trim() + "\n" +
                          res.getString(R.string.activity_p2plogin_device_id) + " : " + new String(device_net_info_ex.szSerialNo).trim();

            ///Filter repeated and only show IPV4
            ///过滤重复的以及只显示IPV4
            if((!inforSet.contains(temp)) && (device_net_info_ex.iIPVersion == 4)){
                inforSet.add(temp);
                Message msg = mHandler.obtainMessage(UPDATE_SEARCH_DEV_INFOR);
                msg.obj = temp;
                mHandler.sendMessage(msg);
            }
        }
    };
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wificonfiguration);
        setTitle(R.string.activity_main_wifi_config);
        mConfigModule = new WIFIConfigurationModule(this);
        res = this.getResources();
        setupView();
    }

    private void setupView(){
        mSnEditText = (EditText)findViewById(R.id.sn_et);
        mSsidEditText = (EditText)findViewById(R.id.ssid_et);
        mPwdEditText = (EditText)findViewById(R.id.pwd_et);
        mInforLv = (ListView)findViewById(R.id.device_search_list);
        ((Button)findViewById(R.id.config_start)).setOnClickListener(this);
        mSearchDeviceBtn = (Button)findViewById(R.id.device_search_button);
        mSearchDeviceBtn.setOnClickListener(this);
        mInforLv.setAdapter(new InforAdapter());
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.config_start){
            mConfigModule.configIPCWifi(mSnEditText.getText().toString(),
                                        mSsidEditText.getText().toString(),
                                        mPwdEditText.getText().toString());

        } else if(id == R.id.device_search_button){
            if(!mSearchFlag) {
                ((InforAdapter)mInforLv.getAdapter()).clean();
                inforSet.clear();
                if(mConfigModule.StartSearchDevices(callback)) {
                    mSearchFlag = true;
                    mSearchDeviceBtn.setText(R.string.config_stopsearchdevice);
                }
            } else {
                mConfigModule.StopSearchDevices();
                mSearchFlag = false;
                mSearchDeviceBtn.setText(R.string.config_startsearchdevice);
            }
        }
    }

    private class InforAdapter extends BaseAdapter {
       private ArrayList<String> list = new ArrayList<String>();
        public InforAdapter(){
            if (list == null)
                list = new ArrayList<String>();
        }
        public void insertInfor(String  i){
            this.list.add(i);
            this.notifyDataSetChanged();
            mInforLv.setSelection(this.list.size()-1);
        }
        public void clean(){
            this.list.clear();
            this.notifyDataSetChanged();
        }
        @Override
        public int getCount() {
            return this.list.size();
        }

        @Override
        public Object getItem(int position) {
            return null;
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            TextView temp = null;
            if(temp == null) {
                temp = new TextView(WIFIConfigurationActivity.this);
                AbsListView.LayoutParams params = new AbsListView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                temp.setLayoutParams(params);
                temp.setTextColor(Color.BLACK);
            }
            temp.setText(list.get(position));
            return temp;
        }
    };

    @Override
    protected void onDestroy(){
        mConfigModule.StopSearchDevices();
        mConfigModule.lDevSearchHandle = 0;
        mConfigModule = null;
        super.onDestroy();
    }
}
