package com.enlern.pen.sms.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.enlern.pen.sms.MainActivity;
import com.enlern.pen.sms.R;
import com.enlern.pen.sms.adapter.AgricultureAdapter;
import com.enlern.pen.sms.base.ActivityManager;
import com.enlern.pen.sms.serial.BroadcastMain;
import com.enlern.pen.sms.serial.RecCallBack;
import com.xiandon.wsn.node.NodeInfo;
import com.xiandon.wsn.node.SmsAnalysis;
import com.xiandon.wsn.node.SmsAnalysisV2;

import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * Created by pen on 2017/11/16.
 */

public class AgricultureActivity extends BaseActivity {

    @BindView(R.id.tv_public_title)
    TextView tvPublicTitle;
    @BindView(R.id.recyclerView_node)
    RecyclerView recyclerViewNode;
    @BindView(R.id.tv_title_setting)
    TextView tvTitleSetting;

    private Context mContext;
    private AgricultureAdapter adapter;
    private BroadcastMain broadcastMain;

    private SmsAnalysisV2 analysis;
    private String TAG = "AgricultureActivity";


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_agriculture);
        ButterKnife.bind(this);
        mContext = AgricultureActivity.this;
        initView();
    }

    private void initView() {
        ActivityManager.getInstance().addActivity(this);
        tvPublicTitle.setText("环境信息");
        adapter = new AgricultureAdapter(mContext);
        if (MainActivity.getBoolean) {
            tvTitleSetting.setVisibility(View.GONE);
        }

        GridLayoutManager manager = new GridLayoutManager(mContext, 4);
        manager.setOrientation(GridLayoutManager.VERTICAL);
        recyclerViewNode.setLayoutManager(manager);
        //添加分割线
        recyclerViewNode.addItemDecoration(new DividerItemDecoration(
                this, DividerItemDecoration.VERTICAL));

        recyclerViewNode.setAdapter(adapter);

        analysis = new SmsAnalysisV2(mContext);

        broadcastMain = new BroadcastMain();
        broadcastMain.setCallBack(new RecCallBack() {
            @Override
            public void nodeRec(String rec) {
                try {
                    NodeInfo info = analysis.analysis(rec);
                    Log.i(TAG, "nodeRec: " + info.getWsn());
                    if (info != null && MainActivity.getBoolean) {
                        adapter.update(info);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (XmlPullParserException e) {
                    e.printStackTrace();
                }
            }
        });

    }

    @OnClick({R.id.tv_title_setting, R.id.tv_title_clean})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.tv_title_setting:
                startActivity(new Intent(mContext, MainActivity.class));
                ActivityManager.getInstance().finishActivity(AgricultureActivity.this);
                break;
            case R.id.tv_title_clean:
                adapter.clearData();
                break;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}
