package com.dahuatech.netsdk.activity;

import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.dahuatech.netsdk.R;
import com.dahuatech.netsdk.common.ToolKits;

import java.util.ArrayList;
import java.util.List;

public class FunctionListActivity extends AppCompatActivity {

    private AdapterView.OnItemClickListener mListener = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            switch (position) {
                case 0:
                    startActivity(LivePreviewActivity.class);
                    break;
                case 1:
                    startActivity(LivePreviewDoubleChannelActivity.class);
                    break;
                case 2:
                    startActivity(PlaybackActivity.class);
                    break;
                case 3:
                    startActivity(TalkActivity.class);
                    break;
                case 4:
                    startActivity(AlarmListenActivity.class);
                    break;
                case 5:
                    startActivity(AlarmPushActivity.class);
                    break;
                case 6:
                    startActivity(AlarmControlActivity.class);
                    break;
                case 7:
                    startActivity(DeviceControlActivity.class);
                    break;
                case 8:
                    startActivity(FileBrowserActivity.class);
                    break;
                default:
                    ToolKits.writeLog("onListItemClick: " + position);
                    break;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_function_list);
        setTitle(R.string.title_function_list);
        setupView();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    private void setupView() {
        ListView listView = (ListView) findViewById(R.id.listView);
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
                R.layout.function_list_row,
                R.id.line1,
                getListItems(getResources())
        );

        listView.setOnItemClickListener(mListener);
        listView.setAdapter(adapter);
    }

    private List<String> getListItems(Resources res) {
        List<String> data = new ArrayList<String>();
        data.add(res.getString(R.string.activity_function_list_live_preview));
        data.add(res.getString(R.string.activity_function_list_double_channel));
        data.add(res.getString(R.string.activity_function_list_play_back));
        data.add(res.getString(R.string.activity_function_list_talk));
        data.add(res.getString(R.string.activity_function_list_alarm_listen));
        data.add(res.getString(R.string.activity_function_list_alarm_push));
        data.add(res.getString(R.string.activity_function_list_alarm_control));
        data.add(res.getString(R.string.activity_function_list_device_control));
        data.add(res.getString(R.string.function_list_files_browser));
        return data;
    }

    private void startActivity(final Class cls){
        startActivity(new Intent(this,cls));
    }

}
