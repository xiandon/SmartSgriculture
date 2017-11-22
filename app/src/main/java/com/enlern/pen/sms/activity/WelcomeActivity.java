package com.enlern.pen.sms.activity;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.view.KeyEvent;
import android.view.View;
import android.widget.TextView;

import com.enlern.pen.sms.MainActivity;
import com.enlern.pen.sms.R;
import com.enlern.pen.sms.base.ActivityManager;
import com.enlern.pen.sms.storage.SPUtils;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * Created by pen on 2017/11/15.
 */

public class WelcomeActivity extends BaseActivity {
    @BindView(R.id.tv_public_title)
    TextView tvPublicTitle;
    @BindView(R.id.tv_title_setting)
    TextView tvTitleSetting;
    @BindView(R.id.tv_title_clean)
    TextView tvTitleClean;

    private Context mContext;
    public static String sJuin = "1";


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_welcome);
        ButterKnife.bind(this);
        mContext = WelcomeActivity.this;
        initView();


    }

    private void initView() {
        ActivityManager.getInstance().addActivity(this);
        tvPublicTitle.setText("上海因仑智慧农业");
        tvTitleClean.setVisibility(View.GONE);
        tvTitleSetting.setVisibility(View.GONE);

        SPUtils.put(mContext, "AUTO", "Manual");


    }

    @OnClick({R.id.tv_wel_agriculture, R.id.tv_wel_monitor, R.id.tv_wel_control, R.id.tv_wel_setting})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.tv_wel_agriculture:
                startActivity(new Intent(mContext, AgricultureActivity.class));
                break;
            case R.id.tv_wel_monitor:
                startActivity(new Intent(mContext, MonitorActivity.class));
                break;
            case R.id.tv_wel_control:
                startActivity(new Intent(mContext, ControlActivity.class));
                break;
            case R.id.tv_wel_setting:
                if (sJuin.equals("1")) {
                    startActivity(new Intent(mContext, MainActivity.class));
                } else if (sJuin.equals("2")) {
                    finish();
                }
                break;
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        switch (keyCode) {
            case KeyEvent.KEYCODE_BACK:
                showDialog(mContext);
                break;

            default:
                break;
        }
        return false;
    }

    private void showDialog(final Context context) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("您确定要退出本程序！").setCancelable(false)
                .setPositiveButton("确定", new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        ActivityManager.getInstance().appExit(context);
                    }
                }).setNegativeButton("取消", null);
        AlertDialog alert = builder.create();
        alert.show();
    }
}
