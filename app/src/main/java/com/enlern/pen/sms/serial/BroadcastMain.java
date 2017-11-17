package com.enlern.pen.sms.serial;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.enlern.pen.sms.MainActivity;


/**
 * Created by pen on 2017/10/18.
 */

public class BroadcastMain extends BroadcastReceiver {
    private String rec;

    public static RecCallBack callBack;

    public static void setCallBack(RecCallBack callBack) {
        BroadcastMain.callBack = callBack;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        rec = intent.getStringExtra("REC_NODE_DATA");
        if (callBack != null && MainActivity.getBoolean) {
            callBack.nodeRec(rec);
        }
    }

}
