package com.enlern.pen.sms.activity;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.enlern.pen.sms.R;
import com.enlern.pen.sms.storage.SPUtils;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * Created by pen on 2017/11/21.
 */

public class SettingActivity extends BaseActivity {
    @BindView(R.id.tv_public_title)
    TextView tvPublicTitle;
    @BindView(R.id.tv_title_setting)
    TextView tvTitleSetting;
    @BindView(R.id.tv_title_clean)
    TextView tvTitleClean;
    @BindView(R.id.tv_setting_soil_l)
    EditText tvSettingSoilL;
    @BindView(R.id.tv_setting_soil_h)
    EditText tvSettingSoilH;
    @BindView(R.id.tv_setting_co_l)
    EditText tvSettingCoL;
    @BindView(R.id.tv_setting_co_h)
    EditText tvSettingCoH;
    @BindView(R.id.tv_setting_room_l)
    EditText tvSettingRoomL;
    @BindView(R.id.tv_setting_room_h)
    EditText tvSettingRoomH;
    @BindView(R.id.tv_setting_sun_l)
    EditText tvSettingSunL;
    @BindView(R.id.tv_setting_sun_h)
    EditText tvSettingSunH;
    private Context context;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_alert);
        ButterKnife.bind(this);
        context = SettingActivity.this;

        tvTitleClean.setVisibility(View.GONE);
        tvTitleSetting.setVisibility(View.GONE);

        tvSettingSoilL.setText((String) SPUtils.get(context, "S83L", "20"));
        tvSettingSoilH.setText((String) SPUtils.get(context, "S83H", "60"));

        tvSettingCoL.setText((String) SPUtils.get(context, "S89L", "250"));
        tvSettingCoH.setText((String) SPUtils.get(context, "S89H", "600"));

        tvSettingRoomL.setText((String) SPUtils.get(context, "S86L", "15"));
        tvSettingRoomH.setText((String) SPUtils.get(context, "S86H", "30"));

        tvSettingSunL.setText((String) SPUtils.get(context, "S01L", "50"));
        tvSettingSunH.setText((String) SPUtils.get(context, "S01H", "200"));


    }

    @OnClick({R.id.btn_setting_submit, R.id.btn_setting_cancel})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.btn_setting_submit:

                String str83l = tvSettingSoilL.getText().toString();
                String str83h = tvSettingSoilH.getText().toString();
                String str86l = tvSettingRoomL.getText().toString();
                String str86h = tvSettingRoomH.getText().toString();
                String str89l = tvSettingCoL.getText().toString();
                String str89h = tvSettingCoH.getText().toString();
                String str01l = tvSettingSunL.getText().toString();
                String str01h = tvSettingSunH.getText().toString();


                if (Double.parseDouble(str83l) < 15 || Double.parseDouble(str83h) > 80) {
                    Toast.makeText(context, "土壤湿度设置靠谱的值，请重新输入", Toast.LENGTH_SHORT).show();
                    return;
                } else if (Double.parseDouble(str89l) < 200 || Double.parseDouble(str89h) > 650) {
                    Toast.makeText(context, "CO2浓度设置靠谱的值，请重新输入", Toast.LENGTH_SHORT).show();
                    return;
                } else if (Double.parseDouble(str86l) < 10 || Double.parseDouble(str86h) > 35) {
                    Toast.makeText(context, "空气温度设置靠谱的值，请重新输入", Toast.LENGTH_SHORT).show();
                    return;
                } else if (Double.parseDouble(str01l) < 1000 || Double.parseDouble(str01h) > 100000) {
                    Toast.makeText(context, "光照强度设置靠谱的值，请重新输入", Toast.LENGTH_SHORT).show();
                    return;
                }


                SPUtils.put(context, "S83L", str83l);
                SPUtils.put(context, "S83H", str83h);

                SPUtils.put(context, "S89L", str89l);
                SPUtils.put(context, "S89H", str89h);

                SPUtils.put(context, "S86L", str86l);
                SPUtils.put(context, "S86H", str86h);

                SPUtils.put(context, "S01L", str01l);
                SPUtils.put(context, "S01H", str01h);

                Toast.makeText(context, "设置成功", Toast.LENGTH_SHORT).show();

                finish();

                break;
            case R.id.btn_setting_cancel:
                finish();
                break;
        }
    }
}
