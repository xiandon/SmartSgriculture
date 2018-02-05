package com.enlern.pen.sms.fragment;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.enlern.pen.sms.MainActivity;
import com.enlern.pen.sms.R;
import com.enlern.pen.sms.activity.ControlActivity;
import com.enlern.pen.sms.serial.BroadcastMain;
import com.enlern.pen.sms.serial.RecCallBack;
import com.enlern.pen.sms.storage.SPUtils;
import com.xiandon.wsn.node.NodeInfo;
import com.xiandon.wsn.node.SmsAnalysis;
import com.xiandon.wsn.node.SmsAnalysisV2;
import com.xiandon.wsn.serial.SerialPortDownload;

import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;

import android_serialport_api.SerialPort;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;


/**
 * 喷雾系统
 * Created by pen on 2017/10/18.
 */

public class SprayFragment extends BaseFragment {
    @BindView(R.id.tv_control_name)
    TextView tvControlName;
    @BindView(R.id.tv_control_data)
    TextView tvControlData;
    @BindView(R.id.tv_control_num)
    TextView tvControlNum;
    @BindView(R.id.tv_control_chip)
    TextView tvControlChip;
    @BindView(R.id.tv_control_system_num)
    TextView tvControlSystemNum;
    @BindView(R.id.tv_control_frame_num)
    TextView tvControlFrameNum;

    @BindView(R.id.tv_control_name_c)
    TextView tvControlNameC;
    @BindView(R.id.tv_control_data_c)
    TextView tvControlDataC;
    @BindView(R.id.tv_control_num_c)
    TextView tvControlNumC;
    @BindView(R.id.tv_control_chip_c)
    TextView tvControlChipC;
    @BindView(R.id.tv_control_system_num_c)
    TextView tvControlSystemNumC;
    @BindView(R.id.tv_control_frame_num_c)
    TextView tvControlFrameNumC;


    Unbinder unbinder;
    @BindView(R.id.tv_control_alert)
    TextView tvControlAlert;
    @BindView(R.id.tv_control_auto)
    TextView tvControlAuto;
    @BindView(R.id.btn_control_open)
    Button btnControlOpen;
    @BindView(R.id.btn_control_close)
    Button btnControlClose;
    @BindView(R.id.tv_control_sos_tv)
    TextView tvControlSosTv;
    private String TAG = "SprayFragment";
    private View view;
    private BroadcastMain broad;

    private Context context;

    private SmsAnalysisV2 analysis;

    private SerialPortDownload download;
    private SerialPort mSerialPort;

    private boolean bSave = false;
    private int a;
    private int b;

    public static String sosTv = "";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.layout_control, container, false);
        context = getActivity();
        unbinder = ButterKnife.bind(this, view);
        initViews();
        return view;
    }

    private void initViews() {
        a = this.getResources().getColor(R.color.white);
        b = this.getResources().getColor(R.color.gray);

        Drawable topLeft = getResources().getDrawable(R.drawable.if_temperature_air);
        Drawable topRight = getResources().getDrawable(R.drawable.if_spray);

        analysis = new SmsAnalysisV2(context);

        download = new SerialPortDownload();
        String PATH = (String) SPUtils.get(getActivity(), "PATH", "/dev/ttyUSB0");
        String RATE = (String) SPUtils.get(getActivity(), "RATE", "9600");
        try {
            mSerialPort = download.open(PATH, RATE);
        } catch (IOException e) {
            e.printStackTrace();
        }

        tvControlName.setCompoundDrawablesWithIntrinsicBounds(null, topLeft, null, null);
        tvControlNameC.setCompoundDrawablesWithIntrinsicBounds(null, topRight, null, null);
        tvControlName.setText("空气温湿度");
        tvControlNameC.setText("喷雾系统");

        broad = new BroadcastMain();
        broad.setCallBack(new RecCallBack() {
            @Override
            public void nodeRec(String rec) {
                Message message = Message.obtain();
                message.what = 1;
                message.obj = rec;
                handler.sendMessage(message);
            }
        });

        if (MainActivity.getBoolean) {
            handler.postDelayed(runnable, 0);
        }

    }

    private Handler handler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            switch (msg.what) {
                case 0:
                    initViews();
                    break;
                case 1:
                    String rec = (String) msg.obj;
                    try {
                        NodeInfo info = analysis.analysis(rec);
                        if (info != null && MainActivity.getBoolean && ControlActivity.bControl) {
                            bSave = true;
                            write(info);
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    } catch (XmlPullParserException e) {
                        e.printStackTrace();
                    }
                    break;

                case 3:
                    break;
            }
            return false;
        }
    });

    private void write(NodeInfo info) {
        if (info.getNode_num().equals("0086")) {
            tvControlName.setTextColor(a);
            tvControlName.setText(info.getNode_name());
            tvControlData.setText(info.getData_analysis());
            tvControlNum.setText(info.getNode_num());
            tvControlChip.setText(info.getChip_type());
            tvControlSystemNum.setText(info.getSys_board());
            tvControlFrameNum.setText(info.getFrame_num());
            String alert1 = (String) SPUtils.get(context, "S86L", "15");
            String alert2 = (String) SPUtils.get(context, "S86H", "30");
            tvControlAlert.setText(alert1 + " ~ " + alert2 + " ℃");
        } else if (info.getNode_num().equals("0038")) {
            tvControlNameC.setTextColor(a);
            tvControlNameC.setText(info.getNode_name());
            tvControlDataC.setText(info.getData_analysis());
            tvControlNumC.setText(info.getNode_num());
            tvControlChipC.setText(info.getChip_type());
            tvControlSystemNumC.setText(info.getSys_board());
            tvControlFrameNumC.setText(info.getFrame_num());
            String auto = (String) SPUtils.get(context, "AUTOS", "Manual");
            tvControlSosTv.setText(sosTv);
            if (auto.equals("Auto")) {
                btnControlOpen.setVisibility(View.GONE);
                btnControlClose.setVisibility(View.GONE);
            } else {
                btnControlOpen.setVisibility(View.VISIBLE);
                btnControlClose.setVisibility(View.VISIBLE);
            }
            tvControlAuto.setText(auto);
        }
    }

    private void writeNull() {
        tvControlName.setTextColor(b);
        tvControlNameC.setTextColor(b);
    }


    @Override
    protected void lazyLoad() {
        handler.sendEmptyMessage(0);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        unbinder.unbind();
        handler.removeCallbacks(runnable);

    }

    @OnClick({R.id.btn_control_open, R.id.btn_control_close, R.id.tv_control_alert})
    public void onViewClicked(View view) {
        boolean bSave = SPUtils.contains(context, "SAVE" + "0038");
        if (!bSave) {
            Toast.makeText(context, "   请等待设备连接", Toast.LENGTH_SHORT).show();
        }
        String wsn = (String) SPUtils.get(context, "SAVE" + "0038", "ll");

        switch (view.getId()) {
            case R.id.btn_control_open:
                open(wsn, "0000");
                break;
            case R.id.btn_control_close:
                open(wsn, "0001");
                break;

            case R.id.tv_control_alert:
                break;
        }
    }

    private void open(String str, String sStatus) {
        if (str == null || str.length() < 20) {
            return;
        }
        String open = "36" + str.substring(2, 28) + sStatus + str.substring(32, str.length());

        download.DownData(open);
    }

    private Runnable runnable = new Runnable() {
        @Override
        public void run() {
            handler.postDelayed(this, 30000);
            writeNull();
        }
    };
}
