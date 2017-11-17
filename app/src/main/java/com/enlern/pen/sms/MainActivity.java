package com.enlern.pen.sms;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.KeyEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;

import com.enlern.pen.sms.activity.BaseActivity;
import com.enlern.pen.sms.activity.WelcomeActivity;
import com.enlern.pen.sms.adapter.MainRecAdapter;
import com.enlern.pen.sms.base.ActivityManager;
import com.enlern.pen.sms.serial.BroadcastMain;
import com.enlern.pen.sms.storage.SPUtils;
import com.xiandon.wsn.node.NodeInfo;
import com.xiandon.wsn.node.SmsAnalysis;
import com.xiandon.wsn.serial.SerialPortForWsn;
import com.xiandon.wsn.serial.SerialProtocol;

import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.Hashtable;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class MainActivity extends BaseActivity {

    /**
     * 是否在其他类接收广播数据
     */
    public static boolean getBoolean;
    @BindView(R.id.tv_main_light_status)
    TextView tvMainLightStatus;
    @BindView(R.id.tv_main_sos_status)
    TextView tvMainSosStatus;
    @BindView(R.id.tv_title_setting)
    TextView tvTitleSetting;
    @BindView(R.id.tv_title_clean)
    TextView tvTitleClean;

    /*广播*/
    private LocalBroadcastManager broadcastManager;
    private IntentFilter filter;

    @BindView(R.id.sp_serial)
    Spinner spSerial;
    @BindView(R.id.sp_bit)
    Spinner spBit;
    @BindView(R.id.btn_openSerial)
    Button btnOpenSerial;
    @BindView(R.id.recyclerView_show)
    RecyclerView recyclerViewShow;
    @BindView(R.id.tv_public_title)
    TextView tvPublicTitle;
    private String strSerialSel = "";
    private String[] deviceEntries = null;
    private Hashtable<String, String> htSerialToPath = null;
    private String[] serialRates = null;
    private String strSerialPath = "";
    private String strSerialRateSel = "";
    private boolean bSerialIsOpen = false;

    public static ArrayList<byte[]> alFrames;
    final public static int iRcvBufMaxLen = 2048;
    public static int iRcvBufStart = 0;
    public static int iRcvBufLen = 0;
    public static byte[] baRcvBuf = new byte[iRcvBufMaxLen];
    boolean broadCastFlag = false;
    public static Handler mHandler;
    public static SerialPortForWsn mSerialport;

    private Context context;
    private SmsAnalysis analysis;
    private String TAG = "MainActivity";
    private MainRecAdapter adapter;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        context = MainActivity.this;
        initViews();
        setSerialPort();

    }

    private void initViews() {
        WelcomeActivity.sJuin = "2";
        ActivityManager.getInstance().addActivity(this);
        SPUtils.put(context, "JUIN", "2");

        tvTitleClean.setVisibility(View.GONE);
        tvTitleSetting.setVisibility(View.GONE);

        boardCast();
        analysis = new SmsAnalysis(context);
        tvPublicTitle.setText("智慧农业--设置");
        LinearLayoutManager layoutManager = new LinearLayoutManager(MainActivity.this);
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        recyclerViewShow.setLayoutManager(layoutManager);
        adapter = new MainRecAdapter(context);
        recyclerViewShow.setAdapter(adapter);
    }

    /**
     * 广播
     */
    private void boardCast() {
        filter = new IntentFilter("MAIN_REC_DATA_TAG");
        broadcastManager = LocalBroadcastManager.getInstance(context);
        broadcastManager.registerReceiver(new BroadcastMain(), filter);
    }

    private void setBroadCast(String m) {
        Intent intent = new Intent("MAIN_REC_DATA_TAG");
        intent.putExtra("REC_NODE_DATA", m);
        broadcastManager.sendBroadcast(intent);
    }


    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        switch (keyCode) {
            case KeyEvent.KEYCODE_BACK:
                getBoolean = true;
                startActivity(new Intent(context, WelcomeActivity.class));
                break;

            default:
                break;
        }
        return false;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    protected void onResume() {
        super.onResume();
        initViews();
    }

    private void setSerialPort() {
        mHandler = new Handler(new Handler.Callback() {
            @Override
            public boolean handleMessage(Message msg) {
                int msgWhat = msg.what;
                switch (msgWhat) {
                    case 1:
                        break;
                    case 2:
                        if (MainActivity.alFrames != null && MainActivity.alFrames.size() > 0) {
                            String m = SerialProtocol.bytesToHexString(alFrames.get(0));
                            if (m.length() > 100) {
                                m = m.substring(m.length() - 100);
                            }
                            Intent intent = new Intent("MAIN_RETURN_DATA_TAG");
                            intent.putExtra("MAIN_RETURN_DATA", m);

                            try {
                                NodeInfo nodeInfo = analysis.analysis(m);
                                if (nodeInfo != null) {
                                    adapter.addData(0, nodeInfo);
                                    boolean bSave = SPUtils.contains(context, "SAVE" + nodeInfo.getNode_num());
                                    if (!bSave) {
                                        SPUtils.put(context, "SAVE" + nodeInfo.getNode_num(), nodeInfo.getWsn());
                                    }

                                    if (nodeInfo.getNode_num().equals("006032")) {
                                        tvMainLightStatus.setText(nodeInfo.getData_analysis());
                                    }

                                    if (nodeInfo.getNode_num().equals("006039")) {
                                        tvMainSosStatus.setText(nodeInfo.getData_analysis());
                                    }
                                }
                            } catch (IOException e) {
                                e.printStackTrace();
                            } catch (XmlPullParserException e) {
                                e.printStackTrace();
                            }
                                /*广播数据*/
                            setBroadCast(m);
                        }
                        break;
                    case 3:
                        int iLen = msg.arg1;
                        handleSerialData((byte[]) msg.obj, iLen);
                        break;
                    default:
                        break;
                }
                return false;
            }


        });

        mSerialport = new SerialPortForWsn(mHandler);
        deviceEntries = mSerialport.getSerials();
        htSerialToPath = mSerialport.getSerialsToPath();

        ArrayAdapter<String> adaComDevices = new ArrayAdapter<String>(this, R.layout.spinner_bit,
                deviceEntries);
        adaComDevices.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spSerial.setAdapter(adaComDevices);
        spSerial.setOnItemSelectedListener(new Spinner.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                // 选择串口
                strSerialSel = deviceEntries[position];
                // 选择串口路径值
                strSerialPath = htSerialToPath.get(strSerialSel);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                strSerialSel = deviceEntries[0];
            }
        });

        int iRate = 9600;
        serialRates = new String[7];
        for (int i = 0; i < 3; i++) {
            serialRates[i] = String.valueOf(iRate);
            iRate *= 2;
        }
        iRate = 57600;
        for (int i = 3; i < 7; i++) {
            serialRates[i] = String.valueOf(iRate);
            iRate *= 2;
        }

        ArrayAdapter<String> adaComRates = new ArrayAdapter<String>(this, R.layout.spinner_bit,
                serialRates);
        adaComRates.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spBit.setAdapter(adaComRates);
        spBit.setOnItemSelectedListener(new Spinner.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                // 选择波特率
                strSerialRateSel = serialRates[position];
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // 默认波特率
                strSerialRateSel = serialRates[0];
            }
        });

    }

    private void handleSerialData(byte[] buffer, int size) {
        MainActivity.mSerialport.setIsIdle(false);
        int iBufLef = iRcvBufMaxLen - iRcvBufStart - iRcvBufLen;
        if (iBufLef < 0) {
            iRcvBufStart = 0;
            iRcvBufLen = 0;
            iBufLef = iRcvBufMaxLen;
        }
        if (iBufLef < size && iRcvBufStart > 0) {
            for (int i = 0; i < iRcvBufLen; i++) {
                baRcvBuf[i] = baRcvBuf[iRcvBufStart + i];
            }
            iRcvBufStart = 0;
            iBufLef = iRcvBufMaxLen - iRcvBufLen;
        }
        size = (iBufLef < size) ? iBufLef : size;
        int iIdx = iRcvBufStart + iRcvBufLen;
        for (int i = 0; i < size; i++) {
            baRcvBuf[iIdx + i] = buffer[i];
        }
        iRcvBufLen += size;
        SerialProtocol.recvDataLen = iRcvBufLen;

        MainActivity.alFrames = SerialProtocol.ReceiveToQBA(baRcvBuf, iRcvBufStart);
        iRcvBufLen = iRcvBufStart + iRcvBufLen - SerialProtocol.iHandValidIdx;
        iRcvBufStart = SerialProtocol.iHandValidIdx;

        if (MainActivity.alFrames != null && MainActivity.alFrames.size() > 0) {
            if (broadCastFlag) {
            } else {
                Message msg = new Message();
                msg.what = 2;
                MainActivity.mHandler.sendMessage(msg);
            }
        }
        MainActivity.mSerialport.setIsIdle(true);
    }

    @OnClick({R.id.btn_openSerial, R.id.tv_show_clear, R.id.tv_main_light_open, R.id.tv_main_light_close, R.id.tv_main_sos_open, R.id.tv_main_sos_close})
    public void onViewClicked(View view) {
        String wsn = (String) SPUtils.get(context, "SAVE" + "006032", "ll");
        String wsnSos = (String) SPUtils.get(context, "SAVE" + "006039", "ll");
        switch (view.getId()) {
            case R.id.btn_openSerial:
                if (bSerialIsOpen) {
                    mSerialport.closeSerialPort();
                    bSerialIsOpen = false;
                    btnOpenSerial.setText("打开串口");
                } else {
                    try {
                        mSerialport.open(strSerialPath, strSerialRateSel);
                        SPUtils.put(context, "PATH", strSerialPath);
                        SPUtils.put(context, "RATE", strSerialRateSel);
                        bSerialIsOpen = true;
                        btnOpenSerial.setText("关闭串口");

                    } catch (SecurityException e) {

                    } catch (IOException e) {

                    } catch (InvalidParameterException e) {
                    }
                }
                break;
            case R.id.tv_show_clear:
                adapter.clearData();
                break;
            case R.id.tv_main_light_open:
                open(wsn, "0000");
                break;
            case R.id.tv_main_light_close:
                open(wsn, "0001");
                break;
            case R.id.tv_main_sos_open:
                open(wsnSos, "0000");
                break;
            case R.id.tv_main_sos_close:
                open(wsnSos, "0001");
                break;
        }
    }


    private void open(String str, String sStatus) {
        if (str == null || str.length() < 20) {
            return;
        }
        String open = "36" + str.substring(2, 34) + sStatus + str.substring(38, str.length());
        byte[] ff = this.string2byteArrays(open);
        try {
            mSerialport.sendData(ff, 0, ff.length);
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private byte[] string2byteArrays(String s) {
        String ss = s.replace(" ", "");
        int string_len = ss.length();
        int len = string_len / 2;
        if (string_len % 2 == 1) {
            ss = "0" + ss;
            ++string_len;
            ++len;
        }

        byte[] a = new byte[len];

        for (int i = 0; i < len; ++i) {
            a[i] = (byte) Integer.parseInt(ss.substring(2 * i, 2 * i + 2), 16);
        }

        return a;
    }


}
