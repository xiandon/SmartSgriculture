package com.enlern.pen.sms.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;
import android.widget.TextView;

import com.enlern.pen.sms.MainActivity;
import com.enlern.pen.sms.R;
import com.enlern.pen.sms.base.ActivityManager;
import com.enlern.pen.sms.fragment.IrrigationFragment;
import com.enlern.pen.sms.fragment.ShadeFragment;
import com.enlern.pen.sms.fragment.SprayFragment;
import com.enlern.pen.sms.fragment.VentilationFragment;
import com.google.common.collect.Lists;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import github.chenupt.multiplemodel.viewpager.ModelPagerAdapter;
import github.chenupt.multiplemodel.viewpager.PagerManager;
import github.chenupt.springindicator.SpringIndicator;
import github.chenupt.springindicator.viewpager.ScrollerViewPager;

/**
 * Created by pen on 2017/11/16.
 */

public class ControlActivity extends BaseActivity {
    @BindView(R.id.view_pager_system)
    ScrollerViewPager viewPagerSystem;
    @BindView(R.id.indicator)
    SpringIndicator indicator;
    @BindView(R.id.tv_public_title)
    TextView tvPublicTitle;
    @BindView(R.id.tv_title_clean)
    TextView tvTitleClean;
    @BindView(R.id.tv_title_setting)
    TextView tvTitleSetting;
    private Context mContext;

    public static boolean bControl = false;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_system);
        ButterKnife.bind(this);
        mContext = ControlActivity.this;
        initView();
    }

    private void initView() {
        ActivityManager.getInstance().addActivity(this);
        tvTitleClean.setVisibility(View.GONE);
        if (MainActivity.getBoolean) {
            tvTitleSetting.setVisibility(View.GONE);
        }

        int id = getIntent().getIntExtra("id", 0);
        if (id == 1) {
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.view_pager_system, new IrrigationFragment())
                    .addToBackStack(null)
                    .commit();
        } else if (id == 2) {
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.view_pager_system, new VentilationFragment())
                    .addToBackStack(null)
                    .commit();
        } else if (id == 3) {
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.view_pager_system, new SprayFragment())
                    .addToBackStack(null)
                    .commit();
        } else if (id == 4) {
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.view_pager_system, new ShadeFragment())
                    .addToBackStack(null)
                    .commit();
        }


        bControl = true;

        tvPublicTitle.setText("环境控制系统");
        MainActivity.getBoolean = true;
        PagerManager manager = new PagerManager();
        manager.setTitles(getTitles());
        manager.addFragment(new IrrigationFragment());
        manager.addFragment(new VentilationFragment());
        manager.addFragment(new SprayFragment());
        manager.addFragment(new ShadeFragment());
        ModelPagerAdapter adapter = new ModelPagerAdapter(getSupportFragmentManager(), manager);

        viewPagerSystem.setAdapter(adapter);

        viewPagerSystem.fixScrollSpeed();
        indicator.setViewPager(viewPagerSystem);
    }

    private List<String> getTitles() {
        return Lists.newArrayList("灌溉系统", "通风系统", "喷雾系统", "遮阳系统");
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        bControl = false;
    }

    @OnClick({R.id.tv_title_setting})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.tv_title_setting:
                startActivity(new Intent(mContext, MainActivity.class));
                ActivityManager.getInstance().finishActivity();
                break;
        }
    }
}
