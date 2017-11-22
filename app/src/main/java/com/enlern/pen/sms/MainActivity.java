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
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.enlern.pen.sms.activity.BaseActivity;
import com.enlern.pen.sms.activity.ControlActivity;
import com.enlern.pen.sms.activity.SettingActivity;
import com.enlern.pen.sms.activity.WelcomeActivity;
import com.enlern.pen.sms.adapter.MainRecAdapter;
import com.enlern.pen.sms.base.ActivityManager;
import com.enlern.pen.sms.fragment.IrrigationFragment;
import com.enlern.pen.sms.fragment.ShadeFragment;
import com.enlern.pen.sms.fragment.SprayFragment;
import com.enlern.pen.sms.fragment.VentilationFragment;
import com.enlern.pen.sms.serial.BroadcastMain;
import com.enlern.pen.sms.storage.SPUtils;
import com.suke.widget.SwitchButton;
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
    @BindView(R.id.switch_button)
    SwitchButton switchButton;
    @BindView(R.id.tv_main_sos_local)
    TextView tvMainSosLocal;

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

    private boolean bSos83;
    private boolean bSos89;
    private boolean bSos86;
    private boolean bSos01;

    private int iWhere = 0;


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

        getBoolean = true;

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

        String auto = (String) SPUtils.get(context, "AUTOS", "Manual");
        if (auto.equals("Auto")) {
            switchButton.setChecked(true);
            switchButton.isChecked();
        }
        switchButton.toggle();     //switch state
        switchButton.toggle(true);//switch without animation
        switchButton.setShadowEffect(true);//disable shadow effect
        switchButton.setEnabled(true);//disable button
        switchButton.setEnableEffect(true);//disable the switch animation

        switchButton.setOnCheckedChangeListener(new SwitchButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(SwitchButton view, boolean isChecked) {
                if (isChecked) {
                    SPUtils.put(context, "AUTOS", "Auto");
                } else {
                    SPUtils.put(context, "AUTOS", "Manual");
                }
            }
        });

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
                                if (nodeInfo != null && MainActivity.getBoolean) {
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

                                    checkSos(nodeInfo);// 处理警戒值


                                    writeUtils(m, nodeInfo.getNode_num());// 存储协议
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

    private void writeUtils(String m, String node_num) {
        boolean bWrite = SPUtils.contains(context, "WSN" + node_num);
        if (!bWrite) {
            SPUtils.put(context, "WSN" + node_num, m);
        }
    }


    int count11 = 0;
    int count12 = 0;
    int count13 = 0;

    int count21 = 0;
    int count22 = 0;
    int count23 = 0;

    int count31 = 0;
    int count32 = 0;
    int count33 = 0;

    int count41 = 0;
    int count42 = 0;
    int count43 = 0;

    boolean bAuto = false;

    private void checkSos(NodeInfo nodeInfo) {
        String auto = (String) SPUtils.get(context, "AUTOS", "Manual");
        if (auto.equals("Auto")) {
            bAuto = true;
            tvMainSosLocal.setEnabled(false);
        } else {
            bAuto = false;
            tvMainSosLocal.setEnabled(true);
        }
        switch (nodeInfo.getNode_num()) {
            case "006083":
                bSos83 = SPUtils.contains(context, "S831");
                iWhere = 1;
                if (bSos83) {
                    String dS1 = (String) SPUtils.get(context, "S83L", "20");
                    String dS2 = (String) SPUtils.get(context, "S83H", "60");


                    String[] a = analysis.extractAmountMsg(nodeInfo.getData_analysis());
                    String dD = a[1];

                    String control = "WSN006037";
                    boolean bControl = SPUtils.contains(context, control);
                    if (bControl) {
                        String wsn = (String) SPUtils.get(context, control, "Hello");
                        if (dS1 == null || dD == null || dS2 == null) {
                            return;
                        }
                        if (Double.parseDouble(dD) > Double.parseDouble(dS2)) {
                            // 打开
                            if (bAuto) {
                                count12 = 0;
                                count13 = 0;
                                if (count11 < 2) {
                                    open(wsn, "0001");
                                    count11++;
                                }
                            }
                            tvMainSosLocal.setText("土壤湿度报警--湿度过高");
                            IrrigationFragment.sosTv = "土壤湿度报警--湿度过高";
                        } else if (Double.parseDouble(dD) < Double.parseDouble(dS1)) {
                            // 关闭
                            if (bAuto) {
                                count11 = 0;
                                count13 = 0;
                                if (count12 < 2) {
                                    open(wsn, "0000");
                                    count12++;
                                }
                            }
                            tvMainSosLocal.setText("土壤湿度报警--湿度过低");
                            IrrigationFragment.sosTv = "土壤湿度报警--湿度过低";
                        } else {
                            count11 = 0;
                            count12 = 0;
                            if (count13 < 2) {
                                open(wsn, "0001");
                                count13++;
                                tvMainSosLocal.setText("土壤湿度正常");
                                IrrigationFragment.sosTv = "土壤湿度正常";
                            }
                        }
                    } else {
                        Toast.makeText(context, "控制节点尚未连接", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    SPUtils.put(context, "S831", "20.5");
                    SPUtils.put(context, "S832", "60.5");
                }
                break;
            case "006089":
                bSos89 = SPUtils.contains(context, "S891");
                iWhere = 2;
                if (bSos89) {
                    // 设置警戒值
                    String dS1 = (String) SPUtils.get(context, "S89L", "250");
                    String dS2 = (String) SPUtils.get(context, "S89H", "600");

                    // 实际警戒值
                    String a = nodeInfo.getData_analysis();

                    String control = "WSN006031";
                    boolean bControl = SPUtils.contains(context, control);
                    if (bControl) {
                        String wsn = (String) SPUtils.get(context, control, "Hello");
                        if (Double.parseDouble(a) < Double.parseDouble(dS1)) {
                            // 关闭
                            count22 = 0;
                            count23 = 0;
                            if (bAuto) {
                                if (count21 < 2) {
                                    open(wsn, "0001");
                                    count21++;
                                }
                            }
                            tvMainSosLocal.setText("CO2报警--CO2浓度偏低");
                            VentilationFragment.sosTv = "CO2报警--CO2浓度偏低";
                        } else if (Double.parseDouble(a) > Double.parseDouble(dS2)) {
                            // 打开
                            count23 = 0;
                            count21 = 0;
                            if (bAuto) {
                                if (count22 < 2) {
                                    open(wsn, "0000");
                                    count22++;
                                }
                            }
                            tvMainSosLocal.setText("CO2报警--CO2浓度偏高");
                            VentilationFragment.sosTv = "CO2报警--CO2浓度偏高";
                        } else {
                            count22 = 0;
                            count21 = 0;
                            if (bAuto) {
                                if (count23 < 2) {
                                    open(wsn, "0001");
                                    count23++;
                                }
                            }
                            tvMainSosLocal.setText("CO2浓度正常");
                            VentilationFragment.sosTv = "CO2浓度正常";
                        }
                    } else {
                        Toast.makeText(context, "控制节点尚未连接", Toast.LENGTH_SHORT).show();
                    }

                } else {
                    SPUtils.put(context, "S891", "250");
                    SPUtils.put(context, "S892", "600");
                }
                break;
            case "006086":
                bSos86 = SPUtils.contains(context, "S861");
                iWhere = 3;
                if (bSos86) {
                    String dS1 = (String) SPUtils.get(context, "S86L", "15");
                    String dS2 = (String) SPUtils.get(context, "S86H", "30");
                    String[] a = analysis.extractAmountMsg(nodeInfo.getData_analysis());
                    String dD = a[1];

                    String control = "WSN006038";
                    boolean bControl = SPUtils.contains(context, control);
                    if (bControl) {
                        String wsn = (String) SPUtils.get(context, control, "Hello");
                        if (dS1 == null || dD == null || dS2 == null) {
                            return;
                        }
                        if (Double.parseDouble(dD) > Double.parseDouble(dS2)) {
                            // 打开
                            if (bAuto) {
                                count32 = 0;
                                count33 = 0;
                                if (count31 < 2) {
                                    open(wsn, "0000");
                                    count31++;
                                }
                            }
                            tvMainSosLocal.setText("空气温度报警--温度过高");
                            SprayFragment.sosTv = "空气温度报警--温度过高";
                        } else if (Double.parseDouble(dD) < Double.parseDouble(dS1)) {
                            // 关闭
                            if (bAuto) {
                                count31 = 0;
                                count33 = 0;
                                if (count32 < 2) {
                                    open(wsn, "0001");
                                    count32++;
                                }
                            }
                            tvMainSosLocal.setText("空气温度报警--温度过低");
                            SprayFragment.sosTv = "空气温度报警--温度过低";
                        } else {
                            count31 = 0;
                            count32 = 0;
                            if (count33 < 2) {
                                open(wsn, "0001");
                                count33++;
                                tvMainSosLocal.setText("空气温度正常");
                                SprayFragment.sosTv = "空气温度正常";
                            }
                        }
                    } else {
                        Toast.makeText(context, "控制节点尚未连接", Toast.LENGTH_SHORT).show();
                    }


                } else {
                    SPUtils.put(context, "S86L", "15.5");
                    SPUtils.put(context, "S86H", "30.5");
                }
                break;
            case "006001":
                bSos01 = SPUtils.contains(context, "S01L");
                iWhere = 4;
                if (bSos01) {
                    // 设置警戒值
                    String dS1 = (String) SPUtils.get(context, "S01L", "5000");
                    String dS2 = (String) SPUtils.get(context, "S01H", "100000");

                    // 实际警戒值
                    String a = nodeInfo.getData_analysis();

                    String control = "WSN004000";
                    boolean bControl = SPUtils.contains(context, control);
                    if (bControl) {
                        String wsn = (String) SPUtils.get(context, control, "Hello");
                        if (Double.parseDouble(a) > Double.parseDouble(dS2)) {
                            // 关闭
                            count43 = 0;
                            count42 = 0;
                            if (bAuto) {
                                if (count41 < 2) {
                                    openSun(wsn, "0002");
                                    count41++;
                                }
                            }
                            tvMainSosLocal.setText("光照报警--光照偏强");
                            ShadeFragment.sosTv = "光照报警--光照偏强";
                        } else if (Double.parseDouble(a) < Double.parseDouble(dS1)) {
                            // 打开
                            count41 = 0;
                            count43 = 0;
                            if (bAuto) {
                                if (count42 < 2) {
                                    openSun(wsn, "0001");
                                    count41++;
                                }
                            }
                            tvMainSosLocal.setText("光照报警--光照偏弱");
                            ShadeFragment.sosTv = "光照报警--光照偏弱";
                        } else {
                            count41 = 0;
                            count42 = 0;
                            if (bAuto) {
                                if (count43 < 2) {
                                    openSun(wsn, "0002");
                                }
                            }
                            tvMainSosLocal.setText("光照正常");
                            ShadeFragment.sosTv = "光照正常";
                        }
                    } else {
                        Toast.makeText(context, "控制节点尚未连接", Toast.LENGTH_SHORT).show();
                    }

                } else {
                    SPUtils.put(context, "S01L", "5000");
                    SPUtils.put(context, "S01H", "100000");
                }
                break;
        }
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

    @OnClick({R.id.btn_openSerial,
            R.id.tv_show_clear,
            R.id.tv_main_light_open,
            R.id.tv_main_light_close,
            R.id.tv_main_sos_open,
            R.id.tv_main_sos_close,
            R.id.tv_main_sos_local,
            R.id.tv_main_setting_alert})
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
            case R.id.tv_main_sos_local:
                if (iWhere == 1) {
                    /*Intent intent = new Intent(MainActivity.this, ControlActivity.class);
                    intent.putExtra("id", 1);
                    startActivity(intent);*/
                } else if (iWhere == 2) {
                    /*Intent intent = new Intent(MainActivity.this, ControlActivity.class);
                    intent.putExtra("id", 2);
                    startActivity(intent);*/
                } else if (iWhere == 3) {
                    /*Intent intent = new Intent(MainActivity.this, ControlActivity.class);
                    intent.putExtra("id", 3);
                    startActivity(intent);*/
                } else if (iWhere == 4) {
                    /*Intent intent = new Intent(MainActivity.this, ControlActivity.class);
                    intent.putExtra("id", 4);
                    startActivity(intent);*/
                }
                break;

            case R.id.tv_main_setting_alert:
                startActivity(new Intent(context, SettingActivity.class));
                break;
        }
    }


    private void open(String str, String sStatus) {
        if (str == null || str.length() < 20) {
            return;
        }
        String open = "36" + str.substring(2, 34) + sStatus + str.substring(38, str.length());
        Log.i(TAG, "open: " + open);
        byte[] ff = this.string2byteArrays(open);
        try {
            mSerialport.sendData(ff, 0, ff.length);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void openSun(String str, String sStatus) {
        if (str == null || str.length() < 20) {
            return;
        }
        String open = "36" + str.substring(2, 28) + sStatus + str.substring(32, str.length());
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
