
package com.monitor;

import android.app.Activity;
import android.content.Intent;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.View.OnClickListener;
import android.widget.Button;

public class SelectChannelNumActivity extends Activity implements OnClickListener {

    private Button mOneItemBtn;
    private Button mFourItemBtn;
    private Button mNineItemBtn;
    
    private static final String TAG = "SelectChannelNumActivity";
    public static final String CHANNEL_NUM_EXTRA = "channel_num_extra";

    protected void onCreate(android.os.Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.select_layout);
        initUI();
    }

    private void initUI() {
        mOneItemBtn = (Button) findViewById(R.id.one_item_button);
        mOneItemBtn.setOnClickListener(this);
        mFourItemBtn = (Button) findViewById(R.id.four_item_button);
        mFourItemBtn.setOnClickListener(this);
        mNineItemBtn = (Button) findViewById(R.id.nine_item_button);
        mNineItemBtn.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {

        int channelNum = 0;
        switch (v.getId()) {
            case R.id.one_item_button:
                channelNum = 1;
                break;
            case R.id.four_item_button:
                channelNum = 4;
                break;
            case R.id.nine_item_button:
                channelNum = 9;
                break;
            default:
                Log.e(TAG, "onClick: wrong button id " + v.getId());
                finish();
        }
        
        Intent intent = new Intent(this, MainActivity.class);
        intent.putExtra(CHANNEL_NUM_EXTRA, channelNum);
        startActivity(intent);
    };
}
